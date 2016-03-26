package ch.mno.copper.collect.connectors;

import ch.mno.copper.test.WebServer4Tests;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dutoitc on 31.01.2016.
 */
public class HttpConnectorTest {

    final static int PORT = 35742;
    private static WebServer4Tests ws;

    @BeforeClass
    public static void init() {
        ws = new WebServer4Tests(PORT);
        ws.start();
    }

    @AfterClass
    public static void done() throws Exception {
        ws.close();
    }

    @Test
    public void test1() throws Exception {
        try (
                HttpConnector conn = new HttpConnector("localhost", PORT, "http");
        ) {
            String value = conn.get("/ping1");
            Assert.assertEquals("pong1", value);
        }
    }

    @Test
    public void test2() throws ConnectorException {
        try (HttpConnector conn = new HttpConnector("localhost", PORT + 1, "http")) {
            conn.get("/something");
            Assert.fail("Should raise an exception");
        } catch (ConnectorException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("Connection refused"));
        }
    }


    @Test
    public void test3() throws ConnectorException {
        try (HttpConnector conn = new HttpConnector("localhost", PORT, "http")) {
            Map<String, String> nvs = new HashMap<>();
            nvs.put("key1", "value1");
            nvs.put("key2", "value2");
            String res = conn.post("/repeat", nvs);
            Assert.assertTrue(res.startsWith("POST"));
        }
    }


    @Test
    public void testErr() throws ConnectorException {
        try (HttpConnector conn = new HttpConnector("localhost", PORT, "http")) {
            String res = conn.get("/err404");
            Assert.assertEquals("Error 404:Not Found", res);
        }
    }


    @Test
    public void testErrPost() throws ConnectorException {
        try (HttpConnector conn = new HttpConnector("localhost", PORT, "http")) {
            Map<String, String> nvs = new HashMap<>();
            nvs.put("key1", "value1");
            nvs.put("key2", "value2");
            String res = conn.post("/err404", nvs);
            Assert.assertEquals("Error 404:Not Found", res);
        }
    }




}
