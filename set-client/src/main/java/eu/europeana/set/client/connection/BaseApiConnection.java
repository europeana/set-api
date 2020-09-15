package eu.europeana.set.client.connection;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;

import eu.europeana.set.client.http.HttpConnection;
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
	
//	public Gson getUserSetGson() {
//		if (gson == null) {
//			// Load results object from JSON
//			GsonBuilder builder = new GsonBuilder();
//			UserSetDeserializer annoDeserializer = new UserSetDeserializer();
//			
//			builder.registerTypeHierarchyAdapter(UserSet.class,
//					annoDeserializer);
//			
//			gson = builder.setDateFormat(ModelConst.GSON_DATE_FORMAT).create();
//		}
//		return gson;
//	}

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
	
	String getJSONResult(String url) throws IOException {
		logger.trace("Call to UserSet API (GET): {} ", url);
		return getHttpConnection().getURLContent(url);
	}
	
	String getJSONResult(String url, String paramName, String jsonPost) throws IOException {
		logger.trace("Call to UserSet API (POST): {} ", url);
		return getHttpConnection().getURLContent(url, paramName, jsonPost);
	}
	
	String getJSONResultWithBody(String url, String jsonPost) throws IOException {
		logger.trace("Call to UserSet API (POST) with body: {} ", url);
		return getHttpConnection().getURLContentWithBody(url, jsonPost);
	}

	/**
	 * This method makes POST request for given URL and JSON body parameter that returns
	 * response body, response headers and status code.
	 * @param url
	 * @param jsonPost
	 * @return The response body, response headers and status code.
	 * @throws IOException
	 */
	ResponseEntity<String> postURL(String url, String jsonPost) throws IOException {
		logger.trace("Call to UserSet API (POST) with body: {} {}", url , URL_RESPONSE);
		return getHttpConnection().postURL(url, jsonPost);
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
		logger.trace("Call to UserSet API (POST) with body: " + url + 
				". Returns body, headers and status code.");
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
		logger.trace("Call to UserSet API (PUT) with body: " + url + 
				". Returns body, headers and status code.");
		
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
		logger.trace("Call to UserSet API (GET): " + url + 
				". Returns body, headers and status code.");
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
		logger.trace("Call to UserSet API (DELETE): " + url + 
				". Returns headers and status code.");
		return getHttpConnection().deleteURL(url, authorizationHeaderValue);
	}
		
}