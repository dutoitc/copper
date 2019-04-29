package ch.mno.copper.stories;

import ch.mno.copper.collect.AbstractCollectorWrapper;
import ch.mno.copper.collect.StoryTask;
import ch.mno.copper.collect.StoryTaskImpl;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.data.ValuesStore;
import ch.mno.copper.helpers.SyntaxException;
import ch.mno.copper.report.AbstractReporterWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by dutoitc on 23.02.2016.
 */
public class StoriesFacade {
    private Logger LOG = LoggerFactory.getLogger(StoriesFacade.class);

    private static final StoriesFacade instance = new StoriesFacade();

    private List<Story> stories = new ArrayList<>();
    private Set<String> storiesInError = new HashSet<>();
    private StoryGrammar grammar;

    private StoriesFacade() {
        grammar = new StoryGrammar(StoriesFacade.class.getResourceAsStream("/StoryGrammar.txt"));
    }

    public static StoriesFacade getInstance() {
        return instance;
    }

    public StoryGrammar getGrammar() {
        return grammar;
    }

    public List<Story> getStories(boolean shouldRefreshFromDisk) {
        if (shouldRefreshFromDisk) refreshFromDisk();
        return stories;
    }


    public Story buildStory(FileInputStream fileInputStream, Path path) throws IOException, ConnectorException {
        LOG.info("Adding story: " + path.getFileName().toString());
        Story story = new Story(grammar, fileInputStream, path);
        stories.add(story);
        return story;
    }

    public StoryTask buildStoryTask(Story story, ValuesStore valuesStore) {
        return new StoryTaskImpl(story, () -> {
            // This code execute at every trigger (cron, ...) for the given story
            try {
                Map<String, String> values;

                // Collect
                AbstractCollectorWrapper collectorWrapper = story.getCollectorWrapper();
                if (collectorWrapper == null) { // Null means to readInstant value store
                    values = valuesStore.getValuesMapString();
                } else {
                    values = collectorWrapper.execute();
                }
                if (!story.matchWhen(values, valuesStore)) {
                    // Not matching WHEN->ignore report
                    return;
                }

                if (values==null) {
                    LOG.warn("Story " + story.getName() + " returned null values; trying to continue with valuesStore...");
                    values = valuesStore.getValuesMapString();
                }


                // Report
                AbstractReporterWrapper reporter = story.getReporterWrapper();
                if (reporter == null) {
                    values.forEach((key, value) -> valuesStore.put(key, value));
                } else {
                    reporter.execute(values);
                }
            } catch (ConnectorException e) {
                e.printStackTrace();
            }
        }, story.getCron());
    }

    public String saveNewStory(String storyName, String storyText) throws IOException, ConnectorException {
        Story story = new Story(grammar, storyName, storyText);
        File file = story.getSource().toFile();
        if (file.exists()) {
            return "Error: the file " + file.getName() + " already exists.";
        }
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(story.getStoryText());
            fw.flush();
            fw.close();
        }
        stories.add(story); // TODO: update listeneres
        return "Ok";
    }

    public String updateStory(String originalStoryName, String storyName, String storyText) throws IOException, ConnectorException {
        File oldFile = new File("stories/" + originalStoryName);
        if (!oldFile.exists()) {
            return "Error: the file " + originalStoryName + " does not exist.";
        }

        // Remove old story
        Story originalStory = getStoryByName(originalStoryName);

        Story story = new Story(grammar, storyName, storyText);
        File file = story.getSource().toFile();


        try (FileWriter fw = new FileWriter(file)) {
            fw.write(story.getStoryText());
            fw.flush();
            fw.close();
        }
        stories.remove(originalStory);
        stories.add(story); // TODO: update listeneres
        if (!oldFile.getName().equals(file.getName())) oldFile.delete();
        return "Ok";
    }

    public Story getStoryByName(String name) {
        String[] spl = name.split("/");
        String smallName = spl.length == 1 ? name : spl[spl.length - 1];
        List<Story> storyList = stories.stream().filter(s -> s.getName().equals(smallName)).collect(Collectors.toList());
        if (storyList.isEmpty()) {
            return null;
        } else if (storyList.size() > 1) {
            throw new RuntimeException("Error: story name are not unique for " + smallName);
        }
        return storyList.get(0);
    }

    public void deleteStory(String storyName) {
        Story story = getStoryByName(storyName);
        stories.remove(story);
        File file = new File("stories/" + storyName);
        file.delete();
    }

    public void refreshFromDisk() {
        File storiesFolder = new File("stories");
        if (storiesFolder.isFile()) throw new RuntimeException("stories should be a folder, not a file");
        if (!storiesFolder.exists()) storiesFolder.mkdir();

        // Load files: yet use sample values if none is specified
        File stories = new File("stories");
        if (!stories.exists()) {
            LOG.error("Folder not found: 'stories', cannot refresh stories from disk.");
            return;
        }

        List<String> files = new ArrayList<>();
        for (File file : stories.listFiles(f -> f.isFile() && !f.getName().toLowerCase().endsWith("swp"))) {
            files.add("stories/" + file.getName());
        }

        // TODO: keep story hash in memory. If different, reload. And test.
        for (String filename : files) {
            try {
                if (getStoryByName(filename) == null) {
                    try (FileInputStream fileInputStream = new FileInputStream(filename)) {
                        buildStory(fileInputStream, new File(filename).toPath());
                    }
                    if (storiesInError.contains(filename)) {
                        storiesInError.remove(filename);
                        System.out.println("No longer in error: " + filename);
                    }
                }
            } catch (SyntaxException e) {
                if (!storiesInError.contains(filename)) {
                    storiesInError.add(filename);
                    System.err.println("Wrong syntax for " + filename+", ignoring until error fixed on disk."); // FIXME: put story as dirty
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ConnectorException e) {
                e.printStackTrace();
            }
        }

        Iterator<Story> it = this.stories.iterator();
        while (it.hasNext()) {
            Story s = it.next();
            if (!new File("stories/" + s.getName()).exists()) {
                LOG.info("Removing story from memory, no longer on disk: " + s.getName());
                it.remove();
            }
        }

        // TODO: update daemon, web (listeners)
    }

    public StoryValidationResult validate(String story) {
        StoryValidationResult result = new StoryValidationResult(story);
        // TODO: validation
        List<String> blacklist = Arrays.asList("EOL", "SPACE_EOL", "SPACE", "CRON_EL");
        for (String key: grammar.getKeys()) {
            if (blacklist.contains(key)) continue;

            String patternFull = grammar.getPatternFull(key);
            String patternShort = grammar.getPattern(key);
            Pattern pattern = Pattern.compile(patternFull);
            if (patternFull.length()<3) continue;
            if (pattern.matcher(story).matches()) {
                result.addPerfectMatch(0, story.length(), key, patternShort, patternFull);
            } else {
                Matcher matcher = pattern.matcher(story);
                while (matcher.find()) {
                    result.addPartialMatch(matcher.start(), matcher.end(), key, patternShort, patternFull);
                }
            }
        }
        return result;
    }
}
