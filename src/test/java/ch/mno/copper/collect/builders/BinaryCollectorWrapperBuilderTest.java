package ch.mno.copper.collect.builders;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.collect.wrappers.BinaryCollectorWrapper;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.PropertyResolver;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BinaryCollectorWrapperBuilderTest {

    private StoryGrammar storyGrammar;

    @BeforeEach
    void setup() {
        storyGrammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));
    }

    @Test
    void testCheckByPathOnInexistant() {
        BinaryCollectorWrapperBuilder wrapperBuilder = buildBinaryCollectorWrapperBuilder();
        BinaryCollectorWrapper collector = wrapperBuilder.buildCollector("BINARY_CHECK\nCHECK_BY_PATH dummy AS DUMMY_AVAILABLE\n");
        assertEquals("DUMMY_AVAILABLE", collector.getAs().get(0));
        assertEquals("KO", collector.execute2D().get(0).get(0));
        assertEquals("KO", collector.execute().get("DUMMY_AVAILABLE"));
    }

    private BinaryCollectorWrapperBuilder buildBinaryCollectorWrapperBuilder() {
        PropertyResolver propertyResolver = Mockito.mock(PropertyResolver.class);
        Mockito.when(propertyResolver.getProperty("user")).thenReturn("username");
        Mockito.when(propertyResolver.getProperty("pass")).thenReturn("password");
        return new BinaryCollectorWrapperBuilder(storyGrammar, propertyResolver);
    }

    @Test
    void testCheckByPathOnExistant() throws ConnectorException, IOException {
        File f = File.createTempFile("dummy", "tmp");
        f.deleteOnExit();
        BinaryCollectorWrapperBuilder wrapperBuilder = buildBinaryCollectorWrapperBuilder();
        BinaryCollectorWrapper collector = wrapperBuilder.buildCollector("BINARY_CHECK\nCHECK_BY_PATH " + f.getAbsolutePath() + " AS DUMMY_AVAILABLE\n");
        assertEquals("DUMMY_AVAILABLE", collector.getAs().get(0));
        assertEquals("OK", collector.execute2D().get(0).get(0));
        assertEquals("OK", collector.execute().get("DUMMY_AVAILABLE"));
    }

    @Test
    void testWhichOnInexistant() {
        BinaryCollectorWrapperBuilder wrapperBuilder = buildBinaryCollectorWrapperBuilder();
        BinaryCollectorWrapper collector = wrapperBuilder.buildCollector("BINARY_CHECK\nCHECK_BY_WHICH dummy123 AS DUMMY_AVAILABLE\n");
        assertEquals("DUMMY_AVAILABLE", collector.getAs().get(0));
        assertEquals("KO", collector.execute2D().get(0).get(0));
        assertEquals("KO", collector.execute().get("DUMMY_AVAILABLE"));
    }

    @Test
    void testWhichOnExistant() {
        BinaryCollectorWrapperBuilder wrapperBuilder = buildBinaryCollectorWrapperBuilder();
        BinaryCollectorWrapper collector = wrapperBuilder.buildCollector("BINARY_CHECK\nCHECK_BY_WHICH ls AS DUMMY_AVAILABLE\n");
        assertEquals("DUMMY_AVAILABLE", collector.getAs().get(0));
        assertEquals("OK", collector.execute2D().get(0).get(0));
        assertEquals("OK", collector.execute().get("DUMMY_AVAILABLE"));
    }

}