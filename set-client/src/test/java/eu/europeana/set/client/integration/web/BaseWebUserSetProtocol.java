package eu.europeana.set.client.integration.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import eu.europeana.set.client.config.ClientConfiguration;
import eu.europeana.set.client.exception.SetApiClientException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import eu.europeana.set.client.web.WebUserSetApi;
import eu.europeana.set.client.UserSetApiClient;
import eu.europeana.set.definitions.model.UserSet;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseWebUserSetProtocol {

	protected Logger log = LogManager.getLogger(getClass());

	public static final String USER_SET_CONTENT = "/content/userset.json";
	public static final String USER_SET_UPDATE_CONTENT = "/content/usersetupdate.json";

	String START = "{";
	String END = "}";

	private WebUserSetApi apiClient;

	@BeforeEach
	public void initObjects() throws SetApiClientException {
		apiClient = new UserSetApiClient(new ClientConfiguration());
	}

	public WebUserSetApi getApiClient() {
		return apiClient;
	}

	/**
	 * This method creates test set object
	 * 
	 * @param resource JSON test file
	 * @parem profile
	 * @return response entity that contains response body, headers and status code.
	 * @throws IOException
	 */
	protected ResponseEntity<String> storeTestUserSet(String resource, String profile) throws IOException {

		String requestBody = getJsonStringInput(resource);

		/**
		 * store set
		 */
		ResponseEntity<String> storedResponse = getApiClient().createUserSet(requestBody, profile);
		return storedResponse;
	}

	protected String getJsonStringInput(String resource) throws IOException {
		InputStream resourceAsStream = getClass().getResourceAsStream(resource);

		StringBuilder out = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream));
		for (String line = br.readLine(); line != null; line = br.readLine())
			out.append(line);
		br.close();
		return out.toString();

	}

	protected void deleteUserSet(UserSet set) throws SetApiClientException {
		deleteUserSet(set.getIdentifier());
	}

	protected void deleteUserSet(String identifier) throws SetApiClientException {
		WebUserSetApi webUserSetApi = new UserSetApiClient(new ClientConfiguration());
		ResponseEntity<String> re = webUserSetApi.deleteUserSet(identifier);
		assertEquals(HttpStatus.OK, re.getStatusCode());
		log.trace("User set deleted: /" + identifier);
	}

	protected ResponseEntity<String> getUserSet(UserSet set) {
		return getApiClient().getUserSet(set.getIdentifier(), null);
	}
	
	
}
