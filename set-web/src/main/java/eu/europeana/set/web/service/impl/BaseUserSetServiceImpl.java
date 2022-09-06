package eu.europeana.set.web.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import eu.europeana.api.common.config.UserSetI18nConstants;
import eu.europeana.api.commons.config.i18n.I18nService;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.oauth2.model.ApiCredentials;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.api.commons.web.exception.ParamValidationException;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.mongo.service.PersistentUserSetService;
import eu.europeana.set.search.SearchApiRequest;
import eu.europeana.set.search.exception.SearchApiClientException;
import eu.europeana.set.search.service.SearchApiClient;
import eu.europeana.set.search.service.SearchApiResponse;
import eu.europeana.set.search.service.impl.SearchApiClientImpl;
import eu.europeana.set.web.exception.request.RequestBodyValidationException;
import eu.europeana.set.web.model.WebUser;
import eu.europeana.set.web.model.search.CollectionOverview;
import eu.europeana.set.web.model.vocabulary.Roles;
import eu.europeana.set.web.search.UserSetLdSerializer;
import eu.europeana.set.web.service.UserSetService;
import eu.europeana.set.web.service.controller.exception.SetUniquenessValidationException;
import eu.europeana.set.web.utils.UserSetSearchApiUtils;

public abstract class BaseUserSetServiceImpl implements UserSetService{

  @Resource
  PersistentUserSetService mongoPersistance;
  @Resource
  I18nService i18nService;

  UserSetUtils userSetUtils = new UserSetUtils();

  UserSetSearchApiUtils userSetSearchApiUtils = new UserSetSearchApiUtils();

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

  protected UserSetSearchApiUtils getSearchApiUtils() {
    return userSetSearchApiUtils;
  }

