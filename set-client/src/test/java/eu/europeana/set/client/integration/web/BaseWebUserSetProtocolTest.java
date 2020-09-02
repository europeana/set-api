package eu.europeana.set.client.integration.web;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import eu.europeana.set.client.web.WebUserSetApi;
import eu.europeana.set.client.web.WebUserSetApiImpl;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;

public class BaseWebUserSetProtocolTest {

	protected Logger log = LogManager.getLogger(getClass());

	public static final String USER_SET_CONTENT = "/content/userset.json";
	public static final String USER_SET_UPDATE_CONTENT = "/content/usersetupdate.json";

	String START = "{";
	String END = "}";

	private WebUserSetApi apiClient;

	@Before
	public void initObjects() {
		apiClient = new WebUserSetApiImpl();
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

	/**
	 * @param user set
	 */
	protected void deleteUserSet(UserSet set) {
		deleteUserSet(set.getIdentifier());
	}

	protected void deleteUserSet(String identifier) {
		WebUserSetApi webUserSetApi = new WebUserSetApiImpl();
		ResponseEntity<String> re = webUserSetApi.deleteUserSet(identifier);
		assertEquals(HttpStatus.OK, re.getStatusCode());
		log.trace("User set deleted: /" + identifier);
	}

	protected ResponseEntity<String> getUserSet(UserSet set) {
		return getApiClient().getUserSet(set.getIdentifier(), LdProfiles.MINIMAL.name());
	}
	
	
}
