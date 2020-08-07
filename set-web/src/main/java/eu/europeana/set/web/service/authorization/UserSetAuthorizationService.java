package eu.europeana.set.web.service.authorization;

import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.api.commons.service.authorization.AuthorizationService;

public interface UserSetAuthorizationService extends AuthorizationService {

	UserSetConfiguration getConfiguration();
}
