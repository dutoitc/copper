package ch.mno.copper.report;

import ch.mno.copper.CopperException;
import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.store.MapValuesStore;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertThrows(CopperException.class, () -> new WebexDeltaReporterWrapper(storyGrammar, story));
    }

    @Test
    void testAll() throws ConnectorException {
        var story = "RUN ON CRON */5 * * * *\n" +
                "GIVEN STORED VALUES\n" +
                "THEN REPORT BY WEBEX\n" +
                "WITH delta=5\n" +
                "WITH token=\"sometoken\"\n" +
                "WITH room_id=\"someroom\"\n" +
                "WITH key_filter=\".*Status\"\n" +
                "WITH message=\"<h3>Ref-Mon detected status changes:</h3><br/>{{STATUS}}\"";

        final StringBuilder ret = new StringBuilder();
        var reporter = new WebexReporter() {
            @Override
            public void report(String message, Map<String, String> values) {
                ret.append(message).append(';').append(values.toString());
            }
        };

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

        assertEquals("<h3>Ref-Mon detected status changes:</h3><br/>- OneStatus: status1\n" +
                "- TwoStatus: status2;{ROOM_ID=someroom, TOKEN=sometoken}", ret.toString());
    }

}