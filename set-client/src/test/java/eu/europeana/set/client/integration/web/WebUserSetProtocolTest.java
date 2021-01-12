package eu.europeana.set.client.integration.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import eu.europeana.set.definitions.model.vocabulary.LdProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class aims at testing of the annotation methods.
 * This is an integration test, and it is ignored for unit testing
 * @author GrafR
 */
public class WebUserSetProtocolTest extends BaseWebUserSetProtocol {

    private static final String USER_SET_PATH = "http://data.europeana.eu/set/";
		
    @Test
    public void createUserSet() throws IOException {
	String setId = createTestUserSet(USER_SET_CONTENT, LdProfiles.MINIMAL.name());
	assertNotNull(setId);
    }

	/**
	 * This method creates and retrieves user set 
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test
	public void retrieveUserSet() throws IllegalArgumentException, IOException {
		ResponseEntity<String> response;
		String testSetId = createTestUserSet(USER_SET_CONTENT, LdProfiles.MINIMAL.name());
		assertNotNull(testSetId);
		// get user set by ID and user identifier
		response = getApiClient().getUserSet(testSetId, LdProfiles.MINIMAL.name());
		validateResponse(response, HttpStatus.OK);
	}
	
	@Test
	public void updateUserSet() throws IOException {
		String testSetId = createTestUserSet(USER_SET_CONTENT, LdProfiles.MINIMAL.name());
		assertNotNull(testSetId);
		// updated user set value
		String requestBody = getJsonStringInput(USER_SET_UPDATE_CONTENT);
		assertNotNull(requestBody);
		// update user set by identifier URL
		ResponseEntity<String> updateResponse = getApiClient().updateUserSet(
			testSetId, requestBody, LdProfiles.STANDARD.name());
		validateResponse(updateResponse, HttpStatus.OK);
	}
	
	@Test
	public void deleteUserSet() throws IOException {
		String testSetId = createTestUserSet(USER_SET_CONTENT, LdProfiles.MINIMAL.name());
		assertNotNull(testSetId);
		// delete user set by identifier URL
		ResponseEntity<String> deleteResponse = getApiClient().deleteUserSet(
			testSetId);
		assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());
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
		validateResponse(response, HttpStatus.CREATED);
		String testSetId = matchSetId(response.getBody());
		return testSetId;
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

	protected void validateResponse(ResponseEntity<String> response, HttpStatus expectedStatus) {
		assertNotNull(response.getBody());
		assertEquals(expectedStatus, response.getStatusCode());
	}
}
