package ch.mno.copper.stories;

import ch.mno.copper.ValuesStore;
import ch.mno.copper.collect.AbstractCollectorWrapper;
import ch.mno.copper.collect.CollectorTask;
import ch.mno.copper.collect.CollectorTaskImpl;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.report.AbstractReporterWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by dutoitc on 23.02.2016.
 */
public class StoriesFacade {

    private static final StoriesFacade instance = new StoriesFacade();

    private List<Story> stories = new ArrayList<>();
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

    public List<Story> getStories() {
        return stories;
    }


    public Story buildStory(FileInputStream fileInputStream, Path path) throws IOException, ConnectorException {
        Story story = new Story(grammar,fileInputStream, path);
        stories.add(story);
        return story;
    }

    public List<CollectorTask> buildCollectors(ValuesStore valuesStore) {
        List<CollectorTask> collectorTasks = new ArrayList<>();

        stories.forEach(story-> {
            collectorTasks.add(
                    new CollectorTaskImpl(story, () -> {
                        // This code execute at every trigger (cron, ...) for the given story
                        try {
                            Map<String, String> values;
                            AbstractCollectorWrapper collectorWrapper = story.getCollectorWrapper();
                            if (collectorWrapper == null) { // Null means to read value store
                                values = valuesStore.getValuesMapString();
                            } else {
                                values = collectorWrapper.execute();
                            }

                            // TODO: trigg reporter based on values checked
                            AbstractReporterWrapper reporter = story.getReporterWrapper();
                            if (reporter == null) {
                                values.forEach((key, value) -> valuesStore.put(key, value));
                            } else {
                                reporter.execute(values);
                            }
                        } catch (ConnectorException e) {
                            e.printStackTrace();
                        }
                    }, story.getCron())
            );
        });
        return collectorTasks;
    }

    public String saveNewStory(String storyName,String storyText) throws IOException, ConnectorException {
        Story story = new Story(grammar,storyName, storyText);
        File file = story.getSource().toFile();
        if (file.exists()) {
            return "Error: the file " + file.getName() + " already exists.";
        }
        FileWriter fw = new FileWriter(file);
        fw.write(story.getStoryText());
        fw.flush();
        fw.close();
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

        Story story = new Story(grammar,storyName, storyText);
        File file = story.getSource().toFile();


        FileWriter fw = new FileWriter(file);
        fw.write(story.getStoryText());
        fw.flush();
        fw.close();
        stories.add(story); // TODO: update listeneres
        stories.remove(originalStory);
        if (!oldFile.getName().equals(file.getName())) oldFile.delete();
        return "Ok";
    }

    public Story getStoryByName(String name) {
        List<Story> storyList = stories.stream().filter(s->s.getName().equals(name)).collect(Collectors.toList());
        if (storyList.isEmpty()) {
            return null;
        } else if (storyList.size()>1) {
            throw new RuntimeException("Error: story name are not unique for " + name);
        }
        return storyList.get(0);
    }
}
