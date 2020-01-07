package ch.mno.copper.store.db;

import ch.mno.copper.store.StoreValue;
import ch.mno.copper.store.ValuesStore;
import ch.mno.copper.store.data.InstantValues;

import java.io.IOException;
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
        try {
            server.insert(key, value, Instant.now());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void put(String key, String value, Instant d) {
        try {
            server.insert(key, value, d);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearAllData() {
        server.clearAllData();
    }

    @Override
    public String getValue(String key) {
        StoreValue storeValue = null;
        try {
            storeValue = server.readLatest(key);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot readInstant value " + key + ": " + e.getMessage());
        }
        if (storeValue == null) {
            return ""; // no store
        }
        return storeValue.getValue();
    }

    @Override
    public Map<String, StoreValue> getValues() {
        try {
            return server.readLatest().stream()
                    .collect(Collectors.toMap(x -> x.getKey(), x -> x));
        } catch (SQLException e) {
            throw new RuntimeException("Cannot readInstant values: " + e.getMessage(), e);
        }
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
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return values;
    }

    @Override
    public List<InstantValues> queryValues(Instant from, Instant to, long intervalSecond, List<String> columns, int maxValues) {
        try {
            return server.readInstant(columns, from, to, intervalSecond, maxValues);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void load() throws IOException {

    }

    @Override
    public void save() throws IOException {

    }

    @Override
    public Map<String, String> getValuesMapString() {
        try {
            return server.readLatest().stream()
                    .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
        } catch (SQLException e) {
            throw new RuntimeException("Cannot readInstant values: " + e.getMessage(), e);
        }
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

}
