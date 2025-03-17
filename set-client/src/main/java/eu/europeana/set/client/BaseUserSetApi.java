package eu.europeana.set.client;

import eu.europeana.api.commons_sb3.auth.AuthenticationBuilder;
import eu.europeana.api.commons_sb3.auth.AuthenticationHandler;
import eu.europeana.set.client.config.ClientConfiguration;
import eu.europeana.set.client.connection.UserSetApiConnection;
import eu.europeana.set.client.exception.SetApiClientException;

/**
 * Base class for client API
 * @author GordeaS
 *
 */

public class BaseUserSetApi {

    private UserSetApiConnection apiConnection;

    /**
     * Creates BaseUserSetApi instance with client configuration
     * This allows user to insert property file
     * @param config
     * @throws SetApiClientException
     */
    protected BaseUserSetApi(ClientConfiguration config) 
            throws SetApiClientException {
        this(config.getServiceUri(), AuthenticationBuilder.newAuthentication(config));
    }

    protected BaseUserSetApi(String serviceUri
                           , AuthenticationHandler auth) throws SetApiClientException {
        if (serviceUri == null) {
            throw new SetApiClientException(" Set Api Endpoint not provided !!!");
        }

        this.apiConnection = new UserSetApiConnection(serviceUri, auth);
    }

    /**
     * Constructor
     * @throws SetApiClientException
     */
    /*
    public BaseUserSetApi() throws SetApiClientException {
        this(new ClientConfiguration());
    }
    */

    public AuthenticationHandler getAuthenticationHandler() {
        return apiConnection.getAuthenticationHandler();
    }

    public void setAuthenticationHandler(AuthenticationHandler auth) {
        apiConnection.setAuthenticationHandler(auth);
    }

    public UserSetApiConnection getApiConnection() {
        return apiConnection;
    }
}
