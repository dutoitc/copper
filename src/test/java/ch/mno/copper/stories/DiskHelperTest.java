package ch.mno.copper.stories;

import config.CopperScreensProperties;
import config.CopperStoriesProperties;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DiskHelperTest {

    @Test
    public void testFindJSON() {
        CopperScreensProperties screensProperties = new CopperScreensProperties();
        screensProperties.setFolder("src/test/resources/DiskHelperTestsScreens");
        DiskHelper dh = new DiskHelper(new CopperStoriesProperties(), screensProperties);

        // Run
        Map<String, String> screens = dh.findScreens();
        assertEquals(2, screens.size());

        // Check names
        Iterator<String> iterator = screens.keySet().iterator();
        assertEquals("myScreen1", iterator.next());
        assertEquals("myScreen2", iterator.next());

        // Check content
        assertEquals("Body of screen 1", screens.get("myScreen1"));
        assertEquals("Body of screen 2", screens.get("myScreen2"));
    }

}
