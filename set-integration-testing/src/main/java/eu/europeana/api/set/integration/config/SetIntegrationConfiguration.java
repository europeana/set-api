package eu.europeana.api.set.integration.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import eu.europeana.api.set.integration.exception.TechnicalRuntimeException;

public class SetIntegrationConfiguration {

    protected static final String SET_INTEGRATION_TESTING_PROPERTIES_FILE = "/set-integration-testing.properties";
    protected static final String PROP_OAUTH_SERVICE_URI = "oauth.service.uri";
    protected static final String PROP_OAUTH_REQUEST_PARAMS_REGULAR = "oauth.token.request.params.regular";
    protected static final String PROP_OAUTH_REQUEST_PARAMS_EDITOR = "oauth.token.request.params.editor";
    protected static final String PROP_OAUTH_REQUEST_PARAMS_EDITOR2 = "oauth.token.request.params.editor2";


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
	    InputStream resourceAsStream = getClass().getResourceAsStream(SET_INTEGRATION_TESTING_PROPERTIES_FILE);
	    if (resourceAsStream == null) {
		throw new TechnicalRuntimeException(
			"No properties file found in classpath! " + SET_INTEGRATION_TESTING_PROPERTIES_FILE);
	    }
	    
	    getProperties().load(resourceAsStream);
	} catch (IOException e) {
	    throw new TechnicalRuntimeException("Cannot read configuration file: " + SET_INTEGRATION_TESTING_PROPERTIES_FILE, e);
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
	return SET_INTEGRATION_TESTING_PROPERTIES_FILE;
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
    public String getOauthRequestParamsRegular() {
	return getProperties().getProperty(PROP_OAUTH_REQUEST_PARAMS_REGULAR);
    }

    /**
     * This method returns the request params needed to acquire a new token
     *
     * @return
     */
    public String getOauthRequestParamsEditor() {
        return getProperties().getProperty(PROP_OAUTH_REQUEST_PARAMS_EDITOR);
    }
    
    /**
     * This method returns the request params needed to acquire a new token
     *
     * @return
     */
    public String getOauthRequestParamsEditor2() {
        return getProperties().getProperty(PROP_OAUTH_REQUEST_PARAMS_EDITOR2);
    }
}
