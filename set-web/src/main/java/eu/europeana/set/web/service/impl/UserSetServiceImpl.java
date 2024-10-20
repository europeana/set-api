package eu.europeana.set.web.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.api.commons.definitions.config.i18n.I18nConstants;
import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.api.commons.definitions.search.result.ResultsPage;
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
import eu.europeana.set.definitions.model.search.UserSetFacetQuery;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.search.SearchApiRequest;
import eu.europeana.set.search.exception.SearchApiClientException;
import eu.europeana.set.search.service.SearchApiResponse;
import eu.europeana.set.web.config.UserSetI18nConstants;
import eu.europeana.set.web.exception.request.ItemValidationException;
import eu.europeana.set.web.exception.request.RequestBodyValidationException;
import eu.europeana.set.web.exception.request.RequestValidationException;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.model.WebUserSetImpl;
import eu.europeana.set.web.model.search.BaseUserSetResultPage;
import eu.europeana.set.web.model.search.CollectionOverview;
import eu.europeana.set.web.model.search.CollectionPage;
import eu.europeana.set.web.model.search.FacetFieldViewImpl;
import eu.europeana.set.web.model.search.ItemDescriptionsCollectionPage;
import eu.europeana.set.web.model.search.ItemIdsCollectionPage;
import eu.europeana.set.web.model.search.ItemIdsResultPage;
import eu.europeana.set.web.model.search.UserSetIdsResultPage;
import eu.europeana.set.web.model.search.UserSetResultPage;
import eu.europeana.set.web.model.vocabulary.Roles;
import ioinformarics.oss.jackson.module.jsonld.JsonldModule;

public class UserSetServiceImpl extends BaseUserSetServiceImpl {

  /*
   * (non-Javadoc)
   * 
   * @see eu.europeana.UserSet.web.service.UserSetService#storeUserSet(eu.
   * europeana.UserSet.definitions.model.UserSet)
   */
  @Override
  public UserSet storeUserSet(UserSet newUserSet, Authentication authentication)
      throws HttpException {
    setDefaults(newUserSet, authentication);
    if (newUserSet.isEntityBestItemsSet()) {
      verifyPermissionToUpdate(newUserSet, authentication, true);
    }

    //new sets are not yet published
    validateWebUserSet(newUserSet, false);

    // store in mongo database
    updateTotal(newUserSet);
    UserSet updatedUserSet = getMongoPersistence().store(newUserSet);
    getUserSetUtils().updatePagination(updatedUserSet, getConfiguration());
    return updatedUserSet;
  }

  @Override
  public UserSet getUserSetById(String userSetId) throws UserSetNotFoundException {
    UserSet userSet = getMongoPersistence().getByIdentifier(userSetId);
    if (userSet == null) {
      throw new UserSetNotFoundException(UserSetI18nConstants.USERSET_NOT_FOUND,
          UserSetI18nConstants.USERSET_NOT_FOUND, new String[] {userSetId});
    }
    getUserSetUtils().updatePagination(userSet, getConfiguration());
    return userSet;
  }

  @Override
  public List<PersistentUserSet> getUserSetByCreatorId(String creatorId)
      throws UserSetNotFoundException {
    ArrayList<PersistentUserSet> result = new ArrayList<>();
    Iterator<PersistentUserSet> iter = getMongoPersistence().getByCreator(creatorId).iterator();
    iter.forEachRemaining(result::add);
    
    return result;
  }

  /**
   * This method checks if a user set with provided type and user already exists in database
   * 
   * @param creator
   * @return null or existing bookarks folder
   */
  public UserSet getBookmarkFolder(Agent creator) {
    return getBookmarkFolder(creator.getHttpUrl());
  }

  public UserSet getBookmarkFolder(String creatorId) {
    UserSet set = getMongoPersistence().getBookmarkFolder(creatorId);
    if (set != null) {
      set.setBaseUrl(getConfiguration().getSetDataEndpoint());
    }
    return set;
  }

