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
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.set.integration.BaseUserSetTestUtils;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
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
    if(DISABLE_AUTH) {
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

  // publish/unpublish user set tests
  @Test
  public void publishUnpublishUserSet_Success() throws Exception {
    // create set by publisher
    WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, publisherUserToken);

    // publish set by publisher
    // expected change of ownership to editorial team
    publishUserSet(userSet, getConfiguration().getEuropeanaPublisherNickname());

    MockHttpServletResponse response;
    String result;

    // depublish set
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
    // unpublished set, the ownership is changed back to current user
    assertFalse(containsKeyOrValue(result, getConfiguration().getEuropeanaPublisherNickname()));
    assertTrue(containsKeyOrValue(result, USERNAME_PUBLISHER));
    assertEquals(HttpStatus.OK.value(), response.getStatus());
  }

  @Test
  public void updatePublishedUserSet_Success() throws Exception {
    // create userset
    WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

    // publish userset by other user, the ownership stays with the creator
    publishUserSet(userSet, USERNAME_REGULAR);


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
    publishUserSet(userSet, USERNAME_REGULAR);


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
    publishUserSet(userSet, USERNAME_REGULAR);


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
    publishUserSet(userSet, USERNAME_REGULAR);

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
    publishUserSet(userSet, USERNAME_REGULAR);

    // add item to userset as publisher
    MockHttpServletResponse response = mockMvc
        .perform(head(BASE_URL + "{identifier}/{datasetId}/{localId}", userSet.getIdentifier(),
            "2048128", "618580").header(HttpHeaders.AUTHORIZATION, publisherUserToken))
        .andReturn().getResponse();

    assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
  }

  private void publishUserSet(WebUserSetImpl userSet, String expectedOwner)
      throws Exception, UnsupportedEncodingException {

    MockHttpServletResponse response = mockMvc
        .perform(MockMvcRequestBuilders.put(BASE_URL + userSet.getIdentifier() + "/publish")
            .header(HttpHeaders.AUTHORIZATION, publisherUserToken)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name()))
        .andReturn().getResponse();

    String result = response.getContentAsString();
    assertNotNull(result);
    assertTrue(containsKeyOrValue(result, UserSetUtils
        .buildUserSetId(getConfiguration().getSetDataEndpoint(), userSet.getIdentifier())));
    assertTrue(containsKeyOrValue(result, "published"));
    if (expectedOwner != null) {
      assertTrue(containsKeyOrValue(result, expectedOwner));
    }
    assertEquals(HttpStatus.OK.value(), response.getStatus());
  }


  @Test
  public void publishUnpublishUserSet_Exceptions() throws Exception {

    WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

    mockMvc
        .perform(MockMvcRequestBuilders.put(BASE_URL + "test-dummy" + "/publish")
            .header(HttpHeaders.AUTHORIZATION, publisherUserToken)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name()))
        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));

    mockMvc
        .perform(MockMvcRequestBuilders.put(BASE_URL + userSet.getIdentifier() + "/publish")
            .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name()))
        .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

    getUserSetService().deleteUserSet(userSet.getIdentifier());

    userSet = createTestUserSet(USER_SET_BOOKMARK_FOLDER, regularUserToken);

    mockMvc
        .perform(MockMvcRequestBuilders.put(BASE_URL + userSet.getIdentifier() + "/publish")
            .header(HttpHeaders.AUTHORIZATION, publisherUserToken)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name()))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }
}