package eu.europeana.api.set.integration.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Properties;

import eu.europeana.api.commons_sb3.auth.service.GrantConstants;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import eu.europeana.set.client.config.ClientConfiguration;

public class ClientConfigurationTest {

    @Test
    public void clientConfiguration_loadProperties() {
        ClientConfiguration configuration = new ClientConfiguration(loadProperties());
        assertTrue(StringUtils.isNotEmpty(configuration.getAccessToken()));
        assertTrue(StringUtils.isNotEmpty(configuration.getApiKey()));
        assertTrue(StringUtils.isNotEmpty(configuration.getServiceUri()));
        assertTrue(StringUtils.isNotEmpty(configuration.getAuthTokenEndpoitUri()));
        assertNotNull(configuration.getAuthGrant());

    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        properties.put(ClientConfiguration.PROP_SET_SERVICE_URI, "http://localhost/set");
        properties.put(ClientConfiguration.CONFIG_APIKEY, "test");
        properties.put(GrantConstants.access_token, "token_test");
        properties.put(ClientConfiguration.CONFIG_TOKEN_ENDPOINT, "token_endpoint");
        properties.put(ClientConfiguration.CONFIG_GRANT_PARAMS, "token_params");

        return properties;
    }
}