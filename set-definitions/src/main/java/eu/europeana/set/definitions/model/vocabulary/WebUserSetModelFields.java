package eu.europeana.set.definitions.model.vocabulary;

/**
 * Model attribute names
 */
public class WebUserSetModelFields {

	WebUserSetModelFields() {

	}

	//common fields constants
	public static final String ID               = "id";
	public static final String TYPE             = "type";
	public static final String TYPE_COLLECTION  = "Collection";
	public static final String SET_OPEN         = "open";
	public static final String SET_CLOSED       = "closed";

	//** user set fields **/
	public static final String AT_CONTEXT       = "@context";
	public static final String CREATOR          = "creator";
	public static final String CONTRIBUTOR      = "contributor";
	public static final String CREATED          = "created";
	public static final String MODIFIED         = "modified";
	public static final String IS_DEFINED_BY    = "isDefinedBy";
	public static final String SUBJECT          = "subject";
	public static final String ITEMS            = "items";
	public static final String TOTAL            = "total";
	public static final String VISIBILITY       = "visibility";
	public static final String IDENTIFIER       = "identifier";
	public static final String TITLE            = "title";
	public static final String PINNED           = "pinned";
	public static final String TEXT             = "text";
	

	//** creator fields **/
	public static final String NICKNAME         = "nickname";
	public static final String ENTITYUSER_NICKNAME = "entitygalleries";

	// Pinned items constants
	public static final String PINNED_POSITION = "pin";

	
	public static final String VALUE_CONTEXT_EUROPEANA_COLLECTION = "http://www.europeana.eu/schemas/context/collection.jsonld";
}
