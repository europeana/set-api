package eu.europeana.api.set.integration.exception;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.util.Map;
import org.apache.hc.core5.http.HttpHeaders;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import eu.europeana.api.commons_sb3.http.HttpConnection;

@Disabled("Enable back when real intergration tests are implemented")
public class SetIntegrationExceptionTest {

    String expectedMessage = "Error occured when calling oath service!";

    @Test
    void whenExceptionThrown_thenAssertionSucceeds() {
        Exception exception = assertThrows(SetIntegrationException.class, () -> {
           getToken();
        });

        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }


    private void getToken() throws SetIntegrationException {
        try {
            HttpConnection connection = new HttpConnection();
            connection.post("http://fake-url-for-testing.com", 
                "oauthParams", 
                Map.of(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded"), null);
        } catch (IOException e) {
            throw new SetIntegrationException(expectedMessage + e.getMessage(), e);

        }
    }
}
