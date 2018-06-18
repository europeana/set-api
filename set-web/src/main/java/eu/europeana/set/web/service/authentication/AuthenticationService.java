package eu.europeana.set.web.service.authentication;

import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.authentication.Application;
import eu.europeana.set.web.exception.authorization.UserAuthorizationException;

public interface AuthenticationService {

	public Application createMockClientApplication(String apiKey, String applicationName) throws ApplicationAuthenticationException;
	
	public Agent getUserByToken(String apiKey, String userToken) throws UserAuthorizationException;

	public Application getByApiKey(String apiKey) throws ApplicationAuthenticationException;
	
}
