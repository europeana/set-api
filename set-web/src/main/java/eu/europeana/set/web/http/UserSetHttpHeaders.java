package eu.europeana.set.web.http;

import eu.europeana.api.commons.web.http.HttpHeaders;

public class UserSetHttpHeaders {

	private UserSetHttpHeaders() {
		//private constructor to hide implicit one
	}

	// method constants
    public static final String ALLOW_GPDOH = "GET,PUT,DELETE,OPTIONS,HEAD";
    public static final String ALLOW_GPPD  = "GET,POST,PUT,DELETE";
    public static final String ALLOW_GPD   = "GET,PUT,DELETE";
    public static final String ALLOW_PPGHD = "POST,PUT,GET,HEAD,DELETE";
    public static final String ALLOW_PG    = "POST,GET";
    public static final String ALLOW_PUT    = "PUT";
    public static final String ALLOW_PGD = "POST,GET,DELETE";
    

    //other constants
    public static final String VALUE_NO_CAHCHE_STORE_REVALIDATE = "no-cache, no-store, must-revalidate";
    public static final String VALUE_LDP_CONTAINER              = "<http://www.w3.org/ns/ldp#Resource>; rel=\"type\"\n " +
            "<http://www.w3.org/TR/annotation-protocol/constraints>;\n" +
            "rel=\"http://www.w3.org/ns/ldp#constrainedBy\"";
    public static final String VALUE_LDP_CONTENT_TYPE           = HttpHeaders.CONTENT_TYPE_JSONLD_UTF8 + "; profile=\"http://www.w3.org/ns/anno.jsonld\"";
    public static final String VALUE_CONSTRAINTS                = "<http://www.w3.org/TR/annotation-protocol/constraints>; " +
            "rel=\"http://www.w3.org/ns/ldp#constrainedBy\"";
    public static final String VALUE_BASIC_CONTAINER            = "<http://www.w3.org/ns/ldp#BasicContainer>; rel=\"type\"";
    public static final String VALUE_BASIC_RESOURCE             = "<http://www.w3.org/ns/ldp#Resource>; rel=\"type\"";

    //response headers constants
    public static final String PREFERENCE_APPLIED = "Preference-Applied";
    public static final String VARY               = "Vary";
    public static final String ETAG               = "ETag";
    public static final String CACHE_CONTROL      = "Cache-Control";

     // Authorization constants
    public static final String BEARER = "Bearer";

//	/**
//	 * CORS definitions
//	 */
//	public static final String ALLOW_ORIGIN = "Access-Control-Allow-Origin";
//	public static final String ALLOW_METHODS = "Access-Control-Allow-Methods";
//	public static final String ALLOW_HEADERS = "Access-Control-Allow-Headers";
//	public static final String EXPOSE_HEADERS = "Access-Control-Expose-Headers";
//	public static final String ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
//	public static final String MAX_AGE = "Access-Control-Max-Age";
//
//	public static final String ALL = "*";
//	public static final String MAX_AGE_VALUE = "600";

}
