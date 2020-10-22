package eu.europeana.set.client.integration.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.europeana.set.definitions.model.vocabulary.LdProfiles;


/**
 * This class aims at testing of the annotation methods.
 * This is an integration test, and it is ignored for unit testing
 * @author GrafR
 */
@Disabled
public class WebUserSetProtocolTest extends BaseWebUserSetProtocol {

    private static final String USER_SET_PATH = "http://data.europeana.eu/set/";
		
    @Test
    public void createUserSet() throws IOException {
		
	createTestUserSet(USER_SET_CONTENT, LdProfiles.MINIMAL.name());
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
	
	protected void validateResponse(ResponseEntity<String> response, HttpStatus expectedStatus) {
		assertNotNull(response.getBody());
		assertEquals(expectedStatus, response.getStatusCode());
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
		
		ResponseEntity<String> response;
		String testSetId = createTestUserSet(USER_SET_CONTENT, LdProfiles.MINIMAL.name());

		/**
		 * get user set by ID and user identifier
		 */
		response = getApiClient().getUserSet(
				testSetId, LdProfiles.MINIMAL.name());		
		log.info(response.toString());
		validateResponse(response, HttpStatus.OK);
	}

	/**
	 * This method creates test user set object
	 * @param content
	 * @param profile
	 * @return id of created user set
	 * @throws IOException
	 */
	private String createTestUserSet(String content, String profile) throws IOException {
	    ResponseEntity<String> response = storeTestUserSet(content, profile);

	    validateResponse(response);
	    
	    String testSetId = matchSetId(response.getBody().toString());
	    return testSetId;
	}
	
	@Test
	public void updateUserSet() throws IOException {
				
		String testSetId = createTestUserSet(USER_SET_CONTENT, LdProfiles.MINIMAL.name());

		// updated user set value
		String requestBody = getJsonStringInput(USER_SET_UPDATE_CONTENT);
				
                // update user set by identifier URL
		ResponseEntity<String> updateResponse = getApiClient().updateUserSet(
			testSetId, requestBody, LdProfiles.STANDARD.name());
		
		assertEquals( HttpStatus.OK, updateResponse.getStatusCode());
	}
	
	@Test
	public void deleteUserSet() throws IOException {
				
		String testSetId = createTestUserSet(USER_SET_CONTENT, LdProfiles.MINIMAL.name());

		// delete user set by identifier URL
		ResponseEntity<String> deleteResponse = getApiClient().deleteUserSet(
			testSetId);
		
		log.debug("Response body: " + deleteResponse.getBody());
		if(!HttpStatus.NO_CONTENT.equals(deleteResponse.getStatusCode()))
			log.error("Wrong status code: " + deleteResponse.getStatusCode());
		assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());
	}

}
