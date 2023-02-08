package eu.europeana.set.web.service.authorization;

import eu.europeana.api.commons.service.authorization.AuthorizationService;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.set.definitions.config.UserSetConfiguration;

public interface UserSetAuthorizationService extends AuthorizationService {

	UserSetConfiguration getConfiguration();
	
	void checkWriteLockInEffect(String operationName) throws ApplicationAuthenticationException;
}
