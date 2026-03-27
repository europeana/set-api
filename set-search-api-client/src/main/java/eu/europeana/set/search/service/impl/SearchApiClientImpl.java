package eu.europeana.set.search.service.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import eu.europeana.api.commons_sb3.auth.AuthenticationHandler;
import eu.europeana.api.commons_sb3.http.HttpConnection;
import eu.europeana.api.commons_sb3.http.HttpResponseHandler;
import eu.europeana.set.definitions.model.BaseWebResource;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.search.exception.SearchApiClientException;
import eu.europeana.set.search.service.SearchApiClient;
import eu.europeana.set.search.service.SearchApiResponse;

/**
 * @author GrafR
 * @author GordeaS
 *
 */
public class SearchApiClientImpl implements SearchApiClient {

  private static final Logger LOGGER = LogManager.getLogger(SearchApiClientImpl.class);

  private final HttpConnection httpConnection = new HttpConnection(true);

  /**
   * Fetch items from Search api and return the SearchApiResponse with the items list
   * @param uri search api url
   * @param searchPostBody Search post request json body
   * @param auth authentication to access Search api
   * @param descriptions if true include item descriptions, otherwise only ids
   * @return
   * @throws SearchApiClientException sr api exception
   */
  @Override
  public SearchApiResponse searchItems(String uri, String searchPostBody, AuthenticationHandler auth,
      boolean descriptions) throws SearchApiClientException {
    SearchApiResponse searchApiResponse = new SearchApiResponse();
    JSONObject jo = searchItems(uri, searchPostBody, auth);
    List<String> res;
    if (isSuccessful(jo)) {
      if (descriptions) {
        res = extractItemDescriptions(jo);
      } else {
        res = extractItemIds(jo);
      }
      int total = extractTotalResults(jo);
      searchApiResponse.setItems(res);
      searchApiResponse.setTotal(total);
    }
    return searchApiResponse;
  }

  /**
   * Fetch response from search api
   * @param uri
   * @param postBody
   * @param auth
   * @return
   * @throws SearchApiClientException sr api exceptions
   */
  @Override
  public JSONObject searchItems(String uri, String postBody, AuthenticationHandler auth) throws SearchApiClientException {
    String responseBodyAsString = searchItemDescriptionsAsString(uri, postBody, auth);
    try {
      return new JSONObject(responseBodyAsString);
    }catch (JSONException e) {
      throw new SearchApiClientException(
              SearchApiClientException.MESSAGE_CANNOT_PARSE_RESPONSE + e.getMessage(), e);
    }
  }

  /**
   * Checks if the SR API response contains "success": true
   * @param jo jsonObject of Sr api response
   * @return true if response contains "success": true
   */
  private boolean isSuccessful(JSONObject jo) {
    String keySuccess = "success";
    try {
      return jo.has(keySuccess) && jo.getBoolean(keySuccess);
    } catch (JSONException e) {
      // actually it shouldn't happen
      LOGGER.trace("Invalid Json Object", e);
      return false;
    }
  }

  private int extractTotalResults(JSONObject jo) throws SearchApiClientException {
    int total = -1;
    String key = "totalResults";
    try {
      if (jo.has(key)) {
        total = jo.getInt(key);
      }
    } catch (JSONException e) {
      throw new SearchApiClientException("Cannot extract total number of results!", e);
    }
    return total;
  }

  /**
   * Get list of IDs from JSONArray
   * 
   * @param jo json object of SR api response
   * @return list of item ids from the items array
   * @throws JSONException
   */
  protected List<String> extractItemIds(JSONObject jo) throws SearchApiClientException {
    try {
      JSONArray itemsArray = jo.getJSONArray(WebUserSetFields.ITEMS);
      return extractItemsFromSearchResponse(itemsArray, WebUserSetModelFields.ID);
    } catch (JSONException e) {
      throw new SearchApiClientException(
          SearchApiClientException.MESSAGE_CANNOT_PARSE_RESPONSE + e.getMessage(), e);
    }

  }

  /**
   * Get list of items from JSONObject
   * 
   * @param jo json object of SR api response
   * @return list of items from the items array
   * @throws JSONException
   */
  protected List<String> extractItemDescriptions(JSONObject jo) throws SearchApiClientException {
    List<String> list = new ArrayList<>();
    if (jo == null) {
      return list;
    }

    try {
      JSONArray itemsArray = jo.getJSONArray(WebUserSetFields.ITEMS);
      for (int i = 0; i < itemsArray.length(); i++) {
        JSONObject itemAsJson = itemsArray.getJSONObject(i);
        list.add(itemAsJson.toString(4));
      }
      return list;

    } catch (JSONException e) {
      throw new SearchApiClientException(
          SearchApiClientException.MESSAGE_CANNOT_PARSE_RESPONSE + e.getMessage(), e);
    }
  }

  /**
   * Get list of values specified by field name from JSONArray
   * 
   * @param valueObject
   * @return list of values
   * @throws JSONException
   * @throws SearchApiClientException sr api exception
   */
  protected List<String> extractItemsFromSearchResponse(JSONArray valueObject, String fieldName)
      throws SearchApiClientException {

    List<String> list = new ArrayList<>();
    if (valueObject == null) {
      return list;
    }

    try {
      for (int i = 0; i < valueObject.length(); i++) {
        JSONObject guidJson = valueObject.getJSONObject(i);
        String value = guidJson.getString(fieldName);
        if (!list.contains(value))
          list.add(value);
      }
      return list;
    } catch (JSONException e) {
      throw new SearchApiClientException(
          SearchApiClientException.MESSAGE_CANNOT_PARSE_RESPONSE + e.getMessage(), e);

    }

  }

