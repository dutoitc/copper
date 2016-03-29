package ch.mno.copper;

import ch.mno.copper.web.WebServer;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by dutoitc on 20.02.2016.
 */
public class WebServer4TestsTest {

    private static WebServer srv;
    private static Thread thread;

    @BeforeClass
    public static void init() throws InterruptedException {
        srv = new WebServer();
        thread = new Thread(srv);
        thread.start();
        Thread.sleep(2000);
        ValuesStore.getInstance().put("aKey", "aValue");
    }

    @AfterClass
    public static void done() throws Exception {
        srv.stop();
    }

    @Test
    public void testValues() throws URISyntaxException, IOException {
        String url="http://localhost:30400/ws/values";
        String content = IOUtils.toString(new URI(url));
        Assert.assertTrue(content.contains("aKey"));
    }

//    @Test
//    public void testValue() throws URISyntaxException, IOException {
//        String url="http://localhost:30400/ws/value/aKey";
//        String content = IOUtils.toString(new URI(url));
//        Assert.assertTrue(content.contains("aKey"));
//    }


//    @Test
//    public void testStories() throws URISyntaxException, IOException {
//        String url="http://localhost:30400/ws/stories";
//        String content = IOUtils.toString(new URI(url));
//        Assert.assertEquals("", content);
//    }



    @Test
    public void testHome() throws URISyntaxException, IOException {
        String url="http://localhost:30400/";
        String content = IOUtils.toString(new URI(url));
        Assert.assertTrue(content.contains("<title>Copper</title>"));
    }
}
