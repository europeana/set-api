package eu.europeana.api.set.integration.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import eu.europeana.api.commons.definitions.utils.DateUtils;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.set.integration.BaseUserSetTestUtils;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
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
 * @author GordeaS
 */
@SpringBootTest
public class WebUserSetPublishingIT extends BaseUserSetTestUtils {

  @BeforeAll
  public static void initTokens() {
    if (DISABLE_AUTH) {
      return;
    }
    initRegularUserToken();
    initPublisherUserToken();
  }



  private static final String USERNAME_REGULAR = "username1";
  private static final String USERNAME_PUBLISHER = "publisher-username";



  @AfterEach
  protected void deleteCreatedSets() {
    super.deleteCreatedSets();
  }

  // publish and validate user set tests
  @Test
  public void publishUserSet_Success() throws Exception {
    // create set by publisher
    WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, publisherUserToken);

    // publish set by publisher
    // expected change of ownership to editorial team
    String issued = DateUtils.convertDateToStr(new Date());
    MockHttpServletResponse response =
        publishUserSet(userSet, issued, getConfiguration().getEuropeanaPublisherNickname());
    assertNotNull(response);
  }

  @Test
  public void publishPreviouslyPublishedUserSet() throws Exception {

    WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

    // publish published user set
    publishUserSet(userSet, null, USERNAME_REGULAR);
    Date beforeCallDate = new Date();
    MockHttpServletResponse response = mockMvc
        .perform(MockMvcRequestBuilders.put(BASE_URL + userSet.getIdentifier() + "/publish")
        .header(HttpHeaders.AUTHORIZATION, publisherUserToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())).andReturn().getResponse();

    assertEquals(HttpStatus.OK.value(), response.getStatus());
    String result = response.getContentAsString();
    
    Date issued = DateUtils.parseToDate(getStringValue(result, WebUserSetModelFields.ISSUED));
    Date modified = DateUtils.parseToDate(getStringValue(result, WebUserSetModelFields.MODIFIED));
    assertEquals(issued, modified);
    //the value set in the issued field must be higher or equal (if the test executes fast and no milliseconds are recorded in the date) than the time when the method was called
    Date beforeCallDateFormatAdjusted = DateUtils.parseToDate(DateUtils.convertDateToStr(beforeCallDate));
    assertTrue(issued.equals(beforeCallDateFormatAdjusted) || issued.compareTo(beforeCallDateFormatAdjusted)>0);
  }

  // unpublish user set tests
  @Test
  public void unpublishUserSet_Success() throws Exception {
    // create set by publisher
    WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, publisherUserToken);

    // publish set by publisher
    // expected change of ownership to editorial team
    String issued = DateUtils.convertDateToStr(new Date());
    publishUserSet(userSet, issued, getConfiguration().getEuropeanaPublisherNickname());

    MockHttpServletResponse response;
    String result;
    // unpublish set
    response = mockMvc
        .perform(MockMvcRequestBuilders.put(BASE_URL + userSet.getIdentifier() + "/unpublish")
            .header(HttpHeaders.AUTHORIZATION, publisherUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name()))
        .andReturn().getResponse();

    result = response.getContentAsString();
    assertNotNull(result);
    assertTrue(containsKeyOrValue(result, UserSetUtils
        .buildUserSetId(getConfiguration().getSetDataEndpoint(), userSet.getIdentifier())));
    assertTrue(containsKeyOrValue(result, "public"));
    assertFalse(containsKeyOrValue(result, WebUserSetModelFields.ISSUED));
    // unpublished set, the ownership is changed back to current user
    assertFalse(containsKeyOrValue(result, getConfiguration().getEuropeanaPublisherNickname()));
    assertTrue(containsKeyOrValue(result, USERNAME_PUBLISHER));
    assertEquals(HttpStatus.OK.value(), response.getStatus());
  }

  @Test
  public void unpublishNotPublishedUserSet() throws Exception {
    // create set by publisher
    WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, publisherUserToken);

    // unpublish unpublished user set
    // must return 400
    mockMvc
        .perform(MockMvcRequestBuilders.put(BASE_URL + userSet.getIdentifier() + "/unpublish")
            .header(HttpHeaders.AUTHORIZATION, publisherUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name()))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }


  @Test
  public void updatePublishedUserSet_Success() throws Exception {
    // create userset
    WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

    // publish userset by other user, the ownership stays with the creator
    publishUserSet(userSet, null, USERNAME_REGULAR);

    // update userset
    String updatedRequestJson = getJsonStringInput(USER_SET_REGULAR_UPDATED);

    MockHttpServletResponse response = mockMvc
        .perform(MockMvcRequestBuilders.put(BASE_URL + userSet.getIdentifier())
            .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .content(updatedRequestJson).header(HttpHeaders.AUTHORIZATION, publisherUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andReturn().getResponse();

    String result = response.getContentAsString();
    assertNotNull(result);
    assertEquals(HttpStatus.OK.value(), response.getStatus());
    assertTrue(containsKeyOrValue(result, UserSetUtils
        .buildUserSetId(getConfiguration().getSetDataEndpoint(), userSet.getIdentifier())));
    assertTrue(containsKeyOrValue(result, "published"));
    // published by owner, the ownership is changed back to publisher
    assertFalse(containsKeyOrValue(result, USERNAME_PUBLISHER));
    assertTrue(containsKeyOrValue(result, USERNAME_REGULAR));
    // check the updated value of the title
    assertTrue(containsKeyOrValue(result, "Sportswear-updated"));
    // check size of the items
    int itemCount = StringUtils.countMatches(result, "data.europeana.eu/item/");
    assertEquals(5, itemCount);
  }

  @Test
  public void updatePublishedUserSetWithVisibility_Success() throws Exception {
    // create userset
    WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

    // publish userset by other user, the ownership stays with the creator
    publishUserSet(userSet, null, USERNAME_REGULAR);


    // update userset
    String updatedRequestJson = getJsonStringInput(USER_SET_REGULAR_PUBLISHED);

    MockHttpServletResponse response = mockMvc
        .perform(MockMvcRequestBuilders.put(BASE_URL + userSet.getIdentifier())
            .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .content(updatedRequestJson).header(HttpHeaders.AUTHORIZATION, publisherUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andReturn().getResponse();

    String result = response.getContentAsString();
    assertNotNull(result);
    assertEquals(HttpStatus.OK.value(), response.getStatus());
    assertTrue(containsKeyOrValue(result, UserSetUtils
        .buildUserSetId(getConfiguration().getSetDataEndpoint(), userSet.getIdentifier())));
    assertTrue(containsKeyOrValue(result, "published"));
    // published by owner, the ownership is changed back to publisher
    assertFalse(containsKeyOrValue(result, USERNAME_PUBLISHER));
    assertTrue(containsKeyOrValue(result, USERNAME_REGULAR));
    // check the updated value of the title
    assertTrue(containsKeyOrValue(result, "Sportswear-updated"));
    // check size of the items
    int itemCount = StringUtils.countMatches(result, "data.europeana.eu/item/");
    assertEquals(5, itemCount);
  }

  @Test
  public void addItemToPublishedSet() throws Exception {
    // create userset
    WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

    // publish userset by other user, the ownership stays with the creator
    publishUserSet(userSet, null, USERNAME_REGULAR);


    // add item to userset as publisher
    MockHttpServletResponse response = mockMvc
        .perform(put(BASE_URL + "{identifier}/{datasetId}/{localId}", userSet.getIdentifier(), "01",
            "123_test")
                .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
                .header(HttpHeaders.AUTHORIZATION, publisherUserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andReturn().getResponse();

    String result = response.getContentAsString();
    assertNotNull(result);
    assertEquals(HttpStatus.OK.value(), response.getStatus());
    assertTrue(containsKeyOrValue(result, UserSetUtils
        .buildUserSetId(getConfiguration().getSetDataEndpoint(), userSet.getIdentifier())));
    assertTrue(containsKeyOrValue(result, "published"));
    // published by owner, the ownership is changed back to publisher
    // assertFalse(containsKeyOrValue(result, getConfiguration().getEuropeanaPublisherNickname()));
    assertFalse(containsKeyOrValue(result, USERNAME_PUBLISHER));
    assertTrue(containsKeyOrValue(result, USERNAME_REGULAR));
    // check size of the items
    int itemCount = StringUtils.countMatches(result, "data.europeana.eu/item/");
    assertEquals(8, itemCount);
  }

  @Test
  public void removeItemFromPublishedSet() throws Exception {
    // create userset
    WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

    // publish userset by other user, the ownership stays with the creator
    publishUserSet(userSet, null, USERNAME_REGULAR);

    // add item to userset as publisher
    MockHttpServletResponse response = mockMvc
        .perform(delete(BASE_URL + "{identifier}/{datasetId}/{localId}", userSet.getIdentifier(),
            "2048128", "618580")
                .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
                .header(HttpHeaders.AUTHORIZATION, publisherUserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andReturn().getResponse();

    String result = response.getContentAsString();
    assertNotNull(result);
    assertEquals(HttpStatus.OK.value(), response.getStatus());
    assertTrue(containsKeyOrValue(result, UserSetUtils
        .buildUserSetId(getConfiguration().getSetDataEndpoint(), userSet.getIdentifier())));
    assertTrue(containsKeyOrValue(result, "published"));
    // published by owner, the ownership is changed back to publisher
    assertFalse(containsKeyOrValue(result, USERNAME_PUBLISHER));
    assertTrue(containsKeyOrValue(result, USERNAME_REGULAR));
    // check size of the items
    int itemCount = StringUtils.countMatches(result, "data.europeana.eu/item/");
    assertEquals(6, itemCount);
  }


  @Test
  public void checkItemInSetFromPublishedSet() throws Exception {
    // create userset
    WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

    // publish userset by other user, the ownership stays with the creator
    publishUserSet(userSet, null, USERNAME_REGULAR);

    // add item to userset as publisher
    MockHttpServletResponse response = mockMvc
        .perform(head(BASE_URL + "{identifier}/{datasetId}/{localId}", userSet.getIdentifier(),
            "2048128", "618580").header(HttpHeaders.AUTHORIZATION, publisherUserToken))
        .andReturn().getResponse();

    assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
  }

  private MockHttpServletResponse publishUserSet(WebUserSetImpl userSet, String issued,
      String expectedOwner) throws Exception, UnsupportedEncodingException {

    MockHttpServletResponse response =
        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + userSet.getIdentifier() + "/publish")
            .header(HttpHeaders.AUTHORIZATION, publisherUserToken)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .param(WebUserSetFields.REQUEST_PARAM_ISSUED, issued)).andReturn().getResponse();

    String result = response.getContentAsString();
    assertNotNull(result);
    assertTrue(containsKeyOrValue(result, UserSetUtils
        .buildUserSetId(getConfiguration().getSetDataEndpoint(), userSet.getIdentifier())));
    assertTrue(containsKeyOrValue(result, "published"));
    assertTrue(containsKeyOrValue(result, WebUserSetModelFields.ISSUED));
    if (issued != null) {
      assertTrue(containsKeyOrValue(result, issued));
    }
    if (expectedOwner != null) {
      assertTrue(containsKeyOrValue(result, expectedOwner));
    }
    assertEquals(HttpStatus.OK.value(), response.getStatus());
    return response;
  }

    
  @Test
  public void publishNonExistingUserSet() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.put(BASE_URL + "test-dummy" + "/publish")
            .header(HttpHeaders.AUTHORIZATION, publisherUserToken)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name()))
        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
  }
  
  @Test
  public void publishWrongSetType() throws Exception {
    // wrong user set type (bookmark folder)
    WebUserSetImpl userSet2 = createTestUserSet(USER_SET_BOOKMARK_FOLDER, regularUserToken);
    mockMvc
        .perform(MockMvcRequestBuilders.put(BASE_URL + userSet2.getIdentifier() + "/publish")
            .header(HttpHeaders.AUTHORIZATION, publisherUserToken)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name()))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

  }
  
  @Test
  public void publishWithoutPermission() throws Exception {
    // wrong user set identifier
    WebUserSetImpl userSet1 = createTestUserSet(USER_SET_REGULAR, regularUserToken);
    //publish without publish permission 
    mockMvc
        .perform(MockMvcRequestBuilders.put(BASE_URL + userSet1.getIdentifier() + "/publish")
            .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name()))
        .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
  }
   
}
