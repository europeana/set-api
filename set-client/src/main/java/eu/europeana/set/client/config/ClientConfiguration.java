package eu.europeana.set.client.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import eu.europeana.set.client.exception.TechnicalRuntimeException;

/**
 * configuration for accessing remote api
 * 
 * @author GordeaS
 *
 */

public final class ClientConfiguration {

    protected static final String SET_CLIENT_PROPERTIES_FILE = "/set-client.properties";
    protected static final String PROP_SET_API_KEY = "set.api.key";
    protected static final String PROP_SET_SERVICE_URI = "set.service.uri";
    protected static final String PROP_OAUTH_SERVICE_URI = "oauth.service.uri";
    protected static final String PROP_OAUTH_REQUEST_PARAMS = "oauth.token.request.params";

    private Properties properties;
    private static ClientConfiguration singleton;

    /**
     * Hide the default constructor
     */
    private ClientConfiguration() {
    }

    /**
     * Accessor method for the singleton
     * 
     * @return
     */
    public static synchronized ClientConfiguration getInstance() {
	if (singleton == null) {
	    singleton = new ClientConfiguration();
	    singleton.loadProperties();
	}

	return singleton;
    }

    /**
     * Laizy loading of configuration properties
     */
    public synchronized void loadProperties() {

	try {
	    properties = new Properties();
	    InputStream resourceAsStream = getClass().getResourceAsStream(SET_CLIENT_PROPERTIES_FILE);
	    if (resourceAsStream == null) {
		throw new TechnicalRuntimeException(
			"No properties file found in classpath! " + SET_CLIENT_PROPERTIES_FILE);
	    }
	    getProperties().load(resourceAsStream);

	} catch (RuntimeException | IOException e) {
	    throw new TechnicalRuntimeException("Cannot read configuration file: " + SET_CLIENT_PROPERTIES_FILE, e);
	}

    }

    /**
     * provides access to the configuration properties. It is not recommended to use
     * the properties directly, but the
     * 
     * @return
     */
    Properties getProperties() {
	return properties;
    }

    /**
     * 
     * @return the name of the file storing the client configuration
     */
    String getConfigurationFile() {
	return SET_CLIENT_PROPERTIES_FILE;
    }

    /**
     * This method provides access to the API key defined in the configuration file
     * 
     * @see PROP_EUROPEANA_API_KEY
     * 
     * @return
     */
    public String getApiKey() {
	return getProperties().getProperty(PROP_SET_API_KEY);
    }

    /**
     * This method provides access to the search uri value defined in the
     * configuration file
     * 
     * @see PROP_EUROPEANA_SEARCH_URI
     * 
     * @return
     */
    public String getServiceUri() {
	return getProperties().getProperty(PROP_SET_SERVICE_URI);
    }

    /**
     * This method returns the uri of the oauth service as configured in
     * 
     * @return
     */
    public String getOauthServiceUri() {
	return getProperties().getProperty(PROP_OAUTH_SERVICE_URI);
    }

    /**
     * This method returns the request params needed to acquire a new token
     * 
     * @return
     */
    public String getOauthRequestParams() {
	return getProperties().getProperty(PROP_OAUTH_REQUEST_PARAMS);
    }

}
