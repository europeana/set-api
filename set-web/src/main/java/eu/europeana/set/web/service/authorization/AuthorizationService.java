package eu.europeana.set.web.service.authorization;

import eu.europeana.set.definitions.config.UserSetConfiguration;

public interface AuthorizationService extends eu.europeana.api.commons.service.authorization.AuthorizationService {

	UserSetConfiguration getConfiguration();
}
