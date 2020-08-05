package eu.europeana.set.definitions.config;

public interface UserSetConfiguration {

    String SUFFIX_BASEURL                = "baseUrl";
    String PREFIX_MAX_PAGE_SIZE          = "userset.search.maxpagesize.";
    String USERSET_ENVIRONMENT           = "set.environment";
    String VALUE_ENVIRONMENT_PRODUCTION  = "production";
    String VALUE_ENVIRONMENT_TEST        = "test";
    String VALUE_ENVIRONMENT_DEVELOPMENT = "development";

    //TODO: move constants to api commons
    String AUTHORIZATION_API_NAME           = "authorization.api.name";
    String KEY_APIKEY_JWTTOKEN_SIGNATUREKEY = "europeana.apikey.jwttoken.siganturekey";
    String KEY_SEARCH_APIKEY                = "europeana.search.apikey";
    String API_VERSION                      = "set.api.version";


    String getComponentName();

    /**
     * uses set.environment property
     */
    String getEnvironment();

    /**
     * uses annotation.environment.{$environment}.baseUrl property
     */
    String getUserSetBaseUrl();

    int getMaxPageSize(String profile);

    String getSearchApiKey();

    /**
     * checks annotation.environment=production property
     */
    boolean isProductionEnvironment();

    /**
     * authorization settings
     */
    String getJwtTokenSignatureKey();

    String getAuthorizationApiName();

    String getApiVersion();
}
