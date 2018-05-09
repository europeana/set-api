package eu.europeana.set.definitions.model.vocabulary;

import eu.europeana.set.definitions.model.vocabulary.fields.WebUserSetModelFields;

/**
 * @author GrafR
 * 
 */
public interface WebUserSetFields extends WebUserSetModelFields{

	public static final String USER_SET_PROVIDER = "userset";

	/**
	 * Query Params
	 */
	public static final String PARAM_WSKEY = "wskey";

	/**
	 * Mongo
	 */
	public static final String MONGO_ID = "_id";
}
