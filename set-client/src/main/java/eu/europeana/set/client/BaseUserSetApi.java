package eu.europeana.set.client;

import eu.europeana.set.client.config.ClientConfiguration;
import eu.europeana.set.client.connection.UserSetApiConnection;

public abstract class BaseUserSetApi {

	private final ClientConfiguration configuration;
	protected final UserSetApiConnection apiConnection;

	public BaseUserSetApi(ClientConfiguration configuration,
			UserSetApiConnection apiConnection) {
		this.configuration = configuration;
		this.apiConnection = apiConnection;
	}

	public BaseUserSetApi() {
		this.configuration = ClientConfiguration.getInstance();
		this.apiConnection = new UserSetApiConnection(
				getConfiguration().getServiceUri(), getConfiguration().getApiKey());
	}

	public UserSetApiConnection getApiConnection() {
		return apiConnection;
	}

	public ClientConfiguration getConfiguration() {
		return configuration;
	}

}
