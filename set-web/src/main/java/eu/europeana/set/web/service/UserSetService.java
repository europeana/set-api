package eu.europeana.set.web.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.codehaus.jettison.json.JSONException;
import org.springframework.security.core.Authentication;
import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.exception.ParamValidationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.search.UserSetFacetQuery;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.web.exception.request.ItemValidationException;
import eu.europeana.set.web.exception.request.RequestBodyValidationException;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.model.search.BaseUserSetResultPage;
import eu.europeana.set.web.model.search.CollectionPage;
import eu.europeana.set.web.model.search.ItemIdsResultPage;
import eu.europeana.set.web.service.controller.exception.SetUniquenessValidationException;

public interface UserSetService {

    /**
     * This method stores UserSet object in database and in Solr.
     *
     * @param userSet
     * @return UserSet object
     * @throws HttpException
     */
    UserSet storeUserSet(UserSet userSet, Authentication authentication) throws HttpException, IOException;

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
     * @throws HttpException
     * @throws JSONException
     */
    UserSet fetchItems(UserSet storedUserSet, String sort, String sortOrder, int pageNr, int pageSize,
	    LdProfiles profile) throws HttpException, JSONException;

    /**
     * update (stored) <code>persistentUserSet</code> with values from
     * <code>webUserSet</code>
     *
     * @param persistentUserSet
     * @param webUserSet
     * @param profile
     * @return
     * @throws HttpException 
     */
    UserSet updateUserSet(PersistentUserSet persistentUserSet, UserSet webUserSet, LdProfiles profile) throws HttpException;

    /**
     * This method returns UserSet object for given user set identifier.
     *
     * @param
     * @return UserSet object
     */
    UserSet getUserSetById(String userSetId) throws UserSetNotFoundException;

    List<PersistentUserSet> getUserSetByCreatorId(String creatorId) throws UserSetNotFoundException;

    /**
     * This methods converts user set object from JsonLd string format to a UserSet
     * object
     * 
     * @param userSetJsonLdStr
     * @return a UserSet object
     * @throws HttpException
     */
    UserSet parseUserSetLd(String userSetJsonLdStr) throws HttpException;

    /**
     * This method validates and processes the Set description for format and
     * mandatory fields if false responds with HTTP 400
     * 
     * @param webUserSet
     * @param isAlreadyPublished indicates if the set is already in the published state (in the database) 
     * @throws RequestBodyValidationException
     * @throws ParamValidationException
     * @throws SetUniquenessValidationException 
     * @throws ItemValidationException 
     */
    void validateWebUserSet(UserSet webUserSet, boolean isAlreadyPublished) throws RequestBodyValidationException, ParamValidationException, SetUniquenessValidationException, ItemValidationException;

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
     * @param userSets  The list of user sets.
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
     * @param datasetId       The id of dataset
     * @param localId         The id in collection
     * @param position        The position in item list
     * @param existingUserSet
     * @return user set enriched by new item
     * @throws ApplicationAuthorizationException
     * @throws ItemValidationException 
     */
    UserSet insertItem(String datasetId, String localId, String position, UserSet existingUserSet)
	    throws ApplicationAuthenticationException, ItemValidationException;

    /**
     * This method updates existing item list
     * 
     * @param existingUserSet
     * @return updated user set
     */
    UserSet updateItemList(UserSet existingUserSet);

    /**
     * search user sets using the given query and profile
     * 
     * @param searchQuery
     * @param profile
     * @param authentication
     * @return
     */
    ResultSet<? extends UserSet> search(UserSetQuery searchQuery, UserSetFacetQuery facetQuery, List<LdProfiles> profiles,
                                               Authentication authentication);

    BaseUserSetResultPage<?> buildResultsPage(UserSetQuery searchQuery, ResultSet<? extends UserSet> results,
	    String requestUrl, String reqParams, List<LdProfiles> profiles, Authentication authentication)
            throws HttpException;
    
    ItemIdsResultPage buildItemIdsResultsPage(String setId, List<String> itemIds, int page, int pageSize,
	    HttpServletRequest request);
    
    CollectionPage buildCollectionPage(UserSet userSet, LdProfiles profile, int pageNr, int pageSize, HttpServletRequest request) throws HttpException;

    /**
     * This method validates input if the user is the owner/creator of the user set or is admin
     * 
     * @param userSet
     * @param authentication
     * @return
     * @return userSet object
     * @throws HttpException
     */
    UserSet verifyOwnerOrAdmin(UserSet userSet, Authentication authentication, boolean includeEntitySetMsg) throws HttpException;
    
 
    /**
     * This method validates admin role
     *
     * @param  authentication
     *
     * @return true if userToken has admin role
     * @throws HttpException
     */
    boolean isAdmin(Authentication authentication);

    /**
     * This method validates editor role
     *
     * @param  authentication
     *
     * @return true if userToken has editor role
     * @throws HttpException
     */
    boolean hasEditorRole(Authentication authentication);

    /**
     * This method checks the permission for create and update
     * with the authentication token
     *
     * @param  authentication
     *
     * @return true if the user has permission
     * @throws HttpException
     */
    void verifyPermissionToUpdate(UserSet userSet,Authentication authentication, boolean includeEntitySetMsg) throws HttpException;

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
    void applyProfile(UserSet userSet, LdProfiles profile);
    
    /**
     * Gets the profile for pagination urls and item page. Basically gets the profile valid for
     * collection page from the list of profiles passed during search request
     *
     * @param profiles list of candidate profiles
     * @return the profile to be applied for generating the pagination
     */
    LdProfiles getProfileForPagination(List<LdProfiles> profiles);

    /**
     * Return the List of entity sets with
     * items, subject and type value
     * @return
     */
    List<PersistentUserSet> getEntitySetBestBetsItems(UserSetQuery query);

    /**
     * Builds page Url
     * @param collectionUrl
     * @param page
     * @param pageSize
     * @param profile
     * @return
     */
    String buildPageUrl(String collectionUrl, int page, int pageSize, LdProfiles profile) ;

    /**
     * This method publishes and/or un-publishes an existing UserSet.
     * @param userSetId
     * @param authentication
     * @param publish
     * @return
     * @throws HttpException
     */
    UserSet publishUnpublishUserSet(String userSetId, Date issued, Authentication authentication, boolean publish) throws HttpException;

}