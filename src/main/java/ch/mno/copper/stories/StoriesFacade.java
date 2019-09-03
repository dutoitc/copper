package ch.mno.copper.stories;

import ch.mno.copper.collect.StoryTask;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import ch.mno.copper.stories.data.StoryValidationResult;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface StoriesFacade {
    StoryGrammar getGrammar();

    List<Story> getStories(boolean shouldRefreshFromDisk);

    Story buildStory(FileInputStream fileInputStream, Path path) throws IOException, ConnectorException;

    StoryTask buildStoryTask(Story story, ValuesStore valuesStore);

    String saveNewStory(String storyName, String storyText) throws IOException, ConnectorException;

    String updateStory(String originalStoryName, String storyName, String storyText) throws IOException, ConnectorException;

    Story getStoryByName(String name);

    void deleteStory(String storyName);

    void refreshFromDisk();

    StoryValidationResult validate(String story);
}
