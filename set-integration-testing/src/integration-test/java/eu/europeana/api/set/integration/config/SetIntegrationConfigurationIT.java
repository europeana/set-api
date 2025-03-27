package eu.europeana.api.set.integration.config;

import eu.europeana.api.set.integration.exception.SetIntegrationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SetIntegrationConfigurationIT {

    private SetIntegrationConfiguration configuration ;

    @BeforeAll
    void setup() throws SetIntegrationException {
        configuration = SetIntegrationConfiguration.getInstance();

    }
    @Test
    public void configurationTest() throws SetIntegrationException {
        Assertions.assertNotNull(configuration);
        Assertions.assertNotNull(configuration.getConfigurationFile());
        Assertions.assertNotNull(configuration.getOauthServiceUri());
        Assertions.assertNotNull(configuration.getOauthRequestParamsRegular());
        Assertions.assertNotNull(configuration.getOauthRequestParamsEditor());
        Assertions.assertNotNull(configuration.getOauthRequestParamsEditor2());
        Assertions.assertNotNull(configuration.getOauthRequestParamsCreatorEntitySet());
        Assertions.assertNotNull(configuration.getOauthRequestParamsPublisher());
        Assertions.assertEquals("", configuration.getOauthRequestParamsAdmin());


    }
}
