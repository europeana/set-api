package eu.europeana.api.set.integration.config;

import java.io.InputStream;
import java.util.Properties;

import eu.europeana.api.set.integration.exception.TechnicalRuntimeException;

public class SetIntegrationConfiguration {

    protected static final String SET_CLIENT_PROPERTIES_FILE = "/set-integration-testing.properties";
    protected static final String PROP_SET_API_KEY = "set.api.key";
    protected static final String PROP_SET_SERVICE_URI = "set.service.uri";
    protected static final String PROP_OAUTH_SERVICE_URI = "oauth.service.uri";
    protected static final String PROP_OAUTH_REQUEST_PARAMS = "oauth.token.request.params";

    private static Properties properties = null;
    private static SetIntegrationConfiguration singleton;

    /**
     * Hide the default constructor
     */
    private SetIntegrationConfiguration() {
    }

    /**
     * Accessor method for the singleton
     * 
     * @return
     */
    public static synchronized SetIntegrationConfiguration getInstance() {
	if (singleton == null) {
	    singleton = new SetIntegrationConfiguration();
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
	    if (resourceAsStream != null)
		getProperties().load(resourceAsStream);
	    else
		throw new TechnicalRuntimeException(
			"No properties file found in classpath! " + SET_CLIENT_PROPERTIES_FILE);

	} catch (Exception e) {
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
