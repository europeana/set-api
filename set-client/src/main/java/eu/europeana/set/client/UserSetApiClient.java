package eu.europeana.set.client;

import java.util.List;

import eu.europeana.api.commons.auth.AuthenticationHandler;
import eu.europeana.set.client.config.ClientConfiguration;
import eu.europeana.set.client.exception.SetApiClientException;
import eu.europeana.set.client.model.result.RecordPreview;
import eu.europeana.set.client.web.SearchUserSetApi;
import eu.europeana.set.client.web.WebUserSetApi;
import eu.europeana.set.definitions.model.UserSet;

/**
 * Implementation of client api
 *
 * @author GordeaS
 * Refractored by Srishti Singh
 */

public class UserSetApiClient extends BaseUserSetApi {

    private final WebUserSetClient webUserSetClient;
    private final SearchUserSetClient searchUserSetClient;

    public UserSetApiClient(ClientConfiguration configuration) 
            throws SetApiClientException {
        super(configuration);
        this.webUserSetClient = new WebUserSetClient();
        this.searchUserSetClient = new SearchUserSetClient();
    }

    public UserSetApiClient(String serviceUri, AuthenticationHandler auth)
            throws SetApiClientException {
        super(serviceUri, auth);
        this.webUserSetClient = new WebUserSetClient();
        this.searchUserSetClient = new SearchUserSetClient();
    }

    public WebUserSetApi getWebUserSetApi() {
        return webUserSetClient;
    }

    public SearchUserSetApi getSearchUserSetApi() {
        return searchUserSetClient;
    }

    /**
     * Web User Set Client class
     */
    private class WebUserSetClient implements WebUserSetApi {
        @Override
        public UserSet createUserSet(String set, String profile) throws SetApiClientException {
            return getApiConnection().createUserSet(set, profile);

        }

        @Override
        public String deleteUserSet(String identifier) throws SetApiClientException {
            return getApiConnection().deleteUserSet(identifier);
        }

        @Override
        public UserSet getUserSet(String identifier, String profile) throws SetApiClientException {
            return getApiConnection().getUserSet(identifier, profile);
        }

        @Override
        public UserSet updateUserSet(String identifier, String set, String profile) throws SetApiClientException {
            return getApiConnection().updateUserSet(identifier, set, profile);
        }

        @Override
        public List<RecordPreview> getPaginationUserSet(String identifier, String sort, String sortOrder, String page, String pageSize, String profile) throws SetApiClientException {
            return getApiConnection().getPaginationUserSet(identifier, sort, sortOrder, page, pageSize, profile);
        }
    }

    private class SearchUserSetClient implements SearchUserSetApi {

        @Override
        public List<? extends UserSet> searchUserSet(String query, String[] qf,
                                                     String sort, String page, String pageSize, String facet, int facetLimit, String profile) throws SetApiClientException {
            return getApiConnection().searchUserSet(query, qf, sort, page, pageSize, facet, facetLimit, profile);
        }
    }
}
