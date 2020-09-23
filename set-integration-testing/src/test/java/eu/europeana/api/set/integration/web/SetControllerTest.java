package eu.europeana.api.set.integration.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.web.service.controller.jsonld.WebUserSetRest;

/**
 * Test class for UserSet controller.
 * 
 * For all the methods createUserSet , getUserSet , updateUserSet,
 * deleteUserSet, deleteItemFromUserSet, insertItemIntoUserSet, isItemInUserSet
 * 
 * MockMvc test for the Main entry point for server-side Spring MVC. Should
 * check for 200 Ok, 400 bad request (if required paremter are not passed), 401
 * unauthorized (if authentication provided is wrong), and 404 Not found
 * scenarios. Should also check all the headers added using the
 * UserSetHttpHeaders constants
 *
 * @author Roman Graf on 10-09-2020.
 */
@WebMvcTest(WebUserSetRest.class)
@ContextConfiguration(locations = { "classpath:set-web-mvc.xml" })
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class SetControllerTest extends BaseUserSetTest {

    private MockMvc mockMvc;
    
    @Autowired
    private WebApplicationContext wac;

    private static String token;
    
    @BeforeAll
    public static void initToken() {
	initUserSetApi();
	token = getToken();
    }
    
    @BeforeEach
    public void initObjects() {
	this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    String BASE_URL = "/set/";

    public static final String USER_SET_CONTENT = "/content/userset.json";

    /**
     * Test createUserSet 201 Ok response
     * 
     * http://localhost:8080/set/?profile=minimal
     * 
     * Authorization Bearer xyz
     * Content-Type application/json
     * 
     */
    @Test
    public void testCreate_UserSet_201Created() throws Exception {
	String requestJson = getJsonStringInput(USER_SET_CONTENT);

	mockMvc.perform(post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.content(requestJson).header(HttpHeaders.AUTHORIZATION, token)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
//		.andDo(print())
		.andExpect(status().is(HttpStatus.CREATED.value()));
    }

    /**
     * This method extracts JSON content from a file
     * @param resource
     * @return JSON string
     * @throws IOException
     */
    protected String getJsonStringInput(String resource) throws IOException {
	InputStream resourceAsStream = getClass().getResourceAsStream(resource);

	StringBuilder out = new StringBuilder();
	BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream));
	for (String line = br.readLine(); line != null; line = br.readLine())
	    out.append(line);
	br.close();
	return out.toString();

    }

    @Test
    public void create_UserSet_401_bad_request_InvalidInput() throws Exception {
	mockMvc.perform(post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.content("").header(HttpHeaders.AUTHORIZATION, token)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
//		.andDo(print())
		.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void create_UserSet_400_unauthorized_InvalidJWTToken() throws Exception {
	String requestJson = getJsonStringInput(USER_SET_CONTENT);

	mockMvc.perform(post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.content(requestJson).header(HttpHeaders.AUTHORIZATION, "")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
//		.andDo(print())
		.andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));	
    }

}
