package eu.europeana.set.client.connection;

import static eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants.QUERY_PARAM_FACET;
import static eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants.QUERY_PARAM_PAGE;
import static eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants.QUERY_PARAM_PAGE_SIZE;
import static eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants.QUERY_PARAM_PROFILE;
import static eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants.QUERY_PARAM_QF;
import static eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants.QUERY_PARAM_QUERY;
import static eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants.QUERY_PARAM_SORT;
import static eu.europeana.set.definitions.model.vocabulary.WebUserSetFields.PARAM_SORT_ORDER;
import static eu.europeana.set.definitions.model.vocabulary.WebUserSetFields.SEARCH_PATH;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import eu.europeana.api.commons.http.HttpConnection;
import eu.europeana.api.commons.auth.AuthenticationHandler;
import eu.europeana.api.commons.http.HttpResponseHandler;
import eu.europeana.api.commons_sb3.definitions.caching.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import eu.europeana.api.commons.definitions.search.result.impl.ResultsPageImpl;
import eu.europeana.set.client.exception.SetApiClientException;
import eu.europeana.set.client.json.AgentDeserializer;
import eu.europeana.set.client.json.UserSetDeserializer;
import eu.europeana.set.client.model.result.AbstractUserSetApiResponse;
import eu.europeana.set.client.model.result.RecordPreview;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.impl.BaseUserSet;
import eu.europeana.set.definitions.model.vocabulary.ProfileConstants;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;

public class BaseApiConnection {

    protected static final Logger LOGGER = LogManager.getLogger(BaseApiConnection.class);

    private static final String DELETE_URL_RESPONSE = ". Returns status code.";
    private static final String ERROR_MESSAGE       = "Set API Client call failed - ";

    private final HttpConnection httpConnection = new HttpConnection(true);
    private final ObjectMapper mapper = new ObjectMapper();

    private String                setServiceUri;
    private AuthenticationHandler authHandler;

    /**
     * BaseApiConnection constructor
     * @param setServiceUri set api service url
     * @param authHandler Authentication Handler for the client
     */
    public BaseApiConnection(String setServiceUri, AuthenticationHandler authHandler) {
        this.setServiceUri = setServiceUri;
        this.authHandler = authHandler;

        // set object mapper
        SimpleModule module = new SimpleModule();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        module.addDeserializer(Agent.class, new AgentDeserializer());
        module.addDeserializer(UserSet.class, new UserSetDeserializer());

        mapper.registerModule(module);
        mapper.findAndRegisterModules();
    }

    public HttpConnection getHttpConnection() {
        return httpConnection;
    }

    public AuthenticationHandler getAuthenticationHandler() {
        return this.authHandler;
    }

    public void setAuthenticationHandler(AuthenticationHandler auth) {
        this.authHandler = auth;
    }

