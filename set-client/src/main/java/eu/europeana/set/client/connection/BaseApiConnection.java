package eu.europeana.set.client.connection;

import java.io.IOException;
import java.net.URI;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;

import eu.europeana.set.common.http.HttpConnection;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import static eu.europeana.set.definitions.model.vocabulary.WebUserSetFields.SEARCH_PATH;
import static eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants.*;

public class BaseApiConnection {

	Logger logger = LogManager.getLogger(getClass().getName());

	private static final String URL_RESPONSE        = " . Returns body, headers and status code.";
	private static final String DELETE_URL_RESPONSE = ". Returns headers and status code.";
	private static final String API_ADMIN_KEY       = "apiadmin";

	private HttpConnection httpConnection = new HttpConnection();
	private String apiKey;
	private String setServiceUri;
	String regularUserAuthorizationValue;

	public BaseApiConnection(String setServiceUri, String apiKey, String regularUserAuthorizationValue) {
		this.setServiceUri = setServiceUri;
		this.apiKey = apiKey;
		this.regularUserAuthorizationValue = regularUserAuthorizationValue;
	}

	// getters
	public String getAdminApiKey() {
		return API_ADMIN_KEY;
	}

	public HttpConnection getHttpConnection() {
		return httpConnection;
	}

	/**
	 * This method makes POST request for given URL, header and JSON body parameter that returns
	 * response body, response headers and status code.
	 * @param url
	 * @param jsonPost
	 * @param headerValue
	 * @return The response body, response headers and status code.
	 * @throws IOException
	 */
	ResponseEntity<String> postURL(String url, String jsonPost, String headerValue) throws IOException {
		logger.trace("Call to UserSet API (POST) with body: {}. Returns body, headers and status code.", url);
		return getHttpConnection().postURL(url, jsonPost, headerValue);
	}
	
	/**
	 * This method makes PUT request for given URL and JSON body parameter that returns
	 * response body, response headers and status code.
	 * @param url
	 * @param jsonPut
	 * @param authorizationHeaderValue
	 * @return The response body, response headers and status code.
	 * @throws IOException
	 */
	ResponseEntity<String> putURL(String url, String jsonPut, String authorizationHeaderValue) throws IOException {
		logger.trace("Call to UserSet API (PUT) with body: {}. Returns body, headers and status code.", url);
		
		ResponseEntity<String> response = getHttpConnection().putURL(url, jsonPut, authorizationHeaderValue);
		
		response.getStatusCode();
		
		return response;
	}
	
	/**
	 * This method makes GET request for given URL and returns
	 * response body, response headers and status code.
	 * @param url
	 * @return The response body, response headers and status code.
	 * @throws IOException
	 */
	public ResponseEntity<String> getURL(String url) throws IOException {
		logger.trace("Call to UserSet API (GET): {} {}", url, URL_RESPONSE);
		return getHttpConnection().getURL(url);
	}

	/**
	 * This method makes GET request for given URL and returns
	 * response body, response headers and status code.
	 * @param url
	 * @param authorizationHeaderValue
	 * @return The response body, response headers and status code.
	 * @throws IOException
	 */
	public ResponseEntity<String> getURL(String url, String authorizationHeaderValue) throws IOException {
		logger.trace("Call to UserSet API (GET): {}. Returns body, headers and status code.", url);
		return getHttpConnection().getURL(url, authorizationHeaderValue);
	}
	
	
	/**
	 * This method makes DELETE request for given URL that returns
	 * response headers and status code.
	 * @param url
	 * @return The response headers and status code.
	 * @throws IOException
	 */
	ResponseEntity<String> deleteURL(String url) throws IOException {
		logger.trace("Call to UserSet API (DELETE): {} {} ", url, DELETE_URL_RESPONSE);
		return getHttpConnection().deleteURL(url);
	}
	
	/**
	 * This method makes DELETE request for given URL that returns
	 * response headers and status code.
	 * @param url
	 * @param authorizationHeaderValue
	 * @return The response headers and status code.
	 * @throws IOException
	 */
	ResponseEntity<String> deleteURL(String url, String authorizationHeaderValue) throws IOException {
		logger.trace("Call to UserSet API (DELETE): {}. Returns headers and status code.", url);
		return getHttpConnection().deleteURL(url, authorizationHeaderValue);
	}

	/**
	 * Fetches the Set api service url with "/" at the end
	 */
	public StringBuilder getUserSetServiceUri() {
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(this.setServiceUri);
		if(!this.setServiceUri.endsWith(WebUserSetFields.SLASH))
			urlBuilder.append(WebUserSetFields.SLASH);

		return urlBuilder;
	}

	/**
	 * Build get user Set get Url
	 * @param path
	 * @param profile
	 * @return
	 */
	public static URI buildGetUrls(String path, String profile, String apikey) {
		UriBuilder builder = UriComponentsBuilder.newInstance().path(path)
				.queryParam(CommonApiConstants.PARAM_WSKEY, apikey); // by default pass apikey. If in case outh token is null apikey can be used to authenticate;
		if (profile != null) {
			builder.queryParam(QUERY_PARAM_PROFILE, profile);
		}
		return builder.build();
	}

	/**
	 * Builds search url with the given params
	 * @param query
	 * @param qf
	 * @param sort
	 * @param page
	 * @param pageSize
	 * @param facet
	 * @param facetLimit
	 * @param profile
	 * @return
	 */
	public static URI buildSearchUrl(String query, String[] qf, String sort, int page,
														   int pageSize, String facet, int facetLimit,
														   String profile, String apikey) {
		UriBuilder builder = UriComponentsBuilder.newInstance().path(SEARCH_PATH)
				.queryParam(QUERY_PARAM_QUERY, query)
				.queryParam(QUERY_PARAM_PAGE, page)
				.queryParam(QUERY_PARAM_PAGE_SIZE, pageSize)
				.queryParam(CommonApiConstants.PARAM_WSKEY, apikey); // by default pass apikey. If in case outh token is null apikey can be used to authenticate

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