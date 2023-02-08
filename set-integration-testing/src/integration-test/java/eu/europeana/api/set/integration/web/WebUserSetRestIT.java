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
import java.util.Collections;
import javax.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.definitions.vocabulary.CommonLdConstants;
import eu.europeana.api.set.integration.BaseUserSetTestUtils;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.mongo.service.PersistentApiWriteLockService;
import eu.europeana.set.web.exception.request.ItemValidationException;
import eu.europeana.set.web.model.WebUserSetImpl;
import eu.europeana.set.web.search.UserSetQueryBuilder;

/**
 * Test class for UserSet controller.
 * <p>
 * For all the methods createUserSet , getUserSet , updateUserSet, deleteUserSet,
 * deleteItemFromUserSet, insertItemIntoUserSet, isItemInUserSet
 * <p>
 * MockMvc test for the Main entry point for server-side Spring MVC. Should check for 200 Ok, 400
 * bad request (if required paremter are not passed), 401 unauthorized (if authentication provided
 * is wrong), and 404 Not found scenarios. Should also check all the headers added using the
 * UserSetHttpHeaders constants
 *
 * @author Roman Graf on 10-09-2020.
 */
@SpringBootTest
public class WebUserSetRestIT extends BaseUserSetTestUtils {

  @Resource(name = "set_db_apilockService")
  protected PersistentApiWriteLockService writeLockService; 

  @BeforeAll
  public static void initTokens() {
    if(DISABLE_AUTH) {
      return;
    }
    initRegularUserToken();
    initPublisherUserToken();
    initAdminUserToken();
  }


  @AfterEach
  protected void deleteCreatedSets() {
    super.deleteCreatedSets();
  }

