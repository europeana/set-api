package eu.europeana.set.definitions.exception;

/**
 * This class is used to represent header validation errors for the user set class hierarchy 
 * @author GrafR 
 *
 */
public class UserSetProfileValidationException extends Exception {

	private static final long   serialVersionUID      = -3417903860168591652L;
	public  static final String ERROR_INVALID_PROFILE = "Invalid value for requested profile!";
	private String requestedProfile;
	
	public String getRequestedProfile() {
		return requestedProfile;
	}

	public void setRequestedProfile(String requestedProfile) {
		this.requestedProfile = requestedProfile;
	}

	public UserSetProfileValidationException(String requestedProfile){
		this(ERROR_INVALID_PROFILE, requestedProfile, null);
	}
	
	public UserSetProfileValidationException(String message, String requestedProfile, Throwable th){
		super(message, th);
		this.requestedProfile = requestedProfile;
	}
	
	
}
