package eu.europeana.set.web.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.annotation.Resource;

import eu.europeana.set.search.SearchApiRequest;
import eu.europeana.set.web.search.UserSetLdSerializer;
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

	if (updates.getSubject() != null) {
	    persistedSet.setSubject(updates.getSubject());
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
	/**
	 * if entity set, assign entity admin user as a creator
	 * also, add user as 'contributor' if the role is editor
	 * default visibility for Entity set is Public, even if user submits
	 * differently.
	 *  For Pinned sets - set pinned to 0
	 */
	if (StringUtils.equals(newUserSet.getType(), UserSetTypes.ENTITYBESTITEMSSET.getJsonValue())) {
	    newUserSet.setVisibility(VisibilityTypes.PUBLIC.getJsonValue());
	    user.setHttpUrl(UserSetUtils.buildUserUri(getConfiguration().getEntityUserSetUserId()));
	    user.setNickname(WebUserSetModelFields.ENTITYUSER_NICKNAME);
	    newUserSet.setPinned(0);
	    if (hasEditorRights(authentication)) {
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
	return UserSetUtils.buildUserUri((String) authentication.getPrincipal());
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
     * This method retrieves item ids from the closed userSet to build SearchApiRequest.
	 * e.g.
	 * {query='europeana_id:("/165/https___bibdigital_rjb_csic_es_idviewer_11929_40" OR "/2020903/KKSgb2947_97")',
	 * profile = [minimal], start=1, rows=5, sort=europeana_id}
     *
     * @param userSet
     * @param pageSize
     * @return
     * @throws HttpException
     */
    SearchApiRequest buildSearchApiPostBodyForClosedSets(UserSet userSet, int pageSize) {
	// use them to build the search query for retrieving item descriptions using
	// minimal profile
	// europeana_id is in format /collectionId/recordId, this can be easily
	// extracted from the
	// full record ID by removing the base URL http://data.europeana.eu/item
	// e.g. europeana_id:("/08641/1037479000000476635" OR
	// "/08641/1037479000000476943")
	SearchApiRequest searchApiRequest = new SearchApiRequest();
	String id;
	String fullId;
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
	searchApiRequest.setQuery(query.toString());
	searchApiRequest.setProfile(new String[]{CommonApiConstants.PROFILE_MINIMAL});
	searchApiRequest.setRows(maxItems);
	return searchApiRequest;
    }

	/**
	 * Will create the Serach Api post request url
	 * eg : https://api.europeana.eu/record/v2/search.json?wskey=api2demo
	 *
	 * @param userSet
	 * @param apiKey
	 * @return
	 */
    String buildSearchApiPostUrl(UserSet userSet, String apiKey) {
	StringBuilder url = new StringBuilder();
	if (!userSet.isOpenSet()) {
		url.append(getBaseSearchUrl(getConfiguration().getSearchApiUrl()));
	} else {
		url.append(StringUtils.substringBefore(userSet.getIsDefinedBy(), "?"));
	}
	// add apikey
	url.append('?').append(CommonApiConstants.PARAM_WSKEY).append('=').append(apiKey);
	return url.toString();
    }

	/**
	 * Returns the Search APi post Request body
	 * @param userSet
	 * @param apiKey
	 * @param sort
	 * @param sortOrder
	 * @param pageNr
	 * @param pageSize
	 * @return
	 */
    SearchApiRequest buildSearchApiPostBody(UserSet userSet, String sort, String sortOrder, int pageNr, int pageSize) {

	if (!userSet.isOpenSet()) {
	    return buildSearchApiPostBodyForClosedSets(userSet, pageSize);
	}

	SearchApiRequest searchApiRequest = new SearchApiRequest();
	// remove pagination and ordering
	Integer start = pageNr * pageSize + 1;

	searchApiRequest.setQuery(getQueryParamFromURL(userSet.getIsDefinedBy()));
	searchApiRequest.setProfile(new String[]{CommonApiConstants.PROFILE_MINIMAL});
	searchApiRequest.setStart(start);
	searchApiRequest.setRows(pageSize);

	if(sort != null && sortOrder == null) {
		searchApiRequest.setSort(new String[]{sort});
	}
	if (sort != null && sortOrder != null) {
		searchApiRequest.setSort(new String[]{sort + " " + sortOrder});
	}
	return searchApiRequest;
    }

	/**
	 * Returns the query param value from the url passed
	 * @param url
	 * @return
	 */
	private static String getQueryParamFromURL(String url) {
	// decode the url
	String decodedUrl = java.net.URLDecoder.decode(url, StandardCharsets.UTF_8);
    // get the query param value from the getIsDefinedBy
	List<String> queryParam = UriComponentsBuilder.fromUriString(decodedUrl).build().getQueryParams()
			.get(CommonApiConstants.QUERY_PARAM_QUERY);

	StringBuilder query = new StringBuilder();
	if(queryParam != null || !queryParam.isEmpty()) {
		// form the query param for Search
		for(String queryValue : queryParam) {
			query.append(queryValue);
		}
	}
	return query.toString();
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


    private boolean isUri(String value) {
	return value.startsWith("http://") || value.startsWith("https://");
    }

    /**
     * This method validates and processes the favorite set
     * 
     * @param webUserSet The new user set
     * @throws RequestBodyValidationException
     */
    void validateBookmarkFolder(UserSet webUserSet) throws RequestBodyValidationException, ParamValidationException {

	if (!webUserSet.isBookmarksFolder()) {
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
		StringBuilder queryUrl = new StringBuilder(getBaseSearchUrl(validateUrl.toString()));
	    if (!searchUrl.equals(queryUrl.toString())) {
		throw new ParamValidationException(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
			UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
			new String[] { WebUserSetModelFields.IS_DEFINED_BY,
				" the access to api endpoint is not allowed: " + queryUrl });
	    }

	    String apiKey = getConfiguration().getSearchApiKey();
	    SearchApiResponse apiResult;
	    try {
		queryUrl.append('?').append(CommonApiConstants.PARAM_WSKEY).append('=').append(apiKey);
		// the items are not required for validation, hence pageSize =0
		// form the minimal post body
		SearchApiRequest searchApiRequest = buildSearchApiPostBody(webUserSet, null, null, 0, 0);
		String jsonBody = serializeSearchApiRequest(searchApiRequest);

		apiResult = getSearchApiClient().searchItems(queryUrl.toString(), jsonBody , apiKey, false);
	    } catch (SearchApiClientException e) {
		throw new RequestBodyValidationException(
			UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE, new String[] {
				WebUserSetModelFields.IS_DEFINED_BY, "an error occured when calling " + validateUrl },
			e);
	    } catch (IOException e) {
			throw new RequestBodyValidationException(
					UserSetI18nConstants.SEARCH_API_REQUEST_INVALID, new String[] {},
					e);
		}
	    if (apiResult.getTotal() <= 0) {
		throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
			new String[] { WebUserSetModelFields.IS_DEFINED_BY,
				"no items returned when calling " + validateUrl });
	    }
	}
    }

	String serializeSearchApiRequest(SearchApiRequest searchApiRequest) throws IOException {
	UserSetLdSerializer serializer = new UserSetLdSerializer();
	return serializer.serialize(searchApiRequest);
	}
	/**
     * validates the EntityBestItemsSet for entity user set subject field must have
     * a entity reference.
	 *
	 * @param webUserSet
	 * @throws ParamValidationException
	 * @throws RequestBodyValidationException
	 */
    void validateEntityBestItemsSet(UserSet webUserSet)
	    throws ParamValidationException, RequestBodyValidationException {
		if (!webUserSet.isEntityBestItemsSet()) {
			return;
		}

		// creator must be present
		if (webUserSet.getCreator() == null || webUserSet.getCreator().getHttpUrl() == null) {
			throw new ParamValidationException(UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
					UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
					new String[] { WebUserSetModelFields.CREATOR });
		}

	final List<String> subject = webUserSet.getSubject();
	if (subject == null || subject.isEmpty()) {
		// subject must be present
			throw new ParamValidationException(UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
					UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
						new String[] { WebUserSetModelFields.SUBJECT, String.valueOf(subject) });
	} else if (subject.size() != 1 || !isUri(subject.get(0))) {
//	   must include only one HTTP reference
	    throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_VALIDATION_ENTITY_REFERENCE,
		    new String[] { WebUserSetModelFields.SUBJECT, String.valueOf(subject) });

		}

		// entity user set is a close set
		if (webUserSet.isOpenSet()) {
			throw new ParamValidationException(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_NOT_ALLOWED,
					UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_NOT_ALLOWED,
					new String[] { WebUserSetModelFields.IS_DEFINED_BY, webUserSet.getType() });
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
