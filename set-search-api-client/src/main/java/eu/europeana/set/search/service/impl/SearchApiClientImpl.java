package eu.europeana.set.search.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
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

  public static final String EDM_PREVIEW = "edmPreview";
  public static final String EDM_ID = "id";
  public static final String PARAM_URI = "uri";
  
  private static final Logger LOGGER = LogManager.getLogger(SearchApiClientImpl.class);
  
  private final String dataEndoint;
  private final HttpConnection httpConnection = new HttpConnection(true);
   
  /**
   * Constructor setting the data endpoint
   * @param dataEndoint the data endpoint to be used for building item endpoints
   */
  public SearchApiClientImpl(String dataEndoint) {
    this.dataEndoint = dataEndoint;
  }
  
  
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
    
    Map<String, String> headers = Map.of(HttpHeaders.CONTENT_TYPE, "application/json");
    CloseableHttpResponse httpResponse;
    try {
      if (postBody != null) {
        httpResponse = httpConnection.post(url, postBody, headers, auth);
        } else {
          httpResponse = httpConnection.get(url, headers, auth);
        } 
    } catch (IOException e) {
          throw new SearchApiClientException(
              SearchApiClientException.MESSAGE_CANNOT_ACCESS_API + e.getMessage(), e);
     }

    try (httpResponse){
      InputStream content = httpResponse.getEntity().getContent();
      String body = new String(content.readAllBytes(), StandardCharsets.UTF_8);
      if (httpResponse.getCode() != HttpStatus.SC_OK) {
        //search request failed
        throw new SearchApiClientException(SearchApiClientException.MESSAGE_CANNOT_RETRIEVE_ITEMS +
            " Response status: " + httpResponse.getCode() + " Response body: " + body,
            null);
      }
      //return response body
      return body;
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

    String searchResult = null;
    try {
      searchResult = searchItemDescriptionsAsString(searchApiFullUrl, searchPostBody, auth);
      JSONArray itemsArray = new JSONObject(searchResult).getJSONArray("items");
      if(itemsArray == null || itemsArray.length() < 1) {
        //no results found
        return;
      }
      
      //select item with depiction
      //find the json node of the first found item and fill depiction
      for (int i = 0; i < itemsArray.length(); i++) {
        JSONObject recordJsonObject = itemsArray.getJSONObject(i);
        if(!recordJsonObject.has(EDM_PREVIEW)) {
          //no preview
          continue;
        }
        
        //not guaranteed to be the first set's item with depiction, but for the fallback we keep it simple 
        fillDepictionFromRecord(recordJsonObject, depiction);
          break;  
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
      fillDepictionFromRecord(items.getJSONObject(0), depiction);
    } catch (JSONException e) {
      throw new SearchApiClientException(
          "Cannot extract depiction data from search Api response: " + searchResult, null);
    }
  }


  private void fillDepictionFromRecord(final JSONObject recordJsonObject,
      BaseWebResource depiction) throws JSONException, SearchApiClientException {
    
    if(recordJsonObject.has(EDM_PREVIEW)) {  
      String thumbnail = recordJsonObject.getJSONArray(EDM_PREVIEW).getString(0);
      //id is the IMAGE url
      String resourceId = getResourceId(thumbnail);
      depiction.setId(resourceId);
      
      //source is the identifier of the EDM record
      String localID = recordJsonObject.getString(EDM_ID);
      String fullItemId = UserSetUtils.buildItemUrl(getDataEndoint(), localID);
      depiction.setSource(fullItemId);
      //full thumbnail URL
      depiction.setThumbnail(thumbnail);
     }
  }


  private String getResourceId(String thumbnailUrl) throws SearchApiClientException {
    NameValuePair uriParam;
    try {
      uriParam = (new URIBuilder(thumbnailUrl)).getFirstQueryParam(PARAM_URI);
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


  public String getDataEndoint() {
    return dataEndoint;
  }
}