  /**
   * @deprecated check if the update test must merge the properties or if it simply overwrites it
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
  // @Override
  public UserSet updateUserSet(PersistentUserSet persistentUserSet, UserSet webUserSet, LdProfiles profile) throws SetUniquenessValidationException, RequestBodyValidationException, ParamValidationException, ApplicationAuthenticationException {
    //###### FIRST Validate the input data, which is allowed to be partial ####/
    resetImmutableFields(webUserSet, persistentUserSet);
    // TODO: move verification to validateMethod when new specs are available
    // TODO: reassess if the type should be kept muable 
    if (persistentUserSet.isOpenSet() && !webUserSet.isOpenSet()) {
    // isDefinedBy is mandatory for open sets
    throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
        new String[] { WebUserSetModelFields.IS_DEFINED_BY + " (for open sets)" });
    }
    //validate input 
    validateWebUserSet(webUserSet);
    
    //merge properties into the persitentUserSet
    mergeUserSetProperties(persistentUserSet, webUserSet);
    
    // validate items
    validateAndSetItems(persistentUserSet, webUserSet, profile);
    // remove duplicated items
    removeItemDuplicates(webUserSet);

    // update modified date
    persistentUserSet.setModified(new Date());
    updateTotal(persistentUserSet);
    UserSet updatedUserSet = getMongoPersistence().update(persistentUserSet);
    return updatedUserSet;

  }

  private void resetImmutableFields(UserSet webUserSet, PersistentUserSet persistentUserSet) {
    // validate and process the Set description for format and mandatory fields
    // if false respond with HTTP 400
    // set immutable fields before validation
    webUserSet.setCreator(persistentUserSet.getCreator());
    webUserSet.setIdentifier(persistentUserSet.getIdentifier());
//  newUserSet.setSubject(existingUserSet.getSubject());
    if (webUserSet.getVisibility() == null) {
      webUserSet.setVisibility(persistentUserSet.getVisibility());
    }
    webUserSet.setContributor(persistentUserSet.getContributor());
  }



  public String buildResultsPageUrl(String apiUrl, String queryString, String searchProfile) {
    if (StringUtils.isNotBlank(queryString)) {
      // remove out of scope parameters
      queryString = removeParam(CommonApiConstants.QUERY_PARAM_PAGE, queryString);
      queryString = removeParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, queryString);
      // facets are not part of items pagination. Facets are displayed separately
      queryString = removeParam(CommonApiConstants.QUERY_PARAM_FACET, queryString);

      // avoid duplication of query parameters
      queryString = removeParam(CommonApiConstants.QUERY_PARAM_PROFILE, queryString);
    }

    // add mandatory parameters
    if (StringUtils.isNotBlank(searchProfile)) {
      if (StringUtils.isNotEmpty(queryString)){
        queryString += "&";
      }
      queryString += (CommonApiConstants.QUERY_PARAM_PROFILE + "=" + searchProfile);
    }

    if (StringUtils.isNotEmpty(queryString)) {
      return apiUrl + "?" + queryString;
    }
    return apiUrl;
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

  protected CollectionOverview buildCollectionOverview(String pageId, String paginationBaseUrl,
      int pageSize, long totalInCollection, int lastPage, String type, LdProfiles profile) {
    String first = null;
    String last = null;

    // do not generate first and last if pageSize=0
    if (totalInCollection > 0 && pageSize > 0) {
      first = buildPageUrl(paginationBaseUrl, 0, pageSize, profile);
      last = buildPageUrl(paginationBaseUrl, lastPage, pageSize, profile);
    }
    return new CollectionOverview(pageId, totalInCollection, first, last, type);
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
    // avoid null divizion if pages size is 0
    if (totalResults > 0 && pageSize > 0) {
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

  @Override
  public String buildPageUrl(String collectionUrl, int page, int pageSize, LdProfiles profile) {
    StringBuilder builder = new StringBuilder(collectionUrl);
    // if collection url already has a query string, then append "&" or else "?"
    if (collectionUrl.contains("?")) {
      builder.append("&");
    } else {
      builder.append("?");
    }
    builder.append(CommonApiConstants.QUERY_PARAM_PAGE).append("=").append(page);
    builder.append("&").append(CommonApiConstants.QUERY_PARAM_PAGE_SIZE).append("=")
        .append(pageSize);
    // add the profile param if profile is not null (search items in set doesn't use a profile)
    boolean hasProfileParam = StringUtils.contains(collectionUrl, CommonApiConstants.QUERY_PARAM_PROFILE+"=");
    if (profile != null && !hasProfileParam) {
      builder.append("&").append(CommonApiConstants.QUERY_PARAM_PROFILE).append("=")
        .append(profile.getRequestParamValue());
    }
    return builder.toString();
  }

    public String buildCollectionUrl(String searchProfile, String requestUrl, String queryString) {
	// remove out of scope parameters
	queryString = removeParam(CommonApiConstants.QUERY_PARAM_PAGE, queryString);
	queryString = removeParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, queryString);
	// facets are not part of items pagination. Facets are displayed separately
	queryString = removeParam(CommonApiConstants.QUERY_PARAM_FACET, queryString);

	// avoid duplication of query parameters
	queryString = removeParam(CommonApiConstants.QUERY_PARAM_PROFILE, queryString);

	// add mandatory parameters
	if (StringUtils.isNotBlank(searchProfile)) {
		if (!queryString.isEmpty()) {
			queryString += "&";
		}
		queryString += (CommonApiConstants.QUERY_PARAM_PROFILE + "=" + searchProfile);

	}
	
	//TODO: verify if base URL should be used instead
	if (!queryString.isEmpty()) {
		return requestUrl+"?"+queryString;
	}
	return requestUrl;
    }



    protected CollectionOverview buildCollectionOverview(String collectionUrl, int pageSize, long totalInCollection,
	    int lastPage, String type, LdProfiles profile) {
	String first = null;
	String last = null;
	
	if(totalInCollection > 0) {
	    first = buildPageUrl(collectionUrl, 0, pageSize, profile);
	    last = buildPageUrl(collectionUrl, lastPage, pageSize, profile);
	}
	return new CollectionOverview(collectionUrl, totalInCollection, first, last, type);
    }

  protected void setDefaults(UserSet newUserSet, Authentication authentication) {
    Agent user = new WebUser();
    /**
     * if entity set, assign entity admin user as a creator also, add user as 'contributor' if the
     * role is editor default visibility for Entity set is Public, even if user submits differently.
     * For Pinned sets - set pinned to 0
     */
    if (StringUtils.equals(newUserSet.getType(), UserSetTypes.ENTITYBESTITEMSSET.getJsonValue())) {
      newUserSet.setVisibility(VisibilityTypes.PUBLIC.getJsonValue());
      user.setHttpUrl(UserSetUtils.buildUserUri(getConfiguration().getUserDataEndpoint(),
          getConfiguration().getEntityUserSetUserId()));
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
    return UserSetUtils.buildUserUri(getConfiguration().getUserDataEndpoint(),
        (String) authentication.getPrincipal());
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
    for (Iterator<? extends GrantedAuthority> iterator =
        authentication.getAuthorities().iterator(); iterator.hasNext();) {
      // role based authorization
      String role = iterator.next().getAuthority();
      if (StringUtils.equalsIgnoreCase(roleType, role)) {
        return true;
      }
    }
    return false;
  }


