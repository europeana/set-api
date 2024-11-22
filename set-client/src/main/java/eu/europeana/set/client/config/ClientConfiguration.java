package eu.europeana.set.client.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import eu.europeana.set.client.exception.TechnicalRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * configuration for accessing remote api
 * @author GordeaS
 */

public final class ClientConfiguration {

    private static final Logger LOGGER = LogManager.getLogger(ClientConfiguration.class);

    protected static final String SET_CLIENT_PROPERTIES_FILE = "/set-client.user.properties";
    protected static final String PROP_SET_API_KEY = "set.api.key";
    protected static final String PROP_SET_SERVICE_URI = "set.service.uri";
    protected static final String PROP_OAUTH_SERVICE_URI = "oauth.service.uri";
    protected static final String PROP_OAUTH_REQUEST_PARAMS = "oauth.token.request.params";

    private Properties properties;

    public ClientConfiguration() {
        loadProperties(SET_CLIENT_PROPERTIES_FILE);
    }

    public ClientConfiguration(Properties properties) {
        this.properties = properties;
    }

    private Properties loadProperties(String propertiesFile) {
        try { properties = new Properties();
            properties.load(getClass().getResourceAsStream(propertiesFile));
        } catch (IOException e) {
            LOGGER.error("Error loading the properties file {}", propertiesFile);
        }
        return properties;
    }

    String getConfigurationFile() {
	return SET_CLIENT_PROPERTIES_FILE;
    }

    public String getApiKey() {
	return getProperty(PROP_SET_API_KEY);
    }


    public String getServiceUri() {
	return getProperty(PROP_SET_SERVICE_URI);
    }

    public String getOauthServiceUri() {
	return getProperty(PROP_OAUTH_SERVICE_URI);
    }

    public String getOauthRequestParams() {
	return getProperty(PROP_OAUTH_REQUEST_PARAMS);
    }

    private String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }

}
