package ch.mno.copper;

import ch.mno.copper.collect.StoryTask;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.stories.StoriesFacade;
import ch.mno.copper.stories.data.Story;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stories and values provider with cache.
 * At each call, it will load newer stories from disk, remove older from cache
 */
@Component
public class DataProviderImpl implements DataProvider {

    private final StoriesFacade storiesFacade;
    private final ValuesStore valuesStore;
    private Map<String, StoryTask> cachedStoryTasks = new HashMap<>();

    public DataProviderImpl(StoriesFacade storiesFacade, ValuesStore valuesStore) {
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
        return refreshStoryTasks();
    }

    private List<Story> refreshStoryTasks() {
        List<Story> stories = storiesFacade.getStories(true);
        Map<String, StoryTask> newStoryTasks = new HashMap<>(cachedStoryTasks.size());
        newStoryTasks.putAll(cachedStoryTasks);

        // Add newer
        stories.stream()
                .filter(s -> !cachedStoryTasks.containsKey(s.getName()))
                .filter(s -> !s.hasError())
                .forEach(s -> newStoryTasks.put(s.getName(), storiesFacade.buildStoryTask(s, valuesStore)));
        cachedStoryTasks = newStoryTasks;
        return stories;
    }

    @Override
    public StoryTask getStoryTask(Story story) {
        return cachedStoryTasks.get(story.getName());
    }

    @Override
    public ValuesStore getValuesStore() {
        return valuesStore;
    }
}