package eu.europeana.set.client.integration.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

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

		
//	@Test
	public void createUserSet() throws IOException {
		
		ResponseEntity<String> response = storeTestUserSet();

		validateResponse(response);
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
	
					
}
