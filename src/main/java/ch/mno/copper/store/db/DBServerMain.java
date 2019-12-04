package ch.mno.copper.store.db;

/**
 * Convenient class to start only the DB Server
 */
public class DBServerMain {

    public static void main(String[] args) {
        try (
                DBServer dbServer = new DBServerManual(true, 0)) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
