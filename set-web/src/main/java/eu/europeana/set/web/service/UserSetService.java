package eu.europeana.set.web.service;

import java.util.Date;
import java.util.List;

import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.commons_sb3.error.EuropeanaI18nApiException;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidBodyException;
import eu.europeana.api.commons_sb3.error.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import eu.europeana.api.commons_sb3.definitions.search.ResultSet;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.search.UserSetFacetQuery;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.vocabulary.SetPageProfile;
import eu.europeana.set.definitions.model.vocabulary.SetResourceProfile;
import eu.europeana.set.definitions.model.vocabulary.UserSetProfile;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.search.exception.SearchApiClientException;
import eu.europeana.set.search.service.SearchApiResponse;
import eu.europeana.set.web.exception.request.ItemValidationException;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.model.WebResource;
import eu.europeana.set.web.model.search.BaseUserSetResultPage;
import eu.europeana.set.web.model.search.CollectionPage;

public interface UserSetService {

  /**
   * This method validates, generates the id and UserSet object in database
   *
   * @param userSet
   * @param authentication  Authentication object
   * @return UserSet object
   * @throws EuropeanaApiException
   */
  UserSet createUserSet(UserSet userSet, Authentication authentication) throws EuropeanaApiException;
  

  /**
   * This method converts close set to open set by updating respective items
   *
   * @param storedUserSet
   * @param sort
   * @param sortOrder
   * @param pageNr
   * @param pageSize
   * @param profile
   * @return updated set
   * @throws EuropeanaApiException
   */
  UserSet fetchUserSetItems(UserSet storedUserSet, String sort, String sortOrder, int pageNr,
      int pageSize, SetPageProfile profile, Authentication authentication) throws EuropeanaApiException;

  /**
   * update (stored) <code>persistentUserSet</code> with values from <code>webUserSet</code>
   * 
   * @param persistentUserSet stored UserSet
   * @param webUserSet update request web user set
   * @param authentication authentication sent by user
   * @return updated user set
   * @throws EuropeanaApiException EuropeanaApiException
   */
  UserSet updateUserSet(PersistentUserSet persistentUserSet, UserSet webUserSet, Authentication authentication) throws EuropeanaApiException;

  /**
   * This method returns UserSet object for given user set identifier.
   *
   * @param
   * @return UserSet object
   */
  UserSet getUserSetById(String userSetId) throws ResourceNotFoundException;

  List<PersistentUserSet> getUserSetByCreatorId(String creatorId) throws UserSetNotFoundException;

  /**
   * This methods converts user set object from JsonLd string format to a UserSet object
   * 
   * @param userSetJsonLdStr
   * @return a UserSet object
   * @throws EuropeanaI18nApiException
   */
  UserSet parseUserSetLd(String userSetJsonLdStr) throws InvalidBodyException;

  /**
   * This method validates and processes the Set description for format and mandatory fields if
   * false responds with HTTP 400
   * 
   * @param webUserSet web user set
   * @param isAlreadyPublished indicates if the set is already in the published state (in the
   *        database)
   * @param authentication authentication sent by user
   * @throws EuropeanaApiException
   */
  void validateWebUserSet(UserSet webUserSet, boolean isAlreadyPublished, Authentication authentication) throws EuropeanaApiException;

  /**
   * This method deletes user set by user set Id value.
   * 
   * @param userSetId The id of the user set
   * @throws UserSetNotFoundException
   */
  void deleteUserSet(String userSetId) throws UserSetNotFoundException;

  /**
   * This method deletes list of user set.
   * 
   * @param userSets The list of user sets.
   * @param creatorId Creator of the user Sets
   */
  void deleteUserSets(String creatorId, List<PersistentUserSet> userSets);

  /**
   * remove duplicate items in the user set by preserving the order of items
   * 
   * @param userSet
   */
  void removeItemDuplicates(UserSet userSet);

  /**
   * This method enriches user set by provided item
   * 
   * @param datasetId The id of dataset
   * @param localId The id in collection
   * @param position The position in item list
   * @param existingUserSet
   * @return user set enriched by new item
   * @throws ItemValidationException
   */
  UserSet insertItem(String datasetId, String localId, String position, UserSet existingUserSet) throws ItemValidationException;

  UserSet insertMultipleItems(List<String> items, String position, int itemsPosition,
      UserSet existingUserSet, Authentication authentication) throws ItemValidationException;

