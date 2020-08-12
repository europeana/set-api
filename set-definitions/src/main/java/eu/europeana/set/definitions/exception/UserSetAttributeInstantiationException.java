package eu.europeana.set.definitions.exception;

/**
 * This class is used represent validation errors for the annotation class hierarchy 
 * @author Sergiu Gordea 
 *
 */
public class UserSetAttributeInstantiationException extends RuntimeException{

	private static final long serialVersionUID         = -6895963160368650224L;
	public static final String BASE_MESSAGE            = "Cannot instantiate user set attribute. ";
	public static final String DEFAULT_MESSAGE         = "Cannot instantiate user set attribute for type: ";
	public static final String MESSAGE_UNKNOWN_TYPE    = "Unknown/unsurported property. Cannot instantiate user set attribute for type: ";
	public static final String MESSAGE_UNKNOWN_KEYWORD = "Unknown/unsurported keyword. Cannot instantiate value of the user set attribute using the keyword: ";
	public static final String MESSAGE_ID_NOT_URL      = "ID value must be a valid URL";
	
	public final String propertyName;
	public final String propertyValue;
	
	public UserSetAttributeInstantiationException(String propertyName){
		this(propertyName, DEFAULT_MESSAGE);
	}
	
	public UserSetAttributeInstantiationException(String propertyName, String message){
		this(propertyName, null, message);
	}
	
	public UserSetAttributeInstantiationException(String propertyName, String propertyValue, String message){
		this(propertyName, propertyValue, message, null);
	}
	
	public UserSetAttributeInstantiationException(String propertyName, String propertyValue, String message, Throwable th){
		super(message + propertyName, th);
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
	}
	
}
