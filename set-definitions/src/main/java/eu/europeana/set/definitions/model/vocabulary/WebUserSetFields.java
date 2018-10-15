package eu.europeana.set.definitions.model.vocabulary;

import eu.europeana.set.definitions.model.vocabulary.fields.WebUserSetModelFields;

/**
 * @author GrafR
 * 
 */
public interface WebUserSetFields extends WebUserSetModelFields{

	// Web application
	public static final String USER_SET_PROVIDER = "userset";
	public static final String USER_SET_APPLICATION_NAME = "webuserset";
	public static final String SLASH = "/";
	public static final String PAR_CHAR = "?";
	public static final String AND = "&";
	public static final String EQUALS = "=";
	public static final String JSON_LD_REST = ".jsonld";
	
	//
	// Query Params
	//
	public static final String PARAM_WSKEY = "wskey";
	public static final String PATH_PARAM_SET_ID = "identifier";
	public static final String PATH_PARAM_DATASET_ID = "datasetId";
	public static final String PATH_PARAM_LOCAL_ID = "localId";
	public static final String PATH_PARAM_POSITION = "position";
	public static final String USER_TOKEN = "userToken";
	public static final String PROFILE = "profile";
	
	//
	// Mongo
	//
	public static final String MONGO_ID = "_id";
	
    //
    // JsonLd 
    //
    public final static String CONTEXT = "http://www.europeana.eu/schemas/context/collection.jsonld";
    public final static String CONTEXT_FIELD = "@context";
    public final static String IDENTIFIER = "identifier";
    public final static String TITLE = "title";
    public final static String DESCRIPTION = "description";
    public final static String TYPE = "type";
    public final static String ITEMS = "items";
    
    // Defaults
	public static final String USER_ANONYMOUNS = "anonymous";
	public static final String PROFILE_MINIMAL = "minimal";
    
    //
    // Pagination
    //
	public final static String BASE_SET_URL = "http://data.europeana.eu/set/";
	public final static String BASE_ITEM_URL = "http://data.europeana.eu/item/";
    public final static int MAX_ITEMS_PER_PAGE = 10;
    public final static String PAGE = "page";
	public final static String PAGE_SIZE = "pageSize";
    public final static int MAX_ITEMS_TO_PRESENT = 1000;
	
	//
	// Serialization
	//
	public static final String SEPARATOR_SEMICOLON = ":";
	public static final String SET_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	
	// Internal fields 
	public static final String INTERNAL_TYPE = "internalType";	
	
	// Agent fields 
	public static final String NAME = "name";	
	
	// Authentication
	public static final String USER_ADMIN = "admin";	
	public static final String PROVIDER_EUROPEANA_DEV = "eanadev";	
	
	//
	// Validation definitions
	//
	public static final String READ_METHOD = "read";
	public static final String WRITE_METHOD = "write";
	public static final String DELETE_METHOD = "delete";	
}
