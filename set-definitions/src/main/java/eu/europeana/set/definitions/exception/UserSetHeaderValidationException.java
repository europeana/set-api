package eu.europeana.set.definitions.exception;

/**
 * This class is used to represent header validation errors for the user set class hierarchy 
 * @author GrafR 
 *
 */
public class UserSetHeaderValidationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3417903860168591652L;
	public static final String ERROR_INVALID_HEADER = "Invalid values in user set header!";

	public UserSetHeaderValidationException(String message){
		super(message);
	}
	
	public UserSetHeaderValidationException(String message, Throwable th){
		super(message, th);
	}
	
	
}
