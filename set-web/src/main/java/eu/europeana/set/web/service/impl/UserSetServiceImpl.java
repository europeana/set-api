package eu.europeana.set.web.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

import eu.europeana.api.common.config.I18nConstants;
import eu.europeana.api.commons.config.i18n.I18nService;
import eu.europeana.api.commons.definitions.search.Query;
import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.web.definitions.WebFields;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.exception.InternalServerException;
import eu.europeana.api.commons.web.exception.ParamValidationException;
import eu.europeana.set.definitions.exception.UserSetAttributeInstantiationException;
import eu.europeana.set.definitions.exception.UserSetInstantiationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.fields.WebUserSetModelFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.search.exception.SearchApiClientException;
import eu.europeana.set.search.service.SearchApiClient;
import eu.europeana.set.search.service.SearchApiResponse;
import eu.europeana.set.search.service.impl.SearchApiClientImpl;
import eu.europeana.set.web.exception.request.RequestBodyValidationException;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.model.WebUserSetImpl;
import eu.europeana.set.web.model.vocabulary.Roles;
import eu.europeana.set.web.search.BaseUserSetResultPage;
import eu.europeana.set.web.search.CollectionView;
import eu.europeana.set.web.search.UserSetIdsResultPage;
import eu.europeana.set.web.search.UserSetResultPage;
import eu.europeana.set.web.service.UserSetService;
import ioinformarics.oss.jackson.module.jsonld.JsonldModule;

public class UserSetServiceImpl extends BaseUserSetServiceImpl implements UserSetService {

    @Resource
    I18nService i18nService;

    UserSetUtils userSetUtils = new UserSetUtils();

    private SearchApiClient setApiService = new SearchApiClientImpl();

    public UserSetUtils getUserSetUtils() {
	return userSetUtils;
    }

    public SearchApiClient getSearchApiClient() {
	return setApiService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europeana.UserSet.web.service.UserSetService#storeUserSet(eu.
     * europeana.UserSet.definitions.model.UserSet)
     */
    @Override
    public UserSet storeUserSet(UserSet newUserSet) {

	UserSet extUserSet = getUserSetUtils().analysePagination(newUserSet);

	// store in mongo database
	return getMongoPersistence().store(extUserSet);
    }

    @Override
    public void updateUserSetPagination(UserSet newUserSet) {
	getUserSetUtils().updatePagination(newUserSet);
    }

    @Override
    public UserSet getUserSetById(String userSetId) throws UserSetNotFoundException {
	UserSet res = getMongoPersistence().getByIdentifier(userSetId);
	if (res == null) {
	    throw new UserSetNotFoundException(I18nConstants.USERSET_NOT_FOUND, I18nConstants.USERSET_NOT_FOUND,
		    new String[] { userSetId });
	}
	return res;
    }

    /**
     * This method checks if a user set with provided type and user already exists in database
     * @param creator
     * @return null or existing bookarks folder
     */
    public UserSet getBookmarksFolder(Agent creator){
	return getMongoPersistence().getBookmarksFolder(creator.getHttpUrl());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eu.europeana.set.web.service.UserSetService#buildIdentifierUrl(java.lang.
     * String, java.lang.String)
     */
    public String buildIdentifierUrl(String id, String base) {
	return getUserSetUtils().buildIdentifierUrl(id, base);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eu.europeana.set.web.service.UserSetService#fillPagination(eu.europeana.set.
     * definitions.model.UserSet)
     */
    public UserSet fillPagination(UserSet userSet) {
	return getUserSetUtils().fillPagination(userSet);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eu.europeana.set.web.service.UserSetService#updatePagination(eu.europeana.set
     * .definitions.model.UserSet)
     */
    public UserSet updatePagination(UserSet userSet) {
	return getUserSetUtils().updatePagination(userSet);
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europeana.UserSet.web.service.UserSetService#storeUserSet(eu.
     * europeana.UserSet.definitions.model.UserSet, boolean)
     */
    @Override
    public UserSet updateUserSet(PersistentUserSet persistentUserSet, UserSet webUserSet) {
	mergeUserSetProperties(persistentUserSet, webUserSet);
	updateUserSetPagination(persistentUserSet);
	// update modified date
	persistentUserSet.setModified(new Date());
	return getMongoPersistence().update(persistentUserSet);
    }

