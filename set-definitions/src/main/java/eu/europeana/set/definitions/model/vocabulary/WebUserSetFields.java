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
    public static final String COMMA                      = ",";
    public static final String SPACE                      = " ";
    public static final String EQUALS_PARAMETER           = "=";
    public static final String JSON_LD_REST               = ".jsonld";
    public static final String FORMAT_JSONLD              = "jsonld";

    // Query Params Constants
    public static final String PATH_PARAM_SET_ID          = "identifier";
    public static final String PATH_PARAM_DATASET_ID      = "datasetId";
    public static final String PATH_PARAM_CREATOR_ID      = "creator";
    public static final String PATH_PARAM_LOCAL_ID        = "localId";
    public static final String PATH_PARAM_POSITION        = "position";
    public static final String REQUEST_PARAM_ISSUED       = "issued";

    /**
     * sort order should be included in the sort param
     *     
     * @deprecated    
     */
    @Deprecated(since = "")
    public static final String PARAM_SORT_ORDER           = "sortOrder";
    public static final String SORT_ORDER_DESC = "desc";
    public static final String SORT_ORDER_ASC = "asc";

    public static final String TEXT_SCORE_SORT            = "score";

    // JsonLd Constants
    public static final String CONTEXT                    = "https://api.europeana.eu/schema/context/set.jsonld";
    public static final String CONTEXT_FIELD              = "@context";
    public static final String IDENTIFIER                 = "identifier";
    public static final String TITLE                      = "title";
    public static final String DESCRIPTION                = "description";
    public static final String TYPE                       = "type";
    public static final String ITEMS                      = "items";
    public static final String LANG                      = "lang";

    public static final String FIRST                     = "first";
    public static final String LAST                      = "last";

    //additional search fields  
    public static final String ITEM                      = "item";
    public static final String SET_ID                    = "set_id";

    //results page
    public static final String PART_OF                   = "partOf";
    public static final String NEXT                      = "next";
    public static final String PREV                      = "prev";
    public static final String FACETS                    = "facets";
    public static final String FIELD                     = "field";
    public static final String VALUES                    = "values";
    public static final String FACET_TYPE 		= "facet";
    public static final String LABEL			= "label";
    public static final String COUNT 			= "count";

    //collection page
    public static final String START_INDEX = "startIndex";
   
    // Serialization Constants
    public static final String SEPARATOR_SEMICOLON        = ":";

    // Entity user set and Elevation Constants
    public static final String ELEVATION_FILENAME        = "elevate.xml";
    public static final String ELEVATION_AGENT_QUERY     = "edm_agent:";
    public static final String ELEVATION_CONCEPT_QUERY   = "skos_concept:";
    public static final String ELEVATION_TIMESPAN_QUERY  = "edm_timespan:";
    public static final String ELEVATION_ORGANIZATION_QUERY = "foaf_organization:";
    public static final String ORGANIZATION              = "organization";
    public static final String AGENT                     = "agent";
    public static final String CONCEPT                   = "concept";
    public static final String TIMESPAN                  = "timespan";
    public static final String DATA_EUROPEANA_BASE_URL      = "http://data.europeana.eu/";
    public static final String PROJECT_EUROPEANA_BASE_URL      = "https://pro.europeana.eu/project/";
    public static final String ENTITY_URI_BASE      = "/base";

}
