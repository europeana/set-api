package eu.europeana.set.client;

import eu.europeana.set.client.config.ClientConfiguration;
import eu.europeana.set.client.connection.UserSetApiConnection;
import eu.europeana.set.client.exception.SetApiClientException;
import eu.europeana.set.client.exception.TechnicalRuntimeException;
import eu.europeana.set.common.http.HttpConnection;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

/**
 * Base class for client API
 * @author GordeaS
 *
 */

public class BaseUserSetApi {

    private final ClientConfiguration configuration;
    private UserSetApiConnection apiConnection;

    protected BaseUserSetApi(ClientConfiguration configuration) throws SetApiClientException {
        this.configuration = configuration;
        if (this.configuration.getServiceUri() == null && this.configuration.getApiKey() == null) {
            throw new SetApiClientException(" Set Api Endpoint not provide !!!");
        }

        if (this.configuration.getOauthServiceUri() == null || this.configuration.getOauthRequestParams() == null) {
            throw new SetApiClientException("Oauth uri and param not provided !!!");
        }

        this.apiConnection = new UserSetApiConnection(
                this.configuration.getServiceUri(),
                this.configuration.getApiKey(),
                getOauthToken(this.configuration.getOauthServiceUri(), this.configuration.getOauthRequestParams()));
    }

    public BaseUserSetApi() throws SetApiClientException {
	this(new ClientConfiguration());
    }

    private String getOauthToken(String oauthServiceUri, String oauthRequestParams ) {
        try {
            String ACCESS_TOKEN = "access_token";
            HttpConnection connection = new HttpConnection();
            ResponseEntity<String> response;
            response = connection.post(oauthServiceUri, oauthRequestParams, "application/x-www-form-urlencoded");

            if (HttpStatus.OK == response.getStatusCode()) {
                String body = response.getBody();
                JSONObject json = new JSONObject(body);
                if (json.has(ACCESS_TOKEN)) {
                    return "Bearer " + json.getString(ACCESS_TOKEN);
                } else {
                    throw new TechnicalRuntimeException(
                            "Cannot extract authentication token from reponse:" + body);
                }
            } else {
                throw new TechnicalRuntimeException("Error occured when calling oath service! " + response);
            }
        } catch (IOException | JSONException e) {
            throw new TechnicalRuntimeException("Cannot retrieve authentication token!", e);
        }
    }

    public UserSetApiConnection getApiConnection() {
        return apiConnection;
    }

    public ClientConfiguration getConfiguration() {
        return configuration;
    }
}
