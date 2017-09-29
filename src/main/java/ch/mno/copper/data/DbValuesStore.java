package ch.mno.copper.data;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dutoitc on 19.09.2017.
 */
public class DbValuesStore implements ValuesStore {

    private static DbValuesStore instance;


    /** Singleton factory */
    public static DbValuesStore getInstance() {
        if (instance==null) {
            synchronized (DbValuesStore.class) {
                if (instance==null) {
                    instance = new DbValuesStore();
                }
            }
        }
        return instance;
    }


    @Override
    public void put(String key, String value) {
        try {
            DbHelper.insert(key, value, Instant.now());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getValue(String key) {
        StoreValue storeValue = null;
        try {
            storeValue = DbHelper.readLatest(key);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot read value " + key + ": " + e.getMessage());
        }
        if (storeValue==null) {
            return ""; // no data
        }
        return storeValue.getValue();
    }

    @Override
    public Map<String, StoreValue> getValues() {
        try {
            List<StoreValue> values = DbHelper.readLatest();
            Map<String, StoreValue> map = new HashMap<>(values.size()*4/3+1);
            values.forEach(v->map.put(v.getKey(), v));
            return map;
        } catch (SQLException e) {
            throw new RuntimeException("Cannot read values: " + e.getMessage(), e);
        }
    }

    @Override
    public Collection<String> queryValues(Instant from, Instant to) {
        return DbHelper.readUpdatedKeys(from, to);
    }

    @Override
    public List<List<String>> queryValues(Instant from, Instant to, String columns) {
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
}
