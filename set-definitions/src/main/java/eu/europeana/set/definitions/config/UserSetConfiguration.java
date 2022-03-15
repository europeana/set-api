package eu.europeana.set.definitions.config;

public interface UserSetConfiguration{

	public String getComponentName();
	
	/**
	 * uses set.environment property
	 */
	public String getEnvironment();
		
	/**
	 * uses annotation.environment.{$environment}.baseUrl property
	 */
	@Deprecated
	public String getUserSetBaseUrl();
	
	public int getMaxPageSize(String profile);
	
	public int getMaxSearchDereferencedItems();
	
	public int getMaxRetrieveDereferencedItems();
	
	public String getSearchApiKey();

	public String getEntityUserSetUserId();

	public String getElevationFileLocation();
	
	/**
	 * authorization settings
	 */
	public String getJwtTokenSignatureKey();
	public String getAuthorizationApiName();
	public String getApiVersion();
	public String getSearchApiUrl();

  String getUserDataEndpoint();

  String getSetDataEndpoint();

  String getSetApiEndpoint();

  String getItemDataEndpoint();

  String getApiBasePath();

  boolean isApiKeyValidationEnabled();
}
