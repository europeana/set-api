package eu.europeana.set.search.service;


import java.io.IOException;
import java.util.List;

import org.codehaus.jettison.json.JSONException;

import eu.europeana.api.commons.definitions.search.Query;
import eu.europeana.api.commons.web.exception.HttpException;


public interface SetApiService {

	/**
	 * This methods converts items from user set object to a list of IDs.
	 * @param uri The search URI
	 * @return a list of IDs
	 * @throws HttpException
	 * @throws IOException
	 * @throws JSONException
	 */
	List<String> parseItemsByUrl(String uri) throws HttpException, IOException, JSONException;

	/**
	 * This method searches the user sets using the provided search query and specific filters
	 * @param query
	 * @return
	 * @throws HttpException
	 */
	public SearchApiResponse search(Query query) throws HttpException;
	
    /**
     * @param uri
     * @param apiKey
     * @param action
     * @return
     * @throws IOException
     * @throws JSONException
     * @throws HttpException
     */
    public SearchApiResponse queryEuropeanaApi(String uri, String apiKey, String action) 
    		throws IOException, JSONException, HttpException;

}
