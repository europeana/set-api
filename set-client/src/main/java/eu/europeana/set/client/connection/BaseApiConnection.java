package eu.europeana.set.client.connection;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.api.commons.definitions.search.result.impl.ResultsPageImpl;
import eu.europeana.set.client.exception.SetApiClientException;
import eu.europeana.set.client.model.result.AbstractUserSetApiResponse;
import eu.europeana.set.common.http.HttpConnection;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.impl.BaseUserSet;
import eu.europeana.set.definitions.model.vocabulary.ProfileConstants;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants.*;
import static eu.europeana.set.definitions.model.vocabulary.WebUserSetFields.SEARCH_PATH;

public class BaseApiConnection {

    protected static final Logger LOGGER = LogManager.getLogger(BaseApiConnection.class);

    private static final String DELETE_URL_RESPONSE = ". Returns status code.";
    private static final String ERROR_MESSAGE = "Set API Client call failed - ";

    private final HttpConnection httpConnection = new HttpConnection();
    private final String apiKey;
    private final String setServiceUri;
    String regularUserAuthorizationValue;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * BaseApiConnection constructor
     * @param setServiceUri set api service url
     * @param apiKey apikey
     * @param regularUserAuthorizationValue auth value
     */
    public BaseApiConnection(String setServiceUri, String apiKey, String regularUserAuthorizationValue) {
        this.setServiceUri = setServiceUri;
        this.apiKey = apiKey;
        this.regularUserAuthorizationValue = regularUserAuthorizationValue;
    }

    public HttpConnection getHttpConnection() {
        return httpConnection;
    }


