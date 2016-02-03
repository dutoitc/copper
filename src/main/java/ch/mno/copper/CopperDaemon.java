package ch.mno.copper;

import ch.mno.copper.collect.CollectorTask;
import ch.mno.copper.process.AbstractProcessor;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dutoitc on 02.02.2016.
 */
// Optimisations: sleep until next task run (compute on task addition). Log next task run.
public class CopperDaemon implements Runnable {

    public static final int N_THREADS = 10;
    public static final int TASK_CHEK_INTERVAL = 1000 * 3; // don't limit processors !
    private final List<AbstractProcessor> processors;
    private final ValuesStore valuesStore;
    private boolean shouldRun = true;
    private List<CollectorTask> collectorTasks;

    private ExecutorService executorService;

    public CopperDaemon(ValuesStore valuesStore, List<CollectorTask> collectorTasks, List<AbstractProcessor> processors) {
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
        System.out.println("Copper daemon has started.");
        while (shouldRun) {
            // Collectors
//            System.out.println("Daemon run");
            collectorTasks.stream().filter(t->t.shouldRun()).forEach(task-> {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        // Run CopperTask with exception catch, next run computation and time logging.
                        long t0 = System.currentTimeMillis();
                        try {
                            System.out.println("Scheduling task " + task.getTaskId());
                            task.getRunnable().run();
                        } catch (Exception e) {
                            System.err.println("Task " + task.getTaskId() + " execution error: " + e.getMessage());
                        }
                        task.markAsRun();
                        System.out.println("Task " + task.getTaskId() + " ended in " + (System.currentTimeMillis()-t0)/60 + "s.");
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

            // Wait for some time
//            System.out.println("Daemon sleep");
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


    /*
    Scheduler s = new Scheduler();
		// Schedule a once-a-minute task.
		s.schedule("* * * * *", new Runnable() {
			public void run() {
				System.out.println("Another minute ticked away...");
			}
		});
		// Starts the scheduler.
		s.start();

		String pattern = "0 3 * jan-jun,sep-dec mon-fri";
Predictor p = new Predictor(pattern);
for (int i = 0; i < n; i++) {
	System.out.println(p.nextMatchingDate());
}
     */

}
