package eu.europeana.api.set.integration.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import eu.europeana.api.set.integration.exception.SetIntegrationException;

public class SetIntegrationConfiguration {

    protected static final String SET_INTEGRATION_TESTING_PROPERTIES_FILE = "set-integration-testing.user.properties";
    protected static final String PROP_OAUTH_SERVICE_URI = "oauth.service.uri";
    protected static final String PROP_OAUTH_REQUEST_PARAMS_REGULAR = "oauth.token.request.params.regular";
    protected static final String PROP_OAUTH_REQUEST_PARAMS_EDITOR = "oauth.token.request.params.editor";
    protected static final String PROP_OAUTH_REQUEST_PARAMS_EDITOR2 = "oauth.token.request.params.editor2";
    protected static final String PROP_OAUTH_REQUEST_PARAMS_CREATOR_ENTITYSET = "oauth.token.request.params.creator.entityset";
    protected static final String PROP_OAUTH_REQUEST_PARAMS_PUBLISHER = "oauth.token.request.params.publisher";
    protected static final String PROP_OAUTH_REQUEST_PARAMS_ADMIN = "oauth.token.request.params.admin";
    protected static final Logger LOG = LogManager.getLogger(SetIntegrationConfiguration.class);
    public static final String CONFIG_FOLDER = "/opt/app/config/";
    

    private static Properties properties =  new Properties();
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
    public static synchronized SetIntegrationConfiguration getInstance() throws SetIntegrationException {
	if (singleton == null) {
	    singleton = new SetIntegrationConfiguration();
	    singleton.loadProperties();
	}
	return singleton;
    }

    /**
     * Laizy loading of configuration properties
     *
     * @throws SetIntegrationException if property file is not loaded
     */
    public synchronized void loadProperties() throws SetIntegrationException {
        loadProperties(SET_INTEGRATION_TESTING_PROPERTIES_FILE);
        if(getProperties().isEmpty()) {
          throw new SetIntegrationException("Cannot load properties from configuration file: " + SET_INTEGRATION_TESTING_PROPERTIES_FILE);
        }
    }
    
    
    void loadProperties(String propertiesFile) {
      File externalConfigFile = new File(CONFIG_FOLDER, propertiesFile);
      // first check if the
      if (externalConfigFile.exists()) {
        loadFromConfigFile(externalConfigFile);
      } else {
        loadFromClasspathFile(propertiesFile);
      }
    }

    void loadFromClasspathFile(String propertiesFile) {
      try {
        // try loading from classpath
        //ensure /
        String classpathPropsFile = (propertiesFile.startsWith("/") ? propertiesFile :("/" + propertiesFile) );
        getProperties().load(getClass().getResourceAsStream(classpathPropsFile));

      } catch (IOException e) {
          if (LOG.isErrorEnabled()) {
              LOG.error("Error loading the properties file from classpath: {}", propertiesFile, e);
          }
      }
    }

    void loadFromConfigFile(File externalConfigFile) {
      try (InputStream input = java.nio.file.Files.newInputStream(externalConfigFile.toPath())) {
        getProperties().load(input);
      } catch (IOException e) {
          if (LOG.isErrorEnabled()) {
              LOG.error("Error loading the properties config folder: {}", externalConfigFile.getName(), e);
          }
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
     * This method returns the request params needed to acquire a new token for regular user
     * 
     * @return
     */
    public String getOauthRequestParamsRegular() {
	return getProperties().getProperty(PROP_OAUTH_REQUEST_PARAMS_REGULAR);
    }

    /**
     * This method returns the request params needed to acquire a new token for a user with editor role
     *
     * @return
     */
    public String getOauthRequestParamsEditor() {
        return getProperties().getProperty(PROP_OAUTH_REQUEST_PARAMS_EDITOR, "");
    }
    
    /**
     * This method returns the request params needed to acquire a new token for a second user with editor role
     *
     * @return
     */
    public String getOauthRequestParamsEditor2() {
        return getProperties().getProperty(PROP_OAUTH_REQUEST_PARAMS_EDITOR2);
    }
    
    /**
     * This method returns the request params needed to acquire a new token for the user which is the ownwer of entity sets
     *
     * @return
     */
    public String getOauthRequestParamsCreatorEntitySet() {
        return getProperties().getProperty(PROP_OAUTH_REQUEST_PARAMS_CREATOR_ENTITYSET, "");
    }
    
    /**
     * This method returns the request params needed to acquire a new token for a user with publisher role
     *
     * @return
     */
    public String getOauthRequestParamsPublisher() {
        return getProperties().getProperty(PROP_OAUTH_REQUEST_PARAMS_PUBLISHER, "");
    }
    
    public String getOauthRequestParamsAdmin() {
      return getProperties().getProperty(PROP_OAUTH_REQUEST_PARAMS_ADMIN, "");
    }

}
