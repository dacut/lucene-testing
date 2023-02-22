package org.kanga.lucenetesting;

/**
 *  Exception thrown when the user has specified invalid command line arguments.
 */
public class InvalidUsageException extends RuntimeException {
    public InvalidUsageException() {
        super();
    }

    public InvalidUsageException(String message) {
        super(message);
    }

    public InvalidUsageException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidUsageException(Throwable cause) {
        super(cause);
    }
}
