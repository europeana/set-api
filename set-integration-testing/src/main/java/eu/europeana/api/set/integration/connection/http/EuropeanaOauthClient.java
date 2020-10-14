package eu.europeana.api.set.integration.connection.http;

import java.io.IOException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import eu.europeana.api.set.integration.config.SetIntegrationConfiguration;
import eu.europeana.set.common.http.HttpConnection;

/**
 * @author GrafR
 */
public class EuropeanaOauthClient{

    public static final String HEADER_AUTHORIZATION = "Authorization";
    String regularUserAuthorizationValue = null;

    public EuropeanaOauthClient() {
    }

    public String getOauthToken() {
	try {
	    String ACCESS_TOKEN = "access_token";
	    String oauthUri = SetIntegrationConfiguration.getInstance().getOauthServiceUri();
	    String oauthParams = SetIntegrationConfiguration.getInstance().getOauthRequestParams();
	    HttpConnection connection = new HttpConnection();
	    ResponseEntity<String> response;
	    response = connection.post(oauthUri, oauthParams, "application/x-www-form-urlencoded");

	    if (HttpStatus.OK.equals(response.getStatusCode())) {
		String body = response.getBody();
		JSONObject json = new JSONObject(body);
		if (json.has(ACCESS_TOKEN)) {
		    return "Bearer " + json.getString(ACCESS_TOKEN);
		} else {
		    throw new RuntimeException("Cannot extract authentication token from reponse:" + body);
		}
	    } else {
		throw new RuntimeException(
			"Error occured when calling oath service! " + response);
	    }
	} catch (IOException | JSONException e) {
	    throw new RuntimeException("Cannot retrieve authentication token!", e);
	}
    }
}