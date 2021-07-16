package ch.mno.copper.collect.builders;

import ch.mno.copper.collect.collectors.JmxCollector;
import ch.mno.copper.collect.wrappers.JmxCollectorWrapper;
import ch.mno.copper.helpers.SyntaxException;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.PropertyResolver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by xsicdt on 29/02/16.
 */
class JmxCollectorWrapperBuilderTest {

    private StoryGrammar storyGrammar;

    @BeforeEach
    void init() {
        storyGrammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));
    }

    @Test
    void test() {
        String jmx = "GIVEN COLLECTOR JMX WITH url=service:jmx:rmi://src2737v.myhost:44444/jndi/rmi://src2737v.myhost:1099/karaf-trun,user=${user},password=${pass}\n" +
                "    QUERY java.lang:type=Runtime FOR SpecName    AS JMX_LOCAL_RUNTIME_SPECNAME\n" +
                "    QUERY java.lang:type=Runtime FOR SpecVersion AS JMX_LOCAL_RUNTIME_SPECVERSION\n" +
                "WHEN CRON */5 7-18 * * 1-5\n" +
                "THEN STORE VALUES\n";
        PropertyResolver propertyResolver = Mockito.mock(PropertyResolver.class);
        Mockito.when(propertyResolver.getProperty("user")).thenReturn("username");
        Mockito.when(propertyResolver.getProperty("pass")).thenReturn("password");
        JmxCollectorWrapper wrapper = new JmxCollectorWrapperBuilder(storyGrammar, propertyResolver).buildCollector(jmx);
        //
        List<JmxCollector.JmxQuery> jmxQueries = wrapper.getJmxQueries();
        List<String> as = wrapper.getAs();
        assertEquals(2, jmxQueries.size());
        assertEquals(2, as.size());
        assertEquals("java.lang:type=Runtime", jmxQueries.get(0).getObjectName());
        assertEquals("java.lang:type=Runtime", jmxQueries.get(1).getObjectName());
        assertEquals("SpecName", jmxQueries.get(0).getValue());
        assertEquals("SpecVersion", jmxQueries.get(1).getValue());
        assertEquals("JMX_LOCAL_RUNTIME_SPECNAME", as.get(0));
        assertEquals("JMX_LOCAL_RUNTIME_SPECVERSION", as.get(1));
        assertEquals("[JMX_LOCAL_RUNTIME_SPECNAME, JMX_LOCAL_RUNTIME_SPECVERSION]", StringUtils.join(as));
        assertEquals("service:jmx:rmi://src2737v.myhost:44444/jndi/rmi://src2737v.myhost:1099/karaf-trun", wrapper.getUrl());
        assertEquals("username", wrapper.getUsername());
        assertEquals("password", wrapper.getPassword());
    }


    @Test
    void testWrongSyntax() {
        String jmx = "GIVEN COLLECTOR JMX WITH nothing\n" +
                "    QUERY java.lang:type=Runtime FOR SpecName    AS JMX_LOCAL_RUNTIME_SPECNAME\n" +
                "    QUERY java.lang:type=Runtime FOR SpecVersion AS JMX_LOCAL_RUNTIME_SPECVERSION\n" +
                "WHEN CRON */5 7-18 * * 1-5\n" +
                "THEN STORE VALUES\n";
        PropertyResolver propertyResolver = Mockito.mock(PropertyResolver.class);
        assertThrows(SyntaxException.class, ()->new JmxCollectorWrapperBuilder(storyGrammar, propertyResolver).buildCollector(jmx));
    }
}