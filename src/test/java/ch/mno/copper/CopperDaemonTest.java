package ch.mno.copper;

import ch.mno.copper.collect.StoryTask;
import ch.mno.copper.process.AbstractProcessor;
import ch.mno.copper.store.ValuesStore;
import org.junit.jupiter.api.Disabled;

import java.util.Collection;
import java.util.List;

/**
 * Created by dutoitc on 17.02.2016.
 */
@Disabled("to be reworked")
public class CopperDaemonTest {

//    @Test
//    public void testAll() throws InterruptedException {
//        SimpleStoryTask coll1 = new SimpleStoryTask();
//        SimpleProcessor proc1 = new SimpleProcessor(Arrays.asList("key1"));
//        SimpleProcessor proc2 = new SimpleProcessor(Arrays.asList("key2"));
//        CopperDaemon.TASK_CHEK_INTERVAL=100;
//
//        ValuesStore valueStore = ValuesStore.getInstance();
//        List<StoryTask> lstCollectors = Arrays.asList(coll1);
//        List<AbstractProcessor> lstProcessors = Arrays.asList(proc1, proc2);
//        DataProvider dataProvider = new DataProvider() {
//            @Override
//            public Set<Map.Entry<String, StoryTask>> getStoryTasks() {
//                ()->lstCollectors;
//            }
//
//            @Override
//            public List<Story> getStories() {
//                return null;
//            }
//
//            @Override
//            public StoryTask getStoryTask(Story story) {
//                return null;
//            }
//        }
//        CopperDaemon daemon = CopperDaemon.runWith(dataProvider);
//        int nb=0;
//        while (coll1.nbRuns==0 && nb++<20) {
//            Thread.sleep(CopperDaemon.TASK_CHEK_INTERVAL / 4);
//        }
//        Assert.assertEquals("Check1", 1, coll1.nbRuns);
//        Assert.assertEquals("Check2", 1, coll1.nbMark);
//        nb=0;
//        while (coll1.nbRuns==1 && nb++<20) {
//            Thread.sleep(CopperDaemon.TASK_CHEK_INTERVAL / 4);
//        }
//        Assert.assertEquals("Check3", 2, coll1.nbRuns);
//        Assert.assertEquals("Check4", 2, coll1.nbMark);
//
//        // Test processors
//        Assert.assertEquals("Check5", 0, proc1.nbTrig);
//        Assert.assertEquals("Check6", 0, proc2.nbTrig);
//        valueStore.put("key1", "value1");
//        nb=0;
//        while (proc1.nbTrig==0 && nb++<400) {
////            System.out.println(nb+" sleeping " + CopperDaemon.TASK_CHEK_INTERVAL / 4);
//            Thread.sleep(CopperDaemon.TASK_CHEK_INTERVAL / 4);
//        }
//        Assert.assertEquals("Check7-nb=" + nb, 1, proc1.nbTrig);
//        Assert.assertEquals("Check8", 0, proc2.nbTrig);
//        valueStore.put("key2", "value2");
//        nb=0;
//        while (proc2.nbTrig==0 && nb++<20) {
//            Thread.sleep(CopperDaemon.TASK_CHEK_INTERVAL / 4);
//        }
//        Assert.assertEquals("Check9", 1, proc1.nbTrig);
//        Assert.assertEquals("Check10", 1, proc2.nbTrig);
//
//        daemon.stop();
//        Thread.sleep(CopperDaemon.TASK_CHEK_INTERVAL+100);
//    }

    private static class SimpleStoryTask implements StoryTask {
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
