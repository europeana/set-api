package eu.europeana.set.definitions.config;

public interface UserSetConfiguration {

  public static final String BEAN_SET_PERSITENCE_SERVICE = "set_db_setService";

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

  public String getSearchApiProfileForItemDescriptions();

  public String getEntityUserSetUserId();

  public String getEuropeanaPublisherId();

  public String getEuropeanaPublisherNickname();

  public String getElevationFileLocation();

  /**
   * authorization settings
   */
  public String getJwtTokenSignatureKey();

  public String getAuthorizationApiName();

  public String getSearchApiUrl();

  String getUserDataEndpoint();

  String getSetDataEndpoint();

  String getSetApiEndpoint();

  String getItemDataEndpoint();

  String getApiBasePath();

  boolean isApiKeyValidationEnabled();

  boolean isAuthEnabled();
  
  int getMaxItems();
}
