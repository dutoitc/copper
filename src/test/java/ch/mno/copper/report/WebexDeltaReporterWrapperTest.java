package ch.mno.copper.report;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.store.MapValuesStore;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WebexDeltaReporterWrapperTest {

    private StoryGrammar storyGrammar;

    @BeforeEach
    void init() {
        storyGrammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));
    }

    @Test
    void testMissingPattern() {
        var story = "blah\n" +
                "REPORT BY TELEPATHY EVERYTHING";
        assertThrows(RuntimeException.class, () -> new WebexDeltaReporterWrapper(storyGrammar, story));
    }

    @Test
    void testAll() {
        var story = "RUN ON CRON */5 * * * *\n" +
                "GIVEN STORED VALUES\n" +
                "THEN REPORT BY WEBEX\n" +
                "WITH delta=5\n" +
                "WITH token=\"sometoken\"\n" +
                "WITH room_id=\"someroom\"\n" +
                "WITH key_filter=\".*Status\"\n" +
                "WITH message=\"<h3>Ref-Mon detected status changes:</h3><br/>{{STATUS}}\"";


        WebexReporter reporter = Mockito.mock(WebexReporter.class);
        //
        var wrapper = new WebexDeltaReporterWrapper(storyGrammar, story);
        wrapper.setReporter4Tests(reporter);

        // Check
        MapValuesStore valuesStore = new MapValuesStore();
        valuesStore.put("key1", "value1");
        valuesStore.put("OneStatus", "status1");
        valuesStore.put("key2", "value2");
        valuesStore.put("TwoStatus", "status2");
        wrapper.execute(new HashMap<>(), valuesStore);
    }

}