package ch.mno.copper;

import ch.mno.copper.collect.StoryTask;
import ch.mno.copper.data.ValuesStore;
import ch.mno.copper.process.AbstractProcessor;
import ch.mno.copper.stories.Story;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dutoitc on 02.02.2016.
 */
// Optimisations: sleep until next task run (compute on task addition). Log next task run.
public class CopperDaemon implements Runnable {

    private final DataProvider dataProvider;
    private Logger LOG = LoggerFactory.getLogger(CopperDaemon.class);

    public static final int N_THREADS = 10;
    public static int TASK_CHEK_INTERVAL = 1000 * 3; // don't overload processors !
    private final List<AbstractProcessor> processors = new ArrayList<>();
    private final ValuesStore valuesStore;
    private boolean shouldRun = true;
    private List<String> storiesToRun = new ArrayList<>();
    private LocalDateTime lastQueryTime = LocalDateTime.MIN;

    /**
     * Manual run by the web
     */

    private ExecutorService executorService;
    private JMXConnectorServer jmxConnectorServer;

    private CopperDaemon(DataProvider dataProvider) {
        executorService = Executors.newFixedThreadPool(N_THREADS);
        this.valuesStore = CopperMediator.getInstance().getValuesStore();
        this.dataProvider = dataProvider;
    }

    public static CopperDaemon runWith(DataProvider dataProvider) {
        CopperDaemon daemon = new CopperDaemon(dataProvider);
        CopperMediator.getInstance().registerCopperDaemon(daemon);
        Thread thread = new Thread(daemon);
        thread.start();
        return daemon;
    }


    private void runIteration() {
        // Refresh stories from disk
        List<Story> stories = dataProvider.getStories(); // With refresh


        for (Story story : stories) {
            // Run story ?
            StoryTask task = dataProvider.getStoryTask(story);
            if (!storiesToRun.contains(story.getName()) && (task == null || !task.shouldRun())) continue;
            storiesToRun.remove(story.getName());

            executorService.submit(() -> {
                // Run CopperTask with exception catch, next run computation and time logging.
                long t0 = System.currentTimeMillis();
                String taskName = task.getTaskId() + "[" + task.getTitle() + "]";
                task.markAsRunning();
                try {
                    LOG.info("Running task " + task.getTaskId());
                    task.getRunnable().run();
                } catch (NullPointerException e) {
                    LOG.error("Task {} execution error: {}", taskName, e.getMessage());
                    LOG.error("Error NPE ", e.getMessage());
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
                LOG.info("Task {} ended in {}s.", taskName, (System.currentTimeMillis() - t0) / 60);
            });
        }

        // Processors
        LocalDateTime queryTime = LocalDateTime.now(); // Keep time, so that next run will have data between query time assignation and valueStore read time
        Collection<String> changedValues = valuesStore.queryValues(lastQueryTime, LocalDateTime.MAX);
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

        // Start JMX
        startJMX();

        while (shouldRun) {
            LOG.trace("Daemon run");
            runIteration();

            // Save
            try {
                valuesStore.save();
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


    private void startJMX() {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            java.rmi.registry.LocateRegistry.createRegistry(30409);
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:30409/server");
            jmxConnectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, server);
            jmxConnectorServer.start();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        shouldRun = false;
        try {
            jmxConnectorServer.stop();
        } catch (IOException e) {
        }
    }

    public void runStory(String storyName) {
        synchronized (storiesToRun) {
            storiesToRun.add(storyName);
        }
    }
}
