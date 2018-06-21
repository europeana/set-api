package eu.europeana.set.client.integration.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


/**
 * This class aims at testing of the annotation methods.
 * @author GrafR
 */
public class WebUserSetProtocolTest extends BaseWebUserSetProtocolTest { 

		
	@Test
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
	
//	@Test
	public void getUserSet() throws IllegalAccessException, IllegalArgumentException
		, InvocationTargetException, IOException {
		
//		ResponseEntity<String> createResponse = storeTestUserSet(); 

		/**
		 * get annotation by provider and identifier
		 */
		ResponseEntity<String> response = getApiClient().getUserSet(
				getApiKey()
				, "58"
				, TEST_USER_TOKEN
				);		
		validateResponse(response, HttpStatus.OK);
	}
	
					
}
