package eu.europeana.set.web.service.impl;

import static eu.europeana.set.web.service.authorization.UserSetAuthorizationUtils.getAuthHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import eu.europeana.api.commons_sb3.oauth2.utils.OAuthUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import eu.europeana.api.commons_sb3.definitions.search.result.ResultsPage;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.commons_sb3.error.EuropeanaI18nApiException;
import eu.europeana.api.commons_sb3.error.config.ErrorConfig;
import eu.europeana.api.commons_sb3.error.exceptions.ApplicationAuthenticationException;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidBodyException;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidParamException;
import eu.europeana.api.commons_sb3.oauth2.model.ApiCredentials;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.SetPageProfile;
import eu.europeana.set.definitions.model.vocabulary.UserSetProfile;
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
import eu.europeana.set.web.config.UserSetI18nConstants;
import eu.europeana.set.web.exception.request.ItemValidationException;
import eu.europeana.set.web.exception.request.RequestBodyValidationException;
import eu.europeana.set.web.model.WebUser;
import eu.europeana.set.web.model.search.CollectionOverview;
import eu.europeana.set.web.model.search.SearchApiUtils;
import eu.europeana.set.web.model.vocabulary.Roles;
import eu.europeana.set.web.search.UserSetLdSerializer;
import eu.europeana.set.web.service.UserSetService;
import eu.europeana.set.web.service.controller.exception.SetUniquenessValidationException;
import  jakarta.annotation.Resource;

public abstract class BaseUserSetServiceImpl implements UserSetService {

  @Resource(name = UserSetConfiguration.BEAN_SET_PERSITENCE_SERVICE)
  PersistentUserSetService mongoPersistance;

  @Resource
  UserSetConfiguration configuration;

  UserSetUtils userSetUtils = new UserSetUtils();

  SearchApiUtils userSetSearchApiUtils = SearchApiUtils.getInstance();

  private SearchApiClient searchApiClient = new SearchApiClientImpl();

