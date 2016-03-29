package ch.mno.copper;

import ch.mno.copper.collect.StoryTask;
import ch.mno.copper.stories.Story;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by dutoitc on 25.02.2016.
 */
public interface DataProvider {

    Set<Map.Entry<String, StoryTask>> getStoryTasks();

    List<Story> getStories();

    StoryTask getStoryTask(Story story);
}
