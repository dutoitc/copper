package ch.mno.copper.store.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

import ch.mno.copper.store.data.InstantValue;

public class InstantValueMapper {

    static InstantValue map(ResultSet rs) throws SQLException {
        long idValueStore = rs.getLong("idValueStore");
        if (idValueStore == 0) {
            idValueStore = -1; // TODO: check if id is null, not zero
        }
        String dbKey = rs.getString("c1");
        String value = rs.getString("value");
        if (value == null) {
            value = "";
        }
        Timestamp timestamp = rs.getTimestamp("ts");
        Instant ts = null;
        if (timestamp != null) {
            ts = timestamp.toInstant();
        }
        return new InstantValue(idValueStore, dbKey, value, ts);
    }

}
