package eu.europeana.set.client.connection;

import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.set.client.config.ClientConfiguration;
import eu.europeana.set.client.exception.TechnicalRuntimeException;
import eu.europeana.set.common.http.HttpConnection;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;

/**
 * @author GrafR
 */
public class UserSetApiConnection extends BaseApiConnection {

  public UserSetApiConnection(String setServiceUri, String apiKey, String regularUserAuthorizationValue) {
   super(setServiceUri, apiKey, regularUserAuthorizationValue);
  }


  /**
   * This method creates UserSet object from Json string. Example HTTP request for tag object:
   * http://localhost:8080/set/?profile=minimal
   *
   * @param set The UserSet body
   * @param profile
   * @return response entity that comprises response body, headers and status code.
   * @throws IOException
   */
  public ResponseEntity<String> createUserSet(String set, String profile) throws IOException {

    StringBuilder urlBuilder = getUserSetServiceUri();
    if (StringUtils.isNotEmpty(profile)) {
      urlBuilder.append(WebUserSetFields.PAR_CHAR);
      urlBuilder.append(CommonApiConstants.QUERY_PARAM_PROFILE)
          .append(WebUserSetFields.EQUALS_PARAMETER).append(profile);
    }

    String resUrl = urlBuilder.toString();

    logger.trace("Ivoking create set: {} ", resUrl);

    /**
     * Execute Europeana API request
     */
    return postURL(resUrl, set, regularUserAuthorizationValue);
  }

  /**
   * This method retrieves UserSet object. Example HTTP request for tag object:
   * http://localhost:8080/set/{identifier}.jsonld?profile=minimal where identifier is: 496
   *
   * @param identifier
   * @param profile
   * @return response entity that comprises response body, headers and status code.
   * @throws IOException
   */
  public ResponseEntity<String> getUserSet(String identifier, String profile) throws IOException {

    StringBuilder urlBuilder = getUserSetServiceUri();
    urlBuilder.append(identifier).append(WebUserSetFields.JSON_LD_REST);
    if (StringUtils.isNotEmpty(profile)) {
      urlBuilder.append(WebUserSetFields.PAR_CHAR);
      urlBuilder.append(CommonApiConstants.QUERY_PARAM_PROFILE)
          .append(WebUserSetFields.EQUALS_PARAMETER).append(profile);
    }

    /**
     * Execute Europeana API request
     */
    return getURL(urlBuilder.toString(), regularUserAuthorizationValue);
  }

  /**
   * This method updates UserSet object by the passed Json update string. Example HTTP request:
   * http://localhost:8080/set/{identifier}.jsonld?profile=standard where identifier is: 496 and the
   * update JSON string is: { "title": {"en":"Sport"},"description": {"en":"Best sport"} }
   *
   * @param identifier The identifier that comprise set ID
   * @param updateUserSet The update UserSet body in JSON format
   * @param profile
   * @return response entity that comprises response body, headers and status code.
   * @throws IOException
   */
  public ResponseEntity<String> updateUserSet(String identifier, String updateUserSet,
      String profile) throws IOException {

    StringBuilder urlBuilder = getUserSetServiceUri();
    urlBuilder.append(identifier).append(WebUserSetFields.JSON_LD_REST);
    if (StringUtils.isNotEmpty(profile)) {
      urlBuilder.append(WebUserSetFields.PAR_CHAR);
      urlBuilder.append(CommonApiConstants.QUERY_PARAM_PROFILE)
          .append(WebUserSetFields.EQUALS_PARAMETER).append(profile);
    }

    /**
     * Execute Europeana API request
     */
    return putURL(urlBuilder.toString(), updateUserSet, regularUserAuthorizationValue);
  }

  /**
   * This method deletes UserSet object by the passed identifier. Example HTTP request:
   * http://localhost:8080/set/{identifier}.jsonld?profile=minimal where identifier is: 494
   *
   * @param identifier The identifier that comprise set ID
   * @return response entity that comprises response headers and status code.
   * @throws IOException
   */
  public ResponseEntity<String> deleteUserSet(String identifier) throws IOException {

    StringBuilder urlBuilder = getUserSetServiceUri();
    urlBuilder.append(identifier).append(WebUserSetFields.JSON_LD_REST);

    /**
     * Execute Europeana API request
     */
    return deleteURL(urlBuilder.toString(), regularUserAuthorizationValue);
  }



}
