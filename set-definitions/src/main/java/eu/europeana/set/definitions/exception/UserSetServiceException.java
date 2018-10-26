package eu.europeana.set.definitions.exception;


public class UserSetServiceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -167560566275881316L;

	public UserSetServiceException(String message, Throwable th) {
		super(message, th);
	}

	public UserSetServiceException(String message) {
		super(message);
	}
	
	
}
