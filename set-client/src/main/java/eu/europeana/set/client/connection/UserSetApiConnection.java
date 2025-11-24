package eu.europeana.set.client.connection;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import eu.europeana.api.commons_sb3.auth.AuthenticationHandler;
import eu.europeana.api.commons_sb3.definitions.caching.ResourceCaching;
import org.apache.commons.lang3.StringUtils;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonApiConstants;
import eu.europeana.set.client.exception.SetApiClientException;
import eu.europeana.set.client.model.result.RecordPreview;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;

/**
 * @author GrafR
 */
public class UserSetApiConnection extends BaseApiConnection {

  public UserSetApiConnection(String setServiceUri, AuthenticationHandler auth) {
      super(setServiceUri, auth);
  }

  /**
   * This method creates UserSet object from Json string. Example HTTP request for tag object:
   * http://localhost:8080/set/?profile=minimal
   *
   * @param set The UserSet body
   * @param profile profile requested
   * @return response entity that comprises response body, headers and status code.
   * @throws IOException
   */
  public UserSet createUserSet(String set, String profile) 
         throws SetApiClientException {
      StringBuilder urlBuilder = getUserSetServiceUri();
      if (StringUtils.isNotEmpty(profile)) {
          urlBuilder.append(WebUserSetFields.PAR_CHAR)
                    .append(CommonApiConstants.QUERY_PARAM_PROFILE)
                    .append(WebUserSetFields.EQUALS_PARAMETER)
                    .append(profile);
      }
      String resUrl = urlBuilder.toString();
      LOGGER.trace("Ivoking create set: {} ", resUrl);
      return getCreateUserSetResponse(resUrl, set);
  }

    /**
     * This method retrieves UserSet object. Example HTTP request for tag object:
     * http://localhost:8080/set/{identifier}.jsonld?profile=minimal where identifier is: 496
     *
     * @param identifier set id
     * @param profile profile requested
     * @param caching  if present caching headers are set in the http request.
     * @throws IOException
     * @return userset
     */
    public Optional<UserSet> getUserSet(String identifier, Optional<String> profile, Optional<ResourceCaching> caching)
            throws SetApiClientException {
        StringBuilder urlBuilder = getUserSetServiceUri().append(buildGetUrls(
                identifier + WebUserSetFields.JSON_LD_REST,
                profile.isPresent() ? profile.get() : null));
        return getUserSetResponse(urlBuilder.toString(), caching);
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
    urlBuilder.append(identifier);
    if (StringUtils.isNotEmpty(profile)) {
      urlBuilder.append(WebUserSetFields.PAR_CHAR);
      urlBuilder.append(CommonApiConstants.QUERY_PARAM_PROFILE)
          .append(WebUserSetFields.EQUALS_PARAMETER).append(profile);
    }
    return getUpdateUserSetResponse(urlBuilder.toString(), updateUserSet);
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
    urlBuilder.append(identifier);
    return deleteURL(urlBuilder.toString());
  }

  /**
   * This method fetches the get user set pagination results
   * @param identifier
   * @param sort
   * @param sortOrder
   * @param page
   * @param pageSize
   * @param profile
   * @return
   * @throws SetApiClientException
   */
  public List<RecordPreview> getPaginationUserSet(String identifier, String sort,
                                                  String sortOrder, String page, String pageSize, String profile) throws SetApiClientException {
    StringBuilder urlBuilder = getUserSetServiceUri().append(
            buildPaginatedGetUrls(identifier + WebUserSetFields.JSON_LD_REST, sort, sortOrder, page, pageSize, profile));
    return getUserSetPaginatedResponse(urlBuilder.toString(), profile);

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
  public List<? extends UserSet> searchUserSet(String query, String[] qf
                                             , String sort
                                             , String page, String pageSize
                                             , String facet, int facetLimit
                                             , String profile) throws SetApiClientException {

    StringBuilder urlBuilder = getUserSetServiceUri().append(buildSearchUrl(query, qf, sort, page, pageSize, facet, facetLimit, profile));
    return getSearchUserSetResponse(urlBuilder.toString(), profile);
  }
}