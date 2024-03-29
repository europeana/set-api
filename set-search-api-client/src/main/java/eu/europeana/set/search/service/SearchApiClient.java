package eu.europeana.set.search.service;


import java.io.IOException;

import org.apache.commons.httpclient.HttpException;
import org.codehaus.jettison.json.JSONException;

import eu.europeana.set.search.exception.SearchApiClientException;


public interface SearchApiClient {

    /**
     * @param uri
     * @param searchPostBody Search post request json body
     * @param apiKey
     * @param descriptions if true include item descriptions, otherwise only ids
     * @return
     * @throws IOException
     * @throws JSONException
     * @throws SearchApiClientException 
     * @throws HttpException
     */
    public SearchApiResponse searchItems(String uri, String searchPostBody, String apiKey, boolean descriptions)
    		throws SearchApiClientException;

    /**
     * This method queries Europeana API by URI retrieves item descriptions
     * @param uri
     * @param searchPostBody Search post request json body
     * @param apiKey
     * @return
     * @throws IOException
     * @throws JSONException
     * @throws SearchApiClientException 
     * @throws HttpException
     */
    public SearchApiResponse searchItemDescriptions(String uri, String searchPostBody, String apiKey)
    		throws SearchApiClientException;
    
}
