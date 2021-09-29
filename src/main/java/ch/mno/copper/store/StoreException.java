package ch.mno.copper.store;

public class StoreException extends RuntimeException {

    public StoreException(String message) {
        super(message);
    }

    public StoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
