package eu.europeana.api.set.integration.connection.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import eu.europeana.api.commons_sb3.http.HttpConnection;
import eu.europeana.api.set.integration.config.SetIntegrationConfiguration;
import eu.europeana.api.set.integration.exception.SetIntegrationException;

/**
 * @author GrafR Currently only used in test classes
 */
public class EuropeanaOauthClient {

  public static final String HEADER_AUTHORIZATION = "Authorization";
  public static final String REGULAR_USER = "REGULAR";
  public static final String EDITOR_USER = "EDITOR";
  public static final String EDITOR2_USER = "EDITOR2";
  public static final String CREATOR_ENTITYSETS = "CREATOR_ES";
  public static final String PUBLISHER_USER = "PUBLISHER";
  public static final String ADMIN_USER = "ADMIN";

  public EuropeanaOauthClient() {
    //
  }

  public String getOauthToken(String user) throws SetIntegrationException {
    String accessToken = "access_token";
    String oauthUri = SetIntegrationConfiguration.getInstance().getOauthServiceUri();
    String oauthParams = null;
    switch (user) {
      case REGULAR_USER:
        oauthParams = SetIntegrationConfiguration.getInstance().getOauthRequestParamsRegular();
        break;
      case EDITOR_USER:
        oauthParams = SetIntegrationConfiguration.getInstance().getOauthRequestParamsEditor();
        break;
      case EDITOR2_USER:
        oauthParams = SetIntegrationConfiguration.getInstance().getOauthRequestParamsEditor2();
        break;
      case CREATOR_ENTITYSETS:
        oauthParams =
            SetIntegrationConfiguration.getInstance().getOauthRequestParamsCreatorEntitySet();
        break;
      case PUBLISHER_USER:
        oauthParams = SetIntegrationConfiguration.getInstance().getOauthRequestParamsPublisher();
        break;
      case ADMIN_USER:
        oauthParams = SetIntegrationConfiguration.getInstance().getOauthRequestParamsAdmin();
        break;
    }

    HttpConnection connection = new HttpConnection();
    try (CloseableHttpResponse response = connection.post(oauthUri, oauthParams,
        Map.of(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded"), null)) {

      if (HttpStatus.SC_OK == response.getCode()) {
        InputStream content = response.getEntity().getContent();
        String body = new String(content.readAllBytes(), StandardCharsets.UTF_8);
        JSONObject json = new JSONObject(body);
        if (json.has(accessToken)) {
          return "Bearer " + json.getString(accessToken);
        } else {
          throw new SetIntegrationException(
              "Cannot extract authentication token from reponse:" + body);
        }
      } else {
        throw new SetIntegrationException("Error occured when calling oath service! " + response);
      }
    } catch (IOException | JSONException e) {
      throw new SetIntegrationException("Cannot retrieve authentication token!", e);
    }
  }
}
