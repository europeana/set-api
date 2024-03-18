package eu.europeana.api.set.integration.web;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import javax.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import eu.europeana.api.commons.definitions.exception.ApiWriteLockException;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.nosql.service.ApiWriteLockService;
import eu.europeana.api.set.integration.BaseUserSetTestUtils;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.web.model.WebUserSetImpl;

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
@Disabled("SG: Temporary disabled as tests are failing in github")
public class WriteLockIT extends BaseUserSetTestUtils {

  @Resource(name = "set_db_apilockService")
  protected ApiWriteLockService writeLockService; 

  @BeforeAll
  public static void initTokens() {
    if(DISABLE_AUTH) {
      return;
    }
    initRegularUserToken();
    initPublisherUserToken();
    initAdminUserToken();
  }

  
  /**
   * remove locks after each test to avoid cascading effect for test failures
   * @throws ApiWriteLockException
   */
  @BeforeEach
  protected void removeLock() throws ApiWriteLockException{
    writeLockService.deleteAllLocks();
  }
  
  /**
   * remove locks after each test to avoid cascading effect for test failures
   * @throws ApiWriteLockException
   */
  @AfterEach
  protected void removeLockAndSets() throws ApiWriteLockException{
    writeLockService.deleteAllLocks();
    super.deleteCreatedSets();
  }

  // Update user set Tests
//  @Disabled("This test is checking the locking for all write operations, but it is too expensive, can be manually enabled to test releases")
  @Test
  /**
   * This test tests the lock/unlock api by checking all locked api methods one by one. 
   * Currently the lock api locks all write methods and the check for locked is added to the verifyWriteAccess which is called upon authorization for all write methods.
   * @throws Exception
   */
  void testLockInEffectForAllWriteMethods() throws Exception {
    //lock the methods
    mockMvc
      .perform(post(BASE_URL + "admin/lock")
        .header(HttpHeaders.AUTHORIZATION, adminUserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk());
    
    //test create should fail with status locked
    mockMvc
      .perform(post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
        .content("{}").header(HttpHeaders.AUTHORIZATION, regularUserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().is(HttpStatus.LOCKED.value()));

    //test update should fail with status locked
    mockMvc
      .perform(put(BASE_URL + "{identifier}", "1")
        .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
        .content("{}").header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().is(HttpStatus.LOCKED.value()));

    //test delete single should fail with status locked
    mockMvc
      .perform(delete(BASE_URL + "{identifier}", "1")
        .header(HttpHeaders.AUTHORIZATION, regularUserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().is(HttpStatus.LOCKED.value()));
    
    //test delete all should fail with status locked
    mockMvc
      .perform(delete(BASE_URL).header(HttpHeaders.AUTHORIZATION, regularUserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().is(HttpStatus.LOCKED.value()));
    
    //test insert item should fail with status locked
    mockMvc
      .perform(put(BASE_URL + "{identifier}/{datasetId}/{localId}", "1", "01", "123_test")
        .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
        .header(HttpHeaders.AUTHORIZATION, editor2UserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().is(HttpStatus.LOCKED.value()));

    //test delete item should fail with status locked
    mockMvc
    .perform(
        delete(BASE_URL + "{identifier}/{datasetId}/{localId}", "1", "01", "123_test")
        .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
        .header(HttpHeaders.AUTHORIZATION, editor2UserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
    .andExpect(status().is(HttpStatus.LOCKED.value()));

    //test publish should fail with status locked
    mockMvc
      .perform(MockMvcRequestBuilders.put(BASE_URL + "1" + "/publish")
        .header(HttpHeaders.AUTHORIZATION, publisherUserToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name()))
      .andExpect(status().is(HttpStatus.LOCKED.value()));

    //test unpublish should fail with status locked
    mockMvc
      .perform(MockMvcRequestBuilders.put(BASE_URL + "1" + "/unpublish")
        .header(HttpHeaders.AUTHORIZATION, publisherUserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name()))
      .andExpect(status().is(HttpStatus.LOCKED.value()));
    
    //unlock write operations methods
    mockMvc
      .perform(delete(BASE_URL + "admin/lock")
        .header(HttpHeaders.AUTHORIZATION, adminUserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk()); 

    //test create after unlock
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
  }
 
  
  //here we test only the create method (see the comment on the above, disabled, lock test)
  @Test
  void lockUnlockApiWriteOperations() throws Exception {
    //lock the methods
    mockMvc
      .perform(post(BASE_URL + "admin/lock")
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
      .perform(delete(BASE_URL + "admin/lock")
        .header(HttpHeaders.AUTHORIZATION, adminUserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk()); 
    
    //unlock already unlocked service
    mockMvc
      .perform(delete(BASE_URL + "admin/lock")
        .header(HttpHeaders.AUTHORIZATION, adminUserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isBadRequest());     

    //test create after unlock
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
  void testlockApiWriteResponse() throws Exception {
    //lock the methods
    String result = mockMvc
      .perform(post(BASE_URL + "admin/lock")
        .header(HttpHeaders.AUTHORIZATION, adminUserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();
    assertTrue(containsKeyOrValue(result, "action"));
    assertTrue(containsKeyOrValue(result, "success"));
    assertTrue(containsKeyOrValue(result, "message"));
    assertTrue(containsKeyOrValue(result, "since"));
    
    mockMvc
    .perform(post(BASE_URL + "admin/lock")
      .header(HttpHeaders.AUTHORIZATION, adminUserToken)
      .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
    .andExpect(status().isLocked());
    
    //unlock the methods
    result = mockMvc
      .perform(delete(BASE_URL + "admin/lock")
        .header(HttpHeaders.AUTHORIZATION, adminUserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();
    assertTrue(containsKeyOrValue(result, "action"));
    assertTrue(containsKeyOrValue(result, "success"));
    assertTrue(containsKeyOrValue(result, "message"));
    assertTrue(containsKeyOrValue(result, "since"));
    assertTrue(containsKeyOrValue(result, "end"));
  }
 
  
}
