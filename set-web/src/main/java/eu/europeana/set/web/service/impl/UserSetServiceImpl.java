package eu.europeana.set.web.service.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

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

import eu.europeana.api.common.config.UserSetI18nConstants;
import eu.europeana.api.commons.definitions.config.i18n.I18nConstants;
import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.oauth2.model.ApiCredentials;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.exception.InternalServerException;
import eu.europeana.api.commons.web.exception.ParamValidationException;
import eu.europeana.set.definitions.config.UserSetConfigurationImpl;
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
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.search.exception.SearchApiClientException;
import eu.europeana.set.search.service.SearchApiResponse;
import eu.europeana.set.web.exception.request.RequestBodyValidationException;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.model.WebUser;
import eu.europeana.set.web.model.WebUserSetImpl;
import eu.europeana.set.web.model.search.BaseUserSetResultPage;
import eu.europeana.set.web.model.search.CollectionPreview;
import eu.europeana.set.web.model.search.ItemIdsResultPage;
import eu.europeana.set.web.model.search.ResultList;
import eu.europeana.set.web.model.search.UserSetIdsResultPage;
import eu.europeana.set.web.model.search.UserSetResultPage;
import eu.europeana.set.web.model.vocabulary.Roles;
import eu.europeana.set.web.service.UserSetService;
import ioinformarics.oss.jackson.module.jsonld.JsonldModule;

public class UserSetServiceImpl extends BaseUserSetServiceImpl implements UserSetService {

    /*
     * (non-Javadoc)
     * 
     * @see eu.europeana.UserSet.web.service.UserSetService#storeUserSet(eu.
     * europeana.UserSet.definitions.model.UserSet)
     */
    @Override
    public UserSet storeUserSet(UserSet newUserSet, Authentication authentication) throws HttpException {
	setDefaults(newUserSet, authentication);

	validateWebUserSet(newUserSet);

	UserSet extUserSet = getUserSetUtils().updatePagination(newUserSet);

	// store in mongo database
	return getMongoPersistence().store(extUserSet);
    }

    private void setDefaults(UserSet newUserSet, Authentication authentication) {
	Agent user = new WebUser();
	user.setHttpUrl(getUserId(authentication));
	user.setNickname(((ApiCredentials) authentication.getCredentials()).getUserName());
	newUserSet.setCreator(user);

	if (newUserSet.getVisibility() == null) {
	    newUserSet.setVisibility(VisibilityTypes.PRIVATE.getJsonValue());
	}

	if (newUserSet.getType() == null) {
	    newUserSet.setType(UserSetTypes.COLLECTION.getJsonValue());
	}
    }

    @Override
    public UserSet getUserSetById(String userSetId) throws UserSetNotFoundException {
	UserSet res = getMongoPersistence().getByIdentifier(userSetId);
	if (res == null) {
	    throw new UserSetNotFoundException(UserSetI18nConstants.USERSET_NOT_FOUND,
		    UserSetI18nConstants.USERSET_NOT_FOUND, new String[] { userSetId });
	}
	return res;
    }

    @Override
    public List<PersistentUserSet> getUserSetByCreatorId(String creatorId) throws UserSetNotFoundException {
	return getMongoPersistence().getByCreator(creatorId).asList();
    }

    /**
     * This method checks if a user set with provided type and user already exists
     * in database
     * 
     * @param creator
     * @return null or existing bookarks folder
     */
    public UserSet getBookmarkFolder(Agent creator) {
	return getBookmarkFolder(creator.getHttpUrl());
    }

    public UserSet getBookmarkFolder(String creatorId) {
	return getMongoPersistence().getBookmarkFolder(creatorId);
    }

    @Override
    public UserSet parseUserSetLd(String userSetJsonLdStr)
	    throws RequestBodyValidationException, UserSetInstantiationException {

	JsonParser parser;
	ObjectMapper mapper = new ObjectMapper();
	mapper.registerModule(new JsonldModule());
	mapper.configure(Feature.AUTO_CLOSE_SOURCE, true);
//	mapper.configure(MapperFeature.AUTO_DETECT_SETTERS, false);
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
	    // set item list, as effect of profiles, the parser sends
	    removeItemDuplicates(userSet);
	    return userSet;
	} catch (UserSetAttributeInstantiationException e) {
	    throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_CANT_PARSE_BODY,
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
	    throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
		    new String[] { WebUserSetModelFields.TITLE });
	}

	// validate context