    /**
     * Fetches the get user set response (GET request)
     *
     * If caching object is present, will add the caching headers in the requests
     *    1. If the ETag is present in the caching set the HTTP request header with If-None-Match.
     *    2. If the LastModified is present in the caching set the HTTP request header with If-Modified-Since
     *
     *    When receiving the response, if the response is a HTTP 200 then a User Set needs to be parsed and
     *    the ResourceCaching needs to be set with all the caching Response headers.
     *
     *    If the response is 304 nothing to be done
     *    For 404 throw an exception
     *
     * NOTE : Other 3xx response will follow the redirects as the getHttpConnection() is set to follow redirects.
     * @param url
     * @return
     * @throws SetApiClientException
     */
    protected Optional<UserSet> getUserSetResponse(String url, Optional<ResourceCaching> caching) throws SetApiClientException {
        try {
            LOGGER.trace("Call to Get UserSet API (GET) : {}.", url);
            HttpGet get = getHttpConnection().getHttpRequest(url, null, getAuthenticationHandler());
            ResourceCaching apiCaching = setCachingHeaders(caching, get);
            HttpResponseHandler response = getHttpConnection().executeHttpClient(get);
            String responseBody = response.getResponse();
            if (response.getStatus() == HttpStatus.SC_OK) {
                updateResourceCaching(response.getCachingHeaders(), apiCaching);
                return Optional.of(mapper.readValue(responseBody, UserSet.class));
            } else if (response.getStatus() == HttpStatus.SC_NOT_MODIFIED) {
               return Optional.empty();
            }
            else {
                AbstractUserSetApiResponse errorResponse = mapper.readValue(responseBody, AbstractUserSetApiResponse.class);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(ERROR_MESSAGE + " {} ", errorResponse.getMessage());
                }
                throw new SetApiClientException(ERROR_MESSAGE + errorResponse.getMessage(), response.getStatus());
            }
        } catch (IOException e) {
            throw new SetApiClientException(ERROR_MESSAGE + e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    private ResourceCaching setCachingHeaders(Optional<ResourceCaching> cachingOptional, HttpRequest get) {
        ResourceCaching apiCaching = new ResourceCaching();
        if (cachingOptional.isPresent()) {
            apiCaching = cachingOptional.get();
            if (apiCaching.getETag() != null) {
                get.setHeader(HttpHeaders.IF_NONE_MATCH, apiCaching.getETag());
            }
            if (apiCaching.getLastModified() != null) {
                get.setHeader(HttpHeaders.IF_MODIFIED_SINCE, apiCaching.getLastModified());
            }
        }
        return apiCaching;
    }

    /**
     * Update the resource caching with the header values received from the response
     * @param cachingHeaders Http response caching headers
     * @param apiCaching resource caching
     */
    private void updateResourceCaching(List<Header> cachingHeaders, ResourceCaching apiCaching) {
        for (Header h: cachingHeaders) {
            if (StringUtils.equals(h.getName(), CachingHeaders.ETAG)) {
                apiCaching.setETag(CachingUtils.parseETag(new WeakETag(h.getValue()).format()));
            }
            if (StringUtils.equals(h.getName(), CachingHeaders.LAST_MODIFIED)) {
                apiCaching.setLastModified(CachingUtils.getLastModified(Long.parseLong(h.getValue())));

            } if (StringUtils.equals(h.getName(), CachingHeaders.CACHE_CONTROL)) {
                apiCaching.setCacheControl(h.getValue());

            }
        }
    }


    /**
     * Fetches the create user set response (GET request)
     *
     * @param url
     * @return
     * @throws SetApiClientException
     */
    protected UserSet getCreateUserSetResponse(String url, String requestBody) throws SetApiClientException {
        try {
            LOGGER.trace("Call to Create UserSet API (POST) : {}.", url);
            return parseSetApiResponse(getHttpConnection().post(
                    url, requestBody, ContentType.APPLICATION_JSON.getMimeType(), getAuthenticationHandler()),
                    HttpStatus.SC_CREATED);
        }
        catch (IOException e) {
            throw new SetApiClientException(ERROR_MESSAGE + e.getMessage()
                                          , HttpStatus.SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * Fetches the User Set api response PUT update userset
     *
     * @param url                      url to be executed
     * @param requestBody              request body for POST and PUT request
     * @return
     * @throws SetApiClientException
     */
    protected UserSet getUpdateUserSetResponse(String url, String requestBody) 
              throws SetApiClientException {
        try {
            LOGGER.trace("Call to Update UserSet API : {PUT}. {} ", url);
            return parseSetApiResponse(getHttpConnection().put(url, requestBody, getAuthenticationHandler()),
                    HttpStatus.SC_OK);
        }
        catch (IOException e) {
            throw new SetApiClientException(ERROR_MESSAGE + e.getMessage()
                                         ,  HttpStatus.SC_INTERNAL_SERVER_ERROR, e);
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
    protected String deleteURL(String url) throws SetApiClientException {
        try {
            LOGGER.trace("Call to UserSet API (DELETE): {} {} ", url, DELETE_URL_RESPONSE);
            HttpResponseHandler response = getHttpConnection().deleteURL(url, getAuthenticationHandler());
            if (response.getStatus() != HttpStatus.SC_NO_CONTENT) {
                String responseBody = response.getResponse();
                AbstractUserSetApiResponse errorResponse = mapper.readValue(responseBody, AbstractUserSetApiResponse.class);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(ERROR_MESSAGE + " {} ", errorResponse.getMessage());
                }
                throw new SetApiClientException(ERROR_MESSAGE + errorResponse.getMessage(), response.getStatus());
            }
            return String.valueOf(response.getStatus());
        } catch (IOException e) {
            throw new SetApiClientException(ERROR_MESSAGE + e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    private UserSet parseSetApiResponse(HttpResponseHandler response, Integer statusToCheck) throws SetApiClientException, JsonProcessingException {
        String responseBody = response.getResponse();
        if (response.getStatus() == statusToCheck) {
            return mapper.readValue(responseBody, UserSet.class);
        } else {
            AbstractUserSetApiResponse errorResponse = mapper.readValue(responseBody, AbstractUserSetApiResponse.class);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(ERROR_MESSAGE + " {} ", errorResponse.getMessage());
            }
            throw new SetApiClientException(ERROR_MESSAGE + errorResponse.getMessage(), response.getStatus());
        }
    }

    /**
     * Fetches the get user Set pagination response
     *
     * Check profile for the deserialization
     * META - empty page (no items) ,  ITEMS_META - only set descriptions, not item descriptions , ITEMS - items set as set ids
     * ITEMS is the default profile value
     * @param url
     * @return
     * @throws SetApiClientException
     */
    protected List<RecordPreview> getUserSetPaginatedResponse(String url, String profile) throws SetApiClientException {
        try {
            LOGGER.trace("Call to Get UserSet API (Paginated): {} ", url);
            HttpResponseHandler response = getHttpConnection().get(url, ContentType.APPLICATION_JSON.getMimeType(),  getAuthenticationHandler());
            String responseBody = response.getResponse();
            if (response.getStatus() == HttpStatus.SC_OK) {
                TypeReference<ResultsPageImpl<RecordPreview>> typeRef = new TypeReference<>() {};
                return mapper.readValue(responseBody, typeRef).getItems();

            } else {
                AbstractUserSetApiResponse errorResponse = mapper.readValue(responseBody, AbstractUserSetApiResponse.class);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(ERROR_MESSAGE + " {} ", errorResponse.getMessage());
                }
                throw new SetApiClientException(ERROR_MESSAGE + errorResponse.getMessage(), response.getStatus());
            }
        } catch (IOException e) {
            throw new SetApiClientException(ERROR_MESSAGE + e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR, e);
        }
    }


    /**
     * Fetches the user Set search api response
     *
     * @param url
     * @return
     * @throws SetApiClientException
     */
    protected List<? extends UserSet> getSearchUserSetResponse(String url, String profile) throws SetApiClientException {
        try {
            LOGGER.trace("Call to UserSet API (SEARCH): {} ", url);
            HttpResponseHandler response = getHttpConnection().get(url, "application/json",  getAuthenticationHandler());
            String responseBody = response.getResponse();
            if (response.getStatus() == HttpStatus.SC_OK) {
                if (StringUtils.equals(profile, ProfileConstants.VALUE_PARAM_ITEMS)) {
                    TypeReference<ResultsPageImpl<String>> typeRef = new TypeReference<>() {};
                    List<String> items = mapper.readValue(responseBody, typeRef).getItems();
                    List<UserSet> sets = new ArrayList<>(items.size());
                    for (String id: items) {
                        UserSet set = new BaseUserSet();
                        set.setIdentifier(StringUtils.substringAfterLast(id, "/"));
                        sets.add(set);
                    }
                    return  sets;
                }
                TypeReference<ResultsPageImpl<UserSet>> typeRef = new TypeReference<>() {};
                return mapper.readValue(responseBody, typeRef).getItems();

            } else {
                AbstractUserSetApiResponse errorResponse = mapper.readValue(responseBody, AbstractUserSetApiResponse.class);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(ERROR_MESSAGE + " {} ", errorResponse.getMessage());
                }
                throw new SetApiClientException(ERROR_MESSAGE + errorResponse.getMessage(), response.getStatus());
            }
        } catch (IOException e) {
            throw new SetApiClientException(ERROR_MESSAGE + e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * Fetches the Set api service url with "/" at the end
     */
    public StringBuilder getUserSetServiceUri() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(this.setServiceUri);
        if (!this.setServiceUri.endsWith(String.valueOf(WebUserSetFields.SLASH)))
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
    public static URI buildGetUrls(String path, String profile) throws SetApiClientException{
        try {
            URIBuilder builder = new URIBuilder(path);
            if (profile != null) {
                builder.addParameter(QUERY_PARAM_PROFILE, profile);
            }
            return builder.build();
        } catch (URISyntaxException e) {
            throw  new SetApiClientException("Error creating Get url -  " + e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * Build paginated user set get url
     * @param path
     * @param sort
     * @param sortOrder
     * @param page
     * @param pageSize
     * @param profile
     * @return
     */
    public static URI buildPaginatedGetUrls(String path, String sort,
                                            String sortOrder, String page, String pageSize, String profile) throws SetApiClientException {
        try {
            URIBuilder builder = new URIBuilder(path)
                    .addParameter(QUERY_PARAM_PAGE, page)
                    .addParameter(QUERY_PARAM_PAGE_SIZE, pageSize);

            if (sort != null) {
                builder.addParameter(QUERY_PARAM_SORT, sort);
            }
            if (sortOrder != null) {
                builder.addParameter(PARAM_SORT_ORDER, sortOrder);
            }
            if (profile != null) {
                builder.addParameter(QUERY_PARAM_PROFILE, profile);
            }
            return builder.build();
        } catch (URISyntaxException e) {
            throw  new SetApiClientException("Error creating Paginated Get Urls " +e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR, e);
        }
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
    public static URI buildSearchUrl(String query, String[] qf, String sort, String page,
                                     String pageSize, String facet, int facetLimit,
                                     String profile) throws SetApiClientException {
        try {
            URIBuilder builder = new URIBuilder(SEARCH_PATH)
                    .addParameter(QUERY_PARAM_QUERY, query)
                    .addParameter(QUERY_PARAM_PAGE, page)
                    .addParameter(QUERY_PARAM_PAGE_SIZE, pageSize);

            if (qf != null) {
                builder.addParameter(QUERY_PARAM_QF, String.valueOf(qf));
            }
            if (sort != null) {
                builder.addParameter(QUERY_PARAM_SORT, sort);
            }
            if (facet != null) {
                builder.addParameter(QUERY_PARAM_FACET, facet);
                builder.addParameter("facet.limit", String.valueOf(facetLimit));
            }
            if (profile != null) {
                builder.addParameter(QUERY_PARAM_PROFILE, profile);
            }
            return builder.build();
        } catch (URISyntaxException e) {
            throw  new SetApiClientException("Error creating Search Urls ", HttpStatus.SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    public String getSetServiceUri() {
        return setServiceUri;
    }
}