    /**
     * @deprecated check if the update test must merge the properties or if it
     *             simply overwrites it
     * @param userSet
     * @param updatedWebUserSet
     */
    @Deprecated
    private void mergeUserSetProperties(PersistentUserSet userSet, UserSet updatedWebUserSet) {
	if (updatedWebUserSet != null) {
	    if (updatedWebUserSet.getContext() != null) {
		userSet.setContext(updatedWebUserSet.getContext());
	    }

	    if (updatedWebUserSet.getType() != null) {
		userSet.setType(updatedWebUserSet.getType());
	    }

	    if (updatedWebUserSet.getVisibility() != null) {
		userSet.setVisibility(updatedWebUserSet.getVisibility());
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
	}
    }

    @Override
    public UserSet parseUserSetLd(String userSetJsonLdStr) throws HttpException {

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
	    throw new RequestBodyValidationException(I18nConstants.USERSET_CANT_PARSE_BODY,
		    new String[] { e.getMessage() }, e);
	} catch (JsonParseException e) {
	    throw new UserSetInstantiationException("Json formating exception! " + e.getMessage(), e);
	} catch (IOException e) {
	    throw new UserSetInstantiationException("Json reading exception! " + e.getMessage(), e);
	}
    }

    /**
     * This method normalizes item list if they exist to remove duplicated items.
     * 
     * @param userSet
     * @throws ParamValidationException
     */
    public void removeItemDuplicates(UserSet userSet) {
	if (userSet.getItems() != null && !userSet.getItems().isEmpty()) {
	    List<String> distinctItems = userSet.getItems().stream().distinct().collect(Collectors.toList());
	    userSet.setItems(distinctItems);
	}
    }

    public void validateWebUserSet(UserSet webUserSet) throws RequestBodyValidationException, ParamValidationException {

	// validate title
	if (webUserSet.getTitle() == null && !isBookmarksFolder(webUserSet)) {
	    throw new RequestBodyValidationException(I18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
		    new String[] { WebUserSetModelFields.TITLE });
	}

	// validate context
	if (webUserSet.getContext() != null
		&& !WebUserSetModelFields.VALUE_CONTEXT_EUROPEANA_COLLECTION.equals(webUserSet.getContext())) {
	    throw new RequestBodyValidationException(I18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
		    new String[] { WebUserSetModelFields.AT_CONTEXT, webUserSet.getContext() });
	}

	//validate visibility
	if (webUserSet.getVisibility()!= null && !VisibilityTypes.isValid(webUserSet.getVisibility())) {
	    throw new RequestBodyValidationException(I18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
		    new String[] { WebUserSetModelFields.VISIBILITY, webUserSet.getVisibility()});
	}
	
	// validate isDefinedBy and items - we should not have both of them
	if (webUserSet.getItems() != null && webUserSet.getIsDefinedBy() != null) {
	    throw new RequestBodyValidationException(I18nConstants.USERSET_VALIDATION_PROPERTY_NOT_ALLOWED,
		    new String[] { WebUserSetModelFields.ITEMS, WebUserSetModelFields.SET_OPEN });
	}
	
	validateBookmarkFolder(webUserSet);	
	validateControlledValues(webUserSet);
    }

