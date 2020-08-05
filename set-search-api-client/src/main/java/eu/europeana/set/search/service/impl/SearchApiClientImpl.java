package eu.europeana.set.search.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.europeana.set.definitions.model.vocabulary.fields.WebUserSetModelFields;
import org.apache.commons.httpclient.HttpException;
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

    private HttpConnection httpConnection = new HttpConnection();

    Logger logger = LogManager.getLogger(getClass().getName());

    public HttpConnection getHttpConnection() {
	return httpConnection;
    }

    public void setHttpConnection(HttpConnection httpConnection) {
	this.httpConnection = httpConnection;
    }

    /**
     * This method queries Europeana API by URI
     * 
     * @param apiKey
     * @param action
     * @param uri    The query URL from isDefinedBy field.
     * @return response from Europeana API in JSON-LD format
     * @throws IOException
     * @throws JSONException
     * @throws HttpException
     */
    public SearchApiResponse searchItems(String uri, String apiKey, String action)
	    throws SearchApiClientException{

	if (!uri.contains("wskey="))
	    uri += ("&wskey=" + apiKey);
	List<String> res = searchItems(uri);
	SearchApiResponse searchApiResponse = new SearchApiResponse(apiKey, action);
	searchApiResponse.setItems(res);
	return searchApiResponse;
    }

    /**
     * Get list of IDs from JSONArray
     * 
     * @param valueObject
     * @return
     * @throws JSONException
     */
    protected List<String> jsonArrayToStringArray(JSONArray valueObject) throws JSONException {
	return extractItemDescriptions(valueObject, WebUserSetModelFields.ID);
    }

    /**
     * Get list of values specified by field name from JSONArray
     * 
     * @param valueObject
     * @return list of values
     * @throws JSONException
     */
    protected List<String> extractItemDescriptions(JSONArray valueObject, String fieldName) throws JSONException {	List<String> list = new ArrayList<>();
	if (valueObject == null) {
	    return list;
	}
	for (int i = 0; i < valueObject.length(); i++) {
	    JSONObject guidJson = valueObject.getJSONObject(i);
	    String value = guidJson.getString(fieldName);
	    if (!list.contains(value))
	    	list.add(value);
	}
	return list;
    }

    public List<String> searchItems(String uri) throws SearchApiClientException {
	String jsonResponse;
	try {
	    jsonResponse = getHttpConnection().getURLContent(uri);
	    if (jsonResponse == null) {
		// HTTP Error Code
		throw new SearchApiClientException(SearchApiClientException.MESSAGE_INVALID_ISSHOWNBY, null);
	    }
	    JSONObject jo = new JSONObject(jsonResponse);
	    JSONArray itemsArray = jo.getJSONArray(WebUserSetFields.ITEMS);
	    return jsonArrayToStringArray(itemsArray);
	    
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
    
    /* (non-Javadoc)
     * @see eu.europeana.set.search.service.SearchApiClient#searchItemDescriptions(java.lang.String, java.lang.String, java.lang.String)
     */
    public SearchApiResponse searchItemDescriptions(String uri, String apiKey, String action)
	    throws SearchApiClientException{

	if (!uri.contains("wskey="))
	    uri += ("&wskey=" + apiKey);
	List<String> res = searchItemDescriptions(uri);
	SearchApiResponse searchApiResponse = new SearchApiResponse(apiKey, action);
	searchApiResponse.setItems(res);
	return searchApiResponse;
    }    
    

    /**
     * This method queries Europeana API by URI and retrievs item descriptions
     * @param uri
     * @return item list in JSON format
     * @throws SearchApiClientException
     */
    public List<String> searchItemDescriptions(String uri) throws SearchApiClientException {
	String jsonResponse;
	try {
	    jsonResponse = getHttpConnection().getURLContent(uri);
	    if (jsonResponse == null) {
		// HTTP Error Code
		throw new SearchApiClientException(SearchApiClientException.MESSAGE_INVALID_ISSHOWNBY, null);
	    }
	    JSONObject jo = new JSONObject(jsonResponse);
	    JSONArray itemsArray = jo.getJSONArray(WebUserSetFields.ITEMS);
	    return extractItemDescriptions(itemsArray, "dcDescription");

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
}