//	if (webUserSet.getContext() != null
//		&& !WebUserSetModelFields.VALUE_CONTEXT_EUROPEANA_COLLECTION.equals(webUserSet.getContext())) {
//	    throw new RequestBodyValidationException(I18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
//		    new String[] { WebUserSetModelFields.AT_CONTEXT, webUserSet.getContext() });
//	}

	// validate isDefinedBy and items - we should not have both of them
	if (webUserSet.getItems() != null && webUserSet.isOpenSet()) {
	    throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_NOT_ALLOWED,
		    new String[] { WebUserSetModelFields.ITEMS, WebUserSetModelFields.SET_OPEN });
	}

	validateBookmarkFolder(webUserSet);
	validateControlledValues(webUserSet);
	validateIsDefinedBy(webUserSet);
    }

    /**
     * This method validates and processes the favorite set
     * 
     * @param webUserSet The new user set
     * @throws RequestBodyValidationException
     */
    private void validateBookmarkFolder(UserSet webUserSet)
	    throws RequestBodyValidationException, ParamValidationException {

	if (!isBookmarksFolder(webUserSet)) {
	    return;
	}

	if (!webUserSet.isPrivate()) {
	    throw new ParamValidationException(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
		    UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
		    new String[] { WebUserSetModelFields.VISIBILITY, webUserSet.getVisibility() });
	}

	if (webUserSet.isOpenSet()) {
	    throw new ParamValidationException(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_NOT_ALLOWED,
		    UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_NOT_ALLOWED,
		    new String[] { WebUserSetModelFields.IS_DEFINED_BY, webUserSet.getType() });
	}

	if (webUserSet.getCreator() == null || webUserSet.getCreator().getHttpUrl() == null) {
	    throw new ParamValidationException(UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
		    UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
		    new String[] { WebUserSetModelFields.CREATOR });
	}

	UserSet usersBookmarkFolder = getBookmarkFolder(webUserSet.getCreator());
	if (usersBookmarkFolder == null) {
	    // the user doesn't have yet a bookmark folder
	    return;
	}

	// for create method indicate existing bookmark folder
	if (webUserSet.getIdentifier() == null) {
	    throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_VALIDATION_BOOKMARKFOLDER_EXISTS,
		    new String[] { usersBookmarkFolder.getIdentifier(),
			    usersBookmarkFolder.getCreator().getHttpUrl() });
	}

	// for update method indicate the existing bookmark folder (cannot change type
	// to BookmarkFolder)
	if (!webUserSet.getIdentifier().equals(usersBookmarkFolder.getIdentifier())) {
	    // update method, prevent creation of 2 BookmarkFolders
	    throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_VALIDATION_BOOKMARKFOLDER_EXISTS,
		    new String[] { usersBookmarkFolder.getIdentifier(),
			    usersBookmarkFolder.getCreator().getHttpUrl() });

	}
    }

    /**
     * This method validates controlled values e.g. type and visibility
     * 
     * @param webUserSet The new user set
     * @throws RequestBodyValidationException
     */
    private void validateControlledValues(UserSet webUserSet) throws RequestBodyValidationException {

	if (webUserSet.getVisibility() == null || !VisibilityTypes.isValid(webUserSet.getVisibility())) {
	    throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
		    new String[] { WebUserSetModelFields.VISIBILITY, webUserSet.getVisibility() });
	}

	if (webUserSet.getType() == null || !UserSetTypes.isValid(webUserSet.getType())) {
	    throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
		    new String[] { WebUserSetModelFields.TYPE, webUserSet.getType() });
	}
    }

    /**
     * The value of isDefinedBy is validated (e.g. search URL
     * https://api.europeana.eu/record/search.json?) to point to the Search API. We
     * make a GET request upon creation to see if the request total items returns
     * more then 0 and success is true (meaning is valid).
     * 
     * @param webUserSet
     * @throws ParamValidationException
     * @throws RequestBodyValidationException
     */
    private void validateIsDefinedBy(UserSet webUserSet)
	    throws ParamValidationException, RequestBodyValidationException {

	if (webUserSet.isOpenSet()) {
	    String searchUrl = getBaseSearchUrl(getConfiguration().getSearchApiUrl());
	    StringBuilder validateUrl = new StringBuilder(webUserSet.getIsDefinedBy());
	    String queryUrl = getBaseSearchUrl(validateUrl.toString());
	    if (!searchUrl.equals(queryUrl)) {
		throw new ParamValidationException(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
			UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
			new String[] { WebUserSetModelFields.IS_DEFINED_BY,
				" the access to api endpoint is not allowed: " + queryUrl });
	    }

	    String apiKey = getConfiguration().getSearchApiKey();
	    SearchApiResponse apiResult;
	    try {
		// the items are not required for validation
		validateUrl.append("&rows=0");
		apiResult = getSearchApiClient().searchItems(validateUrl.toString(), apiKey, false);
	    } catch (SearchApiClientException e) {
		throw new RequestBodyValidationException(
			UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE, new String[] {
				WebUserSetModelFields.IS_DEFINED_BY, "an error occured when calling " + validateUrl },
			e);
	    }

	    if (apiResult.getTotal() <= 0) {
		throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
			new String[] { WebUserSetModelFields.IS_DEFINED_BY,
				"no items returned when calling " + validateUrl });
	    }
	}
    }

    /**
     * This method extracts base URL from the search URL
     * 
     * @param searchUrl
     * @return base URL
     */
    private String getBaseSearchUrl(String searchUrl) {
	String res = searchUrl;

	int endPos = searchUrl.indexOf('?');
	if (endPos >= 0) {
	    res = searchUrl.substring(0, endPos);
	}
	return res;
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

    /*
     * (non-Javadoc)
     *
     * @see
     * eu.europeana.set.web.service.UserSetService#deleteUserSets(java.lang.String)
     */
    public void deleteUserSets(String creatorId, List<PersistentUserSet> userSets) {
	List<String> setsToBeDeleted = new ArrayList<>();
	if (!userSets.isEmpty()) {
	    for (PersistentUserSet userSet : userSets) {
		setsToBeDeleted.add(userSet.getIdentifier());
	    }
	}
	getMongoPersistance().removeAll(userSets);
	getLogger().info("User sets deleted for user {}. Sets deleted are : {} ", creatorId, setsToBeDeleted);
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
		getLogger().trace("Position validation warning: {} ", position, e);
		// invalid position, assume last (-1)
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
	String newItem = UserSetUtils.buildItemUrl(WebUserSetFields.BASE_ITEM_URL, datasetId, localId);

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
		    // the items is already present at the correct position
		    extUserSet = getUserSetUtils().updatePagination(existingUserSet);
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
	// update total
	getUserSetUtils().updatePagination(existingUserSet);

	// generate and add a created and modified timestamp to the Set
	existingUserSet.setModified(new Date());

	// Respond with HTTP 200
	// update an existing user set. merge user sets - insert new fields in existing
	// object
	return getMongoPersistence().update((PersistentUserSet) existingUserSet);
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
    public UserSet fetchItems(UserSet userSet, String sort, String sortOrder, int pageNr, int pageSize,
	    LdProfiles profile) throws HttpException {

	if (!userSet.isOpenSet()
		&& (userSet.getItems() == null || (userSet.getItems() != null && userSet.getItems().isEmpty()))) {
	    // if empty closed userset, nothing to do
	    return userSet;
	}

	String apiKey = getConfiguration().getSearchApiKey();
	String url = buildSearchApiUrl(userSet, apiKey, sort, sortOrder, pageNr, pageSize);
//    	uri = userSet.getIsDefinedBy() + additionalParameters;

	SearchApiResponse apiResult;
	try {
	    if (LdProfiles.STANDARD == profile) {
		apiResult = getSearchApiClient().searchItems(url, apiKey, false);
		setItemIds(userSet, apiResult);
	    } else if (LdProfiles.ITEMDESCRIPTIONS == profile) {
		apiResult = getSearchApiClient().searchItems(url, apiKey, true);
		int total = apiResult.getTotal();
		if (!userSet.isOpenSet()) {
		    // dereferenciation of closed sets is limited to 100
		    // use the number of item ids
		    total = userSet.getItems().size();
		}
		setItems(userSet, apiResult.getItems(), total);
	    }
	    return userSet;
	} catch (SearchApiClientException e) {
	    if (SearchApiClientException.MESSAGE_INVALID_ISDEFINEDNBY.equals(e.getMessage())) {
		throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
			new String[] { WebUserSetModelFields.IS_DEFINED_BY, url }, e);
	    } else {
		throw new InternalServerException(e);
	    }
	}
    }

    private void setItemIds(UserSet userSet, SearchApiResponse apiResult) {
	if (apiResult.getItems() == null) {
	    return;
	}

	List<String> items = new ArrayList<>(apiResult.getItems().size());
	for (String item : apiResult.getItems()) {
	    items.add(UserSetUtils.buildItemUrl(item));
	}
	setItems(userSet, items, apiResult.getTotal());
    }

    private void setItems(UserSet userSet, List<String> items, int total) {
//	if (!items.isEmpty()) {
	userSet.setItems(items);
	userSet.setTotal(total);
//	}
    }

    private String buildSearchApiUrl(UserSet userSet, String apiKey, String sort, String sortOrder, int pageNr,
	    int pageSize) {

	if (!userSet.isOpenSet()) {
	    return buildSearchApiUrlForClosedSets(userSet, apiKey, pageSize);
	}

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

    @Override
    public ResultSet<? extends UserSet> search(UserSetQuery searchQuery, LdProfiles profile,
	    Authentication authentication) {
	// add user information for visibility filtering criteria
	searchQuery.setAdmin(hasAdminRights(authentication));
	searchQuery.setUser(getUserId(authentication));

	return getMongoPersistance().find(searchQuery);
    }

    @Override
    public BaseUserSetResultPage<?> buildResultsPage(UserSetQuery searchQuery, ResultSet<? extends UserSet> results,
	    StringBuilder requestUrl, String reqParams, LdProfiles profile, Authentication authentication)
	    throws HttpException {

	BaseUserSetResultPage<?> resPage = null;
	int resultPageSize = results.getResults().size();
	int pageSize = searchQuery.getPageSize();
	String searchProfile = searchQuery.getSearchProfile();
	long totalInCollection = results.getResultSize();
	int lastPage = getLastPage(totalInCollection, pageSize);
	int currentPage = searchQuery.getPageNr();
	String collectionUrl = buildCollectionUrl(searchProfile, requestUrl, reqParams);
	
	if (LdProfiles.STANDARD == profile || LdProfiles.ITEMDESCRIPTIONS == profile) {
	    resPage = new UserSetResultPage();
	    setPageItems(results, (UserSetResultPage) resPage, authentication, profile);
	} else {
	    // LdProfiles.MINIMAL.equals(profile) - default
	    resPage = new UserSetIdsResultPage();
	    setPageItems(results, (UserSetIdsResultPage) resPage, resultPageSize);
	}

	resPage.setPartOf(buildCollectionPreview(collectionUrl, pageSize, totalInCollection, lastPage));
	addPagination(resPage, collectionUrl, currentPage, pageSize, lastPage);

	return resPage;
    }

    private void addPagination(BaseUserSetResultPage<?> resPage, String collectionUrl, int page, int pageSize,
	    int lastPage) {
	String currentPageUrl = buildPageUrl(collectionUrl, page, pageSize);
	resPage.setCurrentPageUri(currentPageUrl);

	if (page > 0) {
	    String prevPage = buildPageUrl(collectionUrl, page - 1, pageSize);
	    resPage.setPrevPageUri(prevPage);
	}

	// if current page is not the last one
	if (!isLastPage(page, lastPage)) {
	    String nextPage = buildPageUrl(collectionUrl, page + 1, pageSize);
	    resPage.setNextPageUri(nextPage);
	}
    }

    private ResultList buildCollectionPreview(String collectionUrl, int pageSize, long totalInCollection, int lastPage) {
	String first = buildPageUrl(collectionUrl, 0, pageSize);
	String last = buildPageUrl(collectionUrl, lastPage, pageSize);
	return new ResultList(collectionUrl, totalInCollection, first, last);
    }

    /**
     * calculates the last Page
     * 
     * @param totalResults
     * @param pageSize
     * @return
     */
    protected int getLastPage(long totalResults, int pageSize) {
	long lastPage = 0;
	if (totalResults > 0) {
	    long reaminder = (totalResults % pageSize);
	    int extraPage = (reaminder == 0 ? 0 : 1);
	    lastPage = ((totalResults / pageSize) + extraPage) - 1;
	}

	return Math.toIntExact(lastPage);
    }

    /**
     * Checks if the currentPage is LastPage
     * 
     * @param currentPage
     * @param lastPage
     * @return
     */
    private boolean isLastPage(int currentPage, int lastPage) {
	return (currentPage == lastPage);
    }

    private void setPageItems(ResultSet<? extends UserSet> results, UserSetIdsResultPage resPage, int resultPageSize) {
	List<String> items = new ArrayList<>(resultPageSize);
	for (UserSet set : results.getResults()) {
	    items.add(((WebUserSetImpl) set).getId());
	}
	resPage.setItems(items);
	resPage.setTotalInPage(items.size());
    }

    private void setPageItems(ResultSet<? extends UserSet> results, UserSetResultPage resPage,
	    Authentication authentication, LdProfiles profile) throws HttpException {
	List<UserSet> items = new ArrayList<>(results.getResults().size());

	// TODO: define a second parameter for itemset page size
	int derefItems = getConfiguration().getMaxSearchDereferencedItems();

	for (UserSet userSet : results.getResults()) {
	    if (LdProfiles.ITEMDESCRIPTIONS == profile) {
		fetchItems(userSet, null, null, CommonApiConstants.DEFAULT_PAGE, derefItems, profile);
	    }

	    // items not included in results
//	    set.setItems(null);
//	    set.setTotal(0);
	    if (!userSet.isPrivate()) {
		items.add(userSet);
	    } else {
		if (isOwner(userSet, authentication) || hasAdminRights(authentication)) {
		    items.add(userSet);
		} else {
		    // inlcude only the id
		    WebUserSetImpl id = new WebUserSetImpl();
		    id.setIdentifier(userSet.getIdentifier());
		    items.add(id);
		}
	    }

	    applyProfile(userSet, profile);
	}
	resPage.setItems(items);
	resPage.setTotalInPage(items.size());
    }

    @SuppressWarnings("unchecked")
    public ItemIdsResultPage buildItemIdsResultsPage(List<String> itemIds, int page, int pageSize,
	    HttpServletRequest request) {
//	new ResultsPageImpl<T>()
	ItemIdsResultPage result = new ItemIdsResultPage();
	
	StringBuilder requestURL = new StringBuilder(request.getRequestURL().toString());
	String collectionUrl = buildCollectionUrl(null, requestURL, request.getQueryString());

	if (itemIds != null && !itemIds.isEmpty()) {
	    //build isPartOf (result) 
	    long totalnCollection = (long) itemIds.size();
	    int lastPage = getLastPage(itemIds.size(), pageSize);
	    
	    result.setPartOf( buildCollectionPreview(collectionUrl, pageSize, totalnCollection, lastPage));
	    
	    //build Result page properties
	    int startPos = page * pageSize;
	    if (startPos < itemIds.size()) {
		int toIndex = Math.min(startPos + pageSize, itemIds.size());
		List<String> pageItems = itemIds.subList(startPos, toIndex);
		result.setItems(pageItems);
		result.setTotalInPage(pageItems.size());
		addPagination(result, collectionUrl, page, pageSize, lastPage);
	    }
	}

	return result;
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

	if (userSet.getCreator() == null || userSet.getCreator().getHttpUrl() == null) {
	    return false;
	}
	String userId = UserSetUtils.buildCreatorUri((String) authentication.getPrincipal());
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
	return UserSetUtils.buildCreatorUri((String) authentication.getPrincipal());
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
	    throw new ApplicationAuthenticationException(UserSetI18nConstants.USER_NOT_AUTHORIZED,
		    UserSetI18nConstants.USER_NOT_AUTHORIZED,
		    new String[] {
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
		    I18nConstants.OPERATION_NOT_AUTHORIZED,
		    new String[] {
			    "Only the creators of the user set or admins are authorized to perform this operation." },
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

    /**
     * This method retrieves item ids from the closed userSet to build query e.g.
     * https://api.europeana.eu/api/v2/search.json?profile=minimal&
     * query=europeana_id%3A(%22%2F08641%2F1037479000000476635%22%20OR%20%20%22%2F08641%2F1037479000000476943%22)
     * &rows=12&start=1
     * 
     * @param userSet
     * @param apiKey
     * @return
     * @throws HttpException
     */
    private String buildSearchApiUrlForClosedSets(UserSet userSet, String apiKey, int pageSize) {
	// use them to build the search query for retrieving item descriptions using
	// minimal profile
	// europeana_id is in format /collectionId/recordId, this can be easily
	// extracted from the
	// full record ID by removing the base URL http://data.europeana.eu/item
	// e.g. europeana_id:("/08641/1037479000000476635" OR
	// "/08641/1037479000000476943")
	String id;
	String fullId;
//	int maxDerefItems = 100;
	int maxItems = Math.min(userSet.getItems().size(), pageSize);

	StringBuilder query = new StringBuilder(100);
	query.append("europeana_id:(");
	for (int i = 0; i < maxItems; i++) {
	    fullId = userSet.getItems().get(i);
	    if (i > 0) {
		query.append(" OR ");
	    }
	    id = fullId.replace(WebUserSetFields.BASE_ITEM_URL, ""); // .replace("/", "%2F");
	    query.append('"').append('/').append(id).append('"');
	}
	// close bracket
	query.append(')');
	StringBuilder url = new StringBuilder(getConfiguration().getSearchApiUrl());
	url.append(URLEncoder.encode(query.toString(), StandardCharsets.UTF_8));

	url.append('&').append(CommonApiConstants.PARAM_WSKEY).append('=').append(apiKey);
	// append rows=100
	url.append('&').append(CommonApiConstants.QUERY_PARAM_ROWS).append('=').append(maxItems);

	return url.toString();
    }

    /**
     * This methods applies Linked Data profile to a user set
     * 
     * @param userSet The given user set
     * @param profile Provided Linked Data profile
     * @return profiled user set value
     */
    public UserSet applyProfile(UserSet userSet, LdProfiles profile) {

	// check that not more then maximal allowed number of items are
	// presented
	if (profile != LdProfiles.MINIMAL && userSet.getItems() != null) {
	    int itemsCount = userSet.getItems().size();
	    if (itemsCount > UserSetConfigurationImpl.MAX_ITEMS_TO_PRESENT) {
		List<String> itemsPage = userSet.getItems().subList(0, UserSetConfigurationImpl.MAX_ITEMS_TO_PRESENT);
		userSet.setItems(itemsPage);
		profile = LdProfiles.STANDARD;
		getLogger().debug("Profile switched to standard, due to set size!");
	    }
	}

	// set unnecessary fields to null - the empty fields will not be
	// presented
	switch (profile) {
	case ITEMDESCRIPTIONS:
	    // set serializedItems
	    ((WebUserSetImpl) userSet).setSerializedItems(userSet.getItems());
	    break;
	case STANDARD:
	    // not for stadard or item descriptions profile
	    setSerializedItemIds(userSet);
	    break;
	case MINIMAL:
	    // for the open sets with minimal profile we set the value to -1
	    // so that the total will not be serialized
	    if (userSet.isOpenSet()) {
		userSet.setTotal(-1);
	    }
	    userSet.setItems(null);
	    break;
	default:
	    userSet.setItems(null);
	    break;
	}

	return userSet;
    }

    private void setSerializedItemIds(UserSet userSet) {
	if (userSet.getItems() == null) {
	    return;
	}
	List<String> jsonSerialized = new ArrayList<>(userSet.getItems().size());
	for (String itemId : userSet.getItems()) {
	    // jsonSerialized.add(JSONObject.quote(itemId));
	    jsonSerialized.add('"' + itemId + '"');
	}
	((WebUserSetImpl) userSet).setSerializedItems(jsonSerialized);
//	userSet.setItems(null);
    }

}