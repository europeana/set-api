package eu.europeana.set.definitions.model.vocabulary;

import eu.europeana.set.definitions.exception.UserSetHeaderValidationException;

/**
 * This enumeration is intended for Linked Data profiles
 * 
 * @author GrafR
 *
 */
public enum LdProfiles implements ProfileKeyword {

	MINIMAL(VALUE_LD_MINIMAL, VALUE_PREFER_MINIMAL), STANDARD(VALUE_LD_CONTAINEDIRIS, VALUE_PREFER_CONTAINEDIRIS);
	
	private String headerValue;
	private String preferHeaderValue;
	

	LdProfiles(String headerValue, String preferHeaderValue){
		this.headerValue = headerValue; 
		this.preferHeaderValue = preferHeaderValue;
	}
	
	/**
	 * Identifying agent type by the Linked Data value.
	 * For user friendliness the the comparison is case insensitive  
	 * @param ldValue
	 * @return
	 * @throws UserSetHeaderValidationException 
	 */
	public static LdProfiles getByHeaderValue(String headerValue) throws UserSetHeaderValidationException{
		
		for(LdProfiles ldType : LdProfiles.values()) {
			if(headerValue.equals(ldType.getHeaderValue())) {
				return ldType;
			}
		}
		throw new UserSetHeaderValidationException(
				UserSetHeaderValidationException.ERROR_INVALID_HEADER + ": " + headerValue);		
	}
	
	public static LdProfiles getByName(String name) throws UserSetHeaderValidationException{
		
		for(LdProfiles ldType : LdProfiles.values()){
			if(name.equals(ldType.name().toLowerCase())) {
				return ldType;
			}
		}
		throw new UserSetHeaderValidationException(
				UserSetHeaderValidationException.ERROR_INVALID_HEADER + ": " + name);		
	}
	
	@Override
	public String getHeaderValue() {
		return headerValue;
	}
	
	@Override
	public String toString() {
		return getHeaderValue();
	}

	public String getPreferHeaderValue() {
		return preferHeaderValue;
	}
	
}
