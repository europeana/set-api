package eu.europeana.set.web.service.impl;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import eu.europeana.api.commons_sb3.auth.AuthenticationHandler;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.commons_sb3.error.EuropeanaI18nApiException;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidBodyException;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidParamException;
import eu.europeana.api.commons_sb3.error.exceptions.ResourceNotFoundException;
import  jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.api.commons_sb3.definitions.search.ResultSet;
import eu.europeana.api.commons_sb3.definitions.utils.LoggingUtils;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonLdConstants;
import eu.europeana.set.definitions.exception.UserSetAttributeInstantiationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.search.UserSetFacetQuery;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.SetPageProfile;
import eu.europeana.set.definitions.model.vocabulary.SetResourceProfile;
import eu.europeana.set.definitions.model.vocabulary.UserSetProfile;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.search.SearchApiRequest;
import eu.europeana.set.search.exception.SearchApiClientException;
import eu.europeana.set.search.service.SearchApiResponse;
import eu.europeana.set.web.config.UserSetI18nConstants;
import eu.europeana.set.web.exception.request.ItemValidationException;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.model.WebResource;
import eu.europeana.set.web.model.WebUserSetImpl;
import eu.europeana.set.web.model.search.BaseUserSetResultPage;
import eu.europeana.set.web.model.search.CollectionOverview;
import eu.europeana.set.web.model.search.CollectionPage;
import eu.europeana.set.web.model.search.FacetFieldViewImpl;
import eu.europeana.set.web.model.search.ItemDescriptionsCollectionPage;
import eu.europeana.set.web.model.search.ItemDescriptionsResultPage;
import eu.europeana.set.web.model.search.ItemIdsCollectionPage;
import eu.europeana.set.web.model.search.ItemIdsResultPage;
import eu.europeana.set.web.model.search.SearchApiUtils;
import eu.europeana.set.web.model.search.UserSetIdsResultPage;
import eu.europeana.set.web.model.search.UserSetResultPage;
import eu.europeana.set.web.service.controller.jsonld.WebUserSetRequestUtils;
import ioinformarics.oss.jackson.module.jsonld.JsonldModule;

import static eu.europeana.set.web.service.authorization.UserSetAuthorizationUtils.getAuthHandler;

public class UserSetServiceImpl extends BaseUserSetServiceImpl {

  @Override
  public UserSet getUserSetById(String userSetId) throws ResourceNotFoundException {
    UserSet userSet = getMongoPersistence().getByIdentifier(userSetId);
    if (userSet == null) {
      throw new ResourceNotFoundException(UserSet.class, Arrays.asList(userSetId));
    }
    //update total/first/last
    getUserSetUtils().updatedTotal(userSet);
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
  public UserSet parseUserSetLd(String userSetJsonLdStr) throws InvalidBodyException {

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
    } catch (UserSetAttributeInstantiationException | IOException e) {
      throw new InvalidBodyException(Collections.singletonMap(
              UserSetI18nConstants.USERSET_CANT_PARSE_BODY, Arrays.asList(e.getMessage())), e);
    }
  }

