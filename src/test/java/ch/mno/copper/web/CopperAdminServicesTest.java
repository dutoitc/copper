package ch.mno.copper.web;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.daemon.CopperDaemon;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.stories.StoriesFacade;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import ch.mno.copper.web.dto.StoryPostDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CopperAdminServicesTest {

    static StoryGrammar grammar;

    @BeforeAll
    static void init() throws FileNotFoundException {
        grammar = new StoryGrammar(new FileInputStream("src/main/resources/StoryGrammar.txt"));
    }


    @Test
    void postStory_new() throws IOException, ConnectorException {
        StoriesFacade storiesFacade = Mockito.mock(StoriesFacade.class);
        Mockito.when(storiesFacade.saveNewStory("newStory", "storyText")).thenReturn("OK");
        CopperAdminServices service = new CopperAdminServices(null, storiesFacade, null);
        //
        StoryPostDTO post = StoryPostDTO.of("new", "newStory", "storyText");
        String ret = service.postStory("dummy", post).getBody();
        //
        assertEquals("OK", ret);
    }

    @Test
    void postStory_New_Exception() throws IOException, ConnectorException {
        StoriesFacade storiesFacade = Mockito.mock(StoriesFacade.class);
        Mockito.when(storiesFacade.saveNewStory("newStory", "storyText")).thenThrow(new IOException("Exception for test"));
        CopperAdminServices service = new CopperAdminServices(null, storiesFacade, null);
        //
        StoryPostDTO post = StoryPostDTO.of("new", "dummy", "storyText");
        assertThrows(ResponseStatusException.class, () -> service.postStory("dummy", post));
    }

    @Test
    void postStory_update() throws IOException, ConnectorException {
        String oldStoryText = "RUN ON CRON */5 * * * *\n" +
                "GIVEN COLLECTOR WEB WITH url=http://localhost:30400\n" +
                "    KEEP responseCode AS COPPER_WEB_RETURN_CODE\n" +
                "THEN STORE VALUES";
        String newStoryText = "RUN ON CRON */3 * * * *\n" +
                "GIVEN COLLECTOR WEB WITH url=http://localhost:50400\n" +
                "    KEEP responseCode AS COPPER2_WEB_RETURN_CODE\n" +
                "THEN STORE VALUES";

        StoriesFacade storiesFacade = Mockito.mock(StoriesFacade.class);
        Mockito.when(storiesFacade.updateStory("oldStory", "newStory", newStoryText)).thenReturn("OK");
        Story story = new Story(grammar, "oldStory", oldStoryText);
        Mockito.when(storiesFacade.getStoryByName("oldStory")).thenReturn(story);
        CopperAdminServices service = new CopperAdminServices(null, storiesFacade, null);
        //
        StoryPostDTO post = StoryPostDTO.of("oldStory", "newStory", newStoryText);
        String ret = service.postStory("oldStory", post).getBody();
        //
        assertEquals("OK", ret);
    }

    @Test
    void postStory_update_exception() throws IOException, ConnectorException {
        String oldStoryText = "RUN ON CRON */5 * * * *\n" +
                "GIVEN COLLECTOR WEB WITH url=http://localhost:30400\n" +
                "    KEEP responseCode AS COPPER_WEB_RETURN_CODE\n" +
                "THEN STORE VALUES";
        String newStoryText = "RUN ON ... (wrong syntax)";

        StoriesFacade storiesFacade = Mockito.mock(StoriesFacade.class);
        Mockito.when(storiesFacade.updateStory("oldStory", "newStory", newStoryText)).thenThrow(new IOException("Exception for test"));
        Story story = new Story(grammar, "oldStory", oldStoryText);
        Mockito.when(storiesFacade.getStoryByName("oldStory")).thenReturn(story);
        CopperAdminServices service = new CopperAdminServices(null, storiesFacade, null);
        //
        StoryPostDTO post = StoryPostDTO.of("oldStory", "newStory", newStoryText);
        assertThrows(ResponseStatusException.class, () -> service.postStory("oldStory", post));
    }

    @Test
    void getStoryRun() {
        CopperDaemon daemon = Mockito.mock(CopperDaemon.class);
        CopperAdminServices service = new CopperAdminServices(null, null, daemon);
        //
        String ret = service.getStoryRun("storyName");
        Mockito.verify(daemon).runStory("storyName");
        assertEquals("Story storyName marked for execution", ret);
    }

    @Test
    void getStoryDelete() {
        StoriesFacade storiesFacade = Mockito.mock(StoriesFacade.class);
        CopperAdminServices service = new CopperAdminServices(null, storiesFacade, null);
        //
        String ret = service.getStoryDelete("storyName");
        Mockito.verify(storiesFacade).deleteStory("storyName");
        assertEquals("Story storyName deleted.", ret);
    }

    @Test
    void deleteValuesOlderThanOneMonth() {
        ValuesStore vs = Mockito.mock(ValuesStore.class);
        CopperAdminServices service = new CopperAdminServices(vs, null, null);
        service.deleteValuesOlderThanOneMonth();
        Mockito.verify(vs).deleteValuesOlderThanXDays(30);
    }

    @Test
    void deleteValuesOlderThanThreeMonth() {
        ValuesStore vs = Mockito.mock(ValuesStore.class);
        CopperAdminServices service = new CopperAdminServices(vs, null, null);
        service.deleteValuesOlderThanThreeMonth();
        Mockito.verify(vs).deleteValuesOlderThanXDays(90);
    }

    @Test
    void deleteValuesOfKey() {
        ValuesStore vs = Mockito.mock(ValuesStore.class);
        CopperAdminServices service = new CopperAdminServices(vs, null, null);
        service.deleteValuesOfKey("key");
        Mockito.verify(vs).deleteValuesOfKey("key");
    }

    @Test
    void getStories() throws IOException, ConnectorException {
        String storyText1 = "RUN ON CRON */5 * * * *\n" +
                "GIVEN COLLECTOR WEB WITH url=http://localhost:30400\n" +
                "    KEEP responseCode AS COPPER_WEB_RETURN_CODE\n" +
                "THEN STORE VALUES";
        String storyText2 = "RUN ON CRON */3 * * * *\n" +
                "GIVEN COLLECTOR WEB WITH url=http://localhost:50400\n" +
                "    KEEP responseCode AS COPPER2_WEB_RETURN_CODE\n" +
                "THEN STORE VALUES";
        Story story1 = new Story(grammar, "story1", storyText1);
        Story story2 = new Story(grammar, "story2", storyText2);
        //
        StoriesFacade storiesFacade = Mockito.mock(StoriesFacade.class);
        Mockito.when(storiesFacade.getStories(true)).thenReturn(Arrays.asList(story1, story2));
        CopperAdminServices service = new CopperAdminServices(null, storiesFacade, null);
        //
        String ret = service.getStories();
        assertEquals("[{\"name\":\"story1\",\"cron\":\"*/5 * * * *\",\"storyText\":\"RUN ON CRON */5 * * * *\\nGIVEN COLLECTOR WEB WITH url\\u003dhttp://localhost:30400\\n    KEEP responseCode AS COPPER_WEB_RETURN_CODE\\nTHEN STORE VALUES\\n\",\"hasError\":false,\"nextRun\":XXX},{\"name\":\"story2\",\"cron\":\"*/3 * * * *\",\"storyText\":\"RUN ON CRON */3 * * * *\\nGIVEN COLLECTOR WEB WITH url\\u003dhttp://localhost:50400\\n    KEEP responseCode AS COPPER2_WEB_RETURN_CODE\\nTHEN STORE VALUES\\n\",\"hasError\":false,\"nextRun\":XXX}]", comparableJSON(ret));
    }


    @Test
    void getStory() throws IOException, ConnectorException {
        String storyText1 = "RUN ON CRON */5 * * * *\n" +
                "GIVEN COLLECTOR WEB WITH url=http://localhost:30400\n" +
                "    KEEP responseCode AS COPPER_WEB_RETURN_CODE\n" +
                "THEN STORE VALUES";
        Story story1 = new Story(grammar, "story1", storyText1);
        StoriesFacade storiesFacade = Mockito.mock(StoriesFacade.class);
        Mockito.when(storiesFacade.getStoryByName("story1")).thenReturn(story1);
        CopperAdminServices service = new CopperAdminServices(null, storiesFacade, null);
        //
        String ret = service.getStory("story1");
        assertEquals("{\"name\":\"story1\",\"cron\":\"*/5 * * * *\",\"storyText\":\"RUN ON CRON */5 * * * *\\nGIVEN COLLECTOR WEB WITH url\\u003dhttp://localhost:30400\\n    KEEP responseCode AS COPPER_WEB_RETURN_CODE\\nTHEN STORE VALUES\\n\",\"hasError\":false,\"nextRun\":XXX}", comparableJSON(ret));
    }

    private String comparableJSON(String ret) {
        return ret.replaceAll("[0-9]{6}[0-9]+", "XXX");
    }


    @Test
    void getStory_notfound() {
        StoriesFacade storiesFacade = Mockito.mock(StoriesFacade.class);
        Mockito.when(storiesFacade.getStoryByName("story1")).thenReturn(null);
        CopperAdminServices service = new CopperAdminServices(null, storiesFacade, null);
        //
        assertThrows(ResponseStatusException.class, () -> service.getStory("story1"));
    }

    @Test
    void getOverview() throws IOException, ConnectorException {
        String storyText1 = "RUN ON CRON */5 * * * *\n" +
                "GIVEN COLLECTOR WEB WITH url=http://localhost:30400\n" +
                "    KEEP responseCode AS COPPER_WEB_RETURN_CODE\n" +
                "THEN STORE VALUES";
        String storyText2 = "RUN ON CRON */3 * * * *\n" +
                "GIVEN COLLECTOR WEB WITH url=http://localhost:50400\n" +
                "    KEEP responseCode AS COPPER2_WEB_RETURN_CODE\n" +
                "THEN STORE VALUES";
        Story story1 = new Story(grammar, "story1", storyText1);
        Story story2 = new Story(grammar, "story2", storyText2);
        //
        StoriesFacade storiesFacade = Mockito.mock(StoriesFacade.class);
        Mockito.when(storiesFacade.getStories(true)).thenReturn(Arrays.asList(story1, story2));
        CopperAdminServices service = new CopperAdminServices(null, storiesFacade, null);
        String ret = comparableJSON(service.getOverview());
        assertEquals("{\"overviewStories\":[{\"storyId\":\"story1\",\"nextRun\":XXX},{\"storyId\":\"story2\",\"nextRun\":XXX}]}", ret);
    }

    @Test
    void postValue() {
        ValuesStore vs = Mockito.mock(ValuesStore.class);
        CopperAdminServices service = new CopperAdminServices(vs, null, null);
        service.postValue("key", "value");
        Mockito.verify(vs).put("key", "value");
    }

}
