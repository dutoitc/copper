package ch.mno.copper.store.db;

import ch.mno.copper.store.StoreException;
import ch.mno.copper.store.StoreValue;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.store.data.InstantValues;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by dutoitc on 19.09.2017.
 */
public class DBValuesStore implements ValuesStore {

    private DBServer server;

    public DBValuesStore(DBServer server) {
        this.server = server;
    }

    @Override
    public void put(String key, String value) {
        server.insert(key, value, Instant.now());
    }

    public void put(String key, String value, Instant d) {
        server.insert(key, value, d);
    }

    public void clearAllData() {
        server.clearAllData();
    }

    @Override
    public String getValue(String key) {
        StoreValue storeValue;
        storeValue = server.readLatest(key);
        if (storeValue == null) {
            return ""; // no store
        }
        return storeValue.getValue();
    }

    @Override
    public Map<String, StoreValue> getValues() {
        return server.readLatest().stream()
                .collect(Collectors.toMap(StoreValue::getKey, x -> x, (a, b) -> b)); // Ignoring duplicate, keep last

    }

    @Override
    public Collection<String> queryValues(Instant from, Instant to) {
        return server.readUpdatedKeys(from, to);
    }

    @Override
    public List<StoreValue> queryValues(Instant from, Instant to, List<String> columns, int maxValues) {
        List<StoreValue> values = new ArrayList<>();
        for (String key : columns) {
            try {
                values.addAll(server.read(key, from, to, maxValues));
            } catch (SQLException e) {
                throw new StoreException(e.getMessage(), e);
            }
        }
        return values;
    }

    @Override
    public List<InstantValues> queryValues(Instant from, Instant to, long intervalSecond, List<String> columns, int maxValues) {
        return server.readInstant(columns, from, to, intervalSecond, maxValues);

    }

    @Override
    public void load() {
        // Nothing yet
    }

    @Override
    public void save() {
        // Nothing yet
    }

    @Override
    public Map<String, String> getValuesMapString() {
        return server.readLatest().stream()
                .distinct()
                .collect(Collectors.toMap(StoreValue::getKey, StoreValue::getValue));
    }

    @Override
    public String getValuesAlerts() {
        return server.findAlerts();
    }

    @Override
    public String deleteValuesOlderThanXDays(int nbDays) {
        int nb = server.deleteValuesOlderThanXDays(nbDays);
        return "OK, " + nb + " deleted";
    }

    @Override
    public String deleteValuesOfKey(String key) {
        int nb = server.deleteValuesOfKey(key);
        return "OK, " + nb + " deleted";
    }

    @Override
    public String deleteDuplicates() {
        int nb = server.deleteDuplicates();
        return "OK, " + nb + " deleted";
    }

}
