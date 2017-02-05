package Exceptions;

public class ClientInitializationException extends Exception {

    /**
     * Client Initialization Exception
     * <p>
     * Used when there is an issue initializing client connection
     */
    private static final long serialVersionUID = 1L;

    public ClientInitializationException(String message) {
        super(message);
    }
}
