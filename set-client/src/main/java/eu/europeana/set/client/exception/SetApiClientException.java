package eu.europeana.set.client.exception;

public class SetApiClientException extends Exception {
    private static final long serialVersionUID = 8281933808897246375L;

    public SetApiClientException(String message, Exception e) {
        super(message, e);
    }

    public SetApiClientException(String message) {
        super(message);
    }
}

