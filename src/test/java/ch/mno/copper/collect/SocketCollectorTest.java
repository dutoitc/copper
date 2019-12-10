package ch.mno.copper.collect;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.collect.connectors.SocketConnector;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import ch.mno.copper.test.WebServer4Tests;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

/**
 * Created by dutoitc on 26.03.2016.
 */
public class SocketCollectorTest {


    public static final int JMX_PORT = 39056;
    private static JMXConnectorServer connectorServer;
    final static int HTTP_PORT = 35743;
    private static WebServer4Tests ws;
    private static StoryGrammar storyGrammar;


    @BeforeClass
    public static void setup() throws IOException {
        storyGrammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));

        // JMX Server
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        java.rmi.registry.LocateRegistry.createRegistry(JMX_PORT);
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:"+JMX_PORT+"/server");
        connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, server);
        connectorServer.start();

        // HTTP Server
        ws = new WebServer4Tests(HTTP_PORT);
        ws.start();
    }

    @AfterClass
    public static void done() throws Exception {
        connectorServer.stop();
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
        SocketCollectorWrapper collector = SocketCollectorWrapper.buildCollector(storyGrammar, "SOCKET WITH host=localhost,port=" + HTTP_PORT + ",timeout_ms=5000\nKEEP status AS myStatus\n");
        Assert.assertEquals("OK", collector.execute2D().get(0).get(0));
        Assert.assertEquals("OK", collector.execute().get("myStatus"));
    }


}