  Logger logger = LogManager.getLogger(getClass());
  
  
  //update the pagination fields of the set (used only for the serialization to the output)
  protected void updatePagination(UserSet userSet, UserSetConfiguration config) {
    userSetUtils.updatePagination(userSet, config);
  }
 
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
    return searchApiClient;
  }

  protected UserSetConfiguration getConfiguration() {
    return configuration;
  }

  protected SearchApiUtils getSearchApiUtils() {
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

    persistedSet.setCollectionType(updates.getCollectionType());

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
  public UserSet updateUserSet(PersistentUserSet persistentUserSet, UserSet webUserSet,
                               Authentication authentication) throws EuropeanaApiException {
    // ###### FIRST Validate the input data, which is allowed to be partial ####/
    resetImmutableFields(webUserSet, persistentUserSet);
    // TODO: move verification to validateMethod when new specs are available
    // TODO: reassess if the type should be kept muable
    if (persistentUserSet.isOpenSet() && webUserSet.getIsDefinedBy()==null) {
      // isDefinedBy is mandatory for open sets
      throw new InvalidBodyException(
              Collections.singletonMap(UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
                      Arrays.asList(WebUserSetModelFields.IS_DEFINED_BY + " (for open sets)")));
    }
    
    // validate input
    validateWebUserSet(webUserSet, persistentUserSet.isPublished(), authentication);

    // merge properties into the persitentUserSet
    mergeUserSetProperties(persistentUserSet, webUserSet);

    //total and modified are updated in persistence layer
    return getMongoPersistence().store(persistentUserSet);
  }

  private void resetImmutableFields(UserSet webUserSet, PersistentUserSet persistentUserSet) {
    // validate and process the Set description for format and mandatory fields
    // if false respond with HTTP 400
    // set immutable fields before validation
    webUserSet.setCreator(persistentUserSet.getCreator());
    webUserSet.setIdentifier(persistentUserSet.getIdentifier());
    // newUserSet.setSubject(existingUserSet.getSubject());
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
      if (StringUtils.isNotEmpty(queryString)) {
        queryString += "&";
      }
      queryString += (CommonApiConstants.QUERY_PARAM_PROFILE + "=" + searchProfile);
    }

    if (StringUtils.isNotEmpty(queryString)) {
      return apiUrl + "?" + queryString;
    }
    return apiUrl;
  }

  protected String removeParam(final String queryParam, String queryString) {
    String tmp;
    // avoid name conflicts search "queryParam="
    int startPos = queryString.indexOf(queryParam + "=");
    int startEndPos = queryString.indexOf('&', startPos + 1);

    if (startPos >= 0) {
      // make sure to remove the "&" if not the first param
      if (startPos > 0) {
        startPos--;
      }

      tmp = queryString.substring(0, startPos);

      if (startEndPos > 0) {
        // tmp += queryParams.substring(startEndPos);
        tmp = (new StringBuilder(tmp)).append(queryString.substring(startEndPos)).toString();
      }
    } else {
      tmp = queryString;
    }
    return tmp;
  }

  protected CollectionOverview buildCollectionOverview(String pageId, String paginationBaseUrl,
      int pageSize, long totalInCollection, int lastPage, String type, UserSetProfile profile) {
    String first = null;
    String last = null;

    // do not generate first and last if pageSize=0
    if (totalInCollection > 0 && pageSize > 0) {
      first = buildPageUrl(paginationBaseUrl, WebUserSetFields.DEFAULT_PAGE, pageSize, profile);
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
    long lastPage = WebUserSetFields.DEFAULT_PAGE;
    // avoid null divizion if pages size is 0
    if (totalResults > 0 && pageSize > 0) {
      long reaminder = (totalResults % pageSize);
      int extraPage = (reaminder == 0 ? 0 : 1);
      lastPage = ((totalResults / pageSize) + extraPage) + WebUserSetFields.DEFAULT_PAGE - 1;
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
  public String buildPageUrl(String collectionUrl, int page, int pageSize, UserSetProfile profile) {
    StringBuilder builder = new StringBuilder(collectionUrl);
    // if collection url already has a query string, then append "&" or else "?"
    if (collectionUrl.contains("?")) {
      builder.append('&');
    } else {
      builder.append('?');
    }
    builder.append(CommonApiConstants.QUERY_PARAM_PAGE).append('=').append(page);
    builder.append('&').append(CommonApiConstants.QUERY_PARAM_PAGE_SIZE).append('=')
        .append(pageSize);
    // add the profile param if profile is not null (search items in set doesn't use a profile)
    boolean hasProfileParam =
        StringUtils.contains(collectionUrl, CommonApiConstants.QUERY_PARAM_PROFILE + '=');
    if (profile != null && !hasProfileParam) {
      builder.append('&').append(CommonApiConstants.QUERY_PARAM_PROFILE).append('=')
          .append(profile.getProfileParamValue());
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
        queryString += '&';
      }
      queryString += (CommonApiConstants.QUERY_PARAM_PROFILE + '=' + searchProfile);

    }

    // TODO: verify if base URL should be used instead
    if (!queryString.isEmpty()) {
      return requestUrl + "?" + queryString;
    }
    return requestUrl;
  }



  protected CollectionOverview buildCollectionOverview(String collectionUrl, int pageSize,
      long totalInCollection, int lastPage, String type, SetPageProfile profile) {
    String first = null;
    String last = null;

    if (totalInCollection > 0) {
      first = buildPageUrl(collectionUrl, WebUserSetFields.DEFAULT_PAGE, pageSize, profile);
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

  @Override
  public SetPageProfile getProfileForPagination(List<SetPageProfile> profiles, SetPageProfile defaultPageProfile) {
    SetPageProfile ret = null;
    for (SetPageProfile profile : profiles) {
      if (SetPageProfile.FACETS != profile) {
        return ret = profile;
      }
    }
    
    if (ret == null && defaultPageProfile != null) {
      // if only technical profiles included in request, append the default profile
      ret = defaultPageProfile;
      profiles.add(defaultPageProfile);
    }
    return ret;
  }
  
  @Override
  public SetPageProfile getProfileForPagination(List<SetPageProfile> profiles) {
    return getProfileForPagination(profiles, null);
  }

  /**
   * Validate conformity of item URLs
   * @param items
   * @throws ItemValidationException
   */
  protected void validateItems(List<String> items) throws ItemValidationException {
    if(items==null || items.isEmpty()) {
      return;
    }
    List<String> invalidItems = new ArrayList<>();
    for(String item : items) {
      try {
        validateItemWhole(item);
      }
      catch (ItemValidationException ex) {
        invalidItems.add(item);
      }
    }
    if (!invalidItems.isEmpty()) {
      throw new ItemValidationException(UserSetI18nConstants.USERSET_ITEM_INVALID_FORMAT,
              Arrays.asList(invalidItems.toString()));
    }
  }

  //items can be either a uri or a record identifier (e.g. "/1234/XPTO_2")
  protected List<String> validateItemsStrings(List<String> items) throws ItemValidationException {
    List<String> itemsWithFullUrls = new ArrayList<String>(); 
    if(items==null) {
      return null;
    }
    
    List<String> invalidItems = new ArrayList<>();
    String fullUrl;
    for(String item : items) {
      try {
        validateItem(item);
        //convert to fullUrl if needed
        fullUrl = (item.startsWith(getConfiguration().getItemDataEndpoint())) ? 
            item : UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), item);
        //do not include duplicates
        if(!itemsWithFullUrls.contains(fullUrl)) {
          itemsWithFullUrls.add(fullUrl);
        }
      } catch (ItemValidationException ex) {
        logger.trace("Invalid item: {}", item);
        invalidItems.add(item);
      }
    }
    if(!invalidItems.isEmpty()) {
      throw new ItemValidationException(UserSetI18nConstants.USERSET_ITEM_INVALID_FORMAT, Arrays.asList(invalidItems.toString()));
    }
    return itemsWithFullUrls; 
  }

  private void validateItem(String item) throws ItemValidationException {
    String recordId = (item.startsWith(getConfiguration().getItemDataEndpoint())) ? extractRecordId(item) : item;
    validateEuropeanaRecordId(recordId);
  }

  private String extractRecordId(String item) {
    //remove base item url
    String itemWithoutBase = item.substring(getConfiguration().getItemDataEndpoint().length());
    if('/' != itemWithoutBase.charAt(0)) {
      itemWithoutBase = '/' + itemWithoutBase;
    }
    return itemWithoutBase;
  }

  protected void validateItemWhole(String item) throws ItemValidationException {
    if(!item.startsWith(getConfiguration().getItemDataEndpoint())) {
      throw new ItemValidationException(UserSetI18nConstants.USERSET_ITEM_INVALID_FORMAT, Arrays.asList(item));
    }
    else {
      validateEuropeanaRecordId(extractRecordId(item));
    }
  }
  
  /*
   * item validation is also implemented in the recommendation-api and can be moved to api-commons
   */
  protected void validateEuropeanaRecordId(String item) throws ItemValidationException {
     if(! UserSetUtils.EUROPEANA_ID.matcher(item).matches()) {
       throw new ItemValidationException(UserSetI18nConstants.USERSET_ITEM_INVALID_FORMAT, Arrays.asList(item));
     }
  }
  
  public void validateWebUserSet(UserSet webUserSet, boolean isAlreadyPublished, Authentication authentication)
          throws EuropeanaApiException{

    // validate title
    if (webUserSet.getTitle() == null && !webUserSet.isBookmarksFolder()) {
      throw new InvalidBodyException(Collections.singletonMap(
              UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY, Arrays.asList(WebUserSetModelFields.TITLE)));
    }

    // validate open sets
    if (webUserSet.isOpenSet()) {
      //we should not have items for the open sets
      if(webUserSet.getItems() != null) {
        throw new InvalidBodyException(Collections.singletonMap(
                UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_NOT_ALLOWED,
                Arrays.asList(WebUserSetModelFields.ITEMS, WebUserSetModelFields.SET_OPEN)));
      }
      
      //isDefinedBy is mandatory for open sets
      if(webUserSet.getIsDefinedBy() == null) {
        throw new InvalidBodyException(Collections.singletonMap(
                UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY, Arrays.asList(WebUserSetModelFields.IS_DEFINED_BY)));
      }
    }
    else {
      //for sets that are not open sets (closed sets), isDefinedBy is not allowed
      if(webUserSet.getIsDefinedBy() != null) {
        throw new InvalidBodyException(Collections.singletonMap(
                UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_NOT_ALLOWED,
                Arrays.asList(WebUserSetModelFields.IS_DEFINED_BY, WebUserSetModelFields.SET_CLOSED)));
      }
    }

    // prevent updating the state to "published" (must use the publish method for that)
    if (!isAlreadyPublished && webUserSet.isPublished()) {
      throw new InvalidParamException(Arrays.asList(WebUserSetModelFields.VISIBILITY, "", webUserSet.getVisibility()));
    }
    
    validateProvider(webUserSet);
    validateBookmarkFolder(webUserSet);
    validateControlledValues(webUserSet);
    validateAndSanitizeIsDefinedBy(webUserSet, authentication);
    validateEntityBestItemsSet(webUserSet);
    validateItems(webUserSet.getItems());
  }
  
  void validateProvider(UserSet webUserSet) throws InvalidBodyException {
    if (webUserSet.getProvider() == null) {
      return;
    }
    // check if the provider is provided that it is not an empty object
    if (StringUtils.isBlank(webUserSet.getProvider().getId())
        && StringUtils.isBlank(webUserSet.getProvider().getName())) {
      final String message = "must contain either an id or a name.";
      throw new InvalidBodyException(Collections.singletonMap(
              UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE, Arrays.asList(WebUserSetModelFields.PROVIDER, message)));
    }
    // check provider id if available
    if (!StringUtils.isBlank(webUserSet.getProvider().getId())) {
      final String providerId = webUserSet.getProvider().getId();
      boolean isAllowedProviderId =
          (providerId.startsWith(WebUserSetFields.PROJECT_EUROPEANA_BASE_URL)
              || providerId.startsWith(WebUserSetFields.DATA_EUROPEANA_BASE_URL));
      if (!isAllowedProviderId) {
        final String message = providerId + " - must be under one of the domains: "
            + WebUserSetFields.DATA_EUROPEANA_BASE_URL + ", "
            + WebUserSetFields.PROJECT_EUROPEANA_BASE_URL;
        throw new InvalidBodyException(Collections.singletonMap(
            UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE, Arrays.asList(WebUserSetModelFields.PROVIDER, message)));
      }
    }

  }

  /**
   * This method validates and processes the favorite set
   * 
   * @param webUserSet The new user set
   * @throws RequestBodyValidationException
   */
  void validateBookmarkFolder(UserSet webUserSet) throws EuropeanaApiException {

    if (!webUserSet.isBookmarksFolder()) {
      return;
    }

    if (!webUserSet.isPrivate()) {
      throw new InvalidParamException(Arrays.asList(WebUserSetModelFields.VISIBILITY, "", webUserSet.getVisibility()));
    }

    if (webUserSet.isOpenSet()) {
      throw new InvalidBodyException(Collections.singletonMap(
          UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_NOT_ALLOWED,
          Arrays.asList(WebUserSetModelFields.IS_DEFINED_BY, webUserSet.getType())));
    }

    if (webUserSet.getCreator() == null || webUserSet.getCreator().getHttpUrl() == null) {
      throw new InvalidBodyException(Collections.singletonMap(
              UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
              Arrays.asList(WebUserSetModelFields.CREATOR)));
    }

    UserSet usersBookmarkFolder = getBookmarkFolder(webUserSet.getCreator());
    if (usersBookmarkFolder == null) {
      // the user doesn't have yet a bookmark folder
      return;
    }

    // for create method indicate existing bookmark folder
    if (webUserSet.getIdentifier() == null) {
      throw new InvalidBodyException(Collections.singletonMap(
              UserSetI18nConstants.USERSET_VALIDATION_BOOKMARKFOLDER_EXISTS,
              Arrays.asList(usersBookmarkFolder.getIdentifier(), usersBookmarkFolder.getCreator().getHttpUrl())));
    }

    // for update method indicate the existing bookmark folder (cannot change type
    // to BookmarkFolder)
    if (!webUserSet.getIdentifier().equals(usersBookmarkFolder.getIdentifier())) {
      // update method, prevent creation of 2 BookmarkFolders
      throw new InvalidBodyException(Collections.singletonMap(
              UserSetI18nConstants.USERSET_VALIDATION_BOOKMARKFOLDER_EXISTS,
              Arrays.asList(usersBookmarkFolder.getIdentifier(), usersBookmarkFolder.getCreator().getHttpUrl())));
    }
  }

  protected abstract UserSet getBookmarkFolder(Agent creator);

  /**
   * This method validates controlled values e.g. type and visibility
   * 
   * @param webUserSet The new user set
   * @throws RequestBodyValidationException
   */
  void validateControlledValues(UserSet webUserSet) throws InvalidBodyException {

    if (webUserSet.getVisibility() == null
        || !VisibilityTypes.isValid(webUserSet.getVisibility())) {
      throw new InvalidBodyException(Collections.singletonMap(
              UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
              Arrays.asList(WebUserSetModelFields.VISIBILITY, webUserSet.getVisibility())));
    }

    if (webUserSet.getType() == null || !UserSetTypes.isValid(webUserSet.getType())) {
      throw new InvalidBodyException(Collections.singletonMap(
              UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
              Arrays.asList(WebUserSetModelFields.TYPE, webUserSet.getType())));
    }
  }

  /**
   * The value of isDefinedBy is validated (e.g. search URL
   * https://api.europeana.eu/record/search.json?) to point to the Search API. We make a GET request
   * upon creation to see if the request total items returns more then 0 and success is true
   * (meaning is valid).
   * The URL from isDefinedBy is sanitized to remove API Keys if included
   * The URL is stored (without any api key information).
   * 
   * @param webUserSet the user set
   * @throws InvalidBodyException if invocation of isDefinedBy doesn't return results oR if invalid isDefinedBy url
   */
  void validateAndSanitizeIsDefinedBy(UserSet webUserSet, Authentication authentication)
      throws InvalidBodyException {
    if (webUserSet.isOpenSet()) {
      //remove the apikey provided by the user from the isDefinedBy field
      String sanitisedIsDefinedBy = removeParam(OAuthUtils.PARAM_WSKEY, webUserSet.getIsDefinedBy());
      webUserSet.setIsDefinedBy(sanitisedIsDefinedBy);

      SearchApiResponse apiResult = retrieveTotalForOpenSets(webUserSet, authentication);
      if (apiResult.getTotal() <= 0) {
        throw new InvalidBodyException(Collections.singletonMap(
                UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
                Arrays.asList(WebUserSetModelFields.IS_DEFINED_BY,
                        "no items returned when calling " + webUserSet.getIsDefinedBy())));
      }
    }
  }

  /**
   * retrieve total for open sets from SR api
   * 1. Validation : it must check that the base URL is according to what
   *                 is expected for that environment. This is to prevent the
   *                 URL of a Search API acceptance environment (or preview) being used in Set production.
   * 2. SR API call : check by calling the URL that it returns a 200 using the authorisation credentials that are supplied
   *                  as part of the request (not any possible api key information that may have been present in the URL).
   *                  This is to make sure that the URL is properly constructed according to the Search API specifications.
   *
   * @param webUserSet user set
   * @param authentication authentication provided by user
   * @return
   * @throws InvalidBodyException
   */
  @Override
  public SearchApiResponse retrieveTotalForOpenSets(UserSet webUserSet, Authentication authentication) throws InvalidBodyException {
   String queryUrl = baseUrlValidation(webUserSet.getIsDefinedBy());
    try {
      // the items are not required for validation, Only totalResults is fetched
      // hence pageSize =0 and form the minimal post body
      SearchApiRequest searchApiRequest = getSearchApiUtils().buildSearchApiPostBody(webUserSet,
          getConfiguration().getItemDataEndpoint(), null, null, 0, 0, null);
      String jsonBody = serializeSearchApiRequest(searchApiRequest);

      return getSearchApiClient().searchItems(
              queryUrl,
              jsonBody,
              getAuthHandler(authentication),
              false);

    } catch (SearchApiClientException e) {
      throw new InvalidBodyException(Collections.singletonMap(
              UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
              Arrays.asList(WebUserSetModelFields.IS_DEFINED_BY,
                      "an error occured when calling " + webUserSet.getIsDefinedBy())), e);
    } catch (IOException e) {
      throw new InvalidBodyException(Collections.singletonMap(
              UserSetI18nConstants.SEARCH_API_REQUEST_INVALID, Collections.emptyList()), e);
    }
  }

  /**
   * Validation : it must check that the base URL is according to what
   * is expected for that environment. This is to prevent the URL of a
   * Search API acceptance environment (or preview) being used in Set production.
   *
   * @param isDefinedBy isDefinedBy url of the user set
   */
  private String baseUrlValidation(String isDefinedBy) throws InvalidBodyException {
    String searchUrl = getConfiguration().getSearchApiUrl();

    StringBuilder queryUrl = new StringBuilder(getSearchApiUtils().getBaseSearchUrl(isDefinedBy));

    if (!searchUrl.equals(queryUrl.toString())) {
      throw new InvalidBodyException(Collections.singletonMap(
              UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
              Arrays.asList(WebUserSetModelFields.IS_DEFINED_BY,
                      " the access to api endpoint is not allowed: " + queryUrl)));
    }
    return queryUrl.toString();
  }


  String serializeSearchApiRequest(SearchApiRequest searchApiRequest) throws IOException {
    UserSetLdSerializer serializer = new UserSetLdSerializer();
    return serializer.serialize(searchApiRequest);
  }

  /**
   * validates the EntityBestItemsSet for entity user set subject field must have a entity
   * reference.
   * 
   * @param webUserSet the user set to verify
   * @throws RequestBodyValidationException
   * @throws SetUniquenessValidationException
   */
  void validateEntityBestItemsSet(UserSet webUserSet) throws InvalidBodyException, SetUniquenessValidationException {
    if (!webUserSet.isEntityBestItemsSet()) {
      return;
    }

    // creator must be present
    if (webUserSet.getCreator() == null || webUserSet.getCreator().getHttpUrl() == null) {
      throw new InvalidBodyException(Collections.singletonMap(
              UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
              Arrays.asList(WebUserSetModelFields.CREATOR)));
    }

    // subject field must be present. Only one uri value should be present
    // if the value is an entity reference , make sure we don't save /base in the uri.
    final List<String> subject = webUserSet.getSubject();
    if (subject == null || subject.isEmpty()) {
      // subject must be present
      throw new InvalidBodyException(Collections.singletonMap(
              UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
              Arrays.asList(WebUserSetModelFields.SUBJECT, String.valueOf(subject))));
    } else if (subject.size() != 1 || !isUri(subject.get(0))) {
      // must include only one HTTP reference
      throw new InvalidBodyException(Collections.singletonMap(
              UserSetI18nConstants.USERSET_VALIDATION_ENTITY_REFERENCE,
              Arrays.asList(WebUserSetModelFields.SUBJECT, String.valueOf(subject))));
    }
    // if present check of entity uri pattern
    if (StringUtils.startsWith(subject.get(0), WebUserSetFields.DATA_EUROPEANA_BASE_URL)
        && StringUtils.contains(subject.get(0), WebUserSetFields.ENTITY_URI_BASE)) {
      // must include only one HTTP reference
      throw new InvalidBodyException(Collections.singletonMap(
              UserSetI18nConstants.USERSET_VALIDATION_ENTITY_URI,
              Arrays.asList(WebUserSetModelFields.SUBJECT, String.valueOf(subject))));
    }

    // entity user set is a close set
    if (webUserSet.isOpenSet()) {
      throw new InvalidBodyException(Collections.singletonMap(
              UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_NOT_ALLOWED,
              Arrays.asList(WebUserSetModelFields.IS_DEFINED_BY, webUserSet.getType())));
    }

    checkDuplicateUserSets(webUserSet);
  }

  void checkDuplicateUserSets(UserSet userSet) throws SetUniquenessValidationException {
    // check the set uniqueness only for the EntityBestItemsSet type
    if (UserSetTypes.ENTITYBESTITEMSSET.getJsonValue().equals(userSet.getType())) {
      List<String> duplicateSetsIds = getMongoPersistence().getDuplicateUserSetsIds(userSet);
      if (duplicateSetsIds != null) {
        String[] i18nParamsSetDuplicates = new String[1];
        i18nParamsSetDuplicates[0] = String.join(",", duplicateSetsIds);
        throw new SetUniquenessValidationException(null, UserSetI18nConstants.USERSET_DUPLICATION,
           Arrays.asList(i18nParamsSetDuplicates));
      }
    }
  }

  PersistentUserSet updateUserSetForPublish(PersistentUserSet userSet, Date issued, Authentication authentication){
    // update the visibility to publish
    if (isOwner(userSet, authentication)) {
      // if the requesting user is the owner of the gallery, the ownership is reassigned to
      // @europeana
      Agent creator = buildEuropeanaPublisherUser();
      userSet.setCreator(creator);
    }
    userSet.setVisibility(VisibilityTypes.PUBLISHED.getJsonValue());
    Date now = new Date();
    if(issued==null) {
      issued=now;
    }
    userSet.setIssued(issued);
    userSet.setModified(now);
    return getMongoPersistence().store(userSet);
  }

  private Agent buildEuropeanaPublisherUser() {
    Agent creator = new WebUser();
    creator.setHttpUrl(UserSetUtils.buildUserUri(getConfiguration().getUserDataEndpoint(),
        getConfiguration().getEuropeanaPublisherId()));
    creator.setNickname(getConfiguration().getEuropeanaPublisherNickname());
    return creator;
  }

  PersistentUserSet updateUserSetForUnpublish(PersistentUserSet userSet,
      Authentication authentication) {
    // update the visibility to public
    if (hasPublisherAsOwner(userSet)) {
      // if the owner is @europeana, then the ownership is reassigned to the requesting user
      Agent creator = buildUserFromAuthentication(authentication);
      userSet.setCreator(creator);
    }
    
    userSet.setVisibility(VisibilityTypes.PUBLIC.getJsonValue());
    userSet.setIssued(null);
    userSet.setModified(new Date());
    return getMongoPersistence().store(userSet);
  }

  private boolean hasPublisherAsOwner(PersistentUserSet userSet) {
    return isOwner(userSet, getConfiguration().getEuropeanaPublisherId());
  }

  private Agent buildUserFromAuthentication(Authentication authentication) {
    Agent creator = new WebUser();
    creator.setHttpUrl(getUserId(authentication));
    creator.setNickname(((ApiCredentials) authentication.getCredentials()).getUserName());
    return creator;
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
    final String userIdentifier = (String) authentication.getPrincipal();
    return isOwner(userSet, userIdentifier);
  }

  protected boolean isOwner(UserSet userSet, final String userIdentifier) {
    if (userSet.getCreator() == null || userSet.getCreator().getHttpUrl() == null) {
      return false;
    }
    String userId =
        UserSetUtils.buildUserUri(getConfiguration().getUserDataEndpoint(), userIdentifier);
    return userSet.getCreator().getHttpUrl().equals(userId);
  }

  protected int calculatePosition(int position, List<String> items) {
    int positionFinal = items.size();
    if (position >= 0 && position < items.size()) {
      positionFinal = position;
    }
    return positionFinal;
  }

  protected void addPagination(ResultsPage<?> resPage, String collectionUrl, int page, int pageSize, int lastPage,
      UserSetProfile profile) {
        String currentPageUrl = buildPageUrl(collectionUrl, page, pageSize, profile);
        resPage.setCurrentPageUri(currentPageUrl);
      
        if (page > WebUserSetFields.DEFAULT_PAGE) {
          String prevPage = buildPageUrl(collectionUrl, page - 1, pageSize, profile);
          resPage.setPrevPageUri(prevPage);
        }
      
        // if current page is not the last one
        if (!isLastPage(page, lastPage)) {
          String nextPage = buildPageUrl(collectionUrl, page + 1, pageSize, profile);
          resPage.setNextPageUri(nextPage);
        }
      }

  protected String buildSetIdUrl(final String identifier) {
    return getConfiguration().getSetDataEndpoint() + identifier;
  }

  protected int validateLastPage(long totalInCollection, int pageSize, int pageNr) throws InvalidBodyException {
        int lastPage = getLastPage(totalInCollection, pageSize);
        if (pageNr > lastPage) {
          throw new InvalidBodyException(Collections.singletonMap(
                  UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
                  Arrays.asList(CommonApiConstants.QUERY_PARAM_PAGE,
                          "value out of range: " + pageNr + ", last page:" + lastPage)));
        }
        return lastPage;
      }

  /**
   * This method validates if the user is the owner/creator of the userset or the admin
   * 
   * @param userSet
   * @param authentication
   * @return
   * @return userSet object
   * @throws EuropeanaI18nApiException
   */
  @Override
  public UserSet verifyOwnerOrAdmin(UserSet userSet, Authentication authentication, boolean includeEntitySetMsg) throws EuropeanaI18nApiException {
    return verifyOwnerOrAdminOrRole(userSet, authentication, null, includeEntitySetMsg);
  }

  /**
   * This method validates if the user is the owner/creator of the userset or the admin
   * 
   * @param userSet the user set to verify access
   * @param authentication the authentication token
   * @param role optional role granting access
   * @return the userset if the access is granted
   * @throws EuropeanaI18nApiException if hte access is not granted
   */
  protected UserSet verifyOwnerOrAdminOrRole(UserSet userSet, Authentication authentication, String role, boolean includeEntitySetMsg)
      throws EuropeanaI18nApiException {
      
        if (authentication == null) {
          // access by API KEY, authentication not available
          throw new ApplicationAuthenticationException( null,
                  UserSetI18nConstants.USER_NOT_AUTHORIZED,
                  Arrays.asList("Access to update operations of private User Sets require user authentication with JwtToken"),
                  HttpStatus.FORBIDDEN);
        }
      
        // verify ownership
        if (isOwner(userSet, authentication) || hasAdminRights(authentication)) {
          // approve owner or admin
          return userSet;
        }
        if (role != null && hasRole(authentication, role)) {
          // approve usr with role if provided
          return userSet;
        } else {
          // not authorized
          StringBuilder message = new StringBuilder();
          if (includeEntitySetMsg) {
            message.append(
                "Only the contributors, creator of the entity user set or admins are authorized to perform this operation.");
          } else {
            message.append(
                "Only the creators of the user set or admins are authorized to perform this operation.");
          }
          throw new ApplicationAuthenticationException(null,
                  ErrorConfig.OPERATION_NOT_AUTHORIZED,
                  Arrays.asList(message.toString()),
                  HttpStatus.FORBIDDEN);
        }
      }

  /**
   * This method checks the permission to create or Update the entity user sets for entity sets
   * creation or updating the items: 1) 'contributors' (users with editor role) 2) owner or admin ;
   * all three are allowed to create/update the entity set
   *
   * @param existingUserSet
   * @param authentication
   * @throws
   */
  public void verifyPermissionToUpdate(UserSet existingUserSet, Authentication authentication, boolean includeEntitySetMsg)
          throws EuropeanaI18nApiException {
        if (existingUserSet.isEntityBestItemsSet() && hasEditorRole(authentication)) {
          return;
        }
        // verifyOwnerOrAdmin(existingUserSet, authentication, includeEntitySetMsg);
        if (existingUserSet.isPublished()) {
          verifyOwnerOrAdminOrRole(existingUserSet, authentication, Roles.PUBLISHER.getName(), false);
        } else {
          verifyOwnerOrAdmin(existingUserSet, authentication, false);
        }
      }
}