  /**
   * This method normalizes item list if they exist to remove duplicated items.
   * 
   * @param userSet
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
    getLogger().info("User sets deleted for user {}. Sets deleted are : {} ",
        LoggingUtils.sanitizeUserInput(creatorId), setsToBeDeleted);
  }

  /**
   * This method validates position input, if false responds with -1
   * 
   * @param position The given position
   * @param items The item list
   * @return position The validated position in list to insert
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

  public UserSet deleteItem(String item, UserSet existingUserSet) {
    if (existingUserSet.getItems() == null) {
      // nothing to delete, do not update the set
      return existingUserSet;
    }
    // check if it is a pinned item, decrease the counter by 1 for entity sets
    if (existingUserSet.isEntityBestItemsSet()) {
      int currentPosition = existingUserSet.getItems().indexOf(item);
      if (currentPosition < existingUserSet.getPinned()) {
        existingUserSet.setPinned(existingUserSet.getPinned() - 1);
      }
    }
    // if already exists - remove item and update modified date
    existingUserSet.getItems().remove(item);

    // update an existing user set
    // update pagination fields is done during the serialization
    return getMongoPersistence().store((PersistentUserSet) existingUserSet);
  }

  private void updateIsShownBy(UserSet userSet, String firstItemOld, Authentication authentication) {
    String firstItemNew = null;
    if (userSet.getItems() != null && !userSet.getItems().isEmpty()) {
      firstItemNew = userSet.getItems().get(0);
    }
    if (!StringUtils.equals(firstItemOld, firstItemNew)) {
      try {
        final WebResource isShownBy = generateDepiction(userSet, authentication);
        userSet.setIsShownBy(isShownBy);
      } catch (SearchApiClientException e) {
        if (getLogger().isInfoEnabled()) {
          getLogger().info("Cannot generate depiciton for set: {}. Exception: {}.",
              userSet.getIdentifier(), e.getMessage());
        }
      }
    }
  }

  public UserSet deleteMultipleItems(List<String> items, UserSet existingUserSet, Authentication authentication)
      throws ItemValidationException {
    if (existingUserSet.getItems() == null || existingUserSet.getItems().isEmpty()) {
      return existingUserSet;
    }

    // keep the first item to check if it is changed, for the re-creation of the isShownBy field
    String firstItemOld = existingUserSet.getItems().get(0);
    // convert to full URIs if needed
    List<String> fullUriItems = validateItemsStrings(items);

    boolean itemsRemoved = false;
    // check if it is a pinned item, decrease the counter by 1 for entity sets
    if (existingUserSet.isEntityBestItemsSet()) {
      itemsRemoved = removeItemsForEntityBestItemsSet(existingUserSet, fullUriItems);
    } else {
      // remove
      itemsRemoved = existingUserSet.getItems().removeAll(fullUriItems);
    }

    UserSet updatedUserSet = existingUserSet;
    if (itemsRemoved) {
      // update isShownBy
      updateIsShownBy(updatedUserSet, firstItemOld, authentication);

      // update a user set in db (including modified and total)
      updatedUserSet = getMongoPersistence().store((PersistentUserSet) existingUserSet);
    }

    return updatedUserSet;
  }

  private boolean removeItemsForEntityBestItemsSet(UserSet existingUserSet,
      List<String> fullUriItems) {
    boolean itemsRemoved = false;
    for (String item : fullUriItems) {
      int currentPosition = existingUserSet.getItems().indexOf(item);
      if (currentPosition > -1) {
        if (currentPosition < existingUserSet.getPinned()) {
          // decrease counter when removing pinned items
          existingUserSet.descreasePinned(1);
        }
        existingUserSet.getItems().remove(item);
        itemsRemoved = true;
      }
    }
    return itemsRemoved;
  }

  @Override
  public UserSet insertMultipleItems(List<String> items, String position, int itemsPosition,
      UserSet existingUserSet, Authentication authentication) throws ItemValidationException {

    // keep the first item to check if it is changed, for the re-creation of the isShownBy field
    String firstItemOld = null;
    if (existingUserSet.getItems() != null && !existingUserSet.getItems().isEmpty()) {
      firstItemOld = existingUserSet.getItems().get(0);
    }

    List<String> fullUriItems = validateItemsStrings(items);
    List<String> duplicatedItems = computeDuplicateList(existingUserSet, fullUriItems);
    boolean isPinnRequest = WebUserSetRequestUtils.isPinPosition(position);

    if (duplicatedItems != null) {
      processDuplicates(existingUserSet, fullUriItems, duplicatedItems, isPinnRequest);
    }

    addItems(existingUserSet, fullUriItems, itemsPosition, isPinnRequest);
    
    // update isShownBy
    updateIsShownBy(existingUserSet, firstItemOld, authentication);

    //pagination will be updated during serialization
    return getMongoPersistence().store((PersistentUserSet) existingUserSet);
  }

  private void processDuplicates(UserSet existingUserSet, List<String> items,
      List<String> duplicatedItems, boolean isPinnRequest) {
    if (isPinnRequest) {
      // compute number of already pinned items
      int duplicatedPinned = 0;
      if (existingUserSet.getPinned() > 0) {
        final List<String> pinnedItems =
            existingUserSet.getItems().subList(0, existingUserSet.getPinned());
        duplicatedPinned = countDupplicatedItems(duplicatedItems, pinnedItems);
      }
      // remove all duplicates from the set
      existingUserSet.getItems().removeAll(duplicatedItems);
      // and decrease the “pinned” field with the number of items that were already pinned
      existingUserSet.descreasePinned(duplicatedPinned);
    } else {
      // not pin request
      if (existingUserSet.getPinned() > 0) {
        // remove from the submitted list all items that were pinned before
        final List<String> pinedItems =
            existingUserSet.getItems().subList(0, existingUserSet.getPinned());
        items.removeAll(pinedItems);
        // not duplicated anymore, after removing from set
        duplicatedItems.removeAll(pinedItems);
      }
      // remove from the set, the ones that were not pinned
      existingUserSet.getItems().removeAll(duplicatedItems);
    }

  }

  private int countDupplicatedItems(List<String> duplicatedItems, final List<String> pinnedItems) {
    int counter = 0;
    for (String pinnedItem : pinnedItems) {
      if (duplicatedItems.contains(pinnedItem)) {
        counter++;
      }
    }
    return counter;
  }

  @SuppressWarnings("external_fbcontrib:CE_CLASS_ENVY")
  private void addItems(UserSet existingUserSet, List<String> items, int position,
      boolean isPinnRequest) {
    // init items list if needed
    if (existingUserSet.getItems() == null) {
      existingUserSet.setItems(new ArrayList<String>());
    }

    if (isPinnRequest) {
      // append pinned at the beginning
      existingUserSet.getItems().addAll(0, items);
      existingUserSet.increasePinned(items.size());
    } else {
      final boolean insertAtFixPosition = position > -1 && existingUserSet.getItems() != null
          && position < existingUserSet.getItems().size();
      if (insertAtFixPosition) {
        // append at given position
        existingUserSet.getItems().addAll(position, items);
      } else {
        // append to the end
        existingUserSet.getItems().addAll(items);
      }
    }
  }

  private List<String> computeDuplicateList(@NonNull UserSet existingUserSet,
      @NonNull List<String> newItems) {
    if (existingUserSet.getItems() == null || existingUserSet.getItems().isEmpty()) {
      return null;
    }
    // new items should not be empty at this stage.
    // create a copy of the list
    List<String> res = new ArrayList<String>(newItems);
    // compute duplicates
    res.retainAll(existingUserSet.getItems());
    if (res.isEmpty()) {
      return null;
    }
    return res;
  }



  /*
   * (non-Javadoc)
   * 
   * @see eu.europeana.set.web.service.UserSetService#insertItem(java.lang.String, java.lang.String,
   * java.lang.String, eu.europeana.set.definitions.model.UserSet)
   */
  public UserSet insertItem(String datasetId, String localId, String position,
      UserSet existingUserSet) throws ItemValidationException {
    String itemForPartialValidation = "/" + datasetId + "/" + localId;
    validateEuropeanaRecordId(itemForPartialValidation);

    String newItem =
        UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), datasetId, localId);

    // check if the position is "pin" and is a EntityBestItem set then
    // insert the item at the 0 position
    UserSet userSet;

    if (WebUserSetRequestUtils.isPinPosition(position) && existingUserSet.isEntityBestItemsSet()) {
      userSet = insertItem(existingUserSet, newItem, 0, true);
    } else {
      // validate position
      // -1 if invalid
      int positionInt =
          validatePosition(position, existingUserSet.getItems(), existingUserSet.getPinned());
      userSet = insertItem(existingUserSet, newItem, positionInt, false);
    }
    //redundant will be updated during serialization by applying the profile
    updatePagination(userSet, getConfiguration());

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
    final boolean insert =
        existingUserSet.getItems() == null || !existingUserSet.getItems().contains(newItem);
    if (insert) {
      // add item && create item list if needed
      addNewItemToList(existingUserSet, finalPosition, newItem);
      updatePinCount(existingUserSet, pinnedItem, -1);
      extUserSet = getMongoPersistence().store((PersistentUserSet) existingUserSet);
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
        extUserSet = getMongoPersistence().store((PersistentUserSet) existingUserSet);
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
      existingUserSet.setItems(List.of(newItem));
    } else if (positionInt == -1) {
      // last position
      existingUserSet.getItems().add(newItem);
    } else {
      // given position
      existingUserSet.getItems().add(positionInt, newItem);
    }
  }

  @Override
  public UserSet fetchUserSetItems(UserSet userSet, String sort, String sortOrder, int pageNr,
      int pageSize, SetPageProfile profile, Authentication authentication) throws EuropeanaApiException {
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
    String searchApiProfile = null;
    searchApiProfile = getConfiguration().getSearchApiProfileForItemDescriptions();

    String url = getSearchApiUtils().buildSearchApiUrl(userSet, getConfiguration().getSearchApiUrl(), searchApiProfile);
    SearchApiRequest searchApiRequest = getSearchApiUtils().buildSearchApiPostBody(userSet,
        getConfiguration().getItemDataEndpoint(), sort, sortOrder, pageNr, pageSize,
        searchApiProfile);

    try {
      String jsonBody = serializeSearchApiRequest(searchApiRequest);
      AuthenticationHandler searchApiAuth = getAuthHandler(authentication);
      SearchApiResponse apiResult;
      if (userSet.isOpenSet() && SetPageProfile.ITEMS == profile) {
        // item ids for open sets
        apiResult = getSearchApiClient().searchItems(url, jsonBody, searchApiAuth, false);
        setItemIds(userSet, apiResult);
      } else if (SetPageProfile.ITEMS_META == profile) {
        // item descriptions for open otr closed sets
        apiResult = getSearchApiClient().searchItems(url, jsonBody, searchApiAuth, true);
        int total = apiResult.getTotal();
        if (!userSet.isOpenSet()) {
          //for closed sets, total is the total in set
          total = userSet.getItems().size();
        }
        List<String> sortedItemDescriptions =
            sortItemDescriptions(userSet, apiResult.getItems(), pageNr, pageSize);
        setItems(userSet, sortedItemDescriptions, total);
      }
      return userSet;
    } catch (SearchApiClientException e) {
      if (SearchApiClientException.MESSAGE_INVALID_ISDEFINEDNBY.equals(e.getMessage())) {
        throw new InvalidBodyException(Collections.singletonMap(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
                Arrays.asList(WebUserSetModelFields.IS_DEFINED_BY, url)), e);
      } else {
        throw new EuropeanaApiException(e.getMessage(), e);
      }
    } catch (IOException e) {
      throw new InvalidBodyException(Collections.singletonMap(UserSetI18nConstants.SEARCH_API_REQUEST_INVALID, Collections.emptyList()), e);
    }
  }

  private List<String> sortItemDescriptions(UserSet userSet, List<String> itemDescriptions,
      int pageNr, int pageSize) {

    if (userSet.getItems() != null) {
      return reorderItemDescriptions(userSet.getItems(), itemDescriptions, pageNr, pageSize);
    }
    // if open set OR userSet.getItems == null , return the same order as retrieved
    return itemDescriptions;
  }

  private List<String> reorderItemDescriptions(List<String> itemIds, List<String> itemDescriptions,
      int pageNr, int pageSize) {

    List<String> orderedItemDescriptions = new ArrayList<String>(itemDescriptions.size());

    // calculate the index of "start" and "end" to get the right page of items
    Integer start = (pageNr - WebUserSetFields.DEFAULT_PAGE) * pageSize;
    // should not exceed the size of item list
    Integer end = Math.min((start + pageSize), itemIds.size());
    final String itemDataEndpoint = getConfiguration().getItemDataEndpoint();
    String itemUri;
    String localId;
    String recordIdJsonString;
    
    for (int i = start; i < end; i++) {
      itemUri = itemIds.get(i);
      boolean found = false;
      localId = UserSetUtils.extractItemIdentifier(itemUri, itemDataEndpoint);
      //json serialization with JSONObject escapes forward slashes (which is optional according to the specs)
      recordIdJsonString = UserSetUtils.buildRecordIdJsonString(localId, true, true);
      //search description for current item
      for (String description : itemDescriptions) {
        // match record's id in json string
        if (description.contains(recordIdJsonString)) {
          orderedItemDescriptions.add(description);
          found = true;
          break;
        }
      }
      //
      if (!found) {
        orderedItemDescriptions.add("{\"id\":\"" + localId + "\"}");
      }
    }
    return orderedItemDescriptions;
  }

  @Override
  public ResultSet<? extends UserSet> search(UserSetQuery searchQuery, UserSetFacetQuery facetQuery,
      List<SetPageProfile> profiles, Authentication authentication) {
    // add user information for visibility filtering criteria
    searchQuery.setAdmin(hasAdminRights(authentication));
    searchQuery.setUser(getUserId(authentication));
    ResultSet<PersistentUserSet> results = getMongoPersistance().find(searchQuery);
    // get facets
    if (profiles.contains(SetPageProfile.FACETS) && facetQuery != null) {
      Map<String, Long> valueCountMap = getMongoPersistence().getFacets(facetQuery);
      results.setFacetFields(
          Arrays.asList(new FacetFieldViewImpl(facetQuery.getOutputField(), valueCountMap)));
    }
    return results;
  }


  @Override
  public BaseUserSetResultPage<?> buildResultsPage(UserSetQuery searchQuery,
      ResultSet<? extends UserSet> results, String requestUrl, String reqParams,
      @NonNull SetPageProfile serializationProfile,
      @NonNull List<SetPageProfile> profiles, Authentication authentication) throws EuropeanaApiException {

    BaseUserSetResultPage<?> resPage = null;
    int resultPageSize = results.getResults().size();
    int pageSize = searchQuery.getPageSize();
    int currentPage = searchQuery.getPageNr();
    long totalInCollection = results.getResultSize();

    int lastPage = validateLastPage(totalInCollection, pageSize, currentPage);
    // get profile for pagination urls and item Page
    //SetPageProfile profile = getProfileForPagination(profiles);

    String apiEndpointUrl = getConfiguration().getSetApiEndpoint() + "search";
    // 'id' field of the page Url
    String resultsPageId =
        buildResultsPageUrl(apiEndpointUrl, reqParams, serializationProfile.getProfileParamValue());

    resPage = createResultPageWithItems(results, resultPageSize, serializationProfile, profiles, authentication);

    // we don't want to add profile in partOf, hence profile is passed null
    // pageId is the same as the baseUrl for pagination
    CollectionOverview ResultList = buildCollectionOverview(resultsPageId, resultsPageId, pageSize,
        totalInCollection, lastPage, CommonLdConstants.ResultList, null);

    resPage.setPartOf(ResultList);
    addPagination(resPage, resultsPageId, currentPage, pageSize, lastPage, serializationProfile);
    return resPage;
  }

  private BaseUserSetResultPage<?> createResultPageWithItems(ResultSet<? extends UserSet> results,
      int resultPageSize, SetPageProfile serializationProfile, List<SetPageProfile> profiles,
      Authentication authentication) throws EuropeanaI18nApiException {
    BaseUserSetResultPage<?> resPage;
    switch (serializationProfile) {
      case ITEMS_META:
        // only set descriptions, not item descriptions
        resPage = setPageItemsAsSetData(results, authentication, serializationProfile);
        break;
      case ITEMS:
        resPage = setPageItemsAsSetIds(results, resultPageSize);
        break;
      case META:
        // empty page (no items)
        resPage = new UserSetResultPage();
        break;
      case FACETS:
        // serialization profile should not be facets
      default:
        throw new InvalidParamException(Arrays.asList(CommonApiConstants.QUERY_PARAM_PROFILE,
                "items.meta, items, meta ",
                serializationProfile.getProfileParamValue()));
    }
    // add facets if requested
    if (profiles.contains(SetPageProfile.FACETS)) {
      resPage.setFacetFields(results.getFacetFields());
    }
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

  UserSetResultPage setPageItemsAsSetData(ResultSet<? extends UserSet> results,
      Authentication authentication, SetPageProfile profile) {

    UserSetResultPage resPage = new UserSetResultPage();
    List<UserSet> items = new ArrayList<>(results.getResults().size());

    for (UserSet userSet : results.getResults()) {
      // int derefItems = getConfiguration().getMaxSearchDereferencedItems();
      // if (SetPageProfile.ITEMS_META == profile) {
      // fetchUserSetItems(userSet, null, null, WebUserSetFields.DEFAULT_PAGE, derefItems, profile);
      // }

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
      //set 
      updatePagination(userSet, configuration);
      
      // Apply META Profile for Sets
      applyProfile(userSet, SetResourceProfile.META);
      //TODO: SG: should HIDE or retrieve the total for open sets?
      if(userSet.isOpenSet()) {
        userSet.setTotal(-1);
      }
    }
    resPage.setItems(items);
    resPage.setTotalInPage(items.size());
    return resPage;
  }

  @Override
  public CollectionPage buildCollectionPage(UserSet userSet, UserSetProfile profile, int pageNr,
      int pageSize, HttpServletRequest request) throws EuropeanaApiException {

    // validate params
    int totalInCollection = userSet.getTotal();
    int lastPage = validateLastPage(totalInCollection, pageSize, pageNr);


    // build partOf
    final String apiEndpointUrl = getConfiguration().getSetApiEndpoint() + userSet.getIdentifier();
    String paginationBaseUrl = buildResultsPageUrl(apiEndpointUrl, request.getQueryString(), null);
    String setId = buildSetIdUrl(userSet.getIdentifier());
    // we don't want to add profile in partOf, hence profile is passed null
    CollectionOverview partOf = buildCollectionOverview(setId, paginationBaseUrl, pageSize,
        totalInCollection, lastPage, CommonLdConstants.Collection, profile);

    // build Collection Page object
    CollectionPage page = createCollectionPageWithItems(userSet, profile, pageNr, pageSize,
        totalInCollection, partOf);
    
    if(page == null) {
      //avoid NPE, should still not happen at runtime  
      return null;
    }

    // add pagination URLs
    page.setCurrentPageUri(buildPageUrl(paginationBaseUrl, pageNr, pageSize, profile));

    if (pageNr > WebUserSetFields.DEFAULT_PAGE) {
      page.setPrevPageUri(buildPageUrl(paginationBaseUrl, pageNr - 1, pageSize, profile));
    }

    if (pageNr < lastPage) {
      page.setNextPageUri(buildPageUrl(paginationBaseUrl, pageNr + 1, pageSize, profile));
    }

    return page;
  }

  private CollectionPage createCollectionPageWithItems(UserSet userSet, UserSetProfile profile,
      int pageNr, int pageSize, int totalInCollection, CollectionOverview partOf) {
    CollectionPage page = null;
    int startIndex = (pageNr - WebUserSetFields.DEFAULT_PAGE) * pageSize;
    final int endIndex = Math.min(startIndex + pageSize, totalInCollection);

    // handle ITEMDESCRIPTIONS profile separately as it will have only the requested items present
    // Also, we don't want to sublist the item list, as number items returned from search api may
    // not be equal to
    // number of items requested
    // TODO: refactor to use setter methods
    switch ((SetPageProfile) profile) {
      case ITEMS_META:
        // dereferenced items are allready present in the set
        page = createItemDescriptionsCollectionPage(userSet, partOf, startIndex);
        break;
      case META:
        page = createEmptyCollectionPage(userSet, partOf, startIndex, endIndex);
        break;
      case ITEMS:
        if (startIndex >= endIndex) {
          // this if for the empty user Sets / empty colleciton page
          page = createEmptyCollectionPage(userSet, partOf, startIndex, endIndex);
        } else {
          page = createItemIdsCollectionPage(userSet, partOf, startIndex, endIndex);
        }
        break;
      case FACETS:
        // should not be used for this method
        break;
    }
    return page;
  }

  private CollectionPage createItemIdsCollectionPage(UserSet userSet, CollectionOverview partOf,
      int startIndex, final int endIndex) {
    CollectionPage page;
    page = new ItemIdsCollectionPage(userSet, partOf, startIndex);
    if (userSet.getItems() == null) {
      // return immediately if the set has no items
      return page;
    }
    List<String> items = userSet.getItems().subList(startIndex, endIndex);
    page.setItems(items);
    page.setTotalInPage(items.size());
    return page;
  }

  private CollectionPage createItemDescriptionsCollectionPage(UserSet userSet,
      CollectionOverview partOf, int startIndex) {
    CollectionPage page;
    page = new ItemDescriptionsCollectionPage(userSet, partOf, startIndex);
    ((ItemDescriptionsCollectionPage) page).setItemList(userSet.getItems());
    if (userSet.getItems() != null) {
      page.setTotalInPage(userSet.getItems().size());
    }
    return page;
  }

  private CollectionPage createEmptyCollectionPage(UserSet userSet, CollectionOverview partOf,
      int startIndex, final int endIndex) {
    CollectionPage page = new CollectionPage(userSet, partOf, startIndex);
    // if pageNr is too high, do not set negative totalInPage
    page.setTotalInPage(Math.max(0, endIndex - startIndex));
    return page;
  }

  public BaseUserSetResultPage<String> buildRecordsResultsPage(String setIdentifier,
      List<String> itemIds, int page, int pageSize, SetPageProfile profile,
      HttpServletRequest request, Authentication authentication) throws EuropeanaApiException {
    // new ResultsPageImpl<T>()
    BaseUserSetResultPage<String> result;

    // String requestURL = request.getUrl();
    String baseUrl = getConfiguration().getSetApiEndpoint();
    String relativePath =
        request.getRequestURI().replaceFirst(getConfiguration().getApiBasePath(), "");
    String resultPageId = baseUrl + relativePath;
    String collectionUrl = buildResultsPageUrl(resultPageId, request.getQueryString(), null);

    // if no results found, return empty page
    if (itemIds == null || itemIds.isEmpty()) {
      // empty result page, but we must still return the ID
      return createItemIdsResultPage(page, pageSize, 0, profile, collectionUrl);
    }

    // build isPartOf (result)
    long totalnCollection = (long) itemIds.size();
    int lastPage = getLastPage(itemIds.size(), pageSize);

    // build Result page properties
    int startPos = (page - WebUserSetFields.DEFAULT_PAGE) * pageSize;
    if (startPos >= itemIds.size()) {
      // requested page is out of range, return empty result
      return createItemIdsResultPage(page, pageSize, 0, profile, collectionUrl);
    }

    // compute Items list for page
    int toIndex = Math.min(startPos + pageSize, itemIds.size());
    List<String> pageItems = itemIds.subList(startPos, toIndex);

    switch (profile) {
      case META:
        // no items, just total
        result =
            createItemIdsResultPage(lastPage, pageSize, totalnCollection, profile, collectionUrl);
        break;
      case ITEMS:
        result =
            createItemIdsResultPage(lastPage, pageSize, totalnCollection, profile, collectionUrl);
        result.setItems(pageItems);
        result.setTotalInPage(pageItems.size());
        break;
      case ITEMS_META:
        result = createItemDescriptionsResultPage(lastPage, pageSize, totalnCollection, profile,
            collectionUrl);
        List<String> dereferencedItems = dereferenceItems(pageItems, profile, authentication);
        ((ItemDescriptionsResultPage) result).setItemList(dereferencedItems);
        result.setTotalInPage(dereferencedItems.size());
        break;
      case FACETS:
        // serialization profile should not be facets
      default:
        throw new InvalidParamException(Arrays.asList(CommonApiConstants.QUERY_PARAM_PROFILE,
                "items.meta, items, meta ",
                profile.getProfileParamValue()));

    }

    // there is no profile param for search items in user set
    final CollectionOverview collectionOverview =
        buildCollectionOverview(collectionUrl, collectionUrl, pageSize, totalnCollection, lastPage,
            CommonLdConstants.ResultList, profile);
    result.setPartOf(collectionOverview);

    // there is no profile param for searching items in user set
    addPagination(result, collectionUrl, page, pageSize, lastPage, profile);

    return result;
  }

  private List<String> dereferenceItems(@NonNull List<String> pageItems, SetPageProfile profile,
                                        Authentication authentication) throws EuropeanaApiException {
    UserSet itemsSet = new WebUserSetImpl();
    itemsSet.setItems(pageItems);
    UserSet dereferenced = fetchUserSetItems(itemsSet, null, null, WebUserSetFields.DEFAULT_PAGE,
        pageItems.size(), profile, authentication);
    return dereferenced.getItems();
  }

  private ItemIdsResultPage createItemIdsResultPage(int page, int pageSize, long totalnCollection,
      SetPageProfile profile, String collectionUrl) {
    ItemIdsResultPage result = new ItemIdsResultPage();
    result.setCurrentPageUri(buildPageUrl(collectionUrl, page, pageSize, profile));
    result.setTotalInCollection(totalnCollection);
    return result;
  }

  private ItemDescriptionsResultPage createItemDescriptionsResultPage(int page, int pageSize,
      long totalnCollection, SetPageProfile profile, String collectionUrl) {
    ItemDescriptionsResultPage result = new ItemDescriptionsResultPage();
    result.setCurrentPageUri(buildPageUrl(collectionUrl, page, pageSize, profile));
    result.setTotalInCollection(totalnCollection);
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

  @Override
  /**
   * This methods applies Linked Data profile to a user set, preparing the set for serialization
   * It adds the pagination information and processes the items according to the profile
   * 
   * @param userSet The given user set
   * @param profile Provided Linked Data profile
   * @return profiled user set value
   */
  public void applyProfile(UserSet userSet, SetResourceProfile profile) {
    //ensure pagination set 
    if(hasNoPagination(userSet)){
      //update pagination before reseting items
      //sets also the base URL
      updatePagination(userSet, getConfiguration());
    }
    
    // set unnecessary fields to null - the empty fields will not be
    // presented
    switch (profile) {
      // currently only one profile for SetResource
      // update when needed
      case META:
        userSet.setItems(null);
        break;
      default:
        userSet.setItems(null);
    }
  }
  
  /**
   * This methods applies Linked Data profile to a user set
   * 
   * @param userSet The given user set
   * @param profile Provided Linked Data profile
   * @return profiled user set value
   */
  @Override
  public void applyProfile(UserSet userSet, SetPageProfile profile) {
    //ensure pagination set
    if(hasNoPagination(userSet)){
      //update pagination before reseting items
      //sets also the base URL
      updatePagination(userSet, getConfiguration());
    }

    // check that not more then maximal allowed number of items are
    // presented
    if (SetPageProfile.META != profile && userSet.getItems() != null) {
      int itemsCount = userSet.getItems().size();
      final int maxPageSize = getConfiguration().getMaxPageSize(profile.getProfileParamValue());
      if (itemsCount > maxPageSize) {
        List<String> itemsPage = userSet.getItems().subList(0, maxPageSize);
        userSet.setItems(itemsPage);
        // profile = LdProfiles.STANDARD;
        // getLogger().debug("Profile switched to standard, due to set size!");
      }
    }

    // set unnecessary fields to null - the empty fields will not be
    // presented
    switch (profile) {
      case ITEMS_META:
        // set serializedItems
        ((WebUserSetImpl) userSet).setSerializedItems(userSet.getItems());
        break;
      case ITEMS:
        // not for stadard or item descriptions profile
        setSerializedItemIds(userSet);
        break;
      case META:
        userSet.setItems(null);
        break;
      default:
        userSet.setItems(null);
        break;
    }
  }

  private boolean hasNoPagination(UserSet userSet) {
    return StringUtils.isEmpty(userSet.getFirst());
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
  public UserSet publishUnpublishUserSet(String userSetId, Date issued,
      Authentication authentication, boolean publish) throws EuropeanaApiException {
    PersistentUserSet userSet = getMongoPersistence().getByIdentifier(userSetId);
    // if the user set does not exist, return 404
    if (userSet == null) {
      throw new ResourceNotFoundException(UserSet.class, Arrays.asList(userSetId));
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
   */
  private void validateUserSetForPublishUnPublish(PersistentUserSet userSet, boolean publish)
      throws InvalidBodyException {
    // Check if the “type” of the set is “EntityBestItemsSet” or “BookmarkFolder”, if so respond
    // with 400;
    if (isPublishingPrevented(userSet)) {
      throw new InvalidBodyException(Collections.singletonMap(UserSetI18nConstants.USER_SET_OPERATION_NOT_ALLOWED,
          Arrays.asList("Publish/Unpublish user set ", userSet.getType())));
    }
    // verify the state of the object
    if (!publish && !userSet.isPublished()) {
      // if depublishing
      throw new InvalidBodyException(Collections.singletonMap(UserSetI18nConstants.USER_SET_OPERATION_NOT_ALLOWED,
              Arrays.asList("Unpublish", "not published")));
    }
  }

  private boolean isPublishingPrevented(PersistentUserSet userSet) {
    return userSet.isBookmarksFolder() || userSet.isEntityBestItemsSet();
  }

  @Override
  public WebResource generateDepiction(UserSet userSet, Authentication authentication) throws SearchApiClientException {
    if (userSet.getItems() == null || userSet.getItems().isEmpty()) {
      return null;
    }

    String itemId = userSet.getItems().get(0);
    WebResource depiction = generateDepictionByItemId(itemId, authentication);
    // if no thumbnail search further 
    if(!depiction.hasThumbnail() && userSet.getItems().size() > 1) {   
      //search in first 10 items
      final int shortListSize = 10;
      depiction = generateDepictionByItemList(userSet, shortListSize, authentication);
      
      if(!depiction.hasThumbnail() && userSet.getItems().size() > shortListSize) {
        //search in first 100 items
        final int longListSize = 100;
        depiction = generateDepictionByItemList(userSet, longListSize, authentication);
      }    
    }
    
    if(depiction.hasThumbnail()) {
      return depiction;
    }
    else {
      return null;
    }

  }

  private WebResource generateDepictionByItemId(String itemId, Authentication authentication) throws SearchApiClientException {
    String url =
        SearchApiUtils.getInstance().buildSearchApiUrlForItem(getConfiguration().getSearchApiUrl(),
            getConfiguration().getItemDataEndpoint(), itemId,
            getConfiguration().getSearchApiProfileForItemDescriptions());

    WebResource depiction = new WebResource();
    getSearchApiClient().fillDepiction(url, itemId, depiction, getAuthHandler(authentication));
    return depiction;
  }

  private WebResource generateDepictionByItemList(UserSet userSet, int pageSize, Authentication authentication)
      throws SearchApiClientException {

    WebResource depiction = new WebResource();
    String searchApiProfile = getConfiguration().getSearchApiProfileForItemDescriptions();

    String url = getSearchApiUtils().buildSearchApiUrl(userSet,
        getConfiguration().getSearchApiUrl(), searchApiProfile);

    //first "pageSize" items
    final List<String> itemsToSearch =
        userSet.getItems().subList(0, Math.min(userSet.getItems().size(), pageSize));
    
    SearchApiRequest searchApiRequest = getSearchApiUtils()
        .buildSearchApiPostBodyForItemIds(itemsToSearch, getConfiguration().getItemDataEndpoint(), 1, pageSize, searchApiProfile);
    
    try {
      String jsonBody = serializeSearchApiRequest(searchApiRequest);
      getSearchApiClient().fillDepiction( url, jsonBody, itemsToSearch, getConfiguration().getItemDataEndpoint(), depiction,
              getAuthHandler(authentication));
    } catch (SearchApiClientException | IOException e) {
      if(logger.isInfoEnabled()) {
        logger.info("Cannot retrieve depiction using the first {} items of set with id: {} ", pageSize, userSet.getIdentifier(), e);
      }
    } 
    return depiction;
  }

  @Override
  public UserSet createUserSet(UserSet userSet, Authentication authentication) throws EuropeanaApiException {
    setDefaults(userSet, authentication);
    if (userSet.isEntityBestItemsSet()) {
      verifyPermissionToUpdate(userSet, authentication, true);
    }

    // new sets are not yet published
    validateWebUserSet(userSet, false, authentication);

    // store in mongo database
    UserSet updatedUserSet = getMongoPersistence().create(userSet);
    updatePagination(updatedUserSet, getConfiguration());
    return updatedUserSet;
  }

}