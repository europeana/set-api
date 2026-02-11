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
import java.util.Date;

import eu.europeana.api.commons_sb3.definitions.utils.DateUtils;
import eu.europeana.api.set.integration.exception.SetIntegrationException;
import eu.europeana.set.definitions.config.UserSetConfigurationImpl;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import eu.europeana.api.commons_sb3.definitions.search.ResultSet;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonLdConstants;
import eu.europeana.api.set.integration.IntegrationTestSetup;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.AgentTypes;
import eu.europeana.set.definitions.model.vocabulary.ProfileConstants;
import eu.europeana.set.definitions.model.vocabulary.SetPageProfile;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.web.model.WebUserSetImpl;
import eu.europeana.set.web.search.UserSetQueryBuilder;

import static eu.europeana.api.commons_sb3.definitions.http.HttpHeaders.*;

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
public class WebUserSetRestIT extends IntegrationTestSetup {

  @BeforeAll
  static void initTokens() throws SetIntegrationException {
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
            post(BASE_URL)
                .content(requestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    String identifier = getSetIdentifier(getConfiguration().getSetDataEndpoint(), result);
    assertNotNull(identifier);
    addToCreatedSets(identifier);
  }

  @Test
  public void createGalleryWithDepiction() throws Exception {
   String requestJson = getJsonStringInput(USER_SET_GALLERY_DEPICTION);

   String result = mockMvc
       .perform(
           post(BASE_URL)
               .content(requestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
               .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
       .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
   String identifier = getSetIdentifier(getConfiguration().getSetDataEndpoint(), result);
   assertNotNull(identifier);
   addToCreatedSets(identifier);
   
   assertTrue(containsKeyOrValue(result, WebUserSetFields.TYPE_GALLERY));
   assertTrue(containsKeyOrValue(result, WebUserSetFields.IS_SHOWN_BY));
 }

  
  @Test
  public void create_UserSet_DynamicCollecton_without_isDefinedBy() throws Exception {
    String requestJson = getJsonStringInput(USER_SET_OPEN);
    JSONObject withoutIsDefinedBy = new JSONObject(requestJson);
    withoutIsDefinedBy.remove(WebUserSetModelFields.IS_DEFINED_BY);
    
    mockMvc
    .perform(
        post(BASE_URL)
            .content(withoutIsDefinedBy.toString()).header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
    .andExpect(status().isBadRequest());
  }

    @Test
    public void create_UserSet_WithIsDefinedBy_Success() throws Exception {
        String requestJson = getJsonStringInput(USER_SET_OPEN);

        String result = mockMvc
                .perform(
                        post(BASE_URL)
                                .content(requestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

        String identifier = getSetIdentifier(getConfiguration().getSetDataEndpoint(), result);
        assertNotNull(identifier);
        addToCreatedSets(identifier);

        assertNotNull(result);
        // check isDefinedBy is present and in without any wskey param
        assertTrue(containsKeyOrValue(result, WebUserSetModelFields.IS_DEFINED_BY));
        assertFalse(getvalueOfkey(result, WebUserSetModelFields.IS_DEFINED_BY).contains("wskey="));
    }

    @Test
    public void create_UserSet_WithIsDefinedBy_InvalidUrl() throws Exception {
        String requestJson = getJsonStringInput(USER_SET_OPEN);

        // update the isDefinedBy with different environment value
       requestJson = requestJson.replace("https://api.europeana.eu",
               "http://localhost:8080");

        String result = mockMvc
                .perform(
                        post(BASE_URL)
                                .content(requestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();

        assertNotNull(result);
        assertTrue(StringUtils.contains(result,
                "Invalid property value. isDefinedBy :  the access to api endpoint is not allowed"));
  }


    @Test
  public void create_UserSet_Collection_with_isDefinedBy() throws Exception {
    String requestJson = getJsonStringInput(USER_SET_REGULAR);
    JSONObject withIsDefinedBy = new JSONObject(requestJson);
    withIsDefinedBy.append(WebUserSetModelFields.IS_DEFINED_BY, "https://api.europeana.eu/api/v2/search.json?query=%28europeana_collectionName%3A08641%2A%20AND%20NOT%28proxy_dc_identifier%3A%5C%221037479000000477087%5C%22%29%29");
    
    mockMvc
    .perform(
        post(BASE_URL)
            .content(withIsDefinedBy.toString()).header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
    .andExpect(status().isBadRequest());
  }
  
  @Test
  public void create_UserSet_401_bad_request_InvalidInput() throws Exception {
    mockMvc
        .perform(
            post(BASE_URL)
                .content("{}").header(HttpHeaders.AUTHORIZATION, regularUserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void create_published_UserSet_400_bad_request_InvalidInput() throws Exception {
    String requestJson = getJsonStringInput(USER_SET_REGULAR_PUBLISHED);
    mockMvc
        .perform(
            post(BASE_URL)
                .content(requestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }

  //@Test
  //@Disabled("Items are now ignored in the create request")
  public void create_UserSet_InvalidItems() throws Exception {
    String requestJson = getJsonStringInput(USER_SET_INVALID_ITEMS);
    mockMvc
        .perform(
            post(BASE_URL)
                .content(requestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
        //.andExpect(result -> assertEquals(2, StringUtils.countMatches(Arrays.toString(((ItemValidationException)result.getResolvedException()).getI18nParams()),"http")));
  }  
  
  @Test
  public void create_UserSet_400_unauthorized_InvalidJWTToken() throws Exception {
    String requestJson = getJsonStringInput(USER_SET_REGULAR);

    mockMvc
        .perform(post(BASE_URL)
            .content(requestJson).header(HttpHeaders.AUTHORIZATION, "")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
  }
  
  //@Test
//  @Disabled("Items are now ignored in the create request")
  void create_Collection_numberOfItemsLimit() throws Exception {
    String setJsonInit = getJsonStringInput(USER_SET_LARGE);
    JSONObject setJson=new JSONObject(setJsonInit);
    setJson.getJSONArray("items").put("http://data.europeana.eu/item/9200388/test_item");

    String result = mockMvc
        .perform(
            post(BASE_URL)
                .content(setJson.toString()).header(HttpHeaders.AUTHORIZATION, regularUserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();

    assertTrue(result.contains("Number of items") && result.contains("above") && result.contains("limit"));

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
            // .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, "10")
            .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andReturn().getResponse();

    assertEquals(response.getHeader(HttpHeaders.CONTENT_TYPE), CONTENT_TYPE_JSONLD_UTF8);
    assertNotNull(response.getHeader(HttpHeaders.ETAG));
    // check last Modified
      assertNotNull(response.getHeader(HttpHeaders.LAST_MODIFIED));
      assertEquals(new Date(response.getHeader(HttpHeaders.LAST_MODIFIED)),
              new Date(DateUtils.getRFC_1123_FormatDate(userSet.getModified())));

      String result = response.getContentAsString();
    assertNotNull(result);
    assertEquals(HttpStatus.OK.value(), response.getStatus());
    assertTrue(containsKeyOrValue(result, CommonLdConstants.COLLECTION));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.FIRST));
      /**
       * check pagination values for set that has items less than 10
       */
      assertTrue(containsKeyOrValue(result, WebUserSetFields.LAST));
      assertEquals(7,
            (new JSONObject(result)).get(WebUserSetFields.TOTAL));
      assertEquals(getUserSetUtils().fillPage(
              userSet, getConfiguration(), 1, UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE),
              (new JSONObject(result)).get(WebUserSetFields.FIRST).toString());
      assertEquals(getUserSetUtils().fillPage(
                      userSet, getConfiguration(), 1, UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE),
              (new JSONObject(result)).get(WebUserSetFields.LAST).toString());

      // the default minimal profile is used
    assertFalse(containsKeyOrValue(result, WebUserSetFields.ITEMS));
    // without page in request, it is not a collection page
    assertFalse(containsKeyOrValue(result, CommonLdConstants.COLLECTION_PAGE));
    assertFalse(containsKeyOrValue(result, WebUserSetFields.PART_OF));
    assertEquals(AgentTypes.PERSON.getJsonValue(), ((JSONObject)(new JSONObject(result)).get("creator")).getString("type"));
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
            .content(updatedRequestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }
  
  //@Test
  @Disabled("Update does not suport item list anymore. This test sould be migrated to insertMultipleItems tests")
  public void updateUserSet_InvalidItems() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

    String updatedRequestJson = getJsonStringInput(USER_SET_INVALID_ITEMS);
    // update the userset
    mockMvc
        .perform(put(BASE_URL + "{identifier}", userSet.getIdentifier())
            .content(updatedRequestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
       // .andExpect(result -> assertEquals(2, StringUtils.countMatches(Arrays.toString(((ItemValidationException)result.getResolvedException()).getI18nParams()),"http")));
  }

  @Test
  public void updateUserSet_Success() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

    String updatedRequestJson = getJsonStringInput(UPDATED_USER_SET_CONTENT);
    // update the userset
    MockHttpServletResponse response = mockMvc
        .perform(put(BASE_URL + "{identifier}", userSet.getIdentifier())
            .content(updatedRequestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andReturn().getResponse();

    String result = response.getContentAsString();
    assertNotNull(result);
    assertTrue(containsKeyOrValue(result, UserSetUtils
        .buildUserSetId(getConfiguration().getSetDataEndpoint(), userSet.getIdentifier())));
    // the minimal profile is always used in the respose, so no items
    assertFalse(containsKeyOrValue(result, WebUserSetFields.ITEMS));

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
        Collections.singletonList(SetPageProfile.ITEMS), getAuthentication(regularUserToken));
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
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
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
  
  @Test
  void insertItems_Collection_limitReached() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE, regularUserToken);
    String identifier = userSet.getIdentifier();

    String result = mockMvc
        .perform(put(BASE_URL + "{identifier}/{datasetId}/{localId}", identifier, "01", "123_test")
            .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS)
            .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();

    assertTrue(result.contains("limit") && result.contains("reached"));

  }
  
  @Test
  void changeTypeToGallery_itemsLimit() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE, regularUserToken);
    String identifier = userSet.getIdentifier();

    //update type to Collection, to add items over the limit (for Galleries)
    String updatedRequestJson = getJsonStringInput(UPDATED_USER_SET_CONTENT);
    mockMvc
        .perform(put(BASE_URL + "{identifier}", identifier)
            .content(updatedRequestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andReturn().getResponse();

    //insert one item over the limit
    mockMvc
        .perform(put(BASE_URL + "{identifier}/{datasetId}/{localId}", identifier, "01", "123_test")
            .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS)
            .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse()
        .getContentAsString();

    //change type to Gallery
    JSONObject updatedGallery = new JSONObject(updatedRequestJson);
    updatedGallery.put(WebUserSetModelFields.COLLECTION_TYPE, WebUserSetModelFields.TYPE_GALLERY);
    String response = mockMvc
        .perform(put(BASE_URL + "{identifier}", userSet.getIdentifier())
            .content(updatedGallery.toString()).header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
        .andReturn().getResponse().getContentAsString();
    assertTrue(response.contains("items") && response.contains("above") && response.contains("limit"));
  }

  @Test
  void insertAndDeleteMultipleItems() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);
    
    //inserting items with both full and partial url
    String item1="http://data.europeana.eu/item/01/123_newItem";
    String item2="/02/223_newItem";
    JSONArray newItemsJson = new JSONArray();
    newItemsJson.put(item1);
    newItemsJson.put(item2);
    
    mockMvc.perform(
        put(BASE_URL + "{identifier}/items", userSet.getIdentifier())
          .content(newItemsJson.toString())
          .header(HttpHeaders.AUTHORIZATION, adminUserToken)
          .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.OK.value()))
        .andReturn().getResponse()
        .getContentAsString();

    UserSet updatedUserSet = getUserSetService().getUserSetById(userSet.getIdentifier());
    //check for the new items
    assertTrue(updatedUserSet.getItems().contains(item1));
    String item2FullUrl = UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), item2);
    assertTrue(updatedUserSet.getItems().contains(item2FullUrl));

    //deleting new items
    mockMvc
        .perform(
            delete(BASE_URL + "{identifier}/items", userSet.getIdentifier())
                .content(newItemsJson.toString())
                .header(HttpHeaders.AUTHORIZATION, adminUserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse();

    //check items are deleted
    updatedUserSet = getUserSetService().getUserSetById(userSet.getIdentifier());
    assertFalse(updatedUserSet.getItems().contains(item1));
    assertFalse(updatedUserSet.getItems().contains(item2FullUrl));
  }
  


}