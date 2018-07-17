package eu.europeana.set.web.service.authorization;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;

import eu.europeana.api.common.config.I18nConstants;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.authentication.Application;
import eu.europeana.set.web.exception.authorization.OperationAuthorizationException;
import eu.europeana.set.web.exception.authorization.UserAuthorizationException;
import eu.europeana.set.web.model.vocabulary.Operations;
import eu.europeana.set.web.model.vocabulary.Roles;
import eu.europeana.set.web.service.authentication.AuthenticationService;


public class AuthorizationServiceImpl implements AuthorizationService {
	
	protected final Logger logger = Logger.getLogger(getClass());

	@Resource
	AuthenticationService authenticationService;
	
	@Resource
	UserSetConfiguration configuration;

	public AuthorizationServiceImpl(AuthenticationService authenticationService){
		this.authenticationService = authenticationService;
	}
	
	public AuthorizationServiceImpl(){
		
	}
	
	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}
	

	/* (non-Javadoc)
	 * @see eu.europeana.set.web.service.authorization.AuthorizationService#authorizeUser(java.lang.String, java.lang.String, eu.europeana.set.definitions.model.UserSetId, java.lang.String)
	 */
	@Override
	public Agent authorizeUser(String userToken, String apiKey, String setId, String operationName)
			throws UserAuthorizationException, ApplicationAuthenticationException, OperationAuthorizationException {
		
		Application app = getAuthenticationService().getByApiKey(apiKey);
		Agent user = getAuthenticationService().getUserByToken(apiKey, userToken);
		
		if (user== null || user.getName() == null || user.getUserGroup() == null)
			throw new UserAuthorizationException(I18nConstants.INVALID_TOKEN, 
					I18nConstants.INVALID_TOKEN, new String[]{userToken}, HttpStatus.FORBIDDEN);
		
		if(!isAdmin(user) && !hasPermission(app, setId, operationName))
			throw new OperationAuthorizationException(I18nConstants.USER_NOT_AUTHORIZED, 
					I18nConstants.USER_NOT_AUTHORIZED, 
					new String[]{"UserSet id: "+ userToken},
					HttpStatus.FORBIDDEN);
				
		//check permissions
		if(isAdmin(user) && hasPermission(user, operationName))//allow all
			return user;
		else if(isTester(user) && configuration.isProductionEnvironment()){
			// testers not allowed in production environment
			throw new UserAuthorizationException(I18nConstants.TEST_USER_FORBIDDEN, 
					I18nConstants.TEST_USER_FORBIDDEN, new String[]{user.getName()}, HttpStatus.FORBIDDEN);
		} else	if(hasPermission(user, operationName)){
			//user is authorized
			return user;
		}

		//user is not authorized to perform operation
		throw new UserAuthorizationException(I18nConstants.USER_NOT_AUTHORIZED, 
				I18nConstants.USER_NOT_AUTHORIZED, new String[]{user.getName()}, HttpStatus.FORBIDDEN);	
	}

	//verify client app privileges 
	protected boolean hasPermission(Application app, String setId, String operationName) {
		if(Operations.MODERATION_ALL.equals(operationName) 
				|| Operations.RETRIEVE.equals(operationName) 
				|| Operations.UPDATE.equals(operationName) 
				|| Operations.CREATE.equals(operationName))
			return true;
		
		return setId != null; 
	}

	//verify user privileges
	protected boolean hasPermission(Agent user, String operationName) {
		Roles role = Roles.valueOf(user.getUserGroup());
		
		for (String operation : role.getOperations()) {
			if(operation.equalsIgnoreCase(operationName))
				return true;//users is authorized, everything ok
		}
		
		return false;
	}

	protected boolean isAdmin(Agent user) {
		return Roles.ADMIN.name().equals(user.getUserGroup());
	}
	
	protected boolean isTester(Agent user) {
		return Roles.TESTER.name().equals(user.getUserGroup());
	}
	
	public UserSetConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(UserSetConfiguration configuration) {
		this.configuration = configuration;
	}
}