    /**
     * This method validates and processes the favorite set
     * @param webUserSet The new user set
     * @throws RequestBodyValidationException
     */
    private void validateBookmarkFolder(UserSet webUserSet) throws RequestBodyValidationException, ParamValidationException {

	if(!isBookmarksFolder(webUserSet)) {
	    return;
	}
	
	if(!webUserSet.isPrivate()) {
	    throw new ParamValidationException(I18nConstants.USERSET_VALIDATION_PROPERTY_VALUE, 
		    I18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
			new String[] { WebUserSetModelFields.VISIBILITY, webUserSet.getVisibility() });
	}
	
	if(webUserSet.isOpenSet()) {
	    throw new ParamValidationException(I18nConstants.USERSET_VALIDATION_PROPERTY_NOT_ALLOWED, 
		    I18nConstants.USERSET_VALIDATION_PROPERTY_NOT_ALLOWED,
			new String[] { WebUserSetModelFields.IS_DEFINED_BY, webUserSet.getType()});
	}
	
	if(webUserSet.getCreator() == null || webUserSet.getCreator().getHttpUrl() == null) {
	    throw new ParamValidationException(I18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY, 
		    I18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
			new String[] { WebUserSetModelFields.CREATOR});
	}
	
	
	UserSet usersBookmarkFolder = getBookmarksFolder(webUserSet.getCreator());
	if(usersBookmarkFolder == null) {
	    //the user doesn't have yet a bookmark folder
	    return;
	}
	
	if(webUserSet.getIdentifier() == null) {
	    //create method
	    throw new RequestBodyValidationException(I18nConstants.USERSET_VALIDATION_BOOKMARKFOLDER_EXISTS,
		    new String[] { usersBookmarkFolder.getIdentifier(), usersBookmarkFolder.getCreator().getHttpUrl()});
	}
	
	if(!webUserSet.getIdentifier().equals(usersBookmarkFolder.getIdentifier())) {
	    //update method, prevent creation of 2 BookmarkFolders
	    throw new RequestBodyValidationException(I18nConstants.USERSET_VALIDATION_BOOKMARKFOLDER_EXISTS,
		    new String[] { usersBookmarkFolder.getIdentifier(), usersBookmarkFolder.getCreator().getHttpUrl()});
	}	
    }
    
    /**
     * This method validates controlled values e.g. type and visibility
     * @param webUserSet The new user set
     * @throws RequestBodyValidationException 
     */
    private void validateControlledValues(UserSet webUserSet) throws RequestBodyValidationException {

	if (webUserSet.getVisibility() != null && !VisibilityTypes.isValid(webUserSet.getVisibility())) {
	    throw new RequestBodyValidationException(I18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
		    new String[] { WebUserSetModelFields.VISIBILITY, webUserSet.getVisibility()});
	}

	if (webUserSet.getType() != null && !UserSetTypes.isValid(webUserSet.getType())) {
	    throw new RequestBodyValidationException(I18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
		    new String[] { WebUserSetModelFields.TYPE, webUserSet.getType()});
	}
    }
    
