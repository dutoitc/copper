package ch.mno.copper.data;

import java.time.Instant;

/**
 * Created by xsicdt on 25/08/17.
 */
public class StoreValue {

    protected long id;
    protected String key;
    protected String value;
    protected Instant timestampFrom;
    protected Instant timestampTo;
    protected Long nbValues;

    public String getValue() {
        return value;
    }

    public long getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public Instant getTimestampFrom() {
        return timestampFrom;
    }

    public Instant getTimestampTo() {
        return timestampTo;
    }

    public Long getNbValues() {
        return nbValues;
    }

    public StoreValue(long id, String key, String value, Instant timestampFrom, Instant timestampTo, long nbValues) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.timestampFrom = timestampFrom;
        this.timestampTo = timestampTo;
        this.nbValues = nbValues;
    }

    @Override
    public String toString() {
        return "StoreValue{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", timestampFrom=" + timestampFrom +
                ", timestampTo=" + timestampTo +
                ", nbValues=" + nbValues +
                '}';
    }
}
