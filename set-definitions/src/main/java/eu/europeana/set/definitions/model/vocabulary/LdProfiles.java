package eu.europeana.set.definitions.model.vocabulary;

/**
 * This enumeration is intended for Linked Data profiles
 * 
 * @author GrafR
 *
 */
public enum LdProfiles implements ProfileKeyword{

	MINIMAL("ldp:PreferMinimalContainer"), STANDARD("ldp:PreferContainedIRIs");
	
	private String headerValue;

	LdProfiles(String headerValue){
		this.headerValue = headerValue; 
	}
	
	/**
	 * Identifying agent type by the Linked Data value.
	 * For user friendliness the the comparison is case insensitive  
	 * @param ldValue
	 * @return
	 */
	public static LdProfiles getByHeaderValue(String headerValue){
		
		for(LdProfiles ldType : LdProfiles.values()){
			if(headerValue.equals(ldType.getHeaderValue()))
				return ldType;
		}
		return null;
	}
	
	@Override
	public String getHeaderValue() {
		return headerValue;
	}
	
	@Override
	public String toString() {
		return getHeaderValue();
	}

	
}
