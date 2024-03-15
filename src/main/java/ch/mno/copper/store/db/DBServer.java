package ch.mno.copper.store.db;

import ch.mno.copper.store.StoreException;
import ch.mno.copper.store.StoreValue;
import ch.mno.copper.store.data.InstantValues;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Helper to readInstant-write store to a local H2 Database. The DB is automatically created if not existent.
 * Time is from-inclusive, to-exclusive like String.substring.
 * Values once stored will never end but could change over time.
 * Only one value is allowed at a given instant.
 * Insertion could only be done after already inserted values (no insertion in the past).
 * <p>
 * Created by dutoitc on 25.05.2016.
 */
@Slf4j
public class DBServer implements AutoCloseable {

    public static final Instant INSTANT_MAX = Instant.parse("3000-12-31T00:00:00.00Z");
    private static final Logger LOG = LoggerFactory.getLogger(DBServer.class);
    public static final String AN_ERROR_OCCURED_WHILE_READING_VALUES = "An error occured while reading values";
    public static final String AN_ERROR_OCCURED_WHILE_SAVING_VALUES = "An error occured while saving values";
    protected DataSource cp;


    /**
     * Delete all DB store
     */
    public void clearAllData() {
        var sql = "delete from valuestore";
        try (var con = cp.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                int nbRows = ps.executeUpdate();
                LOG.info("Deleted {} lines", nbRows);
            }
        } catch (SQLException e) {
            log.debug("Exception: " + e.getMessage(), e);
        }
    }

    /**
     * Find next sequence number
     */
    private long nextSequence() throws SQLException {
        var sqlNextSequence = "select nextval('SEQ_VALUESTORE_ID')";

        try (var con2 = cp.getConnection()) {
            try (var rs = con2.prepareCall(sqlNextSequence).executeQuery()) {
                if (!rs.next()) throw new StoreException("Sequence error");
                return rs.getLong(1);
            }
        }
    }

    /**
     * Insert a value at given instant. Actuve value will be finished at the same instant.
     * If the value is the same, datelastcheck is updated
     */
    public void insert(String key, String value, Instant instant) {
        var sqlInsert = "INSERT INTO valuestore ( idvaluestore, vkey, vvalue, datefrom, dateto, datelastcheck) VALUES (?,?,?,?,?,?)";

        try (var con = cp.getConnection();
             PreparedStatement stmt = con.prepareStatement(sqlInsert)) {
            // no store is inserted if current value is the same value
            StoreValue previousValue = readLatest(key);
            if (value == null) value = "";

            if (previousValue == null) {
                insertNew(key, value, instant, stmt);
            } else if (!previousValue.getValue().equals(value)) {
                if (previousValue.getTimestampFrom().isAfter(instant)) {
                    throw new StoreException("Cannot insert value in the past for key " + key + ", old.start=" + previousValue.getTimestampFrom() + ", new.start=" + instant);
                }
                insertNew(key, value, instant, stmt);
                terminatePrevious(instant, con, previousValue);
            } else {
                updateDateLastCheck(instant, con, previousValue);
            }
        } catch (SQLException e) {
            throw new StoreException(AN_ERROR_OCCURED_WHILE_SAVING_VALUES, e);
        }
    }

    private void insertNew(String key, String value, Instant instant, PreparedStatement stmt) throws SQLException {
        long id = nextSequence();
        stmt.setLong(1, id);
        stmt.setString(2, key);
        stmt.setString(3, value);
        stmt.setTimestamp(4, Timestamp.from(instant));
        stmt.setTimestamp(5, Timestamp.from(INSTANT_MAX));
        stmt.setTimestamp(6, Timestamp.from(instant));
        int rowInserted = stmt.executeUpdate();
        if (rowInserted != 1) {
            throw new StoreException("DB error: inserted " + rowInserted + " values.");
        }
    }

    void insertForTests(String key, String value, Instant dateFrom, Instant dateTo, Instant dateLastCheck){
        var sqlInsert = "INSERT INTO valuestore ( idvaluestore, vkey, vvalue, datefrom, dateto, datelastcheck) VALUES (?,?,?,?,?,?)";

        try (var con = cp.getConnection();
             PreparedStatement stmt = con.prepareStatement(sqlInsert)) {
            long id = nextSequence();
            stmt.setLong(1, id);
            stmt.setString(2, key);
            stmt.setString(3, value);
            stmt.setTimestamp(4, Timestamp.from(dateFrom));
            stmt.setTimestamp(5, Timestamp.from(dateTo));
            stmt.setTimestamp(6, Timestamp.from(dateLastCheck));
            int rowInserted = stmt.executeUpdate();
            if (rowInserted != 1) {
                throw new StoreException("DB error: inserted " + rowInserted + " values.");
            }
        } catch (SQLException e) {
            throw new StoreException(AN_ERROR_OCCURED_WHILE_SAVING_VALUES, e);
        }
    }

    private void terminatePrevious(Instant instant, Connection con, StoreValue previousValue) {
        var sqlUpdatePrevious = "update valuestore set dateto=? where idvaluestore=?";
        try (PreparedStatement stmt2 = con.prepareStatement(sqlUpdatePrevious)) {
            stmt2.setTimestamp(1, Timestamp.from(instant));
            stmt2.setLong(2, previousValue.getId());
            stmt2.executeUpdate();
        } catch (SQLException e) {
            throw new StoreException("Cannot terminate previous value: " + e.getMessage(), e);
        }
    }

    private void updateDateLastCheck(Instant instant, Connection con, StoreValue previousValue) {
        var sqlUpdatePrevious = "update valuestore set datelastcheck=? where idvaluestore=?";
        try (PreparedStatement stmt2 = con.prepareStatement(sqlUpdatePrevious)) {
            stmt2.setTimestamp(1, Timestamp.from(instant));
            stmt2.setLong(2, previousValue.getId());
            stmt2.executeUpdate();
        } catch (SQLException e) {
            throw new StoreException("Cannot update last check date: " + e.getMessage(), e);
        }
    }


    /**
     * Read the 'key' value at given instant
     */
    public StoreValue read(String key, Instant timestamp) {
        var sql = "SELECT idvaluestore, vkey, vvalue, datefrom, dateto, datelastcheck FROM valuestore where vkey=? and datefrom<=? and dateto>? order by datefrom";
        try (var con = cp.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, key);
            stmt.setTimestamp(2, Timestamp.from(timestamp));
            stmt.setTimestamp(3, Timestamp.from(timestamp));
            List<StoreValue> values;
            try (var rs = stmt.executeQuery()) {
                values = new ArrayList<>();
                while (rs.next()) {
                    values.add(StoreValueMapper.map(rs, false));
                }
            }

            if (values.isEmpty()) {
                return null;
            }
            if (values.size() == 1) {
                return values.get(0);
            }
        } catch (SQLException e) {
            throw new StoreException(AN_ERROR_OCCURED_WHILE_SAVING_VALUES, e);
        }


        throw new StoreException("Too much value for key=" + key + ", instant=" + timestamp.getEpochSecond());
    }


    /**
     * Query some values at some interval of time, to plot graph
     */
    public List<InstantValues> readInstant(List<String> keys, Instant timestampFrom, Instant timestampTo, long intervalSeconds, int maxValues) {
        String sql = "select * from (" +
                "select ts,c1, vvalue, idValueStore, vkey from ( " +
                "select ts, c1 from ( " +
                "WITH RECURSIVE T(ts) AS ( " +
                "    SELECT ? " +
                "    UNION ALL " +
                "    SELECT dateadd('second', ?, ts) FROM T WHERE ts<? " +
                ") " +
                "SELECT * FROM T) t,  (values (XXX)) " +
                ") left outer join valuestore vs on ts>=vs.datefrom and ts<vs.dateto and vkey =c1 " +
                "order by ts desc, vkey desc" +
                " FETCH FIRST " + maxValues + " ROWS ONLY " +
                ") order by ts, vkey";

        String s = '?' + "),(?".repeat(Math.max(0, keys.size() - 1));
        sql = sql.replace("XXX", s);
        try (var con = cp.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.from(timestampFrom));
            stmt.setLong(2, intervalSeconds);
            stmt.setTimestamp(3, Timestamp.from(timestampTo));
            for (var i = 0; i < keys.size(); i++) {
                stmt.setString(4 + i, keys.get(i));
            }
            List<InstantValues> values;
            try (var rs = stmt.executeQuery()) {
                values = new ArrayList<>();
                InstantValues last = null;
                while (rs.next()) {
                    var instantValue = InstantValueMapper.map(rs);
                    if (last == null || !instantValue.getTimestamp().equals(last.getTimestamp())) {
                        last = new InstantValues();
                        last.setTimestamp(instantValue.getTimestamp());
                        values.add(last);
                    }
                    last.put(instantValue.getKey(), instantValue);
                }
            }
            return values;
        } catch (SQLException e) {
            throw new StoreException(AN_ERROR_OCCURED_WHILE_READING_VALUES, e);
        }
    }


    /**
     * Read all values for a given key active between from, to. (could have been inserted before and finish after)
     */
    public List<StoreValue> read(String key, Instant timestampFrom, Instant timestampTo, int maxValues) throws SQLException {
        if (timestampTo.isAfter(Instant.now())) {
            timestampTo = Instant.now();
        }
        String sql = "select * from (" +
                "SELECT idvaluestore, vkey, vvalue, datefrom, dateto, datelastcheck FROM valuestore " +
                "where vkey=? and ((datefrom<? and dateto>?) or (datefrom>=? and datefrom<?) or (dateto>? and dateto<=?)) " +
                "order by datefrom desc " +
                " FETCH FIRST " + maxValues + " ROWS ONLY " +
                ") order by datefrom";
        try (var con = cp.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, key);
            stmt.setTimestamp(2, Timestamp.from(timestampFrom));
            stmt.setTimestamp(3, Timestamp.from(timestampTo));
            stmt.setTimestamp(4, Timestamp.from(timestampFrom));
            stmt.setTimestamp(5, Timestamp.from(timestampTo));
            stmt.setTimestamp(6, Timestamp.from(timestampFrom));
            stmt.setTimestamp(7, Timestamp.from(timestampTo));
            List<StoreValue> values;
            try (var rs = stmt.executeQuery()) {
                values = new ArrayList<>();
                while (rs.next()) {
                    values.add(StoreValueMapper.map(rs, false));
                }
            }
            return values;
        } catch (SQLException e) {
            throw new StoreException(AN_ERROR_OCCURED_WHILE_SAVING_VALUES, e);
        }
    }

    /**
     * Read the latest value of a key)
     */
    public StoreValue readLatest(String key) {
        try (var con = cp.getConnection();
             PreparedStatement stmt = con.prepareStatement("SELECT idvaluestore, vkey, vvalue, datefrom, dateto, datelastcheck FROM valuestore where vkey=? and dateto=?")) {
            stmt.setString(1, key);
            stmt.setTimestamp(2, Timestamp.from(INSTANT_MAX));
            try (var rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                return StoreValueMapper.map(rs, false);
            }
        } catch (SQLException e) {
            throw new StoreException(AN_ERROR_OCCURED_WHILE_SAVING_VALUES, e);
        }
    }

    /**
     * Read all values of a key)
     */
    public List<StoreValue> readAll(String key) {
        List<StoreValue> values = new ArrayList<>();
        try (var con = cp.getConnection();
             PreparedStatement stmt = con.prepareStatement("SELECT idvaluestore, vkey, vvalue, datefrom, dateto, datelastcheck FROM valuestore where vkey=? order by datefrom")) {
            stmt.setString(1, key);
            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    values.add(StoreValueMapper.map(rs, false));
                }
            }
        } catch (SQLException e) {
            throw new StoreException(AN_ERROR_OCCURED_WHILE_SAVING_VALUES, e);
        }
        return values;
    }

    /**
     * Read all latest values
     */
    public List<StoreValue> readLatest() {
        List<StoreValue> values = new ArrayList<>();
        try (var con = cp.getConnection();
             PreparedStatement stmt = con.prepareStatement("SELECT idvaluestore, vkey, vvalue, datefrom, dateto,datelastcheck,\n" +
                     "(select count(*) from valuestore vs2 where vs2.vkey = vs.vkey) as nbValues\n" +
                     "FROM valuestore vs\n" +
                     "where dateto=?")) {
            stmt.setTimestamp(1, Timestamp.from(INSTANT_MAX));
            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    values.add(StoreValueMapper.map(rs, true));
                }
            }
        } catch (SQLException e) {
            throw new StoreException(AN_ERROR_OCCURED_WHILE_SAVING_VALUES, e);
        }
        return values;
    }

    public String findAlerts() {
        String sql = "\n" +
                "        select vkey, count(*) as nb\n" +
                "        from valuestore\n" +
                "        group by vkey\n" +
                "        having count(*)>100\n" +
                "        order by count(*) desc";

        var sb = new StringBuilder();
        try (var con = cp.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sb.append(rs.getString("vkey"));
                    sb.append(':');
                    sb.append(rs.getLong("nb"));
                    sb.append('\n');
                }
                return sb.toString();
            }
        } catch (SQLException e) {
            throw new StoreException(AN_ERROR_OCCURED_WHILE_SAVING_VALUES, e);
        }
    }

    /**
     * Read keys updated between from(inclusive) ant to(exclusive)
     */
    public Collection<String> readUpdatedKeys(Instant from, Instant to) {
        List<String> values = new ArrayList<>();
        try (var con = cp.getConnection();
             PreparedStatement stmt = con.prepareStatement("SELECT distinct vkey FROM valuestore where datefrom>=? and datefrom<?")) {
            stmt.setTimestamp(1, Timestamp.from(from));
            stmt.setTimestamp(2, Timestamp.from(to));
            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    values.add(rs.getString("vkey"));
                }
            }
        } catch (SQLException e) {
            throw new StoreException(AN_ERROR_OCCURED_WHILE_READING_VALUES, e);
        }
        return values;
    }

    @Override
    public void close() throws Exception {
        // Nothing to close yet
    }

    public int deleteValuesOlderThanXDays(int nbDays) {
        try (var con = cp.getConnection();
             PreparedStatement stmt = con.prepareStatement("DELETE from valuestore where dateto<DATEADD('DAY',-" + nbDays + ", CURRENT_DATE)")) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new StoreException(AN_ERROR_OCCURED_WHILE_READING_VALUES, e);
        }
    }

    public int deleteValuesOfKey(String key) {
        if (key.contains(";")) {
            throw new StoreException("SQL Injection error"); // Really simple protection
        }
        try (var con = cp.getConnection();
             var stmt = con.prepareStatement("DELETE from valuestore where vkey=?")) {
            stmt.setString(1, key);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new StoreException(AN_ERROR_OCCURED_WHILE_READING_VALUES, e);
        }
    }

    public int deleteDuplicates() {
        try (var con = cp.getConnection();
             var stmt = con.prepareStatement("DELETE FROM valuestore\n" +
                     "    WHERE idvaluestore NOT IN (\n" +
                     "    SELECT idvaluesstore\n" +
                     "    FROM (\n" +
                     "        SELECT MAX(idvaluestore) AS idvaluesstore\n" +
                     "        FROM valuestore\n" +
                     "        WHERE dateto = '3000-12-31 01:00:00.000000'\n" +
                     "        GROUP BY vkey\n" +
                     "    ) AS subquery\n" +
                     ") AND dateto = ?")) {
            stmt.setTimestamp(1, Timestamp.from(INSTANT_MAX));
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new StoreException(AN_ERROR_OCCURED_WHILE_READING_VALUES, e);
        }
    }
}
