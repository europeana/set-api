package eu.europeana.set.client.web;

import org.springframework.http.ResponseEntity;

public interface WebUserSetApi {

	/**
	 * This method creates user set describing it in body JSON string.
	 * @param requestBody Contains the body JSON string
	 * @param profile
	 * @return response entity containing body, headers and status code.
	 */
	public ResponseEntity<String> createUserSet(
			String requestBody, String profile);
	
	/**
	 * This method retrieves user set from database
	 * 
	 * @param identifier
	 * @param profile
	 * @return response entity that contains response body, headers and status code.
	 */	
	public ResponseEntity<String> getUserSet(
			String identifier, String profile);
	
	/**
	 * This method deletes user set by the given identifier
	 * @param identifier
	 * @return response entity containing headers and status code.
	 */
	public ResponseEntity<String> deleteUserSet(
			String identifier);
	
	/**
	 * This method updates user set by the given update string in JSON format
	 * @param identifier 
	 * @param requestBody
	 * @param profile
	 * @return response entity containing body, headers and status code.
	 */
	public ResponseEntity<String> updateUserSet(
			String identifier, String requestBody, String profile);

}