package eu.europeana.set.definitions.config;

import java.util.Properties;

import eu.europeana.set.definitions.model.vocabulary.LdProfiles;

public class UserSetConfigurationImpl implements UserSetConfiguration {

    public static final String SUFFIX_BASEURL = "baseUrl";

    public static final String PREFIX_RETRIEVE_MAX_PAGE_SIZE  = "set.retrieve.maxpagesize.";
    public static final String KEY_SEARCH_DEREFERENCE_ITEMS   = "set.search.dereference.items.max";
    public static final String KEY_RETRIEVE_DEREFERENCE_ITEMS = "set.retrieve.dereference.items.max";
    
    public static final int    DEFAULT_ITEMS_PER_PAGE        = 10;
    @Deprecated
    /**
     * use getMaxPageSize instead
     */
    public static final int    MAX_ITEMS_TO_PRESENT      = 1000;
    

    public static final String USERSET_ENVIRONMENT           = "set.environment";
    public static final String VALUE_ENVIRONMENT_PRODUCTION  = "production";
    public static final String VALUE_ENVIRONMENT_TEST        = "test";
    public static final String VALUE_ENVIRONMENT_DEVELOPMENT = "development";
    public static final String ENTITY_USERSET_USERID            = "entity.userset.user.id";
    public static final String ELEVATION_FILE_LOCATION          = "elevation.file.folder";

    // TODO: move constants to api commons
    public static final String AUTHORIZATION_API_NAME           = "authorization.api.name";
    public static final String KEY_APIKEY_JWTTOKEN_SIGNATUREKEY = "europeana.apikey.jwttoken.siganturekey";
    public static final String KEY_SEARCH_APIKEY                = "europeana.search.apikey";
    public static final String KEY_SEARCH_URL                   = "europeana.search.url";
    public static final String API_VERSION                      = "set.api.version";

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

    @Override
    public String getUserSetBaseUrl() {
	String key = USERSET_ENVIRONMENT + "." + getEnvironment() + "." + SUFFIX_BASEURL;
	return getSetProperties().getProperty(key);
    }

    @Override
    public String getEnvironment() {
	return getSetProperties().getProperty(USERSET_ENVIRONMENT);
    }

    public int getMaxPageSize(String profile) {
	//TODO enable configuration per profile when specified
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
    public boolean isProductionEnvironment() {
	return VALUE_ENVIRONMENT_PRODUCTION.equals(getEnvironment());
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
    public String getElevationFileLocation() {
        return getSetProperties().getProperty(ELEVATION_FILE_LOCATION);
    }

    @Override
    public String getSearchApiUrl() {
	return getSetProperties().getProperty(KEY_SEARCH_URL);
    }

}
