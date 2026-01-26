package eu.europeana.api.set.integration.client;

import eu.europeana.api.commons_sb3.auth.apikey.ApikeyBasedAuthentication;
import eu.europeana.set.client.connection.BaseApiConnection;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientAPIConnectionTest {
    private static final String SERVICE_URI     = "testUri";
    private static final String API_KEY_1       = "api_key";

    private BaseApiConnection baseApiConnection;

    @BeforeEach
    void setup() {
     baseApiConnection = new BaseApiConnection(SERVICE_URI, new ApikeyBasedAuthentication("test"));
    }

    @Test
    public void Test_getUserSetServiceUri() {
        StringBuilder result = baseApiConnection.getUserSetServiceUri();
        assertEquals(SERVICE_URI + WebUserSetFields.SLASH, result.toString());
    }

}