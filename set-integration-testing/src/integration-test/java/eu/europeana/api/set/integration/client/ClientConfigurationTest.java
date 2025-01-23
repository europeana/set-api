package eu.europeana.api.set.integration.client;

import eu.europeana.set.client.config.ClientConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientConfigurationTest {

    @Test
    public void clientConfiguration_loadProperties() {
        ClientConfiguration configuration = new ClientConfiguration(loadProperties());
        assertTrue(StringUtils.isNotEmpty(configuration.getOauthRequestParams()));
        assertTrue(StringUtils.isNotEmpty(configuration.getServiceUri()));
        assertTrue(StringUtils.isNotEmpty(configuration.getOauthServiceUri()));
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        properties.put(ClientConfiguration.PROP_SET_SERVICE_URI, "service-ur-test");
        properties.put(ClientConfiguration.PROP_SET_API_KEY, "test");
        properties.put(ClientConfiguration.PROP_OAUTH_SERVICE_URI, "outh-test");
        properties.put(ClientConfiguration.PROP_OAUTH_REQUEST_PARAMS, "params-test");

        return properties;
    }
}
