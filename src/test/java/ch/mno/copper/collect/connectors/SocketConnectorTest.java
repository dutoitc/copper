package ch.mno.copper.collect.connectors;

import ch.mno.copper.AbstractJmxServerTestStarter;
import ch.mno.copper.test.WebServer4Tezts;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SocketConnectorTest extends AbstractJmxServerTestStarter {

    static int httpPort = 35742;
    private static WebServer4Tezts ws;

    @BeforeAll
    public static void setup() throws InterruptedException {
        // HTTP Server
        ws = new WebServer4Tezts(httpPort);
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
        try (SocketConnector conn = new SocketConnector("localhost", 1, 1000);) {
            SocketConnector.CONNECTION_CHECK status = conn.checkConnection();
            assertEquals(SocketConnector.CONNECTION_CHECK.IO_EXCEPTION, status);
        }
    }

    @Test
    void testCheckConnectionOnRealServerJMX() {
        try (
                var conn = new SocketConnector("localhost", getJmxPort(), 1000);
        ) {
            SocketConnector.CONNECTION_CHECK status = conn.checkConnection();
            assertEquals(SocketConnector.CONNECTION_CHECK.OK, status);
        }
    }

    @Test
    void testCheckConnectionOnRealServerHTTP() {
        try (SocketConnector connector = new SocketConnector("localhost", httpPort, 10000)) {
            SocketConnector.CONNECTION_CHECK status = connector.checkConnection();
            assertEquals(SocketConnector.CONNECTION_CHECK.OK, status);
        }
    }

}