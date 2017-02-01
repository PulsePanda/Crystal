package Exceptions;

public class ServerInitializationException extends Exception {

    /**
     * Server Initialization Exception
     * <p>
     * Used when there is an error initializing server connection
     */
    private static final long serialVersionUID = 1L;

    public ServerInitializationException(String message) {
        super(message);
    }
}
