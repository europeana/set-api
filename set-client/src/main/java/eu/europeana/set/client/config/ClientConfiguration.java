package eu.europeana.set.client.config;

import java.io.IOException;
import java.util.Properties;

import eu.europeana.api.commons.auth.AuthenticationConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * configuration for accessing remote api
 * @author GordeaS
 */

public class ClientConfiguration extends AuthenticationConfig {

    private static final Logger LOGGER = LogManager.getLogger(ClientConfiguration.class);

    protected static final String SET_CLIENT_PROPERTIES_FILE = "/set-client.user.properties";
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



    public String getServiceUri() {
        return getProperty(PROP_SET_SERVICE_URI);
    }

}
