package ch.mno.copper.store.db;

import ch.mno.copper.store.StoreValue;
import ch.mno.copper.store.data.InstantValue;
import ch.mno.copper.store.data.InstantValues;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.metadata.HikariDataSourcePoolMetadata;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
public class DBServer implements AutoCloseable {

    private static Logger LOG = LoggerFactory.getLogger(DBServer.class);
    public static final Instant INSTANT_MAX = Instant.parse("3000-12-31T00:00:00.00Z");
    protected DataSource cp;



    /**
     * Delete all DB store
     */
    public void clearAllData() {
        String sql = "delete from valuestore";
        try (Connection con = cp.getConnection()) {
            int nbRows;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                nbRows = ps.executeUpdate();
            }
            LOG.info("Deleted " + nbRows + " lines");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private long nextSequence() throws SQLException {
        String sqlNextSequence = "select nextval('SEQ_VALUESTORE_ID')";

        try (Connection con2 = cp.getConnection()) {
            // Find next sequence number
            try (ResultSet rs = con2.prepareCall(sqlNextSequence).executeQuery()) {
                if (!rs.next()) throw new RuntimeException("Sequence error");
                return rs.getLong(1);
            }
        }
    }

    /**
     * Insert a value at given instant. Actuve value will be finished at the same instant. no store is inserted if current value is the same value
     */
    public void insert(String key, String value, Instant instant) throws SQLException {
        String sqlInsert = "INSERT INTO valuestore ( idvaluestore, key, value, datefrom, dateto) VALUES (?,?,?,?,?)";
        String sqlUpdatePrevious = "update valuestore set dateto=? where idvaluestore=?";

        try (Connection con = cp.getConnection();
             PreparedStatement stmt = con.prepareStatement(sqlInsert)) {
            // no store is inserted if current value is the same value
            StoreValue previousValue = readLatest(key);
            if (previousValue != null) {
                if (previousValue.getValue() == null) {
                    if (value == null) {
                        return; // No update
                    }
                } else {
                    if (previousValue.getValue().equals(value)) {
                        return; // No update
                    }
                }
            }
            if (value==null ) value="";

            long id = nextSequence();
            stmt.setLong(1, id);
            stmt.setString(2, key);
            stmt.setString(3, value);
            stmt.setTimestamp(4, Timestamp.from(instant));
            stmt.setTimestamp(5, Timestamp.from(INSTANT_MAX));
            int rowInserted = stmt.executeUpdate();
            if (rowInserted != 1) {
                throw new RuntimeException("DB error: inserted " + rowInserted + " values.");
            }

            // Stop previous
            if (previousValue != null) {
                if (previousValue.getTimestampFrom().isAfter(instant)) {
                    throw new RuntimeException("Cannot insert value in the past for key " + key + ", old.start=" + previousValue.getTimestampFrom() + ", new.start=" + instant);
                }

                try (PreparedStatement stmt2 = con.prepareStatement(sqlUpdatePrevious)) {
                    stmt2.setTimestamp(1, Timestamp.from(instant));
                    stmt2.setLong(2, previousValue.getId());
                    stmt2.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException("Cannot update previous value: " + e.getMessage(), e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("An error occured while saving values", e);
        }
    }

    /**
     * Read the 'key' value at given instant
     */
    public StoreValue read(String key, Instant timestamp) throws SQLException {
        String sql = "SELECT idvaluestore, key, value, datefrom, dateto FROM valuestore where key=? and datefrom<=? and dateto>? order by datefrom";
        try (Connection con = cp.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, key);
            stmt.setTimestamp(2, Timestamp.from(timestamp));
            stmt.setTimestamp(3, Timestamp.from(timestamp));
            List<StoreValue> values;
            try (ResultSet rs = stmt.executeQuery()) {
                values = new ArrayList<>();
                while (rs.next()) {
                    values.add(StoreValueMapper.map(rs, false));
                }
            }

            if (values.size() == 0) {
                return null;
            }
            if (values.size() == 1) {
                return values.get(0);
            }
        } catch (SQLException e) {
            throw new RuntimeException("An error occured while saving values", e);
        }


        throw new RuntimeException("Too much value for key=" + key + ", instant=" + timestamp.getEpochSecond());
    }

    public  List<InstantValues> readInstant(List<String> keys, Instant timestampFrom, Instant timestampTo, long intervalSeconds, int maxValues) throws SQLException {
        String sql = "select * from (" +
                "select ts,c1, value, idValueStore, key from ( " +
                "select ts, c1 from ( " +
                "WITH RECURSIVE T(ts) AS ( " +
                "    SELECT ? " +
                "    UNION ALL " +
                "    SELECT dateadd('second', ?, ts) FROM T WHERE ts<? " +
                ") " +
                "SELECT * FROM T) t,  (values (XXX)) " +
                ") left outer join valuestore vs on ts>=vs.datefrom and ts<vs.dateto and key =c1 " +
                "order by ts desc, key desc" +
                " FETCH FIRST " + maxValues + " ROWS ONLY " +
                ") order by ts, key";
        String s = "?";
        for (int i = 1; i < keys.size(); i++) {
            s += "),(?";
        }
        sql = sql.replace("XXX", s);
        try (Connection con = cp.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.from(timestampFrom));
            stmt.setLong(2, intervalSeconds);
            stmt.setTimestamp(3, Timestamp.from(timestampTo));
            for (int i = 0; i < keys.size(); i++) {
                stmt.setString(4 + i, keys.get(i));
            }
            List<InstantValues> values;
            try (ResultSet rs = stmt.executeQuery()) {
                values = new ArrayList<>();
                InstantValues last = null;
                while (rs.next()) {
                    InstantValue instantValue = InstantValueMapper.map(rs);
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
            throw new RuntimeException("An error occured while reading values", e);
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
                "SELECT idvaluestore, key, value, datefrom, dateto FROM valuestore " +
                "where key=? and ((datefrom<? and dateto>?) or (datefrom>=? and datefrom<?) or (dateto>? and dateto<=?)) " +
                "order by datefrom desc " +
                " FETCH FIRST " + maxValues + " ROWS ONLY " +
                ") order by datefrom";
        try (Connection con = cp.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, key);
            stmt.setTimestamp(2, Timestamp.from(timestampFrom));
            stmt.setTimestamp(3, Timestamp.from(timestampTo));
            stmt.setTimestamp(4, Timestamp.from(timestampFrom));
            stmt.setTimestamp(5, Timestamp.from(timestampTo));
            stmt.setTimestamp(6, Timestamp.from(timestampFrom));
            stmt.setTimestamp(7, Timestamp.from(timestampTo));
            List<StoreValue> values;
            try (ResultSet rs = stmt.executeQuery()) {
                values = new ArrayList<>();
                while (rs.next()) {
                    values.add(StoreValueMapper.map(rs, false));
                }
            }
            return values;
        } catch (SQLException e) {
            throw new RuntimeException("An error occured while saving values", e);
        }
    }

    /**
     * Read the latest value of a key)
     */
    public  StoreValue readLatest(String key) throws SQLException {
        try (Connection con = cp.getConnection();
             PreparedStatement stmt = con.prepareStatement("SELECT idvaluestore, key, value, datefrom, dateto FROM valuestore where key=? and dateto=?")) {
            stmt.setString(1, key);
            stmt.setTimestamp(2, Timestamp.from(INSTANT_MAX));
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                return StoreValueMapper.map(rs, false);
            }
        } catch (SQLException e) {
            throw new RuntimeException("An error occured while saving values", e);
        }
    }

    /**
     * Read all latest values
     */
    public  List<StoreValue> readLatest() throws SQLException {
        List<StoreValue> values = new ArrayList<>();
        try (Connection con = cp.getConnection();
             PreparedStatement stmt = con.prepareStatement("SELECT idvaluestore, key, value, datefrom, dateto,\n" +
                     "(select count(*) from valuestore vs2 where vs2.key = vs.key) as nbValues\n" +
                     "FROM valuestore vs\n" +
                     "where dateto=?")) {
            stmt.setTimestamp(1, Timestamp.from(INSTANT_MAX));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    values.add(StoreValueMapper.map(rs, true));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("An error occured while saving values", e);
        }
        return values;
    }

    public String findAlerts() {
        String sql="\n" +
                "        select key, count(*) as nb\n" +
                "        from valuestore\n" +
                "        group by key\n" +
                "        having count(*)>100\n" +
                "        order by count(*) desc";

        StringBuilder sb = new StringBuilder();
        try (Connection con = cp.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sb.append(rs.getString("key"));
                    sb.append(':');
                    sb.append(rs.getLong("nb"));
                    sb.append('\n');
                }
                return sb.toString();
            }
        } catch (SQLException e) {
            throw new RuntimeException("An error occured while saving values", e);
        }
    }

    /**
     * Read keys updated between from(inclusive) ant to(exclusive)
     */
    public  Collection<String> readUpdatedKeys(Instant from, Instant to) {
        List<String> values = new ArrayList<>();
        try (Connection con = cp.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT distinct key FROM valuestore where datefrom>=? and datefrom<?")) {
            stmt.setTimestamp(1, Timestamp.from(from));
            stmt.setTimestamp(2, Timestamp.from(to));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    values.add(rs.getString("key"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("An error occured while reading values", e);
        }
        return values;
    }

    @Override
    public void close() throws Exception {
    }

    public int deleteValuesOlderThanXDays(int nbDays) {
        try (Connection con = cp.getConnection();
             PreparedStatement stmt = con.prepareStatement("DELETE from valuestore where dateto<DATEADD('DAY',-" + nbDays + ", CURRENT_DATE)")) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("An error occured while reading values", e);
        }
    }
}
