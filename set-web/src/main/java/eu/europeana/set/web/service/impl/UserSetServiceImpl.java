package eu.europeana.set.web.service.impl;


import com.fasterxml.jackson.core.JsonParseException;

import eu.europeana.api.common.config.I18nConstants;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.set.definitions.exception.UserSetAttributeInstantiationException;
//import eu.europeana.api.commons.config.i18n.I18nService;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.utils.JsonUtils;
import eu.europeana.set.web.exception.request.RequestBodyValidationException;
import eu.europeana.set.web.model.WebUserSetImpl;
import eu.europeana.set.web.service.UserSetService;

public class UserSetServiceImpl extends BaseUserSetServiceImpl implements UserSetService {

    UserSetUtils userSetUtils = new UserSetUtils();

    public UserSetUtils getUserSetUtils() {
      return userSetUtils;
    }
    

//	@Resource
//	PersistentUserService mongoUserPersistance;
	
//	@Resource
//	private UserSetConfiguration configuration;
	
	
	
//	@Resource
//	I18nService i18nService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.europeana.UserSet.web.service.UserSetService#storeUserSet(eu.
	 * europeana.UserSet.definitions.model.UserSet)
	 */
	@Override
	public UserSet storeUserSet(UserSet newUserSet) {
		
		UserSet extUserSet = getUserSetUtils().analysePagination(newUserSet);

		// store in mongo database
		UserSet res = getMongoPersistence().store(extUserSet);
		return res;
	}
	
	@Override
	public UserSet getUserSetById(String userSetId) {
		// store in mongo database
		UserSet res = getMongoPersistence().getByIdentifier(userSetId);
		return res; //analysePagination(res);
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
		
//		if (updatedWebUserSet.getLastUpdate() != null) {
//			UserSet.setLastUpdate(updatedWebUserSet.getLastUpdate());
//		} else {
//			Date timeStamp = new java.util.Date();
//			UserSet.setLastUpdate(timeStamp);
//		}
		
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

	@Override
	public UserSet parseUserSetLd(String userSetJsonLdStr)
			throws JsonParseException, HttpException {

		/**
		 * parse JsonLd string using JsonLdParser. JsonLd string -> JsonLdParser
		 * -> JsonLd object
		 */
		try {
	        UserSet userSet = JsonUtils.toUserSetObject(userSetJsonLdStr, WebUserSetImpl.class);
            return userSet;
//			UserSetLdParser europeanaParser = new UserSetLdParser();
//			return europeanaParser.parseUserSet(UserSetJsonLdStr);
		} catch (UserSetAttributeInstantiationException e) {
			throw new RequestBodyValidationException(userSetJsonLdStr, I18nConstants.USERSET_CANT_PARSE_BODY, e);
		}
	}
	
}
