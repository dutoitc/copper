package ch.mno.copper.collect.builders;

import ch.mno.copper.AbstractJmxServerTestStarter;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.collect.wrappers.SocketCollectorWrapper;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import ch.mno.copper.test.WebServer4Tests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.PropertyResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by dutoitc on 26.03.2016.
 */
class SocketCollectorWrapperBuilderTest extends AbstractJmxServerTestStarter {

    static int httpPort = 0;

    private static WebServer4Tests ws;
    private static StoryGrammar grammar;

    @BeforeAll
    public static void setup() {
        grammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));
        // HTTP Server
        ws = new WebServer4Tests(httpPort);
        ws.start();
        httpPort = ws.getPort();
    }

    @AfterAll
    public static void done() throws Exception {
        ws.close();
    }

    @Test
    void testCheckConnectionOnDummyServer() throws ConnectorException {
        String story="GIVEN SOCKET WITH host=localhost,port=1,timeout_ms=1000\n" +
                "    KEEP status AS myStatus\n"  +
                "WHEN CRON 0 6 * * 1-5\n" +
                "THEN STORE VALUES\n";
        SocketCollectorWrapper wrapper = buildWrapper(story);
        //
        assertEquals("myStatus", wrapper.getAs().get(0));
        assertEquals("IO_EXCEPTION", wrapper.execute2D().get(0).get(0));
        assertEquals("IO_EXCEPTION", wrapper.execute().get("myStatus"));
    }

    private SocketCollectorWrapper buildWrapper(String story) {
        PropertyResolver propertyResolver = Mockito.mock(PropertyResolver.class);
        Mockito.when(propertyResolver.getProperty("user")).thenReturn("username");
        Mockito.when(propertyResolver.getProperty("pass")).thenReturn("password");
        SocketCollectorWrapperBuilder socketCollectorWrapperBuilder = new SocketCollectorWrapperBuilder(grammar, propertyResolver);
        return socketCollectorWrapperBuilder.buildCollector(story);
    }

    @Test
    void testCheckConnectionOnRealServerJMX() throws ConnectorException {
        String story="GIVEN SOCKET WITH host=localhost,port="+JMX_PORT+",timeout_ms=1000\n" +
                "    KEEP status AS myStatus\n"  +
                "WHEN CRON 0 6 * * 1-5\n" +
                "THEN STORE VALUES\n";
        SocketCollectorWrapper wrapper = buildWrapper(story);
        assertEquals("OK", wrapper.execute2D().get(0).get(0));
        assertEquals("OK", wrapper.execute().get("myStatus"));
    }

    @Test
    void testCheckConnectionOnRealServerHTTP() throws ConnectorException {
        String story="GIVEN SOCKET WITH host=127.0.0.1,port="+JMX_PORT+",timeout_ms=5000\n" +
                "    KEEP status AS myStatus\n"  +
                "WHEN CRON 0 6 * * 1-5\n" +
                "THEN STORE VALUES\n";
        SocketCollectorWrapper wrapper = buildWrapper(story);
        assertEquals("OK", wrapper.execute2D().get(0).get(0));
        assertEquals("OK", wrapper.execute().get("myStatus"));
    }


}
