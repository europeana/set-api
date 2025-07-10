/*
 * HttpConnector.java - europeana4j
 * (C) 2011 Digibis S.L.
 */
package eu.europeana.set.common.http;

import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;


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
	 * @return HttpResponseHandler that comprises response body as String and status code.
	 * @throws IOException
	 */

	public HttpResponseHandler get(String url, String acceptHeaderValue, String authorizationHeaderValue) throws IOException {
		HttpGet get = new HttpGet(url);
		if(StringUtils.isNotBlank(acceptHeaderValue)) {
		  addHeaders(get, HttpHeaders.ACCEPT, acceptHeaderValue);
		}
		if(StringUtils.isNotBlank(authorizationHeaderValue)) {
		  addHeaders(get, HEADER_AUTHORIZATION,authorizationHeaderValue);
		}
		return executeHttpClient(get);
	}

    /**
     * This method makes POST request for given URL and JSON body parameter.
     *
     * @param url
     * @param requestBody
     * @param contentType
     * @return HttpResponseHandler that comprises response body as String and status code.
     * @throws IOException
     */
    public HttpResponseHandler post(String url, String requestBody, String contentType, String authorizationHeaderValue) throws IOException {
        HttpPost post = new HttpPost(url);
        if(StringUtils.isNotBlank(contentType)) {
          addHeaders(post, HttpHeaders.CONTENT_TYPE, contentType);
        }
        if(StringUtils.isNotBlank(authorizationHeaderValue)) {
          addHeaders(post, HEADER_AUTHORIZATION, authorizationHeaderValue);
        }
        if(requestBody!=null) {
          post.setEntity(new StringEntity(requestBody));
        }
		return executeHttpClient(post);
	}



    /**
     * This method makes PUT request for given URL and JSON body parameter.
     *
     * @param url
     * @param jsonParamValue
     * @return HttpResponseHandler that comprises response body as String and status code.
     * @throws IOException
     */
    public HttpResponseHandler put(String url, String jsonParamValue, String authorizationHeaderValue) throws IOException {
		HttpPut put = new HttpPut(url);
		if(StringUtils.isNotBlank(authorizationHeaderValue)) {
		  addHeaders(put, HEADER_AUTHORIZATION,authorizationHeaderValue);
		}
		put.setEntity(new StringEntity(jsonParamValue));

		return executeHttpClient(put);

	}

    /**
     * This method makes DELETE request for given identifier URL.
     *
     * @param url                       The identifier URL
     * @param authorizationtHeaderValue
     * @return HttpResponseHandler that comprises response body as String and status code.
     * @throws IOException
     */
    public HttpResponseHandler deleteURL(String url, String authorizationtHeaderValue) throws IOException {
		HttpDelete delete = new HttpDelete(url);
		if(StringUtils.isNotBlank(authorizationtHeaderValue)) {
		  addHeaders(delete,HEADER_AUTHORIZATION, authorizationtHeaderValue);
		}
		return executeHttpClient(delete);
	}


    private <T extends HttpUriRequestBase> HttpResponseHandler executeHttpClient(ClassicHttpRequest request) throws IOException {
      HttpResponseHandler responseHandler = new HttpResponseHandler();      
      httpClient.execute(request, responseHandler); 
      return responseHandler;
	}

	private <T extends HttpUriRequestBase> void addHeaders(ClassicHttpRequest request, String headerName, String headerValue) {
		if (StringUtils.isNotBlank(headerValue)) {
			request.setHeader(headerName, headerValue);
		}
	}
}
