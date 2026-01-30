package eu.europeana.set.search.service;


import java.io.IOException;
import java.util.List;

import eu.europeana.api.commons_sb3.auth.AuthenticationHandler;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import eu.europeana.set.definitions.model.BaseWebResource;
import eu.europeana.set.search.exception.SearchApiClientException;


public interface SearchApiClient {

    /**
     * Searches items from SR api
     * @param uri url
     * @param searchPostBody Search post request json body
     * @param auth authentication handler for SR api
     * @param descriptions if true include item descriptions, otherwise only ids
     * @return
     * @throws IOException
     * @throws JSONException
     * @throws SearchApiClientException
     */
    SearchApiResponse searchItems(String uri, String searchPostBody, AuthenticationHandler auth, boolean descriptions)
    		throws SearchApiClientException;

    /**
     * This method queries Europeana API by URI retrieves item descriptions
     * @param uri url
     * @param searchPostBody Search post request json body
     * @param auth authentication handler for SR api
     * @return
     * @throws SearchApiClientException 
     */
    SearchApiResponse searchItemDescriptions(String uri, String searchPostBody, AuthenticationHandler auth)
    		throws SearchApiClientException;

    /**
     * Returns JsonObject of SR api response
     * @param uri url of sr api
     * @param postBody body for the request
     * @param auth authentication handler for SR api
     * @return
     * @throws SearchApiClientException
     */
    JSONObject searchItems(String uri, String postBody, AuthenticationHandler auth) throws SearchApiClientException;

    /**
     * fill depictions
     * @param searchApiUri sr api url
     * @param itemId item ids
     * @param depiction depiction
     * @param auth authentication handler for SR api
     * @throws SearchApiClientException
     */
    void fillDepiction(String searchApiUri, String itemId, BaseWebResource depiction, AuthenticationHandler auth)
            throws SearchApiClientException;

    /**
     *
     * @param searchApiFullUrl sr api url
     * @param searchPostBody post body
     * @param itemIds item ids
     * @param itemDataEndpoint item data endpoint
     * @param depiction  depiction
     * @param auth authentication handler for SR api
     * @throws SearchApiClientException
     */
    void fillDepiction(String searchApiFullUrl, String searchPostBody, List<String> itemIds,
        String itemDataEndpoint, BaseWebResource depiction, AuthenticationHandler auth) throws SearchApiClientException;
    
}
