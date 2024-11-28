package eu.europeana.set.client.integration.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.europeana.set.client.exception.SetApiClientException;
import eu.europeana.set.definitions.model.UserSet;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * This class aims at testing of the annotation methods.
 * This is an integration test, and it is ignored for unit testing
 * @author GrafR
 */
@Disabled
public class WebUserSetProtocolTest extends BaseWebUserSetProtocol {

    private static final String USER_SET_PATH = "http://data.europeana.eu/set/";
		
    @Test
    public void createUserSet() throws SetApiClientException, IOException {
	String setId = createTestUserSet(USER_SET_CONTENT, null);
	assertNotNull(setId);
	apiClient.getWebUserSetApi().deleteUserSet(setId);
    }

	/**
	 * This method creates and retrieves user set
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test
	public void retrieveUserSet() throws IllegalArgumentException, IOException, SetApiClientException {
		String testSetId = createTestUserSet(USER_SET_CONTENT, null);
		assertNotNull(testSetId);
		// get user set by ID and user identifier
		UserSet userSet = apiClient.getWebUserSetApi().getUserSet(testSetId, null);
		assertNotNull(userSet);
		assertEquals(testSetId, userSet.getIdentifier());
	}

	@Test
	public void updateUserSet() throws IOException, SetApiClientException {
		String testSetId = createTestUserSet(USER_SET_CONTENT, null);
		assertNotNull(testSetId);
		// updated user set value
		String requestBody = getJsonStringInput(USER_SET_UPDATE_CONTENT);
		assertNotNull(requestBody);
		// update user set by identifier URL
		UserSet updateResponse = apiClient.getWebUserSetApi().updateUserSet(
			testSetId, requestBody, null);
		assertNotNull(updateResponse);
		assertEquals(testSetId, updateResponse.getIdentifier());
	}

	@Test
	public void deleteUserSet() throws IOException, SetApiClientException {
		String testSetId = createTestUserSet(USER_SET_CONTENT,null);
		assertNotNull(testSetId);
		// delete user set by identifier URL
		String deleteResponse = apiClient.getWebUserSetApi().deleteUserSet(testSetId);
		assertEquals(HttpStatus.SC_NO_CONTENT, deleteResponse);
	}

	/**
	 * This method creates test user set object
	 * @param content
	 * @param profile
	 * @return id of created user set
	 * @throws IOException
	 */
	private String createTestUserSet(String content, String profile) throws SetApiClientException, IOException {
		UserSet response = storeTestUserSet(content, profile);
		return response.getIdentifier();
	}
}
