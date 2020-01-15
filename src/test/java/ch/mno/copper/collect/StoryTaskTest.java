package ch.mno.copper.collect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.store.MapValuesStore;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.stories.StoryTaskBuilder;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;

/**
 * Created by dutoitc on 16.02.2016.
 */
public class StoryTaskTest {


    private static StoryGrammar grammar;

    @BeforeClass
    public static void init() {
        grammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));
    }


    @Test
    public void testCronMinute() {
        List<String> values = new ArrayList<>();
        StoryTask ct = new StoryTaskImpl(null, ()->values.add("1"), "* * * * *");
        Assert.assertTrue(Math.abs(ct.getNextRun()-System.currentTimeMillis())<=60000);
    }

    @Test
    public void testCronMinute2() {
        List<String> values = new ArrayList<>();
        StoryTask ct = new StoryTaskImpl(null, ()->values.add("1"), "0 * * * *");
        Assert.assertTrue(Math.abs(ct.getNextRun()-System.currentTimeMillis())<=60*60*1000);
    }

    @Test
    public void testBuildAndRunOk() throws IOException, ConnectorException {
        Story story = new Story(grammar, "StoryName", "RUN ON CRON 0 8,13 * * *\nGIVEN STORED VALUES\nTHEN\nSTORE VALUES");
        story.setCollectorWrapper4Tests(new AbstractCollectorWrapper() {
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
        });

        ValuesStore vs = new MapValuesStore();
        StoryTask storyTask = StoryTaskBuilder.build(story, vs);
        storyTask.getRunnable().run();
        String str = ((MapValuesStore) vs).getValuesAsString();
        Assert.assertEquals("[KEY1=VALUE1][KEY2=VALUE2]", str);
    }


    @Test
    public void testBuildAndRunForErrorShouldStoreEntriesAsError() throws IOException, ConnectorException {
        Story story = new Story(grammar, "StoryName", "RUN ON CRON 0 8,13 * * *\nGIVEN STORED VALUES\nTHEN\nSTORE VALUES");
        story.setCollectorWrapper4Tests(new AbstractCollectorWrapper() {
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
        });

        ValuesStore vs = new MapValuesStore();
        StoryTask storyTask = StoryTaskBuilder.build(story, vs);
        storyTask.getRunnable().run();
        String str = ((MapValuesStore) vs).getValuesAsString();
        Assert.assertEquals("[KEY1=ERR][KEY2=ERR]", str);
    }


    @Test
    public void testBuildAndRunForTimeoutShouldStoreEntriesAsError() throws IOException, ConnectorException {
        Story story = new Story(grammar, "StoryName", "RUN ON CRON 0 8,13 * * *\nGIVEN STORED VALUES\nTHEN\nSTORE VALUES");
        story.setCollectorWrapper4Tests(new AbstractCollectorWrapper() {
            @Override
            public Map<String, String> execute() throws ConnectorException {
                try {
                    Thread.sleep(1000*StoryTaskBuilder.TIMEOUT_SEC + 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                throw new ConnectorException("The End", null);
            }

            @Override
            public List<List<String>> execute2D() {
                return null;
            }

            public List<String> getAs() {
                return Arrays.asList("KEY1", "KEY2");
            }
        });

        ValuesStore vs = new MapValuesStore();
        StoryTask storyTask = StoryTaskBuilder.build(story, vs);
        storyTask.getRunnable().run();
        String str = ((MapValuesStore) vs).getValuesAsString();
        Assert.assertEquals("[KEY1=ERR][KEY2=ERR]", str);
    }


}