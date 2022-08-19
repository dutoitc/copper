package ch.mno.copper;

public class CopperException extends RuntimeException {

    public CopperException(String message) {
        super(message);
    }

    public CopperException(String message, Throwable cause) {
        super(message, cause);
    }

}