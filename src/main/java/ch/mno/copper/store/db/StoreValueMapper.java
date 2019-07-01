package ch.mno.copper.store.db;

import ch.mno.copper.store.StoreValue;
import org.h2.jdbc.JdbcSQLException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class StoreValueMapper {

    static StoreValue map(ResultSet rs) throws SQLException {
        long idValueStore = rs.getLong("idValueStore");
        String dbKey = rs.getString("key");
        String value = rs.getString("value");
        Instant from = rs.getTimestamp("datefrom").toInstant();
        Instant to = rs.getTimestamp("dateto").toInstant();
        Long nbValues = -1l;
        try {
            nbValues = rs.getLong("nbValues");
        } catch (JdbcSQLException e) {
            if (!e.getMessage().contains("Column \"nbValues\" not found")) throw e;
        }
        return new StoreValue(idValueStore, dbKey, value, from, to, nbValues);
    }

}
