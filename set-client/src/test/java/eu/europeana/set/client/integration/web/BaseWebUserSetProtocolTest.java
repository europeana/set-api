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

import eu.europeana.set.client.config.ClientConfiguration;
import eu.europeana.set.client.web.WebUserSetApi;
import eu.europeana.set.client.web.WebUserSetApiImpl;
import eu.europeana.set.definitions.model.UserSet;

public class BaseWebUserSetProtocolTest {

	protected Logger log = LogManager.getLogger(getClass());

	public static final String USER_SET_CONTENT = "/content/userset.json";

	String START = "{";
	String END = "}";

	String BODY_VALUE_TO_UPDATE = "\"title\": {" + "\"en\": \"Sport\"}," 
			+ "\"description\": {" + "\"en\": \"Best sport\"}";

	public String USER_SET_UPDATE_BODY_JSON = START + BODY_VALUE_TO_UPDATE + END;

	public String TEST_USER_TOKEN = "tester1";
	public String ADMIN_USER_TOKEN = "admin";
	public String ANONYMOUS_USER_TOKEN = "anonymous";
	public String TEST_SET_ID = "118";

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
	 * @return response entity that contains response body, headers and status code.
	 * @throws IOException
	 */
	protected ResponseEntity<String> storeTestUserSet() throws IOException {

		String requestBody = getJsonStringInput(USER_SET_CONTENT);

		/**
		 * store set
		 */
		ResponseEntity<String> storedResponse = getApiClient().createUserSet(getApiKey(), requestBody, TEST_USER_TOKEN);
		return storedResponse;
	}

	public String getApiKey() {

		return ClientConfiguration.getInstance().getApiKey();
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
		ResponseEntity<String> re = webUserSetApi.deleteUserSet(getApiKey(), identifier, TEST_USER_TOKEN);
		assertEquals(HttpStatus.OK, re.getStatusCode());
		log.trace("User set deleted: /" + identifier);
	}

	protected ResponseEntity<String> getUserSet(UserSet set) {
		return getApiClient().getUserSet(getApiKey(), set.getIdentifier(), TEST_USER_TOKEN);
	}
	
	
}
