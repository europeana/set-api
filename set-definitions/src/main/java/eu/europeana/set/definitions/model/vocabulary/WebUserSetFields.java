package eu.europeana.set.definitions.model.vocabulary;

/**
 * @author GrafR
 */
public class WebUserSetFields extends WebUserSetModelFields {

    WebUserSetFields() {
    }

    // Web application Constants
    public static final String USER_SET_PROVIDER          = "userset";
    public static final String USER_SET_APPLICATION_NAME  = "webuserset";
    public static final String SLASH                      = "/";
    public static final String PAR_CHAR                   = "?";
    public static final String AND                        = "&";
    public static final String EQUALS_PARAMETER           = "=";
    public static final String JSON_LD_REST               = ".jsonld";
    public static final String FORMAT_JSONLD              = "jsonld";

    // Query Params Constants
    public static final String PATH_PARAM_SET_ID          = "identifier";
    public static final String PATH_PARAM_DATASET_ID      = "datasetId";
    public static final String PATH_PARAM_LOCAL_ID        = "localId";
    public static final String PATH_PARAM_POSITION        = "position";

    /**
     * sort order should be included in the sort param
     *     
     * @deprecated    
     */
    @Deprecated(since = "")
    public static final String PARAM_SORT_ORDER           = "sortOrder";

    // Mongo Constants
    public static final String MONGO_ID                   = "_id";

    // JsonLd Constants
    public static final String CONTEXT                    = "http://www.europeana.eu/schemas/context/collection.jsonld";
    public static final String CONTEXT_FIELD              = "@context";
    public static final String IDENTIFIER                 = "identifier";
    public static final String TITLE                      = "title";
    public static final String DESCRIPTION                = "description";
    public static final String TYPE                       = "type";
    public static final String ITEMS                      = "items";

    public static final String FIRST                     = "first";
    public static final String LAST                      = "last";

    //additional search fields  
    public static final String ITEM                      = "item";
    public static final String SET_ID                    = "set_id";

    //results page
    public static final String PART_OF = "partOf";
    public static final String NEXT = "next";
    public static final String PREV = "prev";
    
    //collection page
    public static final String START_INDEX = "startIndex";
    

    // Pagination Cosntants
    public static final String BASE_SET_URL              = "http://data.europeana.eu/set/";
    public static final String BASE_ITEM_URL             = "http://data.europeana.eu/item/";
   
    // Serialization Constants
    public static final String SEPARATOR_SEMICOLON        = ":";
    public static final String SET_DATE_FORMAT            = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String DEFAULT_CREATOR_URL        = "http://data.europeana.eu/user/";

    // Europeana API Constants
//    public static final String BASE_ITEM_URL              = "http://data.europeana.eu/item";
}
