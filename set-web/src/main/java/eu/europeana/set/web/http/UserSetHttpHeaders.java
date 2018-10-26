package eu.europeana.set.web.http;

import eu.europeana.api.commons.web.http.HttpHeaders;

public interface UserSetHttpHeaders extends HttpHeaders {

	public static final String ALLOW_GPuDOH = "GET,PUT,DELETE,OPTIONS,HEAD";
	
	public static final String ALLOW_GPPD = "GET,POST,PUT,DELETE";
	public static final String ALLOW_GPD = "GET,PUT,DELETE";
	public static final String ALLOW_PPGHD = "POST,PUT,GET,HEAD,DELETE";
	public static final String ALLOW_PG = "POST,GET";

	public static final String VALUE_PRIVATE = "private";

	public static final String VALUE_LDP_CONTAINER = "<http://www.w3.org/ns/ldp#Resource>; rel=\"type\"\n"+
			"<http://www.w3.org/TR/annotation-protocol/constraints>;\n" +
			"rel=\"http://www.w3.org/ns/ldp#constrainedBy\"";
	public static final String VALUE_LDP_CONTENT_TYPE = CONTENT_TYPE_JSONLD_UTF8 + "; profile=\"http://www.w3.org/ns/anno.jsonld\"";
	public static final String VALUE_CONSTRAINTS = "<http://www.w3.org/TR/annotation-protocol/constraints>; " +
			"rel=\"http://www.w3.org/ns/ldp#constrainedBy\"";

//	public static final String VALUE_BASIC_CONTAINER = "<http://www.w3.org/ns/ldp#BasicContainer>; rel=\"type\"\n" + 
//			"<http://www.w3.org/ns/ldp#Resource>; rel=\"type\"";	
//	
	public static final String VALUE_BASIC_CONTAINER = "<http://www.w3.org/ns/ldp#BasicContainer>; rel=\"type\"";	
	
	public static final String VALUE_BASIC_RESOURCE = "<http://www.w3.org/ns/ldp#Resource>; rel=\"type\"";	
	
	/**
	 * Response headers
	 */
	public static final String PREFERENCE_APPLIED = "Preference-Applied";

	/**
	 * Authorization
	 */
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
