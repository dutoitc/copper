package ch.mno.copper.stories;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import ch.mno.copper.collect.StoryTask;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import ch.mno.copper.stories.data.StoryValidationResult;

public interface StoriesFacade {
    StoryGrammar getGrammar();

    List<Story> getStories(boolean shouldRefreshFromDisk);

    Story buildStory(FileInputStream fileInputStream, String storyName) throws IOException;

    StoryTask buildStoryTask(Story story, ValuesStore valuesStore);

    String saveNewStory(String storyName, String storyText) throws IOException, ConnectorException;

    String updateStory(String originalStoryName, String storyName, String storyText) throws IOException, ConnectorException;

    Story getStoryByName(String name);

    void deleteStory(String storyName);

    void refreshFromDisk();

    StoryValidationResult validate(String story);
}
