package eu.europeana.set.client.integration.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


/**
 * This class aims at testing of the annotation methods.
 * This is an integration test, and it is ignored for unit testing
 * @author GrafR
 */
@Ignore
public class WebUserSetProtocolTest extends BaseWebUserSetProtocolTest { 

	private static final String USER_SET_PATH = "http://data.europeana.eu/set/";
		
//	@Test
	public void createUserSet() throws IOException {
		
		ResponseEntity<String> response = storeTestUserSet();

		validateResponse(response);
	}

    /**
     * This method matches response body to get user set ID
     * e.g. ID 134 from "http://data.europeana.eu/set/134"
     * @param body The response body
     * @return The matched user set ID
     */
    public String matchSetId(String body) {
    	String res = "";
    	log.info("body: " + body);
      	String regex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]\\b";
	    Pattern pat = Pattern.compile(regex);
	    Matcher mat = pat.matcher(body);

	    while (mat.find()) {
	    	res = mat.group();
	      	if (res.contains(USER_SET_PATH)) {
	      		return res.replaceAll(USER_SET_PATH, "");
	      	}
	    }
	    
	    return res;
	  }

	protected void validateResponse(ResponseEntity<String> response) {
		validateResponse(response, HttpStatus.CREATED);
	}
	
	protected void validateResponse(ResponseEntity<String> response, HttpStatus status) {
		assertNotNull(response.getBody());
		assertEquals(response.getStatusCode(), status);
	}
	
	@Test
	public void getUserSet() throws IllegalAccessException, IllegalArgumentException
		, InvocationTargetException, IOException {
		
		/**
		 * get user set by ID and user identifier
		 */
		ResponseEntity<String> response = getApiClient().getUserSet(
				getApiKey()
				, TEST_SET_ID
				, TEST_USER_TOKEN
				);		
		validateResponse(response, HttpStatus.OK);
	}
	
					
	/**
	 * This method creates and retrieves user set 
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IOException
	 */
	@Test
	public void retrieveUserSet() throws IllegalAccessException, IllegalArgumentException
		, InvocationTargetException, IOException {
		
		ResponseEntity<String> response = storeTestUserSet();

		validateResponse(response);
		
		String testSetId = matchSetId(response.getBody().toString());

		/**
		 * get user set by ID and user identifier
		 */
		response = getApiClient().getUserSet(
				getApiKey()
				, testSetId
				, TEST_USER_TOKEN
				);		
		log.info(response.toString());
		validateResponse(response, HttpStatus.OK);
	}
	
					
}
