package ch.mno.copper.store.data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dutoitc on 30.09.2017.
 */
public class InstantValues {

    protected Instant timestamp;
    protected Map<String, InstantValue> values = new HashMap<>();

    public void put(String key, InstantValue value) {
        values.put(key, value);
    }


    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, InstantValue> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "InstantValues{" +
                "timestamp=" + timestamp +
                ", values=" + values +
                '}';
    }
}
