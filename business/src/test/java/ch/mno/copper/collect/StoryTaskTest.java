package ch.mno.copper.collect;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dutoitc on 16.02.2016.
 */
public class StoryTaskTest {

    @Test
    public void testCronMinute() {
        List<String> values = new ArrayList<String>();
        StoryTask ct = new StoryTaskImpl(null, ()->values.add("1"), "* * * * *");
        Assert.assertTrue(Math.abs(ct.getNextRun()-System.currentTimeMillis())<=60000);
    }

    @Test
    public void testCronMinute2() {
        List<String> values = new ArrayList<String>();
        StoryTask ct = new StoryTaskImpl(null, ()->values.add("1"), "0 * * * *");
        Assert.assertTrue(Math.abs(ct.getNextRun()-System.currentTimeMillis())<=60*60*1000);
    }

}
