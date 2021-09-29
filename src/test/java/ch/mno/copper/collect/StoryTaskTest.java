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

import static org.junit.jupiter.api.Assertions.*;

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
        StoryTaskBuilder builder = new StoryTaskBuilder(collectorWrapperFactory);

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

        StoryTaskBuilder builder = new StoryTaskBuilder(collectorWrapperFactory);
        StoryTask storyTask = builder.build(story, vs);
        storyTask.getRunnable().run();
        String str = ((MapValuesStore) vs).getValuesAsString();
        assertEquals("[KEY1=ERR][KEY2=ERR]", str);
    }

    @Test
    void testAttributes() throws IOException {
        String storyText = "RUN ON CRON */3 * * * *\n" +
                "GIVEN COLLECTOR WEB WITH url=http://localhost:50400\n" +
                "    KEEP responseCode AS COPPER2_WEB_RETURN_CODE\n" +
                "THEN STORE VALUES";
        Story story = new Story(grammar, "storyName.txt", storyText);
        var task = new StoryTaskImpl(story, ()->{}, "* * * * *");
        assertEquals("storyName.txt", task.storyName());
        assertEquals("storyName", task.getTitle());
        var task2 = new StoryTaskImpl(story, ()->{}, "* * * * *");
        assertTrue(task2.getTaskId()>task.getTaskId());
    }

    @Test
    void lifecycle() throws IOException {
        String storyText = "RUN ON CRON */3 * * * *\n" +
                "GIVEN COLLECTOR WEB WITH url=http://localhost:50400\n" +
                "    KEEP responseCode AS COPPER2_WEB_RETURN_CODE\n" +
                "THEN STORE VALUES";
        Story story = new Story(grammar, "storyName.txt", storyText);
        var task = new StoryTaskImpl(story, ()->{}, "* * * * *");
        long v = System.currentTimeMillis() - 1;
        task.cronData.setNextRun4Test(v); // Don't wait a minute
        assertEquals(v, task.getNextRun());
        assertTrue(task.shouldRun());
        task.markAsRunning();
        assertFalse(task.shouldRun());
        task.markAsRun();
    }

    @Test
    void testNextRun() throws IOException {
        String storyText = "RUN ON CRON */3 * * * *\n" +
                "GIVEN COLLECTOR WEB WITH url=http://localhost:50400\n" +
                "    KEEP responseCode AS COPPER2_WEB_RETURN_CODE\n" +
                "THEN STORE VALUES";
        Story story = new Story(grammar, "storyName.txt", storyText);
        var task = new StoryTaskImpl(story, ()->{}, "* * 31 12 *");
        assertTrue(task.getNextRun()>System.currentTimeMillis());
        assertFalse(task.shouldRun());
        task = new StoryTaskImpl(story, ()->{}, null);
        assertEquals(0, task.getNextRun());
    }


}