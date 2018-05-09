package eu.europeana.set.web.service.impl;


import java.util.Date;

import javax.annotation.Resource;

import eu.europeana.api.commons.config.i18n.I18nService;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.web.service.UserSetService;

public class UserSetServiceImpl extends BaseUserSetServiceImpl implements UserSetService {

//	@Resource
//	PersistentUserService mongoUserPersistance;
	
//	@Resource
//	private UserSetConfiguration configuration;
	
	
	
	@Resource
	I18nService i18nService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.europeana.UserSet.web.service.UserSetService#storeUserSet(eu.
	 * europeana.UserSet.definitions.model.UserSet)
	 */
	@Override
	public UserSet storeUserSet(UserSet newUserSet) {
		// store in mongo database
		UserSet res = getMongoPersistence().store(newUserSet);
		return res;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.europeana.UserSet.web.service.UserSetService#storeUserSet(eu.
	 * europeana.UserSet.definitions.model.UserSet, boolean)
	 */
	@Override
	public UserSet updateUserSet(PersistentUserSet persistentUserSet, UserSet webUserSet) {
		mergeUserSetProperties(persistentUserSet, webUserSet);
		UserSet res = updateAndReindex(persistentUserSet);

		return res;
	}

	private void mergeUserSetProperties(PersistentUserSet UserSet, UserSet updatedWebUserSet) {
		if (updatedWebUserSet.getType() != null)
			UserSet.setType(updatedWebUserSet.getType());

		if (updatedWebUserSet.getTitle() != null)
			UserSet.setTitle(updatedWebUserSet.getTitle());
		
		if (updatedWebUserSet.getLastUpdate() != null) {
			UserSet.setLastUpdate(updatedWebUserSet.getLastUpdate());
		} else {
			Date timeStamp = new java.util.Date();
			UserSet.setLastUpdate(timeStamp);
		}
		
		if (updatedWebUserSet.getCreator() != null)
			UserSet.setCreator(updatedWebUserSet.getCreator());
			
		if (updatedWebUserSet.getCreated() != null)
			UserSet.setCreated(updatedWebUserSet.getCreated());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.europeana.UserSet.web.service.UserSetService#existsInDb(eu.
	 * europeana.UserSet.definitions.model.UserSetId)
	 */
//	public boolean existsInDb(UserSetId annoId) {
//		boolean res = false;
//		try {
//			UserSet dbRes = getMongoPersistence().find(annoId);
//			if (dbRes != null)
//				res = true;
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//		return res;
//	}

	@Override
	public String getComponentName() {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public UserSet storeUserSet(UserSet UserSet) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	@Override
//	public UserSet updateUserSet(PersistentUserSet persistentUserSet, UserSet webUserSet) {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