  protected boolean hasPublisherRights(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return hasRole(authentication, Roles.PUBLISHER.getName());
    }

  void setItemIds(UserSet userSet, SearchApiResponse apiResult) {
    if (apiResult.getItems() == null) {
      return;
    }

    List<String> items = new ArrayList<>(apiResult.getItems().size());
    for (String item : apiResult.getItems()) {
      items.add(UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), item));
    }
    setItems(userSet, items, apiResult.getTotal());
  }

  void setItems(UserSet userSet, List<String> items, int total) {
    userSet.setItems(items);
    userSet.setTotal(total);
  }


  private boolean isUri(String value) {
    return value.startsWith("http://") || value.startsWith("https://");
  }

  /**
   * Gets the profile for pagination urls and item page. Basically gets the profile valid for
   * collection page from the list of profiles passed during search request
   *
   * @param profiles
   * @return
   */
  public LdProfiles getProfileForPagination(List<LdProfiles> profiles) {
    LdProfiles profile = null;
    for (LdProfiles ldProfile : profiles) {
      if (LdProfiles.FACETS != profile) {
        profile = ldProfile;
      }
    }
    
    if(profile == null) {
      profile = LdProfiles.STANDARD;
    }
    return profile;
  }
  
  private void validateAndSetItems(UserSet storedUserSet, UserSet userSetUpdates, LdProfiles profile)
      throws ApplicationAuthenticationException {
    // no validation of items for open sets, they are retrieved dynamically
    if (storedUserSet.isOpenSet()) {
      return;
    }

    // for entity sets update :profile should be minimal and
    // there must not be any items present in new user set
    // only metadata can be update for entity sets
    if (storedUserSet.isEntityBestItemsSet()) {
      if (LdProfiles.MINIMAL != profile) {
        throw new ApplicationAuthenticationException(
            UserSetI18nConstants.USERSET_PROFILE_MINIMAL_ALLOWED,
            UserSetI18nConstants.USERSET_PROFILE_MINIMAL_ALLOWED, new String[] {},
            HttpStatus.PRECONDITION_FAILED, null);
      }
      if (userSetUpdates.getItems() != null && userSetUpdates.getItems().size() > 0) {
        throw new ApplicationAuthenticationException(
            UserSetI18nConstants.USERSET_MINIMAL_UPDATE_PROFILE,
            UserSetI18nConstants.USERSET_MINIMAL_UPDATE_PROFILE, new String[] {},
            HttpStatus.BAD_REQUEST, null);
      }
    }

    // update the Set based on its identifier (replace member items with the new
    // items
    // that are present in the Set description only when a profile is indicated and
    // is
    // different from "ldp:PreferMinimalContainer" is referred in the "Prefer"
    // header)
    // if the provided userset contains a list of items and the profile is set to
    // minimal,
    // respond with HTTP 412)
    if (LdProfiles.MINIMAL == profile) {
      if (userSetUpdates.getItems() != null && userSetUpdates.getItems().size() > 0) { // new user set
                                                                                     // contains
        // items
        throw new ApplicationAuthenticationException(
            UserSetI18nConstants.USERSET_MINIMAL_UPDATE_PROFILE,
            UserSetI18nConstants.USERSET_MINIMAL_UPDATE_PROFILE, new String[] {},
            HttpStatus.PRECONDITION_FAILED, null);
      }
    } else { // it is a Standard profile
      if (userSetUpdates.getItems() == null || userSetUpdates.getItems().size() == 0) { // new user
                                                                                      // set
                                                                                      // contains no
                                                                                      // // items
        throw new ApplicationAuthenticationException(UserSetI18nConstants.USERSET_CONTAINS_NO_ITEMS,
            UserSetI18nConstants.USERSET_CONTAINS_NO_ITEMS, new String[] {},
            HttpStatus.PRECONDITION_FAILED, null);
      }
      storedUserSet.setItems(userSetUpdates.getItems());
    }
  }
  
  public void validateWebUserSet(UserSet webUserSet) throws RequestBodyValidationException,
      ParamValidationException, SetUniquenessValidationException {

    // validate title
    if (webUserSet.getTitle() == null && !webUserSet.isBookmarksFolder()) {
      throw new RequestBodyValidationException(
          UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
          new String[] {WebUserSetModelFields.TITLE});
    }

    // validate isDefinedBy and items - we should not have both of them
    if (webUserSet.getItems() != null && webUserSet.isOpenSet()) {
      throw new RequestBodyValidationException(
          UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_NOT_ALLOWED,
          new String[] {WebUserSetModelFields.ITEMS, WebUserSetModelFields.SET_OPEN});
    }

    // check that the visibility cannot be set to published
    if (webUserSet.isPublished()) {
      throw new ParamValidationException(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
          UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
          new String[] {WebUserSetModelFields.VISIBILITY, webUserSet.getVisibility()});
    }

    validateProvider(webUserSet);
    validateBookmarkFolder(webUserSet);
    validateControlledValues(webUserSet);
    validateIsDefinedBy(webUserSet);
    validateEntityBestItemsSet(webUserSet);
  }
  
  void validateProvider(UserSet webUserSet) throws RequestBodyValidationException {
    if(webUserSet.getProvider() == null) {
      return;
    }
    //check if the provider is provided that it is not an empty object
    if(StringUtils.isBlank(webUserSet.getProvider().getId()) 
        && StringUtils.isBlank(webUserSet.getProvider().getName())) {
      final String message = "must contain either an id or a name.";
      throw new RequestBodyValidationException(
          UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
          new String[] {WebUserSetModelFields.PROVIDER, message}); 
    }
    // check provider id if available
    if (!StringUtils.isBlank(webUserSet.getProvider().getId())) {
      final String providerId = webUserSet.getProvider().getId();
      boolean isAllowedProviderId =
          (providerId.startsWith(WebUserSetFields.PROJECT_EUROPEANA_BASE_URL)
              || providerId.startsWith(WebUserSetFields.DATA_EUROPEANA_BASE_URL));
      if (!isAllowedProviderId) {
        final String message = providerId + " - must be under one of the domains: " +WebUserSetFields.DATA_EUROPEANA_BASE_URL + ", " + WebUserSetFields.PROJECT_EUROPEANA_BASE_URL;
        throw new RequestBodyValidationException(
            UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
            new String[] {WebUserSetModelFields.PROVIDER, message});
      }
    }

  }

  /**
   * This method validates and processes the favorite set
   * 
   * @param webUserSet The new user set
   * @throws RequestBodyValidationException
   */
  void validateBookmarkFolder(UserSet webUserSet)
      throws RequestBodyValidationException, ParamValidationException {

    if (!webUserSet.isBookmarksFolder()) {
      return;
    }

    if (!webUserSet.isPrivate()) {
      throw new ParamValidationException(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
          UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
          new String[] {WebUserSetModelFields.VISIBILITY, webUserSet.getVisibility()});
    }

    if (webUserSet.isOpenSet()) {
      throw new ParamValidationException(
          UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_NOT_ALLOWED,
          UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_NOT_ALLOWED,
          new String[] {WebUserSetModelFields.IS_DEFINED_BY, webUserSet.getType()});
    }

    if (webUserSet.getCreator() == null || webUserSet.getCreator().getHttpUrl() == null) {
      throw new ParamValidationException(UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
          UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
          new String[] {WebUserSetModelFields.CREATOR});
    }

    UserSet usersBookmarkFolder = getBookmarkFolder(webUserSet.getCreator());
    if (usersBookmarkFolder == null) {
      // the user doesn't have yet a bookmark folder
      return;
    }

    // for create method indicate existing bookmark folder
    if (webUserSet.getIdentifier() == null) {
      throw new RequestBodyValidationException(
          UserSetI18nConstants.USERSET_VALIDATION_BOOKMARKFOLDER_EXISTS, new String[] {
              usersBookmarkFolder.getIdentifier(), usersBookmarkFolder.getCreator().getHttpUrl()});
    }

    // for update method indicate the existing bookmark folder (cannot change type
    // to BookmarkFolder)
    if (!webUserSet.getIdentifier().equals(usersBookmarkFolder.getIdentifier())) {
      // update method, prevent creation of 2 BookmarkFolders
      throw new RequestBodyValidationException(
          UserSetI18nConstants.USERSET_VALIDATION_BOOKMARKFOLDER_EXISTS, new String[] {
              usersBookmarkFolder.getIdentifier(), usersBookmarkFolder.getCreator().getHttpUrl()});

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

    if (webUserSet.getVisibility() == null
        || !VisibilityTypes.isValid(webUserSet.getVisibility())) {
      throw new RequestBodyValidationException(
          UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
          new String[] {WebUserSetModelFields.VISIBILITY, webUserSet.getVisibility()});
    }

    if (webUserSet.getType() == null || !UserSetTypes.isValid(webUserSet.getType())) {
      throw new RequestBodyValidationException(
          UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
          new String[] {WebUserSetModelFields.TYPE, webUserSet.getType()});
    }
  }

  /**
   * The value of isDefinedBy is validated (e.g. search URL
   * https://api.europeana.eu/record/search.json?) to point to the Search API. We make a GET request
   * upon creation to see if the request total items returns more then 0 and success is true
   * (meaning is valid).
   * 
   * @param webUserSet
   * @throws ParamValidationException
   * @throws RequestBodyValidationException
   */
  void validateIsDefinedBy(UserSet webUserSet)
      throws ParamValidationException, RequestBodyValidationException {

    if (webUserSet.isOpenSet()) {
      String searchUrl = getSearchApiUtils().getBaseSearchUrl(getConfiguration().getSearchApiUrl());
      StringBuilder queryUrl =
          new StringBuilder(getSearchApiUtils().getBaseSearchUrl(webUserSet.getIsDefinedBy()));
      if (!searchUrl.equals(queryUrl.toString())) {
        throw new ParamValidationException(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
            UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
            new String[] {WebUserSetModelFields.IS_DEFINED_BY,
                " the access to api endpoint is not allowed: " + queryUrl});
      }

      String apiKey = getConfiguration().getSearchApiKey();
      SearchApiResponse apiResult;
      try {
        queryUrl.append('?').append(CommonApiConstants.PARAM_WSKEY).append('=').append(apiKey);
        // the items are not required for validation, hence pageSize =0
        // form the minimal post body
        SearchApiRequest searchApiRequest = getSearchApiUtils().buildSearchApiPostBody(webUserSet,
            getConfiguration().getItemDataEndpoint(), null, null, 0, 0, null);
        String jsonBody = serializeSearchApiRequest(searchApiRequest);

        apiResult = getSearchApiClient().searchItems(queryUrl.toString(), jsonBody, apiKey, false);
      } catch (SearchApiClientException e) {
        throw new RequestBodyValidationException(
            UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
            new String[] {WebUserSetModelFields.IS_DEFINED_BY,
                "an error occured when calling " + webUserSet.getIsDefinedBy()},
            e);
      } catch (IOException e) {
        throw new RequestBodyValidationException(UserSetI18nConstants.SEARCH_API_REQUEST_INVALID,
            null, e);
      }
      if (apiResult.getTotal() <= 0) {
        throw new RequestBodyValidationException(
            UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
            new String[] {WebUserSetModelFields.IS_DEFINED_BY,
                "no items returned when calling " + webUserSet.getIsDefinedBy()});
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
	 * @param webUserSet the user set to verify
	 * @throws ParamValidationException
	 * @throws RequestBodyValidationException
	 * @throws SetUniquenessValidationException
	 */
    void validateEntityBestItemsSet(UserSet webUserSet)
	    throws ParamValidationException, RequestBodyValidationException, SetUniquenessValidationException {
		if (!webUserSet.isEntityBestItemsSet()) {
			return;
		}

    // creator must be present
    if (webUserSet.getCreator() == null || webUserSet.getCreator().getHttpUrl() == null) {
      throw new ParamValidationException(UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
          UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
          new String[] {WebUserSetModelFields.CREATOR});
    }

    // subject field must be present. Only one uri value should be present
    // if the value is an entity reference , make sure we don't save /base in the uri.
    // we have recently chnged the syntax of entity uri so to remove the /base.
    final List<String> subject = webUserSet.getSubject();
    if (subject == null || subject.isEmpty()) {
      // subject must be present
      throw new ParamValidationException(UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
          UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
          new String[] {WebUserSetModelFields.SUBJECT, String.valueOf(subject)});
    } else if (subject.size() != 1 || !isUri(subject.get(0))) {
      // must include only one HTTP reference
      throw new RequestBodyValidationException(
          UserSetI18nConstants.USERSET_VALIDATION_ENTITY_REFERENCE,
          new String[] {WebUserSetModelFields.SUBJECT, String.valueOf(subject)});
    }
    // if present check of entity uri pattern
    if (StringUtils.startsWith(subject.get(0), WebUserSetFields.DATA_EUROPEANA_BASE_URL) && StringUtils.contains(subject.get(0), "/base")) {
      // http://data.europeana.eu/concept/base/114"
      String clean = StringUtils.remove(subject.get(0), "/base");
      subject.clear();
      subject.add(clean);
      webUserSet.setSubject(subject);

    }

    // entity user set is a close set
    if (webUserSet.isOpenSet()) {
      throw new ParamValidationException(
          UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_NOT_ALLOWED,
          UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_NOT_ALLOWED,
          new String[] {WebUserSetModelFields.IS_DEFINED_BY, webUserSet.getType()});
    }
    
    checkDuplicateUserSets(webUserSet);
  }
    
    void checkDuplicateUserSets(UserSet userSet) throws SetUniquenessValidationException {
      //check the set uniqueness only for the EntityBestItemsSet type
      if(UserSetTypes.ENTITYBESTITEMSSET.getJsonValue().equals(userSet.getType())) {
        List<String> duplicateSetsIds = getMongoPersistence().getDuplicateUserSetsIds(userSet);
        if(duplicateSetsIds!=null) {
            String [] i18nParamsSetDuplicates = new String [1];
            i18nParamsSetDuplicates[0]=String.join(",", duplicateSetsIds);
            throw new SetUniquenessValidationException(UserSetI18nConstants.USERSET_DUPLICATION,
                UserSetI18nConstants.USERSET_DUPLICATION, i18nParamsSetDuplicates);
        }
      }
    }

  void updateTotal(UserSet existingUserSet) {
    if (existingUserSet.getItems() != null) {
      existingUserSet.setTotal(existingUserSet.getItems().size());
    } else {
      existingUserSet.setTotal(0);
    }
  }
    
    protected PersistentUserSet updateUserSetForPublish (PersistentUserSet userSet) {
      //update the visibility to publish
      if(!userSet.getVisibility().equalsIgnoreCase(VisibilityTypes.PUBLISHED.getJsonValue())) {
        Agent creator = new WebUser();
        creator.setHttpUrl(UserSetUtils.buildUserUri(getConfiguration().getUserDataEndpoint(), getConfiguration().getEntityUserSetUserId()));
        creator.setNickname(WebUserSetModelFields.ENTITYUSER_NICKNAME);
        userSet.setCreator(creator);
        
        userSet.setVisibility(VisibilityTypes.PUBLISHED.getJsonValue());
        userSet.setModified(new Date());
        return getMongoPersistence().update(userSet);
      } else {
        return userSet;
      }
    }
    
    protected PersistentUserSet updateUserSetForUnpublish (PersistentUserSet userSet, Authentication authentication) {
      //update the visibility to public
      if(userSet.getVisibility().equalsIgnoreCase(VisibilityTypes.PUBLISHED.getJsonValue())) {
        Agent creator = new WebUser();
        creator.setHttpUrl(getUserId(authentication));
        creator.setNickname(((ApiCredentials) authentication.getCredentials()).getUserName());
        userSet.setCreator(creator);
      
        userSet.setVisibility(VisibilityTypes.PUBLIC.getJsonValue());
        userSet.setModified(new Date());
        return getMongoPersistence().update(userSet);
      } else {
        return userSet;
      }

    }
    
}
