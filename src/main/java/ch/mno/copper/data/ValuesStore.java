package ch.mno.copper.data;

import java.io.IOException;
import java.time.LocalDateTime;
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

    Collection<String> queryValues(LocalDateTime from, LocalDateTime to);

    List<List<String>> queryValues(LocalDateTime from, LocalDateTime to, String columns);

    void load() throws IOException;

    void save() throws IOException;

}
