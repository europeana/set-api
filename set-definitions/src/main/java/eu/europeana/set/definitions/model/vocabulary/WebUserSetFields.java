package eu.europeana.set.definitions.model.vocabulary;

import eu.europeana.set.definitions.model.vocabulary.fields.WebUserSetModelFields;

/**
 * @author GrafR
 * 
 */
public interface WebUserSetFields extends WebUserSetModelFields {

    // Web application
    public static final String USER_SET_PROVIDER = "userset";
    public static final String USER_SET_APPLICATION_NAME = "webuserset";
    public static final String SLASH = "/";
    public static final String PAR_CHAR = "?";
    public static final String AND = "&";
    public static final String EQUALS = "=";
    public static final String JSON_LD_REST = ".jsonld";
    public static final String FORMAT_JSONLD = "jsonld";

    //
    // Query Params
    //
//    public static final String PARAM_WSKEY = "wskey";
    public static final String PATH_PARAM_SET_ID = "identifier";
    public static final String PATH_PARAM_DATASET_ID = "datasetId";
    public static final String PATH_PARAM_LOCAL_ID = "localId";
    public static final String PATH_PARAM_POSITION = "position";

    @Deprecated
    // sort order should be included in the sort param
    public static final String PARAM_SORT_ORDER = "sortOrder";

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
    
    //results page
    public final static String PART_OF = "partOf";
    public final static String NEXT = "next";
    public final static String PREV = "prev";
    

    // Defaults

    //
    // Pagination
    //
    public final static String BASE_SET_URL = "http://data.europeana.eu/set/";
    public final static String BASE_ITEM_URL = "http://data.europeana.eu/item/";
    public final static int MAX_ITEMS_PER_PAGE = 10;
    public final static int MAX_ITEMS_TO_PRESENT = 1000;

    //
    // Serialization
    //
    public static final String SEPARATOR_SEMICOLON = ":";
    public static final String SET_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
      
    // Europeana API
    public final String BASE_URL_DATA = "http://data.europeana.eu/item";
}
