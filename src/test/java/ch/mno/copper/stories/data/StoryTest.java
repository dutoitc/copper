package ch.mno.copper.stories.data;

import ch.mno.copper.store.StoreValue;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.store.data.InstantValues;
import ch.mno.copper.stories.StoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by xsicdt on 19/01/18.
 */
class StoryTest {
    StoryGrammar grammar;


    @BeforeEach
    void init() throws FileNotFoundException {
        grammar = new StoryGrammar(new FileInputStream("src/main/resources/StoryGrammar.txt"));
    }

    @Test
    void testMatchWhen() {
        assertFalse(Story.matchWhen("17", ">", "18"));
        assertFalse(Story.matchWhen("18", ">", "18"));
        assertTrue(Story.matchWhen("19", ">", "18"));
        assertFalse(Story.matchWhen("19", "<", "18"));
        assertFalse(Story.matchWhen("18", "<", "18"));
        assertTrue(Story.matchWhen("17", "<", "18"));
        assertTrue(Story.matchWhen("17", "=", "17"));
    }

    @Test
    void testMatchWhen2() {
        assertFalse(Story.matchWhen("17.1", ">", "18.1"));
        assertFalse(Story.matchWhen("18.1", ">", "18.1"));
        assertTrue(Story.matchWhen("19.1", ">", "18.1"));
        assertFalse(Story.matchWhen("19.1", "<", "18.1"));
        assertFalse(Story.matchWhen("18.1", "<", "18.1"));
        assertTrue(Story.matchWhen("17.1", "<", "18.1"));
        assertTrue(Story.matchWhen("17.1", "=", "17.1"));
    }

    @Test
    void testStory() throws IOException {
        String storyText = "RUN ON CRON 5 * * * *\n" +
                "GIVEN STORED VALUES\n" +
                "WHEN AAA>0\n" +
                "THEN REPORT BY MAIL to \"test@dummy\"\n" +
                "     WITH title=\"some title\"\n" +
                "     WITH message=\"Yo !\"\n";
        Story story = new Story(grammar, "testStory", storyText);
        ValuesStore store = buildValuesStore();

        // Test values
        assertEquals("5 * * * *", story.getCron());
        assertEquals("testStory", story.getName());
        assertEquals(storyText, story.getStoryText());
        assertEquals("GIVEN STORED VALUES", story.getGiven());

        // Test no match
        Map<String, String> values = new HashMap<>();
        assertFalse(story.matchWhen(values, store));

        // Test condition on values
        values.put("AAA", "42");
        assertTrue(story.matchWhen(values, store));
        values.put("AAA", "0");
        assertFalse(story.matchWhen(values, store));

        // Test condition on store
        values = new HashMap<>();
        store.put("AAA", "42");
        assertTrue(story.matchWhen(values, store));
        store.put("AAA", "0");
        assertFalse(story.matchWhen(values, store));
    }

    @Test
    void testStoryWrongOperator() {
        assertThrows(StoryException.class, () -> Story.matchWhen("17", "&", "42"));
        assertThrows(StoryException.class, () -> Story.matchWhen("17", "&", "42.0"));
    }


    @Test
    void testStoryOK() throws IOException {
        Story story = new Story(grammar, "testStory", "RUN ON CRON 5 * * * *\n" +
                "GIVEN STORED VALUES\n" +
                "WHEN AAA>0\n" +
                "THEN REPORT BY MAIL to \"test@dummy\"\n" +
                "     WITH title=\"some title\"\n" +
                "     WITH message=\"Yo !\"\n");
        assertTrue(story.isValid());
        assertFalse(story.hasError());
        assertTrue(story.getWhen()!=null);
    }

    @Test
    void testStoryOK_Daily() throws IOException {
        Story story = new Story(grammar, "testStory", "RUN DAILY at 1234\n" +
                "GIVEN STORED VALUES\n" +
                "WHEN AAA>0\n" +
                "THEN REPORT BY MAIL to \"test@dummy\"\n" +
                "     WITH title=\"some title\"\n" +
                "     WITH message=\"Yo !\"\n");
        assertEquals("34 12 * * *", story.getCron());
    }

    @Test()
    void testStoryWrongSyntaxMissingGiven()  {
        assertThrows(StoryException.class, () -> new Story(grammar, "testStory", "RUN ON CRON 5 * * * *\n" +
                "GIVEN WRONG_SYNTAX_IN_STORY\n" +
                "WHEN AAA>0\n" +
                "THEN REPORT BY MAIL to \"test@dummy\"\n" +
                "     WITH title=\"some title\"\n" +
                "     WITH message=\"Yo !\"\n"));
    }

    @Test()
    void testStoryWrongSyntaxMissingRunOn()  {
        assertThrows(StoryException.class, () -> new Story(grammar, "testStory", "\n" +
                "GIVEN STORED VALUES\n" +
                "WHEN AAA>0\n" +
                "THEN REPORT BY MAIL to \"test@dummy\"\n" +
                "     WITH title=\"some title\"\n" +
                "     WITH message=\"Yo !\"\n"));
    }

    @Test()
    void testStoryWrongSyntaxMissingReporter()  {
        assertThrows(StoryException.class, () -> new Story(grammar, "testStory", "RUN ON CRON 5 * * * *\n" +
                "GIVEN STORED VALUES\n" +
                "WHEN AAA>0\n" +
                "THEN WRONG_SYNTAX_IN_STORY"));
    }


    @Test()
    void testStoryWrongSyntax() throws IOException {
        var story = new Story(grammar, "testStory", "RUN ON CRON 5 * * * *\n" +
                "GIVEN STORED VALUES\n" +
                "WHEN AAA>0\n" +
                "AND WRONG_SYNTAX_IN_QUERY\n" +
                "THEN REPORT BY MAIL to \"test@dummy\"\n" +
                "     WITH title=\"some title\"\n" +
                "     WITH message=\"Yo !\"\n");
        assertFalse(story.isValid());
        assertTrue(story.hasError());
        assertTrue(story.getError().contains("does not match"));
    }

    private ValuesStore buildValuesStore() {
        ValuesStore store = new ValuesStore() {
            Map<String, String> localStore = new HashMap<>();

            @Override
            public void put(String key, String value) {
                localStore.put(key, value);
            }

            @Override
            public String getValue(String key) {
                return localStore.get(key);
            }

            @Override
            public Map<String, StoreValue> getValues() {
                return null;
            }

            @Override
            public Collection<String> queryValues(Instant from, Instant to) {
                return null;
            }

            @Override
            public List<StoreValue> queryValues(Instant from, Instant to, List<String> columns, int maxValues) {
                return null;
            }

            @Override
            public List<InstantValues> queryValues(Instant from, Instant to, long intervalSecond, List<String> columns, int maxValues) {
                return null;
            }

            @Override
            public void load() {

            }

            @Override
            public void save() {

            }

            @Override
            @Deprecated
            public Map<String, String> getValuesMapString() {
                return null;
            }

            @Override
            public String getValuesAlerts() {
                return "";
            }

            @Override
            public String deleteValuesOlderThanXDays(int nbDays) {
                return null;
            }

            @Override
            public String deleteValuesOfKey(String key) {
                return null;
            }
        };
        return store;
    }


}
