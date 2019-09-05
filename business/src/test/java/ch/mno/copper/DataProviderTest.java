package ch.mno.copper;

import ch.mno.copper.collect.StoryTask;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.store.MapValuesStore;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.stories.StoriesFacade;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import ch.mno.copper.stories.data.StoryValidationResult;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataProviderTest {

    @Test
    public void testAfterNewStory() throws IOException, ConnectorException {
        StoriesFacadeMock storiesFacade = new StoriesFacadeMock();
        MapValuesStore valuesStore = new MapValuesStore();
        DataProviderImpl dataprovider = new DataProviderImpl(storiesFacade, valuesStore);
        StoryGrammar storyGrammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));

        // Initial: zero stories
        Assert.assertEquals(0,dataprovider.getStories().size());

        // Add one story
        storiesFacade.stories.put("story1", new Story(storyGrammar, "MyStory1Name", "someData"));
        Assert.assertEquals(1,dataprovider.getStories().size());

        // Get story
        Assert.assertEquals("someData\n", dataprovider.getStories().get(0).getStoryText());

        // Update story
        storiesFacade.stories.put("story1", new Story(storyGrammar, "MyStory1Name", "someDataNew"));

        // Get story new
        Assert.assertEquals("someDataNew\n", dataprovider.getStories().get(0).getStoryText());
    }


    private class StoriesFacadeMock implements StoriesFacade {

        Map<String, Story> stories = new HashMap<>();

        @Override
        public StoryGrammar getGrammar() {
            return null;
        }

        @Override
        public List<Story> getStories(boolean shouldRefreshFromDisk) {
            return new ArrayList<>(stories.values());
        }

        @Override
        public Story buildStory(FileInputStream fileInputStream, Path path) throws IOException, ConnectorException {
            return null;
        }

        @Override
        public StoryTask buildStoryTask(Story story, ValuesStore valuesStore) {
            return null;
        }

        @Override
        public String saveNewStory(String storyName, String storyText) throws IOException, ConnectorException {
            return null;
        }

        @Override
        public String updateStory(String originalStoryName, String storyName, String storyText) throws IOException, ConnectorException {
            return null;
        }

        @Override
        public Story getStoryByName(String name) {
            return stories.get(name);
        }

        @Override
        public void deleteStory(String storyName) {
            stories.remove(storyName);
        }

        @Override
        public void refreshFromDisk() {

        }

        @Override
        public StoryValidationResult validate(String story) {
            return null;
        }
    }

}
