package eu.europeana.set.client.integration.web;

import eu.europeana.set.client.config.ClientConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("needs configuration file")
public class ClientConfigurationTest {

    @Test
    public void clientConfiguration_loadProperties() {
        assertTrue(StringUtils.isNotEmpty(ClientConfiguration.getInstance().getOauthRequestParams()));
        assertTrue(StringUtils.isNotEmpty(ClientConfiguration.getInstance().getServiceUri()));
        assertTrue(StringUtils.isNotEmpty(ClientConfiguration.getInstance().getOauthServiceUri()));
    }
}
