package eu.europeana.api.set.integration.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.web.model.WebUserSetImpl;
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
public class EntitySetTest extends BaseUserSetTestUtils {

    @BeforeEach
    public void initApplication() {
	//actually it will be called only once
	super.initApplication();
    }

    @BeforeAll
    public static void initTokens() {
	initRegularUserToken();
	initEntitySetTokens();
	}
    
    // create Entity user set validation tests
    @Test
    public void create_EntityUserSet_Unauthorized_InvalidUserRole() throws Exception {
	String requestJson = getJsonStringInput(ENTITY_USER_SET_REGULAR);

	mockMvc.perform(post(BASE_URL).queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.content(requestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void create_EntityUserSet_Unauthorized_EmptyToken() throws Exception {
	String requestJson = getJsonStringInput(ENTITY_USER_SET_REGULAR);

	mockMvc.perform(post(BASE_URL).queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.content(requestJson).header(HttpHeaders.AUTHORIZATION, "")
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    // create entity user set with editor token
    @Test
    public void create_EntityUserSet_Success() throws Exception {
	String requestJson = getJsonStringInput(ENTITY_USER_SET_REGULAR);

	String result = mockMvc
		.perform(post(BASE_URL).queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
			.content(requestJson).header(HttpHeaders.AUTHORIZATION, editorUserToken)
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.CREATED.value())).andReturn().getResponse().getContentAsString();

	String identifier = getSetIdentifier(result);
	assertNotNull(getSetIdentifier(result));
	String creator = getSetCreator(result);
	assertNotNull(creator);
	assertTrue(StringUtils.contains(creator, getConfiguration().getEntityUserSetUserId()));
	assertNotNull(getSetContributors(result));
	getUserSetService().deleteUserSet(identifier);

    }

    @Test
    public void create_EntityUserSet_InvalidSubject() throws Exception {
	String requestJson = getJsonStringInput(ENTITY_USER_SET_INVALID_SUBJECT);

	mockMvc.perform(post(BASE_URL).queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.content(requestJson).header(HttpHeaders.AUTHORIZATION, editorUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void create_EntityUserSet_InvalidMultipleSubjects() throws Exception {
	String requestJson = getJsonStringInput(ENTITY_USER_SET_INVALID_MULTIPLE_SUBJECTS);

	mockMvc.perform(post(BASE_URL).queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.content(requestJson).header(HttpHeaders.AUTHORIZATION, editorUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    // check if editor can update the entity set
    @Test
    public void update_EntityUserSet_withEditor() throws Exception {

	WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
	String identifier = userSet.getIdentifier();

	String updateRequestJson = getJsonStringInput(ENTITY_USER_SET_UPDATE);
	mockMvc.perform(put(BASE_URL + "{identifier}", identifier)
		.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
		.content(updateRequestJson).header(HttpHeaders.AUTHORIZATION, editorUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.FORBIDDEN.value()));

	getUserSetService().deleteUserSet(identifier);
    }

    @Test
    public void update_EntityUserSet_withRegularUser() throws Exception {

	WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
	String identifier = userSet.getIdentifier();

	String updateRequestJson = getJsonStringInput(ENTITY_USER_SET_UPDATE);
	mockMvc.perform(put(BASE_URL + "{identifier}", identifier)
		.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
		.content(updateRequestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.FORBIDDEN.value()));

	getUserSetService().deleteUserSet(identifier);
    }


    @Test
    public void update_EntityUserSet_noSubject() throws Exception {

	WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
	String identifier = userSet.getIdentifier();

	String updateRequestJson = getJsonStringInput(ENTITY_USER_SET_NO_SUBJECT);
	mockMvc.perform(put(BASE_URL + "{identifier}", identifier)
		.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
		.content(updateRequestJson).header(HttpHeaders.AUTHORIZATION, creatorEntitySetUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

	getUserSetService().deleteUserSet(identifier);
    }

	@Test
	public void update_EntityUserSet_profileStandard() throws Exception {

		WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
		String identifier = userSet.getIdentifier();

		String updateRequestJson = getJsonStringInput(ENTITY_USER_SET_UPDATE);
		mockMvc.perform(put(BASE_URL + "{identifier}", identifier)
				.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
				.content(updateRequestJson).header(HttpHeaders.AUTHORIZATION, creatorEntitySetUserToken)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().is(HttpStatus.PRECONDITION_FAILED.value()));

		getUserSetService().deleteUserSet(identifier);
	}

	@Test
	public void update_EntityUserSet_profileMinimalWithItems() throws Exception {

		WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
		String identifier = userSet.getIdentifier();

		String updateRequestJson = getJsonStringInput(ENTITY_USER_SET_REGULAR);
		mockMvc.perform(put(BASE_URL + "{identifier}", identifier)
				.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
				.content(updateRequestJson).header(HttpHeaders.AUTHORIZATION, creatorEntitySetUserToken)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

		getUserSetService().deleteUserSet(identifier);
	}

    @Test
    public void update_EntityUserSet_ok() throws Exception {

	WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
	String identifier = userSet.getIdentifier();

	String updateRequestJson = getJsonStringInput(ENTITY_USER_SET_UPDATE);
	String result = mockMvc.perform(put(BASE_URL + "{identifier}", identifier)
		.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.content(updateRequestJson).header(HttpHeaders.AUTHORIZATION, creatorEntitySetUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse().getContentAsString();

	assertTrue(containsKeyOrValue(result, "https://updated.reference.uri"));
	assertTrue(containsKeyOrValue(result, userSet.getId()));

	// check if items are not overwritten
	UserSet updaedUserSet = getUserSetService().getUserSetById(userSet.getIdentifier());
	assertEquals(userSet.getPinned(), updaedUserSet.getPinned());
	assertEquals(userSet.getItems(), updaedUserSet.getItems());

	getUserSetService().deleteUserSet(identifier);
    }


    @Test
    public void delete_EntityUserSet_withRegularUser() throws Exception {
	WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
	String identifier = userSet.getIdentifier();

	mockMvc.perform(delete(BASE_URL + "{identifier}", identifier)
		.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
		.header(HttpHeaders.AUTHORIZATION, regularUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.FORBIDDEN.value()));

	getUserSetService().deleteUserSet(identifier);
    }

    @Test
    public void delete_EntityUserSet_withEditorUser() throws Exception {
	WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
	String identifier = userSet.getIdentifier();

	mockMvc.perform(delete(BASE_URL + "{identifier}", identifier)
		.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
		.header(HttpHeaders.AUTHORIZATION, editorUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }


    @Test
	public void insertItems_EntityUserSets_withRegularUser() throws Exception {
		WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
		String identifier = userSet.getIdentifier();

		mockMvc.perform(put(BASE_URL + "{identifier}/{datasetId}/{localId}", identifier, "01", "123_test")
		.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
		.header(HttpHeaders.AUTHORIZATION, regularUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
	    .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

		getUserSetService().deleteUserSet(identifier);

	}

	@Test
	public void insertItems_EntityUserSets_withEditorUser() throws Exception {
		WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
		String identifier = userSet.getIdentifier();

		String newItem = UserSetUtils.buildItemUrl(WebUserSetFields.BASE_ITEM_URL, "01", "123_test");

		String result = mockMvc.perform(put(BASE_URL + "{identifier}/{datasetId}/{localId}", identifier, "01", "123_test")
				.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
				.header(HttpHeaders.AUTHORIZATION, editor2UserToken)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse().getContentAsString();

		assertTrue(containsKeyOrValue(result, newItem));
		assertTrue(containsKeyOrValue(result, userSet.getId()));
		getUserSetService().deleteUserSet(identifier);

	}

	@Test
	public void insertPinnedItems_EntityUserSets_withEditorUser() throws Exception {
		WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
		String identifier = userSet.getIdentifier();

		String newItem = UserSetUtils.buildItemUrl(WebUserSetFields.BASE_ITEM_URL, "01", "123_test");

		String result = mockMvc.perform(put(BASE_URL + "{identifier}/{datasetId}/{localId}", identifier, "01", "123_test")
				.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
				.queryParam(WebUserSetFields.PATH_PARAM_POSITION, WebUserSetModelFields.PINNED_POSITION)
				.header(HttpHeaders.AUTHORIZATION, editor2UserToken)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse().getContentAsString();

		assertTrue(containsKeyOrValue(result, newItem));
		assertTrue(containsKeyOrValue(result, userSet.getId()));
		assertTrue(containsKeyOrValue(result, "pinned"));

        // add more pinned items
		UserSet existingUserSet = getUserSetService().getUserSetById(userSet.getIdentifier());
		getUserSetService().insertItem("02",  "123_test", WebUserSetModelFields.PINNED_POSITION, existingUserSet);
		getUserSetService().insertItem("03",  "123_test", WebUserSetModelFields.PINNED_POSITION, existingUserSet);
		getUserSetService().insertItem("04",  "123_test", WebUserSetModelFields.PINNED_POSITION, existingUserSet);

		// check if item is present
		assertTrue(existingUserSet.getItems().contains(UserSetUtils.buildItemUrl(WebUserSetFields.BASE_ITEM_URL, "02", "123_test")));
		assertTrue(existingUserSet.getItems().contains(UserSetUtils.buildItemUrl(WebUserSetFields.BASE_ITEM_URL, "03", "123_test")));
		assertTrue(existingUserSet.getItems().contains(UserSetUtils.buildItemUrl(WebUserSetFields.BASE_ITEM_URL, "04", "123_test")));

		// check count of pinned items
		assertEquals(4, existingUserSet.getPinned());

		// add entity item at 0 position
		getUserSetService().insertItem("05",  "123_test", "0", existingUserSet);
		String entityItem = UserSetUtils.buildItemUrl(WebUserSetFields.BASE_ITEM_URL, "05", "123_test");
		// check the item
		assertTrue(existingUserSet.getItems().contains(entityItem));
		assertEquals(4, existingUserSet.getPinned());
		assertEquals(7, existingUserSet.getItems().size());
		// entity item at 4+0 position
		assertEquals(4, existingUserSet.getItems().indexOf(entityItem));

        // add entity item at 3 position
		getUserSetService().insertItem("06",  "123_test", "3", existingUserSet);
		entityItem = UserSetUtils.buildItemUrl(WebUserSetFields.BASE_ITEM_URL, "06", "123_test");
		// check the item
		assertTrue(existingUserSet.getItems().contains(entityItem));
		assertEquals(4, existingUserSet.getPinned());
		assertEquals(8, existingUserSet.getItems().size());
		// entity item at 4+3 position
		assertEquals(7, existingUserSet.getItems().indexOf(entityItem));

		getUserSetService().deleteUserSet(identifier);

	}

	@Test
	public void insertAlreadyExistingPinnedItemAsNormalItem_EntityUserSets_withEditorUser() throws Exception {
		WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
		String identifier = userSet.getIdentifier();

		// add 3 pinned items
		getUserSetService().insertItem("01",  "123_test", WebUserSetModelFields.PINNED_POSITION, userSet);
		getUserSetService().insertItem("02",  "123_test", WebUserSetModelFields.PINNED_POSITION, userSet);
		getUserSetService().insertItem("03",  "123_test", WebUserSetModelFields.PINNED_POSITION, userSet);

		// check if pinned item is present
		assertTrue(userSet.getItems().contains(UserSetUtils.buildItemUrl(WebUserSetFields.BASE_ITEM_URL, "01", "123_test")));
		assertTrue(userSet.getItems().contains(UserSetUtils.buildItemUrl(WebUserSetFields.BASE_ITEM_URL, "02", "123_test")));
		assertTrue(userSet.getItems().contains(UserSetUtils.buildItemUrl(WebUserSetFields.BASE_ITEM_URL, "03", "123_test")));
		assertEquals(userSet.getPinned(), 3);

		// item to be chnaged into normal entity item
		String newItem = UserSetUtils.buildItemUrl(WebUserSetFields.BASE_ITEM_URL, "01", "123_test");

		String result = mockMvc.perform(put(BASE_URL + "{identifier}/{datasetId}/{localId}", identifier, "01", "123_test")
				.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
				.queryParam(WebUserSetFields.PATH_PARAM_POSITION, "3")
				.header(HttpHeaders.AUTHORIZATION, editor2UserToken)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse().getContentAsString();

		assertTrue(containsKeyOrValue(result, newItem));
		assertTrue(containsKeyOrValue(result, userSet.getId()));
		UserSet existingUserSet = getUserSetService().getUserSetById(userSet.getIdentifier());

		// check if item is present and pinned value is decreased
		assertTrue(existingUserSet.getItems().contains(UserSetUtils.buildItemUrl(WebUserSetFields.BASE_ITEM_URL, "01", "123_test")));
		assertEquals(existingUserSet.getPinned(), 2 );
		// 3
		System.out.println(existingUserSet.getItems());
        // 3+3 = 6 , which is more than items.size(), so will be added at last
		assertEquals(existingUserSet.getItems().size()-1, existingUserSet.getItems().indexOf(newItem));

		getUserSetService().deleteUserSet(identifier);
	}

	@Test
	public void insertAlreadyExistingItemAsPinnedItem_EntityUserSets_withEditorUser() throws Exception {
		WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);

		String identifier = userSet.getIdentifier();

		// add 1 entity item
		getUserSetService().insertItem("02",  "456_test", null, userSet);

		// add 1 pinned item
		getUserSetService().insertItem("01",  "123_test", WebUserSetModelFields.PINNED_POSITION, userSet);

		// check if pinned item is present
		assertTrue(userSet.getItems().contains(UserSetUtils.buildItemUrl(WebUserSetFields.BASE_ITEM_URL, "02", "456_test")));
		assertTrue(userSet.getItems().contains(UserSetUtils.buildItemUrl(WebUserSetFields.BASE_ITEM_URL, "01", "123_test")));
		assertEquals(userSet.getPinned(), 1);
		assertEquals(userSet.getTotal(), 4);

		// item to be chnaged into Pinned item
		String newItem = UserSetUtils.buildItemUrl(WebUserSetFields.BASE_ITEM_URL, "02",  "456_test");

		String result = mockMvc.perform(put(BASE_URL + "{identifier}/{datasetId}/{localId}", identifier, "02",  "456_test")
				.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
				.queryParam(WebUserSetFields.PATH_PARAM_POSITION, WebUserSetFields.PINNED_POSITION)
				.header(HttpHeaders.AUTHORIZATION, editor2UserToken)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse().getContentAsString();

		assertTrue(containsKeyOrValue(result, newItem));
		assertTrue(containsKeyOrValue(result, userSet.getId()));
		UserSet existingUserSet = getUserSetService().getUserSetById(userSet.getIdentifier());

		// check if item is present and pinned value is increases
		assertTrue(existingUserSet.getItems().contains(UserSetUtils.buildItemUrl(WebUserSetFields.BASE_ITEM_URL, "02",  "456_test")));
		assertEquals(existingUserSet.getPinned(), 2 );
		assertEquals( 0, existingUserSet.getItems().indexOf(newItem));

		getUserSetService().deleteUserSet(identifier);

	}

	@Test
	public void deletePinnedItems_EntityUserSets_withEditorUser() throws Exception {
		WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
		getUserSetService().insertItem("01",  "123_test", WebUserSetModelFields.PINNED_POSITION, userSet);
		getUserSetService().insertItem("02",  "123_test", WebUserSetModelFields.PINNED_POSITION, userSet);

		assertEquals(2, userSet.getPinned());
		String identifier = userSet.getIdentifier();

		String newItem = UserSetUtils.buildItemUrl(WebUserSetFields.BASE_ITEM_URL, "01", "123_test");

		String result = mockMvc.perform(delete(BASE_URL + "{identifier}/{datasetId}/{localId}", identifier, "01", "123_test")
				.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
				.header(HttpHeaders.AUTHORIZATION, editor2UserToken)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse().getContentAsString();

		assertFalse(containsKeyOrValue(result, newItem));
		assertTrue(containsKeyOrValue(result, userSet.getId()));

		UserSet userSet1 = getUserSetService().getUserSetById(userSet.getIdentifier());
		assertEquals(1, userSet1.getPinned());

		getUserSetService().deleteUserSet(identifier);

	}

    private String getSetCreator(String result) throws JSONException {
	assertNotNull(result);
	JSONObject json = new JSONObject(result);
	String creator = json.getString(WebUserSetModelFields.CREATOR);
	assertNotNull(creator);
	return creator;
    }

    private List<String> getSetContributors(String result) throws JSONException {
	assertNotNull(result);
	JSONObject json = new JSONObject(result);
	return Collections.singletonList(json.getString(WebUserSetModelFields.CONTRIBUTOR));
    }
}
