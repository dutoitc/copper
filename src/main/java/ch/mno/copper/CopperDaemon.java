package ch.mno.copper;

import ch.mno.copper.collect.CollectorTask;
import ch.mno.copper.process.AbstractProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dutoitc on 02.02.2016.
 */
// Optimisations: sleep until next task run (compute on task addition). Log next task run.
public class CopperDaemon implements Runnable {

    private Logger LOG = LoggerFactory.getLogger(CopperDaemon.class);

    public static final int N_THREADS = 10;
    public static int TASK_CHEK_INTERVAL = 1000 * 3; // don't overload processors !
    private final List<AbstractProcessor> processors;
    private final ValuesStore valuesStore;
    private boolean shouldRun = true;
    private List<CollectorTask> collectorTasks;

    private ExecutorService executorService;

    private CopperDaemon(ValuesStore valuesStore, List<CollectorTask> collectorTasks, List<AbstractProcessor> processors) {
        executorService = Executors.newFixedThreadPool(N_THREADS);
        this.collectorTasks = collectorTasks;
        this.processors = processors;
        this.valuesStore = valuesStore;
    }

    public static CopperDaemon runWith(ValuesStore valuesStore, List<CollectorTask> collectorTasks, List<AbstractProcessor> processors) {
        CopperDaemon daemon = new CopperDaemon(valuesStore, collectorTasks, processors);
        Thread thread = new Thread(daemon);
        thread.start();
        return daemon;
    }

    @Override
    public void run() {
        LOG.info("Copper daemon has started.");
        while (shouldRun) {
            // Collectors
            LOG.trace("Daemon run");
            collectorTasks.stream().filter(t->t.shouldRun()).forEach(task-> {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        // Run CopperTask with exception catch, next run computation and time logging.
                        long t0 = System.currentTimeMillis();
                        String taskName = task.getTaskId() + "[" + task.getTitle() + "]";
                        try {
                            LOG.info("Scheduling task " + task.getTaskId());
                            task.getRunnable().run();
                        } catch (Exception e) {
                            LOG.error("Task {} execution error: {}", taskName, e.getMessage());
                            LOG.error("Error", e);
                        }
                        task.markAsRun();
                        LOG.info("Task {} ended in {}s.", taskName, (System.currentTimeMillis()-t0)/60);
                    }
                };
                executorService.submit(runnable);
            });

            // Processors
            Collection<String> changedValues = valuesStore.getChangedValues();
            processors.forEach(p->{
                Collection<String> keys = p.findKnownKeys(changedValues);
                if (!keys.isEmpty()) {
                    p.trig(valuesStore, keys);
                }
            });

            // Save
            try {
                valuesStore.save(new FileOutputStream("valuesStore.tmp"));
            } catch (IOException e) {
                throw new RuntimeException("Cannot save to valuesStore.tmp");
            }

            // Wait for some time
            LOG.trace("Daemon sleep");
            try {
                Thread.sleep(TASK_CHEK_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
    }

    public void stop() {
        shouldRun = false;
    }

}
