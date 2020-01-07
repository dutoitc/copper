package ch.mno.copper.store.db;

import javax.sql.DataSource;

public class DBServerSpring extends DBServer {

    public DBServerSpring(DataSource dataSource) {
        cp = dataSource;
    }
}
