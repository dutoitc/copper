package ch.mno.copper;

import ch.mno.copper.collect.CollectorTask;
import ch.mno.copper.process.AbstractProcessor;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by dutoitc on 17.02.2016.
 */
public class CopperDaemonTest {

    @Test
    public void testAll() throws InterruptedException {
        SimpleCollectorTask coll1 = new SimpleCollectorTask();
        SimpleProcessor proc1 = new SimpleProcessor(Arrays.asList("key1"));
        SimpleProcessor proc2 = new SimpleProcessor(Arrays.asList("key2"));
        CopperDaemon.TASK_CHEK_INTERVAL=100;

        ValuesStore valueStore = ValuesStore.getInstance();
        List<CollectorTask> lstCollectors = Arrays.asList(coll1);
        List<AbstractProcessor> lstProcessors = Arrays.asList(proc1, proc2);
        DataProvider dataProvider = ()->lstCollectors;
        CopperDaemon daemon = CopperDaemon.runWith(valueStore, dataProvider, lstProcessors);
        int nb=0;
        while (coll1.nbRuns==0 && nb++<20) {
            Thread.sleep(CopperDaemon.TASK_CHEK_INTERVAL / 4);
        }
        Assert.assertEquals(1, coll1.nbRuns);
        Assert.assertEquals(1, coll1.nbMark);
        nb=0;
        while (coll1.nbRuns==1 && nb++<20) {
            Thread.sleep(CopperDaemon.TASK_CHEK_INTERVAL / 4);
        }
        Assert.assertEquals(2, coll1.nbRuns);
        Assert.assertEquals(2, coll1.nbMark);

        // Test processors
        Assert.assertEquals(0, proc1.nbTrig);
        Assert.assertEquals(0, proc2.nbTrig);
        valueStore.put("key1", "value1");
        nb=0;
        while (proc1.nbTrig==0 && nb++<20) {
            Thread.sleep(CopperDaemon.TASK_CHEK_INTERVAL / 4);
        }
        Assert.assertEquals(1, proc1.nbTrig);
        Assert.assertEquals(0, proc2.nbTrig);
        valueStore.put("key2", "value2");
        nb=0;
        while (proc2.nbTrig==0 && nb++<20) {
            Thread.sleep(CopperDaemon.TASK_CHEK_INTERVAL / 4);
        }
        Assert.assertEquals(1, proc1.nbTrig);
        Assert.assertEquals(1, proc2.nbTrig);

        daemon.stop();
        Thread.sleep(CopperDaemon.TASK_CHEK_INTERVAL+100);
    }

    private static class SimpleCollectorTask implements CollectorTask {
        int nbRuns=0;
        int nbMark = 0;

        @Override
        public String storyName() {
            return "aName";
        }

        @Override
        public String getTitle() {
            return "aTitle";
        }

        @Override
        public long getTaskId() {
            return 42;
        }

        @Override
        public Runnable getRunnable() {
            return new Runnable() {
                @Override
                public void run() {
                    nbRuns++;
                }
            };
        }

        @Override
        public boolean shouldRun() {
            return true;
        }

        @Override
        public long getNextRun() {
            return 0;
        }

        @Override
        public void markAsRun() {
            nbMark++;
        }

        @Override
        public void markAsRunning() {

        }
    }

    private static class SimpleProcessor extends AbstractProcessor {

        int nbTrig=0;

        public SimpleProcessor(List<String> valuesTrigger) {
            super(valuesTrigger);
        }

        @Override
        public void trig(ValuesStore valueStore, Collection<String> changedValueKeys) {
            nbTrig++;
        }
    }

}
