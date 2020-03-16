package eu.europeana.set.web.service.authorization;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.oauth2.provider.ClientDetailsService;

import eu.europeana.api.commons.definitions.vocabulary.Role;
import eu.europeana.api.commons.service.authorization.BaseAuthorizationService;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.authentication.Application;
import eu.europeana.set.web.model.vocabulary.Operations;
import eu.europeana.set.web.model.vocabulary.Roles;
import eu.europeana.set.web.model.vocabulary.UserRoles;
import eu.europeana.set.web.service.authentication.AuthenticationService;


public class AuthorizationServiceImpl extends BaseAuthorizationService implements AuthorizationService {
	
	protected final Logger logger = LogManager.getLogger(getClass());

	@Resource
	AuthenticationService authenticationService;
	
	@Resource
	UserSetConfiguration configuration;

    @Resource(name = "commons_oauth2_europeanaClientDetailsService")
    ClientDetailsService clientDetailsService;
	
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
	
//    @Override
    protected String getAuthorizationApiName() {
    	return getConfiguration().getAuthorizationApiName();
    }

    @Override
    protected ClientDetailsService getClientDetailsService() {
    	return clientDetailsService;
    }

    @Override
    protected String getSignatureKey() {
    	return getConfiguration().getJwtTokenSignatureKey();
    }

    @Override
    protected Role getRoleByName(String name) {
    	return UserRoles.getRoleByName(name);
    }

    @Override
    protected String getApiName() {
    	return getConfiguration().getAuthorizationApiName();
    }
	
}
