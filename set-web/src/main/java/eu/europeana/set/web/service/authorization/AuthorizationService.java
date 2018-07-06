package eu.europeana.set.web.service.authorization;

import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.set.definitions.model.UserSetId;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.web.exception.authorization.OperationAuthorizationException;
import eu.europeana.set.web.exception.authorization.UserAuthorizationException;

public interface AuthorizationService {

	/**
	 * use authorizeUser(String userToken, String apiKey, String operationName) 
	 * @param userToken
	 * @param apiKey
	 * @param setId The user set id object
	 * @param operationName
	 * @return
	 * @throws UserAuthorizationException
	 */
	//TODO: EA1217 change setId to string
	Agent authorizeUser(String userToken, String apiKey, UserSetId setId, String operationName)
			throws UserAuthorizationException, ApplicationAuthenticationException, OperationAuthorizationException;

}
