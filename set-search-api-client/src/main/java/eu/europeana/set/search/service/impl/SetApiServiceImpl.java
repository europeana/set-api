package eu.europeana.set.search.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.europeana.api.commons.definitions.search.Query;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.search.connection.HttpConnection;
import eu.europeana.set.search.service.SearchApiResponse;
import eu.europeana.set.search.service.SetApiService;

/**
 * @author GrafR
 *
 */
public class SetApiServiceImpl implements SetApiService {

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
     * @param uri  The query URL from isDefinedBy field.
     * @return response from Europeana API in JSON-LD format
     * @throws IOException
     * @throws JSONException 
     * @throws HttpException 
     */
    public synchronized SearchApiResponse queryEuropeanaApi(String uri, String apiKey, String action) throws IOException, JSONException, HttpException {
		List<String> res = parseItemsByUrl(uri);
		SearchApiResponse searchApiResponse = new SearchApiResponse(apiKey, action);
		searchApiResponse.setItems(res);
		return searchApiResponse;
    }

	/**
	 * Get list of IDs from JSONArray
	 * @param valueObject
	 * @return
	 * @throws JSONException
	 */
	protected List<String> jsonArrayToStringArray(JSONArray valueObject) throws JSONException {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < valueObject.length(); i++) {
			JSONObject guidJson = valueObject.getJSONObject(i);
			String id = guidJson.getString(WebUserSetFields.ID);
			list.add(id);
		}
		return list;
	}

    /**
     * Uses the underlying Jettison to parse a JSON object.
     * 
     * @param jsonString The JSON String representation.
     * @return JSONObject
     */
    protected static JSONObject parseJson(String jsonString) throws JSONException {
        JSONObject jo = null;
        try {
            jo = new JSONObject(jsonString);
        } catch (JSONException e) {
            throw new JSONException("Could not parse JSON string: " + jsonString + e);
        }

        return jo;
    }
    
	/* (non-Javadoc)
	 * @see eu.europeana.set.search.service.SetApiService#parseItemsByUrl(java.lang.String)
	 */
	@Override
	public List<String> parseItemsByUrl(String uri) throws HttpException, IOException, JSONException {
		String jsonResponse = getHttpConnection().getURLContent(uri);
		JSONObject jo = parseJson(jsonResponse);
		JSONArray itemsArray = jo.getJSONArray(WebUserSetFields.ITEMS);
		return jsonArrayToStringArray(itemsArray);
	}

	@Override
	public SearchApiResponse search(Query query) throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}
}
