package ch.mno.copper.report;

import ch.mno.copper.stories.Story;
import ch.mno.copper.stories.StoryGrammar;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dutoitc on 26.04.2019.
 */
public class CsvReporterWrapperTest {

    private StoryGrammar storyGrammar;

    @Before
    public void init() throws FileNotFoundException {
        storyGrammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));
    }

    @Test
    public void test() throws IOException {
        // New file
        File file = File.createTempFile("copper", "tmp");
        file.delete();
        file.deleteOnExit();

        // Story
        String story = "REPORT BY CSV to \"" + file.getAbsolutePath() + "\" WITH headers=\"my header1;my header2;my header3\"\n" +
                "    WITH line=\"{{value1}};{{value2}};{{value3}}\"";
        CsvReporterWrapper wrapper = ReporterWrapperFactory.buildReporterWrapper(storyGrammar, story);

        // Run
        Map<String, String> values = new HashMap();
        values.put("value1", "123");
        values.put("value2", "456");
        values.put("value3", "789");
        wrapper.execute(values);

        // Test
        String res = IOUtils.toString(new FileInputStream(file));
        Assert.assertEquals("my header1;my header2;my header3\r\n123;456;789\r\n", res);

        // Run
        values = new HashMap();
        values.put("value1", "aaa");
        values.put("value3", "ccc");
        wrapper.execute(values);

        // Test
        res = IOUtils.toString(new FileInputStream(file));
        Assert.assertEquals("my header1;my header2;my header3\r\n123;456;789\r\naaa;;ccc\r\n", res);
    }

}
