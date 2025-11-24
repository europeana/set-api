package eu.europeana.set.web.service.authorization;

import eu.europeana.api.commons_sb3.error.exceptions.ApplicationAuthenticationException;
import eu.europeana.api.commons_sb3.web.service.authorization.AuthorizationService;
import eu.europeana.set.definitions.config.UserSetConfiguration;

public interface UserSetAuthorizationService extends AuthorizationService {

	UserSetConfiguration getConfiguration();
	
	void checkWriteLockInEffect(String operationName) throws ApplicationAuthenticationException;
}