/*
 * HttpConnector.java - europeana4j
 * (C) 2011 Digibis S.L.
 */
package eu.europeana.set.common.http;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;
import java.io.IOException;


/**
 * The class encapsulating simple HTTP access.
 *
 * @author Srishti Singh
 * @since 26 November 2024
 */
public class HttpConnection {

    public static final String HEADER_AUTHORIZATION = "Authorization";
    private final CloseableHttpClient httpClient;

    public HttpConnection() {
        httpClient = HttpClientBuilder.create().build();
    }

	/**
	 *This method makes GET request for given URL.
	 * @param url
	 * @param acceptHeaderValue
	 * @param authorizationHeaderValue
	 * @return
	 * @throws IOException
	 */

	public CloseableHttpResponse get(String url, String acceptHeaderValue, String authorizationHeaderValue) throws IOException {
		HttpGet get = new HttpGet(url);
		addHeaders(get, HttpHeaders.ACCEPT, acceptHeaderValue);
		addHeaders(get, HEADER_AUTHORIZATION,authorizationHeaderValue);
		return executeHttpClient(get);
	}

    /**
     * This method makes POST request for given URL and JSON body parameter.
     *
     * @param url
     * @param requestBody
     * @param contentType
     * @return ResponseEntity that comprises response body in JSON format, headers
     * and status code.
     * @throws IOException
     */
    public CloseableHttpResponse post(String url, String requestBody, String contentType, String authorizationHeaderValue) throws IOException {
        HttpPost post = new HttpPost(url);
        addHeaders(post, HttpHeaders.CONTENT_TYPE, contentType);
        addHeaders(post, HEADER_AUTHORIZATION, authorizationHeaderValue);
        post.setEntity(new StringEntity(requestBody));
		return executeHttpClient(post);
	}



    /**
     * This method makes PUT request for given URL and JSON body parameter.
     *
     * @param url
     * @param jsonParamValue
     * @return ResponseEntity that comprises response body in JSON format, headers
     * and status code.
     * @throws IOException
     */
    public CloseableHttpResponse put(String url, String jsonParamValue, String authorizationHeaderValue) throws IOException {
		HttpPut put = new HttpPut(url);
		addHeaders(put, HEADER_AUTHORIZATION,authorizationHeaderValue);
		put.setEntity(new StringEntity(jsonParamValue));

		return executeHttpClient(put);

	}

    /**
     * This method makes DELETE request for given identifier URL.
     *
     * @param url                       The identifier URL
     * @param authorizationtHeaderValue
     * @return ResponseEntity that comprises response headers and status code.
     * @throws IOException
     */
    public CloseableHttpResponse deleteURL(String url, String authorizationtHeaderValue) throws IOException {
		HttpDelete delete = new HttpDelete(url);
		addHeaders(delete,HEADER_AUTHORIZATION, authorizationtHeaderValue);
		return executeHttpClient(delete);
	}


    private <T extends HttpUriRequestBase> CloseableHttpResponse executeHttpClient(T url) throws IOException {
		return httpClient.execute(url);

	}

	private <T extends HttpUriRequestBase> void addHeaders(T url, String headerName, String headerValue) {
		if (StringUtils.isNotBlank(headerValue)) {
			url.setHeader(headerName, headerValue);
		}
	}
}
