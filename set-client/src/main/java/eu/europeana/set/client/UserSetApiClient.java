package eu.europeana.set.client;

import java.io.IOException;

import eu.europeana.set.client.config.ClientConfiguration;
import eu.europeana.set.client.exception.SetApiClientException;
import eu.europeana.set.client.web.SearchUserSetApi;
import eu.europeana.set.client.web.WebUserSetApi;
import org.springframework.http.ResponseEntity;

import eu.europeana.set.client.exception.TechnicalRuntimeException;

/**
 * Implementation of client api
 * @author GordeaS
 *
 */

public class UserSetApiClient extends BaseUserSetApi {

	private final WebUserSetClient webUserSetClient;
	private final SearchUserSetClient searchUserSetClient;

    public UserSetApiClient(ClientConfiguration configuration) throws SetApiClientException {
	super(configuration);
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
	private class WebUserSetClient implements  WebUserSetApi {
		@Override
		public ResponseEntity<String> createUserSet(String set, String profile) {
			ResponseEntity<String> res;
			try {
				res = getApiConnection().createUserSet(set, profile);
			} catch (IOException e) {
				throw new TechnicalRuntimeException(
						"Exception occured when invoking the UserSetJsonApi createUserSet method", e);
			}
			return res;
		}

		@Override
		public ResponseEntity<String> deleteUserSet(String identifier) {
			ResponseEntity<String> res;
			try {
				res = getApiConnection().deleteUserSet(identifier);
			} catch (IOException e) {
				throw new TechnicalRuntimeException(
						"Exception occured when invoking the UserSetJsonApi deleteUserSet method", e);
			}

			return res;
		}

		@Override
		public ResponseEntity<String> getUserSet(String identifier, String profile) {
			ResponseEntity<String> res;
			try {
				res = getApiConnection().getUserSet(identifier, profile);
			} catch (IOException e) {
				throw new TechnicalRuntimeException("Exception occured when invoking the UserSetJsonApi getUserSet method",
						e);
			}

			return res;
		}

		@Override
		public ResponseEntity<String> updateUserSet(String identifier, String set, String profile) {
			ResponseEntity<String> res;
			try {
				res = getApiConnection().updateUserSet(identifier, set, profile);
			} catch (IOException e) {
				throw new TechnicalRuntimeException(
						"Exception occured when invoking the UserSetJsonApi updateUserSet method", e);
			}

			return res;
		}
	}

	private class SearchUserSetClient implements SearchUserSetApi {

		@Override
		public ResponseEntity<String> searchUserSet(String query, String[] qf,
													String sort, int page, int pageSize, String facet, int facetLimit, String profile) {
			ResponseEntity<String> res;
			try {
				res = getApiConnection().searchUserSet(query, qf, sort, page, pageSize, facet, facetLimit, profile);
			} catch (IOException e) {
				throw new TechnicalRuntimeException(
						"Exception occured when invoking the UserSetSearch API searchUserSet method", e);
			}
			return  res;
		}
	}


}