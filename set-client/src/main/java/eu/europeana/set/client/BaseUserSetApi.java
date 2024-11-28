package eu.europeana.set.client;

import eu.europeana.set.client.config.ClientConfiguration;
import eu.europeana.set.client.connection.UserSetApiConnection;
import eu.europeana.set.client.exception.SetApiClientException;
import eu.europeana.set.common.http.HttpConnection;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

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
        if (this.configuration.getServiceUri() == null) {
            throw new SetApiClientException(" Set Api Endpoint not provided !!!");
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

    private String getOauthToken(String oauthServiceUri, String oauthRequestParams ) throws SetApiClientException{
        try {
            String ACCESS_TOKEN = "access_token";
            HttpConnection connection = new HttpConnection();

            CloseableHttpResponse response = connection.post(oauthServiceUri, oauthRequestParams, "application/x-www-form-urlencoded", null);
            String body = EntityUtils.toString(response.getEntity());

            System.out.println(body);
            if (HttpStatus.SC_OK == response.getCode()) {
                JSONObject json = new JSONObject(body);
                if (json.has(ACCESS_TOKEN)) {
                    return "Bearer " + json.getString(ACCESS_TOKEN);
                } else {
                    throw new SetApiClientException("Cannot extract authentication token from reponse:" + body);
                }
            } else {
                throw new SetApiClientException("Error occured when calling oath service! " + response);
            }
        } catch (IOException | JSONException | ParseException e) {
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
