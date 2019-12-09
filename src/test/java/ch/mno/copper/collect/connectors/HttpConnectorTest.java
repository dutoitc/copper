package ch.mno.copper.collect.connectors;

import ch.mno.copper.AbstractWebPortSpringTest;
import ch.mno.copper.CopperApplication;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

public class HttpConnectorTest extends AbstractWebPortSpringTest {

    @Test
    public void ping1() throws Exception {
        try (HttpConnector conn = new HttpConnector("localhost", port, "http")) {
            String value = conn.get("/ping1");
            Assert.assertEquals("pong1", value);
        }
    }

    @Test
    public void test2() throws ConnectorException {
        try (HttpConnector conn = new HttpConnector("localhost", port + 10, "http")) {
            String res = conn.get("/something");
            Assert.fail("Should raise an exception, but got " + res);
        } catch (ConnectorException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("Connection refused"));
        }
    }


    @Test
    public void test3() throws ConnectorException {
        try (HttpConnector conn = new HttpConnector("localhost", port, "http")) {
            Map<String, String> nvs = new HashMap<>();
            nvs.put("key1", "value1");
            nvs.put("key2", "value2");
            String res = conn.post("/repeat", nvs);
            Assert.assertTrue(res.startsWith("POST"));
        }
    }

    @Test
    public void testErr() throws ConnectorException {
        try (HttpConnector conn = new HttpConnector("localhost", port, "http")) {
            String res = conn.get("/err404");
            Assert.assertEquals("Error 404:Not Found", res);
        }
    }

    @Test
    public void testErrPost() throws ConnectorException {
        try (HttpConnector conn = new HttpConnector("localhost", port, "http")) {
            Map<String, String> nvs = new HashMap<>();
            nvs.put("key1", "value1");
            nvs.put("key2", "value2");
            String res = conn.post("/err404", nvs);
            Assert.assertEquals("Error 404:Not Found", res);
        }
    }


}
