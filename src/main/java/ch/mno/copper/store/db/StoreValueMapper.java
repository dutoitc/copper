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
        String dbKey = rs.getString("vkey");
        String value = rs.getString("vvalue");
        Instant from = rs.getTimestamp("datefrom").toInstant();
        Instant datelastcheck = rs.getTimestamp("datelastcheck").toInstant();
        Instant to = rs.getTimestamp("dateto").toInstant();
        long nbValues = -1;
        if (wantNbValues) {
            nbValues = rs.getLong("nbValues");
        }
        return new StoreValue(idValueStore, dbKey, value, from, to, datelastcheck, nbValues);
    }

}
