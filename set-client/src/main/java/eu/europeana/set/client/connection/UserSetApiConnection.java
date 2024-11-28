package eu.europeana.set.client.connection;

import java.io.IOException;

import eu.europeana.api.commons.definitions.search.result.impl.ResultsPageImpl;
import eu.europeana.set.client.exception.SetApiClientException;
import eu.europeana.set.definitions.model.UserSet;
import org.apache.commons.lang3.StringUtils;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
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
  public UserSet createUserSet(String set, String profile) throws SetApiClientException {
    StringBuilder urlBuilder = getUserSetServiceUri();
    if (StringUtils.isNotEmpty(profile)) {
      urlBuilder.append(WebUserSetFields.PAR_CHAR);
      urlBuilder.append(CommonApiConstants.QUERY_PARAM_PROFILE)
          .append(WebUserSetFields.EQUALS_PARAMETER).append(profile);
    }
    String resUrl = urlBuilder.toString();
    LOGGER.trace("Ivoking create set: {} ", resUrl);
    return getCreateUserSetResponse(resUrl, set, regularUserAuthorizationValue);
  }

  /**
   * This method retrieves UserSet object. Example HTTP request for tag object:
   * http://localhost:8080/set/{identifier}.jsonld?profile=minimal where identifier is: 496
   *
   * @param identifier
   * @param profile
   * @throws IOException
   */
  public UserSet getUserSet(String identifier, String profile) throws SetApiClientException {
    StringBuilder urlBuilder = getUserSetServiceUri().append(buildGetUrls(identifier + WebUserSetFields.JSON_LD_REST, profile));
    return getUserSetResponse(urlBuilder.toString(),  regularUserAuthorizationValue);
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
  public UserSet updateUserSet(String identifier, String updateUserSet, String profile) throws SetApiClientException {
    StringBuilder urlBuilder = getUserSetServiceUri();
    urlBuilder.append(identifier).append(WebUserSetFields.JSON_LD_REST);
    if (StringUtils.isNotEmpty(profile)) {
      urlBuilder.append(WebUserSetFields.PAR_CHAR);
      urlBuilder.append(CommonApiConstants.QUERY_PARAM_PROFILE)
          .append(WebUserSetFields.EQUALS_PARAMETER).append(profile);
    }
    return getUpdateUserSetResponse(urlBuilder.toString(), updateUserSet, regularUserAuthorizationValue);
  }

  /**
   * This method deletes UserSet object by the passed identifier. Example HTTP request:
   * http://localhost:8080/set/{identifier}.jsonld?profile=minimal where identifier is: 494
   *
   * @param identifier The identifier that comprise set ID
   * @return response entity that comprises response headers and status code.
   * @throws IOException
   */
  public String deleteUserSet(String identifier) throws SetApiClientException {
    StringBuilder urlBuilder = getUserSetServiceUri();
    urlBuilder.append(identifier).append(WebUserSetFields.JSON_LD_REST);
    return deleteURL(urlBuilder.toString(), regularUserAuthorizationValue);
  }

  /**
   * This method searches usersets for the given queries and params
   * Example : /set/search?query=visibility:published&pageSize=1000
   * @param query
   * @param qf
   * @param sort
   * @param page
   * @param pageSize
   * @param facet
   * @param facetLimit
   * @param profile
   * @return
   * @throws IOException
   */
  public ResultsPageImpl<? extends UserSet> searchUserSet(String query, String[] qf, String sort, int page,
                                                          int pageSize, String facet, int facetLimit,
                                                          String profile) throws SetApiClientException {

    StringBuilder urlBuilder = getUserSetServiceUri().append(buildSearchUrl(query, qf, sort, page, pageSize, facet, facetLimit, profile));
    return getSearchUserSetResponse(urlBuilder.toString(), regularUserAuthorizationValue);
  }
}
