package ch.mno.copper.collect;

import ch.mno.copper.AbstractJmxServerTestStarter;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import ch.mno.copper.test.WebServer4Tests;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by dutoitc on 26.03.2016.
 */
public class SocketCollectorTest extends AbstractJmxServerTestStarter {

    final static int HTTP_PORT = 35743;

    private WebServer4Tests ws;
    private StoryGrammar storyGrammar;

    @Before
    public void setup() throws IOException {
        storyGrammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));

        // HTTP Server
        ws = new WebServer4Tests(HTTP_PORT);
        ws.start();
    }

    @After
    public void done() throws Exception {
        ws.close();
    }

    @Test
    public void testCheckConnectionOnDummyServer() throws ConnectorException {
        SocketCollectorWrapper collector = SocketCollectorWrapper.buildCollector(storyGrammar, "SOCKET WITH host=localhost,port=1,timeout_ms=1000\nKEEP status AS myStatus\n");
        Assert.assertEquals("myStatus", collector.getAs().get(0));
        Assert.assertEquals("IO_EXCEPTION", collector.execute2D().get(0).get(0));
        Assert.assertEquals("IO_EXCEPTION", collector.execute().get("myStatus"));
    }

    @Test
    public void testCheckConnectionOnRealServerJMX() throws ConnectorException {
        SocketCollectorWrapper collector = SocketCollectorWrapper.buildCollector(storyGrammar, "SOCKET WITH host=localhost,port=" + JMX_PORT + ",timeout_ms=1000\nKEEP status AS myStatus\n");
        Assert.assertEquals("OK", collector.execute2D().get(0).get(0));
        Assert.assertEquals("OK", collector.execute().get("myStatus"));
    }

    @Test
    public void testCheckConnectionOnRealServerHTTP() throws ConnectorException {
        SocketCollectorWrapper collector = SocketCollectorWrapper.buildCollector(storyGrammar, "SOCKET WITH host=localhost,port=" + HTTP_PORT + ",timeout_ms=1000\nKEEP status AS myStatus\n");
        Assert.assertEquals("OK", collector.execute2D().get(0).get(0));
        Assert.assertEquals("OK", collector.execute().get("myStatus"));
    }


}
