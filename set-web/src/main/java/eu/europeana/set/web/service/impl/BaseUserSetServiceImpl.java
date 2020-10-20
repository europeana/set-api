package eu.europeana.set.web.service.impl;

import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.europeana.api.commons.config.i18n.I18nService;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.mongo.service.PersistentUserSetService;
import eu.europeana.set.search.service.SearchApiClient;
import eu.europeana.set.search.service.impl.SearchApiClientImpl;

public abstract class BaseUserSetServiceImpl {

    @Resource
    PersistentUserSetService mongoPersistance;
    @Resource
    I18nService i18nService;

    UserSetUtils userSetUtils = new UserSetUtils();

    @Resource
    UserSetConfiguration configuration;

    private SearchApiClient setApiService = new SearchApiClientImpl();

    Logger logger = LogManager.getLogger(getClass());

    protected PersistentUserSetService getMongoPersistence() {
	return mongoPersistance;
    }

    public void setMongoPersistance(PersistentUserSetService mongoPersistance) {
	this.mongoPersistance = mongoPersistance;
    }

    public Logger getLogger() {
	return logger;
    }

    public void setLogger(Logger logger) {
	this.logger = logger;
    }

    public PersistentUserSetService getMongoPersistance() {
	return mongoPersistance;
    }

    public UserSetUtils getUserSetUtils() {
	return userSetUtils;
    }

    public SearchApiClient getSearchApiClient() {
	return setApiService;
    }

    protected UserSetConfiguration getConfiguration() {
	return configuration;
    }
    
    /**
     * @deprecated check if the update test must merge the properties or if it
     *             simply overwrites it
     * @param persistedSet
     * @param updates
     */
    @Deprecated(since = "")
    void mergeUserSetProperties(PersistentUserSet persistedSet, UserSet updates) {
	if (updates == null) {
	    return;
	}

	mergeDescriptiveProperties(persistedSet, updates);

	mergeProvenanceProperties(persistedSet, updates);

	if (updates.getIsDefinedBy() != null) {
	    persistedSet.setIsDefinedBy(updates.getIsDefinedBy());
	}

    }

    void mergeProvenanceProperties(PersistentUserSet persistedSet, UserSet updates) {
	if (updates.getCreator() != null) {
	    persistedSet.setCreator(updates.getCreator());
	}

	if (updates.getCreated() != null) {
	    persistedSet.setCreated(updates.getCreated());
	}
    }

    void mergeDescriptiveProperties(PersistentUserSet persistedSet, UserSet updates) {
	if (updates.getType() != null) {
	    persistedSet.setType(updates.getType());
	}

	if (updates.getVisibility() != null) {
	    persistedSet.setVisibility(updates.getVisibility());
	}

	if (updates.getTitle() != null) {
	    if (persistedSet.getTitle() != null) {
		for (Map.Entry<String, String> entry : updates.getTitle().entrySet()) {
		    persistedSet.getTitle().put(entry.getKey(), entry.getValue());
		}
	    } else {
		persistedSet.setTitle(updates.getTitle());
	    }
	}

	if (updates.getDescription() != null) {
	    if (persistedSet.getDescription() != null) {
		for (Map.Entry<String, String> entry : updates.getDescription().entrySet()) {
		    persistedSet.getDescription().put(entry.getKey(), entry.getValue());
		}
	    } else {
		persistedSet.setDescription(updates.getDescription());
	    }
	}
    }


    /*
     * (non-Javadoc)
     * 
     * @see eu.europeana.UserSet.web.service.UserSetService#storeUserSet(eu.
     * europeana.UserSet.definitions.model.UserSet, boolean)
     */
//    @Override
    public UserSet updateUserSet(PersistentUserSet persistentUserSet, UserSet webUserSet) {
	mergeUserSetProperties(persistentUserSet, webUserSet);
	updateUserSetPagination(persistentUserSet);
	// update modified date
	persistentUserSet.setModified(new Date());
	return getMongoPersistence().update(persistentUserSet);
    }
    
//    @Override
    public void updateUserSetPagination(UserSet newUserSet) {
	getUserSetUtils().updatePagination(newUserSet);
    }
}
