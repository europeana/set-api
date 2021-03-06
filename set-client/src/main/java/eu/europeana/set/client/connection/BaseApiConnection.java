package eu.europeana.set.client.connection;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;

import eu.europeana.set.common.http.HttpConnection;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;

public class BaseApiConnection {

	Logger logger = LogManager.getLogger(getClass().getName());

	private static final String URL_RESPONSE        = " . Returns body, headers and status code.";
	private static final String DELETE_URL_RESPONSE = ". Returns headers and status code.";
	private static final String API_ADMIN_KEY       = "apiadmin";

	private String apiKey;
	private String setServiceUri = "";
	private HttpConnection httpConnection = new HttpConnection();


	public String getApiKey() {
		return apiKey;
	}

	public String getAdminApiKey() {
		return API_ADMIN_KEY;
	}
	
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public StringBuilder getUserSetServiceUri() {
		StringBuilder urlBuilder = new StringBuilder();	
		urlBuilder.append(setServiceUri);		
		
		if(!setServiceUri.endsWith(WebUserSetFields.SLASH))
			urlBuilder.append(WebUserSetFields.SLASH);
		
		return urlBuilder;
	}

	public void setUserSetServiceUri(String setServiceUri) {
		this.setServiceUri = setServiceUri;
	}

	public HttpConnection getHttpConnection() {
		return httpConnection;
	}

	public void setHttpConnection(HttpConnection httpConnection) {
		this.httpConnection = httpConnection;
	}
	

	/**
	 * Create a new connection to the UserSet Service (REST API).
	 * 
	 * @param apiKey
	 *            API Key required to access the API
	 */
	public BaseApiConnection(String setServiceUri, String apiKey) {
		this.apiKey = apiKey;
		this.setServiceUri = setServiceUri;
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
		
}