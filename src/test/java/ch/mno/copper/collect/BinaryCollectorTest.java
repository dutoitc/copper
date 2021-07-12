package ch.mno.copper.collect;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BinaryCollectorTest {


    private StoryGrammar storyGrammar;

    @BeforeEach
    void setup() throws IOException {
        storyGrammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));
    }


    @Test
    void testCheckByPathOnInexistant() throws ConnectorException {
        BinaryCollectorWrapper collector = BinaryCollectorWrapper.buildCollector(storyGrammar,
                "BINARY_CHECK\nCHECK_BY_PATH dummy AS DUMMY_AVAILABLE\n");
        assertEquals("DUMMY_AVAILABLE", collector.getAs().get(0));
        assertEquals("KO", collector.execute2D().get(0).get(0));
        assertEquals("KO", collector.execute().get("DUMMY_AVAILABLE"));
    }

    @Test
    void testCheckByPathOnExistant() throws ConnectorException, IOException {
        File f = File.createTempFile("dummy", "tmp");
        f.deleteOnExit();
        BinaryCollectorWrapper collector = BinaryCollectorWrapper.buildCollector(storyGrammar,
                "BINARY_CHECK\nCHECK_BY_PATH " + f.getAbsolutePath() + " AS DUMMY_AVAILABLE\n");
        assertEquals("DUMMY_AVAILABLE", collector.getAs().get(0));
        assertEquals("OK", collector.execute2D().get(0).get(0));
        assertEquals("OK", collector.execute().get("DUMMY_AVAILABLE"));
    }

    @Test
    void testWhichOnInexistant() throws ConnectorException {
        BinaryCollectorWrapper collector = BinaryCollectorWrapper.buildCollector(storyGrammar,
                "BINARY_CHECK\nCHECK_BY_WHICH dummy123 AS DUMMY_AVAILABLE\n");
        assertEquals("DUMMY_AVAILABLE", collector.getAs().get(0));
        assertEquals("KO", collector.execute2D().get(0).get(0));
        assertEquals("KO", collector.execute().get("DUMMY_AVAILABLE"));
    }

    @Test
    void testWhichOnExistant() throws ConnectorException {
        BinaryCollectorWrapper collector = BinaryCollectorWrapper.buildCollector(storyGrammar,
                "BINARY_CHECK\nCHECK_BY_WHICH ls AS DUMMY_AVAILABLE\n");
        assertEquals("DUMMY_AVAILABLE", collector.getAs().get(0));
        assertEquals("OK", collector.execute2D().get(0).get(0));
        assertEquals("OK", collector.execute().get("DUMMY_AVAILABLE"));
    }

}