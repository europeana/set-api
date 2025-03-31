package eu.europeana.api.set.integration.exception;

/**
 * This class is meant to be used for marking and handling technical exceptions that might occur within the system
 *
 * @author GrafR
 */
public class SetIntegrationException extends Exception {

    private static final long serialVersionUID = -2040704860209418648L;

    /**
     * SetIntegrationException with message and exception
     * @param message error message
     * @param e exception thrown
     */
    public SetIntegrationException(String message, Exception e) {
        super(message, e);
    }

    /**
     * SetIntegrationException with message
     * @param message error message
     */
    public SetIntegrationException(String message) {
        super(message);
    }
}
