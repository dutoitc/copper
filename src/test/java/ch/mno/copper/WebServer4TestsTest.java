package ch.mno.copper;

import ch.mno.copper.store.MapValuesStore;
import ch.mno.copper.store.ValuesStore;
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
    private static ValuesStore valueStore;

    @BeforeClass
    public static void init() throws InterruptedException {
        srv = new WebServer(46789);
        thread = new Thread(srv);
        thread.start();
        Thread.sleep(5000); // Wait for server start
        valueStore = new MapValuesStore();
        valueStore.put("aKey", "aValue");
        new CopperMediator(valueStore, null, null, null, null);
    }

    @AfterClass
    public static void done() throws Exception {
        if (srv!=null) {
            srv.stop();
        }
    }

    @Test
    // FIXME: erreur 500
    public void testValues() throws URISyntaxException, IOException {
        String url="http://localhost:" + srv.getPort() + "/ws/values";
        String content = IOUtils.toString(new URI(url), "UTF-8");
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
        String url="http://localhost:" + srv.getPort() + "/dashboard";
        System.out.println("Trying " + url);
        String content = IOUtils.toString(new URI(url));
        System.out.println(content);
        Assert.assertTrue(content.contains("<title>Copper UI</title>"));
    }
}
