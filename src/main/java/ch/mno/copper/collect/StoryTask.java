package ch.mno.copper.collect;

/**
 * Created by dutoitc on 17.02.2016.
 */
public interface StoryTask {

    String storyName();

    String getTitle();

    long getTaskId();

    Runnable getRunnable();

    boolean shouldRun();

    long getNextRun();

    void markAsRun();

    void markAsRunning();

}
