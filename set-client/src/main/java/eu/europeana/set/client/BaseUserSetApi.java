package eu.europeana.set.client;

import eu.europeana.set.client.config.ClientConfiguration;
import eu.europeana.set.client.connection.UserSetApiConnection;

/**
 * Base class for client API
 * @author GordeaS
 *
 */

public class BaseUserSetApi {

    private final ClientConfiguration configuration;
    protected final UserSetApiConnection apiConnection;
    
    protected BaseUserSetApi(ClientConfiguration configuration, UserSetApiConnection apiConnection) {
	this.configuration = configuration;
	this.apiConnection = apiConnection;
    }

    protected BaseUserSetApi() {
	this.configuration = ClientConfiguration.getInstance();
	this.apiConnection = new UserSetApiConnection(getConfiguration().getServiceUri(),
		getConfiguration().getApiKey());
    }

    public UserSetApiConnection getApiConnection() {
	return apiConnection;
    }

    public ClientConfiguration getConfiguration() {
	return configuration;
    }

}
