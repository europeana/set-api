package eu.europeana.api.set.integration.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;

import eu.europeana.api.set.integration.exception.SetIntegrationException;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import eu.europeana.set.client.exception.SetApiClientException;


/**
 * This class aims at testing of different exceptions related to set methods.
 * This is an integration test, and it is ignored for unit testing
 *
 * @author GrafR
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class UserSetClientExceptionsTest extends BaseUserSetClientTest {

  @LocalServerPort
  private int port;
    
  @BeforeAll
  void initObjects() throws SetApiClientException, SetIntegrationException {
    initObjects(port);
  }

    public String CORRUPTED_JSON =
            START +
                    "\"title\",=\"some title\"," +
                    END;

    public String CORRUPTED_UPDATE_BODY =
            "\"newfield\":=,\"some value\"";

    public String CORRUPTED_UPDATE_JSON =
            START +
                    CORRUPTED_UPDATE_BODY + "," +
                    "\"title\":" + "\"some title\"," +
                    END;

    public String WRONG_GENERATED_IDENTIFIER = "-1";

    public String UNKNOWN_WSKEY = "invalid_wskey";

    public String INVALID_USER_TOKEN = "invalid_user_token";

    public String UNKNOWN_PROVIDER = "unknown_provider";

    public String UNKNOWN_PROVIDED_IDENTIFIER = "unknown_provided_identifier";

    @Test
    public void createUserSetWithoutBody() {
        try {
            apiClient.getWebUserSetApi().createUserSet(null, null);
        } catch (SetApiClientException e) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, e.getRemoteStatusCode());
        }

    }

    @Test
    public void createUserSetWithCorruptedBody() {
        try {
            apiClient.getWebUserSetApi().createUserSet(CORRUPTED_JSON, null);
        } catch (SetApiClientException e) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, e.getRemoteStatusCode());
        }
    }

    @Test
    public void getUserSetWithWrongIdentifier() {
        try {
            apiClient.getWebUserSetApi().getUserSet(WRONG_GENERATED_IDENTIFIER, null);
        } catch (SetApiClientException e) {
            assertEquals(HttpStatus.SC_NOT_FOUND, e.getRemoteStatusCode());
        }
    }

    @Test
    public void updateUserSetWithWrongIdentifier() throws IOException {
        String requestBody = getJsonStringInput(USER_SET_CONTENT);
        try {
            apiClient.getWebUserSetApi().updateUserSet(WRONG_GENERATED_IDENTIFIER, requestBody, null);
        } catch (SetApiClientException e) {
            assertEquals(HttpStatus.SC_NOT_FOUND, e.getRemoteStatusCode());
        }
    }

}