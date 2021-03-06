package ch.mno.copper.collect.connectors;

import ch.mno.copper.AbstractWebPortSpringTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

@Ignore // FIXME unstable test on Jenkins (parallelization problem ?)
public class HttpConnectorTest extends AbstractWebPortSpringTest {

    @Test
    @Ignore // FIXME unstable test on Jenkins (parallelization problem ?)
    public void ping1() throws Exception {
        try (HttpConnector conn = new HttpConnector("localhost", port, "http")) {
            String value = conn.get("/ping1");
            Assert.assertEquals("pong1", value);
        }
    }

    @Test
    @Ignore // FIXME unstable test on Jenkins (parallelization problem ?)
    public void test2() throws ConnectorException {
        // Port non ouvert + 10
        try (HttpConnector conn = new HttpConnector("localhost", port + 10, "http")) {
            String res = conn.get("/something");
            Assert.fail("Should raise an exception, but got " + res);
        } catch (ConnectorException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("Connection refused"));
        }
    }


    @Test
    @Ignore // FIXME unstable test on Jenkins (parallelization problem ?)
    public void test3() throws ConnectorException {
        try (HttpConnector conn = new HttpConnector("localhost", port, "http")) {
            Map<String, String> nvs = new HashMap<>();
            nvs.put("key1", "value3");
            nvs.put("key2", "value2");
            String res = conn.post("/repeat", nvs);
            Assert.assertTrue(res.contains("key1=value3&key2=value2"));
        }
    }

    @Test
    @Ignore // FIXME unstable test on Jenkins (parallelization problem ?)
    public void testErr() throws ConnectorException {
        try (HttpConnector conn = new HttpConnector("localhost", port, "http")) {
            String res = conn.get("/err404");
            Assert.assertEquals("Error 404:", res);
        }
    }

    @Test
    @Ignore // FIXME unstable test on Jenkins (parallelization problem ?)
    public void testErrPost() throws ConnectorException {
        try (HttpConnector conn = new HttpConnector("localhost", port, "http")) {
            Map<String, String> nvs = new HashMap<>();
            nvs.put("key1", "value1");
            nvs.put("key2", "value2");
            String res = conn.post("/err404", nvs);
            Assert.assertEquals("Error 404:", res);
        }
    }


}
