package ch.mno.copper.collect;

/**
 * Created by dutoitc on 17.02.2016.
 */
public interface CollectorTask {
    String getTitle();

    long getTaskId();

    Runnable getRunnable();

    boolean shouldRun();

    long getNextRun();

    void markAsRun();
}
