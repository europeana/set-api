<<<<<<< HEAD
package eu.europeana.set.client.connection;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;

import eu.europeana.set.client.http.HttpConnection;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;

public class BaseApiConnection {

	private String apiKey;
	private String setServiceUri = "";
	private HttpConnection httpConnection = new HttpConnection();

	Logger logger = LogManager.getLogger(getClass().getName());

	public String getApiKey() {
		return apiKey;
	}

	public String getAdminApiKey() {
		return "apiadmin";
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
		logger.trace("Call to UserSet API (GET): " + url);
		return getHttpConnection().getURLContent(url);
	}
	
	String getJSONResult(String url, String paramName, String jsonPost) throws IOException {
		logger.trace("Call to UserSet API (POST): " + url);
		return getHttpConnection().getURLContent(url, paramName, jsonPost);
	}
	
	String getJSONResultWithBody(String url, String jsonPost) throws IOException {
		logger.trace("Call to UserSet API (POST) with body: " + url);
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
		logger.trace("Call to UserSet API (POST) with body: " + url + 
				". Returns body, headers and status code.");
		//System.out.println("post: " + url);
		return getHttpConnection().postURL(url, jsonPost);
	}
	
	
	/**
	 * This method makes PUT request for given URL and JSON body parameter that returns
	 * response body, response headers and status code.
	 * @param url
	 * @param jsonPost
	 * @return The response body, response headers and status code.
	 * @throws IOException
	 */
	ResponseEntity<String> putURL(String url, String jsonPut) throws IOException {
		logger.trace("Call to UserSet API (PUT) with body: " + url + 
				". Returns body, headers and status code.");
		
		ResponseEntity<String> response = getHttpConnection().putURL(url, jsonPut);
		
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
		logger.trace("Call to UserSet API (GET): " + url + 
				". Returns body, headers and status code.");
		return getHttpConnection().getURL(url);
	}
	
	
	/**
	 * This method makes DELETE request for given URL that returns
	 * response headers and status code.
	 * @param url
	 * @return The response headers and status code.
	 * @throws IOException
	 */
	ResponseEntity<String> deleteURL(String url) throws IOException {
		logger.trace("Call to UserSet API (DELETE): " + url + 
				". Returns headers and status code.");
		return getHttpConnection().deleteURL(url);
	}
	
}
=======
package eu.europeana.set.client.connection;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;

import eu.europeana.set.client.http.HttpConnection;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;

public class BaseApiConnection {

	private String apiKey;
	private String setServiceUri = "";
	private HttpConnection httpConnection = new HttpConnection();

	Logger logger = LogManager.getLogger(getClass().getName());

	public String getApiKey() {
		return apiKey;
	}

	public String getAdminApiKey() {
		return "apiadmin";
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
		logger.trace("Call to UserSet API (GET): " + url);
		return getHttpConnection().getURLContent(url);
	}
	
	String getJSONResult(String url, String paramName, String jsonPost) throws IOException {
		logger.trace("Call to UserSet API (POST): " + url);
		return getHttpConnection().getURLContent(url, paramName, jsonPost);
	}
	
	String getJSONResultWithBody(String url, String jsonPost) throws IOException {
		logger.trace("Call to UserSet API (POST) with body: " + url);
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
		logger.trace("Call to UserSet API (POST) with body: " + url + 
				". Returns body, headers and status code.");
		//System.out.println("post: " + url);
		return getHttpConnection().postURL(url, jsonPost);
	}
	
	
	/**
	 * This method makes PUT request for given URL and JSON body parameter that returns
	 * response body, response headers and status code.
	 * @param url
	 * @param jsonPost
	 * @return The response body, response headers and status code.
	 * @throws IOException
	 */
	ResponseEntity<String> putURL(String url, String jsonPut) throws IOException {
		logger.trace("Call to UserSet API (PUT) with body: " + url + 
				". Returns body, headers and status code.");
		
		ResponseEntity<String> response = getHttpConnection().putURL(url, jsonPut);
		
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
		logger.trace("Call to UserSet API (GET): " + url + 
				". Returns body, headers and status code.");
		return getHttpConnection().getURL(url);
	}
	
	
	/**
	 * This method makes DELETE request for given URL that returns
	 * response headers and status code.
	 * @param url
	 * @return The response headers and status code.
	 * @throws IOException
	 */
	ResponseEntity<String> deleteURL(String url) throws IOException {
		logger.trace("Call to UserSet API (DELETE): " + url + 
				". Returns headers and status code.");
		return getHttpConnection().deleteURL(url);
	}
	
}
>>>>>>> refs/remotes/origin/develop
