package eu.europeana.api.set.integration.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.io.IOException;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import eu.europeana.set.client.exception.SetApiClientException;
import eu.europeana.set.definitions.model.UserSet;

/**
 * This class aims at testing of the annotation methods.
 * This is an integration test, and it is ignored for unit testing
 * @author GrafR
 */
public class WebUserSetProtocolTest extends BaseWebUserSetProtocol{

    @Test
    public void createUserSet() throws SetApiClientException, IOException {
	String setId = createTestUserSetForClient(BaseWebUserSetProtocol.USER_SET_CONTENT, null);
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
		String testSetId = createTestUserSetForClient(BaseWebUserSetProtocol.USER_SET_CONTENT, null);
		assertNotNull(testSetId);
		// get user set by ID and user identifier
		UserSet userSet = apiClient.getWebUserSetApi().getUserSet(testSetId, null);
		assertNotNull(userSet);
		assertEquals(testSetId, userSet.getIdentifier());
	}

	@Test
	public void updateUserSet() throws IOException, SetApiClientException {
		String testSetId = createTestUserSetForClient(BaseWebUserSetProtocol.USER_SET_CONTENT, null);
		assertNotNull(testSetId);
		// updated user set value
		String requestBody = getJsonStringInput(BaseWebUserSetProtocol.USER_SET_UPDATE_CONTENT);
		assertNotNull(requestBody);
		// update user set by identifier URL
		UserSet updateResponse = apiClient.getWebUserSetApi().updateUserSet(
			testSetId, requestBody, null);
		assertNotNull(updateResponse);
		assertEquals(testSetId, updateResponse.getIdentifier());
	}

	@Test
	public void deleteUserSet() throws IOException, SetApiClientException {
		String testSetId = createTestUserSetForClient(BaseWebUserSetProtocol.USER_SET_CONTENT,null);
		assertNotNull(testSetId);
		// delete user set by identifier URL
		String deleteResponse = apiClient.getWebUserSetApi().deleteUserSet(testSetId);
		assertEquals(String.valueOf(HttpStatus.SC_NO_CONTENT), deleteResponse);
	}

	/**
	 * This method creates test user set object
	 * @param content
	 * @param profile
	 * @return id of created user set
	 * @throws IOException
	 */
	private String createTestUserSetForClient(String content, String profile) throws SetApiClientException, IOException {
		UserSet response = storeTestUserSet(content, profile);
		return response.getIdentifier();
	}
}
