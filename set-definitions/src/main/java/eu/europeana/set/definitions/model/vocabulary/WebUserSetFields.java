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
	public static final String PATH_PARAM_SET_ID = "identifier";
	public static final String USER_TOKEN = "userToken";
	public static final String USER_ANONYMOUNS = "anonymous";
		
	/**
	 * Mongo
	 */
	public static final String MONGO_ID = "_id";
	
    /**
     * JsonLd 
     */
    public final static String CONTEXT = "http://www.europeana.eu/schemas/context/collection.jsonld";
    public final static String CONTEXT_FIELD = "@context";
    public final static String IDENTIFIER = "identifier";
    public final static String TITLE = "title";
    public final static String DESCRIPTION = "description";
    public final static String TYPE = "type";
    public final static String ITEMS = "items";
    
    /**
     * Pagination
     */
	public final static String BASE_SET_URL = "http://data.europeana.eu/set/";
    public final static int MAX_ITEMS_PER_PAGE = 10;
    public final static String PAGE = "page";
	public final static String PAGE_SIZE = "pageSize";
	
	/**
	 * Serialization
	 */
	public static final String SEPARATOR_SEMICOLON = ":";
}
