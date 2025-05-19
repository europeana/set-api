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
	 * @param profile
	 * @return response entity containing body, headers and status code.
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
	 */
	Optional<UserSet> getUserSet(String identifier, Optional<String> profile, Optional<ResourceCaching> caching) throws SetApiClientException;
	
	/**
	 * This method deletes user set by the given identifier
	 * @param identifier
	 * @return response entity containing headers and status code.
	 */
	String deleteUserSet(
			String identifier) throws SetApiClientException;
	
	/**
	 * This method updates user set by the given update string in JSON format
	 * @param identifier 
	 * @param requestBody
	 * @param profile
	 * @return response entity containing body, headers and status code.
	 */
	UserSet updateUserSet(String identifier, String requestBody, String profile) throws SetApiClientException;

	/**
	 * This method fetches the user set pagination request
	 * @param identifier set id
	 * @param sort sort fields
	 * @param sortOrder order of sort
	 * @param page page number
	 * @param pageSize size of the page
	 * @param profile profile requested
	 * @return
	 * @throws SetApiClientException
	 */
	List<RecordPreview> getPaginationUserSet(String identifier, String sort, String sortOrder, String page, String pageSize, String profile) throws SetApiClientException;
}