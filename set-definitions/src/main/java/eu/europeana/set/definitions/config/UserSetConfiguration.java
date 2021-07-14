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
	public String getUserSetBaseUrl();
	
	public int getMaxPageSize(String profile);
	
	public int getMaxSearchDereferencedItems();
	
	public int getMaxRetrieveDereferencedItems();
	
	public String getSearchApiKey();

	public String getEntityUserSetUserId();

	public String getElevationFileLocation();

	public String getBestBetsFileLocation();

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
