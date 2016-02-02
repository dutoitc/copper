package ch.mno.copper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Store for values. Timestamp is set as data insertion.
 * Put will erase data. get will return null or -1 if not found.
 * Created by dutoitc on 29.01.2016.
 */
public class ValuesStore {

    // TODO: dependency graph, with temporized triggers and quiet time

    private static final ValuesStore instance = new ValuesStore();
    private Map<String, StoreValue> map = new HashMap<>();
    private Set<String> changedValues = new HashSet<>();

    protected ValuesStore() {
    }

    public static ValuesStore getInstance() {
        return instance;
    }

    public void put(String key, String value) {
        if (map.containsKey(key) && map.get(key).equals(value)) return;
        map.put(key, new StoreValue(value));
        changedValues.add(key);
    }

    public Collection<String> getChangedValues() {
        // TODO: Improve this with a quiet time for stable values ?
        List<String> values = new ArrayList<>(changedValues);
        changedValues.clear();
        return values;
    }

    public String getValue(String key) {
        if (map.containsKey(key)) {
            return map.get(key).value;
        }
        return null;
    }

    public long getTimestamp(String key) {
        if (map.containsKey(key)) {
            return map.get(key).timestamp;
        }
        return -1;
    }

    public static class StoreValue {
        private String value;
        private long timestamp;
        public StoreValue(String value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }
    }

}
