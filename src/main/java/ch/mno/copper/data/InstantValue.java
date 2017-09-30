package ch.mno.copper.data;

import java.time.Instant;

/**
 * Created by xsicdt on 25/08/17.
 */
public class InstantValue {

    protected long id;
    protected String key;
    protected String value;
    protected Instant timestamp;

    public String getValue() {
        return value;
    }

    public long getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public Instant getTimestamp() {
        return timestamp;
    }


    public InstantValue(long id, String key, String value, Instant timestamp) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "StoreValue{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
