package ch.mno.copper.stories.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StoriesHolder {

    private static final Logger LOG = LoggerFactory.getLogger(StoriesHolder.class);

    private List<Story> stories = new ArrayList<>();
    private Set<String> storiesInError = new HashSet<>();

    public List<Story> getStories() {
        return stories;
    }

    public Story add(Story story) {
        stories.add(story);
        return story;
    }

    public void replace(Story originalStory, Story story) {
        stories.remove(originalStory);
        stories.add(story);
    }

    public List<Story> findStoriesByName(String name) {
        String[] spl = name.split("/");
        String smallName = spl.length == 1 ? name : spl[spl.length - 1];
        return stories.stream().filter(s -> s.getName().equals(smallName)).collect(Collectors.toList());
    }

    public void remove(Story story) {
        stories.remove(story);
    }

    public void removeIfError(String filename) {
        if (storiesInError.contains(filename)) {
            storiesInError.remove(filename);
            LOG.info("No longer in error: " + filename);
        }
    }

    public void markAsError(String filename) {
        if (!storiesInError.contains(filename)) {
            storiesInError.add(filename);
            LOG.warn("Wrong syntax for " + filename+", ignoring until error fixed on disk."); // FIXME: put story as dirty
        }
    }

    public Story findStoryByName(String name) {
        List<Story> storyList = findStoriesByName(name);
        // Check number
        if (storyList.isEmpty()) {
            return null;
        } else if (storyList.size() > 1) {
            throw new RuntimeException("Error: story name are not unique for " + name);
        }
        return storyList.get(0);
    }
}