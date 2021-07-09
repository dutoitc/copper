package ch.mno.copper.collect;

import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by xsicdt on 29/02/16.
 */
public class JmxCollectorWrapperTest {

    private StoryGrammar storyGrammar;

    @BeforeEach
    public void init() {
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
        assertEquals(2, wrapper.jmxQueries.size());
        assertEquals(2, wrapper.as.size());
        assertEquals("java.lang:type=Runtime", wrapper.jmxQueries.get(0).objectName);
        assertEquals("java.lang:type=Runtime", wrapper.jmxQueries.get(1).objectName);
        assertEquals("SpecName", wrapper.jmxQueries.get(0).value);
        assertEquals("SpecVersion", wrapper.jmxQueries.get(1).value);
        assertEquals("JMX_LOCAL_RUNTIME_SPECNAME", wrapper.as.get(0));
        assertEquals("JMX_LOCAL_RUNTIME_SPECVERSION", wrapper.as.get(1));
        assertEquals("[JMX_LOCAL_RUNTIME_SPECNAME, JMX_LOCAL_RUNTIME_SPECVERSION]", StringUtils.join(wrapper.getAs()));
    }

}