package ch.mno.copper.data;

/**
 * Created by xsicdt on 25/08/17.
 */
public class StoreValue {
    protected String value;
    protected long timestamp;

    public String getValue() {
        return value;
    }

    public StoreValue(String value) {
        this.value = value;
        this.timestamp = System.currentTimeMillis();
    }

    StoreValue(String value, long timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "StoreValue{" +
                "value='" + value + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    public long getTimestamp() {
        return timestamp;
    }
}
