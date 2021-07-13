package ch.mno.copper.stories;

import config.CopperScreensProperties;
import config.CopperStoriesProperties;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DiskHelperTest {

    @Test
    void testFindJSON() throws IOException {
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

    @Test
    void testMany() throws IOException {
        String storiesFolder = "DiskHelperTestsScreens";
        String storyName = "story-diskhelpertest-1";
        File fileStory = new File(storiesFolder + '/' + storyName);
        //
        CopperStoriesProperties storiesProperties = new CopperStoriesProperties();
        storiesProperties.setFolder(storiesFolder);
        CopperScreensProperties screensProperties = new CopperScreensProperties();
        screensProperties.setFolder(storiesFolder);
        DiskHelper dh = new DiskHelper(storiesProperties, screensProperties);

        //
        if (fileStory.exists()) {
            fileStory.delete();
        }
        dh.saveNewStory(storyName, "Lorem Ipsum");
        assertTrue(fileStory.exists());
        assertEquals("Lorem Ipsum", Files.readString(fileStory.toPath()));

        //
        assertEquals("Lorem Ipsum", dh.findScreenData(storyName));

        //rewrite must fail
        try {
            dh.saveNewStory(storyName, "Lorem Ipsum");
            fail();
        } catch (FileAlreadyExistsException e) {
            assertEquals("Error: the file story-diskhelpertest-1 already exists.", e.getMessage());
        }

        // updateStory
        dh.updateStory(storyName, "ipsodec");
        assertEquals("ipsodec", Files.readString(fileStory.toPath()));

        // delete story
        dh.deleteStory(storyName);
        assertFalse(fileStory.exists());

        // updateStory on deleted must fail
        try {
            dh.updateStory(storyName, "deladec");
            fail();
        } catch (NoSuchFileException e) {
            assertEquals("Error: the file story-diskhelpertest-1 must already exists.", e.getMessage());
        }

    }

}
