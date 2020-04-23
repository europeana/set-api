package eu.europeana.set.definitions.model.vocabulary.fields;

public interface WebUserSetModelFields {

	
	/**
	 * Model attribute names
	 */
	//** common fields **/
	public static final String ID = "id";
	public static final String TYPE = "type";
	public static final String TITLE = "title";

	
	//** user set fields **/
	public static final String AT_CONTEXT = "@context";
	public static final String CREATOR = "creator";
	public static final String CREATED = "created";
	public static final String IS_DEFINED_BY = "isDefinedBy";
	public static final String TOTAL = "total";	
	
	public static final String VALUE_CONTEXT_EUROPEANA_COLLECTION = "http://www.europeana.eu/schemas/context/collection.jsonld";
	
}
