package Exceptions;

public class ConfigurationException extends Exception {

    /**
     * Configuration Exception
     * <p>
     * Used when there is an error initializing configuration setup
     */
    private static final long serialVersionUID = 1L;

    public ConfigurationException(String message) {
        super(message);
    }
}
