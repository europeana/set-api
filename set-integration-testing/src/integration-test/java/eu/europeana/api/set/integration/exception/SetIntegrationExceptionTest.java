package eu.europeana.api.set.integration.exception;

import eu.europeana.set.common.http.HttpConnection;
import eu.europeana.set.common.http.HttpResponseHandler;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;

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
            HttpResponseHandler response = connection.post("http://test.com", "oauthParams", "application/x-www-form-urlencoded", null);
        } catch (IOException e) {
            throw new SetIntegrationException(expectedMessage + e.getMessage(), e);

        }
    }
}
