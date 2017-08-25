package ch.mno.copper.collect;

import ch.mno.copper.stories.Story;
import ch.mno.copper.stories.StoryGrammar;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;

/**
 * Created by xsicdt on 29/02/16.
 */
public class JmxCollectorWrapperTest {

    private StoryGrammar storyGrammar;

    @Before
    public void init() throws FileNotFoundException {
        storyGrammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));
    }

    @Test
    public void test() {
            String jmx = "GIVEN COLLECTOR JMX WITH url=service:jmx:rmi://src2737v.myhost:44444/jndi/rmi://src2737v.myhost:1099/karaf-trun,user=tadmin,password=tadmin\n" +
                    "    QUERY java.lang:type=Runtime FOR SpecName    AS JMX_LOCAL_RUNTIME_SPECNAME\n" +
                    "    QUERY java.lang:type=Runtime FOR SpecVersion AS JMX_LOCAL_RUNTIME_SPECVERSION\n" +
                    "WHEN CRON */5 7-18 * * 1-5\n" +
                    "THEN STORE VALUES\n";
        JmxCollectorWrapper wrapper = JmxCollectorWrapper.buildCollector(storyGrammar, jmx);
        Assert.assertEquals(2, wrapper.jmxQueries.size());
        Assert.assertEquals(2, wrapper.as.size());
        Assert.assertEquals("java.lang:type=Runtime", wrapper.jmxQueries.get(0).objectName);
        Assert.assertEquals("java.lang:type=Runtime", wrapper.jmxQueries.get(1).objectName);
        Assert.assertEquals("SpecName", wrapper.jmxQueries.get(0).value);
        Assert.assertEquals("SpecVersion", wrapper.jmxQueries.get(1).value);
        Assert.assertEquals("JMX_LOCAL_RUNTIME_SPECNAME", wrapper.as.get(0));
        Assert.assertEquals("JMX_LOCAL_RUNTIME_SPECVERSION", wrapper.as.get(1));
    }
}