    boolean isBookmarksFolder(UserSet userSet) {
	return UserSetTypes.BOOKMARKSFOLDER.getJsonValue().equals(userSet.getType());	
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eu.europeana.set.web.service.UserSetService#deleteUserSet(java.lang.String)
     */
    public void deleteUserSet(String userSetId) throws UserSetNotFoundException {

	getMongoPersistence().remove(userSetId);
    }

    /**
     * This method validates position input, if false responds with -1
     * 
     * @param position The given position
     * @param items    The item list
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
		// invalid position, assume last (-1)
		getLogger().trace("Position validation warning: {} ", e.getMessage());
	    }
	}
	return positionInt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europeana.set.web.service.UserSetService#insertItem(java.lang.String,
     * java.lang.String, java.lang.String,
     * eu.europeana.set.definitions.model.UserSet)
     */
    public UserSet insertItem(String datasetId, String localId, String position, UserSet existingUserSet)
	    throws ApplicationAuthenticationException {
	// validate position
	// -1 if invalid
	int positionInt = validatePosition(position, existingUserSet.getItems());

	// build new item URL
	String newItem = buildIdentifierUrl(datasetId + "/" + localId, WebUserSetFields.BASE_ITEM_URL);

	// check if item already exists in the Set, if so remove it
	// insert item to Set in the indicated position (or last position if no position
	// was indicated).
	UserSet extUserSet = null;
	if (existingUserSet.getItems() == null) {
	    addNewItemToList(existingUserSet, -1, newItem);
	    extUserSet = updateItemList(existingUserSet);
	} else {
	    if (!existingUserSet.getItems().contains(newItem)) {
		// add item
		addNewItemToList(existingUserSet, positionInt, newItem);
		extUserSet = updateItemList(existingUserSet);
	    } else {
		// replace item
		int currentPos = existingUserSet.getItems().indexOf(newItem);
		if (currentPos == positionInt) {
		    // do not change user set, just add pagination
		    extUserSet = fillPagination(existingUserSet);
		} else {
		    replaceItem(existingUserSet, positionInt, newItem);
		    extUserSet = updateItemList(existingUserSet);
		}
	    }
	}
	return extUserSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eu.europeana.set.web.service.UserSetService#updateItemList(eu.europeana.set.
     * definitions.model.UserSet)
     */
    public UserSet updateItemList(UserSet existingUserSet) {
	UserSet extUserSet;
	updateUserSetPagination(existingUserSet);

	// generate and add a created and modified timestamp to the Set
	existingUserSet.setModified(new Date());

	// Respond with HTTP 200
	// update an existing user set. merge user sets - insert new fields in existing
	// object
	UserSet updatedUserSet = updateUserSetInDb(existingUserSet, null);
	extUserSet = fillPagination(updatedUserSet);
	return extUserSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eu.europeana.set.web.service.UserSetService#replaceItem(eu.europeana.set.
     * definitions.model.UserSet, int, java.lang.String)
     */
    public void replaceItem(UserSet existingUserSet, int positionInt, String newItem) {
	existingUserSet.getItems().remove(newItem);
	addNewItemToList(existingUserSet, positionInt, newItem);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eu.europeana.set.web.service.UserSetService#addNewItemToList(eu.europeana.set
     * .definitions.model.UserSet, int, java.lang.String)
     */
    public void addNewItemToList(UserSet existingUserSet, int positionInt, String newItem) {

	if (existingUserSet.getItems() == null) {
	    // empty items list
	    List<String> list = new ArrayList<>();
	    list.add(newItem);
	    existingUserSet.setItems(list);
	} else if (positionInt == -1) {
	    // last position
	    existingUserSet.getItems().add(newItem);
	} else {
	    // given position
	    existingUserSet.getItems().add(positionInt, newItem);
	}
    }

    @Override
    public UserSet fetchDynamicSetItems(UserSet userSet, String apiKey, String sort, String sortOrder, int pageNr,
	    int pageSize) throws HttpException {

	String url = buildSearchApiUrl(userSet, apiKey, sort, sortOrder, pageNr, pageSize);
//    	uri = userSet.getIsDefinedBy() + additionalParameters;

	SearchApiResponse apiResult;
	try {
	    apiResult = getSearchApiClient().searchItems(url, apiKey, "UserSet.FETCH_ITEMS");
	    setItems(userSet, apiResult);
	    return userSet;
	} catch (SearchApiClientException e) {
	    if (SearchApiClientException.MESSAGE_INVALID_ISSHOWNBY.equals(e.getMessage())) {
		throw new RequestBodyValidationException(I18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
			new String[] { WebUserSetModelFields.IS_DEFINED_BY, userSet.getIsDefinedBy() });
	    } else {
		throw new InternalServerException(e);
	    }
	}
    }

    private void setItems(UserSet userSet, SearchApiResponse apiResult) {
	List<String> items = new ArrayList<>();
	for (String item : apiResult.getItems()) {
	    items.add(WebUserSetFields.BASE_URL_DATA + item);
	}
	if (! items.isEmpty()) {
	    userSet.setItems(items);
	    userSet.setTotal(items.size());
	}
    }

    private String buildSearchApiUrl(UserSet userSet, String apiKey, String sort, String sortOrder, int pageNr,
	    int pageSize) {
	// String uri;
	// String additionalParameters;
	// additionalParameters = buildSearchQuery(sort, sortOrder, pageNr, pageSize);
	UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(userSet.getIsDefinedBy());
	// MultiValueMap<String, String> parameters = uriBuilder.build().queryParams();
	uriBuilder.replaceQueryParam(CommonApiConstants.QUERY_PARAM_PROFILE, CommonApiConstants.PROFILE_MINIMAL);
	// remove pagination and ordering
	Integer start = pageNr * pageSize + 1;
	uriBuilder.replaceQueryParam("start", start);
	uriBuilder.replaceQueryParam("rows", pageSize);
	// remove apikey if exists
	if (apiKey != null)
	    uriBuilder.replaceQueryParam(CommonApiConstants.PARAM_WSKEY, apiKey);

	if (sortOrder == null) {
	    uriBuilder.replaceQueryParam(CommonApiConstants.QUERY_PARAM_SORT, sort);
	} else {
	    uriBuilder.replaceQueryParam(CommonApiConstants.QUERY_PARAM_SORT, sort + "+" + sortOrder);
	}

	return uriBuilder.build(true).toUriString();
    }

    /**
     * This method appends additional search parameter from HTTP request
     * 
     * @param sort
     * @param sortOrder
     * @param pageNr
     * @param pageSize
     * @return additional search Query string
     */
    public String buildSearchQuery(String sort, String sortOrder, int pageNr, int pageSize) {
	// TODO use SearchQuery object from api commons
	StringBuilder searchQuery = new StringBuilder();

	searchQuery.append(WebUserSetFields.AND);
	searchQuery.append(pageNr);
	searchQuery.append(CommonApiConstants.QUERY_PARAM_PAGE).append("=");
	if (pageNr < 0)
	    searchQuery.append(CommonApiConstants.DEFAULT_PAGE);
	else
	    searchQuery.append(pageNr);

	searchQuery.append(WebUserSetFields.AND);
	searchQuery.append(CommonApiConstants.QUERY_PARAM_PAGE_SIZE).append("=");
	if (pageSize < 0)
	    searchQuery.append(WebUserSetFields.MAX_ITEMS_PER_PAGE);
	else
	    searchQuery.append(pageSize);

	searchQuery.append(WebUserSetFields.AND);
	if (!Strings.isNullOrEmpty(sort)) {
	    searchQuery.append(CommonApiConstants.QUERY_PARAM_SORT).append("=");
	    searchQuery.append(sort);
//	    searchQuery.append(WebUserSetFields.AND);
//	    searchQuery.append(WebUserSetFields.PARAM_SORT_ORDER).append("=");
//	    searchQuery.append(sortOrder);
	}

	return searchQuery.toString();
    }

    /*
     * (non-Javadoc)
     * @deprecated
	 * @see
     * eu.europeana.set.web.service.UserSetService#updateUserSetsWithCloseSetItems(
     * eu.europeana.set.definitions.model.UserSet, java.util.List)
     *
     */
    @Deprecated
    // TODO: fix the implementation and remove this method
    public UserSet updateUserSetInDb(UserSet storedUserSet, List<String> items) {
//    	if (items.size() > 0) {
//	    	storedUserSet.setItems(items);
//	    	storedUserSet.setTotal(items.size());
//    	}
	storedUserSet.setModified(new Date());
	// simply store userSet
	return getMongoPersistence().update((PersistentUserSet) storedUserSet);
    }

    @Override
    public ResultSet<? extends UserSet> search(UserSetQuery searchQuery, LdProfiles profile) {
	return  getMongoPersistance().find(searchQuery);
}
    @Override
    public BaseUserSetResultPage<?> buildResultsPage(UserSetQuery searchQuery, ResultSet<? extends UserSet> results,
	    StringBuffer requestUrl, String reqParams, LdProfiles profile, Authentication authentication) {

	BaseUserSetResultPage<?> resPage = null;
	int resultPageSize = results.getResults().size();

	if (LdProfiles.MINIMAL.equals(profile)) {
	    resPage = new UserSetIdsResultPage();
	    setPageItems(results, (UserSetIdsResultPage) resPage, resultPageSize);
	} else if (LdProfiles.STANDARD.equals(profile)) {
	    resPage = new UserSetResultPage();
	    setPageItems(results, (UserSetResultPage) resPage, resultPageSize, authentication);
	} else if (LdProfiles.ITEMDESCRIPTIONS.equals(profile)) {
	    resPage = new UserSetIdsResultPage();
	    setPageItemsExt(results, (UserSetIdsResultPage) resPage, resultPageSize, "dcDescription");
	}

//	resPage.setFacetFields(results.getFacetFields());
//	resPage.setTotalInPage(resultPageSize);

	String collectionUrl = buildCollectionUrl(searchQuery, requestUrl, reqParams);
	resPage.setPartOf(new CollectionView(collectionUrl, results.getResultSize()));

	int currentPage = searchQuery.getPageNr();
	String currentPageUrl = buildPageUrl(collectionUrl, currentPage, searchQuery.getPageSize());
	resPage.setCurrentPageUri(currentPageUrl);

	if (currentPage > 0) {
	    String prevPage = buildPageUrl(collectionUrl, currentPage - 1, searchQuery.getPageSize());
	    resPage.setPrevPageUri(prevPage);
	}

	// if current page is not the last one
	boolean isLastPage = resPage.getTotalInCollection() <= (currentPage + 1) * searchQuery.getPageSize();
	if (!isLastPage) {
	    String nextPage = buildPageUrl(collectionUrl, currentPage + 1, searchQuery.getPageSize());
	    resPage.setNextPageUri(nextPage);
	}

	return resPage;
    }

    private void setPageItems(ResultSet<? extends UserSet> results, UserSetIdsResultPage resPage, int resultPageSize) {
	List<String> items = new ArrayList<>(resultPageSize);
	for (UserSet set : results.getResults()) {
	    items.add(((WebUserSetImpl) set).getId());
	}
	resPage.setItems(items);
	resPage.setTotalInPage(items.size());
    }

    /**
     * This method consturcts page items using provided field name
     * @param results
     * @param resPage
     * @param resultPageSize
     * @param fieldName
     */
    private void setPageItemsExt(ResultSet<? extends UserSet> results, UserSetIdsResultPage resPage, int resultPageSize, String fieldName) {
	List<String> items = new ArrayList<String>(resultPageSize);
	for (UserSet set : results.getResults()) {
	    if (fieldName.equals("dcDescription")) {
    	        items.add(convertStringListToJsonString(((WebUserSetImpl) set).getItems()));
	    } else {
	        items.add(((WebUserSetImpl) set).getId());
	    }
	}
	resPage.setItems(items);
	resPage.setTotalInPage(items.size());
    }
    
    /**
     * This method converts list of strings to JSON string
     * @param input
     * @return JSON string
     */
    private String convertStringListToJsonString(List<String> input) {
	String res = ""; 
	for (String str : input) {
	    res = res + "\"" + str + "\",";
	}
	return res;
    }
    
    private void setPageItems(ResultSet<? extends UserSet> results, UserSetResultPage resPage, int resultPageSize,
	    Authentication authentication) {
	List<UserSet> items = new ArrayList<>(results.getResults().size());

	for (UserSet set : results.getResults()) {
	    // items not included in results
	    set.setItems(null);
	    set.setTotal(0);
	    if (!set.isPrivate()) {
		items.add(set);
	    } else {
		if (isOwner(set, authentication) || hasAdminRights(authentication)) {
		    items.add(set);
		} else {
		    // inlcude only the id
		    WebUserSetImpl id = new WebUserSetImpl();
		    id.setIdentifier(set.getIdentifier());
		    items.add(id);
		}
	    }

	}
	resPage.setItems(items);
	resPage.setTotalInPage(items.size());
    }

    private String buildPageUrl(String collectionUrl, int page, int pageSize) {
	StringBuilder builder = new StringBuilder(collectionUrl);
	builder.append("&").append(CommonApiConstants.QUERY_PARAM_PAGE).append("=").append(page);

	builder.append("&").append(CommonApiConstants.QUERY_PARAM_PAGE_SIZE).append("=").append(pageSize);

	return builder.toString();
    }

    private String buildCollectionUrl(Query searchQuery, StringBuffer requestUrl, String queryString) {

	// queryString = removeParam(WebAnnotationFields.PARAM_WSKEY,
	// queryString);

	// remove out of scope parameters
	queryString = removeParam(CommonApiConstants.QUERY_PARAM_PAGE, queryString);
	queryString = removeParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, queryString);

	// avoid duplication of query parameters
	queryString = removeParam(CommonApiConstants.QUERY_PARAM_PROFILE, queryString);

	// add mandatory parameters
	if (StringUtils.isNotBlank(searchQuery.getSearchProfile())) {
	    queryString += ("&" + CommonApiConstants.QUERY_PARAM_PROFILE + "=" + searchQuery.getSearchProfile());
	}

	return requestUrl.append("?").append(queryString).toString();
    }

