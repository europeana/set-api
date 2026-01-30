package eu.europeana.api.set.integration.exception;

import java.io.IOException;

import eu.europeana.api.commons_sb3.http.HttpConnection;
import org.junit.Assert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Enable back when real intergration tests are implemented")
public class SetIntegrationExceptionTest {

    String expectedMessage = "Error occured when calling oath service!";

    @Test
    void whenExceptionThrown_thenAssertionSucceeds() {
        Exception exception = Assert.assertThrows( SetIntegrationException.class, () -> {
           getToken();
        });

        String actualMessage = exception.getMessage();
        Assert.assertTrue(actualMessage.contains(expectedMessage));
    }


    private void getToken() throws SetIntegrationException {
        try {
            HttpConnection connection = new HttpConnection();
            connection.post("http://fake-url-for-testing.com", "oauthParams", "application/x-www-form-urlencoded", null);
        } catch (IOException e) {
            throw new SetIntegrationException(expectedMessage + e.getMessage(), e);

        }
    }
}
