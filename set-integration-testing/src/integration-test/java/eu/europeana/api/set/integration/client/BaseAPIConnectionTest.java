package eu.europeana.api.set.integration.client;

import eu.europeana.set.client.connection.BaseApiConnection;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseAPIConnectionTest {
    private static final String SERVICE_URI     = "testUri";
    private static final String API_KEY_1       = "api_key";

    private BaseApiConnection baseApiConnection;

    @BeforeEach
    void setup() {
     baseApiConnection = new BaseApiConnection(SERVICE_URI, API_KEY_1, null);
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
