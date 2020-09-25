/*
 * HttpConnector.java - europeana4j
 * (C) 2011 Digibis S.L.
 */
package eu.europeana.api.set.integration.connection.http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * The class encapsulating simple HTTP access.
 *
 * @author GrafR
 */
public class HttpConnection {

    private static final int CONNECTION_RETRIES = 3;
    private static final int TIMEOUT_CONNECTION = 40000;
    private HttpClient httpClient = null;

    /**
     * This method makes POST request for given URL and JSON body parameter.
     *
     * @param url
     * @param requestBody
     * @param contentType
     * @return ResponseEntity that comprises response body in JSON format, headers
     *         and status code.
     * @throws IOException
     */
    @SuppressWarnings("deprecation")
    public ResponseEntity<String> post(String url, String requestBody, String contentType) throws IOException {
	HttpClient client = this.getHttpClient(CONNECTION_RETRIES, TIMEOUT_CONNECTION);
	PostMethod post = new PostMethod(url);
	post.setRequestHeader(HttpHeaders.CONTENT_TYPE, contentType);
	post.setRequestBody(requestBody);
	
	try {
	    client.executeMethod(post);
	    return buildResponseEntity(post);
	} finally {
	    post.releaseConnection();
	}
    }
    
    
    
      /**
     * This method builds a response entity that comprises response body, headers
     * and status code for the passed HTTP method
     *
     * @param method The HTTP method (e.g. post, put, delete or get)
     * @return response entity
     * @throws IOException
     */
    private ResponseEntity<String> buildResponseEntity(HttpMethod method) throws IOException {

	MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(15);
	for (Header header : method.getResponseHeaders())
	    headers.add(header.getName(), header.getValue());

	String res = null;
	if (method.getResponseBody() != null && method.getResponseBody().length > 0) {
	    byte[] byteResponse = method.getResponseBody();
	    res = new String(byteResponse, StandardCharsets.UTF_8);
	}
	return new ResponseEntity<>(res, headers, HttpStatus.valueOf(method.getStatusCode()));
    }

 
    private HttpClient getHttpClient(int connectionRetry, int conectionTimeout) {
	if (this.httpClient == null) {
	    HttpClient client = new HttpClient();

	    // configure retry handler
	    client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
		    new DefaultHttpMethodRetryHandler(connectionRetry, false));

	    // when using a http proxy
	    String proxyHost = System.getProperty("http.proxyHost");
	    if ((proxyHost != null) && (proxyHost.length() > 0)) {
		String proxyPortSrt = System.getProperty("http.proxyPort");
		if (proxyPortSrt == null) {
		    proxyPortSrt = "8080";
		}
		int proxyPort = Integer.parseInt(proxyPortSrt);

		client.getHostConfiguration().setProxy(proxyHost, proxyPort);
	    }

	    // configure timeouts
	    boolean bTimeout = false;
	    String connectTimeOut = System.getProperty("sun.net.client.defaultConnectTimeout");
	    if ((connectTimeOut != null) && (connectTimeOut.length() > 0)) {
		client.getParams().setIntParameter("sun.net.client.defaultConnectTimeout",
			Integer.parseInt(connectTimeOut));
		bTimeout = true;
	    }
	    String readTimeOut = System.getProperty("sun.net.client.defaultReadTimeout");
	    if ((readTimeOut != null) && (readTimeOut.length() > 0)) {
		client.getParams().setIntParameter("sun.net.client.defaultReadTimeout", Integer.parseInt(readTimeOut));
		bTimeout = true;
	    }
	    if (!bTimeout) {
		client.getParams().setIntParameter(HttpMethodParams.SO_TIMEOUT, conectionTimeout);
	    }

	    this.httpClient = client;
	}
	return this.httpClient;
    }
}
