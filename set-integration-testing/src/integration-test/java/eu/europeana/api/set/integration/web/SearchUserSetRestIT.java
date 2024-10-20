package eu.europeana.api.set.integration.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.definitions.vocabulary.CommonLdConstants;
import eu.europeana.api.set.integration.BaseUserSetTestUtils;
import eu.europeana.api.set.integration.connection.http.EuropeanaOauthClient;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.web.model.search.FacetValue;
import eu.europeana.set.web.search.UserSetQueryBuilder;

@SpringBootTest
public class SearchUserSetRestIT extends BaseUserSetTestUtils {

  private static final String API_KEY = "api2demo";
  private static final String SEARCH_URL = "/set/search";
  private static final String SEARCH_SET_ID = WebUserSetFields.SET_ID + ":";
  private static final String SEARCH_INVALID_SET_ID = WebUserSetFields.SET_ID + ":" + "test";
  private static final String PUBLIC_VISIBILITY =
      WebUserSetFields.VISIBILITY + ":" + VisibilityTypes.PUBLIC.getJsonValue();
  private static final String PRIVATE_VISIBILITY =
      WebUserSetFields.VISIBILITY + ":" + VisibilityTypes.PRIVATE.getJsonValue();
  private static final String PUBLISHED_VISIBILITY =
      WebUserSetFields.VISIBILITY + ":" + VisibilityTypes.PUBLISHED.getJsonValue();
  private static final String SEARCH_CREATOR = WebUserSetFields.CREATOR + ":";
  private static final String SEARCH_COLLECTION =
      WebUserSetFields.TYPE + ":" + UserSetTypes.COLLECTION;
  private static final String SEARCH_ENTITY_SET =
      WebUserSetFields.TYPE + ":" + UserSetTypes.ENTITYBESTITEMSSET;
  private static final String SEARCH_TITLE_LANG_EN = WebUserSetFields.LANG + ":" + "en";
  // private static final String SORT_MODIFIED_WebUserSetFields.MODIFIED
  private static final String PAGE_SIZE = "100";


  @BeforeAll
  public static void initTokens() {
    if (DISABLE_AUTH) {
      return;
    }
    initRegularUserToken();
    editorUserToken = retrieveOatuhToken(EuropeanaOauthClient.EDITOR_USER);
    initPublisherUserToken();
  }

  @AfterEach
  protected void deleteCreatedSets() {
    super.deleteCreatedSets();
  }