    protected String removeParam(final String queryParam, String queryParams) {
	String tmp;
	// avoid name conflicts search "queryParam="
	int startPos = queryParams.indexOf(queryParam + "=");
	int startEndPos = queryParams.indexOf("&", startPos + 1);

	if (startPos >= 0) {
	    // make sure to remove the "&" if not the first param
	    if (startPos > 0)
		startPos--;
	    tmp = queryParams.substring(0, startPos);

	    if (startEndPos > 0)
		tmp += queryParams.substring(startEndPos);
	} else {
	    tmp = queryParams;
	}
	return tmp;
    }

    /**
     * This method checks if user is an owner of the user set
     * 
     * @param userSet
     * @param authentication
     * @return true if user is owner of a user set
     */
    public boolean isOwner(UserSet userSet, Authentication authentication) {
	if (authentication == null) {
	    return false;
	}
	
	if(userSet.getCreator() == null || userSet.getCreator().getHttpUrl() == null) {
	    return false;
	}
	String userId = buildCreatorUri((String) authentication.getPrincipal());
	return userSet.getCreator().getHttpUrl().equals(userId);
    }

    /**
     * This method retrieves user id from authentication object
     * 
     * @param authentication
     * @return the user id
     */
    @Override
    public String getUserId(Authentication authentication) {
	return buildCreatorUri((String) authentication.getPrincipal());
    }

