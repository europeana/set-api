package eu.europeana.set.client.web;

import org.springframework.http.ResponseEntity;

public interface WebUserSetApi {

	/**
	 * This method creates user set describing it in body JSON string and
	 * providing it with associated wskey.
	 * @param wskey
	 * @param requestBody Contains the body JSON string
	 * @param userToken
	 * @return response entity containing body, headers and status code.
	 */
	public ResponseEntity<String> createUserSet(
			String wskey, String requestBody, String userToken);
	
	/**
	 * This method retrieves user set from database
	 * 
	 * @param apiKey
	 * @param identifier
	 * @param userToken
	 * @return response entity that contains response body, headers and status code.
	 */	
	public ResponseEntity<String> getUserSet(
			String wskey, String identifier, String userToken);
	
	/**
	 * This method deletes user set by the given identifier
	 * @param wskey
	 * @param identifier
	 * @param userToken
	 * @return response entity containing headers and status code.
	 */
	public ResponseEntity<String> deleteUserSet(
			String wskey, String identifier, String userToken);
	
	/**
	 * This method updates user set by the given update string in JSON format
	 * @param wskey
	 * @param identifier 
	 * @param requestBody
	 * @param userToken
	 * @return response entity containing body, headers and status code.
	 */
	public ResponseEntity<String> updateUserSet(
			String wskey, String identifier, String requestBody, String userToken);

}
