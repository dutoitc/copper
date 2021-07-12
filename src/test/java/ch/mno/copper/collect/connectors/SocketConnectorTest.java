package ch.mno.copper.collect.connectors;

import ch.mno.copper.AbstractJmxServerTestStarter;
import ch.mno.copper.test.WebServer4Tests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SocketConnectorTest extends AbstractJmxServerTestStarter {

    static int httpPort = 35742;
    private static WebServer4Tests ws;

    @BeforeAll
    public static void setup() throws IOException, InterruptedException {
        // HTTP Server
        ws = new WebServer4Tests(httpPort);
        ws.start();
        httpPort = ws.getPort();
        int nbTries = 30;
        while (!ws.isRunning() && nbTries-- > 0) {
            Thread.sleep(100);
        }
        if (!ws.isRunning()) {
            throw new RuntimeException("Webservice4Tests not running after some time !");
        }
    }

    @AfterAll
    public static void done() throws Exception {
        ws.close();
    }

    @Test
    void testCheckConnectionOnDummyServer() {
        SocketConnector.CONNECTION_CHECK status = new SocketConnector("localhost", 1, 1000).checkConnection();
        assertEquals(SocketConnector.CONNECTION_CHECK.IO_EXCEPTION, status);
    }

    @Test
    void testCheckConnectionOnRealServerJMX() {
        SocketConnector.CONNECTION_CHECK status = new SocketConnector("localhost", JMX_PORT, 1000).checkConnection();
        assertEquals(SocketConnector.CONNECTION_CHECK.OK, status);
    }

    @Test
    void testCheckConnectionOnRealServerHTTP() {
        SocketConnector connector = new SocketConnector("localhost", httpPort, 10000);
        SocketConnector.CONNECTION_CHECK status = connector.checkConnection();
        try {
            assertEquals(SocketConnector.CONNECTION_CHECK.OK, status);
        } catch (AssertionError e) {
            Exception exception = connector.getLastException();
            System.err.println("Dernière exception: " + exception == null ? "null" : exception.getMessage());
            exception.printStackTrace();
            throw e;
        }

    }


}