    @Override
    public String buildCreatorUri(String userId) {
	return WebFields.DEFAULT_CREATOR_URL + userId;
    }

    /**
     * This method validates input values wsKey, identifier and userToken.
     * 
     * @param userSet
     * @param authentication
     * @return
     * @return userSet object
     * @throws HttpException
     */
    @Override
    public UserSet verifyOwnerOrAdmin(UserSet userSet, Authentication authentication) throws HttpException {

	if (authentication == null) {
	    // access by API KEY, authentication not available
	    throw new ApplicationAuthenticationException(I18nConstants.USER_NOT_AUTHORIZED,
		    I18nConstants.USER_NOT_AUTHORIZED, new String[] {
			    "Access to update operations of private User Sets require user authentication with JwtToken" },
		    HttpStatus.FORBIDDEN);
	}

	// verify ownership
	if (isOwner(userSet, authentication) || hasAdminRights(authentication)) {
	    // approve owner or admin
	    return userSet;
	} else {
	    // not authorized
	    throw new ApplicationAuthenticationException(I18nConstants.OPERATION_NOT_AUTHORIZED,
		    I18nConstants.OPERATION_NOT_AUTHORIZED, new String[] {
			    "Only the creators of the annotation or admins are authorized to perform this operation." }, 
		    HttpStatus.FORBIDDEN);
	}
    }

