package ch.mno.copper.data;

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

    List<List<String>> queryValues(Instant from, Instant to, String columns);

    void load() throws IOException;

    void save() throws IOException;

    Map<String,String> getValuesMapString();
}
