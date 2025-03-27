package eu.europeana.api.set.integration.exception;

import eu.europeana.api.set.integration.BaseUserSetTestUtils;
import eu.europeana.api.set.integration.config.SetIntegrationConfiguration;
import eu.europeana.api.set.integration.connection.http.EuropeanaOauthClient;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class SetIntegrationExceptionTest {

    String expectedMessage = "Error occured when calling oath service!";

    @Test
    void whenExceptionThrown_thenAssertionSucceeds() {
        Exception exception = Assert.assertThrows( SetIntegrationException.class, () -> {
            BaseUserSetTestUtils.retrieveOauthToken("invalid");
        });

        String actualMessage = exception.getMessage();
        Assert.assertTrue(actualMessage.contains(expectedMessage));
    }
}
