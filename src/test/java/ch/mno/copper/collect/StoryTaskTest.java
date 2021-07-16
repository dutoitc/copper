package ch.mno.copper.collect;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.collect.wrappers.AbstractCollectorWrapper;
import ch.mno.copper.collect.wrappers.CollectorWrapperFactory;
import ch.mno.copper.store.MapValuesStore;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.stories.StoryTaskBuilder;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.PropertyResolver;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by dutoitc on 16.02.2016.
 */
class StoryTaskTest {


    private static StoryGrammar grammar;

    @BeforeAll
    public static void init() {
        grammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));
    }


    @Test
    void testCronMinute() {
        List<String> values = new ArrayList<>();
        StoryTask ct = new StoryTaskImpl(null, () -> values.add("1"), "* * * * *");
        assertTrue(Math.abs(ct.getNextRun() - System.currentTimeMillis()) <= 60000);
    }

    @Test
    void testCronMinute2() {
        List<String> values = new ArrayList<>();
        StoryTask ct = new StoryTaskImpl(null, () -> values.add("1"), "0 * * * *");
        assertTrue(Math.abs(ct.getNextRun() - System.currentTimeMillis()) <= 60 * 60 * 1000);
    }

    @Test
    void testBuildAndRunOk() throws IOException {
        Story story = new Story(grammar, "StoryName", "RUN ON CRON 0 8,13 * * *\nGIVEN STORED VALUES\nTHEN\nSTORE VALUES");

        CollectorWrapperFactory collectorWrapperFactory = new CollectorWrapperFactory(Mockito.mock(PropertyResolver.class), grammar) {
            public AbstractCollectorWrapper build(String storyGiven) {
                return new AbstractCollectorWrapper() {
                    @Override
                    public Map<String, String> execute() {
                        Map<String, String> map = new HashMap<>();
                        map.put("KEY1", "VALUE1");
                        map.put("KEY2", "VALUE2");
                        return map;
                    }

                    @Override
                    public List<List<String>> execute2D() {
                        return null;
                    }
                };
            }
        };
        StoryTaskBuilder builder = new StoryTaskBuilder(collectorWrapperFactory, grammar);

        ValuesStore vs = new MapValuesStore();
        StoryTask storyTask = builder.build(story, vs);
        storyTask.getRunnable().run();
        String str = ((MapValuesStore) vs).getValuesAsString();
        assertEquals("[KEY1=VALUE1][KEY2=VALUE2]", str);
    }


    @Test
    void testBuildAndRunForErrorShouldStoreEntriesAsError() throws IOException, ConnectorException {
        Story story = new Story(grammar, "StoryName", "RUN ON CRON 0 8,13 * * *\nGIVEN STORED VALUES\nTHEN\nSTORE VALUES");

        ValuesStore vs = new MapValuesStore();
        CollectorWrapperFactory collectorWrapperFactory = new CollectorWrapperFactory(Mockito.mock(PropertyResolver.class), grammar) {
            public AbstractCollectorWrapper build(String storyGiven) {
                return new AbstractCollectorWrapper() {
                    @Override
                    public Map<String, String> execute() throws ConnectorException {
                        throw new ConnectorException("Timeout", null);
                    }

                    @Override
                    public List<List<String>> execute2D() {
                        return null;
                    }

                    public List<String> getAs() {
                        return Arrays.asList("KEY1", "KEY2");
                    }
                };
            }
        };

        StoryTaskBuilder builder = new StoryTaskBuilder(collectorWrapperFactory, grammar);
        StoryTask storyTask = builder.build(story, vs);
        storyTask.getRunnable().run();
        String str = ((MapValuesStore) vs).getValuesAsString();
        assertEquals("[KEY1=ERR][KEY2=ERR]", str);
    }

//
//    @Test
//    @SuppressWarnings("java:S2925")
//    void testBuildAndRunForTimeoutShouldStoreEntriesAsError() throws IOException {
//        Story story = new Story(grammar, "StoryName", "RUN ON CRON 0 8,13 * * *\nGIVEN STORED VALUES\nTHEN\nSTORE VALUES");
//
//        ValuesStore vs = new MapValuesStore();
//        CollectorWrapperFactory collectorWrapperFactory = new CollectorWrapperFactory(Mockito.mock(PropertyResolver.class), grammar) {
////            @Override
////            public Map<String, String> execute() throws ConnectorException {
////                try {
////                    Thread.sleep(1000 * StoryTaskBuilder.TIMEOUT_SEC + 1000);
////                } catch (InterruptedException e) {
////                    e.printStackTrace();
////                }
////                throw new ConnectorException("The End", null);
////            }
////
////            @Override
////            public List<List<String>> execute2D()  {
////                return null;
////            }
//
//            public List<String> getAs() {
//                return Arrays.asList("KEY1", "KEY2");
//            }
//        };
//        StoryTaskBuilder builder = new StoryTaskBuilder(collectorWrapperFactory, grammar);
//        StoryTask storyTask = builder.build(story, vs);
//        storyTask.getRunnable().run();
//        String str = ((MapValuesStore) vs).getValuesAsString();
//        assertEquals("[KEY1=ERR][KEY2=ERR]", str);
//    }


}