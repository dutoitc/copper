package ch.mno.copper.collect.connectors;

import ch.mno.copper.AbstractWebPortSpringTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpConnectorTest extends AbstractWebPortSpringTest {

    @Test
    void ping1() throws Exception {
        try (HttpConnector conn = new HttpConnector("localhost", port, "http")) {
            String value = conn.get("/ping1");
            assertEquals("pong1", value);
        }
    }

    @Test
    void test2() {
        // Port non ouvert + 10
        try (HttpConnector conn = new HttpConnector("localhost", port + 10, "http")) {
            String res = conn.get("/something");
            fail("Should raise an exception, but got " + res);
        } catch (ConnectorException e) {
            assertTrue(e.getMessage().contains("Connexion refusée") || e.getMessage().contains("Connection refused"), e.getMessage());
        }
    }


    @Test
    void test3() throws ConnectorException {
        try (HttpConnector conn = new HttpConnector("localhost", port, "http")) {
            Map<String, String> nvs = new HashMap<>();
            nvs.put("key1", "value3");
            nvs.put("key2", "value2");
            String res = conn.post("/repeat", nvs);
            assertTrue(res.contains("key1=value3&key2=value2"));
        }
    }

    @Test
    void testErr() throws ConnectorException {
        try (HttpConnector conn = new HttpConnector("localhost", port, "http")) {
            String res = conn.get("/err404");
            assertEquals("Error 404:", res);
        }
    }

    @Test
    void testErrPost() throws ConnectorException {
        try (HttpConnector conn = new HttpConnector("localhost", port, "http")) {
            Map<String, String> nvs = new HashMap<>();
            nvs.put("key1", "value1");
            nvs.put("key2", "value2");
            String res = conn.post("/err404", nvs);
            assertEquals("Error 404:", res);
        }
    }


}
