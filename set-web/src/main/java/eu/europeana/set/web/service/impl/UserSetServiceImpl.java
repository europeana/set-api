package eu.europeana.set.web.service.impl;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

import eu.europeana.api.common.config.I18nConstants;
import eu.europeana.api.commons.config.i18n.I18nService;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.exception.ParamValidationException;
import eu.europeana.set.definitions.exception.UserSetAttributeInstantiationException;
import eu.europeana.set.definitions.exception.UserSetInstantiationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.fields.WebUserSetModelFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.search.service.SearchApiResponse;
import eu.europeana.set.search.service.SetApiService;
import eu.europeana.set.search.service.impl.SetApiServiceImpl;
import eu.europeana.set.web.exception.request.RequestBodyValidationException;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.model.WebUserSetImpl;
import eu.europeana.set.web.service.UserSetService;
import ioinformarics.oss.jackson.module.jsonld.JsonldModule;

public class UserSetServiceImpl extends BaseUserSetServiceImpl implements UserSetService {

    @Resource
    I18nService i18nService;
    
    UserSetUtils userSetUtils = new UserSetUtils();

    private SetApiService setApiService = new SetApiServiceImpl();

    public UserSetUtils getUserSetUtils() {
      return userSetUtils;
    }

    public SetApiService getSetApiService() {
    	return setApiService;
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
		} else if (checkDisabled && res.isDisabled()) {
					throw new UserSetNotFoundException(I18nConstants.USER_SET_NOT_AVAILABLE, 
							I18nConstants.USER_SET_NOT_AVAILABLE, new String[] {userSetId}, HttpStatus.GONE);
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
		updateUserSetPagination(persistentUserSet);
		//update modified date
		persistentUserSet.setModified(new Date());
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
			if (updatedWebUserSet.getContext() != null) {
				userSet.setContext(updatedWebUserSet.getContext());
			}
			
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
	
		    if (updatedWebUserSet.getIsDefinedBy() != null) {
				userSet.setIsDefinedBy(updatedWebUserSet.getIsDefinedBy());
			}

			userSet.setDisabled(updatedWebUserSet.isDisabled());
		}
	}

	@Override
	public UserSet parseUserSetLd(String userSetJsonLdStr)
			throws	HttpException {

		JsonParser parser;
	    ObjectMapper mapper = new ObjectMapper();
	    mapper.registerModule(new JsonldModule());
	    mapper.configure(Feature.AUTO_CLOSE_SOURCE, true);
//	    mapper.configure(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME, true);
	    
	    
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
			throw new RequestBodyValidationException(I18nConstants.USERSET_CANT_PARSE_BODY, new String[]{e.getMessage()}, e);
		} catch (JsonParseException e) {
			throw new UserSetInstantiationException("Json formating exception! " + e.getMessage(), e);
		} catch (IOException e) {
			throw new UserSetInstantiationException("Json reading exception! " + e.getMessage(), e);
		}
	}
	
	/**
	 * This method normalizes item list if they exist to remove duplicated items.
	 * @param userSet
	 * @throws ParamValidationException
	 */
	public void removeItemDuplicates(UserSet userSet){
		if (userSet.getItems() != null && !userSet.getItems().isEmpty()){
			List<String> distinctItems = userSet.getItems().stream().distinct().collect(Collectors.toList());
			userSet.setItems(distinctItems);
		} 
	}
	
	public void validateWebUserSet(UserSet webUserSet) throws RequestBodyValidationException {

		//validate title
		if (webUserSet.getTitle() == null) {
			throw new RequestBodyValidationException(I18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY, 
					new String[]{WebUserSetModelFields.TITLE});
		}
		
		//validate context
		if(webUserSet.getContext()!= null && !WebUserSetModelFields.VALUE_CONTEXT_EUROPEANA_COLLECTION.equals(webUserSet.getContext())){
			throw new RequestBodyValidationException(I18nConstants.USERSET_VALIDATION_PROPERTY_VALUE, 
					new String[]{WebUserSetModelFields.AT_CONTEXT, webUserSet.getContext()});
		}	
		
		// validate isDefinedBy and items - we should not have both of them
		if (webUserSet.getItems() != null && webUserSet.getIsDefinedBy() != null) {
		    throw new RequestBodyValidationException(I18nConstants.USERSET_VALIDATION_PROPERTY_NOT_ALLOWED,
			    new String[] { WebUserSetModelFields.ITEMS,  WebUserSetModelFields.TYPE_OPEN});
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.europeana.set.web.service.UserSetService#deleteUserSet(java.lang.String)
	 */
	public void deleteUserSet(String userSetId) throws UserSetNotFoundException {

		// in case it is a closed set, remove the items that are members of the Set.
		UserSet userSet = getUserSetById(userSetId);
		if (!userSet.isOpenSet()) {
			for (String item : userSet.getItems()) {
				getMongoPersistence().remove(item);
			}
		}

		// if the user is an Administrator then permanently remove the Set. 
		getMongoPersistence().remove(userSetId);
	}
	
	/**
	 * This method validates position input, if false responds with -1
	 * 
	 * @param position
	 *            The given position
	 * @param items
	 *            The item list
	 * @return position The validated position in list to insert
	 * @throws ApplicationAuthenticationException
	 */
	public int validatePosition(String position, List<String> items) throws ApplicationAuthenticationException {
		int positionInt = -1;
		if (StringUtils.isNotEmpty(position)) {
			try {
				positionInt = Integer.parseInt(position);
				if (positionInt > items.size()) {
					positionInt = -1;
				}
			} catch (RuntimeException e) {
				//invalid position, assume last (-1)
				getLogger().trace("Position validation warning: " + e.getMessage());
			}
		}
		return positionInt;
	}

	/* (non-Javadoc)
	 * @see eu.europeana.set.web.service.UserSetService#insertItem(java.lang.String, java.lang.String, java.lang.String, eu.europeana.set.definitions.model.UserSet)
	 */
	public UserSet insertItem(String datasetId, String localId, String position, UserSet existingUserSet)
			throws ApplicationAuthenticationException {
		// validate position 
		//-1 if invalid
		int positionInt = validatePosition(position, existingUserSet.getItems());

		// build new item URL
		String newItem = buildIdentifierUrl(
				datasetId + "/" + localId, WebUserSetFields.BASE_ITEM_URL);

		// check if item already exists in the Set, if so remove it
		// insert item to Set in the indicated position (or last position if no position was indicated).
		UserSet extUserSet = null;		
		if (existingUserSet.getItems() == null) {
			addNewItemToList(existingUserSet, -1, newItem);
			extUserSet = updateItemList(existingUserSet);	
		} else {
			if (!existingUserSet.getItems().contains(newItem)) {
				//add item
				addNewItemToList(existingUserSet, positionInt, newItem);
				extUserSet = updateItemList(existingUserSet);	
			} else {
				//replace item
				int currentPos = existingUserSet.getItems().indexOf(newItem);
				if (currentPos == positionInt) {
					//do not change user set, just add pagination
					extUserSet = fillPagination(existingUserSet);											
				} else {
					replaceItem(existingUserSet, positionInt, newItem);				
					extUserSet = updateItemList(existingUserSet);
				}			
			}
		}
		return extUserSet;
	}

	/* (non-Javadoc)
	 * @see eu.europeana.set.web.service.UserSetService#updateItemList(eu.europeana.set.definitions.model.UserSet)
	 */
	public UserSet updateItemList(UserSet existingUserSet) {
		UserSet extUserSet;
		updateUserSetPagination(existingUserSet);

		// generate and add a created and modified timestamp to the Set
		existingUserSet.setModified(new Date());
		
		// Respond with HTTP 200
		// update an existing user set. merge user sets - insert new fields in existing object
		UserSet updatedUserSet = updateUserSetInDb(
				(PersistentUserSet) existingUserSet, null);
		extUserSet = fillPagination(updatedUserSet);
		return extUserSet;
	}

	/* (non-Javadoc)
	 * @see eu.europeana.set.web.service.UserSetService#replaceItem(eu.europeana.set.definitions.model.UserSet, int, java.lang.String)
	 */
	public void replaceItem(UserSet existingUserSet, int positionInt, String newItem) {
			existingUserSet.getItems().remove(newItem);
			addNewItemToList(existingUserSet, positionInt, newItem);
	}

	/* (non-Javadoc)
	 * @see eu.europeana.set.web.service.UserSetService#addNewItemToList(eu.europeana.set.definitions.model.UserSet, int, java.lang.String)
	 */
	public void addNewItemToList(UserSet existingUserSet, int positionInt, String newItem) {
		
		if (existingUserSet.getItems() == null) {
			//empty items list
			List<String> list = new ArrayList<String>();
			list.add(newItem);
			existingUserSet.setItems(list);			
		} else if (positionInt == -1) {
			//last position
			existingUserSet.getItems().add(newItem);
		} else {
			//given position
			existingUserSet.getItems().add(positionInt, newItem);						 
		} 
	}

    @Override
    public UserSet fetchDynamicSetItems(UserSet storedUserSet, String apiKey, String action,
    		String sort, String sortOrder, int pageNr, int pageSize)
	    throws HttpException, IOException, JSONException {
    	
    	String uri;
    	String additionalParameters;
    	additionalParameters = buildSearchQuery(sort, sortOrder, pageNr, pageSize);
    	uri = storedUserSet.getIsDefinedBy() + additionalParameters;
    	SearchApiResponse apiResult = getSetApiService().queryEuropeanaApi(uri, apiKey, action);
    	List<String> items = new ArrayList<String>();
    	for(String item : apiResult.getItems()) {
    		items.add(WebUserSetFields.BASE_URL_DATA + item);
    	}
    	if (items.size() > 0) {
	    	storedUserSet.setItems(items);
	    	storedUserSet.setTotal(items.size());
    	}
    	return storedUserSet;
    }
    
	/**
	 * This method appends additional search parameter from HTTP request
	 * @param sort
	 * @param sortOrder
	 * @param pageNr
	 * @param pageSize
	 * @return additional search Query string
	 */
	public String buildSearchQuery(String sort, String sortOrder, int pageNr, int pageSize) {

		StringBuilder searchQuery = new StringBuilder();
		
		searchQuery.append(WebUserSetFields.AND);
		searchQuery.append(pageNr);
		searchQuery.append(WebUserSetFields.PAGE).append("=");
		if(pageNr < 0)
			searchQuery.append(WebUserSetFields.DEFAULT_PAGE);
		else
			searchQuery.append(pageNr);

		searchQuery.append(WebUserSetFields.AND);
		searchQuery.append(WebUserSetFields.PAGE_SIZE).append("=");
		if(pageSize < 0)
			searchQuery.append(WebUserSetFields.MAX_ITEMS_PER_PAGE);
		else
			searchQuery.append(pageSize);
		
		searchQuery.append(WebUserSetFields.AND);
		if (!Strings.isNullOrEmpty(sort)) {
			searchQuery.append(WebUserSetFields.PARAM_SORT).append("=");
			searchQuery.append(sort);
			searchQuery.append(WebUserSetFields.AND);
			searchQuery.append(WebUserSetFields.PARAM_SORT_ORDER).append("=");
			searchQuery.append(sortOrder);
		}

		return searchQuery.toString();
	}
        
    /* (non-Javadoc)
     * @see eu.europeana.set.web.service.UserSetService#updateUserSetsWithCloseSetItems(eu.europeana.set.definitions.model.UserSet, java.util.List)
     */
    @Deprecated
    //TODO: fix the implementation and remove this method
    public UserSet updateUserSetInDb(UserSet storedUserSet, List<String> items) {    	
//    	if (items.size() > 0) {
//	    	storedUserSet.setItems(items);
//	    	storedUserSet.setTotal(items.size());
//    	}
	storedUserSet.setModified(new Date());
    	//simply store userSet
	return getMongoPersistence().update((PersistentUserSet) storedUserSet);
    }
    
}
