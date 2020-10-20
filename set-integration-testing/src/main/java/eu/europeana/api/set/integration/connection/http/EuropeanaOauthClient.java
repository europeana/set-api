package eu.europeana.api.set.integration.connection.http;

import java.io.IOException;

import eu.europeana.api.set.integration.exception.TechnicalRuntimeException;
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

    public EuropeanaOauthClient() {
    	//
    }

    public String getOauthToken() {
	try {
	    String accessToken = "access_token";
	    String oauthUri = SetIntegrationConfiguration.getInstance().getOauthServiceUri();
	    String oauthParams = SetIntegrationConfiguration.getInstance().getOauthRequestParams();
	    HttpConnection connection = new HttpConnection();
	    ResponseEntity<String> response;
	    response = connection.post(oauthUri, oauthParams, "application/x-www-form-urlencoded");

	    if (HttpStatus.OK.value() == response.getStatusCode().value()) {
		String body = response.getBody();
		JSONObject json = new JSONObject(body);
		if (json.has(accessToken)) {
		    return "Bearer " + json.getString(accessToken);
		} else {
		    throw new TechnicalRuntimeException("Cannot extract authentication token from reponse:" + body);
		}
	    } else {
		throw new TechnicalRuntimeException(
			"Error occured when calling oath service! " + response);
	    }
	} catch (IOException | JSONException e) {
	    throw new TechnicalRuntimeException("Cannot retrieve authentication token!", e);
	}
    }
}