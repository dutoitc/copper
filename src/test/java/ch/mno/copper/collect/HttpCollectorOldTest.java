package ch.mno.copper.collect;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.test.WebServer4Tests;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

/**
 * Created by dutoitc on 26.03.2016.
 */
public class HttpCollectorOldTest {


    final static int PORT = 35743;
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
        List<String> values = HttpCollectorOld.httpQuery("http://localhost:" + PORT, "?ping1", "?ping2");
        Assert.assertEquals("pong1", values.get(0));
        Assert.assertEquals("pong2", values.get(1));
    }

    @Test(expected = ConnectorException.class)
    public void testWrongUrl() throws ConnectorException {
        HttpCollectorOld.httpQuery("httpd://dummy", "");
    }


    @Test(expected = ConnectorException.class)
    public void testWrongUrl2() throws ConnectorException {
        HttpCollectorOld.httpQuery("http://257.1.1.1", "");
    }

    @Test
    public void testWrongErr404() throws Exception {
        List<String> res = HttpCollectorOld.httpQuery("http://localhost:" + PORT, "err404");
        Assert.assertEquals("Error 404:Not Found", res.get(0));
    }


}
