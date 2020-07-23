package eu.europeana.set.web.service;

import java.io.IOException;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.springframework.security.core.Authentication;

import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.web.exception.request.RequestBodyValidationException;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.search.BaseUserSetResultPage;

public interface UserSetService {

	/**
	 * This method stores UserSet object in database and in Solr.
	 * @param UserSet
	 * @return UserSet object
	 */
	public UserSet storeUserSet(UserSet UserSet);

    /**
     * This method converts close set to open set by updating respective items
     * 
     * @param storedUserSet
     * @param apiKey
     * @param action
     * @return updated set
     * @throws HttpException
     * @throws IOException
     * @throws JSONException
     */
    public UserSet fetchDynamicSetItems(UserSet storedUserSet, String apiKey,
    		String sort, String sortOrder, int pageNr, int pageSize)
    	    throws HttpException, IOException, JSONException;
    
    /**
     * This method converts open set to close set by updating respective items
     * @param storedUserSet
     * @param items
     * @return updated set
     */
    public UserSet updateUserSetInDb(UserSet storedUserSet, List<String> items);
    
	/**
	 * update (stored) <code>persistentUserSet</code> with values from <code>webUserSet</code>
	 * @param persistentUserSet
	 * @param webUserSet
	 * @return
	 */
	public UserSet updateUserSet(PersistentUserSet persistentUserSet, UserSet webUserSet);
	
	/**
	 * This method updates user set pagination values. 
	 * @param newUserSet
	 * @return user set with updated pagination values
	 */
	public void updateUserSetPagination(UserSet newUserSet);		
	
	/**
	 * This method returns UserSet object for given user set identifier.
	 * @param
	 * @return UserSet object
	 */
	public UserSet getUserSetById(String userSetId) throws UserSetNotFoundException; 
		
	/**
	 * This method forms an identifier URL
	 * @param id The sequential ID
	 * @param base The base URL
	 * @return identifier URL
	 */
	public String buildIdentifierUrl(String id, String base);
	
    /**
     * This method computes pagination values for user set
     * @param userSet
     * @return enriched user set
     */
    public UserSet fillPagination(UserSet userSet);
	
    /**
     * This method computes pagination values for user set without update of identifier
     * @param userSet
     * @return enriched user set
     */
    public UserSet updatePagination(UserSet userSet);
	
	/**
	 * This methods converts user set object from JsonLd string format to a UserSet object
	 * @param userSetJsonLdStr
	 * @return a UserSet object
	 * @throws HttpException
	 */
	public UserSet parseUserSetLd(String userSetJsonLdStr) throws HttpException;

	/**
	 * This method validates and processes the Set description for format and mandatory fields
     * if false responds with HTTP 400
	 * @param webUserSet
	 * @throws RequestBodyValidationException 
	 */
	public void validateWebUserSet(UserSet webUserSet) throws RequestBodyValidationException;
	
	/**
	 * This method deletes user set by user set Id value.
	 * @param userSetId The id of the user set
	 * @throws UserSetNotFoundException 
	 */
	public void deleteUserSet(String userSetId) throws UserSetNotFoundException;
	
	/**
	 * remove duplicate items in the user set by preserving the order of items
	 * @param userSet
	 */
	public void removeItemDuplicates(UserSet userSet);
	
	/**
	 * This method validates position input, if false responds with -1
	 * 
	 * @param position
	 *            The given position
	 * @param items
	 *            The item list
	 * @return position The validated position in list to insert
	 * @throws ApplicationAuthenticationException
	 */
	public int validatePosition(String position, List<String> items) throws ApplicationAuthenticationException;

	/**
	 * This method enriches user set by provided item
	 * @param datasetId The id of dataset
	 * @param localId The id in collection
	 * @param position The position in item list
	 * @param existingUserSet
	 * @return user set enriched by new item
	 * @throws ApplicationAuthenticationException
	 */
	public UserSet insertItem(String datasetId, String localId, String position, UserSet existingUserSet)
			throws ApplicationAuthenticationException;

	/**
	 * This method updates existing item list
	 * @param existingUserSet
	 * @return updated user set
	 */
	public UserSet updateItemList(UserSet existingUserSet);

	/**
	 * This method replaces item in user set
	 * @param existingUserSet
	 * @param positionInt
	 * @param newItem
	 */
	public void replaceItem(UserSet existingUserSet, int positionInt, String newItem);

	/**
	 * Add item to the list in given position if provided.
	 * @param existingUserSet
	 * @param positionInt
	 * @param newItem
	 */
	public void addNewItemToList(UserSet existingUserSet, int positionInt, String newItem);

	/**
	 * search user sets using the given query and profile 
	 * @param searchQuery
	 * @param profile
	 * @return 
	 */
	public ResultSet<? extends UserSet> search(UserSetQuery searchQuery, LdProfiles profile);

	public BaseUserSetResultPage<?> buildResultsPage(UserSetQuery searchQuery, ResultSet<? extends UserSet> results,
		    StringBuffer requestUrl, String reqParams, LdProfiles profile, Authentication authentication);

	/**
	 * This method validates input values wsKey, identifier and userToken.
	 * 
	 * @param identifier
	 * @param userId
	 * @return
	 * @return userSet object
	 * @throws HttpException
	 */
	UserSet verifyOwnerOrAdmin(UserSet userSet, Authentication authentication) throws HttpException;

	String buildCreatorUri(String userId);

	/**
	 * This method retrieves user id from authentication object
	 * 
	 * @param authentication
	 * @return the user id
	 */
	String getUserId(Authentication authentication);

}