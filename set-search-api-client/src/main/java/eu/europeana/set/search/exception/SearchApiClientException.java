package eu.europeana.set.search.exception;

public class SearchApiClientException extends Exception{

    /**
     * 
     */
    private static final long serialVersionUID = 1439016644410763324L;
    public static final String MESSAGE_CANNOT_ACCESS_API = "Cannot access search API to retrieve items of open set.";
    public static final String MESSAGE_CANNOT_PARSE_RESPONSE = "Cannot parse search API response.";
    public static final String MESSAGE_CANNOT_RETRIEVE_ITEMS = "Cannot retrieve items of open set.";
    public static final String MESSAGE_INVALID_ISSHOWNBY = "An error occured when retrieving items through search API. IsShownBy might not be a valid API URL.";
    public SearchApiClientException(String message, Throwable th) {
	super (message, th);
    }
}
