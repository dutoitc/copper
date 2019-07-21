package ch.mno.copper;

import ch.mno.copper.collect.StoryTask;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.stories.StoriesFacade;
import ch.mno.copper.stories.data.Story;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A provider with store cache. At each call, it will load newer stories from disk, remove older from cache
 */
public class DataproviderImpl implements DataProvider {

    StoriesFacade storiesFacade;
    private ValuesStore valuesStore;
    private Map<String, StoryTask> cachedStoryTasks = new HashMap<>();

    public DataproviderImpl(StoriesFacade storiesFacade, ValuesStore valuesStore) {
        this.storiesFacade = storiesFacade;
        this.valuesStore = valuesStore;
        refreshStoryTasks();
    }

    @Override
    public Set<Map.Entry<String, StoryTask>> getStoryTasks() {
        return cachedStoryTasks.entrySet();
    }

    @Override
    public List<Story> getStories() {
        List<Story> stories = refreshStoryTasks();
        return stories;
    }

    private List<Story> refreshStoryTasks() {
        List<Story> stories = storiesFacade.getStories(true);
        Map<String, StoryTask> newStoryTasks = new HashMap<>(cachedStoryTasks.size());
        newStoryTasks.putAll(cachedStoryTasks);

        // Add newer
        stories.stream()
                .filter(s -> !cachedStoryTasks.containsKey(s.getName()))
                .filter(s->!s.hasError())
                .forEach(s -> newStoryTasks.put(s.getName(), storiesFacade.buildStoryTask(s, valuesStore)));
        cachedStoryTasks = newStoryTasks;
        return stories;
    }

    @Override
    public StoryTask getStoryTask(Story story) {
        return cachedStoryTasks.get(story.getName());
    }
}