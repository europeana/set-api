package eu.europeana.api.set.integration.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonLdConstants;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.ProfileConstants;
import eu.europeana.set.definitions.model.vocabulary.SetPageProfile;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.web.search.UserSetQueryBuilder;

@SpringBootTest
class SearchUserSetRestIT extends BaseSearchUserSetTesting{

  @Test
  void searchWithValidSetId() throws Exception {
    UserSet set = createTestUserSet(USER_SET_REGULAR, regularUserToken);
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_META)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_SET_ID + set.getIdentifier())
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.OK.value()));
    // delete item created by test
    // getUserSetService().deleteUserSet(set.getIdentifier());

  }

  @Test
  void searchTitleLang() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);

    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_META)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_TITLE_LANG_EN)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.OK.value()));
    assertNotNull(set);
  }

  @Test
  void searchWithPublicVisibility() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);

    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_META)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, PUBLIC_VISIBILITY)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.OK.value()));

    assertNotNull(set);
  }

  @Test
  void searchEntitySet() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_BEST_ITEMS, editorUserToken);
    String query = SEARCH_ENTITY_SET;
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
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
  }

  @Test
  void searchEntitySetByContributor() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_BEST_ITEMS, editorUserToken);
    String contributor = (String) getAuthentication(editorUserToken).getPrincipal();
    String query = "contributor:" + contributor;
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
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
  }

  @Test
  void searchEntitySetByContributorUri() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_BEST_ITEMS, editorUserToken);
    String contributor = (String) getAuthentication(editorUserToken).getPrincipal();
    final String contributorId =
        UserSetUtils.buildUserUri(getConfiguration().getUserDataEndpoint(), contributor);
    String query = "contributor:" + contributorId;
    String result =
        mockMvc
            .perform(get(SEARCH_URL)
                .param(CommonApiConstants.QUERY_PARAM_PROFILE,
                    ProfileConstants.VALUE_PARAM_ITEMS_META)
                    .header(HttpHeaders.AUTHORIZATION, editorUserToken)
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
  }

  @Test
  void searchSetByTextQueryDefault() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_REGULAR_PUBLIC, editorUserToken);
    // subject in json file: http://data.europeana.eu/concept/base/114
    final String title = set.getTitle().get("en");
    // String query = "sportswear golf";
    String query = title;
    String result =
        mockMvc
            .perform(get(SEARCH_URL)
                .param(CommonApiConstants.QUERY_PARAM_PROFILE,
                    ProfileConstants.VALUE_PARAM_ITEMS_META)
                    .header(HttpHeaders.AUTHORIZATION, regularUserToken)
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
  }

  @Test
  void searchSetByTextWithVisibilityFilter() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_REGULAR_PUBLIC, editorUserToken);
    // String contributor = (String) getAuthentication(editorUserToken).getPrincipal();
    // subject in json file: http://data.europeana.eu/concept/base/114
    final String title = set.getTitle().get("en");
    String query = "sportswear golf";
    String qf = "visibility:public";
    String result =
        mockMvc
            .perform(get(SEARCH_URL)
                .param(CommonApiConstants.QUERY_PARAM_PROFILE,
                    ProfileConstants.VALUE_PARAM_ITEMS_META)
                    .header(HttpHeaders.AUTHORIZATION, regularUserToken)
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
  }


  @Test
  void searchSetMultipleCriteriaWithOutTextQuery() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_REGULAR_PUBLIC, editorUserToken);
    String creator = (String) getAuthentication(editorUserToken).getPrincipal();
    // subject in json file: http://data.europeana.eu/concept/base/114
    final String title = set.getTitle().get("en");
    String query = "visibility:public item:/08641/1037479000000476703 creator:" + creator;
    String result =
        mockMvc
            .perform(get(SEARCH_URL)
                .param(CommonApiConstants.QUERY_PARAM_PROFILE,
                    ProfileConstants.VALUE_PARAM_ITEMS_META)
                    .header(HttpHeaders.AUTHORIZATION, regularUserToken)
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
  }


  @Test
  void searchEntitySetBySubject() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_BEST_ITEMS, editorUserToken);
    // String contributor = (String) getAuthentication(editorUserToken).getPrincipal();
    // subject in json file: http://data.europeana.eu/concept/base/114
    final String subject = set.getSubject().get(0);
    String query = "subject:" + subject;
    String result =
        mockMvc
            .perform(get(SEARCH_URL)
                .param(CommonApiConstants.QUERY_PARAM_PROFILE,
                    ProfileConstants.VALUE_PARAM_ITEMS_META)
                    .header(HttpHeaders.AUTHORIZATION, regularUserToken)
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
  }

  @Test
  void searchWithPublicVisibility_ItemsDescription() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);

    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS_META)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, PUBLIC_VISIBILITY)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.OK.value()));

    assertNotNull(set);
  }

  @Test
  void searchWithOpenUserSet_SetsMeta() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_REGULAR, regularUserToken);

    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS_META)
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
    // search is not dereferencing items anymore
    assertEquals(1, noOfOccurance(result, WebUserSetFields.ITEMS));

    // extra check if the sets (items) are serialised properly and have extended fields
    assertTrue(containsKeyOrValue(result, "title"));
  }

  @Test
  void searchWithPrivateVisibility() throws Exception {
    deleteBookmarkFolder(regularUserToken);
    UserSet set1 = createTestUserSet(USER_SET_MANDATORY, regularUserToken);
    UserSet set2 = createTestUserSet(USER_SET_REGULAR, regularUserToken);
    // Update tests to delete sets before test and enable bookmark folder creation
    UserSet set3 = createTestUserSet(USER_SET_BOOKMARK_FOLDER, regularUserToken);
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_META)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, PRIVATE_VISIBILITY)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken))
        .andExpect(status().is(HttpStatus.OK.value()));

    assertNotNull(set1);
    assertNotNull(set2);
    assertNotNull(set3);
  }

  @Test
  void searchWithPublishedVisibility() throws Exception {
    UserSet set = createTestUserSet(USER_SET_REGULAR, regularUserToken);
    // publish a user set
    mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + set.getIdentifier() + "/publish")
        .header(HttpHeaders.AUTHORIZATION, publisherUserToken)).andReturn().getResponse();

    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_META)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, PUBLISHED_VISIBILITY)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken))
            .andExpect(status().is(HttpStatus.OK.value()));
  }

  @Test
  void searchWithMultipleQfParams() throws Exception {
    deleteBookmarkFolder(regularUserToken);
    UserSet set1 = createTestUserSet(USER_SET_REGULAR, regularUserToken);
    UserSet set2 = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);
    // Update tests to delete sets before test and enable bookmark folder creation
    UserSet set3 = createTestUserSet(USER_SET_BOOKMARK_FOLDER, editorUserToken);
    String creatorRegular = (String) getAuthentication(regularUserToken).getPrincipal();
    String creatorEditor = (String) getAuthentication(editorUserToken).getPrincipal();
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS)
            .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, UserSetQueryBuilder.SEARCH_ALL)
            .queryParam(CommonApiConstants.QUERY_PARAM_QF, SEARCH_CREATOR + creatorRegular)
            .queryParam(CommonApiConstants.QUERY_PARAM_QF, SEARCH_CREATOR + creatorEditor)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse()
        .getContentAsString();
    /*
     * check ids (first 2 inside, 3. one no because it is private and the user cannot see
     * private sets of other users)
     */
    assertTrue(containsKeyOrValue(result, UserSetUtils
        .buildUserSetId(getConfiguration().getSetDataEndpoint(), set1.getIdentifier())));
    assertTrue(containsKeyOrValue(result, UserSetUtils
        .buildUserSetId(getConfiguration().getSetDataEndpoint(), set2.getIdentifier())));
    assertFalse(containsKeyOrValue(result, UserSetUtils
        .buildUserSetId(getConfiguration().getSetDataEndpoint(), set3.getIdentifier())));
    
    //search with added visibility qf param
    result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS)
            .header(HttpHeaders.AUTHORIZATION, editorUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, UserSetQueryBuilder.SEARCH_ALL)
            .queryParam(CommonApiConstants.QUERY_PARAM_QF, SEARCH_CREATOR + creatorRegular)
            .queryParam(CommonApiConstants.QUERY_PARAM_QF, SEARCH_CREATOR + creatorEditor)
            .queryParam(CommonApiConstants.QUERY_PARAM_QF, PRIVATE_VISIBILITY)
            .queryParam(CommonApiConstants.QUERY_PARAM_QF, PUBLIC_VISIBILITY)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse()
        .getContentAsString();

    /*
     * check ids (the 2. and 3. are inside, first not because it is a private set from another user and a 
     * user can only see his own private sets)
     */
    assertFalse(containsKeyOrValue(result, UserSetUtils
        .buildUserSetId(getConfiguration().getSetDataEndpoint(), set1.getIdentifier())));
    assertTrue(containsKeyOrValue(result, UserSetUtils
        .buildUserSetId(getConfiguration().getSetDataEndpoint(), set2.getIdentifier())));
    assertTrue(containsKeyOrValue(result, UserSetUtils
        .buildUserSetId(getConfiguration().getSetDataEndpoint(), set3.getIdentifier())));
  }

  @Test
  void searchItemsInSet_ItemsMeta() throws Exception {
    UserSet set1 = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);

    String setIdentifier = set1.getIdentifier();
    String[] qf = new String[] {"item:/08641/1037479000000476467",
        "item:/08641/1037479000000476875", "item:/11654/_Botany_U_1419207", "item:/2048128/618580",
        "item:/2048128/618580", "item:/2048128/notexisting", "item:/2048128/notexisting1"};

    // retrieve fist page of results
    String result =
        callSearchItemsInSet(setIdentifier, qf, String.valueOf(WebUserSetFields.DEFAULT_PAGE), "1",
            ProfileConstants.VALUE_PARAM_ITEMS_META, regularUserToken);
    // check ids
    String searchUri = "/set/" + setIdentifier + "/search";
    assertTrue(StringUtils.contains(result, searchUri));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.TOTAL));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.ResultPage));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.ResultList));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.FIRST));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.LAST));
    // first page no prev
    assertFalse(containsKeyOrValue(result, WebUserSetFields.PREV));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.NEXT));
    // check field from item descriptions
    assertTrue(containsKeyOrValue(result, "guid"));
  }

  @Test
  void searchItemsInSet() throws Exception {
    UserSet set1 = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);

    String setIdentifier = set1.getIdentifier();
    String[] qf = new String[] {"item:/08641/1037479000000476467",
        "item:/08641/1037479000000476875", "item:/11654/_Botany_U_1419207", "item:/2048128/618580",
        "item:/2048128/618580", "item:/2048128/notexisting", "item:/2048128/notexisting1"};
    final String secondPageIndex = String.valueOf(WebUserSetFields.DEFAULT_PAGE + 1);
    // using pagesize 2, we get two pages of results (only 4 items found in set)
    // retrieve last page
    String result = callSearchItemsInSet(setIdentifier, qf, secondPageIndex, "2", null, regularUserToken);
    // check ids
    String searchUri = "/set/" + setIdentifier + "/search";
    assertTrue(StringUtils.contains(result, searchUri));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.TOTAL));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.ResultPage));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.ResultList));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.FIRST));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.LAST));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.PREV));
    // last page no next
    assertTrue(!containsKeyOrValue(result, WebUserSetFields.NEXT));

    // retrieve fist page of results
    result = callSearchItemsInSet(setIdentifier, qf, String.valueOf(WebUserSetFields.DEFAULT_PAGE),
        "2", null, regularUserToken);
    // check ids
    assertTrue(StringUtils.contains(result, searchUri));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.TOTAL));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.ResultPage));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.ResultList));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.FIRST));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.LAST));
    // first page no prev
    assertTrue(!containsKeyOrValue(result, WebUserSetFields.PREV));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.NEXT));
  }
  
  @Test
  void searchItemsInSet_withPost() throws Exception {
    UserSet set1 = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);

    String setIdentifier = set1.getIdentifier();
    List<String> items = List.of("/08641/1037479000000476467",
        "/08641/1037479000000476875", "/11654/_Botany_U_1419207", "/2048128/618580",
        "/2048128/618580", "/2048128/notexisting", "/2048128/notexisting1");
    final int secondPageIndex = WebUserSetFields.DEFAULT_PAGE + 1;
    // using pagesize 2, we get two pages of results (only 4 items found in set)
    // retrieve last page
    
    String result = callSearchItemsInSetWithPost(setIdentifier, null, items, secondPageIndex, 2, null, regularUserToken, null);
    // check ids
    String searchUri = "/set/" + setIdentifier + "/search";
    assertTrue(StringUtils.contains(result, searchUri));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.TOTAL));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.ResultPage));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.ResultList));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.FIRST));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.LAST));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.PREV));
    // last page no next
    assertTrue(!containsKeyOrValue(result, WebUserSetFields.NEXT));

    // retrieve fist page of results
    result = callSearchItemsInSetWithPost(setIdentifier, "*", items, WebUserSetFields.DEFAULT_PAGE,
        2, SetPageProfile.ITEMS.getProfileParamValue(), regularUserToken, HttpStatus.OK);
    // check ids
    assertTrue(StringUtils.contains(result, searchUri));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.TOTAL));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.ResultPage));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.ResultList));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.FIRST));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.LAST));
    // first page no prev
    assertFalse(containsKeyOrValue(result, WebUserSetFields.PREV));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.NEXT));
  }
  
  @Test
  void searchItemsInSet_withPost_ItemsProfile() throws Exception {
    UserSet set1 = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);

    String setIdentifier = set1.getIdentifier();
    String inSetQuery = "{\"query\":\"*\",\"qf\":[\"item:/000000/\"],\"profile\":[\"items\"]}"; 
    String result = callSearchItemsInSetWithPost(setIdentifier, inSetQuery , regularUserToken, null);
    // check ids
    String searchUri = "/set/" + setIdentifier + "/search";
    assertTrue(StringUtils.contains(result, searchUri));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.TOTAL));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.ResultPage));
    //expect empty page
    assertFalse(containsKeyOrValue(result, CommonLdConstants.ResultList));
    assertFalse(containsKeyOrValue(result, WebUserSetFields.FIRST));
    assertFalse(containsKeyOrValue(result, WebUserSetFields.LAST));
    assertFalse(containsKeyOrValue(result, WebUserSetFields.PREV));
    // last page no next
    assertFalse(containsKeyOrValue(result, WebUserSetFields.NEXT));
  }
  
  @Test
  void searchItemsInSetPrivate() throws Exception {
    UserSet set1 = createTestUserSet(USER_SET_REGULAR, regularUserToken);

    String setIdentifier = set1.getIdentifier();
    String[] qf = new String[] {"item:/08641/1037479000000476467",
        "item:/08641/1037479000000476875", "item:/11654/_Botany_U_1419207", "item:/2048128/618580",
        "item:/2048128/618580", "item:/2048128/notexisting", "item:/2048128/notexisting1"};
    String result = callSearchItemsInSet(setIdentifier, qf, null, null, null, regularUserToken);
    // check ids
    String searchUri = "/set/" + setIdentifier + "/search";
    assertTrue(StringUtils.contains(result, searchUri));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.TOTAL));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.ResultPage));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.ResultList));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.FIRST));
    assertTrue(containsKeyOrValue(result, WebUserSetFields.LAST));
    // first page no next
    assertTrue(!containsKeyOrValue(result, WebUserSetFields.PREV));
    // last page no next
    assertTrue(!containsKeyOrValue(result, WebUserSetFields.NEXT));
  }

  @Test
  void searchItemsInSet_empty_response() throws Exception {
    UserSet set1 = createTestUserSet(USER_SET_REGULAR, regularUserToken);

    String setIdentifier = set1.getIdentifier();
    String result = callSearchItemsInSet(setIdentifier, new String[] {"item:/nonexisting/item"},
        null, null, null, regularUserToken);
    // check ids
    String searchUri = "/set/" + setIdentifier + "/search";
    assertTrue(StringUtils.contains(result, searchUri));
    // total should be 0
    assertTrue(containsKeyOrValue(result, WebUserSetFields.TOTAL));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.ResultPage));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.id));
  }

  @Test
  void searchItemsInSet_No_QF_Param() throws Exception {
    UserSet set1 = createTestUserSet(USER_SET_REGULAR, regularUserToken);
    String setIdentifier = set1.getIdentifier();

    String result = callSearchItemsInSet(setIdentifier, null, null, null, null, regularUserToken);
    // check ids
    String searchUri = "/set/" + setIdentifier + "/search";
    assertTrue(StringUtils.contains(result, searchUri));
    // total should be 0
    assertTrue(containsKeyOrValue(result, WebUserSetFields.TOTAL));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.ResultPage));
    assertTrue(containsKeyOrValue(result, CommonLdConstants.id));
  }

  @Test
  void searchGalleries() throws Exception {
    UserSet set1 = createTestUserSet(USER_SET_GALLERY, regularUserToken);
    assertNotNull(set1);
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_GALLERY)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .header(HttpHeaders.AUTHORIZATION, regularUserToken))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse()
        .getContentAsString();

    assertTrue(containsKeyOrValue(result, UserSetUtils
        .buildUserSetId(getConfiguration().getSetDataEndpoint(), set1.getIdentifier())));
    // only id retured in the minimal profile, to be enabled after refactoring profiles
    // assertTrue(containsKeyOrValue(result, WebUserSetFields.TYPE_GALLERY));
  }

  @Test
  void searchTypeCollection() throws Exception {
    UserSet set1 = createTestUserSet(USER_SET_REGULAR, regularUserToken);
    UserSet set2 = createTestUserSet(USER_SET_MANDATORY, regularUserToken);
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_META)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_COLLECTION)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.OK.value()));

    assertNotNull(set1);
    assertNotNull(set2);
  }


  // Multiple profile validation
  @Test
  void searchFacetsMultipleProfileInvalid() throws Exception {
    String profile = ProfileConstants.VALUE_PARAM_FACETS + "," + "test";
    mockMvc
        .perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, profile)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "visibility"))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }


  @Test
  void searchFacetsMultipleValidProfileWithFacets() throws Exception {
    createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);
    String profile = ProfileConstants.VALUE_PARAM_META + "," + ProfileConstants.VALUE_PARAM_FACETS;
    mockMvc
        .perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, profile)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "visibility"))
        .andExpect(status().is(HttpStatus.OK.value()));
  }

  @Test
  void searchFacetsMultipleValidProfileWithFacets_Debug() throws Exception {
    String profile = ProfileConstants.VALUE_PARAM_DEBUG + "," + ProfileConstants.VALUE_PARAM_FACETS;
    mockMvc
        .perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, profile)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "visibility"))
        .andExpect(status().is(HttpStatus.OK.value()));
  }

  @Test
  void searchFacetsMultipleValidProfileWithFacetsDebug() throws Exception {
    // profiles can also be space separated
    String profile = ProfileConstants.VALUE_PARAM_DEBUG + " " + ProfileConstants.VALUE_PARAM_FACETS
        + "  " + ProfileConstants.VALUE_PARAM_META;
    mockMvc
        .perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, profile)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "visibility"))
        .andExpect(status().is(HttpStatus.OK.value()));
  }

  @Test
  void searchFacetsValidFacetTest() throws Exception {
    // delete the bookmarkFolder already if exists
    deleteBookmarkFolder(regularUserToken);
    deleteBookmarkFolder(editorUserToken);

    UserSet set1 = createTestUserSet(USER_SET_BOOKMARK_FOLDER, regularUserToken);
    UserSet set2 = createTestUserSet(USER_SET_BOOKMARK_FOLDER_1, editorUserToken);

    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_FACETS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
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
  void searchFacet0PageSizeTest() throws Exception {
    // delete the bookmarkFolder already if exists
    deleteBookmarkFolder(regularUserToken);
    deleteBookmarkFolder(editorUserToken);

    UserSet set1 = createTestUserSet(USER_SET_BOOKMARK_FOLDER, regularUserToken);
    UserSet set2 = createTestUserSet(USER_SET_BOOKMARK_FOLDER_1, editorUserToken);

    // /set/search?pageSize=0&query=*&profile=facets&wskey=&facet=item
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_FACETS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
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

}