  @Test
  public void searchEmptyApiKey() throws Exception {
    // UserSet set = createTestUserSet(USER_SET_BOOKMARK_FOLDER, regularUserToken);
    mockMvc
        .perform(
            get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, "")
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "")
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
  }

  @Test
  public void searchInvalidApiKey() throws Exception {

    if (getConfiguration().isApiKeyValidationEnabled()) {
      mockMvc
          .perform(get(SEARCH_URL)
              .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
              .queryParam(CommonApiConstants.PARAM_WSKEY, "invalid_api_key")
              .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "")
              .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
          .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    } else {
      System.out.println("skipped apikey validation test!");
    }

  }

  @Test
  public void searchWithoutApiKey() throws Exception {
    mockMvc
        .perform(
            get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "")
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
  }

  @Test
  public void searchWithValidSetId() throws Exception {
    UserSet set = createTestUserSet(USER_SET_REGULAR, regularUserToken);
    mockMvc
        .perform(
            get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY,
                    SEARCH_SET_ID + set.getIdentifier())
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.OK.value()));
    // delete item created by test
    // getUserSetService().deleteUserSet(set.getIdentifier());

  }

  @Test
  public void searchWithInvalidSetId() throws Exception {
    mockMvc
        .perform(
            get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_INVALID_SET_ID)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  public void searchTitleLang() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);

    mockMvc
        .perform(
            get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_TITLE_LANG_EN)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.OK.value()));
    assertNotNull(set);
  }

  @Test
  public void searchWithPublicVisibility() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);

    mockMvc
        .perform(
            get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, PUBLIC_VISIBILITY)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.OK.value()));

    assertNotNull(set);
  }

  @Test
  public void searchEntitySet() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_BEST_ITEMS, editorUserToken);
    String query = SEARCH_ENTITY_SET;
    String result = mockMvc
        .perform(
            get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse()
        .getContentAsString();

    assertNotNull(result);
    // check id
    // default sorting should include the id on the first position
    final String buildUserSetId =
        UserSetUtils.buildUserSetId(getConfiguration().getSetDataEndpoint(), set.getIdentifier());
    assertTrue(containsKeyOrValue(result, buildUserSetId));

    // delete item created by test
    // getUserSetService().deleteUserSet(set.getIdentifier());
  }

  @Test
  public void searchEntitySetByContributor() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_BEST_ITEMS, editorUserToken);
    String contributor = (String) getAuthentication(editorUserToken).getPrincipal();
    String query = "contributor:" + contributor;
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse()
        .getContentAsString();

    assertNotNull(result);
    // check id
    // default sorting should include the id on the first position
    final String userSetId =
        UserSetUtils.buildUserSetId(getConfiguration().getSetDataEndpoint(), set.getIdentifier());
    assertTrue(containsKeyOrValue(result, userSetId));

    // delete item created by test
    // getUserSetService().deleteUserSet(set.getIdentifier());
  }

  @Test
  public void searchEntitySetByContributorUri() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_BEST_ITEMS, editorUserToken);
    String contributor = (String) getAuthentication(editorUserToken).getPrincipal();
    final String contributorId =
        UserSetUtils.buildUserUri(getConfiguration().getUserDataEndpoint(), contributor);
    String query = "contributor:" + contributorId;
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse()
        .getContentAsString();

    assertNotNull(result);
    // check id
    // default sorting should include the id on the first position
    final String userSetId =
        UserSetUtils.buildUserSetId(getConfiguration().getSetDataEndpoint(), set.getIdentifier());
    assertTrue(containsKeyOrValue(result, userSetId));

    // check contributor
    assertTrue(containsKeyOrValue(result, contributorId));

    // delete item created by test
    // getUserSetService().deleteUserSet(set.getIdentifier());
  }

  @Test
  public void searchSetByEmptyTextQuery() throws Exception {
    // subject in json file: http://data.europeana.eu/concept/base/114
    String query = ":";
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  public void searchSetByTitleQuery() throws Exception {
    // subject in json file: http://data.europeana.eu/concept/base/114
    String query = "title:test";
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  public void searchSetByTextQuery() throws Exception {
    String query = ":sportswear golf";
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();
  }

  @Test
  public void searchSetWithoutTextQueryWithScoreSort() throws Exception {
    // subject in json file: http://data.europeana.eu/concept/base/114
    String query = "visibility:public";
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_SORT, WebUserSetFields.TEXT_SCORE_SORT))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }
  
  @Test
  public void searchSetByTextQueryDefault() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_REGULAR_PUBLIC, editorUserToken);
    // subject in json file: http://data.europeana.eu/concept/base/114
    final String title = set.getTitle().get("en");
    // String query = "sportswear golf";
    String query = title;
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_SORT, WebUserSetFields.TEXT_SCORE_SORT))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse()
        .getContentAsString();

    assertNotNull(result);
    // check id
    // default sorting should include the id on the first position
    assertTrue(containsKeyOrValue(result,
        UserSetUtils.buildUserSetId(getConfiguration().getSetDataEndpoint(), set.getIdentifier())));

    // check subject
    assertTrue(containsKeyOrValue(result, title));

    // delete item created by test
    // getUserSetService().deleteUserSet(set.getIdentifier());
  }

  @Test
  public void searchWithScoreSortInAscOrder() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_REGULAR_PUBLIC, editorUserToken);
    // subject in json file: http://data.europeana.eu/concept/base/114
    final String title = set.getTitle().get("en");
    // String query = "sportswear golf";
    String query = title;
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_SORT, WebUserSetFields.TEXT_SCORE_SORT + " asc"))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  public void searchSetByTextQueryWithMultipleCriteria1() throws Exception {
    String query = "sportswear golf visibility:public";
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();
  }

  @Test
  public void searchSetByTextWithMultipleCriteria2() throws Exception {
    // query parsing for combination like "visibility:public sportswear golf"; is invalid
    String query = "visibility:public :sportswear golf";
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  public void searchSetByTextWithVisibilityFilter() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_REGULAR_PUBLIC, editorUserToken);
    // String contributor = (String) getAuthentication(editorUserToken).getPrincipal();
    // subject in json file: http://data.europeana.eu/concept/base/114
    final String title = set.getTitle().get("en");
    String query = "sportswear golf";
    String qf = "visibility:public";
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_QF, qf)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse()
        .getContentAsString();

    assertNotNull(result);
    // check id
    // default sorting should include the id on the first position
    assertTrue(containsKeyOrValue(result,
        UserSetUtils.buildUserSetId(getConfiguration().getSetDataEndpoint(), set.getIdentifier())));

    // check subject
    assertTrue(containsKeyOrValue(result, title));

    // delete item created by test
    // getUserSetService().deleteUserSet(set.getIdentifier());
  }


  @Test
  public void searchSetMultipleCriteriaWithOutTextQuery() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_REGULAR_PUBLIC, editorUserToken);
    // String contributor = (String) getAuthentication(editorUserToken).getPrincipal();
    // subject in json file: http://data.europeana.eu/concept/base/114
    final String title = set.getTitle().get("en");
    String query = "visibility:public item:/08641/1037479000000476703";
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse()
        .getContentAsString();

    assertNotNull(result);
    // check id
    // default sorting should include the id on the first position
    assertTrue(containsKeyOrValue(result,
        UserSetUtils.buildUserSetId(getConfiguration().getSetDataEndpoint(), set.getIdentifier())));

    // check subject
    assertTrue(containsKeyOrValue(result, title));

    // delete item created by test
    // getUserSetService().deleteUserSet(set.getIdentifier());
  }


  @Test
  public void searchEntitySetBySubject() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_BEST_ITEMS, editorUserToken);
    // String contributor = (String) getAuthentication(editorUserToken).getPrincipal();
    // subject in json file: http://data.europeana.eu/concept/base/114
    final String subject = set.getSubject().get(0);
    String query = "subject:" + subject;
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse()
        .getContentAsString();

    assertNotNull(result);
    // check id
    // default sorting should include the id on the first position
    assertTrue(containsKeyOrValue(result,
        UserSetUtils.buildUserSetId(getConfiguration().getSetDataEndpoint(), set.getIdentifier())));

    // check subject
    assertTrue(containsKeyOrValue(result, subject));

    // delete item created by test
    // getUserSetService().deleteUserSet(set.getIdentifier());
  }

  @Test
  public void searchWithPublicVisibility_ItemsDescription() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);

    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.ITEMDESCRIPTIONS.name())
            .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, PUBLIC_VISIBILITY)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.OK.value()));

    assertNotNull(set);
  }

  @Test
  public void searchWithOpenUserSet_ItemsDescription() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_REGULAR, regularUserToken);

    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.ITEMDESCRIPTIONS.name())
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY,
                (WebUserSetFields.SET_ID + WebUserSetFields.SEPARATOR_SEMICOLON
                    + set.getIdentifier()))
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .header(HttpHeaders.AUTHORIZATION, regularUserToken))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse()
        .getContentAsString();

    assertNotNull(result);

    assertTrue(containsKeyOrValue(result,
        UserSetUtils.buildUserSetId(getConfiguration().getSetDataEndpoint(), set.getIdentifier())));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.ITEMS));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.PART_OF));
    assertEquals("1", getvalueOfkey(result, WebUserSetFields.TOTAL));
    assertEquals(2, noOfOccurance(result, WebUserSetFields.ITEMS));

    // extra check if the items are serialised properly and have extended fields
    assertTrue(containsKeyOrValue(result, "dcDescription"));

    // delete item created by test
    // getUserSetService().deleteUserSet(set.getIdentifier());
  }

  @Test
  public void searchWithPrivateVisibility() throws Exception {
    deleteBookmarkFolder(regularUserToken);
    UserSet set1 = createTestUserSet(USER_SET_MANDATORY, regularUserToken);
    UserSet set2 = createTestUserSet(USER_SET_REGULAR, regularUserToken);
    // Update tests to delete sets before test and enable bookmark folder creation
    UserSet set3 = createTestUserSet(USER_SET_BOOKMARK_FOLDER, regularUserToken);
    mockMvc
        .perform(
            get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, PRIVATE_VISIBILITY)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.OK.value()));

    assertNotNull(set1);
    assertNotNull(set2);
    assertNotNull(set3);
  }

  @Test
  public void searchWithPublishedVisibility() throws Exception {
    UserSet set = createTestUserSet(USER_SET_REGULAR, regularUserToken);
    // publish a user set
    mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + set.getIdentifier() + "/publish")
        .header(HttpHeaders.AUTHORIZATION, publisherUserToken)).andReturn().getResponse();

    mockMvc
        .perform(
            get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, PUBLISHED_VISIBILITY)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.OK.value()));

    // delete item created by test
    // getUserSetService().deleteUserSet(set.getIdentifier());
  }

  @Test
  public void searchWithCreator() throws Exception {
    deleteBookmarkFolder(regularUserToken);
    UserSet set1 = createTestUserSet(USER_SET_REGULAR, regularUserToken);
    UserSet set2 = createTestUserSet(USER_SET_MANDATORY, regularUserToken);
    // Update tests to delete sets before test and enable bookmark folder creation
    UserSet set3 = createTestUserSet(USER_SET_BOOKMARK_FOLDER, regularUserToken);
    String creator = (String) getAuthentication(regularUserToken).getPrincipal();
    String result = mockMvc
        .perform(
            get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
                // apikey will be ignored
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_CREATOR + creator)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse()
        .getContentAsString();
    // check ids
    assertTrue(containsKeyOrValue(result, UserSetUtils
        .buildUserSetId(getConfiguration().getSetDataEndpoint(), set1.getIdentifier())));
    assertTrue(containsKeyOrValue(result, UserSetUtils
        .buildUserSetId(getConfiguration().getSetDataEndpoint(), set2.getIdentifier())));
    assertTrue(containsKeyOrValue(result, UserSetUtils
        .buildUserSetId(getConfiguration().getSetDataEndpoint(), set3.getIdentifier())));

    // delete item created by test
    // getUserSetService().deleteUserSet(set1.getIdentifier());
    // getUserSetService().deleteUserSet(set2.getIdentifier());
    // getUserSetService().deleteUserSet(set3.getIdentifier());
  }

  @Test
  public void searchItemsInSet() throws Exception {
    UserSet set1 = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);

    String setIdentifier = set1.getIdentifier();
    String[] qf = new String[] {"item:/08641/1037479000000476467",
        "item:/08641/1037479000000476875", "item:/11654/_Botany_U_1419207", "item:/2048128/618580",
        "item:/2048128/618580", "item:/2048128/notexisting", "item:/2048128/notexisting1"};
    final String secondPageIndex = String.valueOf(UserSetUtils.DEFAULT_PAGE + 1);
    //using pagesize 2, we get two pages of results (only 4 items found in set)
    //retrieve last page
    String result = callSearchItemsInSet(setIdentifier, qf, secondPageIndex, "2", null);
    // check ids
    String searchUri = "/set/" + setIdentifier + "/search";
    assertTrue(StringUtils.contains(result, searchUri));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.TOTAL));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.RESULT_PAGE));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.RESULT_LIST));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.FIRST));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.LAST));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.PREV));
    // last page no next
    assertTrue(!containsKeyOrValue(result, WebUserSetFields.NEXT));

    //retrieve fist page of results
    result = callSearchItemsInSet(setIdentifier, qf, String.valueOf(UserSetUtils.DEFAULT_PAGE), "2", null);
    // check ids
    assertTrue(StringUtils.contains(result, searchUri));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.TOTAL));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.RESULT_PAGE));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.RESULT_LIST));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.FIRST));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.LAST));
    // first page no prev
    assertTrue(!containsKeyOrValue(result, WebUserSetFields.PREV));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.NEXT));
  }

  @Test
  public void searchItemsInSetPrivate() throws Exception {
    UserSet set1 = createTestUserSet(USER_SET_REGULAR, regularUserToken);

    String setIdentifier = set1.getIdentifier();
    String[] qf = new String[] {"item:/08641/1037479000000476467",
        "item:/08641/1037479000000476875", "item:/11654/_Botany_U_1419207", "item:/2048128/618580",
        "item:/2048128/618580", "item:/2048128/notexisting", "item:/2048128/notexisting1"};
    String result = callSearchItemsInSet(setIdentifier, qf, null, null, regularUserToken);
    // check ids
    String searchUri = "/set/" + setIdentifier + "/search";
    assertTrue(StringUtils.contains(result, searchUri));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.TOTAL));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.RESULT_PAGE));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.RESULT_LIST));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.FIRST));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.LAST));
    // first page no next
    assertTrue(!containsKeyOrValue(result, WebUserSetFields.PREV));
    // last page no next
    assertTrue(!containsKeyOrValue(result, WebUserSetFields.NEXT));

    // delete item created by test
    // getUserSetService().deleteUserSet(setIdentifier);
  }

  @Test
  public void searchItemsInSet_empty_response() throws Exception {
    UserSet set1 = createTestUserSet(USER_SET_REGULAR, regularUserToken);

    String setIdentifier = set1.getIdentifier();
    String result = callSearchItemsInSet(setIdentifier, new String[] {"item:/nonexisting/item"},
        null, null, regularUserToken);
    // check ids
    String searchUri = "/set/" + setIdentifier + "/search";
    assertTrue(StringUtils.contains(result, searchUri));
    // total should be 0
    assertTrue(containsKeyOrValue(result, WebUserSetFields.TOTAL));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.RESULT_PAGE));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.ID));

    // delete item created by test
    // getUserSetService().deleteUserSet(setIdentifier);
  }

  @Test
  public void searchItemsInSet_No_QF_Param() throws Exception {
    UserSet set1 = createTestUserSet(USER_SET_REGULAR, regularUserToken);
    String setIdentifier = set1.getIdentifier();

    String result = callSearchItemsInSet(setIdentifier, null, null, null, regularUserToken);
    // check ids
    String searchUri = "/set/" + setIdentifier + "/search";
    assertTrue(StringUtils.contains(result, searchUri));
    // total should be 0
    assertTrue(containsKeyOrValue(result, WebUserSetFields.TOTAL));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.RESULT_PAGE));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.ID));

    // delete item created by test
    // getUserSetService().deleteUserSet(setIdentifier);
  }

  private String callSearchItemsInSet(String setIdentifier, String[] qf, String page,
      String pageSize, String regularUserToken) throws UnsupportedEncodingException, Exception {

    MockHttpServletRequestBuilder searchRequest =
        buildSearchItemsInSetRequest(setIdentifier, qf, page, pageSize, regularUserToken);

    return mockMvc.perform(searchRequest).andExpect(status().is(HttpStatus.OK.value())).andReturn()
        .getResponse().getContentAsString();

  }

  private MockHttpServletRequestBuilder buildSearchItemsInSetRequest(String setIdentifier,
      String[] qf, String page, String pageSize, String regularUserToken) {
    MockHttpServletRequestBuilder getRequest = get("/set/" + setIdentifier + "/search")
        .param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name());
    if (regularUserToken != null) {
      getRequest.header(HttpHeaders.AUTHORIZATION, regularUserToken);
    } else {
      getRequest.queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY);
    }

    if (page != null) {
      getRequest.queryParam(CommonApiConstants.QUERY_PARAM_PAGE, page);
    }
    if (pageSize != null) {
      getRequest.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, pageSize);
    }

    // apikey will be ignored
    MockHttpServletRequestBuilder requestBuilder =
        getRequest.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, UserSetQueryBuilder.SEARCH_ALL);

    // add qf param
    if (qf != null) {
      for (int i = 0; i < qf.length; i++) {
        requestBuilder.queryParam(CommonApiConstants.QUERY_PARAM_QF, qf[i]);
      }
    }
    return getRequest;
  }

  @Test
  public void searchTypeCollection() throws Exception {
    UserSet set1 = createTestUserSet(USER_SET_REGULAR, regularUserToken);
    UserSet set2 = createTestUserSet(USER_SET_MANDATORY, regularUserToken);
    mockMvc
        .perform(
            get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_COLLECTION)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.OK.value()));

    assertNotNull(set1);
    assertNotNull(set2);
  }

  // Facet validation
  @Test
  public void searchFacetsNoFacetValidationTest() throws Exception {
    mockMvc
        .perform(
            get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.FACETS.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  public void searchFacetsInvalidFacetValidationTest() throws Exception {
    mockMvc
        .perform(
            get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.FACETS.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
                .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "test"))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  public void searchFacetsEmptyFacetValidationTest() throws Exception {
    mockMvc
        .perform(
            get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.FACETS.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
                .queryParam(CommonApiConstants.QUERY_PARAM_FACET, ""))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  public void searchFacetsMultipleFacetValidationTest() throws Exception {
    mockMvc
        .perform(
            get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.FACETS.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
                .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "item,visibility"))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  // Multiple profile validation
  @Test
  public void searchFacetsMultipleProfileInvalid() throws Exception {
    String profile = LdProfiles.FACETS.name() + "," + "test";
    mockMvc
        .perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, profile)
            .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "visibility"))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  public void searchFacetsMultipleProfileWithoutFacets() throws Exception {
    String profile = LdProfiles.MINIMAL.name() + "," + LdProfiles.STANDARD.name();
    mockMvc
        .perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, profile)
            .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "visibility"))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  public void searchFacetsMultipleInvalidProfileWithFacets() throws Exception {
    String profile = LdProfiles.MINIMAL.name() + "," + LdProfiles.FACETS.name() + ","
        + LdProfiles.STANDARD.name();
    mockMvc
        .perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, profile)
            .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "visibility"))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  public void searchFacetsMultipleValidProfileWithFacets() throws Exception {
    String profile = LdProfiles.MINIMAL.name() + "," + LdProfiles.FACETS.name();
    mockMvc
        .perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, profile)
            .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "visibility"))
        .andExpect(status().is(HttpStatus.OK.value()));
  }

  @Test
  public void searchFacetsMultipleValidProfileWithFacets_Debug() throws Exception {
    String profile = LdProfiles.DEBUG.name() + "," + LdProfiles.FACETS.name();
    mockMvc
        .perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, profile)
            .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "visibility"))
        .andExpect(status().is(HttpStatus.OK.value()));
  }

  @Test
  public void searchFacetsMultipleValidProfileWithFacetsDebug() throws Exception {
    //profiles can also be space separated
    String profile =
        LdProfiles.DEBUG.name() + " " + LdProfiles.FACETS.name() + "  " + LdProfiles.MINIMAL.name();
    mockMvc
        .perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, profile)
            .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "visibility"))
        .andExpect(status().is(HttpStatus.OK.value()));
  }

  @Test
  public void searchFacetsValidFacetTest() throws Exception {
    // delete the bookmarkFolder already if exists
    deleteBookmarkFolder(regularUserToken);
    deleteBookmarkFolder(editorUserToken);

    UserSet set1 = createTestUserSet(USER_SET_BOOKMARK_FOLDER, regularUserToken);
    UserSet set2 = createTestUserSet(USER_SET_BOOKMARK_FOLDER_1, editorUserToken);

    String result = mockMvc
        .perform(
            get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.FACETS.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
                .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "item")
                .queryParam("facet.limit", "11"))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse()
        .getContentAsString();

    // check result
    assertTrue(containsKeyOrValue(result, WebUserSetFields.FACETS));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.TYPE));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.FIELD));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.VALUES));
    // verify the facet values
    checkItemFacets(getFacetResultPage(result));

    assertNotNull(set1);
    assertNotNull(set2);
  }

  @Test
  public void searchFacet0PageSizeTest() throws Exception {
    // delete the bookmarkFolder already if exists
    deleteBookmarkFolder(regularUserToken);
    deleteBookmarkFolder(editorUserToken);

    UserSet set1 = createTestUserSet(USER_SET_BOOKMARK_FOLDER, regularUserToken);
    UserSet set2 = createTestUserSet(USER_SET_BOOKMARK_FOLDER_1, editorUserToken);

    // /set/search?pageSize=0&query=*&profile=facets&wskey=&facet=item
    String result =
        mockMvc
            .perform(get(SEARCH_URL)
                .param(CommonApiConstants.QUERY_PARAM_PROFILE,
                    LdProfiles.FACETS.getRequestParamValue())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, "0")
                .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "item")
                .queryParam("facet.limit", "11"))
            .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse()
            .getContentAsString();

    // check result
    assertTrue(containsKeyOrValue(result, WebUserSetFields.FACETS));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.TYPE));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.FIELD));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.VALUES));
    // no items if pageSize is 0
    assertFalse(containsKeyOrValue(result, WebUserSetFields.ITEMS));
    // verify the facet values
    checkItemFacets(getFacetResultPage(result));

    assertNotNull(set1);
    assertNotNull(set2);
  }

  private void checkItemFacets(List<FacetValue> facetValueResultPages) {
    assertEquals(11, facetValueResultPages.size());
    for (FacetValue facet : facetValueResultPages) {
      if (facet.getLabel().equals("http://data.europeana.eu/item/test1/test")) {
        assertEquals(2, facet.getCount());
      }
      if (facet.getLabel().equals("http://data.europeana.eu/item/tes5/test5")) {
        assertEquals(2, facet.getCount());
      }
      if (facet.getLabel().equals("http://data.europeana.eu/item/tes6/test6")) {
        assertEquals(2, facet.getCount());
      }
      if (facet.getLabel().equals("http://data.europeana.eu/item/test11/test11")) {
        assertEquals(1, facet.getCount());
      }
    }
  }

}
