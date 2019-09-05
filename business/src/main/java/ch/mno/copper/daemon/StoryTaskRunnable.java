package ch.mno.copper.daemon;

import ch.mno.copper.collect.StoryTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object to execute a story task.
 * Task is "marked as running", then "mark as run" at the end.
 */
public class StoryTaskRunnable implements Runnable {

    private Logger LOG = LoggerFactory.getLogger(CopperDaemon.class);
    private final StoryTask task;

    public StoryTaskRunnable(StoryTask task) {
        this.task = task;
    }

    @Override
    public void run() {
        // Run CopperTask with exception catch, next run computation and time logging.
        long t0 = System.currentTimeMillis();
        task.markAsRunning();

        String taskName = task.getTaskId() + "[" + task.getTitle() + "]";
        try {
            LOG.info("Running task " + task.getTaskId());
            task.getRunnable().run();
        } catch (NullPointerException e) {
            LOG.error("Task {} execution error: {}", taskName, e.getMessage());
            LOG.error("Error NPE ", e.getMessage());
            e.printStackTrace();
            LOG.error("Error NPE ", e.getCause().getMessage());
            for (StackTraceElement s: e.getStackTrace()) {
                LOG.error("  " + s.getClass() + "."+s.getMethodName()+":"+s.getLineNumber());
            }
        } catch (Exception e) {
            LOG.error("Task {} execution error: {}", taskName, e.getMessage());
            LOG.error("Error", e);
        } finally {
            task.markAsRun();
        }
        LOG.info("Task {} ended in {}s.", taskName, (System.currentTimeMillis() - t0) /1000);
    }

}