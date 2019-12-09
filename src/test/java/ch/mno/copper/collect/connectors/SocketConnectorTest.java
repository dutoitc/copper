package ch.mno.copper.collect.connectors;

import ch.mno.copper.test.WebServer4Tests;
import org.junit.*;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;

public class SocketConnectorTest {

    public static final int JMX_PORT = 39055;
    private static JMXConnectorServer connectorServer;
    final static int HTTP_PORT = 35742;
    private static WebServer4Tests ws;

    @BeforeClass
    public static void setup() throws IOException {
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
    public void testCheckConnectionOnDummyServer() {
        SocketConnector.CONNECTION_CHECK status = new SocketConnector("localhost", 1, 1000).checkConnection();
        Assert.assertEquals(SocketConnector.CONNECTION_CHECK.IO_EXCEPTION, status);
    }

    @Test
    public void testCheckConnectionOnRealServerJMX() {
        SocketConnector.CONNECTION_CHECK status = new SocketConnector("localhost", JMX_PORT, 1000).checkConnection();
        Assert.assertEquals(SocketConnector.CONNECTION_CHECK.OK, status);
    }

    @Test
    public void testCheckConnectionOnRealServerHTTP() {
        SocketConnector.CONNECTION_CHECK status = new SocketConnector("localhost", HTTP_PORT, 1000).checkConnection();
        Assert.assertEquals(SocketConnector.CONNECTION_CHECK.OK, status);
    }


}
