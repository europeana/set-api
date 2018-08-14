package eu.europeana.set.web.service.impl;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.api.common.config.I18nConstants;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.exception.ParamValidationException;
import eu.europeana.set.definitions.exception.UserSetAttributeInstantiationException;
import eu.europeana.set.definitions.exception.UserSetInstantiationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.web.exception.request.RequestBodyValidationException;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.model.WebUserSetImpl;
import eu.europeana.set.web.service.UserSetService;

public class UserSetServiceImpl extends BaseUserSetServiceImpl implements UserSetService {

    UserSetUtils userSetUtils = new UserSetUtils();

    public UserSetUtils getUserSetUtils() {
      return userSetUtils;
    }

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
	public void updateUserSetPagination(UserSet newUserSet) {		
		getUserSetUtils().updatePagination(newUserSet);
	}
	
	@Override
	public UserSet getUserSetById(String userSetId) throws UserSetNotFoundException {
		return getUserSetById(userSetId, true);
	}
	
	@Override
	public UserSet getUserSetById(String userSetId, boolean checkDisabled) throws UserSetNotFoundException {
		UserSet res = getMongoPersistence().getByIdentifier(userSetId);
		if (res == null) {
			throw new UserSetNotFoundException(I18nConstants.USERSET_NOT_FOUND, 
					I18nConstants.USERSET_NOT_FOUND, new String[] {userSetId});
		} else {
			if (checkDisabled) {
				if (res.isDisabled()) {
					throw new UserSetNotFoundException(I18nConstants.USER_SET_NOT_AVAILABLE, 
							I18nConstants.USER_SET_NOT_AVAILABLE, new String[] {userSetId});
				}
			}
		}
		return res; 
	}
	
	/* (non-Javadoc)
	 * @see eu.europeana.set.web.service.UserSetService#buildIdentifierUrl(java.lang.String, java.lang.String)
	 */
	public String buildIdentifierUrl(String id, String base) {
		return getUserSetUtils().buildIdentifierUrl(id, base);
	}
	
    /* (non-Javadoc)
     * @see eu.europeana.set.web.service.UserSetService#fillPagination(eu.europeana.set.definitions.model.UserSet)
     */
    public UserSet fillPagination(UserSet userSet) {
    	return getUserSetUtils().fillPagination(userSet);
    }	
	
    /* (non-Javadoc)
     * @see eu.europeana.set.web.service.UserSetService#updatePagination(eu.europeana.set.definitions.model.UserSet)
     */
    public UserSet updatePagination(UserSet userSet) {
    	return getUserSetUtils().updatePagination(userSet);
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
		UserSet res = getMongoPersistence().update(persistentUserSet);
		return res;
	}

	/* (non-Javadoc)
	 * @see eu.europeana.set.web.service.UserSetService#disableUserSet(eu.europeana.set.definitions.model.UserSet)
	 */
	public UserSet disableUserSet(UserSet existingUserSet) { 					 
		existingUserSet.setDisabled(true);
	 	return updateUserSet((PersistentUserSet) existingUserSet, existingUserSet);
	}
	
	/**
	 * @deprecated check if the update test must merge the properties or if it simply overwrites it
	 * @param UserSet
	 * @param updatedWebUserSet
	 */
	private void mergeUserSetProperties(PersistentUserSet userSet, UserSet updatedWebUserSet) {
		if (updatedWebUserSet != null) {
			if (updatedWebUserSet.getType() != null) {
				userSet.setType(updatedWebUserSet.getType());
			}
		
			if (updatedWebUserSet.getTitle() != null) {
				if (userSet.getTitle() != null) {
					for (Map.Entry<String, String> entry : updatedWebUserSet.getTitle().entrySet()) {
						userSet.getTitle().put(entry.getKey(), entry.getValue());
					}
				} else {
					userSet.setTitle(updatedWebUserSet.getTitle());
				}
			}
			
			if (updatedWebUserSet.getDescription() != null) {
				if (userSet.getDescription() != null) {
					for (Map.Entry<String, String> entry : updatedWebUserSet.getDescription().entrySet()) {
						userSet.getDescription().put(entry.getKey(), entry.getValue());
					}
				} else {
					userSet.setDescription(updatedWebUserSet.getDescription());
				}
			}

			if (updatedWebUserSet.getCreator() != null) {
				userSet.setCreator(updatedWebUserSet.getCreator());
			}
				
			if (updatedWebUserSet.getCreated() != null) {
				userSet.setCreated(updatedWebUserSet.getCreated());
			}
	
			userSet.setDisabled(updatedWebUserSet.isDisabled());
		}
	}

	@Override
	public UserSet parseUserSetLd(String userSetJsonLdStr)
			throws	HttpException {

		JsonParser parser;
	    ObjectMapper mapper = new ObjectMapper();  
	    mapper.configure(Feature.AUTO_CLOSE_SOURCE, true);
	    JsonFactory jsonFactory = mapper.getFactory();
		
		/**
		 * parse JsonLd string using JsonLdParser
		 */
		try {			
			parser = jsonFactory.createParser(userSetJsonLdStr);
			UserSet userSet = mapper.readValue(parser, WebUserSetImpl.class); 
			if (userSet.getModified() == null) {
				Date now = new Date();				
				userSet.setModified(now);
			}
			removeItemDuplicates(userSet);
            return userSet;
		} catch (UserSetAttributeInstantiationException e) {
			throw new RequestBodyValidationException(userSetJsonLdStr, I18nConstants.USERSET_CANT_PARSE_BODY, e);
		} catch (JsonParseException e) {
			throw new UserSetInstantiationException("Json formating exception!", e);
		} catch (IOException e) {
			throw new UserSetInstantiationException("Json reading exception!", e);
		}
	}
	
	/**
	 * This method normalizes item list if they exist to remove duplicated items.
	 * @param userSet
	 * @throws ParamValidationException
	 */
	public void removeItemDuplicates(UserSet userSet) throws ParamValidationException {

		List<String> resItems = new ArrayList<String>();
		
		if (userSet.getItems() != null && userSet.getItems().size() > 0) {
			for (String item : userSet.getItems()) {
				if (!resItems.contains(item)) {
					resItems.add(item);
				}
			}
			userSet.setItems(resItems);		
		}
	}
	
	public void validateWebUserSet(UserSet webUserSet) throws ParamValidationException {

		//validate title
		if (webUserSet.getTitle() == null) {
			throw new ParamValidationException("The title is missing.",
					I18nConstants.USERSET_VALIDATION,
					null);
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.europeana.set.web.service.UserSetService#deleteUserSet(java.lang.String)
	 */
	public void deleteUserSet(String userSetId) throws UserSetNotFoundException {

		getMongoPersistence().remove(userSetId);
	}
	
}
