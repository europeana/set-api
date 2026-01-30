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
     * @param uri
     * @param searchPostBody Search post request json body
     * @param auth
     * @param descriptions if true include item descriptions, otherwise only ids
     * @return
     * @throws IOException
     * @throws JSONException
     * @throws SearchApiClientException
     */
    public SearchApiResponse searchItems(String uri, String searchPostBody, AuthenticationHandler auth, boolean descriptions)
    		throws SearchApiClientException;

    /**
     * This method queries Europeana API by URI retrieves item descriptions
     * @param uri
     * @param searchPostBody Search post request json body
     * @param auth
     * @return
     * @throws IOException
     * @throws JSONException
     * @throws SearchApiClientException 
     */
    public SearchApiResponse searchItemDescriptions(String uri, String searchPostBody, AuthenticationHandler auth)
    		throws SearchApiClientException;

    /**
     * Returns JsonObject of SR api response
     * @param uri
     * @param postBody
     * @param auth
     * @return
     * @throws SearchApiClientException
     */
    JSONObject searchItems(String uri, String postBody, AuthenticationHandler auth) throws SearchApiClientException;

    /**
     * fill depictions
     * @param searchApiUri
     * @param itemId
     * @param depiction
     * @param auth
     * @throws SearchApiClientException
     */
    void fillDepiction(String searchApiUri, String itemId, BaseWebResource depiction, AuthenticationHandler auth)
            throws SearchApiClientException;

    /**
     *
     * @param searchApiFullUrl
     * @param searchPostBody
     * @param itemIds
     * @param itemDataEndpoint
     * @param depiction
     * @param auth
     * @throws SearchApiClientException
     */
    void fillDepiction(String searchApiFullUrl, String searchPostBody, List<String> itemIds,
        String itemDataEndpoint, BaseWebResource depiction, AuthenticationHandler auth) throws SearchApiClientException;
    
}