  /**
   * Fires the search request and returns the body
   * @param url the search api URL
   * @param postBody for search request
   * @param auth authentication handler for SR api
   * @return response body
   * @throws SearchApiClientException sr api exception
   */
  public String searchItemDescriptionsAsString(String url, String postBody, AuthenticationHandler auth)
      throws SearchApiClientException {
    HttpResponseHandler httpResponse;
    try {
      if (postBody != null) {
        httpResponse = httpConnection.post(url, postBody, "application/json", auth);
      } else {
        httpResponse = httpConnection.get(url, "application/json", auth);
      }
      if (httpResponse == null) {
          throw new SearchApiClientException(SearchApiClientException.MESSAGE_INVALID_ISDEFINEDNBY,
              null);
      }
      
      if (httpResponse.getStatus() != HttpStatus.SC_OK) {
        //search request failed
        throw new SearchApiClientException(SearchApiClientException.MESSAGE_CANNOT_RETRIEVE_ITEMS +
            " Response status: " + httpResponse.getStatus() + " Response body: " + httpResponse.getResponse(),
            null);
      }
      //return response body
      return httpResponse.getResponse();
    } catch (IOException e) {
      throw new SearchApiClientException(
          SearchApiClientException.MESSAGE_CANNOT_ACCESS_API + e.getMessage(), e);
    } catch (RuntimeException e) {
      throw new SearchApiClientException(
          SearchApiClientException.MESSAGE_CANNOT_RETRIEVE_ITEMS + e.getMessage(), e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.europeana.set.search.service.SearchApiClient#searchItemDescriptions(java. lang.String,
   * java.lang.String)
   */
  public SearchApiResponse searchItemDescriptions(String uri, String searchPostBody, AuthenticationHandler auth)
      throws SearchApiClientException {
    return searchItems(uri, searchPostBody, auth, true);
  }
  
  @Override
  public void fillDepiction(String searchApiFullUrl, String searchPostBody, List<String> itemIds, String itemDataEndpoint,
                            BaseWebResource depiction, AuthenticationHandler auth)
      throws SearchApiClientException {

    String firstFoundLocalId = null;
    String firstFoundItemlId = null;
    String searchResult = null;
    try {
      searchResult = searchItemDescriptionsAsString(searchApiFullUrl, searchPostBody, auth);
      JSONArray itemsArray = new JSONObject(searchResult).getJSONArray("items");
      if(itemsArray == null || itemsArray.length() < 1) {
        //no results found
        return;
      }
      //search the first found itemId in the search results
      for (String itemId : itemIds) {
        String localId =
            UserSetUtils.extractItemIdentifier(itemId, itemDataEndpoint);
        String recordIdJsonString = UserSetUtils.buildRecordIdJsonString(localId, false, false);
        if(searchResult.contains(recordIdJsonString)) {
          firstFoundLocalId = localId;
          firstFoundItemlId = itemId;
          break;
        }
      }
      
      if(firstFoundLocalId == null) {
        //none found
        return;
      }
      
      //find the json node of the first found item and fill depiction
      for (int i = 0; i < itemsArray.length(); i++) {
        JSONObject recordJsonObject = itemsArray.getJSONObject(i);
        //process
        if(firstFoundLocalId.equals(recordJsonObject.getString("id"))) { 
          fillDepictionFromRecord(firstFoundItemlId, recordJsonObject, depiction);
          break;
        }
      }
     } catch (JSONException e) {
      throw new SearchApiClientException(
          "Cannot extract depiction data from search Api response: " + searchResult, null);
    }
  }
  
  @Override
  public void fillDepiction(String searchApiFullUrl, String itemId, BaseWebResource depiction, AuthenticationHandler auth)
      throws SearchApiClientException {
    JSONObject searchResult = null;
    try {
      searchResult = searchItems(searchApiFullUrl, null, auth);
      JSONArray items = searchResult.getJSONArray("items");
      if(items == null || items.length() < 1) {
        return;
      }
      fillDepictionFromRecord(itemId, items.getJSONObject(0), depiction);
    } catch (JSONException e) {
      throw new SearchApiClientException(
          "Cannot extract depiction data from search Api response: " + searchResult, null);
    }
  }


  private void fillDepictionFromRecord(String itemId, final JSONObject recordJsonObject,
      BaseWebResource depiction) throws JSONException, SearchApiClientException {
    String thumbnail = recordJsonObject.getJSONArray("edmPreview").getString(0);
      String resourceId = getResourceId(thumbnail);

      depiction.setId(resourceId);
      depiction.setSource(itemId);
      depiction.setThumbnail(thumbnail);
  }


  private String getResourceId(String thumbnailUrl) throws SearchApiClientException {
    NameValuePair uriParam;
    try {
      uriParam = (new URIBuilder(thumbnailUrl)).getFirstQueryParam("uri");
    } catch (URISyntaxException e) {
      throw new SearchApiClientException(
          "Invalid thumbnail URL: " + thumbnailUrl, e);
    }
    
    if(uriParam == null) {
      throw new SearchApiClientException(
          "Cannot extract resource id from thumbnail URL: " + thumbnailUrl, null);
    }
    return uriParam.getValue();
  }
}
