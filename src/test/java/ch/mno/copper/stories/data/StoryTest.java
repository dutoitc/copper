package ch.mno.copper.stories.data;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.store.data.InstantValues;
import ch.mno.copper.store.StoreValue;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.stories.data.Story;
import ch.mno.copper.stories.data.StoryGrammar;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xsicdt on 19/01/18.
 */
public class StoryTest {
    StoryGrammar grammar;


    @Before
    public void init() throws FileNotFoundException {
        grammar = new StoryGrammar(new FileInputStream("src/main/resources/StoryGrammar.txt"));
    }

    @Test
    public void testMatchWhen() {
        Assert.assertFalse(Story.matchWhen("17", ">", "18"));
        Assert.assertFalse(Story.matchWhen("18", ">", "18"));
        Assert.assertTrue(Story.matchWhen("19", ">", "18"));
        Assert.assertFalse(Story.matchWhen("19", "<", "18"));
        Assert.assertFalse(Story.matchWhen("18", "<", "18"));
        Assert.assertTrue(Story.matchWhen("17", "<", "18"));
        Assert.assertTrue(Story.matchWhen("17", "=", "17"));
    }

    @Test
    public void testMatchWhen2() {
        Assert.assertFalse(Story.matchWhen("17.1", ">", "18.1"));
        Assert.assertFalse(Story.matchWhen("18.1", ">", "18.1"));
        Assert.assertTrue(Story.matchWhen("19.1", ">", "18.1"));
        Assert.assertFalse(Story.matchWhen("19.1", "<", "18.1"));
        Assert.assertFalse(Story.matchWhen("18.1", "<", "18.1"));
        Assert.assertTrue(Story.matchWhen("17.1", "<", "18.1"));
        Assert.assertTrue(Story.matchWhen("17.1", "=", "17.1"));
    }

    @Test
    public void testStory() throws IOException, ConnectorException {
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
            public void load() throws IOException {

            }

            @Override
            public void save() throws IOException {

            }

            @Override
            public Map<String, String> getValuesMapString() {
                return null;
            }

            @Override
            public String getValuesAlerts() {
                return "";
            }
        };
        Assert.assertTrue(story.matchWhen(values, store));
        values.put("AAA", "0");
        Assert.assertFalse(story.matchWhen(values, store));
    }

}
