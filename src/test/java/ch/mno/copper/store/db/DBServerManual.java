package ch.mno.copper.store.db;

import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBServerManual extends DBServer implements AutoCloseable {
    static String DBURL = "jdbc:h2:./copperdb"; // static for tests
    private static Logger LOG = LoggerFactory.getLogger(DBServerManual.class);
    private final String DBUSER = "";
    private final String DBPASS = "";
    private final int port;

    private Server server;
    private JdbcConnectionPool pool;

    public DBServerManual(boolean withWebserver, int webPort) throws SQLException {
        if (System.getProperty("copper.db.url") != null) {
            DBURL = System.getProperty("copper.db.url");
        }

        if (webPort > 0) {
            server = Server.createWebServer("-webAllowOthers", "-browser", "-webPort", "" + webPort);
        } else {
            server = Server.createWebServer("-webAllowOthers", "-browser");
        }
        server.start();
        port = server.getPort();
        LOG.info("Server DB started");
        pool = JdbcConnectionPool.create(DBURL, DBUSER, DBPASS);
        cp = pool;
        createDatabaseIfNeeded();
        fixSnapshots();

        // launch web console
        if (withWebserver) {
            try {
                new Thread(() -> {
                    Connection conn = null;
                    try {
                        conn = cp.getConnection();
                        try {
                            Server.startWebServer(conn);
                        } catch (SQLException e) {
                            System.out.println(e.getMessage());
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        if (conn != null) {
                            try {
                                conn.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            } finally {

            }
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        pool.dispose();
        if (server != null) {
            server.stop();
        }
        LOG.info("Server DB stopped");
        Thread.sleep(100);
    }

    private void createDatabaseIfNeeded() {
        LOG.info("Checking Database...");
        try (Connection con = cp.getConnection();
             Statement stmt = con.createStatement()) {

            // Create table ?
            try (ResultSet rs = stmt.executeQuery("select count(*) as nb from information_schema.tables where table_name = 'VALUESTORE'")) {
                rs.next();
                if (rs.getInt("nb") == 0) {
                    LOG.info("Database not found. Creating table VALUESTORE...");
                    stmt.execute("CREATE TABLE valuestore (" +
                            "  idvaluestore int NOT NULL," +
                            "  vkey varchar(50) NOT NULL," +
                            "  vvalue varchar(100000) NOT NULL," +
                            "  datefrom timestamp NOT NULL," +
                            "  dateto timestamp NOT NULL," +
                            "  datelastcheck timestamp NOT NULL," +
                            "  primary key (idvaluestore))");
                    LOG.info("Creating sequence SEQ_VALUESTORE_ID");
                    con.commit();
                    stmt.execute("create sequence SEQ_VALUESTORE_ID start with 1");
                    con.commit();
                }

                // Indexes
                stmt.execute("create index if not exists IDX_VS_KEY on valuestore(vkey)");
                stmt.execute("create index if not exists IDX_VS_FROM on valuestore(datefrom)");
                stmt.execute("create index if not exists IDX_VS_TO on valuestore(dateto)");
            }
            con.commit();
        } catch (SQLException e2) {
            e2.printStackTrace();
            throw new RuntimeException("An error occured while initializing DB: " + e2.getMessage(), e2);
        }
        LOG.info("Database checked");
    }

    private void fixSnapshots() {
        LOG.info("Checking Database...");
        try (Connection con = cp.getConnection();
             Statement stmt = con.createStatement()) {

            // Snapshot fixes
            List<String> keys = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery("select vkey, count(*) from valuestore where dateto='3000-12-31 01:00:00.0'\n" +
                    "group by vkey having count(*)>1")) {
                while (rs.next()) {
                    keys.add(rs.getString(1));
                }
            }

            for (String key : keys) {
                LOG.info("Fixing DB snapshots for " + key);

                try (ResultSet rs = stmt.executeQuery("select idvaluestore, datefrom, dateto from valuestore where vkey='" + key + "' order by IDVALUESTORE desc")) {
                    String lastDateFrom = null;
                    while (rs.next()) {
                        String dateFrom = rs.getString("datefrom");
                        String dateTo = rs.getString("dateto");
                        if (lastDateFrom != null && dateTo.compareTo(lastDateFrom) > 0) {
                            try (Statement stmt2 = con.createStatement()) {
                                String sql = "update valuestore set dateto='" + lastDateFrom + "' where idvaluestore=" + rs.getInt(1);
                                stmt2.execute(sql);
                            }
                        }
                        lastDateFrom = dateFrom;
                    }
                }
            }
        } catch (SQLException e2) {
            e2.printStackTrace();
            throw new RuntimeException("An error occured while initializing DB: " + e2.getMessage(), e2);
        }
        LOG.info("Database checked");
    }

    public int getPort() {
        return port;
    }
}
