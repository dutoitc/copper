package ch.mno.copper.web;

import ch.mno.copper.store.MapValuesStore;
import ch.mno.copper.stories.DiskHelper;
import config.CopperScreensProperties;
import config.CopperStoriesProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CopperUserServicesTest {

    private MapValuesStore vs;
    private CopperUserServices service;

    @BeforeEach
    void init() {
        vs = new MapValuesStore();
        service = new CopperUserServices(vs);

        String storiesFolder = Objects.requireNonNull(getClass().getResource("/DiskHelperTestsScreens")).getFile() ;
        String screensFolder = Objects.requireNonNull(getClass().getResource("/DiskHelperTestsScreens")).getFile();
        CopperStoriesProperties storiesProperties = new CopperStoriesProperties();
        storiesProperties.setFolder(storiesFolder);
        CopperScreensProperties screensProperties = new CopperScreensProperties();
        screensProperties.setFolder(screensFolder);
        service.setDiskHelper(new DiskHelper(storiesProperties, screensProperties));
    }

    @Test
    void testPing() {
        assertEquals("pong", service.test());
    }

    @Test
    void testRoot() {
        assertEquals("redirect:swagger-ui.html", service.root());
    }



    @Test
    void getInfoHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("header1", "value1");
        request.addHeader("header2", "value2");
        assertEquals("HTTP HEADERS:\n" +
                "header1=value1\n" +
                "header2=value2\n", service.getInfoHeaders(request));
    }

    @Test
    void testGetScreenJSON() {
        var ret = service.getScreenJson("myScreen1.json");
        assertEquals("200 OK", ret.getStatusCode().toString());
        assertEquals("Body of screen 1", ret.getBody());
    }

    @Test
    void testGetScreenJSON404() {
        var ret = service.getScreenJson("ToyStory.txt");
        assertEquals("404 NOT_FOUND", ret.getStatusCode().toString());
    }

    @Test
    void getScreens() {
        var ret = service.getScreens();
        assertEquals("{myScreen1=Body of screen 1, myScreen2=Body of screen 2}", ret.toString());
    }


}
