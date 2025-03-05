package eu.europeana.set.client;

import eu.europeana.set.client.config.ClientConfiguration;
import eu.europeana.set.client.connection.UserSetApiConnection;
import eu.europeana.set.client.exception.SetApiClientException;

/**
 * Base class for client API
 * @author GordeaS
 *
 */

public class BaseUserSetApi {

    private final ClientConfiguration configuration;
    private UserSetApiConnection apiConnection;

    /**
     * Creates BaseUserSetApi instance with client configuration
     * This allows user to insert property file
     * @param configuration
     * @throws SetApiClientException
     */
    protected BaseUserSetApi(ClientConfiguration configuration) throws SetApiClientException {
        this.configuration = configuration;
        if (this.configuration.getServiceUri() == null) {
            throw new SetApiClientException(" Set Api Endpoint not provided !!!");
        }

        this.apiConnection = new UserSetApiConnection(this.configuration.getServiceUri());
    }

    /**
     * Constructor
     * @throws SetApiClientException
     */
    public BaseUserSetApi() throws SetApiClientException {
	this(new ClientConfiguration());
    }

    public UserSetApiConnection getApiConnection() {
        return apiConnection;
    }

    public ClientConfiguration getConfiguration() {
        return configuration;
    }
}
