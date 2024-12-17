package eu.europeana.api.set.integration.client;

import eu.europeana.set.client.config.ClientConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientConfigurationTest {

    @Test
    public void clientConfiguration_loadProperties() {
        ClientConfiguration configuration = new ClientConfiguration();
        assertTrue(StringUtils.isNotEmpty(configuration.getOauthRequestParams()));
        assertTrue(StringUtils.isNotEmpty(configuration.getServiceUri()));
        assertTrue(StringUtils.isNotEmpty(configuration.getOauthServiceUri()));
    }
}
