package eu.europeana.set.web.http;

import eu.europeana.api.commons_sb3.definitions.http.HttpHeaders;

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
    public static final String CACHE_VALUE_NON_DYNAMIC_PUBLISHED_SET = "public, max-age=86400";
    public static final String CACHE_VALUE_DYNAMIC_SET               = "public, max-age=0";

    public static final String VALUE_LDP_CONTAINER              = "<http://www.w3.org/ns/ldp#Resource>; rel=\"type\"\n " +
            "<http://www.w3.org/TR/annotation-protocol/constraints>;\n" +
            "rel=\"http://www.w3.org/ns/ldp#constrainedBy\"";
    public static final String VALUE_LDP_CONTENT_TYPE           = HttpHeaders.CONTENT_TYPE_JSONLD_UTF8+ "; profile=\"http://www.w3.org/ns/anno.jsonld\"";
    public static final String VALUE_CONSTRAINTS                = "<http://www.w3.org/TR/annotation-protocol/constraints>; " +
            "rel=\"http://www.w3.org/ns/ldp#constrainedBy\"";
    public static final String VALUE_BASIC_CONTAINER            = "<http://www.w3.org/ns/ldp#BasicContainer>; rel=\"type\"";
    public static final String VALUE_BASIC_RESOURCE             = "<http://www.w3.org/ns/ldp#Resource>; rel=\"type\"";

    //response headers constants
    public static final String VARY               = "Vary";
    public static final String ETAG               = "ETag";
    public static final String CACHE_CONTROL      = "Cache-Control";

     // Authorization constants
    public static final String BEARER = "Bearer";

}