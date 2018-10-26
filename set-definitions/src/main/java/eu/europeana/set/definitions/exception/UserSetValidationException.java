package eu.europeana.set.definitions.exception;

/**
 * This class is used represent validation errors for the user set class hierarchy 
 * @author GrafR 
 *
 */
public class UserSetValidationException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6895963160368650224L;
	public static final String ERROR_NULL_EUROPEANA_ID = "europeanaId must not be null";
	public static final String ERROR_NOT_NULL_OBJECT_ID = "Object ID must be null";
	public static final String ERROR_NULL_USER_SET_ID = "User set ID must not be null";
	public static final String ERROR_NULL_CREATOR = "Creator must not be null";
	public static final String ERROR_INVALID_BODY = "Invalid values in user set body!";

	public UserSetValidationException(String message){
		super(message);
	}
	
	public UserSetValidationException(String message, Throwable th){
		super(message, th);
	}
	
	
}
