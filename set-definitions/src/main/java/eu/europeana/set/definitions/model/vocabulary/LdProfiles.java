package eu.europeana.set.definitions.model.vocabulary;

/**
 * This enumeration is intended for Linked Data profiles
 * 
 * @author GrafR
 *
 */
public enum LdProfiles implements ProfileKeyword{

	MINIMAL("Minimal"), STANDARD("Standard");
	
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
		
		String[] values = headerValue.split(":", 2);
		//last token
		String headerProfile  = values[values.length -1];
		
		for(LdProfiles ldType : LdProfiles.values()){
			if(headerProfile.contains(ldType.getHeaderValue()))
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
