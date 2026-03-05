package eu.europeana.api.set.integration.web;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonLdConstants;
import eu.europeana.api.commons_sb3.oauth2.utils.OAuthUtils;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.vocabulary.ProfileConstants;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;

@SpringBootTest
class SearchUserSetErrorstIT extends BaseSearchUserSetTesting{

  @Test
  void searchEmptyApiKey() throws Exception {
    // UserSet set = createTestUserSet(USER_SET_BOOKMARK_FOLDER, regularUserToken);
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_META)
            .queryParam(OAuthUtils.PARAM_WSKEY, "")
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
  }

  @Test
  void searchInvalidApiKey() throws Exception {

    if (getConfiguration().isApiKeyValidationEnabled()) {
      mockMvc
          .perform(get(SEARCH_URL)
              .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_META)
              .queryParam(OAuthUtils.PARAM_WSKEY, "invalid_api_key")
              .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "")
              .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
          .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    } else {
      System.out.println("skipped apikey validation test!");
    }

  }

  @Test
  void searchWithoutApiKey() throws Exception {
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_META)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
  }


  @Test
  void searchWithInvalidSetId() throws Exception {
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_META)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_INVALID_SET_ID)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }


  @Test
  void searchSetByEmptyTextQuery() throws Exception {
    // subject in json file: http://data.europeana.eu/concept/base/114
    String query = ":";
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  //TODO: why error?
  void searchSetByTitleQuery() throws Exception {
    // subject in json file: http://data.europeana.eu/concept/base/114
    String query = "title:test";
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  void searchSetByTextQuery_WrongQuery() throws Exception {
    String query = ":sportswear golf";
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();
  }
  
  @Test
  //TODO: why error?
  void searchSetWithoutTextQueryWithScoreSort() throws Exception {
    // subject in json file: http://data.europeana.eu/concept/base/114
    String query = "visibility:public";
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_SORT, WebUserSetFields.TEXT_SCORE_SORT))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }
  


  @Test
//TODO: why error?
  void searchWithScoreSortInAscOrder() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_REGULAR_PUBLIC, editorUserToken);
    // subject in json file: http://data.europeana.eu/concept/base/114
    final String title = set.getTitle().get("en");
    // String query = "sportswear golf";
    String query = title;
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE).queryParam(
                CommonApiConstants.QUERY_PARAM_SORT, WebUserSetFields.TEXT_SCORE_SORT + " asc"))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
//TODO: why error?
  void searchSetByTextQueryWithMultipleCriteria1() throws Exception {
    String query = "sportswear golf visibility:public";
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();
  }

  @Test
//TODO: why error?
  void searchSetByTextWithMultipleCriteria2() throws Exception {
    // query parsing for combination like "visibility:sportswear golf"; is invalid
    String query = "visibility::sportswear golf";
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  void searchItemsInSet_withPost_wrongQuery() throws Exception {
    UserSet set1 = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);

    String setIdentifier = set1.getIdentifier();
    List<String> items = List.of("/08641/1037479000000476467",
        "/08641/1037479000000476875", "/11654/_Botany_U_1419207", "/2048128/618580",
        "/2048128/618580", "/2048128/notexisting", "/2048128/notexisting1");
    final int secondPageIndex = WebUserSetFields.DEFAULT_PAGE;
    // using pagesize 2, we get two pages of results (only 4 items found in set)
    // retrieve last page
    
    String result = callSearchItemsInSetWithPost(setIdentifier, "query:wrong", items, secondPageIndex, 2, null, regularUserToken, HttpStatus.BAD_REQUEST);
    // check error message
    // last page no next
    assertTrue(!containsKeyOrValue(result, WebUserSetFields.NEXT)); 
  }

  @Test
  void searchItemsInSet_withPost_noBody() throws Exception {
    UserSet set1 = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);

    String setIdentifier = set1.getIdentifier();
    
    String result = callSearchItemsInSetWithPost(setIdentifier, null, regularUserToken, HttpStatus.BAD_REQUEST);
    // check error response
    assertTrue(containsKeyOrValue(result, "ErrorResponse"));
    assertTrue(result.contains("Required request body is missing"));
  }
  
  @Test
  void searchItemsInSet_withPost_emptyBody() throws Exception {
    UserSet set1 = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);

    String setIdentifier = set1.getIdentifier();
    
    String result = callSearchItemsInSetWithPost(setIdentifier, "{}", regularUserToken, HttpStatus.BAD_REQUEST);
    // check error message
    assertTrue(containsKeyOrValue(result, "ErrorResponse"));
    assertTrue(result.contains("The mandatory parameter qf was not found in the request"));    
  }
  
  @Test
  void searchItemsInSet_withPost_noQf() throws Exception {
    UserSet set1 = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);

    String setIdentifier = set1.getIdentifier();
    
    String result = callSearchItemsInSetWithPost(setIdentifier, "{\"query\":\"'*\"}", regularUserToken, HttpStatus.BAD_REQUEST);
    // check error message
    // last page no next
    assertTrue(!containsKeyOrValue(result, WebUserSetFields.NEXT)); 
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
  void searchByWrongCollectionType() throws Exception {
    final String wrongCollectionType = "wrongCollectionType";
    final String query = WebUserSetFields.COLLECTION_TYPE + ":" + wrongCollectionType;
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_META)
                .header(HttpHeaders.AUTHORIZATION,  regularUserToken)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();

    assertTrue(result.contains(wrongCollectionType));
  }

  // Facet validation
  @Test
  void searchFacetsNoFacetValidationTest() throws Exception {
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_FACETS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  void searchFacetsInvalidFacetValidationTest() throws Exception {
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_FACETS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "test"))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  void searchFacetsEmptyFacetValidationTest() throws Exception {
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_FACETS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_FACET, ""))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  void searchFacetsMultipleFacetValidationTest() throws Exception {
    mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_FACETS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "item,visibility"))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
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
  void searchFacetsMultipleProfileWithoutFacets() throws Exception {
    String profile = ProfileConstants.VALUE_PARAM_META + "," + ProfileConstants.VALUE_PARAM_ITEMS;
    mockMvc
        .perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, profile)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "visibility"))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  void searchFacetsMultipleInvalidProfileWithFacets() throws Exception {
    String profile = ProfileConstants.VALUE_PARAM_MINIMAL + ","
        + ProfileConstants.VALUE_PARAM_FACETS + "," + ProfileConstants.VALUE_PARAM_STANDARD;
    mockMvc
        .perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, profile)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "visibility"))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

}