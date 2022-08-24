package ch.mno.copper.report;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MailReporterWrapperTest {
    private StoryGrammar storyGrammar;

    @BeforeEach
    void init() {
        storyGrammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));
    }

    @Test
    void testMissingPattern() {
        var story = "blah\n" +
                "REPORT BY TELEPATHY EVERYTHING";
        assertThrows(RuntimeException.class, () -> new MailReporterWrapper(storyGrammar, story, "server", "user", "pass", 42, "from", "replyTo"));

    }

    @Test
    void testAll() throws ConnectorException {
        var mockReporter = Mockito.mock(MailReporter.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> argValues = ArgumentCaptor.forClass(Map.class);
        var argMessage = ArgumentCaptor.forClass(String.class);

        var story = "REPORT BY MAIL to \"dummy@vd.ch\" WITH title=\"titre {{value1}} {{value2}}\" WITH message=\"body {{value1}} {{value3}} {{value_unknown}}\"";


        var wrapper = new MailReporterWrapper(storyGrammar, story, "server", "user", "pass", 42, "from", "replyTo") {
            MailReporter buildReporter(String server, String serverUsername, String serverPassword, int serverPort, String from, String replyTo) {
                return mockReporter;
            }
        };

        // Run
        Map<String, String> values = new HashMap<>();
        values.put("value1", "abc");
        values.put("value2", "def");
        values.put("value3", "ghi");
        wrapper.execute(values, null);

        // Check
        Mockito.verify(mockReporter).report(argMessage.capture(),argValues.capture());
        assertEquals("dummy@vd.ch", argValues.getValue().get("TO"));
        assertEquals("titre abc def", argValues.getValue().get("TITLE"));
        assertEquals("body abc ghi ?", argValues.getValue().get("BODY"));
        assertEquals("body abc ghi ?", argMessage.getValue());
    }

}
