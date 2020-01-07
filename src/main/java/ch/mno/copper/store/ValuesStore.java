package ch.mno.copper.store;

import ch.mno.copper.store.data.InstantValues;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by xsicdt on 25/08/17.
 */
public interface ValuesStore {

    void put(String key, String value);

    String getValue(String key);

    Map<String, StoreValue> getValues();

    /**
     *
     * @param from
     * @param to null means no boundaries
     * @return
     */
    Collection<String> queryValues(Instant from, Instant to);

    List<StoreValue> queryValues(Instant from, Instant to, List<String> columns, int maxValues);

    List<InstantValues> queryValues(Instant from, Instant to, long intervalSecond, List<String> columns, int maxValues);

    void load() throws IOException;

    void save() throws IOException;

    @Deprecated // Use getValues()
    Map<String,String> getValuesMapString();

    String getValuesAlerts();

    String deleteValuesOlderThanXDays(int nbDays);

    String deleteValuesOfKey(String key);
}
