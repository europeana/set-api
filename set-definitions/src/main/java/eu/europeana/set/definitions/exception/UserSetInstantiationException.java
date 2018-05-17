package eu.europeana.set.definitions.exception;

/**
 * This class is used represent validation errors for the user set class hierarchy 
 * @author GrafR 
 *
 */
public class UserSetInstantiationException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6895963160368650224L;
	public static final String DEFAULT_MESSAGE = "Cannot instantiate user set attribute: ";
	
	public UserSetInstantiationException(String attributeName){
//		super(DEFAULT_MESSAGE + attributeName);
		super(attributeName);
	}
	
	public UserSetInstantiationException(String attributeName , Throwable th){
//		super(DEFAULT_MESSAGE + attributeName, th);
		super(attributeName, th);
	}
	
}
