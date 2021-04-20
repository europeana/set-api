package eu.europeana.api.set.integration.web;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

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
    public void update_EntityUserSet_ok() throws Exception {
	
	WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
	String identifier = userSet.getIdentifier();

	String updateRequestJson = getJsonStringInput(ENTITY_USER_SET_UPDATE);
	String result = mockMvc.perform(put(BASE_URL + "{identifier}", identifier)
		.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
		.content(updateRequestJson).header(HttpHeaders.AUTHORIZATION, creatorEntitySetUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse().getContentAsString();
	
	assertTrue(containsKeyOrValue(result, "https://updated.reference.uri"));
	assertTrue(containsKeyOrValue(result, userSet.getId()));
	
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
