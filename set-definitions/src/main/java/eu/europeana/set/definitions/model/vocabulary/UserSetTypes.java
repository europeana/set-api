package eu.europeana.set.definitions.model.vocabulary;


/**
 * This enumeration lists supported user set types
 * 
 * @author GrafR
 *
 */
public enum UserSetTypes implements JsonKeyword {

	COLLECTION("Collection"), ORDERED_COLLECTION_PAGE("OrderedCollectionPage");
	
	private String jsonValue;

	UserSetTypes(String jsonValue){
		this.jsonValue = jsonValue; 
	}
	
	/**
	 * Identifying user set type by the json value.
	 * For user friendliness the comparison is case insensitive  
	 * @param jsonValue
	 * @return
	 */
	public static UserSetTypes getByJsonValue(String jsonValue){
		
		String[] values = jsonValue.split(":", 2);
		//last token
		String ignoreNamespace  = values[values.length -1];
		
		for(UserSetTypes agentType : UserSetTypes.values()){
			if(agentType.getJsonValue().equalsIgnoreCase(ignoreNamespace))
				return agentType;
		}
		return null;
	}
	
	@Override
	public String getJsonValue() {
		return jsonValue;
	}
	
	@Override
	public String toString() {
		return getJsonValue();
	}

	
}
