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
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

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
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;

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

    @BeforeAll
    public static void initTokens() {
	initRegularUserToken();	
    }
    
    @BeforeEach
    public void initApplication() {
	super.initApplication();
    }

    // Create User Set Tests
    @Test
    public void create_UserSet_201Created() throws Exception {
	String requestJson = getJsonStringInput(USER_SET_REGULAR);

	String result = mockMvc
		.perform(post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
			.content(requestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
	String identifier = getSetIdentifier(getConfiguration().getUserSetBaseUrl(), result);
	getUserSetService().deleteUserSet(identifier);
    }

    @Test
    public void create_UserSet_401_bad_request_InvalidInput() throws Exception {
	mockMvc.perform(post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.content("{}").header(HttpHeaders.AUTHORIZATION, regularUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isBadRequest());
    }

    @Test
    public void create_UserSet_400_unauthorized_InvalidJWTToken() throws Exception {
	String requestJson = getJsonStringInput(USER_SET_REGULAR);

	mockMvc.perform(post(BASE_URL).queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.content(requestJson).header(HttpHeaders.AUTHORIZATION, "")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    // Get user sets Tests

    @Test
    public void getUserSet_NotAuthorised() throws Exception {
	WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

	mockMvc.perform(get(BASE_URL + "{identifier}", userSet.getIdentifier()).header(HttpHeaders.AUTHORIZATION, "")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));

	getUserSetService().deleteUserSet(userSet.getIdentifier());
    }

    @Test
    public void getUserSet_Success() throws Exception {
	WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

	// get the identifier
	MockHttpServletResponse response = mockMvc.perform(
		get(BASE_URL + "{identifier}", userSet.getIdentifier())
//		.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.header(HttpHeaders.AUTHORIZATION, regularUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).andReturn().getResponse();
		
	String result = response.getContentAsString();
	assertNotNull(result);
	assertEquals(HttpStatus.OK.value(), response.getStatus());
	assertTrue(containsKeyOrValue(result, CommonLdConstants.COLLECTION));
	assertTrue(containsKeyOrValue(result, WebUserSetFields.FIRST));
	assertTrue(containsKeyOrValue(result, WebUserSetFields.LAST));
	//the default minimal profile is used
	assertFalse(containsKeyOrValue(result, WebUserSetFields.ITEMS));
	//without page in request, it is not a collection page
	assertFalse(containsKeyOrValue(result, CollectionPage.COLLECTION_PAGE));
	assertFalse(containsKeyOrValue(result, WebUserSetFields.PART_OF));
	
	
	getUserSetService().deleteUserSet(userSet.getIdentifier());
    }

    @Test
    public void getCloseUserSet_ItemDescriptions() throws Exception {
	WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE, regularUserToken);

	// get the identifier
	MockHttpServletResponse response = mockMvc.perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
		.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.ITEMDESCRIPTIONS.name())
		.header(HttpHeaders.AUTHORIZATION, regularUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).andReturn().getResponse();


	String result = response.getContentAsString();
	assertNotNull(result);
	assertEquals(HttpStatus.OK.value(), response.getStatus());
	assertTrue(containsKeyOrValue(result, UserSetUtils.buildUserSetId(getConfiguration().getUserSetBaseUrl(), userSet.getIdentifier())));

	int idCount = StringUtils.countMatches(result, "\"id\"");
	// as pageSize is not passed in the request, only 10 items will be requested for dereference
	// so "id" = 13 (10 + creator id + userset Identifier + multilingual lang "id" in one of item edmPlaceLabelLangAware)
	assertEquals(13, idCount);
	
	JSONObject json = new JSONObject(result);
	JSONArray itemDescriptions = json.getJSONArray("items");
	// check 5 items
	for (int i = 0; i < 5 ; i++) {
		String itemIdentifier = UserSetUtils.extractItemIdentifier(getConfiguration().getItemDataEndpoint(), userSet.getItems().get(i));
		String itemDescriptionIdentifier = getSetIdentifier("", itemDescriptions.get(i).toString());
		assertEquals(itemIdentifier, itemDescriptionIdentifier);
	}
	
	getUserSetService().deleteUserSet(userSet.getIdentifier());
    }

	@Test
	public void getCloseUserSet_ItemDescriptionsWithPageValues() throws Exception {
		WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE, regularUserToken);

		// get the identifier
		MvcResult response = mockMvc.perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
				.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.ITEMDESCRIPTIONS.name())
				.queryParam(CommonApiConstants.QUERY_PARAM_PAGE, "1")
				.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, "100")
				.header(HttpHeaders.AUTHORIZATION, regularUserToken)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).andReturn();

		String result = response.getResponse().getContentAsString();
		//String request = response.getRequest();
		assertNotNull(result);
		assertEquals(HttpStatus.OK.value(), response.getResponse().getStatus());

		// check the collection url
		assertTrue(containsKeyOrValue(result, getUserSetService().buildCollectionUrl(null, response.getRequest().getRequestURL().toString(), "" )));

		int idCount = StringUtils.countMatches(result, "\"id\"");
		// as pageSize is 100,  only 10 items will be requested for dereference
		// items returned by search api = 93
		// other id : userset Identifier + partOf id + Creator id + one in edmPlaceLabelLangAware as a lang
		// so "id" = 97
		assertEquals(97, idCount);

		JSONObject json = new JSONObject(result);
		JSONArray itemDescriptions = json.getJSONArray("items");
		int start = 1 * 100 ;
		// check 5 items
		for (int i = 0; i < 5 ; i++) {
			String itemIdentifier = UserSetUtils.extractItemIdentifier(getConfiguration().getItemDataEndpoint(), userSet.getItems().get(start));
			String itemDescriptionIdentifier = getSetIdentifier("", itemDescriptions.get(i).toString());
			assertEquals(itemIdentifier, itemDescriptionIdentifier);
			start ++;
		}
		getUserSetService().deleteUserSet(userSet.getIdentifier());
	}

	@Test
	public void getCloseUserSet_ItemDescriptionsWithLastPage() throws Exception {
		WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE, regularUserToken);

		// set size = 249 items, request for more than lastPage
		mockMvc.perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
				.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.ITEMDESCRIPTIONS.name())
				.queryParam(CommonApiConstants.QUERY_PARAM_PAGE, "3")
				.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, "100")
				.header(HttpHeaders.AUTHORIZATION, regularUserToken))
				.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

		getUserSetService().deleteUserSet(userSet.getIdentifier());
	}

	@Test
	public void getOpenUserSet_ItemDescriptions() throws Exception {
		WebUserSetImpl userSet = createTestUserSet(USER_SET_OPEN, regularUserToken);

		// get the identifier
		MockHttpServletResponse response = mockMvc.perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
				.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.ITEMDESCRIPTIONS.name())
				.header(HttpHeaders.AUTHORIZATION, regularUserToken)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).andReturn().getResponse();

		//
		String result = response.getContentAsString();
		assertNotNull(result);
		assertEquals(HttpStatus.OK.value(), response.getStatus());
		assertTrue(containsKeyOrValue(result, UserSetUtils.buildUserSetId(getConfiguration().getUserSetBaseUrl(), userSet.getIdentifier())));

		getUserSetService().deleteUserSet(userSet.getIdentifier());
	}

	// this test is to verify item search for large queries using POST Search API
	@Test
	public void getOpenUserSetLargeQuery_ItemDescriptions_DefaultPageSize() throws Exception {
		WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE_QUERY_OPEN, regularUserToken);

		String result = mockMvc.perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
				.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.ITEMDESCRIPTIONS.name())
				.header(HttpHeaders.AUTHORIZATION, regularUserToken)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).
				andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse().getContentAsString();

		assertNotNull(result);
		assertTrue(containsKeyOrValue(result, UserSetUtils.buildUserSetId(getConfiguration().getUserSetBaseUrl(), userSet.getIdentifier())));
		assertEquals("69", getvalueOfkey(result, WebUserSetFields.TOTAL));
		// one of set and one for creator and items = 10 (default pageSize)
		assertEquals(2 + 10, noOfOccurance(result, WebUserSetFields.ID));

		getUserSetService().deleteUserSet(userSet.getIdentifier());
	}

	// this test is fail-safe check for the open sets, if isdefinedBy has multiple query values
	// to check if UriComponentsBuilder picks all the values from query param correctly
	// See : buildSearchApiPostBody()
	@Test
	public void getOpenUserSetMultipleQuery_ItemDescriptions() throws Exception {
		WebUserSetImpl userSet = createTestUserSet(USER_SET_MULTIPLE_QUERY_OPEN, regularUserToken);

		// get the identifier
		MockHttpServletResponse response = mockMvc.perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
				.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.ITEMDESCRIPTIONS.name())
				.header(HttpHeaders.AUTHORIZATION, regularUserToken)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).andReturn().getResponse();

		//
		String result = response.getContentAsString();
		assertNotNull(result);
		assertEquals(HttpStatus.OK.value(), response.getStatus());
		assertTrue(containsKeyOrValue(result, UserSetUtils.buildUserSetId(getConfiguration().getUserSetBaseUrl(), userSet.getIdentifier())));

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
		.header(HttpHeaders.AUTHORIZATION, regularUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void updateUserSet_Success() throws Exception {
	WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

	String updatedRequestJson = getJsonStringInput(UPDATED_USER_SET_CONTENT);
	// update the userset
	MockHttpServletResponse response = mockMvc.perform(put(BASE_URL + "{identifier}", userSet.getIdentifier())
		.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name()).content(updatedRequestJson)
		.header(HttpHeaders.AUTHORIZATION, regularUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).andReturn().getResponse();

	String result = response.getContentAsString();
	assertNotNull(result);
	assertTrue(containsKeyOrValue(result, UserSetUtils.buildUserSetId(getConfiguration().getUserSetBaseUrl(), userSet.getIdentifier())));

	assertEquals(HttpStatus.OK.value(), response.getStatus());

	getUserSetService().deleteUserSet(userSet.getIdentifier());
    }

    // Delete User associated Tests
    @Test
    public void deleteMysets_Success() throws Exception {
	// ensure that at least onea user set exists into the database
	deleteBookmarkFolder(regularUserToken);
	createTestUserSet(USER_SET_REGULAR, regularUserToken);
	createTestUserSet(USER_SET_BOOKMARK_FOLDER, regularUserToken);
	createTestUserSet(USER_SET_REGULAR, regularUserToken);

	mockMvc.perform(delete(BASE_URL).header(HttpHeaders.AUTHORIZATION, regularUserToken).header(HttpHeaders.CONTENT_TYPE,
		MediaType.APPLICATION_JSON_VALUE)).andExpect(status().is(HttpStatus.NO_CONTENT.value()));
	// TODO: use search by user to verify that all usersets were deleted
	String creator = (String) getAuthentication(regularUserToken).getPrincipal();
	UserSetQuery searchQuery = (new UserSetQueryBuilder()).buildUserSetQuery("creator:" + creator, null, null, 0,
		1, getConfiguration());
	ResultSet<? extends UserSet> results = getUserSetService().search(searchQuery, null, Collections.singletonList(LdProfiles.MINIMAL),
		getAuthentication(regularUserToken));
	assertEquals(0, results.getResultSize());
    }

    @Test
    public void deleteMySets_NotAuthorised() throws Exception {
	mockMvc.perform(delete(BASE_URL).header(HttpHeaders.AUTHORIZATION, "").header(HttpHeaders.CONTENT_TYPE,
		MediaType.APPLICATION_JSON_VALUE)).andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

	@Test
	public void deleteUserAssociatedSets_NotAdmin() throws Exception {
		mockMvc.perform(delete(BASE_URL).queryParam(WebUserSetFields.PATH_PARAM_CREATOR_ID, "creatorID")
				.header(HttpHeaders.AUTHORIZATION, regularUserToken).header(HttpHeaders.CONTENT_TYPE,
						MediaType.APPLICATION_JSON_VALUE)).andExpect(status().is(HttpStatus.FORBIDDEN.value()));
	}

	@Test
	public void deleteUserAssociatedSets_NotAuthorised() throws Exception {
		mockMvc.perform(delete(BASE_URL).queryParam(WebUserSetFields.PATH_PARAM_CREATOR_ID, "creatorID")
				.header(HttpHeaders.AUTHORIZATION, "").header(HttpHeaders.CONTENT_TYPE,
				MediaType.APPLICATION_JSON_VALUE)).andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
	}

	@Test
	public void deleteUserAssociatedSets_BadRequest() throws Exception {
		mockMvc.perform(delete(BASE_URL).queryParam(WebUserSetFields.PATH_PARAM_CREATOR_ID, "")
				.header(HttpHeaders.AUTHORIZATION, regularUserToken).header(HttpHeaders.CONTENT_TYPE,
						MediaType.APPLICATION_JSON_VALUE)).andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
	}

	// Delete User set via identifier Tests
    @Test
    public void deleteUserSet_NotAuthorised() throws Exception {
	WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

	mockMvc.perform(delete(BASE_URL + "{identifier}", userSet.getIdentifier()).header(HttpHeaders.AUTHORIZATION, "")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

//    @Test //a second token is required for this test to work propertly
    public void deleteUserSet_OperationNotAuthorised() throws Exception {
	String testFile = USER_SET_REGULAR;
	WebUserSetImpl userSet = createTestUserSet(testFile, regularUserToken);

	mockMvc.perform(delete(BASE_URL + "{identifier}", userSet.getIdentifier()).header(HttpHeaders.CONTENT_TYPE,
		MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isForbidden());
//      .header(HttpHeaders.AUTHORIZATION, token2)
//                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
	getUserSetService().deleteUserSet(userSet.getIdentifier());
    }

    @Test
    public void deleteUserSet_UserSetNotFound() throws Exception {
	mockMvc.perform(delete(BASE_URL + "{identifier}", "wrong_id").header(HttpHeaders.AUTHORIZATION, regularUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void deleteUserSet_Success() throws Exception {
	String testFile = USER_SET_REGULAR;
	WebUserSetImpl userSet = createTestUserSet(testFile, regularUserToken);

	// delete the identifier
	mockMvc.perform(
		delete(BASE_URL + "{identifier}", userSet.getIdentifier()).header(HttpHeaders.AUTHORIZATION, regularUserToken)
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    }
}
