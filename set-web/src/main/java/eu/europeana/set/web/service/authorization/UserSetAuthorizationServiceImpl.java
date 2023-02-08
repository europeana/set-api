package eu.europeana.set.web.service.authorization;

import java.util.List;
import javax.annotation.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import eu.europeana.api.commons.definitions.vocabulary.Role;
import eu.europeana.api.commons.service.authorization.BaseAuthorizationService;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.exception.ApiWriteLockException;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.mongo.model.internal.PersistentApiWriteLock;
import eu.europeana.set.mongo.service.PersistentApiWriteLockService;
import eu.europeana.set.web.config.UserSetI18nConstants;
import eu.europeana.set.web.exception.authorization.UserAuthorizationException;
import eu.europeana.set.web.model.vocabulary.Roles;
import eu.europeana.set.web.model.vocabulary.SetOperations;

@SuppressWarnings("deprecation")
public class UserSetAuthorizationServiceImpl extends BaseAuthorizationService implements UserSetAuthorizationService {

    protected final Logger logger = LogManager.getLogger(getClass());
    
    @Resource
    UserSetConfiguration configuration;

    @Resource(name = "commons_oauth2_europeanaClientDetailsService")
    ClientDetailsService clientDetailsService;

    @Resource(name = "set_db_apilockService")
    private PersistentApiWriteLockService apiWriteLockService;

    public PersistentApiWriteLockService getPersistentIndexingJobService() {
    return apiWriteLockService;
    }
    
    @Override
    protected ClientDetailsService getClientDetailsService() {
	return clientDetailsService;
    }

    public UserSetConfiguration getConfiguration() {
	return configuration;
    }

    public void setConfiguration(UserSetConfiguration configuration) {
	this.configuration = configuration;
    }

    @Override
    protected String getSignatureKey() {
	return getConfiguration().getJwtTokenSignatureKey();
    }

    @Override
    protected Role getRoleByName(String name) {
	return Roles.getRoleByName(name);
    }

    @Override
    protected String getApiName() {
	return getConfiguration().getAuthorizationApiName();
    }
    
    public Authentication checkPermissions(Authentication authentication,
        String operation) throws ApplicationAuthenticationException{
      return super.checkPermissions(List.of(authentication), getApiName(), operation);
    }
   
    /**
     * Check if a write lock is in effect. Returns HttpStatus.LOCKED for change
     * operations in case the write lock is active.
     * 
     * @param userToken
     * @param operationName
     * @throws UserAuthorizationException
     */
    public void checkWriteLockInEffect(String operationName) throws ApplicationAuthenticationException {
      PersistentApiWriteLock runningJob;
      try {
          runningJob = getPersistentIndexingJobService().getLastActiveLock(WebUserSetModelFields.LOCK_WRITE_NAME);
          // refuse operation if a write lock is effective (allow only unlock and retrieve
          // operations)
          if (runningJob!=null && runningJob.getEnded()==null
              && WebUserSetModelFields.LOCK_WRITE_NAME.equals(runningJob.getName())
              && !(SetOperations.WRITE_UNLOCK.equals(operationName) || SetOperations.ADMIN_REINDEX.equals(operationName) || SetOperations.RETRIEVE.endsWith(operationName))) {
            throw new ApplicationAuthenticationException(UserSetI18nConstants.LOCKED_MAINTENANCE, UserSetI18nConstants.LOCKED_MAINTENANCE, null, HttpStatus.LOCKED, null);
          }
      } catch (ApiWriteLockException e) {
          throw new ApplicationAuthenticationException(UserSetI18nConstants.LOCKED_MAINTENANCE, UserSetI18nConstants.LOCKED_MAINTENANCE, null,
              HttpStatus.LOCKED, e);
      }
    }
    
}
