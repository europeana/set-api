package eu.europeana.set.client.web;

import eu.europeana.api.commons_sb3.definitions.caching.ResourceCaching;
import eu.europeana.set.client.exception.SetApiClientException;
import eu.europeana.set.client.model.result.RecordPreview;
import eu.europeana.set.definitions.model.UserSet;

import java.util.List;
import java.util.Optional;

/**
 * Client API interface
 * @author GordeaS
 */
public interface WebUserSetApi {

	/**
	 * This method creates user set describing it in body JSON string.
	 * @param requestBody Contains the body JSON string
	 * @param profile serialization profile
	 * @return response entity containing body, headers and status code.
	 * @throws SetApiClientException if invocation fails
	 */
	UserSet createUserSet(String requestBody, String profile) throws SetApiClientException;
	
	/**
	 * This method retrieves user set from database
	 * 
	 * @param identifier id of the user set to be fetched
	 * @param profile profile value
	 * @param caching Resource caching object for the caching responses.
	 *                If null/empty will return the normal response with any caching headers being set
	 * @return response entity that contains response body, headers and status code.
	 * @throws SetApiClientException if invocation fails
     */
	Optional<UserSet> getUserSet(String identifier, String profile, ResourceCaching caching) throws SetApiClientException;
	
	/**
	 * This method deletes user set by the given identifier
	 * @param identifier set identifier
	 * @return response entity containing headers and status code.
	 * @throws SetApiClientException if invocation fails
     */
	String deleteUserSet(
			String identifier) throws SetApiClientException;
	
	/**
	 * This method updates user set by the given update string in JSON format
	 * @param identifier  set identifier
	 * @param requestBody body
	 * @param profile serialization profile
	 * @return response entity containing body, headers and status code.
	 * throws SetApiClientException if invocation fails
     */
	UserSet updateUserSet(String identifier, String requestBody, String profile) throws SetApiClientException;

	
	/**
     * This method updates the user set with the items provided in the request body
     * @param identifier of the user set
     * @param items  items to append
     * @param position optional, the position to start with (>= 0) when inserting items, otherwise appended to the end
     * @param profile requested profile for results
     * @return response entity containing body, headers and status code.
     * @throws SetApiClientException if invocation fails
     */
    UserSet addItems(String identifier, List<String> items, String position, String profile) throws SetApiClientException;

    /**
     * This method updates the user set by removing the items provided in the request body
     * @param identifier of the user set
     * @param items  items to remove
     * @param profile requested profile for results
     * @return response entity containing body, headers and status code.
     * @throws SetApiClientException if invocation fails
     */
    UserSet removeItems(String identifier, List<String> items, String profile) throws SetApiClientException;

    
    /**
     * This method verifies id the provided items are available in the given set
     * @param identifier of the user set
     * @param itemId id of the item in /datasetId/localId format
     * @param profile requested profile for results
     * @return response entity containing body, headers and status code.
     * @throws SetApiClientException if invocation fails
     */
    boolean isItemInSet(String identifier, String itemId, String profile)
        throws SetApiClientException;
      
    /**
	 * This method fetches the user set pagination request
	 * @param identifier set id
	 * @param sort sort fields
	 * @param sortOrder order of sort
	 * @param page page number
	 * @param pageSize size of the page
	 * @param profile profile requested
	 * @return list of record previews
	 * @throws SetApiClientException if invocation fails
     */
	List<RecordPreview> getPaginationUserSet(String identifier, String sort, String sortOrder, String page, String pageSize, String profile) throws SetApiClientException;
	
}