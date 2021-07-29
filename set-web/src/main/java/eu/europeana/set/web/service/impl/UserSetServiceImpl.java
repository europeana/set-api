package eu.europeana.set.web.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import eu.europeana.set.search.SearchApiRequest;
import eu.europeana.set.web.exception.authorization.UserAuthorizationException;
import eu.europeana.set.web.model.search.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.api.common.config.UserSetI18nConstants;
import eu.europeana.api.commons.definitions.config.i18n.I18nConstants;
import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.definitions.vocabulary.CommonLdConstants;
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
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.search.exception.SearchApiClientException;
import eu.europeana.set.search.service.SearchApiResponse;
import eu.europeana.set.web.exception.request.RequestBodyValidationException;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.model.WebUserSetImpl;
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
	if (newUserSet.isEntityBestItemsSet()) {
	    checkPermissionToUpdate(newUserSet, authentication, true);
	}
	validateWebUserSet(newUserSet);

	// store in mongo database
	updateTotal(newUserSet);
	UserSet updatedUserSet = getMongoPersistence().store(newUserSet);
	getUserSetUtils().updatePagination(updatedUserSet);
	return updatedUserSet;
    }

    @Override
    public UserSet getUserSetById(String userSetId) throws UserSetNotFoundException {
	UserSet userSet = getMongoPersistence().getByIdentifier(userSetId);
	if (userSet == null) {
	    throw new UserSetNotFoundException(UserSetI18nConstants.USERSET_NOT_FOUND,
		    UserSetI18nConstants.USERSET_NOT_FOUND, new String[] { userSetId });
	}
	getUserSetUtils().updatePagination(userSet);
	return userSet;
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

    public void validateWebUserSet(UserSet webUserSet)
			throws RequestBodyValidationException, ParamValidationException, UserAuthorizationException {

	// validate title
	if (webUserSet.getTitle() == null && !webUserSet.isBookmarksFolder()) {
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
	validateEntityBestItemsSet(webUserSet);
	validateControlledValues(webUserSet);
	validateIsDefinedBy(webUserSet);
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
    public int validatePosition(String position, List<String> items, int pinnedItems)
	    throws ApplicationAuthenticationException {
	int positionInt = -1;
	if (StringUtils.isNotEmpty(position)) {
	    try {
		positionInt = Integer.parseInt(position);
		// if position less than pinned items
		// change the position from the initial start of Entity sets items
		if (positionInt <= pinnedItems) {
		    positionInt = pinnedItems + positionInt;
		}
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
	String newItem = UserSetUtils.buildItemUrl(WebUserSetFields.BASE_ITEM_URL, datasetId, localId);
	// check if the position is "pin" and is a EntityBestItem set then
	// insert the item at the 0 positio
    UserSet userSet;

	if (WebUserSetModelFields.PINNED_POSITION.equals(position)
		&& existingUserSet.isEntityBestItemsSet()) {
	    userSet = insertItem(existingUserSet, newItem, 0, true);
	} else {
		// validate position
	    // -1 if invalid
	    int positionInt = validatePosition(position, existingUserSet.getItems(), existingUserSet.getPinned());
		userSet = insertItem(existingUserSet, newItem, positionInt, false);
	}
	getUserSetUtils().updatePagination(userSet);
	return userSet;
    }

    /**
     * check if item already exists in the Set, if so remove it insert item to Set
     * in the indicated position (or last position if no position was indicated).
     *
     * For entity sets : if pinnedItem : Then increase the counter by one while
     * adding the item in the pinned list. While replacing the item : 1) if pinned
     * item is changed into item , decrease the counter 2) if item is changed into
     * pinned item , increase the counter 3) if the position is same for item/pinned
     * item, counter remains same in both the cases
     *
     * NOTE : Pinned value should be modified only for entity sets
     *
     * @param existingUserSet
     * @param newItem
     * @param positionInt
     * @return
     */
    private UserSet insertItem(UserSet existingUserSet, String newItem, int positionInt, boolean pinnedItem) {
	UserSet extUserSet = null;
	if (existingUserSet.getItems() == null) {
	    addNewItemToList(existingUserSet, -1, newItem);
		if(existingUserSet.isEntityBestItemsSet() && pinnedItem) {
			existingUserSet.setPinned(existingUserSet.getPinned() + 1);
		}
		extUserSet = updateItemList(existingUserSet);
	} else {
	    if (!existingUserSet.getItems().contains(newItem)) {
		// add item
		addNewItemToList(existingUserSet, positionInt, newItem);
		if(existingUserSet.isEntityBestItemsSet() && pinnedItem) {
			existingUserSet.setPinned(existingUserSet.getPinned() + 1);
		}
		extUserSet = updateItemList(existingUserSet);

	    } else {
        // replace item
		int oldPosition = existingUserSet.getItems().indexOf(newItem);
		if (oldPosition == positionInt) {
		    // do not change user set
		    // the items is already present at the correct position
			extUserSet = existingUserSet;
		} else {
		    replaceItem(existingUserSet, positionInt, newItem);
		    if(existingUserSet.isEntityBestItemsSet()) {
		    	if (pinnedItem){
					existingUserSet.setPinned(existingUserSet.getPinned() + 1);
				} else if (oldPosition < existingUserSet.getPinned()) {
					// For entity sets : if existing item is converted from Pinned --> Normal item,
					// decrease the pinned counter
					// ie; already existing Normal item being added as a pinned item now
					// This condition will avoid any changes in Pinned counter:
					// while adding a already existing Normal item as a Normal item again in different position
					// As the old position of Normal item will always be greater than existingUserSet.getPinned()
					existingUserSet.setPinned(existingUserSet.getPinned() - 1);
				}
			}
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
	updateTotal(existingUserSet);
	// generate and add a created and modified timestamp to the Set
	existingUserSet.setModified(new Date());

	// Respond with HTTP 200
	// update an existing user set. merge user sets - insert new fields in existing
	// object
	UserSet updatedUserSet = getMongoPersistence().update((PersistentUserSet) existingUserSet);
	getUserSetUtils().updatePagination(updatedUserSet);
	return updatedUserSet;
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
	// if item already existed, the size of item list has changed
	// Check to avoid IndexOutOfBoundsException
	if (positionInt > existingUserSet.getItems().size()) {
	    positionInt = positionInt - 1;
	}
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
	String url = getSearchApiUtils().buildSearchApiPostUrl(userSet, apiKey, getConfiguration().getSearchApiUrl());
	SearchApiRequest searchApiRequest = getSearchApiUtils().buildSearchApiPostBody(userSet, sort, sortOrder, pageNr, pageSize);
	try {
		String jsonBody = serializeSearchApiRequest(searchApiRequest);
		SearchApiResponse apiResult;
	    if (LdProfiles.STANDARD == profile) {
		apiResult = getSearchApiClient().searchItems(url, jsonBody, apiKey, false);
		setItemIds(userSet, apiResult);
	    } else if (LdProfiles.ITEMDESCRIPTIONS == profile) {
			apiResult = getSearchApiClient().searchItems(url, jsonBody, apiKey, true);
			int total = apiResult.getTotal();
			if (!userSet.isOpenSet()) {
				// dereferenciation of closed sets is limited to 100
				// use the number of item ids
				total = userSet.getItems().size();
			}
			List<String> sortedItemDescriptions = sortItemDescriptions(userSet, apiResult.getItems());
			setItems(userSet, sortedItemDescriptions, total);
		}
	    return userSet;
	} catch (SearchApiClientException e) {
	    if (SearchApiClientException.MESSAGE_INVALID_ISDEFINEDNBY.equals(e.getMessage())) {
		throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
			new String[] { WebUserSetModelFields.IS_DEFINED_BY, url }, e);
	    } else {
		throw new InternalServerException(e);
	    }
	} catch (IOException e) {
		throw new RequestBodyValidationException(
				UserSetI18nConstants.SEARCH_API_REQUEST_INVALID, new String[] {},
				e);
	}
    }

    private List<String> sortItemDescriptions(UserSet userSet, List<String> itemDescriptions) {
	List<String> orderedItemDescriptions = new ArrayList<String>(itemDescriptions.size());
	String localId;
	if(userSet.getItems() != null) {
		for (String itemUri : userSet.getItems()) {
			boolean found = false;
			localId = UserSetUtils.extractItemIdentifier(itemUri);
			//escape "/" to "\/" to match json string
			localId = StringUtils.replace(localId, "/", "\\/");
			for (String description : itemDescriptions) {
				if (description.contains(localId)) {
					orderedItemDescriptions.add(description);
					found = true;
					break;
				}
			}
			if (!found) {
				logger.debug("No item description found for id: {}", localId);
			}
			if (orderedItemDescriptions.size() == itemDescriptions.size()) {
				//skip items not included in the current page
				break;
			}
		}
		return orderedItemDescriptions;
	}
	// if open set OR userSet.getItems == null , return the same order as retrieved
	return itemDescriptions;
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
	    String requestUrl, String reqParams, LdProfiles profile, Authentication authentication)
			throws HttpException {

	BaseUserSetResultPage<?> resPage = null;
	int resultPageSize = results.getResults().size();
	int pageSize = searchQuery.getPageSize();
	int currentPage = searchQuery.getPageNr();
	String searchProfile = searchQuery.getSearchProfile();
	long totalInCollection = results.getResultSize();

	int lastPage = getLastPage(totalInCollection, pageSize);
	String collectionUrl = buildCollectionUrl(searchProfile, requestUrl, reqParams);
	CollectionOverview ResultList = buildCollectionOverview(collectionUrl, pageSize, totalInCollection, lastPage,
		CommonLdConstants.RESULT_LIST);

	if (LdProfiles.STANDARD == profile || LdProfiles.ITEMDESCRIPTIONS == profile) {
	    resPage = new UserSetResultPage();
	    setPageItems(results, (UserSetResultPage) resPage, authentication, profile);
	} else {
	    // LdProfiles.MINIMAL.equals(profile) - default
	    resPage = new UserSetIdsResultPage();
	    setPageItems(results, (UserSetIdsResultPage) resPage, resultPageSize);
	}

	resPage.setPartOf(ResultList);
	addPagination(resPage, collectionUrl, currentPage, pageSize, lastPage);

	return resPage;
    }

    void setPageItems(ResultSet<? extends UserSet> results, UserSetIdsResultPage resPage, int resultPageSize) {
	List<String> items = new ArrayList<>(resultPageSize);
	for (UserSet set : results.getResults()) {
	    items.add(((WebUserSetImpl) set).getId());
	}
	resPage.setItems(items);
	resPage.setTotalInPage(items.size());
    }

    void setPageItems(ResultSet<? extends UserSet> results, UserSetResultPage resPage, Authentication authentication,
	    LdProfiles profile) throws HttpException {
	List<UserSet> items = new ArrayList<>(results.getResults().size());

	// TODO: define a second parameter for itemset page size
	int derefItems = getConfiguration().getMaxSearchDereferencedItems();

	for (UserSet userSet : results.getResults()) {
	    if (LdProfiles.ITEMDESCRIPTIONS == profile) {
		fetchItems(userSet, null, null, CommonApiConstants.DEFAULT_PAGE, derefItems, profile);
	    }

	    // items not included in results
//   	    set.setItems(null);
//   	    set.setTotal(0);
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

    // This method is exceuted only while retrival of user set if pageNr in the request is not null or empty
    @Override
    public CollectionPage buildCollectionPage(UserSet userSet, LdProfiles profile, int pageNr, int pageSize, boolean itemsFetched,
	    HttpServletRequest request) throws ParamValidationException {

	int totalInCollection = userSet.getTotal();
	int lastPage = getLastPage(totalInCollection, pageSize);
	if (pageNr > lastPage) {
	    throw new ParamValidationException(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
		    UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
		    new String[] { CommonApiConstants.QUERY_PARAM_PAGE,
			    "value our of range: " + pageNr + ", last page:" + lastPage });
	}

	String collectionUrl = buildCollectionUrl(null, request.getRequestURL().toString(), request.getQueryString());

	CollectionOverview partOf = buildCollectionOverview(collectionUrl, pageSize, totalInCollection, lastPage,
		CommonLdConstants.COLLECTION);

	CollectionPage page = null;
    int startIndex = 0;
	final int endIndex;

	// If items are fetched for the user sets from Search API, items list consist of exact count of items requested
	// ie; Min(pagesize, [maxpagesize(closeset) OR dereference-items (openset)])
	// hence, the complete itemList will be listed to avoid ArrayIndexOutOfBound exception.
	// for other cases, all the items are available in the userset.getItems(). See: EA-2630
	if (itemsFetched) {
		endIndex = Math.min(pageSize, userSet.getItems().size());
	} else  {
		startIndex = pageNr * pageSize;
		endIndex = Math.min(startIndex + pageSize, totalInCollection);
	}

	if (endIndex > startIndex) {
		List<String> items = userSet.getItems().subList(startIndex, endIndex);
		if (LdProfiles.ITEMDESCRIPTIONS == profile) {
			page = new ItemDescriptionsCollectionPage(userSet, partOf, startIndex);
			((ItemDescriptionsCollectionPage) page).setItemList(items);
		} else {
			page = new ItemIdsCollectionPage(userSet, partOf, startIndex);
			page.setItems(items);
		}
		page.setTotalInPage(items.size());
	} else {
		// this if for the empty user Sets
		page = new CollectionPage(userSet, partOf, startIndex);
		page.setTotalInPage(0);
	}

	page.setCurrentPageUri(buildPageUrl(collectionUrl, pageNr, pageSize));

	if (pageNr > 0) {
	    page.setPrevPageUri(buildPageUrl(collectionUrl, pageNr - 1, pageSize));
	}

	if (pageNr < lastPage) {
	    page.setNextPageUri(buildPageUrl(collectionUrl, pageNr + 1, pageSize));
	}

	return page;
    }

    public ItemIdsResultPage buildItemIdsResultsPage(List<String> itemIds, int page, int pageSize,
	    HttpServletRequest request) {
//	new ResultsPageImpl<T>()
	ItemIdsResultPage result = new ItemIdsResultPage();

	String requestURL = request.getRequestURL().toString();
	String collectionUrl = buildCollectionUrl(null, requestURL, request.getQueryString());

	if (itemIds != null && !itemIds.isEmpty()) {
	    // build isPartOf (result)
	    long totalnCollection = (long) itemIds.size();
	    int lastPage = getLastPage(itemIds.size(), pageSize);

	    result.setPartOf(buildCollectionOverview(collectionUrl, pageSize, totalnCollection, lastPage,
		    CommonLdConstants.RESULT_LIST));

	    // build Result page properties
	    int startPos = page * pageSize;
	    if (startPos < itemIds.size()) {
		int toIndex = Math.min(startPos + pageSize, itemIds.size());
		List<String> pageItems = itemIds.subList(startPos, toIndex);
		result.setItems(pageItems);
		result.setTotalInPage(pageItems.size());
		addPagination(result, collectionUrl, page, pageSize, lastPage);
	    }
	} else {
	    // empty result page, but we must still return the ID
	    result.setCurrentPageUri(buildPageUrl(collectionUrl, page, pageSize));
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
	String userId = UserSetUtils.buildUserUri((String) authentication.getPrincipal());
	return userSet.getCreator().getHttpUrl().equals(userId);
    }

    /**
     * This method checks admin role
     *
     * @param authentication
     * @return true if user is admin
     */
    @Override
    public boolean isAdmin(Authentication authentication) {
	return hasAdminRights(authentication);
    }

    /**
     * Check if user is an editor
     *
     * @param authentication
     * @return true if user has editor role
     */
    @Override
    public boolean hasEditorRole(Authentication authentication) {
	return hasEditorRights(authentication);
    }

    /**
     * This method validates if the user is the owner/creator of the userset or the
     * admin
     * 
     * @param userSet
     * @param authentication
     * @return
     * @return userSet object
     * @throws HttpException
     */
    @Override
    public UserSet verifyOwnerOrAdmin(UserSet userSet, Authentication authentication, boolean includeEntitySetMsg)
	    throws HttpException {

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
	    StringBuilder message = new StringBuilder();
	    if (includeEntitySetMsg) {
		message.append(
			"Only the contributors, creator of the entity user set or admins are authorized to perform this operation.");
	    } else {
		message.append("Only the creators of the user set or admins are authorized to perform this operation.");
	    }
	    throw new ApplicationAuthenticationException(I18nConstants.OPERATION_NOT_AUTHORIZED,
		    I18nConstants.OPERATION_NOT_AUTHORIZED, new String[] { message.toString() }, HttpStatus.FORBIDDEN);
	}
    }

    /**
     * This method checks the permission to create or Update the entity user sets
     * for entity sets creation or updating the items: 1) 'contributors' (users with
     * editor role) 2) owner or admin ; all three are allowed to create/update the
     * entity set
     *
     * @param existingUserSet
     * @param authentication
     * @throws HttpException
     */
    public void checkPermissionToUpdate(UserSet existingUserSet, Authentication authentication,
	    boolean includeEntitySetMsg) throws HttpException {
	if (existingUserSet.isEntityBestItemsSet() && hasEditorRole(authentication)) {
	    return;
	}
	verifyOwnerOrAdmin(existingUserSet, authentication, includeEntitySetMsg);
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

    /**
     * Return the List of entity sets with items, subject and type value
     * 
     * @return
     */
    @Override
    public List<PersistentUserSet> getEntitySetBestBetsItems(UserSetQuery query) {
	return getMongoPersistance().getEntitySetsItemAndSubject(query);
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