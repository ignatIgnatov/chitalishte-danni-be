package bg.chitalishte.exception;

/**
 * Exception thrown when data import fails
 */
public class DataImportException extends RuntimeException {

    public DataImportException(String message) {
        super(message);
    }

    public DataImportException(String message, Throwable cause) {
        super(message, cause);
    }
}