  UserSet deleteItem(String item, UserSet existingUserSet);

  UserSet deleteMultipleItems(List<String> items, UserSet existingUserSet, Authentication authentication) throws ItemValidationException;

  /**
   * search user sets using the given query and profile
   * 
   * @param searchQuery
   * @param profile
   * @param authentication
   * @return
   */
  ResultSet<? extends UserSet> search(UserSetQuery searchQuery, UserSetFacetQuery facetQuery,
      List<SetPageProfile> profile, Authentication authentication);

  BaseUserSetResultPage<?> buildResultsPage(UserSetQuery searchQuery,
      ResultSet<? extends UserSet> results, String requestUrl, String reqParams,
      List<SetPageProfile> profiles, Authentication authentication) throws EuropeanaApiException;

  BaseUserSetResultPage<String> buildRecodsResultsPage(String setId, List<String> itemIds, int page,
      int pageSize, SetPageProfile profile, HttpServletRequest request, Authentication authentication) throws EuropeanaApiException;

  CollectionPage buildCollectionPage(UserSet userSet, UserSetProfile profile, int pageNr,
      int pageSize, HttpServletRequest request) throws EuropeanaApiException;

  /**
   * This method validates input if the user is the owner/creator of the user set or is admin
   *
   * @param userSet user set
   * @param authentication authentication sent by user
   * @return userSet object
   * @throws EuropeanaI18nApiException
   */
  UserSet verifyOwnerOrAdmin(UserSet userSet, Authentication authentication,
      boolean includeEntitySetMsg) throws EuropeanaI18nApiException;


  /**
   * This method validates admin role
   *
   * @param authentication
   *
   * @return true if userToken has admin role
   */
  boolean isAdmin(Authentication authentication);

  /**
   * This method validates editor role
   *
   * @param authentication
   *
   * @return true if userToken has editor role
   */
  boolean hasEditorRole(Authentication authentication);

  /**
   * This method checks the permission for create and update with the authentication token
   *
   * @param authentication
   *
   * @return true if the user has permission
   * @throws EuropeanaI18nApiException
   */
  void verifyPermissionToUpdate(UserSet userSet, Authentication authentication,
      boolean includeEntitySetMsg) throws EuropeanaI18nApiException;

  /**
   * This method retrieves user id from authentication object
   * 
   * @param authentication
   * @return the user id
   */
  String getUserId(Authentication authentication);

  /**
   * This methods applies Linked Data profile to a user set
   * 
   * @param userSet The given user set
   * @param profile Provided Linked Data profile
   * @return profiled user set value
   */
  void applyProfile(UserSet userSet, SetResourceProfile profile);

  /**
   * This methods applies Linked Data profile to a user set
   * 
   * @param userSet The given user set
   * @param profile Provided Linked Data profile
   * @return profiled user set value
   */
  void applyProfile(UserSet userSet, SetPageProfile profile);

  /**
   * Gets the profile for pagination urls and item page. Basically gets the profile valid for
   * collection page from the list of profiles passed during search request
   *
   * @param profiles list of candidate profiles
   * @return the profile to be applied for generating the pagination
   */
  SetPageProfile getProfileForPagination(List<SetPageProfile> profiles);

  /**
   * Return the List of entity sets with items, subject and type value
   * 
   * @return
   */
  List<PersistentUserSet> getEntitySetBestBetsItems(UserSetQuery query);

  /**
   * Builds page Url
   * 
   * @param collectionUrl
   * @param page
   * @param pageSize
   * @param profile
   * @return
   */
  String buildPageUrl(String collectionUrl, int page, int pageSize, UserSetProfile profile);

  /**
   * This method publishes and/or un-publishes an existing UserSet.
   * 
   * @param userSetId
   * @param authentication
   * @param publish
   * @return
   * @throws EuropeanaApiException
   */
  UserSet publishUnpublishUserSet(String userSetId, Date issued, Authentication authentication,
      boolean publish) throws EuropeanaApiException;

  void validateGallerySize(UserSet webUserSet, int newItems) throws ItemValidationException;

  WebResource generateDepiction(UserSet userSet, Authentication authentication) throws SearchApiClientException;

  SearchApiResponse retrieveTotalForOpenSets(UserSet webUserSet, Authentication authentication) throws EuropeanaApiException;

}