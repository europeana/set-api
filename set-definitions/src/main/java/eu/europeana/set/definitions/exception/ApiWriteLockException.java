package eu.europeana.set.definitions.exception;

public class ApiWriteLockException extends Exception {

  private static final long serialVersionUID = -3985380086739407795L;

  public ApiWriteLockException(String mesage) {
		super(mesage);
	}

	public ApiWriteLockException(String mesage, Throwable th) {
		super(mesage, th);
	}

}
