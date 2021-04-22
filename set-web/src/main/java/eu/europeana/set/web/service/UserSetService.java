package eu.europeana.set.web.service;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import eu.europeana.set.web.exception.authorization.UserAuthorizationException;
import org.codehaus.jettison.json.JSONException;
import org.springframework.security.core.Authentication;

import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.exception.ParamValidationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.web.exception.request.RequestBodyValidationException;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.model.search.BaseUserSetResultPage;
import eu.europeana.set.web.model.search.CollectionPage;
import eu.europeana.set.web.model.search.ItemIdsResultPage;

public interface UserSetService {

    /**
     * This method stores UserSet object in database and in Solr.
     *
     * @param userSet
     * @return UserSet object
     * @throws HttpException
     */
    public UserSet storeUserSet(UserSet userSet, Authentication authentication) throws HttpException;

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
     * @throws IOException
     * @throws JSONException
     */
    public UserSet fetchItems(UserSet storedUserSet, String sort, String sortOrder, int pageNr, int pageSize,
	    LdProfiles profile) throws HttpException, IOException, JSONException;

    /**
     * update (stored) <code>persistentUserSet</code> with values from
     * <code>webUserSet</code>
     *
     * @param persistentUserSet
     * @param webUserSet
     * @return
     */
    public UserSet updateUserSet(PersistentUserSet persistentUserSet, UserSet webUserSet);

    /**
     * This method returns UserSet object for given user set identifier.
     *
     * @param
     * @return UserSet object
     */
    public UserSet getUserSetById(String userSetId) throws UserSetNotFoundException;

    /**
     * This method returns List<UserSet> for given user set creatorId
     *
     * @param
     * @return List<PersistentUserSet>
     */
    public List<PersistentUserSet> getUserSetByCreatorId(String creatorId) throws UserSetNotFoundException;

    /**
     * This methods converts user set object from JsonLd string format to a UserSet
     * object
     * 
     * @param userSetJsonLdStr
     * @return a UserSet object
     * @throws HttpException
     */
    public UserSet parseUserSetLd(String userSetJsonLdStr) throws HttpException;

    /**
     * This method validates and processes the Set description for format and
     * mandatory fields if false responds with HTTP 400
     * 
     * @param webUserSet
     * @throws RequestBodyValidationException
     * @throws ParamValidationException
     */
    public void validateWebUserSet(UserSet webUserSet) throws RequestBodyValidationException, ParamValidationException, UserAuthorizationException;

    /**
     * This method deletes user set by user set Id value.
     * 
     * @param userSetId The id of the user set
     * @throws UserSetNotFoundException
     */
    public void deleteUserSet(String userSetId) throws UserSetNotFoundException;

    /**
     * This method deletes list of user set.
     * 
     * @param userSets  The list of user sets.
     * @param creatorId Creator of the user Sets
     */
    public void deleteUserSets(String creatorId, List<PersistentUserSet> userSets);

    /**
     * remove duplicate items in the user set by preserving the order of items
     * 
     * @param userSet
     */
    public void removeItemDuplicates(UserSet userSet);

    /**
     * This method validates position input, if false responds with -1
     * 
     * @param position The given position
     * @param items    The item list
     * @return position The validated position in list to insert
     * @throws ApplicationAuthenticationException
     */
    public int validatePosition(String position, List<String> items, int pinnedItems) throws ApplicationAuthenticationException;

    /**
     * This method enriches user set by provided item
     * 
     * @param datasetId       The id of dataset
     * @param localId         The id in collection
     * @param position        The position in item list
     * @param existingUserSet
     * @return user set enriched by new item
     * @throws ApplicationAuthenticationException
     */
    public UserSet insertItem(String datasetId, String localId, String position, UserSet existingUserSet)
	    throws ApplicationAuthenticationException;

    /**
     * This method updates existing item list
     * 
     * @param existingUserSet
     * @return updated user set
     */
    public UserSet updateItemList(UserSet existingUserSet);

    /**
     * This method replaces item in user set
     * 
     * @param existingUserSet
     * @param positionInt
     * @param newItem
     */
    public void replaceItem(UserSet existingUserSet, int positionInt, String newItem);

    /**
     * Add item to the list in given position if provided.
     * 
     * @param existingUserSet
     * @param positionInt
     * @param newItem
     */
    public void addNewItemToList(UserSet existingUserSet, int positionInt, String newItem);

    /**
     * search user sets using the given query and profile
     * 
     * @param searchQuery
     * @param profile
     * @param authentication
     * @return
     */
    public ResultSet<? extends UserSet> search(UserSetQuery searchQuery, LdProfiles profile,
	    Authentication authentication);

    public BaseUserSetResultPage<?> buildResultsPage(UserSetQuery searchQuery, ResultSet<? extends UserSet> results,
	    String requestUrl, String reqParams, LdProfiles profile, Authentication authentication)
	    throws HttpException;
    
    public ItemIdsResultPage buildItemIdsResultsPage(List<String> itemIds, int page, int pageSize,
	    HttpServletRequest request);
    
    public CollectionPage buildCollectionPage(UserSet userSet, LdProfiles profile, int pageNr, int pageSize, HttpServletRequest request) throws HttpException;

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
    void checkPermissionToUpdate(UserSet userSet,Authentication authentication, boolean includeEntitySetMsg) throws HttpException;

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
    UserSet applyProfile(UserSet userSet, LdProfiles profile);

    /**
     * Return the List of entity sets with
     * items, subject and type value
     * @return
     */
    List<PersistentUserSet> getEntitySetBestBetsItems(UserSetQuery query);
}