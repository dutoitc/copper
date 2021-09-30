package ch.mno.copper.web.adapter;

import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import ch.mno.copper.web.adapters.JsonStoryAdapter;
import ch.mno.copper.web.dto.StoryWEBDTO;
import com.google.gson.stream.JsonWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonStoryAdapterTest {

    private static StoryGrammar grammar;

    @BeforeAll
    public static void init() {
        grammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));
    }

    @Test
    void testAll() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JsonStoryAdapter adapter = new JsonStoryAdapter();
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(os));
        Story story = new Story(grammar, "storyName", "RUN ON CRON */5 * * * *\n" +
                "GIVEN COLLECTOR WEB WITH url=http://localhost:30400\n" +
                "    KEEP responseCode AS COPPER_WEB_RETURN_CODE\n" +
                "THEN STORE VALUES");
        story.setCronData4Test("1 2 3 4 5");
        StoryWEBDTO dto = new StoryWEBDTO(story);

        // Run
        adapter.write(writer, dto);
        writer.flush();
        os.flush();
        assertEquals("{\"name\":\"storyName\",\"cron\":\"1 2 3 4 5\",\"storyText\":\"RUN ON CRON */5 * * * *\\nGIVEN COLLECTOR WEB WITH url=http://localhost:XXX\\n    KEEP responseCode AS COPPER_WEB_RETURN_CODE\\nTHEN STORE VALUES\\n\",\"hasError\":false,\"error\":null,\"nextRun\":XXX}",
                new String(os.toByteArray()).replaceAll("[0-9][0-9]+", "XXX"));
    }

}
