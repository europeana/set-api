package eu.europeana.set.client.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import eu.europeana.api.commons_sb3.auth.AuthenticationConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * configuration for accessing remote api
 * @author GordeaS
 */

public class ClientConfiguration extends AuthenticationConfig {

    private static final Logger LOGGER = LogManager.getLogger(ClientConfiguration.class);
    
    public static final String CONFIG_FOLDER = "/opt/app/config/";
    protected static final String SET_CLIENT_PROPERTIES_FILE = "set-client.user.properties";
    public static final String PROP_SET_SERVICE_URI = "set.service.uri";

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
        String classpathPropsFile = propertiesFile.startsWith("/")? propertiesFile : "/" + propertiesFile; 
        load(getClass().getResourceAsStream(classpathPropsFile));

      } catch (IOException e) {
        LOGGER.error("Error loading the properties file from classpath: {}", propertiesFile, e);
      }
    }

    void loadFromConfigFile(File externalConfigFile) {
      try (InputStream input = java.nio.file.Files.newInputStream(externalConfigFile.toPath())) {
        load(input);
      } catch (IOException e) {
        LOGGER.error("Error loading the properties config folder: {}", externalConfigFile.getName(), e);
      }
    }
    

    public String getConfigurationFile() {
        return SET_CLIENT_PROPERTIES_FILE;
    }



    public String getServiceUri() {
        return getProperty(PROP_SET_SERVICE_URI);
    }

}