    protected boolean hasAdminRights(Authentication authentication) {
	if (authentication == null) {
	    return false;
	}

	for (Iterator<? extends GrantedAuthority> iterator = authentication.getAuthorities().iterator(); iterator
		.hasNext();) {
	    // role based authorization
	    String role = iterator.next().getAuthority();
	    if (Roles.ADMIN.getName().equalsIgnoreCase(role)) {
		return true;
	    }
	}
	return false;
    }
    
    @Override
    public UserSet fetchDynamicSetItemDescriptions(UserSet userSet, String apiKey, String sort, String sortOrder, int pageNr,
	    int pageSize) throws HttpException {

	String url = buildSearchApiUrl(userSet, apiKey, sort, sortOrder, pageNr, pageSize);

	SearchApiResponse apiResult;
	try {
	    apiResult = getSearchApiClient().searchItemDescriptions(url, apiKey, "UserSet.FETCH_ITEMS");    
	    setItemDescriptions(userSet, apiResult);
	    return userSet;
	} catch (SearchApiClientException e) {
	    if (SearchApiClientException.MESSAGE_INVALID_ISSHOWNBY.equals(e.getMessage())) {
		throw new RequestBodyValidationException(I18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
			new String[] { WebUserSetFields.IS_DEFINED_BY, userSet.getIsDefinedBy() });
	    } else {
		throw new InternalServerException(e);
	    }
	}
    }
    
    /**
     * This method completes item list
     * @param userSet
     * @param apiResult
     */
    private void setItemDescriptions(UserSet userSet, SearchApiResponse apiResult) {
	List<String> items = new ArrayList<String>();
	for (String item : apiResult.getItems()) {
	    items.add(item);
	}
	userSet.setItems(items);
	userSet.setTotal(items.size());
    }    
}