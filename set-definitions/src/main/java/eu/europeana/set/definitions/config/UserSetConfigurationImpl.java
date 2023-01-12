package eu.europeana.set.definitions.config;

import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;

public class UserSetConfigurationImpl implements UserSetConfiguration {


  public static final String PREFIX_RETRIEVE_MAX_PAGE_SIZE = "set.retrieve.maxpagesize.";
  public static final String KEY_SEARCH_DEREFERENCE_ITEMS = "set.search.dereference.items.max";
  public static final String KEY_RETRIEVE_DEREFERENCE_ITEMS = "set.retrieve.dereference.items.max";

  public static final int DEFAULT_ITEMS_PER_PAGE = 10;
  @Deprecated
  /**
   * use getMaxPageSize instead
   */
  public static final int MAX_ITEMS_TO_PRESENT = 1000;


  public static final String SET_API_ENDPOINT = "set.api.endpoint.baseUrl";
  public static final String SET_DATA_ENDPOINT = "set.data.endpoint.baseUrl";
  public static final String USER_DATA_ENDPOINT = "user.data.endpoint.baseUrl";
  public static final String ITEM_DATA_ENDPOINT = "item.data.endpoint.baseUrl";

  // Pagination Constants
//  public static final String BASE_ITEM_URL             = "http://data.europeana.eu/item/";


  public static final String USERSET_ENVIRONMENT = "set.environment";
  public static final String BASEURL_PREFIX = USERSET_ENVIRONMENT + ".baseUrl.";

  public static final String ENTITY_USERSET_USERID = "entity.userset.user.id";
  public static final String EUROPEANA_PUBLISHER_ID = "europeana.publisher.id";
  public static final String EUROPEANA_PUBLISHER_NICKNAME = "europeana.publisher.nickname";
  public static final String ELEVATION_FILE_LOCATION = "elevation.file.folder";

  // TODO: move constants to api commons
  public static final String AUTHORIZATION_API_NAME = "authorization.api.name";
  public static final String KEY_APIKEY_JWTTOKEN_SIGNATUREKEY =
      "europeana.apikey.jwttoken.siganturekey";
  public static final String KEY_APIKEY_SERVICE_URL = "europeana.apikey.serviceurl";  
  
  public static final String KEY_AUTH_DISABLED = "set.auth.disabled";
  public static final String KEY_SEARCH_APIKEY = "europeana.search.apikey";
  public static final String KEY_SEARCH_URL = "europeana.search.url";
  public static final String KEY_SEARCH_ITEM_DESCRIPTION_PROFILE = "europeana.search.itemdescription.profile";
  public static final String API_VERSION = "set.api.version";
  public static final String API_BASE_PATH = "set.api.basePath";
  
  
  private Properties setProperties;

  @Override
  public String getComponentName() {
    return "set";
  }

  public Properties getSetProperties() {
    return setProperties;
  }

  public void setSetProperties(Properties setProperties) {
    this.setProperties = setProperties;
  }
  
  public boolean isAuthDisabled() {
    final String property = getSetProperties().getProperty(KEY_AUTH_DISABLED);
    return Boolean.valueOf(property);
  }
  
  @Override
  public boolean isAuthEnabled() {
    return !isAuthDisabled();
  }

  @Override
  public String getUserSetBaseUrl() {
//    String key = BASEURL_PREFIX + getEnvironment();
//    return getSetProperties().getProperty(key);
    return getSetApiEndpoint();
  }

  @Override
  public String getSetApiEndpoint() {
    return getSetProperties().getProperty(SET_API_ENDPOINT);
  }
  
  @Override
  public String getSetDataEndpoint() {
    return getSetProperties().getProperty(SET_DATA_ENDPOINT);
  }
  
  @Override
  public String getUserDataEndpoint() {
    return getSetProperties().getProperty(USER_DATA_ENDPOINT);
  }
  
  @Override
  public String getItemDataEndpoint() {
    return getSetProperties().getProperty(ITEM_DATA_ENDPOINT);
  }
  
  @Override
  public String getEnvironment() {
    return getSetProperties().getProperty(USERSET_ENVIRONMENT);
  }

  public int getMaxPageSize(String profile) {
    // TODO enable configuration per profile when specified
    String key = PREFIX_RETRIEVE_MAX_PAGE_SIZE + LdProfiles.STANDARD.name().toLowerCase();
    return Integer.parseInt(getSetProperties().getProperty(key));
  }

  public int getMaxSearchDereferencedItems() {
    return Integer.parseInt(getSetProperties().getProperty(KEY_SEARCH_DEREFERENCE_ITEMS));
  }

  public int getMaxRetrieveDereferencedItems() {
    return Integer.parseInt(getSetProperties().getProperty(KEY_RETRIEVE_DEREFERENCE_ITEMS));
  }


  @Override
  public String getJwtTokenSignatureKey() {
    return getSetProperties().getProperty(KEY_APIKEY_JWTTOKEN_SIGNATUREKEY);
  }

  public String getAuthorizationApiName() {
    return getSetProperties().getProperty(AUTHORIZATION_API_NAME);
  }

  @Override
  public String getApiVersion() {
    return getSetProperties().getProperty(API_VERSION);
  }

  @Override
  public String getSearchApiKey() {
    return getSetProperties().getProperty(KEY_SEARCH_APIKEY);
  }

  @Override
  public String getEntityUserSetUserId() {
    return getSetProperties().getProperty(ENTITY_USERSET_USERID);
  }

  @Override
  @Deprecated
  /** @deprecated not used amynore, to be removed in the future versions 
  */
  public String getElevationFileLocation() {
    return getSetProperties().getProperty(ELEVATION_FILE_LOCATION);
  }

  @Override
  public String getSearchApiUrl() {
    return getSetProperties().getProperty(KEY_SEARCH_URL);
  }
  
  @Override
  public String getSearchApiProfileForItemDescriptions() {
    return getSetProperties().getProperty(KEY_SEARCH_ITEM_DESCRIPTION_PROFILE);
  }

  @Override
  public String getApiBasePath() {
    if(getSetProperties().containsKey(API_BASE_PATH)) {
      return getSetProperties().getProperty(API_BASE_PATH);
    } else {
      return "/set/";  
    }
  }
  
  @Override
  public boolean isApiKeyValidationEnabled() {
    return getSetProperties().containsKey(KEY_APIKEY_SERVICE_URL) 
        && StringUtils.isNotBlank(getSetProperties().getProperty(KEY_APIKEY_SERVICE_URL));
  }

  @Override
  public String getEuropeanaPublisherId() {
    return getSetProperties().getProperty(EUROPEANA_PUBLISHER_ID);
  }

  @Override
  public String getEuropeanaPublisherNickname() {
    return getSetProperties().getProperty(EUROPEANA_PUBLISHER_NICKNAME);
  }
}
