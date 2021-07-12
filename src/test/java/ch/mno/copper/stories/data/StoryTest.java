package ch.mno.copper.stories.data;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.store.StoreValue;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.store.data.InstantValues;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void testStory() throws IOException, ConnectorException {
        Story story = new Story(grammar, "testStory", "RUN ON CRON 5 * * * *\n" +
                "GIVEN STORED VALUES\n" +
                "WHEN AAA>0\n" +
                "THEN REPORT BY MAIL to \"test@dummy\"\n" +
                "     WITH title=\"some title\"\n" +
                "     WITH message=\"Yo !\"\n");
        Map<String, String> values = new HashMap<>();
        values.put("AAA", "42");
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
        assertTrue(story.matchWhen(values, store));
        values.put("AAA", "0");
        assertFalse(story.matchWhen(values, store));
    }

}
