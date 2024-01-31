package ch.mno.copper.stories;

import ch.mno.copper.CopperException;
import config.CopperScreensProperties;
import config.CopperStoriesProperties;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class DiskHelperTest {

    @Test
    void testFindJSON() {
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
        var dh = DiskHelper.builder()
                .withScreensFolder(storiesFolder)
                .withStoriesFolder(storiesFolder)
                .build();

        //
        if (fileStory.exists()) {
            assertTrue(fileStory.delete());
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
        } catch (IllegalArgumentException e) {
            assertEquals("Error: the file story-diskhelpertest-1 must already exists.", e.getMessage());
        }
    }

    @Test
    void endsWith() {
        var dh = DiskHelper.builder().build();
        assertTrue(dh.endsWith(new File("truc.txt"), "txt"));
        assertTrue(dh.endsWith(new File("truc.TxT"), "txt"));
        assertFalse(dh.endsWith(new File("truc.xml"), "txt"));
    }

    @Test
    void findStoryNames() {
        String storiesFolder = Objects.requireNonNull(getClass().getResource("/DiskHelperTestsScreens")).getFile();
        CopperStoriesProperties storiesProperties = new CopperStoriesProperties();
        storiesProperties.setFolder(storiesFolder);
        CopperScreensProperties screensProperties = new CopperScreensProperties();
        screensProperties.setFolder(storiesFolder);
        DiskHelper dh = new DiskHelper(storiesProperties, screensProperties);

        var names = dh.findStoryNames();
        assertEquals("[myScreen1.json, myScreen2.json, myScreen3.txt]", names.toString());
    }

    @Test
    void testGetStoryAsStream() throws IOException {
        String storiesFolder = Objects.requireNonNull(getClass().getResource("/DiskHelperTestsScreens")).getFile() + "/..";
        var dh = DiskHelper.builder()
                .withScreensFolder(storiesFolder)
                .withStoriesFolder(storiesFolder)
                .build();

        var is = dh.getStoryAsStream("JmxStory1.txt");
        var story = IOUtils.toString(is, StandardCharsets.UTF_8);
        assertEquals("GIVEN\n" +
                "    COLLECTOR JMX\n" +
                "        WITH url=service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi,\n" +
                "             user=aUser,\n" +
                "             password=aPass\n" +
                "        QUERY java.lang:type=Runtime FOR SpecName       AS JMX_LOCAL_RUNTIME_SPECNAME\n" +
                "        QUERY java.lang:type=Runtime FOR SpecVersion    AS JMX_LOCAL_RUNTIME_SPECVERSION\n" +
                "WHEN\n" +
                "    CRON DAILY at 0605\n" +
                "THEN\n" +
                "    STORE VALUES\n", story);
    }


    @Test
    void testSecurePath() {
        DiskHelper dh = DiskHelper.builder().build();
        assertEquals("storyName", dh.securePath("storyName"));
        assertThrows(IllegalArgumentException.class, ()->dh.securePath("/etc/passwd"));
    }

    @Test
    void testX() {
        DiskHelper dh = DiskHelper.builder().build();
        assertThrows(StoryException.class, ()->dh.getSecureFile(".", "../tmp"));
    }


}