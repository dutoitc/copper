package ch.mno.copper.collect;

public class CollectorException extends RuntimeException {

    public CollectorException(String message) {
        super(message);
    }

    public CollectorException(String message, Throwable cause) {
        super(message, cause);
    }
}
