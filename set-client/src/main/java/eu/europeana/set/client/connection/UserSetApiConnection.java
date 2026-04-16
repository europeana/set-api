package eu.europeana.set.client.connection;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import eu.europeana.api.commons_sb3.auth.AuthenticationHandler;
import eu.europeana.api.commons_sb3.definitions.caching.ResourceCaching;
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
    public Optional<UserSet> getUserSet(String identifier, String profile, ResourceCaching caching)
            throws SetApiClientException {
        StringBuilder urlBuilder = getUserSetServiceUri().append(buildGetUrls(
                identifier + WebUserSetFields.JSON_LD_REST,
                profile));
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
   * This method updates the UserSet with the given identifier by adding the provided items. Example HTTP request:
   * PUT http://localhost:8080/set/{identifier}/items?profile=standard&position=1 where identifier is: 496 and the
   * update JSON string is: ["http://data.europeana.eu/item/2022608/TFM_SVB_FTTF_SCH_ALF_G_01_02"]
   *
   * @param identifier The identifier of the user set to be updated
   * @param itemsJson The request body, item ids in JSON array format 
   * @param position optional, the position to start with (>= 0) when inserting items, otherwise appended to the end
   * @param profile the requested profile
   * @return response entity that comprises response body, headers and status code.
   * @throws SetApiClientException if api invocation fails
   */
  public UserSet addItems(String identifier, String itemsJson, String position, String profile) throws SetApiClientException {
    StringBuilder urlBuilder = getUserSetServiceUri();
    urlBuilder.append(identifier).append("/items").append(WebUserSetFields.PAR_CHAR);
    if (StringUtils.isNotEmpty(profile)) {
      urlBuilder.append(CommonApiConstants.QUERY_PARAM_PROFILE)
          .append(WebUserSetFields.EQUALS_PARAMETER).append(profile);
    }
    if(StringUtils.isNotEmpty(position)) {
      urlBuilder.append(WebUserSetFields.AND)
          .append(WebUserSetFields.REQUEST_PARAM_POSITION)
          .append(WebUserSetFields.EQUALS_PARAMETER).append(position);
    }
    return getUpdateUserSetResponse(urlBuilder.toString(), itemsJson);
  }
  
  /**
   * This method updates the UserSet with the given identifier by removing the indicated items. Example HTTP request:
   * http://localhost:8080/set/{identifier}.jsonld?profile=standard where identifier is: 496 and the
   * update JSON string is: { "title": {"en":"Sport"},"description": {"en":"Best sport"} }
   *
   * @param identifier The identifier that comprise set ID
   * @param itemsJson The update UserSet body in JSON format
   * @param profile the requested profile
   * @return response entity that comprises response body, headers and status code.
   * @throws SetApiClientException if api invocation fails
   */
  public UserSet removeItems(String identifier, String itemsJson, String profile) throws SetApiClientException {
    StringBuilder urlBuilder = getUserSetServiceUri();
    urlBuilder.append(identifier);
    urlBuilder.append("/items");
    urlBuilder.append(WebUserSetFields.PAR_CHAR);
    if (StringUtils.isNotEmpty(profile)) {
      urlBuilder.append(CommonApiConstants.QUERY_PARAM_PROFILE)
          .append(WebUserSetFields.EQUALS_PARAMETER).append(profile);
    }
    
    return getUpdateUserSetResponse(urlBuilder.toString(), itemsJson, true);
  }
  
  /**
   * This method verifies if the UserSet with the given identifier contains a given item. Example HTTP request:
   * GET http://localhost:8080/set/{identifier}/{item_dataset}/{item_localId}?profile=standard where identifier is: 496 and the
   * update JSON string is: { "title": {"en":"Sport"},"description": {"en":"Best sport"} }
   *
   * @param setIdentifier The identifier that comprise set ID
   * @param itemId  using format /{item_dataset}/{item_localId}
   * @return response entity that comprises response body, headers and status code.
   * @throws SetApiClientException if api invocation fails
   */
  public boolean checkItems(String setIdentifier, String itemId) throws SetApiClientException {
    StringBuilder urlBuilder = getUserSetServiceUri();
    urlBuilder.append(setIdentifier);
    urlBuilder.append(itemId);
    urlBuilder.append(WebUserSetFields.PAR_CHAR);
   
    try (CloseableHttpResponse response = getHttpConnection().get(urlBuilder.toString(), null, getAuthenticationHandler())) {

      if (HttpStatus.SC_OK == response.getCode() || HttpStatus.SC_NO_CONTENT == response.getCode())  {
        return true;
      } else if (HttpStatus.SC_NOT_FOUND == response.getCode()) {
        return false;
      } 
        throw new SetApiClientException("Cannot interpret API status code:" + response.getCode(), 
            response.getCode());
      } catch (IOException e) {
        int unknownStatusCode = -1;
        throw new SetApiClientException("API invocation failed!", unknownStatusCode, e);
      }
    } 
  
  
  /**
   * This method deletes UserSet object by the passed identifier. Example HTTP request:
   * http://localhost:8080/set/{identifier}.jsonld?profile=minimal where identifier is: 494
   *
   * @param identifier The identifier that comprise set ID
   * @return response entity that comprises response headers and status code.
   * @throws SetApiClientException if invocation fails
   */
  public String deleteUserSet(String identifier) throws SetApiClientException {
    StringBuilder urlBuilder = getUserSetServiceUri();
    urlBuilder.append(identifier);
    return deleteURL(urlBuilder.toString());
  }

  /**
   * This method fetches the get user set pagination results
   * @param identifier set identifier
   * @param sort sort field
   * @param sortOrder sort order
   * @param page page to retrieve
   * @param pageSize size of retrieved page
   * @param profile serialization profile
   * @return the list with record previews
   * @throws SetApiClientException if invocation fails
   */
  public List<RecordPreview> getPaginationUserSet(String identifier, String sort,
                                                  String sortOrder, String page, String pageSize, String profile) throws SetApiClientException {
    StringBuilder urlBuilder = getUserSetServiceUri().append(
            buildPaginatedGetUrls(identifier + WebUserSetFields.JSON_LD_REST, sort, sortOrder, page, pageSize, profile));
    return getUserSetPaginatedResponse(urlBuilder.toString());

  }
  /**
   * This method searches usersets for the given queries and params
   * Example : /set/search?query=visibility:published&pageSize=1000
   * @param query the search query
   * @param qf query filtering 
   * @param sort sort field 
   * @param page page to retrieve
   * @param pageSize size of retrieved page
   * @param facet facet to retrieve
   * @param facetLimit numer of retrieved facets
   * @param profile serialization profile 
   * @return list of sets
   * @throws SetApiClientException if invocation fails
   */
  public List<UserSet> searchUserSet(String query, String[] qf
                                             , String sort
                                             , String page, String pageSize
                                             , String facet, int facetLimit
                                             , String profile) throws SetApiClientException {

    StringBuilder urlBuilder = getUserSetServiceUri().append(buildSearchUrl(query, qf, sort, page, pageSize, facet, facetLimit, profile));
    return getSearchUserSetResponse(urlBuilder.toString(), profile);
  }
}