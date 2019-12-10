package ch.mno.copper.collect.connectors;

import ch.mno.copper.AbstractJmxServerTestStarter;
import ch.mno.copper.test.WebServer4Tests;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class SocketConnectorTest extends AbstractJmxServerTestStarter {

    final static int HTTP_PORT = 35742;
    private static WebServer4Tests ws;

    @BeforeClass
    public static void setup() throws IOException {
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
