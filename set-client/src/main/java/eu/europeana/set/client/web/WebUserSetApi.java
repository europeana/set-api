package eu.europeana.set.client.web;

import eu.europeana.set.client.exception.SetApiClientException;
import eu.europeana.set.definitions.model.UserSet;

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


}