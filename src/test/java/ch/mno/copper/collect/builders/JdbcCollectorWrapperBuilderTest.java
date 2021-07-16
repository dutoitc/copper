package ch.mno.copper.collect.builders;

import ch.mno.copper.collect.wrappers.JdbcCollectorWrapper;
import ch.mno.copper.helpers.SyntaxException;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.PropertyResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by xsicdt on 29/02/16.
 */
class JdbcCollectorWrapperBuilderTest {

    private StoryGrammar storyGrammar;

    @BeforeEach
    void init() {
        storyGrammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));
    }

    @Test
    void test() {
        String story = "RUN ON CRON * * * * *\n" +
                "GIVEN COLLECTOR JDBC\n" +
                "    WITH url=jdbc://something,user=${user},password=${pass}\n" +
                "QUERY \"select something from somewhere as myVar3\"\n" +
                "WHEN CRON 0 6 * * 1-5\n" +
                "THEN STORE VALUES\n";
        PropertyResolver propertyResolver = Mockito.mock(PropertyResolver.class);
        Mockito.when(propertyResolver.getProperty("user")).thenReturn("username");
        Mockito.when(propertyResolver.getProperty("pass")).thenReturn("password");
        JdbcCollectorWrapper wrapper = new JdbcCollectorWrapperBuilder(storyGrammar, propertyResolver).buildCollector(story);
        //
        assertEquals("jdbc://something", wrapper.getUrl());
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