package eu.europeana.api.set.integration.client;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import eu.europeana.set.client.config.ClientConfiguration;

public class ClientConfigurationTest {

    @Test
    public void clientConfiguration_loadProperties() {
        ClientConfiguration configuration = new ClientConfiguration(loadProperties());
        assertTrue(StringUtils.isNotEmpty(configuration.getOauthRequestParams()));
        assertTrue(StringUtils.isNotEmpty(configuration.getOauthRegularUserToken()));
        assertTrue(StringUtils.isNotEmpty(configuration.getServiceUri()));
        assertTrue(StringUtils.isNotEmpty(configuration.getOauthServiceUri()));
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        properties.put(ClientConfiguration.PROP_SET_SERVICE_URI, "service-ur-test");
        properties.put(ClientConfiguration.PROP_SET_API_KEY, "test");
        properties.put(ClientConfiguration.PROP_OAUTH_REGULAR_USER_TOKEN, "oauth-token");
        properties.put(ClientConfiguration.PROP_OAUTH_SERVICE_URI, "outh-test");
        properties.put(ClientConfiguration.PROP_OAUTH_REQUEST_PARAMS, "params-test");

        return properties;
    }
}
