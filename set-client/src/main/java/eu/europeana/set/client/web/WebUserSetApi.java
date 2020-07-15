<<<<<<< HEAD
package eu.europeana.set.client.web;

import org.springframework.http.ResponseEntity;

public interface WebUserSetApi {

	/**
	 * This method creates user set describing it in body JSON string and
	 * providing it with associated wskey.
	 * @param wskey
	 * @param requestBody Contains the body JSON string
	 * @return response entity containing body, headers and status code.
	 */
	public ResponseEntity<String> createUserSet(
			String wskey, String requestBody);
	
	/**
	 * This method retrieves user set from database
	 * 
	 * @param apiKey
	 * @param identifier
	 * @return response entity that contains response body, headers and status code.
	 */	
	public ResponseEntity<String> getUserSet(
			String wskey, String identifier);
	
	/**
	 * This method deletes user set by the given identifier
	 * @param wskey
	 * @param identifier
	 * @return response entity containing headers and status code.
	 */
	public ResponseEntity<String> deleteUserSet(
			String wskey, String identifier);
	
	/**
	 * This method updates user set by the given update string in JSON format
	 * @param wskey
	 * @param identifier 
	 * @param requestBody
	  * @return response entity containing body, headers and status code.
	 */
	public ResponseEntity<String> updateUserSet(
			String wskey, String identifier, String requestBody);

}
=======
package eu.europeana.set.client.web;

import org.springframework.http.ResponseEntity;

public interface WebUserSetApi {

	/**
	 * This method creates user set describing it in body JSON string and
	 * providing it with associated wskey.
	 * @param wskey
	 * @param requestBody Contains the body JSON string
	 * @return response entity containing body, headers and status code.
	 */
	public ResponseEntity<String> createUserSet(
			String wskey, String requestBody);
	
	/**
	 * This method retrieves user set from database
	 * 
	 * @param apiKey
	 * @param identifier
	 * @return response entity that contains response body, headers and status code.
	 */	
	public ResponseEntity<String> getUserSet(
			String wskey, String identifier);
	
	/**
	 * This method deletes user set by the given identifier
	 * @param wskey
	 * @param identifier
	 * @return response entity containing headers and status code.
	 */
	public ResponseEntity<String> deleteUserSet(
			String wskey, String identifier);
	
	/**
	 * This method updates user set by the given update string in JSON format
	 * @param wskey
	 * @param identifier 
	 * @param requestBody
	  * @return response entity containing body, headers and status code.
	 */
	public ResponseEntity<String> updateUserSet(
			String wskey, String identifier, String requestBody);

}
>>>>>>> refs/remotes/origin/develop
