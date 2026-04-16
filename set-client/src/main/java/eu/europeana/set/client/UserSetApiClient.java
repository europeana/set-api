package eu.europeana.set.client;

import java.util.List;
import java.util.Optional;
import org.codehaus.jettison.json.JSONArray;
import eu.europeana.api.commons_sb3.auth.AuthenticationHandler;
import eu.europeana.api.commons_sb3.definitions.caching.ResourceCaching;
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
        public Optional<UserSet> getUserSet(String identifier, Optional<String> profile, Optional<ResourceCaching> caching) throws SetApiClientException {
            return getApiConnection().getUserSet(identifier, profile, caching);

        }

        @Override
        public UserSet updateUserSet(String identifier, String set, String profile) throws SetApiClientException {
            return getApiConnection().updateUserSet(identifier, set, profile);
        }

        @Override
        public List<RecordPreview> getPaginationUserSet(String identifier, String sort, String sortOrder, String page, String pageSize, String profile) throws SetApiClientException {
            return getApiConnection().getPaginationUserSet(identifier, sort, sortOrder, page, pageSize, profile);
        }

        @Override
        public UserSet addItems(String identifier, List<String> items, String position, String profile)
            throws SetApiClientException {
          String requestBody = (new JSONArray(items)).toString();
          return getApiConnection().addItems(identifier, requestBody, position, profile);
        }
        
        @Override
        public UserSet removeItems(String identifier, List<String> items, String profile)
            throws SetApiClientException {
          String requestBody = (new JSONArray(items)).toString();
          return getApiConnection().removeItems(identifier, requestBody, profile);
        }
        
        @Override
        public boolean isItemInSet(String identifier, String itemId, String profile)
            throws SetApiClientException {
          return getApiConnection().checkItems(identifier, itemId);
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