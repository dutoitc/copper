package ch.mno.copper.store.db;

import ch.mno.copper.store.StoreValue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class StoreValueMapper {

    private StoreValueMapper() {

    }

    static StoreValue map(ResultSet rs, boolean wantNbValues) throws SQLException {
        long idValueStore = rs.getLong("idValueStore");
        String dbKey = rs.getString("key");
        String value = rs.getString("value");
        Instant from = rs.getTimestamp("datefrom").toInstant();
        Instant to = rs.getTimestamp("dateto").toInstant();
        Long nbValues = -1l;
        if (wantNbValues) {
            nbValues = rs.getLong("nbValues");
        }
        return new StoreValue(idValueStore, dbKey, value, from, to, nbValues);
    }

}
