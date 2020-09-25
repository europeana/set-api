package eu.europeana.api.set.integration.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import eu.europeana.api.set.integration.config.SetIntegrationConfiguration;

public class BaseUserSetApi {

	private final SetIntegrationConfiguration configuration;
	protected final UserSetApiConnection apiConnection;

	public BaseUserSetApi(SetIntegrationConfiguration configuration,
			UserSetApiConnection apiConnection) {
		this.configuration = configuration;
		this.apiConnection = apiConnection;
	}

	public BaseUserSetApi() {
		this.configuration = SetIntegrationConfiguration.getInstance();
		this.apiConnection = new UserSetApiConnection(
				getConfiguration().getServiceUri(), getConfiguration().getApiKey());
	}

	public UserSetApiConnection getApiConnection() {
		return apiConnection;
	}

	public SetIntegrationConfiguration getConfiguration() {
		return configuration;
	}
	
	/**
	 * This method loads JSON data from a file
	 * @param resource
	 * @return in string format
	 * @throws IOException
	 */
	public String getJsonStringInput(String resource) throws IOException {
		InputStream resourceAsStream = getClass().getResourceAsStream(resource);

		StringBuilder out = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream));
		for (String line = br.readLine(); line != null; line = br.readLine())
			out.append(line);
		br.close();
		return out.toString();
	}	

}
