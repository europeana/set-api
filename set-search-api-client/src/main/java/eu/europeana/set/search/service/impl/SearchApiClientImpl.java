package eu.europeana.set.search.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import eu.europeana.set.common.http.HttpConnection;
import eu.europeana.set.common.http.HttpResponseHandler;
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

  Logger logger = LogManager.getLogger(getClass().getName());

  public HttpConnection createHttpConnection() {
    return new HttpConnection();
  }


  @Override
  public SearchApiResponse searchItems(String uri, String serachPostBody, String apiKey,
      boolean descriptions) throws SearchApiClientException {
    SearchApiResponse searchApiResponse = new SearchApiResponse(apiKey, null);
    uri = appendApiKey(uri, apiKey);
    JSONObject jo = searchItems(uri, serachPostBody);
    List<String> res;
    if (isSuccessfull(jo)) {
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

  private boolean isSuccessfull(JSONObject jo) {
    String keySuccess = "success";
    try {
      return jo.has(keySuccess) && jo.getBoolean(keySuccess);
    } catch (JSONException e) {
      // actually it shouldn't happen
      logger.trace("Invalid Json Object", e);
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

  private String appendApiKey(String uri, String apiKey) {
    if (!uri.contains("wskey=")) {
      uri += ("&wskey=" + apiKey);
    }
    return uri;
  }

  /**
   * Get list of IDs from JSONArray
   * 
   * @param valueObject
   * @return
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
   * Get list of IDs from JSONArray
   * 
   * @param valueObject
   * @return
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
   * @throws SearchApiClientException
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

  @Override
  public JSONObject searchItems(String uri, String postBody) throws SearchApiClientException {
    String responseBodyAsString = searchItemDescriptionsAsString(uri, postBody);
    
    try {
      return new JSONObject(responseBodyAsString);
    }catch (JSONException e) {
      throw new SearchApiClientException(
          SearchApiClientException.MESSAGE_CANNOT_PARSE_RESPONSE + e.getMessage(), e);
    }
  }


  /**
   * Fires the search request and returns the body
   * @param url the search api URL
   * @param postBody for search request
   * @return response body
   * @throws SearchApiClientException
   */
  public String searchItemDescriptionsAsString(String url, String postBody)
      throws SearchApiClientException {
    HttpResponseHandler resp;
    try {
      if (postBody != null) {
        resp = createHttpConnection().post(url, postBody, "application/json", null);
      } else {
        resp = createHttpConnection().get(url, "application/json", null);
      }
      if (resp == null) {
          // HTTP Error Code
          throw new SearchApiClientException(SearchApiClientException.MESSAGE_INVALID_ISDEFINEDNBY,
              null);
      }
      
      if(resp.getStatus() != HttpStatus.SC_OK) {
        //search request failed
        throw new SearchApiClientException(SearchApiClientException.MESSAGE_CANNOT_RETRIEVE_ITEMS +
            " Response status: " + resp.getStatus() + " Response body: " + resp.getResponse(),
            null);
      }
      //return response body
      return resp.getResponse();
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
  public SearchApiResponse searchItemDescriptions(String uri, String searchPostBody, String apiKey)
      throws SearchApiClientException {
    return searchItems(uri, searchPostBody, apiKey, true);
  }
  
  @Override
  public void fillDepiction(String searchApiFullUrl, String searchPostBody, List<String> itemIds, String itemDataEndpoint, BaseWebResource depiction)
      throws SearchApiClientException {

    String firstFoundLocalId = null;
    String firstFoundItemlId = null;
    String searchResult = null;
    try {
      searchResult = searchItemDescriptionsAsString(searchApiFullUrl, searchPostBody);
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
  public void fillDepiction(String searchApiFullUrl, String itemId, BaseWebResource depiction)
      throws SearchApiClientException {

    JSONObject searchResult = null;
    try {
      searchResult = searchItems(searchApiFullUrl, null);
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
    final String queryString = StringUtils.substringAfter(thumbnailUrl, "?");
    List<NameValuePair> params = URLEncodedUtils.parse(queryString, StandardCharsets.UTF_8);
    for (NameValuePair param : params) {
      if ("uri".equals(param.getName())) {
        return param.getValue();
      }
    }
    throw new SearchApiClientException(
        "Cannot extract resource id from thumbnail URL: " + thumbnailUrl, null);
  }
}
