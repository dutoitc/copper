package ch.mno.copper.data;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dutoitc on 19.09.2017.
 */
public class DbValuesStore implements ValuesStore, AutoCloseable {

    private static DbValuesStore instance;
    private static DBServer server;

    private DbValuesStore() {
        try {
            server = new DBServer(true);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Singleton factory
     */
    public static DbValuesStore getInstance() {
        if (instance == null) {
            synchronized (DbValuesStore.class) {
                if (instance == null) {
                    instance = new DbValuesStore();
                }
            }
        }
        return instance;
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
            return ""; // no data
        }
        return storeValue.getValue();
    }

    @Override
    public Map<String, StoreValue> getValues() {
        try {
            List<StoreValue> values = server.readLatest();
            Map<String, StoreValue> map = new HashMap<>(values.size() * 4 / 3 + 1);
            values.forEach(v -> map.put(v.getKey(), v));
            return map;
        } catch (SQLException e) {
            throw new RuntimeException("Cannot readInstant values: " + e.getMessage(), e);
        }
    }

    @Override
    public Collection<String> queryValues(Instant from, Instant to) {
        return server.readUpdatedKeys(from, to);
    }

    @Override
    public List<StoreValue> queryValues(Instant from, Instant to, List<String> columns) {
        List<StoreValue> values = new ArrayList<>();
        for (String key : columns) {
            try {
                values.addAll(server.read(key, from, to));
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return values;
    }

    @Override
    public List<InstantValues> queryValues(Instant from, Instant to, long intervalSecond, List<String> columns) {
        try {
            return server.readInstant(columns, from, to, intervalSecond);
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
            List<StoreValue> values = server.readLatest();
            Map<String, String> map = new HashMap<>(values.size() * 4 / 3 + 1);
            values.forEach(v -> map.put(v.getKey(), v.getValue()));
            return map;
        } catch (SQLException e) {
            throw new RuntimeException("Cannot readInstant values: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() throws Exception {
        if (server!=null) {
            server.close();
            server = null;
        }
        DbValuesStore.instance = null;
    }

    public DBServer getServer4tests() {
        return server;
    }
}
