package eu.europeana.set.definitions.config;

public interface UserSetConfiguration{

	public static final String SUFFIX_BASEURL = "baseUrl";
	
	public static final String PREFIX_MAX_PAGE_SIZE = "userset.search.maxpagesize.";
	
	public static final String USERSET_ENVIRONMENT = "set.environment";
	
	public static final String VALUE_ENVIRONMENT_PRODUCTION = "production";
	public static final String VALUE_ENVIRONMENT_TEST = "test";
	public static final String VALUE_ENVIRONMENT_DEVELOPMENT = "development";
	
	//TODO: move constants to api commons
	public static final String AUTHORIZATION_API_NAME = "authorization.api.name";
	public static final String KEY_APIKEY_JWTTOKEN_SIGNATUREKEY = "europeana.apikey.jwttoken.siganturekey";
	public static final String KEY_SEARCH_APIKEY = "europeana.search.apikey";
	public static final String KEY_SEARCH_URL = "europeana.search.url";
	public static final String API_VERSION = "set.api.version";
	
	
	public String getComponentName();
	
	/**
	 * uses set.environment property
	 */
	public String getEnvironment();
		
	/**
	 * uses annotation.environment.{$environment}.baseUrl property
	 */
	public String getUserSetBaseUrl();
	
	public int getMaxPageSize(String profile);
	
	public String getSearchApiKey();
	
	/**
	 * checks annotation.environment=production property
	 */
	public boolean isProductionEnvironment();
	
	/**
	 * authorization settings
	 */
	public String getJwtTokenSignatureKey();
	public String getAuthorizationApiName();
	public String getApiVersion();
	public String getSearchApiUrl();
}
