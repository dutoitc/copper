package ch.mno.copper.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
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
 * Helper to readInstant-write data to a local H2 Database. The DB is automatically created if not existent.
 * Time is from-inclusive, to-exclusive like String.substring.
 * Values once stored will never end but could change over time.
 * Only one value is allowed at a given instant.
 * Insertion could only be done after already inserted values (no insertion in the past).
 *
 * Created by dutoitc on 25.05.2016.
 */
public class DbHelper {

    private static Logger LOG = LoggerFactory.getLogger(DbHelper.class);
    private static final String DBURL="jdbc:h2:./copperdb";
    private static final String DBUSER="";
    private static final String DBPASS="";
    public static final Instant INSTANT_MAX = Instant.parse("3000-12-31T00:00:00.00Z");

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot load H2: " + e.getMessage(), e);
        }
        createDatabaseIfNeeded();
    }

    private static void createDatabaseIfNeeded() {
        try (Connection con = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
             Statement stmt = con.createStatement())
        {
            // Create table ?
            ResultSet rs = stmt.executeQuery("select count(*) as nb from information_schema.tables where table_name = 'VALUESTORE'");
            rs.next();
            if (rs.getInt("nb")==0) {
                LOG.info("Database not found. Creating table VALUESTORE...");
                stmt.execute("CREATE TABLE valuestore (" +
                        "  idvaluestore int(11) NOT NULL," +
                        "  key varchar(50) NOT NULL," +
                        "  value text NOT NULL," +
                        "  datefrom timestamp NOT NULL," +
                        "  dateto timestamp NOT NULL," +
                        "  primary key (idvaluestore))");
                LOG.info("Creating sequence SEQ_VALUESTORE_ID");
                stmt.execute("create sequence SEQ_VALUESTORE_ID start with 1");
            }
        } catch (SQLException e2) {
            throw new RuntimeException("An error occured while initializing DB: " + e2.getMessage(), e2);
        }
    }


    /** Delete all DB data */
    public static void clearAllData() {
        String sql = "delete from valuestore";
        try (Connection con = DriverManager.getConnection(DBURL, DBUSER, DBPASS))
        {
            PreparedStatement ps = con.prepareStatement(sql);
            int nbRows = ps.executeUpdate();
            LOG.info("Deleted " + nbRows + " lines");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static long nextSequence() throws SQLException {
        String sqlNextSequence = "select nextval('SEQ_VALUESTORE_ID')";

        try (Connection con2 = DriverManager.getConnection(DBURL, DBUSER, DBPASS))
        {
            // Find next sequence number
            ResultSet rs = con2.prepareCall(sqlNextSequence).executeQuery();
            if (!rs.next()) throw new RuntimeException("Sequence error");
            return rs.getLong(1);
        }
    }

    /** Insert a value at given instant. Actuve value will be finished at the same instant. no data is inserted if current value is the same value */
    public static void insert(String key, String value, Instant instant) throws SQLException {
        String sqlInsert = "INSERT INTO valuestore ( idvaluestore, key, value, datefrom, dateto) VALUES (?,?,?,?,?)";
        String sqlUpdatePrevious = "update valuestore set dateto=? where idvaluestore=?";

        try (Connection con = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
             PreparedStatement stmt = con.prepareStatement(sqlInsert))
        {
            // no data is inserted if current value is the same value
            StoreValue previousValue = readLatest(key);
            if (previousValue!=null) {
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

            long id = nextSequence();
            stmt.setLong(1, id);
            stmt.setString(2, key);
            stmt.setString(3, value);
            stmt.setTimestamp(4, Timestamp.from(instant));
            stmt.setTimestamp(5, Timestamp.from(INSTANT_MAX));
            int rowInserted = stmt.executeUpdate();
            if (rowInserted!=1) {
                throw new RuntimeException("DB error: inserted " + rowInserted + " values.");
            }

            // Stop previous
            if (previousValue!=null) {
                if (previousValue.getTimestampFrom().isAfter(instant)) {
                    throw new RuntimeException("Cannot insert value in the past for key " + key + ", old.start="+previousValue.getTimestampFrom()+", new.start="+instant);
                }

                try (PreparedStatement stmt2 = con.prepareStatement(sqlUpdatePrevious))
                {
                    stmt2.setTimestamp(1, Timestamp.from(instant));
                    stmt2.setLong(2, previousValue.id);
                    stmt2.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException("Cannot update previous value: " + e.getMessage(), e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("An error occured while saving values", e);
        }
    }

    /** Read the 'key' value at given instant */
    public static StoreValue read(String key, Instant timestamp) throws SQLException {
        String sql = "SELECT idvaluestore, key, value, datefrom, dateto FROM valuestore where key=? and datefrom<=? and dateto>? order by datefrom";
        try (Connection con = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
             PreparedStatement stmt = con.prepareStatement(sql))
        {
            stmt.setString(1,key);
            stmt.setTimestamp(2,Timestamp.from(timestamp));
            stmt.setTimestamp(3,Timestamp.from(timestamp));
            ResultSet rs = stmt.executeQuery();

            List<StoreValue> values = new ArrayList<>();
            while(rs.next()) {
                values.add(mapStoreValue(rs));
            }

            if (values.size()==0) {
                return null;
            }
            if (values.size()==1) {
                return values.get(0);
            }
        } catch (SQLException e) {
            throw new RuntimeException("An error occured while saving values", e);
        }


        throw new RuntimeException("Too much value for key="+key+", instant="+timestamp.getEpochSecond());
    }

    public static List<InstantValues> readInstant(List<String> keys, Instant timestampFrom, Instant timestampTo, long intervalSeconds) throws SQLException {
        String sql = "select ts,c1, value, idValueStore from ( " +
                "select ts, c1 from ( " +
                "WITH RECURSIVE T(ts) AS ( " +
                "    SELECT ? " +
                "    UNION ALL " +
                "    SELECT dateadd('second', ?, ts) FROM T WHERE ts<? " +
                ") " +
                "SELECT * FROM T) t,  (values (XXX)) " +
                ") left outer join valuestore vs on ts>=vs.datefrom and ts<vs.dateto and key =c1 " +
                "order by ts,key";
        String s = "?";
        for (int i=1; i<keys.size(); i++) {
            s+="),(?";
        }
        sql = sql.replace("XXX", s);
        try (Connection con = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
             PreparedStatement stmt = con.prepareStatement(sql))
        {
            stmt.setTimestamp(1, Timestamp.from(timestampFrom));
            stmt.setLong(2, intervalSeconds);
            stmt.setTimestamp(3, Timestamp.from(timestampTo));
            for (int i=0; i<keys.size(); i++) {
                stmt.setString(4+i, keys.get(i));
            }
            ResultSet rs = stmt.executeQuery();

            List<InstantValues> values = new ArrayList<>();
            InstantValues last = null;
            while(rs.next()) {
                InstantValue instantValue = mapInstantValue(rs);
                if (last==null || !instantValue.getTimestamp().equals(last.timestamp)) {
                    last = new InstantValues();
                    last.setTimestamp(instantValue.getTimestamp());
                    values.add(last);
                }
                last.put(instantValue.getKey(), instantValue);
            }
            return values;
        } catch (SQLException e) {
            throw new RuntimeException("An error occured while saving values", e);
        }
    }



    /** Read all values for a given key active between from, to. (could have been inserted before and finish after) */
    public static List<StoreValue> read(String key, Instant timestampFrom, Instant timestampTo) throws SQLException {
        if (timestampTo.isAfter(Instant.now())) {
            timestampTo = Instant.now();
        }
        String sql = "SELECT idvaluestore, key, value, datefrom, dateto FROM valuestore where key=? and ((datefrom<? and dateto>?) or (datefrom>=? and datefrom<?) or (dateto>? and dateto<=?)) order by datefrom";
        try (Connection con = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
             PreparedStatement stmt = con.prepareStatement(sql))
        {
            stmt.setString(1,key);
            stmt.setTimestamp(2,Timestamp.from(timestampFrom));
            stmt.setTimestamp(3,Timestamp.from(timestampTo));
            stmt.setTimestamp(4,Timestamp.from(timestampFrom));
            stmt.setTimestamp(5,Timestamp.from(timestampTo));
            stmt.setTimestamp(6,Timestamp.from(timestampFrom));
            stmt.setTimestamp(7,Timestamp.from(timestampTo));
            ResultSet rs = stmt.executeQuery();

            List<StoreValue> values = new ArrayList<>();
            while(rs.next()) {
                values.add(mapStoreValue(rs));
            }
            return values;
        } catch (SQLException e) {
            throw new RuntimeException("An error occured while saving values", e);
        }
    }

    /** Read the latest value of a key) */
    public static StoreValue readLatest(String key) throws SQLException {
        try (Connection con = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
             PreparedStatement stmt = con.prepareStatement("SELECT idvaluestore, key, value, datefrom, dateto FROM valuestore where key=? and dateto=?"))
        {
            stmt.setString(1,key);
            stmt.setTimestamp(2, Timestamp.from(INSTANT_MAX));
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;
            return mapStoreValue(rs);
        } catch (SQLException e) {
            throw new RuntimeException("An error occured while saving values", e);
        }
    }

    /** Read all latest values */
    public static List<StoreValue> readLatest() throws SQLException {
        List<StoreValue> values = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
             PreparedStatement stmt = con.prepareStatement("SELECT idvaluestore, key, value, datefrom, dateto FROM valuestore where dateto=?"))
        {
            stmt.setTimestamp(1, Timestamp.from(INSTANT_MAX));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                values.add(mapStoreValue(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("An error occured while saving values", e);
        }
        return values;
    }

    /** Read keys updated between from(inclusive) ant to(exclusive) */
    public static Collection<String> readUpdatedKeys(Instant from, Instant to) {
        List<String> values = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
             PreparedStatement stmt = con.prepareStatement("SELECT distinct key FROM valuestore where datefrom>=? and datefrom<?"))
        {
            stmt.setTimestamp(1, Timestamp.from(from));
            stmt.setTimestamp(2, Timestamp.from(to));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                values.add(rs.getString("key"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("An error occured while saving values", e);
        }
        return values;
    }

    private static StoreValue mapStoreValue(ResultSet rs) throws SQLException {
        long idValueStore = rs.getLong("idValueStore");
        String dbKey = rs.getString("key");
        String value = rs.getString("value");
        Instant from = rs.getTimestamp("datefrom").toInstant();
        Instant to = rs.getTimestamp("dateto").toInstant();
        return new StoreValue(idValueStore, dbKey, value, from, to);
    }

    private static InstantValue mapInstantValue(ResultSet rs) throws SQLException {
        long idValueStore = rs.getLong("idValueStore");
        if (idValueStore==0) idValueStore=-1; // TODO: check if id is null, not zero
        String dbKey = rs.getString("c1");
        String value = rs.getString("value");
        if (value==null) value="";
        Instant ts = rs.getTimestamp("ts").toInstant();
        return new InstantValue(idValueStore, dbKey, value, ts);
    }


//    public static void dumpForTests() {
//        try (Connection con = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
//             PreparedStatement stmt = con.prepareStatement("SELECT idvaluestore, key, value, datefrom, dateto FROM valuestore order by key, datefrom"))
//        {
//            ResultSet rs = stmt.executeQuery();
//            System.out.println("-----------------------------------");
//            System.out.println("dumpForTests");
//            while (rs.next()) {
//                long idValueStore = rs.getLong("idValueStore");
//                String dbKey = rs.getString("key");
//                String value = rs.getString("value");
//                Instant from = rs.getTimestamp("datefrom").toInstant();
//                Instant to = rs.getTimestamp("dateto").toInstant();
//                System.out.println(idValueStore+";"+dbKey+";"+value+";"+from+";"+to);
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException("An error occured while saving values", e);
//        }
//    }

}