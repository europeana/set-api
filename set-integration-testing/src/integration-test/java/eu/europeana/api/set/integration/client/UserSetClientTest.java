package eu.europeana.api.set.integration.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.io.IOException;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import eu.europeana.set.client.exception.SetApiClientException;
import eu.europeana.set.definitions.model.UserSet;

/**
 * This class aims at testing of the annotation methods.
 * This is an integration test, and it is ignored for unit testing
 * @author GrafR
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class UserSetClientTest extends BaseUserSetClientTest {
    
  @LocalServerPort
  private int port;
    
  @BeforeAll
  void initObjects() throws SetApiClientException {
    initObjects(port);
  }

  @AfterAll
  void close() {
	  apiClient.close();
  }
  @Test
  public void createUserSet() throws SetApiClientException, IOException {
    String setId = storeTestUserSet(BaseUserSetClientTest.USER_SET_CONTENT, null);
	assertNotNull(setId);
	deleteCreatedSets();
  }

	/**
	 * This method creates and retrieves user set
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test
	public void retrieveUserSet() throws IllegalArgumentException, IOException, SetApiClientException {
		String testSetId = storeTestUserSet(BaseUserSetClientTest.USER_SET_CONTENT, null);
		assertNotNull(testSetId);
		// get user set by ID and user identifier
		UserSet userSet = apiClient.getWebUserSetApi().getUserSet(testSetId, null);
		assertNotNull(userSet);
		assertEquals(testSetId, userSet.getIdentifier());
		deleteCreatedSets();
	}

	@Test
	public void updateUserSet() throws IOException, SetApiClientException {
		String testSetId = storeTestUserSet(BaseUserSetClientTest.USER_SET_CONTENT, null);
		assertNotNull(testSetId);
		// updated user set value
		String requestBody = getJsonStringInput(BaseUserSetClientTest.USER_SET_UPDATE_CONTENT);
		assertNotNull(requestBody);
		// update user set by identifier URL
		UserSet updateResponse = apiClient.getWebUserSetApi().updateUserSet(
			testSetId, requestBody, null);
		assertNotNull(updateResponse);
		assertEquals(testSetId, updateResponse.getIdentifier());
		deleteCreatedSets();
	}

	@Test
	public void deleteUserSet() throws IOException, SetApiClientException {
		String testSetId = storeTestUserSet(BaseUserSetClientTest.USER_SET_CONTENT,null);
		assertNotNull(testSetId);
		// delete user set by identifier URL
		String deleteResponse = apiClient.getWebUserSetApi().deleteUserSet(testSetId);
		assertEquals(String.valueOf(HttpStatus.SC_NO_CONTENT), deleteResponse);
	}

}
