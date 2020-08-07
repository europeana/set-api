package eu.europeana.set.definitions.exception;

/**
 * Catched exception identifying user set access errors
 * @author GrafR
 *
 */
public class UserSetAccessException extends Exception {

    public static final String COULD_NOT_READ_FROM_FILE_ERROR = "Could not read content from a file.";

	private static final long serialVersionUID = 8724261367420984595L;

	public UserSetAccessException(String message, Throwable th) {
		super(message, th);
	}
}
