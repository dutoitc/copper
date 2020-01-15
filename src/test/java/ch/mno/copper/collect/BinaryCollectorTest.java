package ch.mno.copper.collect;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;

public class BinaryCollectorTest {


    private StoryGrammar storyGrammar;

    @Before
    public void setup() {
        storyGrammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));
    }


    @Test
    public void testCheckByPathOnInexistant() {
        BinaryCollectorWrapper collector = BinaryCollectorWrapper.buildCollector(storyGrammar,
                "BINARY_CHECK\nCHECK_BY_PATH dummy AS DUMMY_AVAILABLE\n");
        Assert.assertEquals("DUMMY_AVAILABLE", collector.getAs().get(0));
        Assert.assertEquals("KO", collector.execute2D().get(0).get(0));
        Assert.assertEquals("KO", collector.execute().get("DUMMY_AVAILABLE"));
    }

    @Test
    public void testCheckByPathOnExistant() throws IOException {
        File f = File.createTempFile("dummy","tmp");
        f.deleteOnExit();
        BinaryCollectorWrapper collector = BinaryCollectorWrapper.buildCollector(storyGrammar,
                "BINARY_CHECK\nCHECK_BY_PATH " + f.getAbsolutePath() + " AS DUMMY_AVAILABLE\n");
        Assert.assertEquals("DUMMY_AVAILABLE", collector.getAs().get(0));
        Assert.assertEquals("OK", collector.execute2D().get(0).get(0));
        Assert.assertEquals("OK", collector.execute().get("DUMMY_AVAILABLE"));
    }

    @Test
    public void testWhichOnInexistant() {
        BinaryCollectorWrapper collector = BinaryCollectorWrapper.buildCollector(storyGrammar,
                "BINARY_CHECK\nCHECK_BY_WHICH dummy123 AS DUMMY_AVAILABLE\n");
        Assert.assertEquals("DUMMY_AVAILABLE", collector.getAs().get(0));
        Assert.assertEquals("KO", collector.execute2D().get(0).get(0));
        Assert.assertEquals("KO", collector.execute().get("DUMMY_AVAILABLE"));
    }

    @Test
    public void testWhichOnExistant() {
        BinaryCollectorWrapper collector = BinaryCollectorWrapper.buildCollector(storyGrammar,
                "BINARY_CHECK\nCHECK_BY_WHICH ls AS DUMMY_AVAILABLE\n");
        Assert.assertEquals("DUMMY_AVAILABLE", collector.getAs().get(0));
        Assert.assertEquals("OK", collector.execute2D().get(0).get(0));
        Assert.assertEquals("OK", collector.execute().get("DUMMY_AVAILABLE"));
    }

}