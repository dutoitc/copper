package ch.mno.copper.collect.connectors;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.mno.copper.AbstractJmxServerTestStarter;
import ch.mno.copper.test.WebServer4Tests;

public class SocketConnectorTest extends AbstractJmxServerTestStarter {

    final static int HTTP_PORT = 35742;
    private static WebServer4Tests ws;

    @BeforeClass
    public static void setup() throws InterruptedException {
        // HTTP Server
        ws = new WebServer4Tests(HTTP_PORT);
        ws.start();
        int nbTries=30;
        while (!ws.isRunning() && nbTries-->0) {
            Thread.sleep(100);
        }
        if (!ws.isRunning()) {
            throw new RuntimeException("Webservice4Tests not running after some time !");
        }
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
        SocketConnector connector = new SocketConnector("localhost", HTTP_PORT, 10000);
        SocketConnector.CONNECTION_CHECK status = connector.checkConnection();
        try {
            Assert.assertEquals(SocketConnector.CONNECTION_CHECK.OK, status);
        }
        catch (AssertionError e) {
            Exception exception = connector.getLastException();
            System.err.println("Dernière exception: " + exception==null?"null":exception.getMessage());
            exception.printStackTrace();
            throw e;
        }

    }


}
