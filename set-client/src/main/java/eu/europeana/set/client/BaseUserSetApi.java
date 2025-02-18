package eu.europeana.set.client;

import java.io.IOException;
import org.apache.hc.core5.http.HttpStatus;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import eu.europeana.set.client.config.ClientConfiguration;
import eu.europeana.set.client.connection.UserSetApiConnection;
import eu.europeana.set.client.exception.SetApiClientException;
import eu.europeana.set.common.http.HttpConnection;
import eu.europeana.set.common.http.HttpResponseHandler;

/**
 * Base class for client API
 * @author GordeaS
 *
 */

public class BaseUserSetApi {

    private final ClientConfiguration configuration;
    private UserSetApiConnection apiConnection;

    /**
     * Creates BaseUserSetApi instance with client configuration
     * This allows user to insert property file
     * @param configuration
     * @throws SetApiClientException
     */
    protected BaseUserSetApi(ClientConfiguration configuration) throws SetApiClientException {
        this.configuration = configuration;
        if (this.configuration.getServiceUri() == null) {
            throw new SetApiClientException(" Set Api Endpoint not provided !!!");
        }

        if (this.configuration.getOauthServiceUri() == null || this.configuration.getOauthRequestParams() == null) {
            throw new SetApiClientException("Oauth uri and param not provided !!!");
        }

        //get the real token in case the one is not provided
        String regularUserTokenProvided=this.configuration.getOauthRegularUserToken();
        String regularUserToken = regularUserTokenProvided!=null ? regularUserTokenProvided 
            : getOauthToken(this.configuration.getOauthServiceUri(), this.configuration.getOauthRequestParams());

        this.apiConnection = new UserSetApiConnection(
                this.configuration.getServiceUri(),
                this.configuration.getApiKey(),
                regularUserToken);
    }

    /**
     * Constructor
     * @throws SetApiClientException
     */
    public BaseUserSetApi() throws SetApiClientException {
	this(new ClientConfiguration());
    }

    private String getOauthToken(String oauthServiceUri, String oauthRequestParams ) throws SetApiClientException{
        try {
            String accessToken = "access_token";
            HttpConnection connection = new HttpConnection();

            HttpResponseHandler response = connection.post(oauthServiceUri, oauthRequestParams, "application/x-www-form-urlencoded", null);
            if (HttpStatus.SC_OK == response.getStatus()) {
                JSONObject json = new JSONObject(response.getResponse());
                if (json.has(accessToken)) {
                    return "Bearer " + json.getString(accessToken);
                } else {
                    throw new SetApiClientException("Cannot extract authentication token from reponse:" + response.getResponse());
                }
            } else {
                throw new SetApiClientException("Error occured when calling oath service! " + response);
            }
        } catch (IOException | JSONException e) {
            throw new SetApiClientException("Cannot retrieve authentication token!", 0 ,  e);
        }
    }

    public UserSetApiConnection getApiConnection() {
        return apiConnection;
    }

    public ClientConfiguration getConfiguration() {
        return configuration;
    }
}
