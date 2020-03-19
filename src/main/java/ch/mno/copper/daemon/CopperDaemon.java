package ch.mno.copper.daemon;

import ch.mno.copper.DataProvider;
import ch.mno.copper.collect.StoryTask;
import ch.mno.copper.process.AbstractProcessor;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.stories.data.Story;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dutoitc on 02.02.2016.
 */
// Optimisations: sleep until next task run (compute on task addition). Log next task run.
public class CopperDaemon implements Runnable, ApplicationListener<ContextRefreshedEvent>, AutoCloseable {

    private final DataProvider dataProvider;
    private static final Logger LOG = LoggerFactory.getLogger(CopperDaemon.class);

    public static final int N_THREADS = 10;
    public static final int TASK_CHEK_INTERVAL = 1000 * 3; // don't overload processors !
    private final List<AbstractProcessor> processors = new ArrayList<>();
    //    private final ValuesStore valuesStore;
    private boolean shouldRun = true;
    private List<String> storiesToRun = new ArrayList<>();
    private LocalDateTime lastQueryTime = LocalDateTime.MIN;

    /**
     * Manual run by the web
     */

    private ExecutorService executorService;
    private Thread threadDaemon;

    public CopperDaemon(DataProvider dataProvider) {
        executorService = Executors.newFixedThreadPool(N_THREADS);
        this.dataProvider = dataProvider;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        threadDaemon = new Thread(this);
        threadDaemon.start();
    }

    @Override
    public void close() throws Exception {
        shouldRun = false;
        threadDaemon.interrupt();
    }

    private void runIteration() {
        // Refresh stories from disk
        List<Story> stories = dataProvider.getStories(); // With refresh

        //
        ValuesStore valuesStore = dataProvider.getValuesStore();

        for (Story story : stories) {
            // Run story ?
            StoryTask task = dataProvider.getStoryTask(story);
            if (storiesToRun.contains(story.getName()) || (task != null && task.shouldRun())) {
                storiesToRun.remove(story.getName());
                executorService.submit(new StoryTaskRunnable(task));
            }
        }

        // Processors
        LocalDateTime queryTime = LocalDateTime.now(); // Keep time, so that next run will have store between query time assignation and valueStore readInstant time
        Collection<String> changedValues = valuesStore.queryValues(lastQueryTime.toInstant(ZoneOffset.UTC), Instant.MAX);
        lastQueryTime = queryTime;
        processors.forEach(p -> {
            Collection<String> keys = p.findKnownKeys(changedValues);
            if (!keys.isEmpty()) {
                p.trig(valuesStore, keys);
            }
        });
    }


    @Override
    public void run() {
        LOG.info("Copper daemon has started.");

        while (shouldRun) {
            LOG.trace("Daemon run");
            runIteration();

            // Save
            try {
                dataProvider.getValuesStore().save();
            } catch (IOException e) {
                throw new RuntimeException("Cannot save to valuesStore");
            }

            // Wait for some time
            LOG.trace("Daemon sleep");
            try {
                Thread.sleep(TASK_CHEK_INTERVAL);
            } catch (InterruptedException e) {
                // OK
            }
        }
        executorService.shutdown();
    }

    public void runStory(String storyName) {
        synchronized (storiesToRun) {
            storiesToRun.add(storyName);
        }
    }
}
