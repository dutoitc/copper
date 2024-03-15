package ch.mno.copper.stories;

import ch.mno.copper.collect.StoryTask;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.helpers.SyntaxException;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.stories.data.StoriesHolder;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import ch.mno.copper.stories.data.StoryValidationResult;
import config.CopperMailProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dutoitc on 23.02.2016.
 */
@Component
public class StoriesFacadeImpl implements StoriesFacade {
    private static final Logger LOG = LoggerFactory.getLogger(StoriesFacadeImpl.class);

    private final StoriesHolder storiesHolder = new StoriesHolder();
    private final StoryGrammar grammar;
    private final DiskHelper diskHelper;
    private final StoryTaskBuilder storyTaskBuilder;

    private final CopperMailProperties copperMailProperties;

    public StoriesFacadeImpl(DiskHelper diskHelper, StoryTaskBuilder storyTaskBuilder, StoryGrammar grammar, CopperMailProperties copperMailProperties) {
        this.diskHelper = diskHelper;
        this.storyTaskBuilder = storyTaskBuilder;
        this.grammar = grammar;
        this.copperMailProperties = copperMailProperties;
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
    public Story buildStory(FileInputStream fileInputStream, String storyName) throws IOException, ConnectorException {
        return new Story(grammar, fileInputStream, storyName, copperMailProperties);
    }

    @Override
    public StoryTask buildStoryTask(Story story, ValuesStore valuesStore) {
        return storyTaskBuilder.build(story, valuesStore);
    }

    @Override
    public String saveNewStory(String storyName, String storyText) throws IOException {
        Story story = new Story(grammar, storyName, storyText, copperMailProperties);
        diskHelper.saveNewStory(storyName, story.getStoryText());
        storiesHolder.add(story); // TODO: update listeneres
        return "Ok";
    }

    @Override
    public String updateStory(String originalStoryName, String storyName, String storyText) throws IOException {
        if (!diskHelper.storyExists(originalStoryName)) {
            return "Error: the file " + originalStoryName + " does not exist.";
        }

        Story originalStory = getStoryByName(originalStoryName);
        Story story = new Story(grammar, storyName, storyText, copperMailProperties);

        // Update
        diskHelper.updateStory(story.getName(), story.getStoryText());
        storiesHolder.replace(originalStory, story);

        // TODO: update listeneres
        if (!originalStoryName.equals(storyName)) diskHelper.deleteStory(originalStoryName);
        return "Ok";
    }

    @Override
    public Story getStoryByName(String name) {
        return storiesHolder.findStoryByName(name);
    }

    @Override
    public void deleteStory(String storyName) {
        Story story = getStoryByName(storyName);
        diskHelper.deleteStory(storyName);
        storiesHolder.remove(story);
    }

    @Override
    public void refreshFromDisk() {
        diskHelper.ensureStoriesFolderExists();

        List<String> storyNames = diskHelper.findStoryNames();

        // TODO: keep story hash in memory. If different, reload. And test.
        for (String storyName : storyNames) {
            try (
                    FileInputStream fileInputStream = diskHelper.getStoryAsStream(storyName)
            ) {
                Story diskStory = buildStory(fileInputStream, storyName);
                Story holderStory = getStoryByName(storyName);
                storiesHolder.removeIfError(storyName);

                // New
                if (holderStory == null) {
                    LOG.info("Adding story: {}", diskStory.getName());
                    storiesHolder.add(diskStory);
                } else if (!holderStory.getStoryText().equals(diskStory.getStoryText())) {
                    // Reload if changed
                    storiesHolder.remove(holderStory);
                    storiesHolder.add(diskStory);
                }
            } catch (SyntaxException e) {
                storiesHolder.markAsError(storyName);
            } catch (IOException | ConnectorException e) {
                LOG.error(e.getMessage(), e);
            } catch (RuntimeException e) {
                // Too verbose yet: invalid stories log errors on each story refresh, every second or so
            }
        }

        // Evict stories from memory if not present on disk
        for (Story story : new ArrayList<>(this.storiesHolder.getStories())) {
            if (!diskHelper.storyExists(story.getName())) {
                LOG.info("Removing story from memory, no longer on disk: {}", story.getName());
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