package eu.europeana.api.set.integration.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import eu.europeana.api.set.integration.IntegrationTestSetup;
import eu.europeana.set.client.config.ClientConfiguration;
import eu.europeana.set.client.exception.SetApiClientException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import eu.europeana.set.client.UserSetApiClient;
import eu.europeana.set.definitions.model.UserSet;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseWebUserSetProtocol extends IntegrationTestSetup {

	protected Logger log = LogManager.getLogger(getClass());

	public static final String USER_SET_CONTENT = "/content/userset.json";
	public static final String USER_SET_UPDATE_CONTENT = "/content/usersetupdate.json";

	String START = "{";
	String END = "}";

	protected UserSetApiClient apiClient;

	@BeforeEach
	public void initObjects() throws SetApiClientException {
		apiClient = new UserSetApiClient(new ClientConfiguration());
	}

	/**
	 * This method creates test set object
	 * 
	 * @param resource JSON test file
	 * @parem profile
	 * @return response entity that contains response body, headers and status code.
	 * @throws IOException
	 */
	protected UserSet storeTestUserSet(String resource, String profile) throws SetApiClientException, IOException {
		String requestBody = getJsonStringInput(resource);
		return apiClient.getWebUserSetApi().createUserSet(requestBody, profile);
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
		String re = apiClient.getWebUserSetApi().deleteUserSet(identifier);
		assertEquals(String.valueOf(HttpStatus.SC_OK), re);
		log.trace("User set deleted: /" + identifier);
	}

	protected UserSet getUserSet(UserSet set) throws SetApiClientException {
		return apiClient.getWebUserSetApi().getUserSet(set.getIdentifier(), null);
	}
	
	
}
