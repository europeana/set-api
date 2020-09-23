package eu.europeana.api.set.integration.exception;

/**
 * This class is meant to be used for marking and handling technical exceptions that might occur within the system
 *
 * @author GrafR
 */
public class TechnicalRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -2040704860209418648L;

    public TechnicalRuntimeException(String message, Exception e) {
        super(message, e);
    }

    public TechnicalRuntimeException(String message) {
        super(message);
    }
}
