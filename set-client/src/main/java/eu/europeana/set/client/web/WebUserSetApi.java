package eu.europeana.set.client.web;

import eu.europeana.set.client.exception.SetApiClientException;
import eu.europeana.set.client.model.result.RecordPreview;
import eu.europeana.set.definitions.model.UserSet;

import java.util.List;

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
	 * @param identifier
	 * @param profile
	 * @return response entity that contains response body, headers and status code.
	 */	
	UserSet getUserSet(
			String identifier, String profile) throws SetApiClientException;
	
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