  // Create User Set Tests
  @Test
  public void create_UserSet_201Created() throws Exception {
    String requestJson = getJsonStringInput(USER_SET_REGULAR);

    String result = mockMvc
        .perform(
            post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .content(requestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    String identifier = getSetIdentifier(getConfiguration().getSetDataEndpoint(), result);
    assertNotNull(identifier);
    addToCreatedSets(identifier);
  }

  @Test
  public void create_UserSet_401_bad_request_InvalidInput() throws Exception {
    mockMvc
        .perform(
            post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .content("{}").header(HttpHeaders.AUTHORIZATION, regularUserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void create_published_UserSet_400_bad_request_InvalidInput() throws Exception {
    String requestJson = getJsonStringInput(USER_SET_REGULAR_PUBLISHED);
    mockMvc
        .perform(
            post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .content(requestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void create_UserSet_InvalidItems() throws Exception {
    String requestJson = getJsonStringInput(USER_SET_INVALID_ITEMS);
    mockMvc
        .perform(
            post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .content(requestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest())
        .andExpect(result -> assertEquals(((ItemValidationException)result.getResolvedException()).getI18nParams().length, 2));
  }  
  
  @Test
  public void create_UserSet_400_unauthorized_InvalidJWTToken() throws Exception {
    String requestJson = getJsonStringInput(USER_SET_REGULAR);

    mockMvc
        .perform(post(BASE_URL)
            .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
            .content(requestJson).header(HttpHeaders.AUTHORIZATION, "")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
  }

  // Get user sets Tests

  @Test
  public void getUserSet_NotAuthorised() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

    mockMvc
        .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
            .header(HttpHeaders.AUTHORIZATION, "")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
  }

  @Test
  public void getUserSet_Success() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

    // get the identifier
    MockHttpServletResponse response = mockMvc
        .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
            // .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
            .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andReturn().getResponse();

    String result = response.getContentAsString();
    assertNotNull(result);
    assertEquals(HttpStatus.OK.value(), response.getStatus());
    assertTrue(containsKeyOrValue(result, CommonLdConstants.COLLECTION));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.FIRST));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.LAST));
    // the default minimal profile is used
    assertFalse(containsKeyOrValue(result, WebUserSetFields.ITEMS));
    // without page in request, it is not a collection page
    assertFalse(containsKeyOrValue(result, CommonLdConstants.COLLECTION_PAGE));
    assertFalse(containsKeyOrValue(result, WebUserSetFields.PART_OF));
  }

  // Update user set Tests

  @Test
  public void updateUserSet_NotAuthorised() throws Exception {
    mockMvc
        .perform(put(BASE_URL + "{identifier}", "test").content("updatedRequestJson")
            .header(HttpHeaders.AUTHORIZATION, "")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
  }

  @Test
  public void updateUserSet_UserSetNotFound() throws Exception {
    mockMvc
        .perform(put(BASE_URL + "{identifier}", "test").content("updatedRequestJson")
            .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
  }

  @Test
  public void updateUserSet_PublishedBadRequest() throws Exception {

    WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

    String updatedRequestJson = getJsonStringInput(USER_SET_REGULAR_PUBLISHED);
    mockMvc
        .perform(put(BASE_URL + "{identifier}", userSet.getIdentifier())
            .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .content(updatedRequestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }
  
  @Test
  public void updateUserSet_InvalidItems() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

    String updatedRequestJson = getJsonStringInput(USER_SET_INVALID_ITEMS);
    // update the userset
    mockMvc
        .perform(put(BASE_URL + "{identifier}", userSet.getIdentifier())
            .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .content(updatedRequestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest())
        .andExpect(result -> assertEquals(((ItemValidationException)result.getResolvedException()).getI18nParams().length, 2));
  }

  @Test
  public void updateUserSet_Success() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

    String updatedRequestJson = getJsonStringInput(UPDATED_USER_SET_CONTENT);
    // update the userset
    MockHttpServletResponse response = mockMvc
        .perform(put(BASE_URL + "{identifier}", userSet.getIdentifier())
            .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .content(updatedRequestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andReturn().getResponse();

    String result = response.getContentAsString();
    assertNotNull(result);
    assertTrue(containsKeyOrValue(result, UserSetUtils
        .buildUserSetId(getConfiguration().getSetDataEndpoint(), userSet.getIdentifier())));

    assertEquals(HttpStatus.OK.value(), response.getStatus());
  }

  // Delete User associated Tests
  @Test
  public void deleteMysets_Success() throws Exception {
    // ensure that at least onea user set exists into the database
    deleteBookmarkFolder(regularUserToken);
    createTestUserSet(USER_SET_REGULAR, regularUserToken);
    createTestUserSet(USER_SET_BOOKMARK_FOLDER, regularUserToken);
    createTestUserSet(USER_SET_REGULAR, regularUserToken);

    mockMvc
        .perform(delete(BASE_URL).header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    // TODO: use search by user to verify that all usersets were deleted
    String creator = (String) getAuthentication(regularUserToken).getPrincipal();
    UserSetQuery searchQuery = (new UserSetQueryBuilder()).buildUserSetQuery("creator:" + creator,
        null, null, 0, 1, getConfiguration());
    ResultSet<? extends UserSet> results = getUserSetService().search(searchQuery, null,
        Collections.singletonList(LdProfiles.MINIMAL), getAuthentication(regularUserToken));
    assertEquals(0, results.getResultSize());
  }

  @Test
  public void deleteMySets_NotAuthorised() throws Exception {
    mockMvc
        .perform(delete(BASE_URL).header(HttpHeaders.AUTHORIZATION, "")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
  }

  @Test
  public void deleteUserAssociatedSets_NotAdmin() throws Exception {
    mockMvc
        .perform(delete(BASE_URL).queryParam(WebUserSetFields.PATH_PARAM_CREATOR_ID, "creatorID")
            .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
  }

  @Test
  public void deleteUserAssociatedSets_NotAuthorised() throws Exception {
    mockMvc
        .perform(delete(BASE_URL).queryParam(WebUserSetFields.PATH_PARAM_CREATOR_ID, "creatorID")
            .header(HttpHeaders.AUTHORIZATION, "")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
  }

  @Test
  public void deleteUserAssociatedSets_BadRequest() throws Exception {
    mockMvc
        .perform(delete(BASE_URL).queryParam(WebUserSetFields.PATH_PARAM_CREATOR_ID, "")
            .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  // Delete User set via identifier Tests
  @Test
  public void deleteUserSet_NotAuthorised() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

    mockMvc
        .perform(delete(BASE_URL + "{identifier}", userSet.getIdentifier())
            .header(HttpHeaders.AUTHORIZATION, "")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
  }

  // @Test //a second token is required for this test to work propertly
  public void deleteUserSet_OperationNotAuthorised() throws Exception {
    String testFile = USER_SET_REGULAR;
    WebUserSetImpl userSet = createTestUserSet(testFile, regularUserToken);

    mockMvc
        .perform(delete(BASE_URL + "{identifier}", userSet.getIdentifier())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isForbidden());
    // .header(HttpHeaders.AUTHORIZATION, token2)
    // .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
  }

  @Test
  public void deleteUserSet_UserSetNotFound() throws Exception {
    mockMvc
        .perform(delete(BASE_URL + "{identifier}", "wrong_id")
            .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
  }

  @Test
  public void deleteUserSet_Success() throws Exception {
    String testFile = USER_SET_REGULAR;
    WebUserSetImpl userSet = createTestUserSet(testFile, regularUserToken);

    // delete the identifier
    mockMvc
        .perform(delete(BASE_URL + "{identifier}", userSet.getIdentifier())
            .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
  }

  @Disabled("This test tests the lock/unlock api by checking all locked api methods one by one."
      + "Currently the lock api locks all write methods and the check for locked is added to the verifyWriteAccess"
      + "which is called upon authorization for all write methods.")
  @Test
  public void lockUnlockServices2() throws Exception {
    //lock the methods
    mockMvc
      .perform(post("/admin/lock")
        .header(HttpHeaders.AUTHORIZATION, adminUserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk());
    
    //test create to be locked
    mockMvc
      .perform(post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
        .content("{}").header(HttpHeaders.AUTHORIZATION, regularUserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().is(HttpStatus.LOCKED.value()));

    //test update to be locked
    mockMvc
      .perform(put(BASE_URL + "{identifier}", "1")
        .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
        .content("{}").header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().is(HttpStatus.LOCKED.value()));

    //test delete single set to be locked
    mockMvc
      .perform(delete(BASE_URL + "{identifier}", "1")
        .header(HttpHeaders.AUTHORIZATION, regularUserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().is(HttpStatus.LOCKED.value()));
    
    //test delete all to be locked
    mockMvc
      .perform(delete(BASE_URL).header(HttpHeaders.AUTHORIZATION, regularUserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().is(HttpStatus.LOCKED.value()));
    
    //test insert item to be locked
    mockMvc
      .perform(put(BASE_URL + "{identifier}/{datasetId}/{localId}", "1", "01", "123_test")
        .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
        .header(HttpHeaders.AUTHORIZATION, editor2UserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().is(HttpStatus.LOCKED.value()));

    //test delete item to be locked
    mockMvc
    .perform(
        delete(BASE_URL + "{identifier}/{datasetId}/{localId}", "1", "01", "123_test")
        .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
        .header(HttpHeaders.AUTHORIZATION, editor2UserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
    .andExpect(status().is(HttpStatus.LOCKED.value()));

    //test publish set to be locked
    mockMvc
      .perform(MockMvcRequestBuilders.put(BASE_URL + "1" + "/publish")
        .header(HttpHeaders.AUTHORIZATION, publisherUserToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name()))
      .andExpect(status().is(HttpStatus.LOCKED.value()));

    //test unpublish set to be locked
    mockMvc
      .perform(MockMvcRequestBuilders.put(BASE_URL + "1" + "/unpublish")
        .header(HttpHeaders.AUTHORIZATION, publisherUserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name()))
      .andExpect(status().is(HttpStatus.LOCKED.value()));
    
    //unlock the methods
    mockMvc
      .perform(post("/admin/unlock")
        .header(HttpHeaders.AUTHORIZATION, adminUserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk()); 

    //test create after unlock
    String requestJson = getJsonStringInput(USER_SET_REGULAR);
    mockMvc
      .perform(
        post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
          .content(requestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
          .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isCreated());
    
    //test update after unlock
    WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);
    String updatedRequestJson = getJsonStringInput(UPDATED_USER_SET_CONTENT);
    mockMvc
      .perform(put(BASE_URL + "{identifier}", userSet.getIdentifier())
        .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
        .content(updatedRequestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk());
   
    //test delete single set after unlock
    mockMvc
        .perform(delete(BASE_URL + "{identifier}", userSet.getIdentifier())
            .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));    
    
    //test delete all after unlock
    createTestUserSet(USER_SET_REGULAR, regularUserToken);
    mockMvc
      .perform(delete(BASE_URL).header(HttpHeaders.AUTHORIZATION, regularUserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    
    //test insert item after unlock
    userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
    mockMvc
        .perform(put(BASE_URL + "{identifier}/{datasetId}/{localId}", userSet.getIdentifier(), "01", "123_test")
            .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .header(HttpHeaders.AUTHORIZATION, editor2UserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.OK.value()));

    //test delete item after unlock
    getUserSetService().insertItem("02", "123_test", WebUserSetModelFields.PINNED_POSITION, userSet);
    mockMvc
      .perform(
          delete(BASE_URL + "{identifier}/{datasetId}/{localId}", userSet.getIdentifier(), "02", "123_test")
            .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .header(HttpHeaders.AUTHORIZATION, editor2UserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().is(HttpStatus.OK.value()));
    
    //test publish after unlock
    userSet = createTestUserSet(USER_SET_REGULAR, publisherUserToken);
    mockMvc
      .perform(MockMvcRequestBuilders.put(BASE_URL + userSet.getIdentifier() + "/publish")
        .header(HttpHeaders.AUTHORIZATION, publisherUserToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name()))
    .andExpect(status().is(HttpStatus.OK.value()));
    
    //test unpublish after unlock
    mockMvc
        .perform(MockMvcRequestBuilders.put(BASE_URL + userSet.getIdentifier() + "/unpublish")
          .header(HttpHeaders.AUTHORIZATION, publisherUserToken)
          .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name()))
        .andExpect(status().is(HttpStatus.OK.value()));
    
    writeLockService.deleteAllLocks();
  }
  
  //here we test only the create method (see the comment on the above, disabled, lock test)
  @Test
  public void lockUnlockServices() throws Exception {
    //lock the methods
    mockMvc
      .perform(post("/admin/lock")
        .header(HttpHeaders.AUTHORIZATION, adminUserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk());
    
    //test create to be locked
    mockMvc
      .perform(post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
        .content("{}").header(HttpHeaders.AUTHORIZATION, regularUserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().is(HttpStatus.LOCKED.value()));
    
    //unlock the methods
    mockMvc
      .perform(post("/admin/unlock")
        .header(HttpHeaders.AUTHORIZATION, adminUserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk()); 

    //test create after unlock
    String requestJson = getJsonStringInput(USER_SET_REGULAR);
    mockMvc
      .perform(
        post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
          .content(requestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
          .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isCreated());
    
    //to leave the clean state in the db since the lock objects are created
    writeLockService.deleteAllLocks();
  }
  
}
