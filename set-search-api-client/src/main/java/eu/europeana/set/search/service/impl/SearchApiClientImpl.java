package eu.europeana.set.search.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.search.connection.HttpConnection;
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
    public SearchApiResponse searchItems(String uri, String apiKey, boolean descriptions) throws SearchApiClientException {

	SearchApiResponse searchApiResponse = new SearchApiResponse(apiKey, null);
	uri = appendApiKey(uri, apiKey);
	JSONObject jo = searchItems(uri);
	List<String> res;
	if(isSuccessfull(jo)) {
	    if(descriptions) {
		res = extractItemDescriptions(jo);
	    }else {
		res = extractItemIds(jo);
	    }
	    int total = extractTotalResults(jo);
	    searchApiResponse.setItems(res);
	    searchApiResponse.setTotal(total);
	}
	
	return searchApiResponse;
    }

    private boolean isSuccessfull(JSONObject jo){
	String key_success = "success";
	try {
	    return jo.has(key_success) && jo.getBoolean(key_success);
	} catch (JSONException e) {
	    //actually it shouldn't happen
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
	    throw new SearchApiClientException("Cannot extract total number of results!",
		    e);
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
	    return extractItemsFromSearchResponse(itemsArray, WebUserSetFields.ID);
	} catch (JSONException e) {
	    throw new SearchApiClientException(SearchApiClientException.MESSAGE_CANNOT_PARSE_RESPONSE + e.getMessage(),
		    e);
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
	List<String> list = new ArrayList<String>();
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
	    throw new SearchApiClientException(SearchApiClientException.MESSAGE_CANNOT_PARSE_RESPONSE + e.getMessage(),
		    e);
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

	List<String> list = new ArrayList<String>();
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
	    throw new SearchApiClientException(SearchApiClientException.MESSAGE_CANNOT_PARSE_RESPONSE + e.getMessage(),
		    e);

	}

    }

    JSONObject searchItems(String uri) throws SearchApiClientException {
	String jsonResponse;
	try {
	    jsonResponse = createHttpConnection().getURLContent(uri);
	    if (jsonResponse == null) {
		// HTTP Error Code
		throw new SearchApiClientException(SearchApiClientException.MESSAGE_INVALID_ISDEFINEDNBY, null);
	    }
	    return new JSONObject(jsonResponse);
	} catch (IOException e) {
	    throw new SearchApiClientException(SearchApiClientException.MESSAGE_CANNOT_ACCESS_API + e.getMessage(), e);
	} catch (JSONException e) {
	    throw new SearchApiClientException(SearchApiClientException.MESSAGE_CANNOT_PARSE_RESPONSE + e.getMessage(),
		    e);
	} catch (RuntimeException e) {
	    throw new SearchApiClientException(SearchApiClientException.MESSAGE_CANNOT_RETRIEVE_ITEMS + e.getMessage(),
		    e);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eu.europeana.set.search.service.SearchApiClient#searchItemDescriptions(java.
     * lang.String, java.lang.String)
     */
    public SearchApiResponse searchItemDescriptions(String uri, String apiKey) throws SearchApiClientException {
	return searchItems(uri, apiKey, true);
    }

}
