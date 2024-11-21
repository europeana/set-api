package eu.europeana.set.client.connection;

import java.io.IOException;
import java.net.URI;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;

import eu.europeana.set.common.http.HttpConnection;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import org.springframework.web.util.UriBuilder;

import static eu.europeana.set.definitions.model.vocabulary.WebUserSetFields.FACETS;
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
	String regularUserAuthorizationValue = null;

	public BaseApiConnection(String setServiceUri, String apiKey, String regularUserAuthorizationValue) {
		this.setServiceUri = setServiceUri;
		this.apiKey = apiKey;
		this.regularUserAuthorizationValue = regularUserAuthorizationValue;
	}
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
         * @param requestHeaderValue
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

	public StringBuilder getUserSetServiceUri() {
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(this.setServiceUri);
		if(!this.setServiceUri.endsWith(WebUserSetFields.SLASH))
			urlBuilder.append(WebUserSetFields.SLASH);

		return urlBuilder;
	}

	public static Function<UriBuilder, URI> buildSearchUrl(String query, String[] qf, String sort, int page,
														   int pageSize, String facet, int facetLimit,
														   String profile) {
		return uriBuilder -> {
			UriBuilder builder =
					uriBuilder
							.path(SEARCH_PATH)
							//.queryParam(WSKEY, wskey)
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
		};
	}

	public String getApiKey() {
		return apiKey;
	}

	public String getSetServiceUri() {
		return setServiceUri;
	}
}