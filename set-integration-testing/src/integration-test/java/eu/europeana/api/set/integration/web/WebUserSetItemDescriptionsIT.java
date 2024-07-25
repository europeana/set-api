package eu.europeana.api.set.integration.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.definitions.vocabulary.CommonLdConstants;
import eu.europeana.api.set.integration.BaseUserSetTestUtils;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
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
public class WebUserSetItemDescriptionsIT extends BaseUserSetTestUtils {

  @BeforeAll
  public static void initTokens() {
    if(DISABLE_AUTH) {
      return;
    }
    initRegularUserToken();
    initPublisherUserToken();
  }

  @AfterEach
  protected void deleteCreatedSets() {
    super.deleteCreatedSets();
  }

  @Test
  public void getCloseUserSet_ItemDescriptions_without_pagination() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE, regularUserToken);

    MockHttpServletResponse response =
        mockMvc
            .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
                .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE,
                    LdProfiles.ITEMDESCRIPTIONS.name())
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andReturn().getResponse();

    String result = response.getContentAsString();
    assertNotNull(result);
    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
  }

  @Test
  public void getCloseUserSet_ItemDescriptions() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE, regularUserToken);

    // get the identifier
    MockHttpServletResponse response =
        mockMvc
            .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
                .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE,
                    LdProfiles.ITEMDESCRIPTIONS.name())
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE, "1")
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, "10")
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andReturn().getResponse();


    String result = response.getContentAsString();
    assertNotNull(result);
    assertEquals(HttpStatus.OK.value(), response.getStatus());
    assertTrue(containsKeyOrValue(result, UserSetUtils
        .buildUserSetId(getConfiguration().getSetDataEndpoint(), userSet.getIdentifier())));

    // completeness field is not inclused in the minimal profile, must not be present in the results
    // otherwise the default standard profile was used
    assertFalse(containsKeyOrValue(result, "completeness"));

    assertEquals(CommonLdConstants.COLLECTION_PAGE, getvalueOfkey(result, WebUserSetFields.TYPE));
    
    int idCount = StringUtils.countMatches(result, "\"id\"");
    // as pageSize is not passed in the request, only 10 items will be requested for dereference
    // so "id" = 12 (10 + creator id + userset Identifier + multilingual lang "id" in one of item
    // edmPlaceLabelLangAware)
    assertEquals(12, idCount);

    verifyFiveItems(userSet, result, 0);
  }

  private void verifyFiveItems(WebUserSetImpl userSet, String result, int offset)
      throws JSONException {
    JSONObject json = new JSONObject(result);
    JSONArray itemDescriptions = json.getJSONArray("items");

    // check 5 items
    for (int i = 0; i < 5; i++) {
      String itemIdentifier = UserSetUtils.extractItemIdentifier(userSet.getItems().get(i + offset),
          getConfiguration().getItemDataEndpoint());
      String itemDescriptionIdentifier = getSetIdentifier("", itemDescriptions.get(i).toString());
      if (!itemDescriptionIdentifier.equals(itemIdentifier)) {
        System.out.println("item not available anymore: " + itemIdentifier);
      }
      assertEquals(itemIdentifier, itemDescriptionIdentifier);
    }
  }

  @Test
  public void getCloseUserSet_ItemDescriptionsWithPageValues() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE, regularUserToken);

    // get the identifier
    final int secondPageIndex = UserSetUtils.DEFAULT_PAGE + 1;
    MvcResult response = mockMvc.perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
        .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.ITEMDESCRIPTIONS.name())
        .queryParam(CommonApiConstants.QUERY_PARAM_PAGE, String.valueOf(secondPageIndex))
        .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, "100")
        .header(HttpHeaders.AUTHORIZATION, regularUserToken)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).andReturn();

    String result = response.getResponse().getContentAsString();
    // String request = response.getRequest();
    assertNotNull(result);
    assertEquals(HttpStatus.OK.value(), response.getResponse().getStatus());

    // check the collection url
    String baseUrl = getConfiguration().getSetApiEndpoint()
        .replaceFirst(getConfiguration().getApiBasePath(), "");
    assertEquals(CommonLdConstants.COLLECTION_PAGE, getvalueOfkey(result, WebUserSetFields.TYPE));
    String requestedPage = baseUrl + response.getRequest().getPathInfo();
    // int pageSize = 100;
    // int page = 2;
    final String collectionUrl = getUserSetService().buildResultsPageUrl(requestedPage,
        response.getRequest().getQueryString(), null);
    final String resultPageId =
        getUserSetService().buildPageUrl(collectionUrl, secondPageIndex, 100, LdProfiles.ITEMDESCRIPTIONS);
    assertTrue(containsKeyOrValue(result, resultPageId));

    // check part of ID
    final String partOfId = UserSetUtils.buildUserSetId(getConfiguration().getSetDataEndpoint(),
        userSet.getIdentifier());
    assertTrue(containsKeyOrValue(result, partOfId));

    System.out.println(result);

    // as pageSize is 100, only 10 items will be requested for dereference
    // items returned by search api = 100 (including the items with only the ids)
    // other id : userset Identifier + partOf id + 5 in edmPlaceLabelLangAware as a lang
    // so "id" = 107
    // this is used to write to a file for checking the text for testing purposes
    // Path path = Paths.get("/home/items-pagination.txt");
    // Files.write(path, result.getBytes(StandardCharsets.UTF_8));

    // int idCount = StringUtils.countMatches(result, "\"id\"");
    // assertEquals(96, idCount);

    JSONObject json = new JSONObject(result);
    JSONArray itemDescriptions = json.getJSONArray("items");
    int start = 1 * 100;
    // check 5 items
    for (int i = 0; i < 5; i++) {
      String itemIdentifier = UserSetUtils.extractItemIdentifier(userSet.getItems().get(start),
          getConfiguration().getItemDataEndpoint());
      String itemDescriptionIdentifier = getSetIdentifier("", itemDescriptions.get(i).toString());
      assertEquals(itemIdentifier, itemDescriptionIdentifier);
      start++;
    }
  }

  @Test
  public void getCloseUserSet_ItemDescriptionsWithLastPage() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE, regularUserToken);

    // set size = 249 items, request for more than lastPage
    final String firstIndexAfterLastPage = "4";
    MockHttpServletResponse response =
        mockMvc.perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
            .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.ITEMDESCRIPTIONS.name())
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE, firstIndexAfterLastPage)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, "100")
            .header(HttpHeaders.AUTHORIZATION, regularUserToken)).andReturn().getResponse();

    String result = response.getContentAsString();
    assertNotNull(result);
    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

    assertEquals("ErrorResponse", getvalueOfkey(result, WebUserSetFields.TYPE));
  }

  @Test
  public void getOpenUserSet_ItemDescriptions() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_OPEN, regularUserToken);

    // get the identifier
    MockHttpServletResponse response =
        mockMvc
            .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
                .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE,
                    LdProfiles.ITEMDESCRIPTIONS.name())
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, "10")
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andReturn().getResponse();

    //
    String result = response.getContentAsString();
    assertNotNull(result);
    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
  }

  // this test is to verify item search for large queries using POST Search API
  @Test
  public void getOpenUserSetLargeQuery_ItemDescriptions_DefaultPageSize() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE_QUERY_OPEN, regularUserToken);

    String result =
        mockMvc
            .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
                .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE,
                    LdProfiles.ITEMDESCRIPTIONS.name())
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE, "1")
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, "10")
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse()
            .getContentAsString();

    assertNotNull(result);
    assertTrue(containsKeyOrValue(result, UserSetUtils
        .buildUserSetId(getConfiguration().getSetDataEndpoint(), userSet.getIdentifier())));
    //assertEquals(UserSetTypes.DYNAMICCOLLECTION.getJsonValue(), getvalueOfkey(result, WebUserSetFields.TYPE));
    assertEquals(CommonLdConstants.COLLECTION_PAGE, getvalueOfkey(result, WebUserSetFields.TYPE));
    assertEquals("10", getvalueOfkey(result, WebUserSetFields.TOTAL));
    // one of set and one for creator and items = 10 (default pageSize)
    assertEquals(2 + 10, noOfOccurance(result, WebUserSetFields.ID));
    // completeness field is not inclused in the minimal profile, must not be present in the results
    // otherwise the default standard profile was used
    assertFalse(containsKeyOrValue(result, "completeness"));
  }

  // this test is fail-safe check for the open sets, if isdefinedBy has multiple query values
  // to check if UriComponentsBuilder picks all the values from query param correctly
  // See : buildSearchApiPostBody()
  @Test
  public void getOpenUserSetMultipleQuery_ItemDescriptions() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_MULTIPLE_QUERY_OPEN, regularUserToken);

    // get the identifier
    MockHttpServletResponse response =
        mockMvc
            .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
                .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE,
                    LdProfiles.ITEMDESCRIPTIONS.name())
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE, "1")
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, "10")
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andReturn().getResponse();

    //
    String result = response.getContentAsString();
    assertNotNull(result);
    assertEquals(HttpStatus.OK.value(), response.getStatus());
    assertTrue(containsKeyOrValue(result, UserSetUtils
        .buildUserSetId(getConfiguration().getSetDataEndpoint(), userSet.getIdentifier())));

//    assertEquals(UserSetTypes.DYNAMICCOLLECTION.getJsonValue(), getvalueOfkey(result, WebUserSetFields.TYPE));
    assertEquals(CommonLdConstants.COLLECTION_PAGE, getvalueOfkey(result, WebUserSetFields.TYPE));
    
    // completeness field is not inclused in the minimal profile, must not be present in the results
    // otherwise the default standard profile was used
    assertFalse(containsKeyOrValue(result, "completeness"));

  }
}
