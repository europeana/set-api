package eu.europeana.set.web.service.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.util.UriComponentsBuilder;

import eu.europeana.api.common.config.UserSetI18nConstants;
import eu.europeana.api.commons.config.i18n.I18nService;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.oauth2.model.ApiCredentials;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.exception.ParamValidationException;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.mongo.service.PersistentUserSetService;
import eu.europeana.set.search.exception.SearchApiClientException;
import eu.europeana.set.search.service.SearchApiClient;
import eu.europeana.set.search.service.SearchApiResponse;
import eu.europeana.set.search.service.impl.SearchApiClientImpl;
import eu.europeana.set.web.exception.request.RequestBodyValidationException;
import eu.europeana.set.web.model.WebUser;
import eu.europeana.set.web.model.search.CollectionOverview;
import eu.europeana.set.web.model.vocabulary.Roles;

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
	// update modified date
	persistentUserSet.setModified(new Date());
	updateTotal(persistentUserSet);
	UserSet updatedUserSet =  getMongoPersistence().update(persistentUserSet);
	getUserSetUtils().updatePagination(updatedUserSet);
	return updatedUserSet;
	
    }

    protected String buildPageUrl(String collectionUrl, int page, int pageSize) {
	StringBuilder builder = new StringBuilder(collectionUrl);
	builder.append("&").append(CommonApiConstants.QUERY_PARAM_PAGE).append("=").append(page);

	builder.append("&").append(CommonApiConstants.QUERY_PARAM_PAGE_SIZE).append("=").append(pageSize);

	return builder.toString();
    }

    protected String buildCollectionUrl(String searchProfile, String requestUrl, String queryString) {

	// queryString = removeParam(WebAnnotationFields.PARAM_WSKEY,
	// queryString);

	// remove out of scope parameters
	queryString = removeParam(CommonApiConstants.QUERY_PARAM_PAGE, queryString);
	queryString = removeParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, queryString);

	// avoid duplication of query parameters
	queryString = removeParam(CommonApiConstants.QUERY_PARAM_PROFILE, queryString);

	// add mandatory parameters
	if (StringUtils.isNotBlank(searchProfile)) {
	    queryString += ("&" + CommonApiConstants.QUERY_PARAM_PROFILE + "=" + searchProfile);
	}

	return requestUrl+"?"+queryString;
    }

    protected String removeParam(final String queryParam, String queryParams) {
	String tmp;
	// avoid name conflicts search "queryParam="
	int startPos = queryParams.indexOf(queryParam + "=");
	int startEndPos = queryParams.indexOf('&', startPos + 1);

	if (startPos >= 0) {
	    // make sure to remove the "&" if not the first param
	    if (startPos > 0) {
		startPos--;
	    }

	    tmp = queryParams.substring(0, startPos);

	    if (startEndPos > 0) {
		// tmp += queryParams.substring(startEndPos);
		tmp = (new StringBuilder(tmp)).append(queryParams.substring(startEndPos)).toString();
	    }
	} else {
	    tmp = queryParams;
	}
	return tmp;
    }

    protected CollectionOverview buildCollectionOverview(String collectionUrl, int pageSize, long totalInCollection,
	    int lastPage, String type) {
	String first = buildPageUrl(collectionUrl, 0, pageSize);
	String last = buildPageUrl(collectionUrl, lastPage, pageSize);
	return new CollectionOverview(collectionUrl, totalInCollection, first, last, type);
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
    protected boolean isLastPage(int currentPage, int lastPage) {
	return (currentPage == lastPage);
    }

    protected void setDefaults(UserSet newUserSet, Authentication authentication) {
	Agent user = new WebUser();
	// if entity set, assign entity admin user as a creator
	// also, add user as 'contributor' if the role is editor
    if (StringUtils.equals(newUserSet.getType(), UserSetTypes.ENTITYBESTITEMSSET.getJsonValue())) {
    	user.setHttpUrl(UserSetUtils.buildCreatorUri(getConfiguration().getEntityUserSetUserId()));
		user.setNickname(WebUserSetModelFields.ENTITYUSER_NICKNAME);
		if(hasEditorRights(authentication)) {
			newUserSet.setContributor(Collections.singletonList(getUserId(authentication)));
		}
	} else {
		user.setHttpUrl(getUserId(authentication));
		user.setNickname(((ApiCredentials) authentication.getCredentials()).getUserName());
	}
    newUserSet.setCreator(user);
	if (newUserSet.getVisibility() == null) {
	    newUserSet.setVisibility(VisibilityTypes.PRIVATE.getJsonValue());
	}

	if (newUserSet.getType() == null) {
	    newUserSet.setType(UserSetTypes.COLLECTION.getJsonValue());
	}
    }

    /**
     * This method retrieves user id from authentication object
     * 
     * @param authentication
     * @return the user id
     */
    public String getUserId(Authentication authentication) {
	return UserSetUtils.buildCreatorUri((String) authentication.getPrincipal());
    }

    protected boolean hasAdminRights(Authentication authentication) {
	if (authentication == null) {
	    return false;
	}
	return hasRole(authentication, Roles.ADMIN.getName());
    }

	public boolean hasEditorRights(Authentication authentication) {
		if (authentication == null) {
			return false;
		}
		return hasRole(authentication, Roles.EDITOR.getName());
	}

	protected boolean hasRole(Authentication authentication, String roleType) {
		for (Iterator<? extends GrantedAuthority> iterator = authentication.getAuthorities().iterator(); iterator
				.hasNext();) {
			// role based authorization
			String role = iterator.next().getAuthority();
			if (StringUtils.equalsIgnoreCase(roleType, role)) {
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
    String buildSearchApiUrlForClosedSets(UserSet userSet, String apiKey, int pageSize) {
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

    String buildSearchApiUrl(UserSet userSet, String apiKey, String sort, String sortOrder, int pageNr, int pageSize) {

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

    /**
     * This method extracts base URL from the search URL
     * 
     * @param searchUrl
     * @return base URL
     */
    String getBaseSearchUrl(String searchUrl) {
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

	boolean isEntityBestItemsSet(UserSet userSet) {
		return UserSetTypes.ENTITYBESTITEMSSET.getJsonValue().equals(userSet.getType());
	}

    void setItemIds(UserSet userSet, SearchApiResponse apiResult) {
	if (apiResult.getItems() == null) {
	    return;
	}

	List<String> items = new ArrayList<>(apiResult.getItems().size());
	for (String item : apiResult.getItems()) {
	    items.add(UserSetUtils.buildItemUrl(item));
	}
	setItems(userSet, items, apiResult.getTotal());
    }

    void setItems(UserSet userSet, List<String> items, int total) {
//	if (!items.isEmpty()) {
	userSet.setItems(items);
	userSet.setTotal(total);
//	}
    }

	/**
	 * if List<String>subject contains an entity reference
	 * referring to the set then it is a EntityBestItemSet
	 *
	 * @return true if it is an EntityBestItemSet
	 */
	public boolean isEntityReference(UserSet userSet) {
		if (userSet.getSubject() != null) {
			for(String subject : userSet.getSubject()) {
				//TODO verify the entity reference value
				if (subject.startsWith("http://") || subject.startsWith("https://"))
					return true;
			}
		}
		return false;
	}
    /**
     * This method validates and processes the favorite set
     * 
     * @param webUserSet The new user set
     * @throws RequestBodyValidationException
     */
    void validateBookmarkFolder(UserSet webUserSet)
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

    protected abstract UserSet getBookmarkFolder(Agent creator);

    /**
     * This method validates controlled values e.g. type and visibility
     * 
     * @param webUserSet The new user set
     * @throws RequestBodyValidationException
     */
    void validateControlledValues(UserSet webUserSet) throws RequestBodyValidationException {

	if (webUserSet.getVisibility() == null || !VisibilityTypes.isValid(webUserSet.getVisibility())) {
	    throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
		    new String[] { WebUserSetModelFields.VISIBILITY, webUserSet.getVisibility() });
	}

	if (webUserSet.getType() == null || !UserSetTypes.isValid(webUserSet.getType())) {
	    throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
		    new String[] { WebUserSetModelFields.TYPE, webUserSet.getType() });
	}
	// if type is BookmarkFolder or Collection, subject must not contain entity reference
	if (! isEntityBestItemsSet(webUserSet) && isEntityReference(webUserSet)) {
		throw new RequestBodyValidationException(UserSetI18nConstants.INVALID_SUBJECT_VALUE,
			new String[] {webUserSet.getType(), WebUserSetModelFields.SUBJECT, String.valueOf(webUserSet.getSubject())});
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
    void validateIsDefinedBy(UserSet webUserSet) throws ParamValidationException, RequestBodyValidationException {

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
	 * validates the EntityBestItemsSet
	 * for entity user set subject field must have a entity reference.
	 *
	 * @param webUserSet
	 * @throws ParamValidationException
	 * @throws RequestBodyValidationException
	 */
	void validateEntityBestItemsSet(UserSet webUserSet) throws ParamValidationException,
			                                                                 RequestBodyValidationException {
		if (!isEntityBestItemsSet(webUserSet)) {
			return;
		}

		// creator must be present
		if (webUserSet.getCreator() == null || webUserSet.getCreator().getHttpUrl() == null) {
			throw new ParamValidationException(UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
					UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
					new String[] { WebUserSetModelFields.CREATOR });
		}

		// subject must be present
		if (webUserSet.getSubject() == null) {
			throw new ParamValidationException(UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
					UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
					new String[] { WebUserSetModelFields.SUBJECT, String.valueOf(webUserSet.getSubject())});
		}

		// entity user set is a close set
		if (webUserSet.isOpenSet()) {
			throw new ParamValidationException(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_NOT_ALLOWED,
					UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_NOT_ALLOWED,
					new String[] { WebUserSetModelFields.IS_DEFINED_BY, webUserSet.getType() });
		}

		if (! isEntityReference(webUserSet)) {
			throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_VALIDATION_ENTITY_REFERENCE,
					new String[] { WebUserSetModelFields.SUBJECT, String.valueOf(webUserSet.getSubject())});
		}
	}

    void updateTotal(UserSet existingUserSet) {
	if(existingUserSet.getItems() != null) {
	    existingUserSet.setTotal(existingUserSet.getItems().size());
	}else {
	    existingUserSet.setTotal(0);
	}
    }
}
