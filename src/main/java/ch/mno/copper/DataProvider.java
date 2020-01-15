package ch.mno.copper;

import java.util.List;

import ch.mno.copper.collect.StoryTask;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.stories.data.Story;

public interface DataProvider {

    List<Story> getStories();

    StoryTask getStoryTask(Story story);

    ValuesStore getValuesStore();
}
