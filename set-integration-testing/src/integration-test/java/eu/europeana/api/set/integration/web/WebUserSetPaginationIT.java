package eu.europeana.api.set.integration.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
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
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.definitions.vocabulary.CommonLdConstants;
import eu.europeana.api.set.integration.BaseUserSetTestUtils;
import eu.europeana.set.definitions.config.UserSetConfigurationImpl;
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
public class WebUserSetPaginationIT extends BaseUserSetTestUtils {

  @BeforeAll
  public static void initTokens() {
    if (DISABLE_AUTH) {
      return;
    }
    initRegularUserToken();
  }


  @AfterEach
  protected void deleteCreatedSets() {
    super.deleteCreatedSets();
  }

  @Test
  public void getUserSetPagination() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE, regularUserToken);

    // get the identifier
    final String secondPageIndex = "2";
    MockHttpServletResponse response = mockMvc
        .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
            .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE, secondPageIndex)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, "5")
            .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andReturn().getResponse();

    //
    String secondPageJson = response.getContentAsString();
    assertNotNull(secondPageJson);
    assertEquals(HttpStatus.OK.value(), response.getStatus());
    // build collection uri?
    // String collectionUrl = buildCollectionUrl(null, request.getRequestURL().toString(),
    // request.getQueryString());
    // assertTrue(constainsKey(result, collectionUrl));

    assertTrue(containsKeyOrValue(secondPageJson, WebUserSetFields.PART_OF));
    assertTrue(containsKeyOrValue(secondPageJson, CommonLdConstants.COLLECTION));
    assertTrue(containsKeyOrValue(secondPageJson, CommonLdConstants.COLLECTION_PAGE));
    assertTrue(containsKeyOrValue(secondPageJson, WebUserSetFields.START_INDEX));
    assertTrue(containsKeyOrValue(secondPageJson, WebUserSetFields.FIRST));
    assertTrue(containsKeyOrValue(secondPageJson, WebUserSetFields.LAST));
    assertTrue(containsKeyOrValue(secondPageJson, WebUserSetFields.PREV));
    assertTrue(containsKeyOrValue(secondPageJson, WebUserSetFields.NEXT));
    assertTrue(containsKeyOrValue(secondPageJson, WebUserSetFields.ITEMS));
    // verify that the ids are not escaped
    assertTrue(containsKeyOrValue(secondPageJson, "http://data.europeana.eu/item/11648/_Botany_L_1444437"));
    // assertTrue(constainsKeyOrValue(result, WebUserSetFields.ITEMS));

    int idCount = StringUtils.countMatches(secondPageJson, "\"id\"");
    // 1 id part of and one for collection page
    assertEquals(2, idCount);

    int total = StringUtils.countMatches(secondPageJson, "\"total\"");
    // 1 id part of and one for collection page
    assertEquals(2, total);
  }


  @Test
  public void getEmptyUserSet() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_MANDATORY, regularUserToken);

    // get the identifier
    MockHttpServletResponse response = mockMvc
        .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
            .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            // .queryParam(CommonApiConstants.QUERY_PARAM_PAGE, "1")
            // .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, "10")
            .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andReturn().getResponse();

    //
    String result = response.getContentAsString();
    assertNotNull(result);
    assertEquals(HttpStatus.OK.value(), response.getStatus());
    // not available for empty sets
    assertFalse(containsKeyOrValue(result, WebUserSetFields.PREV));
    assertFalse(containsKeyOrValue(result, WebUserSetFields.NEXT));
    assertFalse(containsKeyOrValue(result, WebUserSetFields.ITEMS));

    int idCount = StringUtils.countMatches(result, "\"id\"");
    // 1 id for userset and one for creator
    assertEquals(2, idCount);

    int total = StringUtils.countMatches(result, "\"total\"");
    // 1 total only for the set
    assertEquals(1, total);

    // getUserSetService().deleteUserSet(userSet.getIdentifier());
  }

  @Test
  public void getPageForEmptyUserSet() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_MANDATORY, regularUserToken);

    // get the identifier
    MockHttpServletResponse response = mockMvc
        .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
            .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE, String.valueOf(UserSetUtils.DEFAULT_PAGE))
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, "10")
            .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andReturn().getResponse();

    //
    String result = response.getContentAsString();
    assertNotNull(result);
    assertEquals(HttpStatus.OK.value(), response.getStatus());
    // build collection uri?
    // String collectionUrl = buildCollectionUrl(null, request.getRequestURL().toString(),
    // request.getQueryString());
    // assertTrue(containsKeyOrValue(result, collectionUrl));

    assertTrue(containsKeyOrValue(result, WebUserSetFields.PART_OF));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.COLLECTION));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.COLLECTION_PAGE));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.START_INDEX));
    // for empty collections, isPartOf must not contain first and last
    assertFalse(containsKeyOrValue(result, WebUserSetFields.FIRST));
    assertFalse(containsKeyOrValue(result, WebUserSetFields.LAST));
    // not available for empty sets
    assertFalse(containsKeyOrValue(result, WebUserSetFields.PREV));
    assertFalse(containsKeyOrValue(result, WebUserSetFields.NEXT));
    assertFalse(containsKeyOrValue(result, WebUserSetFields.ITEMS));

    int idCount = StringUtils.countMatches(result, "\"id\"");
    // 3 ids: collection page, set, creator?
    assertEquals(2, idCount);

    int total = StringUtils.countMatches(result, "\"total\"");
    // 2 totals: collection page and set
    assertEquals(2, total);
  }

  @Test
  public void getUserSetPaginationDefaultPageSize() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE, regularUserToken);

    // get the identifier
    final String secondPageIdex = "2";
    MockHttpServletResponse response = mockMvc
        .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
            .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE, secondPageIdex)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, "10")
            .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andReturn().getResponse();

    //
    String result = response.getContentAsString();
    assertNotNull(result);
    assertEquals(HttpStatus.OK.value(), response.getStatus());
    // verify that ids are not escaped, use one item from second page
    assertTrue(containsKeyOrValue(result, "http://data.europeana.eu/item/11647/_Botany_AMD_87140"));

    int defaultPageSize = UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE;
    int pageSize = StringUtils.countMatches(result, "http://data.europeana.eu/item/");
    assertEquals(defaultPageSize, pageSize);
  }

  @Test
  public void getUserSetPaginationItemDescriptions() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE, regularUserToken);

    // get the identifier
    final String secondPageIndex = "2";
    MockHttpServletResponse response =
        mockMvc
            .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
                .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE,
                    LdProfiles.ITEMDESCRIPTIONS.name())
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE, secondPageIndex)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, "10")
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andReturn().getResponse();

    //
    String secondPageContent = response.getContentAsString();
    assertNotNull(secondPageContent);
    assertEquals(HttpStatus.OK.value(), response.getStatus());
    // verify that ids are not escaped, use one item from second page
    assertTrue(containsKeyOrValue(secondPageContent, "\\/11647\\/_Botany_AMD_87140"));

    int defaultPageSize = UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE;
    int pageSize = StringUtils.countMatches(secondPageContent, "\\/item\\/");
    assertEquals(defaultPageSize, pageSize);
  }


  @Test
  @Disabled("disabled for automatic testing, requires search api connection")
  public void getUserSetSecondPageItemDescriptions() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE2, regularUserToken);

    // get the identifier
    final String secondPageContent = "2";
    final String requestedPageSize = "100";
    MockHttpServletResponse response =
        mockMvc
            .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
                .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE,
                    LdProfiles.ITEMDESCRIPTIONS.name())
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE, secondPageContent)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, requestedPageSize)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andReturn().getResponse();

    //
    String result = response.getContentAsString();
    assertNotNull(result);
    assertEquals(HttpStatus.OK.value(), response.getStatus());
    // verify that ids are not escaped, use one item from second page
    assertTrue(containsKeyOrValue(result, "\\/22\\/_13784"));

    // int defaultPageSize = UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE;
    // some items are not found, currently only 52 out of 200 are dereferencible on the second page
    int secondPageSize = 52;
    int pageSize = StringUtils.countMatches(result, "\\/item\\/");
    assertEquals(secondPageSize, pageSize);
  }

  @Test
  public void getUserSetPaginationItemDescriptionsOrder() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_TATTOOS, regularUserToken);

    // get the identifier
    final String requestedPageSize = "10";
    MockHttpServletResponse response =
        mockMvc
            .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
                .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE,
                    LdProfiles.ITEMDESCRIPTIONS.name())
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE, String.valueOf(UserSetUtils.DEFAULT_PAGE))
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, requestedPageSize)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andReturn().getResponse();

    String result = response.getContentAsString();
    assertNotNull(result);
    assertEquals(HttpStatus.OK.value(), response.getStatus());

    int defaultPageSize = UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE;
    int pageSize = StringUtils.countMatches(result, "\\/item\\/");

    String[] missingItems = new String[0];
    verifyItemOrder(userSet, result, missingItems);
    assertEquals(defaultPageSize, pageSize);

    // getUserSetService().deleteUserSet(userSet.getIdentifier());
  }


  private void verifyItemOrder(WebUserSetImpl userSet, String result, String[] missingItems)
      throws JSONException {
    JSONObject itemPage = new JSONObject(result);
    JSONArray itemDescriptions = itemPage.getJSONArray("items");
    JSONObject itemDescription;
    String identifier, id;
    int pos;

    // remove missingItems
    if (missingItems != null && missingItems.length > 0) {
      userSet.getItems().removeAll(List.of(missingItems));
    }

    for (int i = 0; i < itemDescriptions.length(); i++) {
      itemDescription = itemDescriptions.getJSONObject(i);
      identifier = itemDescription.getString("id");
      id = "http://data.europeana.eu/item" + identifier;
      pos = userSet.getItems().indexOf(id);
      System.out.println(
          "verifying position for item with identifier: " + identifier + " (id: " + id + ")");

      if (pos >= 0) {
        if (pos > i) {
          System.out.println("Expected items order: \n" + userSet.getItems());
        }
        assertEquals(pos, i);
      } else {
        System.out.println("skipped verification of position for missing item: " + id);
      }
    }

  }

  @Test
  public void getUserSetPaginationEmptyPageNr() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE, regularUserToken);

    // get the identifier
    MockHttpServletResponse response = mockMvc
        .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
            .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE, "")
            // .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, "5")
            .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andReturn().getResponse();

    //
    String result = response.getContentAsString();
    assertNotNull(result);
    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    assertTrue(StringUtils.contains(result, CommonApiConstants.QUERY_PARAM_PAGE));
  }

  @Test
  public void getUserSetPaginationPageSizeExceeded() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE, regularUserToken);

    // get the identifier
    final String requestedPageSize = "200";
    MockHttpServletResponse response = mockMvc
        .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
            .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE, String.valueOf(UserSetUtils.DEFAULT_PAGE))
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, requestedPageSize)
            .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andReturn().getResponse();

    //
    String result = response.getContentAsString();
    assertNotNull(result);
    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    assertTrue(StringUtils.contains(result, CommonApiConstants.QUERY_PARAM_PAGE_SIZE));
  }

}
