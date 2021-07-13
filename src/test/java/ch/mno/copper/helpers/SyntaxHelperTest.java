package ch.mno.copper.helpers;

import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SyntaxHelperTest {

    private StoryGrammar storyGrammar;

    @BeforeEach
    void init() {
        storyGrammar = new StoryGrammar(Story.class.getResourceAsStream("/StoryGrammar.txt"));
    }

    @Test
    void testPatternOK() {
        String txt = "SOCKET WITH host=myhost,port=80,timeout_ms=1000\n" +
                "   KEEP status AS SOCKET_MY1\n";

        String ret = SyntaxHelper.checkSyntax(storyGrammar, storyGrammar.getPatternFull("COLLECTOR_SOCKET"), txt);
        assertEquals("SOCKET WITH host=myhost,port=80,timeout_ms=1000\n" +
                "   KEEP status AS SOCKET_MY1\n", ret);
    }

    @Test
    void testPatternStart() {
        String txt = "SOCKET WITH host=myhost,port=80,timeout_ms=1000\n" +
                "   KEEP ";

        try {
            SyntaxHelper.checkSyntax(storyGrammar, storyGrammar.getPatternFull("COLLECTOR_SOCKET"), txt);
        } catch (SyntaxException e) {
            assertEquals("Pattern \n" +
                    "   >>>SOCKET[\\s+\\r\\n]+WITH[\\s+\\r\\n]+host=.*?,[\\s+\\r\\n]*port=\\d+,[\\s+\\r\\n]*timeout_ms=\\d+[\\s+\\r\\n]\\s*(\\s*KEEP.*?\\s+status AS .*?[\\s+\\r\\n])+\n" +
                    " does not match\n" +
                    "   >>>SOCKET WITH host=myhost,port=80,timeout_ms=1000\n" +
                    "   KEEP \n" +
                    "But it matches the following patterns parts: [EOL,SPACE_EOL,CRON_EL,SPACE]\n" +
                    "\n" +
                    "Pattern start \n" +
                    "   >>>SOCKET[\\s+\\r\\n]+WITH[\\s+\\r\\n]+host=.*?,[\\s+\\r\\n]*port=\\d+,[\\s+\\r\\n]*timeout_ms=\\d+[\\s+\\r\\n]\\s*\n" +
                    "matches\n" +
                    "   >>>SOCKET WITH host=myhost,port=80,timeout_ms=1000\n" +
                    "   \n" +
                    "\n", e.getMessage());
        }
    }


    @Test
    void testOtherPatternOK() {
        String txt = "SOCKET WITH host=myhost,port=80,timeout_ms=1000\n" +
                "   KEEP status AS SOCKET_MY1\n";

        try {
            SyntaxHelper.checkSyntax(storyGrammar, storyGrammar.getPatternFull("COLLECTOR_BINARY"), txt);
        } catch (SyntaxException e) {
            assertEquals("Pattern \n" +
                    "   >>>BINARY_CHECK[\\s+\\r\\n](\\s*(CHECK_BY_WHICH|CHECK_BY_PATH)\\s+.*?\\s+AS\\s+.*?[\\s+\\r\\n])+\n" +
                    " does not match\n" +
                    "   >>>SOCKET WITH host=myhost,port=80,timeout_ms=1000\n" +
                    "   KEEP status AS SOCKET_MY1\n" +
                    "\n" +
                    "But it matches the following patterns parts: [COLLECTOR_SOCKET,SOCKET_QUERY,EOL,SPACE_EOL,JSON_QUERY,CRON_EL,SPACE]\n" +
                    "\n", e.getMessage());
        }
    }

    @Test
    void testPatternKO() {
        String txt = "CHAUSSETTE WITH host=myhost,port=80,timeout_ms=1000\n" +
                "   KEEP status AS SOCKET_MY1\n";

        try {
            SyntaxHelper.checkSyntax(storyGrammar, storyGrammar.getPatternFull("COLLECTOR_SOCKET"), txt);
        } catch (SyntaxException e) {
            assertEquals("Pattern \n" +
                            "   >>>SOCKET[\\s+\\r\\n]+WITH[\\s+\\r\\n]+host=.*?,[\\s+\\r\\n]*port=\\d+,[\\s+\\r\\n]*timeout_ms=\\d+[\\s+\\r\\n]\\s*(\\s*KEEP.*?\\s+status AS .*?[\\s+\\r\\n])+\n" +
                            " does not match\n" +
                            "   >>>CHAUSSETTE WITH host=myhost,port=80,timeout_ms=1000\n" +
                            "   KEEP status AS SOCKET_MY1\n" +
                            "\n" +
                            "But it matches the following patterns parts: [SOCKET_QUERY,EOL,SPACE_EOL,JSON_QUERY,CRON_EL,SPACE]\n" +
                            "\n"
                    , e.getMessage());
        }
    }
}
