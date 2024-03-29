package ch.mno.copper.store;

import ch.mno.copper.helpers.NotImplementedException;
import ch.mno.copper.store.data.InstantValues;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** Map value store without date (only last values are kept) */
public class MapValuesStore implements ValuesStore {

    private ConcurrentHashMap<String, StoreValue> map = new ConcurrentHashMap<>();
    public static final Instant INSTANT_MAX = Instant.parse("3000-12-31T00:00:00.00Z");
    private long nextId=1;

    @Override
    public void put(String key, String value) {
        Instant now = Instant.now();
        map.put(key, new StoreValue(nextId++, key, value, now, INSTANT_MAX, now,1));
    }

    @Override
    public String getValue(String key) {
        if (map.get(key)==null) {
            return null;
        }
        return map.get(key).getValue();
    }

    @Override
    public Map<String, StoreValue> getValues() {
        return map;
    }

    @Override
    public Collection<String> queryValues(Instant from, Instant to) {
        return map.keySet();
    }

    @Override
    public List<StoreValue> queryValues(Instant from, Instant to, List<String> columns, int maxValues) {
        return new ArrayList<>(map.values());
    }

    @Override
    public List<InstantValues> queryValues(Instant from, Instant to, long intervalSecond, List<String> columns, int maxValues) {
        List<InstantValues> values = new ArrayList<>();
        map.values().forEach(storeValue -> {
            var instantValues = new InstantValues();
            instantValues.setTimestamp(storeValue.timestampFrom);
            instantValues.put(storeValue.getKey(), storeValue.toInstantValue());
            values.add(instantValues);
        });
        return values;
    }

    @Override
    public void load() {
        throw new NotImplementedException();
    }

    @Override
    public void save() {
        throw new NotImplementedException();
    }

    @Override
    public Map<String, String> getValuesMapString() {
        return map.values()
                .stream()
                .collect(Collectors.toMap(StoreValue::getKey, StoreValue::getValue));
    }

    @Override
    public String getValuesAlerts() {
        return null;
    }

    @Override
    public String deleteValuesOlderThanXDays(int nbDays) {
        return "OK (no historized values in Map Values Store)";
    }

    @Override
    public String deleteValuesOfKey(String key) {
        map.remove(key);
        return "";
    }

    @Override
    public String deleteDuplicates() {
        return "0"; // No historization-> no duplicates
    }

    /** Values as string for tests */
    public String getValuesAsString() {
        var sb = new StringBuilder();
        map.values().stream()
                .sorted(Comparator.comparing(StoreValue::getKey))
                .forEach(e->sb.append(String.format("[%s=%s]", e.getKey(), e.getValue())));
        return sb.toString();
    }
}
