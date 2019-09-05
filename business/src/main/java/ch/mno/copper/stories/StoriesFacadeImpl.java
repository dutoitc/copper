package ch.mno.copper.stories;

import ch.mno.copper.collect.StoryTask;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.helpers.SyntaxException;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.stories.data.StoriesHolder;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import ch.mno.copper.stories.data.StoryValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dutoitc on 23.02.2016.
 */
public class StoriesFacadeImpl implements StoriesFacade {
    private Logger LOG = LoggerFactory.getLogger(StoriesFacadeImpl.class);

    private StoriesHolder storiesHolder = new StoriesHolder();
    private StoryGrammar grammar;

    public StoriesFacadeImpl() {
        grammar = new StoryGrammar(StoriesFacadeImpl.class.getResourceAsStream("/StoryGrammar.txt"));
    }

    @Override
    public StoryGrammar getGrammar() {
        return grammar;
    }

    @Override
    public List<Story> getStories(boolean shouldRefreshFromDisk) {
        if (shouldRefreshFromDisk) refreshFromDisk();
        return storiesHolder.getStories();
    }

    @Override
    public Story buildStory(FileInputStream fileInputStream, Path path) throws IOException, ConnectorException {
       return new Story(grammar, fileInputStream, path);
    }

    @Override
    public StoryTask buildStoryTask(Story story, ValuesStore valuesStore) {
        return StoryTaskBuilder.build(story, valuesStore);
    }

    @Override
    public String saveNewStory(String storyName, String storyText) throws IOException, ConnectorException {
        Story story = new Story(grammar, storyName, storyText);
        DiskHelper.saveNewStory(story.getSource().toFile(), story.getStoryText());
        storiesHolder.add(story); // TODO: update listeneres
        return "Ok";
    }

    @Override
    public String updateStory(String originalStoryName, String storyName, String storyText) throws IOException, ConnectorException {
        if (!DiskHelper.storyExists(originalStoryName)) {
            return "Error: the file " + originalStoryName + " does not exist.";
        }

        Story originalStory = getStoryByName(originalStoryName);
        Story story = new Story(grammar, storyName, storyText);

        // Update
        DiskHelper.updateStory(story.getSource().toFile(), story.getStoryText());
        storiesHolder.replace(originalStory, story);

        // TODO: update listeneres
        if (!originalStoryName.equals(storyName)) DiskHelper.deleteStory(originalStoryName);;
        return "Ok";
    }

    @Override
    public Story getStoryByName(String name) {
        return storiesHolder.findStoryByName(name);
    }

    @Override
    public void deleteStory(String storyName) {
        Story story = getStoryByName(storyName);
        DiskHelper.deleteStory(storyName);
        storiesHolder.remove(story);
    }

    @Override
    public void refreshFromDisk() {
        DiskHelper.ensureStoriesFolderExists();

        List<String> storyNames = DiskHelper.findStoryNames();

        // TODO: keep story hash in memory. If different, reload. And test.
        for (String storyName : storyNames) {
            try (
                    FileInputStream fileInputStream = DiskHelper.getStoryAsStream(storyName)
            )
            {
                Story diskStory = buildStory(fileInputStream, new File(storyName).toPath());
                Story holderStory = getStoryByName(storyName);
                storiesHolder.removeIfError(storyName);

                // New
                if (holderStory == null) {
                    LOG.info("Adding story: " + diskStory.getSource().getFileName().toString()); // TODO: should create story with storyName, not path ?
                    storiesHolder.add(diskStory);
                } else if (!holderStory.getStoryText().equals(diskStory.getStoryText())) {
                    // Reload if changed
                    storiesHolder.remove(holderStory);
                    storiesHolder.add(diskStory);
                }
            } catch (SyntaxException e) {
                storiesHolder.markAsError(storyName);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ConnectorException e) {
                e.printStackTrace();
            }
        }

        // Evict stories from memory if not present on disk
        for (Story story: new ArrayList<>(this.storiesHolder.getStories())) {
            if (!DiskHelper.storyExists(story.getName())) {
                LOG.info("Removing story from memory, no longer on disk: " + story.getName());
                this.storiesHolder.remove(story);
            }
        }

        // TODO: update daemon, web (listeners)
    }

    @Override
    public StoryValidationResult validate(String story) {
        return new StoryValidator(grammar).validate(story);
    }
}