package eu.europeana.set.client.config;

import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.BasicAuthorizationProvider;

import eu.europeana.auth.AuthenticationConfig;

/**
 * configuration for accessing remote api
 * @author GordeaS
 */

public class ClientConfiguration extends AuthenticationConfig {

    private static final Logger LOGGER = LogManager.getLogger(ClientConfiguration.class);

    protected static final String SET_CLIENT_PROPERTIES_FILE = "/set-client.user.properties";
    public static final String PROP_SET_API_KEY = "set.api.key";
    public static final String PROP_SET_SERVICE_URI = "set.service.uri";
    public static final String PROP_OAUTH_REGULAR_USER_TOKEN = "oauth.regular.user.token";
    public static final String PROP_OAUTH_SERVICE_URI = "oauth.service.uri";
    public static final String PROP_OAUTH_REQUEST_PARAMS = "oauth.token.request.params";

    /**
     * Creates ClientConfiguration instance with set client properties
     */
    public ClientConfiguration() {
        super();
        loadProperties(SET_CLIENT_PROPERTIES_FILE);
    }

    /**
     * CConstructor to inject properties
     * @param properties
     */
    public ClientConfiguration(Properties properties) {
        super(properties);
    }

    private void loadProperties(String propertiesFile) {
        try {
            load(getClass().getResourceAsStream(propertiesFile));
        }
        catch (IOException e) {
            LOGGER.error("Error loading the properties file {}", propertiesFile);
        }
    }

    public String getConfigurationFile() {
        return SET_CLIENT_PROPERTIES_FILE;
    }

    public String getApiKey() {
        return getProperty(PROP_SET_API_KEY);
    }


    public String getServiceUri() {
        return getProperty(PROP_SET_SERVICE_URI);
    }

    public String getOauthRegularUserToken() {
        return getProperty(PROP_OAUTH_REGULAR_USER_TOKEN);
    }
    
    public String getOauthServiceUri() {
        return getProperty(PROP_OAUTH_SERVICE_URI);
    }

    public String getOauthRequestParams() {
        return getProperty(PROP_OAUTH_REQUEST_PARAMS);
    }
}
