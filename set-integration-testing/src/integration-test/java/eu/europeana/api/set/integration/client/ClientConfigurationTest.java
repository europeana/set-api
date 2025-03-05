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
        assertTrue(StringUtils.isNotEmpty(configuration.getServiceUri()));
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        properties.put(ClientConfiguration.PROP_SET_SERVICE_URI, "service-ur-test");
        return properties;
    }
}