    /**
     * Fetches the get user set response (GET request)
     *
     * @param url
     * @param authorizationHeaderValue
     * @return
     * @throws SetApiClientException
     */
    protected UserSet getUserSetResponse(String url, String authorizationHeaderValue) throws SetApiClientException {
        try {
            LOGGER.trace("Call to Get UserSet API (GET) : {}.", url);
            return parseSetApiResponse(getHttpConnection().get(url, null, authorizationHeaderValue), new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_NOT_MODIFIED)));
        } catch (IOException | ParseException e) {
            throw new SetApiClientException(ERROR_MESSAGE + e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * Fetches the create user set response (GET request)
     *
     * @param url
     * @param authorizationHeaderValue
     * @return
     * @throws SetApiClientException
     */
    protected UserSet getCreateUserSetResponse(String url, String requestBody, String authorizationHeaderValue) throws SetApiClientException {
        try {
            LOGGER.trace("Call to Create UserSet API (POST) : {}.", url);
            return parseSetApiResponse(getHttpConnection().post(url, requestBody,"application/json",  authorizationHeaderValue), new ArrayList<>(Arrays.asList(HttpStatus.SC_CREATED)));
        } catch (IOException | ParseException e) {
            throw new SetApiClientException(ERROR_MESSAGE + e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * Fetches the User Set api response PUT update userset
     *
     * @param url                      url to be executed
     * @param requestBody              request body for POST and PUT request
     * @param authorizationHeaderValue authorisation value
     * @return
     * @throws SetApiClientException
     */
    protected UserSet getUpdateUserSetResponse(String url, String requestBody, String authorizationHeaderValue) throws SetApiClientException {
        try {
            LOGGER.trace("Call to Update UserSet API : {PUT}. {} ", url);
            return parseSetApiResponse(getHttpConnection().put(url, requestBody, authorizationHeaderValue), new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
        } catch (IOException | ParseException e) {
            throw new SetApiClientException(ERROR_MESSAGE + e.getMessage(),  HttpStatus.SC_INTERNAL_SERVER_ERROR, e);
        }
    }


    /**
     * This method makes DELETE request for given URL that returns
     * response headers and status code.
     *
     * @param url
     * @return The response headers and status code.
     * @throws IOException
     */
    protected String deleteURL(String url, String authorizationHeaderValue) throws SetApiClientException {
        try {
            LOGGER.trace("Call to UserSet API (DELETE): {} {} ", url, DELETE_URL_RESPONSE);
            CloseableHttpResponse response = getHttpConnection().deleteURL(url, authorizationHeaderValue);
            if (response.getCode() != HttpStatus.SC_NO_CONTENT) {
                String responseBody = EntityUtils.toString(response.getEntity());
                AbstractUserSetApiResponse errorResponse = mapper.readValue(responseBody, AbstractUserSetApiResponse.class);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(ERROR_MESSAGE + " {} ", errorResponse.getMessage());
                }
                throw new SetApiClientException(ERROR_MESSAGE + errorResponse.getMessage(), response.getCode());
            }
            return String.valueOf(response.getCode());
        } catch (IOException | ParseException e) {
            throw new SetApiClientException(ERROR_MESSAGE + e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    private UserSet parseSetApiResponse(CloseableHttpResponse response, List<Integer> statusToCheckList) throws SetApiClientException, IOException, ParseException {
        String responseBody = EntityUtils.toString(response.getEntity());
        if (statusToCheckList.contains(response.getCode())) {
            return mapper.readValue(responseBody, BaseUserSet.class);
        } else {
            AbstractUserSetApiResponse errorResponse = mapper.readValue(responseBody, AbstractUserSetApiResponse.class);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(ERROR_MESSAGE + " {} ", errorResponse.getMessage());
            }
            throw new SetApiClientException(ERROR_MESSAGE + errorResponse.getMessage(), response.getCode());
        }

    }

    /**
     * Fetches the user Set search api response
     *
     * Check profile for the deserialization
     * META - empty page (no items) ,  ITEMS_META - only set descriptions, not item descriptions , ITEMS - items set as set ids
     * ITEMS_META is the default profile value
     * @param url
     * @param authorizationHeaderValue
     * @return
     * @throws SetApiClientException
     */
    protected List< ? extends UserSet> getSearchUserSetResponse(String url, String authorizationHeaderValue, String profile) throws SetApiClientException {
        try {
            LOGGER.trace("Call to UserSet API (SEARCH): {} ", url);
            CloseableHttpResponse response = getHttpConnection().get(url, "application/json", authorizationHeaderValue);
            String responseBody = EntityUtils.toString(response.getEntity());
            if (response.getCode() == HttpStatus.SC_OK) {
               if (StringUtils.equals(profile, ProfileConstants.VALUE_PARAM_ITEMS)) {
                    TypeReference<ResultsPageImpl<String>> typeRef = new TypeReference<>() {};
                    List<String> setIds  = mapper.readValue(responseBody, typeRef).getItems();
                    List<UserSet> userSets = new ArrayList<>();
                   for (String id: setIds) {
                       BaseUserSet userSet = new BaseUserSet();
                       userSet.setIdentifier(StringUtils.substringAfterLast(id, "/"));
                       userSets.add(userSet);
                   }
                   return userSets;
                }
                TypeReference<ResultsPageImpl<BaseUserSet>> typeRef = new TypeReference<>() {};
                return mapper.readValue(responseBody, typeRef).getItems();

            } else {
                AbstractUserSetApiResponse errorResponse = mapper.readValue(responseBody, AbstractUserSetApiResponse.class);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(ERROR_MESSAGE + " {} ", errorResponse.getMessage());
                }
                throw new SetApiClientException(ERROR_MESSAGE + errorResponse.getMessage(), response.getCode());
            }
        } catch (IOException | ParseException e) {
            throw new SetApiClientException(ERROR_MESSAGE + e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * Fetches the Set api service url with "/" at the end
     */
    public StringBuilder getUserSetServiceUri() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(this.setServiceUri);
        if (!this.setServiceUri.endsWith(WebUserSetFields.SLASH))
            urlBuilder.append(WebUserSetFields.SLASH);

        return urlBuilder;
    }

    /**
     * Build get user Set get Url
     *
     * @param path
     * @param profile
     * @return
     */
    public static URI buildGetUrls(String path, String profile) {
        UriBuilder builder = UriBuilder.newInstance().path(path);
        if (profile != null) {
            builder.queryParam(QUERY_PARAM_PROFILE, profile);
        }
        return builder.build();
    }

    /**
     * Builds search url with the given params
     *
     * @param query
     * @param qf
     * @param sort
     * @param page
     * @param pageSize
     * @param facet
     * @param facetLimit
     * @param profile
     * @return search url
     */
    public static URI buildSearchUrl(String query, String[] qf, String sort, int page,
                                     int pageSize, String facet, int facetLimit,
                                     String profile) {
        UriBuilder builder = UriBuilder.newInstance().path(SEARCH_PATH)
                .queryParam(QUERY_PARAM_QUERY, query)
                .queryParam(QUERY_PARAM_PAGE, page)
                .queryParam(QUERY_PARAM_PAGE_SIZE, pageSize);
        if (qf != null) {
            builder.queryParam(QUERY_PARAM_QF, qf);
        }
        if (sort != null) {
            builder.queryParam(QUERY_PARAM_SORT, sort);
        }
        if (facet != null) {
            builder.queryParam(QUERY_PARAM_FACET, facet);
            builder.queryParam("facet.limit", facetLimit);
        }
        if (profile != null) {
            builder.queryParam(QUERY_PARAM_PROFILE, profile);
        }
        return builder.build();
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getSetServiceUri() {
        return setServiceUri;
    }
}