package eu.europeana.set.client.integration.web;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


/**
 * This class aims at testing of different exceptions related to set methods.
 * This is an integration test, and it is ignored for unit testing
 * @author GrafR
 */
@Ignore
public class WebUserSetProtocolExceptionsTest extends BaseWebUserSetProtocolTest {
	
    public String CORRUPTED_JSON = 
    		START +
		    "\"title\",=\"some title\"," +	
		    END;
	
    public String CORRUPTED_UPDATE_BODY =
    		"\"newfield\":=,\"some value\"";

    public String CORRUPTED_UPDATE_JSON =
    		START +
    		CORRUPTED_UPDATE_BODY + "," + 
    		"\"title\":" + "\"some title\"," +
    		END;
    
    public String WRONG_GENERATED_IDENTIFIER = "-1";
    
    public String UNKNOWN_WSKEY = "invalid_wskey";
    
    public String INVALID_USER_TOKEN = "invalid_user_token";

    public String UNKNOWN_PROVIDER = "unknown_provider";

    public String UNKNOWN_PROVIDED_IDENTIFIER = "unknown_provided_identifier";
       
	@Test
	public void createWebUserSetWithWrongWskey() throws IOException {
		
		String requestBody = getJsonStringInput(USER_SET_CONTENT);
		
		ResponseEntity<String> response = getApiClient().createUserSet(
				UNKNOWN_WSKEY, requestBody);
		
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}
	

	@Test
	public void createWebsetUserSetWithoutBody() {
		
		ResponseEntity<String> response = getApiClient().createUserSet(
				getApiKey(), null);
		
		assertEquals( HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	

	@Test
	public void createWebUserSetWithCorruptedBody() {
		
		ResponseEntity<String> response = getApiClient().createUserSet(
				getApiKey()
				, CORRUPTED_JSON);
		
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	
	@Test
	public void getWebUserSetWithWrongIdentifier() {
		
		ResponseEntity<String> response = getApiClient().getUserSet(
				getApiKey(), WRONG_GENERATED_IDENTIFIER);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}
	
	@Test
	public void updateWebsetUserSetWithWrongIdentifierNumber() throws IOException { 
		
		String requestBody = getJsonStringInput(USER_SET_CONTENT);
		
		ResponseEntity<String> response = getApiClient().updateUserSet(
				getApiKey()
				, WRONG_GENERATED_IDENTIFIER
				, requestBody);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}
			
	@Test
	public void updateWebUserSetWithWrongIdentifier() throws IOException { 
		
		String requestBody = getJsonStringInput(USER_SET_CONTENT);
		
		ResponseEntity<String> response = getApiClient().updateUserSet(
				getApiKey()
				, WRONG_GENERATED_IDENTIFIER
				, requestBody);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}
	
//	@Test
//	public void updateWebsetUserSetWithCorruptedUpdateBody() throws JsonParseException, IOException { 
//		
//		/**
//		 * store set and retrieve its identifier 
//		 */
//		UserSet set = createTestUserSet();
//		ResponseEntity<String> response = getApiClient().updateUserSet(
//				getApiKey()
//				, set.getIdentifier()
//				, CORRUPTED_UPDATE_JSON
//				, TEST_USER_TOKEN
//				);
//		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//	}
	
		
//	@Test
//	public void updateWebUserSetWithWrongUserToken() throws JsonParseException, IOException { 
//		
//		/**
//		 * store set and retrieve its id
//		 */
//		UserSet set = createTestUserSet();
//		
//		String requestBody = getJsonStringInput(USER_SET_CONTENT);
//		
//		ResponseEntity<String> response = getApiClient().updateUserSet(
//				getApiKey()
//				, set.getIdentifier()
//				, requestBody
//				, INVALID_USER_TOKEN
//				);
//		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//	}
	
		
//	@Test()
//	public void updateWebUserSetWithUnknownWskey() throws JsonParseException, IOException { 
//		
//		/**
//		 * store set and retrieve its id
//		 */
//		UserSet set = createTestUserSet();
//		
//		String requestBody = getJsonStringInput(USER_SET_CONTENT);
//		
//		ResponseEntity<String> response = getApiClient().updateUserSet(
//				UNKNOWN_WSKEY
//				, set.getIdentifier()
//				, requestBody
//				, TEST_USER_TOKEN
//				);
//		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//	}
	
		
}
