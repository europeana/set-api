package eu.europeana.api.set.integration.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.definitions.vocabulary.CommonLdConstants;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.web.model.WebUserSetImpl;
import eu.europeana.set.web.model.search.CollectionPage;
import eu.europeana.set.web.search.UserSetQueryBuilder;
import eu.europeana.set.web.service.controller.jsonld.WebUserSetRest;

/**
 * Test class for UserSet controller.
 * <p>
 * For all the methods createUserSet , getUserSet , updateUserSet,
 * deleteUserSet, deleteItemFromUserSet, insertItemIntoUserSet, isItemInUserSet
 * <p>
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
public class WebUserSetRestTest extends BaseUserSetTestUtils {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @BeforeEach
    public void initApplication() {
	if (mockMvc == null) {
	    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}
    }

    // Create User Set Tests
    @Test
    public void create_UserSet_201Created() throws Exception {
	String requestJson = getJsonStringInput(USER_SET_REGULAR);

	String result = mockMvc
		.perform(post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
			.content(requestJson).header(HttpHeaders.AUTHORIZATION, token)
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
	String identifier = getSetIdentifier(result);
	getUserSetService().deleteUserSet(identifier);
    }

    private String getSetIdentifier(String result) throws JSONException {
	assertNotNull(result);
	JSONObject json = new JSONObject(result);
	String id = json.getString("id");
	assertNotNull(id);
	String identifier = id.replace(WebUserSetFields.BASE_SET_URL, "");
	return identifier;
    }

    @Test
    public void create_UserSet_401_bad_request_InvalidInput() throws Exception {
	mockMvc.perform(post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.content("{}").header(HttpHeaders.AUTHORIZATION, token)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isBadRequest());
    }

    @Test
    public void create_UserSet_400_unauthorized_InvalidJWTToken() throws Exception {
	String requestJson = getJsonStringInput(USER_SET_REGULAR);

	mockMvc.perform(post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.content(requestJson).header(HttpHeaders.AUTHORIZATION, "")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    // Get user sets Tests

    @Test
    public void getUserSet_NotAuthorised() throws Exception {
	WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, token);

	mockMvc.perform(get(BASE_URL + "{identifier}", userSet.getIdentifier()).header(HttpHeaders.AUTHORIZATION, "")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));

	getUserSetService().deleteUserSet(userSet.getIdentifier());
    }

    @Test
    public void getUserSet_Success() throws Exception {
	WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, token);

	// get the identifier
	MockHttpServletResponse response = mockMvc.perform(get(BASE_URL + "{identifier}", userSet.getIdentifier()).header(HttpHeaders.AUTHORIZATION, token)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).andReturn().getResponse();
		
	String result = response.getContentAsString();
	assertNotNull(result);
	assertEquals(HttpStatus.OK.value(), response.getStatus());
	
	assertTrue(StringUtils.contains(result, CommonLdConstants.COLLECTION));
	//the default minimal profile is used
	assertFalse(StringUtils.contains(result, WebUserSetFields.ITEMS));
	//without page in request, it is not a collection page
	assertFalse(StringUtils.contains(result, CollectionPage.COLLECTION_PAGE));
	assertFalse(StringUtils.contains(result, WebUserSetFields.PART_OF));
	assertFalse(StringUtils.contains(result, WebUserSetFields.FIRST));
	assertFalse(StringUtils.contains(result, WebUserSetFields.LAST));
	
	
	getUserSetService().deleteUserSet(userSet.getIdentifier());
    }

    @Test
    public void getUserSet_ItemDescriptions() throws Exception {
	WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE, token);

	// get the identifier
	MockHttpServletResponse response = mockMvc.perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
		.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.ITEMDESCRIPTIONS.name())
		.header(HttpHeaders.AUTHORIZATION, token)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).andReturn().getResponse();

	//
	String result = response.getContentAsString();
	assertNotNull(result);
	assertEquals(HttpStatus.OK.value(), response.getStatus());
	assertTrue(StringUtils.contains(result, UserSetUtils.buildUserSetId(userSet.getIdentifier())));

	int idCount = StringUtils.countMatches(result, "\"id\"");
	//1 id for userset + 100 ids for dereferenced items, but not all are available in the index anymore
	assertTrue(idCount > 50);
	
	getUserSetService().deleteUserSet(userSet.getIdentifier());
    }

    @Test
    public void getUserSetPagination() throws Exception {
	WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE, token);

	// get the identifier
	MockHttpServletResponse response = mockMvc.perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
		.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE, "1")
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, "5")
		.header(HttpHeaders.AUTHORIZATION, token)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).andReturn().getResponse();

	//
	String result = response.getContentAsString();
	assertNotNull(result);
	assertEquals(HttpStatus.OK.value(), response.getStatus());
	//build collection uri?
//	String collectionUrl = buildCollectionUrl(null, request.getRequestURL().toString(), request.getQueryString());	
//	assertTrue(StringUtils.contains(result, collectionUrl));

	assertTrue(StringUtils.contains(result, WebUserSetFields.PART_OF));
	assertTrue(StringUtils.contains(result, CommonLdConstants.COLLECTION));
	assertTrue(StringUtils.contains(result, CollectionPage.COLLECTION_PAGE));
	assertTrue(StringUtils.contains(result, WebUserSetFields.START_INDEX));
	assertTrue(StringUtils.contains(result, WebUserSetFields.FIRST));
	assertTrue(StringUtils.contains(result, WebUserSetFields.LAST));
	assertTrue(StringUtils.contains(result, WebUserSetFields.PREV));
	assertTrue(StringUtils.contains(result, WebUserSetFields.NEXT));
	assertTrue(StringUtils.contains(result, WebUserSetFields.ITEMS));
	assertTrue(StringUtils.contains(result, WebUserSetFields.ITEMS));
	
	
	int idCount = StringUtils.countMatches(result, "\"id\"");
	//1 id part of and one for collection page
	assertEquals(idCount, 2);
	
	int total = StringUtils.countMatches(result, "\"total\"");
	//1 id part of and one for collection page
	assertEquals(total, 2);
	
	getUserSetService().deleteUserSet(userSet.getIdentifier());
    }
    // Update user set Tests

    @Test
    public void updateUserSet_NotAuthorised() throws Exception {
	mockMvc.perform(put(BASE_URL + "{identifier}", "test").content("updatedRequestJson")
		.header(HttpHeaders.AUTHORIZATION, "")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void updateUserSet_UserSetNotFound() throws Exception {
	mockMvc.perform(put(BASE_URL + "{identifier}", "test").content("updatedRequestJson")
		.header(HttpHeaders.AUTHORIZATION, token)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void updateUserSet_Success() throws Exception {
	WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, token);

	String updatedRequestJson = getJsonStringInput(UPDATED_USER_SET_CONTENT);
	// update the userset
	MockHttpServletResponse response = mockMvc.perform(put(BASE_URL + "{identifier}", userSet.getIdentifier())
		.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name()).content(updatedRequestJson)
		.header(HttpHeaders.AUTHORIZATION, token)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).andReturn().getResponse();

	String result = response.getContentAsString();
	assertNotNull(result);
	assertTrue(StringUtils.contains(result, UserSetUtils.buildUserSetId(userSet.getIdentifier())));

	assertEquals(HttpStatus.OK.value(), response.getStatus());

	getUserSetService().deleteUserSet(userSet.getIdentifier());
    }

    // Delete User associated Tests
    @Test
    public void deleteUserAssociatedSets_Success() throws Exception {
	// ensure that at least onea user set exists into the database
	deleteBookmarkFolder(token);
	createTestUserSet(USER_SET_REGULAR, token);
	createTestUserSet(USER_SET_BOOKMARK_FOLDER, token);
	createTestUserSet(USER_SET_REGULAR, token);

	mockMvc.perform(delete(BASE_URL).header(HttpHeaders.AUTHORIZATION, token).header(HttpHeaders.CONTENT_TYPE,
		MediaType.APPLICATION_JSON_VALUE)).andExpect(status().is(HttpStatus.NO_CONTENT.value()));
	// TODO: use search by user to verify that all usersets were deleted
	String creator = (String) getAuthentication(token).getPrincipal();
	UserSetQuery searchQuery = (new UserSetQueryBuilder()).buildUserSetQuery("creator:" + creator, null, null, 0,
		1);
	ResultSet<? extends UserSet> results = getUserSetService().search(searchQuery, LdProfiles.MINIMAL,
		getAuthentication(token));
	assertEquals(0, results.getResultSize());
    }

    @Test
    public void deleteUserAssociatedSets_NotAuthorised() throws Exception {
	mockMvc.perform(delete(BASE_URL).header(HttpHeaders.AUTHORIZATION, "").header(HttpHeaders.CONTENT_TYPE,
		MediaType.APPLICATION_JSON_VALUE)).andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    // Delete User set via identifier Tests
    @Test
    public void deleteUserSet_NotAuthorised() throws Exception {
	WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, token);

	mockMvc.perform(delete(BASE_URL + "{identifier}", userSet.getIdentifier()).header(HttpHeaders.AUTHORIZATION, "")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

//    @Test //a second token is required for this test to work propertly
    public void deleteUserSet_OperationNotAuthorised() throws Exception {
	String testFile = USER_SET_REGULAR;
	WebUserSetImpl userSet = createTestUserSet(testFile, token);

	mockMvc.perform(delete(BASE_URL + "{identifier}", userSet.getIdentifier()).header(HttpHeaders.CONTENT_TYPE,
		MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isForbidden());
//      .header(HttpHeaders.AUTHORIZATION, token2)
//                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
	getUserSetService().deleteUserSet(userSet.getIdentifier());
    }

    @Test
    public void deleteUserSet_UserSetNotFound() throws Exception {
	mockMvc.perform(delete(BASE_URL + "{identifier}", "wrong_id").header(HttpHeaders.AUTHORIZATION, token)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void deleteUserSet_Success() throws Exception {
	String testFile = USER_SET_REGULAR;
	WebUserSetImpl userSet = createTestUserSet(testFile, token);

	// delete the identifier
	mockMvc.perform(
		delete(BASE_URL + "{identifier}", userSet.getIdentifier()).header(HttpHeaders.AUTHORIZATION, token)
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    }
}
