package ch.mno.copper.stories;

public class StoryException extends RuntimeException {

    public StoryException(String message) {
        super(message);
    }

    public StoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
