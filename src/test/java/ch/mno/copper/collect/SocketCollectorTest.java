package ch.mno.copper.collect;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.mno.copper.AbstractJmxServerTestStarter;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import ch.mno.copper.test.WebServer4Tests;


/**
 * Created by dutoitc on 26.03.2016.
 */
public class SocketCollectorTest extends AbstractJmxServerTestStarter {

    final static int HTTP_PORT = 35743;

    private static WebServer4Tests ws;
    private static StoryGrammar storyGrammar;

    @BeforeClass
    public static void setup() {
        storyGrammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));
        // HTTP Server
        ws = new WebServer4Tests(HTTP_PORT);
        ws.start();
    }

    @AfterClass
    public static void done() throws Exception {
        ws.close();
    }

    @Test
    public void testCheckConnectionOnDummyServer() {
        SocketCollectorWrapper collector = SocketCollectorWrapper.buildCollector(storyGrammar, "SOCKET WITH host=localhost,port=1,timeout_ms=1000\nKEEP status AS myStatus\n");
        Assert.assertEquals("myStatus", collector.getAs().get(0));
        Assert.assertEquals("IO_EXCEPTION", collector.execute2D().get(0).get(0));
        Assert.assertEquals("IO_EXCEPTION", collector.execute().get("myStatus"));
    }

    @Test
    public void testCheckConnectionOnRealServerJMX() {
        SocketCollectorWrapper collector = SocketCollectorWrapper.buildCollector(storyGrammar, "SOCKET WITH host=localhost,port=" + JMX_PORT + ",timeout_ms=1000\nKEEP status AS myStatus\n");
        Assert.assertEquals("OK", collector.execute2D().get(0).get(0));
        Assert.assertEquals("OK", collector.execute().get("myStatus"));
    }

    @Test
    public void testCheckConnectionOnRealServerHTTP() {
        try (
                // HTTP Server
                WebServer4Tests ws = new WebServer4Tests(HTTP_PORT);
        ) {
            ws.start();
            SocketCollectorWrapper collector = SocketCollectorWrapper.buildCollector(storyGrammar, "SOCKET WITH host=127.0.0.1,port=" + HTTP_PORT + ",timeout_ms=5000\nKEEP status AS myStatus\n");
            Assert.assertEquals("OK", collector.execute2D().get(0).get(0));
            Assert.assertEquals("OK", collector.execute().get("myStatus"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