  @Override
  public UserSet parseUserSetLd(String userSetJsonLdStr)
      throws RequestBodyValidationException, UserSetInstantiationException {

    JsonParser parser;
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonldModule());
    mapper.configure(Feature.AUTO_CLOSE_SOURCE, true);
    // mapper.configure(MapperFeature.AUTO_DETECT_SETTERS, false);

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
          new String[] {e.getMessage()}, e);
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
      List<String> distinctItems =
          userSet.getItems().stream().distinct().collect(Collectors.toList());
      userSet.setItems(distinctItems);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.europeana.set.web.service.UserSetService#deleteUserSet(java.lang.String)
   */
  public void deleteUserSet(String userSetId) throws UserSetNotFoundException {

    getMongoPersistence().remove(userSetId);
  }

  /*
   * (non-Javadoc)
   *
   * @see eu.europeana.set.web.service.UserSetService#deleteUserSets(java.lang.String)
   */
  public void deleteUserSets(String creatorId, List<PersistentUserSet> userSets) {
    List<String> setsToBeDeleted = new ArrayList<>();
    if (!userSets.isEmpty()) {
      for (PersistentUserSet userSet : userSets) {
        setsToBeDeleted.add(userSet.getIdentifier());
      }
    }
    getMongoPersistance().removeAll(userSets);
    getLogger().info("User sets deleted for user {}. Sets deleted are : {} ", creatorId,
        setsToBeDeleted);
  }

  /**
   * This method validates position input, if false responds with -1
   * 
   * @param position The given position
   * @param items The item list
   * @return position The validated position in list to insert
   * @throws ApplicationAuthorizationException
   */
  int validatePosition(String position, List<String> items, int pinnedItems) {
    int positionInt = -1;
    if (StringUtils.isNotEmpty(position)) {
      try {
        positionInt = Integer.parseInt(position);
        // if position less than pinned items
        // change the position from the initial start of Entity sets items
        if (positionInt < pinnedItems) {
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
   * @see eu.europeana.set.web.service.UserSetService#insertItem(java.lang.String, java.lang.String,
   * java.lang.String, eu.europeana.set.definitions.model.UserSet)
   */
  public UserSet insertItem(String datasetId, String localId, String position,
      UserSet existingUserSet) throws ApplicationAuthenticationException, ItemValidationException {
    String itemForPartialValidation = "/" + datasetId + "/" + localId;
    validateItemPartial(itemForPartialValidation);

    String newItem =
        UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), datasetId, localId);
    
    //check max number of items for the sets of type Collection
    if(existingUserSet.isCollection() && !existingUserSet.hasItem(newItem)) {
      validateGallerySize(existingUserSet, 1);  
    }

    // check if the position is "pin" and is a EntityBestItem set then
    // insert the item at the 0 position
    UserSet userSet;

    if (WebUserSetModelFields.PINNED_POSITION.equals(position)
        && existingUserSet.isEntityBestItemsSet()) {
      userSet = insertItem(existingUserSet, newItem, 0, true);
    } else {
      // validate position
      // -1 if invalid
      int positionInt =
          validatePosition(position, existingUserSet.getItems(), existingUserSet.getPinned());
      userSet = insertItem(existingUserSet, newItem, positionInt, false);
    }
    getUserSetUtils().updatePagination(userSet, getConfiguration());
    return userSet;
  }  

  /**
   * check if item already exists in the Set, if so remove it insert item to Set in the indicated
   * position (or last position if no position was indicated).
   *
   * For entity sets : if pinnedItem : Then increase the counter by one while adding the item in the
   * pinned list. While replacing the item : 1) if pinned item is changed into item , decrease the
   * counter 2) if item is changed into pinned item , increase the counter 3) if the position is
   * same for item/pinned item, counter remains same in both the cases
   *
   * NOTE : Pinned value should be modified only for entity sets
   *
   * @param existingUserSet
   * @param newItem
   * @param positionInt
   * @return
   */
  private UserSet insertItem(UserSet existingUserSet, String newItem, int positionInt,
      boolean pinnedItem) {
    UserSet extUserSet = null;
    int finalPosition = (existingUserSet.getItems() == null) ? -1 : positionInt;
    final boolean insertOrReplace =
        existingUserSet.getItems() == null || !existingUserSet.getItems().contains(newItem);
    if (insertOrReplace) {
      // add item && create item list if needed
      addNewItemToList(existingUserSet, finalPosition, newItem);
      updatePinCount(existingUserSet, pinnedItem, -1);
      extUserSet = updateItemList(existingUserSet);
    } else {
      // replace item
      int oldPosition = existingUserSet.getItems().indexOf(newItem);
      if (oldPosition == positionInt) {
        // do not change user set
        // the items is already present at the correct position
        extUserSet = existingUserSet;
      } else {
        replaceItem(existingUserSet, finalPosition, newItem);
        updatePinCount(existingUserSet, pinnedItem, oldPosition);
        extUserSet = updateItemList(existingUserSet);
      }
    }

    return extUserSet;
  }
  
  private void updatePinCount(UserSet existingUserSet, boolean pinnedItem, int oldPosition) {
    boolean mustHandlePinCount = existingUserSet.isEntityBestItemsSet();
    if (!mustHandlePinCount) {
      return;
    }

    final boolean previouslyPinned =
        (oldPosition >= 0) && (oldPosition < existingUserSet.getPinned());
    if (previouslyPinned && !pinnedItem) {
      // DECREASE PIN COUNT
      // For entity sets : if existing item is converted from Pinned --> Normal item,
      // decrease the pinned counter
      // ie; already existing Normal item being added as a pinned item now
      // This condition will avoid any changes in Pinned counter:
      // while adding a already existing Normal item as a Normal item again in
      // different position
      // As the old position of Normal item will always be greater than
      // existingUserSet.getPinned()
      existingUserSet.setPinned(existingUserSet.getPinned() - 1);
    } else if (pinnedItem) {
      // increase only if pinned item (do not increase for normal items)
      existingUserSet.setPinned(existingUserSet.getPinned() + 1);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.europeana.set.web.service.UserSetService#updateItemList(eu.europeana.set.
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
    getUserSetUtils().updatePagination(updatedUserSet, getConfiguration());
    return updatedUserSet;
  }

  /**
   * This method replaces item in user set
   * 
   * @param existingUserSet
   * @param positionInt
   * @param newItem
   */
  void replaceItem(UserSet existingUserSet, int positionInt, String newItem) {
    existingUserSet.getItems().remove(newItem);
    // if item already existed, the size of item list has changed
    // Check to avoid IndexOutOfBoundsException
    if (positionInt > existingUserSet.getItems().size()) {
      positionInt = existingUserSet.getItems().size();
    }
    addNewItemToList(existingUserSet, positionInt, newItem);
  }

  /**
   * Add item to the list in given position if provided.
   * 
   * @param existingUserSet
   * @param positionInt
   * @param newItem
   */
  void addNewItemToList(UserSet existingUserSet, int positionInt, String newItem) {

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
  public UserSet fetchItems(UserSet userSet, String sort, String sortOrder, int pageNr,
      int pageSize, LdProfiles profile) throws HttpException {
    if (!userSet.isOpenSet() && (userSet.getItems() == null
        || (userSet.getItems() != null && userSet.getItems().isEmpty()))) {
      // if empty closed userset, nothing to do
      return userSet;
    }
    // for non-empty close-userset, do page validation before fetching items if pageNr exceeds the
    // last page
    // this is also to avoid sending any empty request to search api.
    if (!userSet.isOpenSet() && userSet.getItems().size() > 0) {
      validateLastPage(userSet.getItems().size(), pageSize, pageNr);
    }
    String apiKey = getConfiguration().getSearchApiKey();
    String searchApiProfile = null;
    searchApiProfile = getConfiguration().getSearchApiProfileForItemDescriptions();

    String url = getSearchApiUtils().buildSearchApiPostUrl(userSet, apiKey,
        getConfiguration().getSearchApiUrl(), searchApiProfile);
    SearchApiRequest searchApiRequest = getSearchApiUtils().buildSearchApiPostBody(userSet,
        getConfiguration().getItemDataEndpoint(), sort, sortOrder, pageNr, pageSize,
        searchApiProfile);
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
          // use the count of item ids
          total = userSet.getItems().size();
        }
        List<String> sortedItemDescriptions =
            sortItemDescriptions(userSet, apiResult.getItems(), pageNr, pageSize);        
        setItems(userSet, sortedItemDescriptions, total);
      }
      return userSet;
    } catch (SearchApiClientException e) {
      if (SearchApiClientException.MESSAGE_INVALID_ISDEFINEDNBY.equals(e.getMessage())) {
        throw new RequestBodyValidationException(
            UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
            new String[] {WebUserSetModelFields.IS_DEFINED_BY, url}, e);
      } else {
        throw new InternalServerException(e);
      }
    } catch (IOException e) {
      throw new RequestBodyValidationException(UserSetI18nConstants.SEARCH_API_REQUEST_INVALID,
          new String[] {}, e);
    }
  }

  private List<String> sortItemDescriptions(UserSet userSet, List<String> itemDescriptions,
      int pageNr, int pageSize) {
    
    if (userSet.getItems() != null) {
      return reorderItemDescriptions(userSet, itemDescriptions, pageNr, pageSize);
    }
    // if open set OR userSet.getItems == null , return the same order as retrieved
    return itemDescriptions;
  }

  private List<String> reorderItemDescriptions(UserSet userSet, List<String> itemDescriptions,
      int pageNr, int pageSize) {
    List<String> orderedItemDescriptions = new ArrayList<String>(itemDescriptions.size());
    String localId;
    
    // calculate the index of from and until to get the right page of items
    Integer start = (pageNr - UserSetUtils.DEFAULT_PAGE) * pageSize;
    Integer till = Math.min((start + pageSize), userSet.getItems().size()); // should not exceed
                                                                            // the size of item
                                                                            // list
    for (int i = start; i < till; i++) {
      String itemUri = userSet.getItems().get(i);
      boolean found = false;
      localId =
          UserSetUtils.extractItemIdentifier(itemUri, getConfiguration().getItemDataEndpoint());
      // escape "/" to "\/" to match json string
      localId = StringUtils.replace(localId, "/", "\\/");
      String idWithSpace="\"id\": \"" + localId + '"';
      for (String description : itemDescriptions) {
        if (description.contains(idWithSpace)) {
          orderedItemDescriptions.add(description);
          found = true;
          break;
        }
      }
      if (!found) {
        orderedItemDescriptions.add("{\"id\":\"" + localId + "\"}");
      }
    }
    return orderedItemDescriptions;
  }

  private int validateLastPage(long totalInCollection, int pageSize, int pageNr)
      throws ParamValidationException {
    int lastPage = getLastPage(totalInCollection, pageSize);
    if (pageNr > lastPage) {
      throw new ParamValidationException(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
          UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
          new String[] {CommonApiConstants.QUERY_PARAM_PAGE,
              "value out of range: " + pageNr + ", last page:" + lastPage});
    }
    return lastPage;
  }

  @Override
  public ResultSet<? extends UserSet> search(UserSetQuery searchQuery, UserSetFacetQuery facetQuery,
      List<LdProfiles> profiles, Authentication authentication) {
    // add user information for visibility filtering criteria
    searchQuery.setAdmin(hasAdminRights(authentication));
    searchQuery.setUser(getUserId(authentication));
    ResultSet<PersistentUserSet> results = getMongoPersistance().find(searchQuery);
    // get facets
    if (profiles.contains(LdProfiles.FACETS) && facetQuery != null) {
      Map<String, Long> valueCountMap = getMongoPersistence().getFacets(facetQuery);
      results.setFacetFields(
          Arrays.asList(new FacetFieldViewImpl(facetQuery.getOutputField(), valueCountMap)));
    }
    return results;
  }


  @Override
  public BaseUserSetResultPage<?> buildResultsPage(UserSetQuery searchQuery,
      ResultSet<? extends UserSet> results, String requestUrl, String reqParams,
      List<LdProfiles> profiles, Authentication authentication) throws HttpException {

    BaseUserSetResultPage<?> resPage = null;
    int resultPageSize = results.getResults().size();
    int pageSize = searchQuery.getPageSize();
    int currentPage = searchQuery.getPageNr();
    long totalInCollection = results.getResultSize();

    int lastPage = validateLastPage(totalInCollection, pageSize, currentPage);
    // get profile for pagination urls and item Page
    LdProfiles profile = getProfileForPagination(profiles);

    String apiEndpointUrl = getConfiguration().getSetApiEndpoint() + "search";
    // 'id' field of the page Url
    String resultsPageId =
        buildResultsPageUrl(apiEndpointUrl, reqParams, profile.getRequestParamValue());

    // we don't want to add profile in partOf, hence profile is passed null
    // pageId is the same as the baseUrl for pagination
    CollectionOverview ResultList = buildCollectionOverview(resultsPageId, resultsPageId, pageSize,
        totalInCollection, lastPage, CommonLdConstants.RESULT_LIST, null);

    if (profiles.contains(LdProfiles.STANDARD) || profiles.contains(LdProfiles.ITEMDESCRIPTIONS)) {
      resPage = new UserSetResultPage();
      // LdProfiles.ITEMDESCRIPTIONS OR LdProfiles.STANDARD is passed as profile
      setPageItems(results, (UserSetResultPage) resPage, authentication, profile);
    } else {
      // LdProfiles.MINIMAL.equals(profile) - default
      resPage = setPageItemsAsSetIds(results, resultPageSize);
    }

    resPage.setPartOf(ResultList);
    if (profiles.contains(LdProfiles.FACETS)) {
      resPage.setFacetFields(results.getFacetFields());
    }
    addPagination(resPage, resultsPageId, currentPage, pageSize, lastPage, profile);
    return resPage;
  }

  UserSetIdsResultPage setPageItemsAsSetIds(ResultSet<? extends UserSet> results,
      int resultPageSize) {
    
    UserSetIdsResultPage resPage = new UserSetIdsResultPage();
    List<String> items = new ArrayList<>(resultPageSize);
    for (UserSet set : results.getResults()) {
      items.add(UserSetUtils.buildUserSetId(getConfiguration().getSetDataEndpoint(),
          set.getIdentifier()));
    }
    resPage.setItems(items);
    resPage.setTotalInPage(items.size());
    return resPage;
  }

  void setPageItems(ResultSet<? extends UserSet> results, UserSetResultPage resPage,
      Authentication authentication, LdProfiles profile) throws HttpException {
    List<UserSet> items = new ArrayList<>(results.getResults().size());

    // TODO: define a second parameter for itemset page size
    int derefItems = getConfiguration().getMaxSearchDereferencedItems();

    for (UserSet userSet : results.getResults()) {
      if (LdProfiles.ITEMDESCRIPTIONS == profile) {
        fetchItems(userSet, null, null, UserSetUtils.DEFAULT_PAGE, derefItems, profile);
      }

      // items not included in results
      // set.setItems(null);
      // set.setTotal(0);
      if (!userSet.isPrivate()) {
        items.add(userSet);
      } else {
        if (isOwner(userSet, authentication) || hasAdminRights(authentication)) {
          items.add(userSet);
        } else {
          // inlcude only the id
          WebUserSetImpl id = new WebUserSetImpl();
          id.setBaseUrl(getConfiguration().getSetDataEndpoint());
          id.setIdentifier(userSet.getIdentifier());
          items.add(id);
        }
      }

      applyProfile(userSet, profile);
    }
    resPage.setItems(items);
    resPage.setTotalInPage(items.size());
  }

  private void addPagination(ResultsPage<?> resPage, String collectionUrl, int page,
      int pageSize, int lastPage, LdProfiles profile) {
    String currentPageUrl = buildPageUrl(collectionUrl, page, pageSize, profile);
    resPage.setCurrentPageUri(currentPageUrl);

    if (page > UserSetUtils.DEFAULT_PAGE) {
      String prevPage = buildPageUrl(collectionUrl, page - 1, pageSize, profile);
      resPage.setPrevPageUri(prevPage);
    }

    // if current page is not the last one
    if (!isLastPage(page, lastPage)) {
      String nextPage = buildPageUrl(collectionUrl, page + 1, pageSize, profile);
      resPage.setNextPageUri(nextPage);
    }
  }

  @Override
  public CollectionPage buildCollectionPage(UserSet userSet, LdProfiles profile, int pageNr,
      int pageSize, HttpServletRequest request) throws ParamValidationException {

    // validate params
    int totalInCollection = userSet.getTotal();
    int lastPage = validateLastPage(totalInCollection, pageSize, pageNr);


    // build partOf
    final String apiEndpointUrl = getConfiguration().getSetApiEndpoint() + userSet.getIdentifier();
    String paginationBaseUrl = buildResultsPageUrl(apiEndpointUrl, request.getQueryString(), null);
    String setId = buildSetIdUrl(userSet.getIdentifier());
    // we don't want to add profile in partOf, hence profile is passed null
    CollectionOverview partOf = buildCollectionOverview(setId, paginationBaseUrl, pageSize,
        totalInCollection, lastPage, CommonLdConstants.COLLECTION, profile);

    // build Collection Page object
    CollectionPage page = null;
    int startIndex = (pageNr - UserSetUtils.DEFAULT_PAGE) * pageSize;
    // handle ITEMDESCRIPTIONS profile separately as it will have only the requested items present
    // Also, we don't want to sublist the item list, as number items returned from search api may
    // not be equal to
    // number of items requested
    // TODO: refactor to use setter methods
    if (LdProfiles.ITEMDESCRIPTIONS == profile) {
      page = new ItemDescriptionsCollectionPage(userSet, partOf, startIndex);
      ((ItemDescriptionsCollectionPage) page).setItemList(userSet.getItems());
      page.setTotalInPage(userSet.getItems().size());
    } else { // other profiles
      final int endIndex = Math.min(startIndex + pageSize, totalInCollection);
      if (endIndex > startIndex) {
        List<String> items = userSet.getItems().subList(startIndex, endIndex);
        page = new ItemIdsCollectionPage(userSet, partOf, startIndex);
        page.setItems(items);
        page.setTotalInPage(items.size());
      } else {
        // this if for the empty user Sets
        page = new CollectionPage(userSet, partOf, startIndex);
        page.setTotalInPage(0);
      }
    }

    // add pagination URLs
    page.setCurrentPageUri(buildPageUrl(paginationBaseUrl, pageNr, pageSize, profile));

    if (pageNr > UserSetUtils.DEFAULT_PAGE) {
      page.setPrevPageUri(buildPageUrl(paginationBaseUrl, pageNr - 1, pageSize, profile));
    }

    if (pageNr < lastPage) {
      page.setNextPageUri(buildPageUrl(paginationBaseUrl, pageNr + 1, pageSize, profile));
    }

    return page;
  }

  private String buildSetIdUrl(final String identifier) {
    return getConfiguration().getSetDataEndpoint() + identifier;
  }

  public ItemIdsResultPage buildItemIdsResultsPage(String setIdentifier, List<String> itemIds,
      int page, int pageSize, HttpServletRequest request) {
    // new ResultsPageImpl<T>()
    ItemIdsResultPage result = new ItemIdsResultPage();

    // String requestURL = request.getUrl();
    String baseUrl = getConfiguration().getSetApiEndpoint();
    String relativePath =
        request.getRequestURI().replaceFirst(getConfiguration().getApiBasePath(), "");
    String resultPageId = baseUrl + relativePath;
    String collectionUrl = buildResultsPageUrl(resultPageId, request.getQueryString(), null);

    if (itemIds != null && !itemIds.isEmpty()) {
      // build isPartOf (result)
      long totalnCollection = (long) itemIds.size();
      int lastPage = getLastPage(itemIds.size(), pageSize);

      // there is no profile param for search items in user set
      final CollectionOverview collectionOverview = buildCollectionOverview(collectionUrl,
          collectionUrl, pageSize, totalnCollection, lastPage, CommonLdConstants.RESULT_LIST, null);
      result.setPartOf(collectionOverview);

      // build Result page properties
      int startPos = (page - UserSetUtils.DEFAULT_PAGE) * pageSize;
      if (startPos < itemIds.size()) {
        int toIndex = Math.min(startPos + pageSize, itemIds.size());
        List<String> pageItems = itemIds.subList(startPos, toIndex);
        result.setItems(pageItems);
        result.setTotalInPage(pageItems.size());
        // there is no profile param for searching items in user set
        addPagination(result, collectionUrl, page, pageSize, lastPage, null);
      }
    } else {
      // empty result page, but we must still return the ID
      result.setCurrentPageUri(buildPageUrl(collectionUrl, page, pageSize, null));
    }

    return result;
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
   * This method validates if the user is the owner/creator of the userset or the admin
   * 
   * @param userSet
   * @param authentication
   * @return
   * @return userSet object
   * @throws HttpException
   */
  @Override
  public UserSet verifyOwnerOrAdmin(UserSet userSet, Authentication authentication,
      boolean includeEntitySetMsg) throws HttpException {

    return verifyOwnerOrAdminOrRole(userSet, authentication, null, includeEntitySetMsg);
  }

  /**
   * This method validates if the user is the owner/creator of the userset or the admin
   * 
   * @param userSet the user set to verify access
   * @param authentication the authentication token
   * @param role optional role granting access
   * @return the userset if the access is granted
   * @throws HttpException if hte access is not granted
   */
  UserSet verifyOwnerOrAdminOrRole(UserSet userSet, Authentication authentication, String role,
      boolean includeEntitySetMsg) throws HttpException {

    if (authentication == null) {
      // access by API KEY, authentication not available
      throw new ApplicationAuthenticationException(UserSetI18nConstants.USER_NOT_AUTHORIZED,
          UserSetI18nConstants.USER_NOT_AUTHORIZED,
          new String[] {
              "Access to update operations of private User Sets require user authentication with JwtToken"},
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
      throw new ApplicationAuthenticationException(I18nConstants.OPERATION_NOT_AUTHORIZED,
          I18nConstants.OPERATION_NOT_AUTHORIZED, new String[] {message.toString()},
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
   * @throws HttpException
   */
  public void verifyPermissionToUpdate(UserSet existingUserSet, Authentication authentication,
      boolean includeEntitySetMsg) throws HttpException {
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
 

  /**
   * This methods applies Linked Data profile to a user set
   * 
   * @param userSet The given user set
   * @param profile Provided Linked Data profile
   * @return profiled user set value
   */
  public void applyProfile(UserSet userSet, LdProfiles profile) {
    // update
    userSet.setBaseUrl(getConfiguration().getSetDataEndpoint());

    // check that not more then maximal allowed number of items are
    // presented
    if (profile != LdProfiles.MINIMAL && userSet.getItems() != null) {
      int itemsCount = userSet.getItems().size();
      final int maxPageSize = getConfiguration().getMaxPageSize(profile.getRequestParamValue());
      if (itemsCount > maxPageSize) {
        List<String> itemsPage =
            userSet.getItems().subList(0, maxPageSize);
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
    // userSet.setItems(null);
  }

  @Override
  public UserSet publishUnpublishUserSet(String userSetId, Date issued, Authentication authentication,
      boolean publish) throws HttpException {
    PersistentUserSet userSet = getMongoPersistence().getByIdentifier(userSetId);
    // if the user set does not exist, return 404
    if (userSet == null) {
      throw new UserSetNotFoundException(UserSetI18nConstants.USERSET_NOT_FOUND,
          UserSetI18nConstants.USERSET_NOT_FOUND, new String[] {userSetId});
    }
    validateUserSetForPublishUnPublish(userSet, publish);
    if (publish) {
      return updateUserSetForPublish(userSet, issued, authentication);
    } else {
      return updateUserSetForUnpublish(userSet, authentication);
    }
  }

  /**
   * Validates the user set for publishing or un-publishing
   * 
   * @param userSet
   * @param authentication
   * @throws HttpException
   */
  private void validateUserSetForPublishUnPublish(PersistentUserSet userSet, boolean publish) throws HttpException {
    // Check if the “type” of the set is “EntityBestItemsSet” or “BookmarkFolder”, if so respond
    // with 400;
    if (isPublishingPrevented(userSet)) {
      throw new RequestValidationException(UserSetI18nConstants.USER_SET_OPERATION_NOT_ALLOWED,
          new String[] {"Publish/Unpublish user set ", userSet.getType()});
    }
    // verify the state of the object
    if (!publish && !userSet.isPublished()) {
      // if depublishing
      throw new RequestValidationException(UserSetI18nConstants.USER_SET_OPERATION_NOT_ALLOWED,
          new String[] {"Unpublish", "not published"});
    }
  }

  private boolean isPublishingPrevented(PersistentUserSet userSet) {
    return userSet.isBookmarksFolder() || userSet.isEntityBestItemsSet();
  }
}
