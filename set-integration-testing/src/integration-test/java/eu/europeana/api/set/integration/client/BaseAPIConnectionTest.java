package eu.europeana.api.set.integration.client;

import eu.europeana.api.set.integration.IntegrationTestSetup;
import eu.europeana.set.client.connection.BaseApiConnection;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseAPIConnectionTest extends IntegrationTestSetup {
    private static final String SERVICE_URI     = "testUri";
    private static final String API_KEY_1       = "api_key";

    private BaseApiConnection baseApiConnection;

    @BeforeAll
    void setup() {
     initRegularUserToken();
     baseApiConnection = new BaseApiConnection(SERVICE_URI, API_KEY_1, regularUserToken);
    }

    @Test
    public void Test_getUserSetServiceUri() {
        StringBuilder result = baseApiConnection.getUserSetServiceUri();
        assertEquals(SERVICE_URI + WebUserSetFields.SLASH, result.toString());

        result = new StringBuilder();
        baseApiConnection = new BaseApiConnection(SERVICE_URI + WebUserSetFields.SLASH, API_KEY_1, null);
        result = baseApiConnection.getUserSetServiceUri();
        assertEquals(SERVICE_URI + WebUserSetFields.SLASH, result.toString());
    }

}
