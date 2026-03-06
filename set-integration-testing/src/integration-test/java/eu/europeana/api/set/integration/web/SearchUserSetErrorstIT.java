package eu.europeana.api.set.integration.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

  private static final String ERROR_MSG_QF_MANDATORY = "The mandatory parameter qf was not found in the request";

  @Test
  void searchEmptyApiKey() throws Exception {
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_META)
            .queryParam(OAuthUtils.PARAM_WSKEY, "")
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value())).andReturn().getResponse()
        .getContentAsString();
    // check error response
    verifyCommonErrorFields(result);
    
  }

  @Test
  void searchInvalidApiKey() throws Exception {

    if (getConfiguration().isApiKeyValidationEnabled()) {
      String result = mockMvc
          .perform(get(SEARCH_URL)
              .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_META)
              .queryParam(OAuthUtils.PARAM_WSKEY, "invalid_api_key")
              .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "")
              .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
          .andExpect(status().is(HttpStatus.UNAUTHORIZED.value())).andReturn().getResponse()
          .getContentAsString();
      // check error response
      verifyCommonErrorFields(result);
    } else {
      System.out.println("skipped apikey validation test!");
    }

  }

  @Test
  void searchWithoutApiKey() throws Exception {
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_META)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value())).andReturn().getResponse()
        .getContentAsString();
    // check error response
    verifyCommonErrorFields(result);
  }


  @Test
  void searchWithInvalidSetId() throws Exception {
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_META)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_INVALID_SET_ID)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();
    // check error response
    verifyCommonErrorFields(result);
  }


  @Test
  void searchSetByEmptyTextQuery() throws Exception {
    // subject in json file: http://data.europeana.eu/concept/base/114
    String query = ":";
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();
    // check error response
    verifyCommonErrorFields(result);
    assertTrue(result.contains("The parameter  sent in the request is invalid, expected : valid field name in search query, found : "));
    
  }

  @Test
  void searchSetByNotSupportedField_Title() throws Exception {
    // subject in json file: http://data.europeana.eu/concept/base/114
    String query = "title:test";
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();
    
    // check error response
    verifyCommonErrorFields(result);
    assertTrue(result.contains("expected : valid field name in search query, found : title"));
  }

  @Test
  void searchSetByTextQuery_WrongQuery() throws Exception {
    String query = ":sportswear golf";
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();
    // check error response
    verifyCommonErrorFields(result);
    assertTrue(result.contains("The parameter  sent in the request is invalid, expected : valid field name in search query, found : "));
  }
  
  @Test
  void searchSetWithWrongSort_Score() throws Exception {
    // subject in json file: http://data.europeana.eu/concept/base/114
    String query = "visibility:public";
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_SORT, WebUserSetFields.TEXT_SCORE_SORT))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();
    // check error response
    verifyCommonErrorFields(result);
    assertTrue(result.contains("The parameter sort sent in the request is invalid, expected : it cannot contain 'score' if the search is not on the text field, found : score"));
  }


  @Test
  void searchSetWithWrongSort_ScoreAsc() throws Exception {
    // create object in database
    UserSet set = createTestUserSet(USER_SET_REGULAR_PUBLIC, editorUserToken);
    // subject in json file: http://data.europeana.eu/concept/base/114
    final String title = set.getTitle().get("en");
    String query = title;
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE).queryParam(
                CommonApiConstants.QUERY_PARAM_SORT, WebUserSetFields.TEXT_SCORE_SORT + " asc"))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();
    // check error response
    verifyCommonErrorFields(result);
    assertTrue(result.contains("The parameter sort sent in the request is invalid, expected : it cannot contain 'score asc' since only the descending order is supported, found : score asc"));
  }

  @Test
  void searchSetByTextQueryAndVisibility_WrongFormat() throws Exception {
    String query = "sportswear golf visibility:public";
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();
    // check error response
    verifyCommonErrorFields(result);
    assertTrue(result.contains("The parameter sportswear golf visibility sent in the request is invalid, expected : valid field name in search query, found : sportswear golf visibility"));
  }

  @Test
  void searchSetByTextWithMultipleCriteria_WrongFormat() throws Exception {
    // query parsing for combination like "visibility:sportswear golf"; is invalid
    String query = "visibility::sportswear golf";
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_ITEMS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();
    // check error response
    verifyCommonErrorFields(result);
    assertTrue(result.contains("The parameter visibility sent in the request is invalid, expected : valid formatting of search query for field 'visibility', found : "));
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
    // check error response
    verifyCommonErrorFields(result);
    assertFalse(containsKeyOrValue(result, WebUserSetFields.NEXT)); 
  }

  @Test
  void searchItemsInSet_withPost_noBody() throws Exception {
    UserSet set1 = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);
    String setIdentifier = set1.getIdentifier();
    
    String result = callSearchItemsInSetWithPost(setIdentifier, null, regularUserToken, HttpStatus.BAD_REQUEST);
    // check error response
    verifyCommonErrorFields(result);
    assertTrue(result.contains("Required request body is missing"));
  }
  
  @Test
  void searchItemsInSet_withPost_emptyBody() throws Exception {
    UserSet set1 = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);
    String setIdentifier = set1.getIdentifier();
    
    String result = callSearchItemsInSetWithPost(setIdentifier, "{}", regularUserToken, HttpStatus.BAD_REQUEST);
    // check error response
    verifyCommonErrorFields(result);
    assertTrue(result.contains(ERROR_MSG_QF_MANDATORY));    
  }
  
  @Test
  void searchItemsInSet_withPost_noQf() throws Exception {
    UserSet set1 = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);
    String setIdentifier = set1.getIdentifier();
    
    String inSetQuery = "{\"query\":\"*\"}";
    String result = callSearchItemsInSetWithPost(setIdentifier, inSetQuery, regularUserToken, HttpStatus.BAD_REQUEST);
    // check error response
    verifyCommonErrorFields(result);
    assertTrue(result.contains(ERROR_MSG_QF_MANDATORY));
    
  }
  
  @Test
  void searchItemsInSet_withPost_noItemPrefix() throws Exception {
    UserSet set1 = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);

    String setIdentifier = set1.getIdentifier();
    String inSetQuery = "{\"query\":\"*\",\"qf\":[\"/000000/\"],\"profile\":[\"items\"]}"; 
    
    String result = callSearchItemsInSetWithPost(setIdentifier, inSetQuery, regularUserToken, HttpStatus.BAD_REQUEST);
    // check error response
    verifyCommonErrorFields(result);
    // check error message
    assertTrue(result.contains("The parameter qf sent in the request is invalid, expected : entries with format 'item:<identifier>', found : /000000/")); 
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

    // check error response
    verifyCommonErrorFields(result);
    assertTrue(result.contains(wrongCollectionType));
  }

  // Facet validation
  @Test
  void searchFacetsNoFacetValidationTest() throws Exception {
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_FACETS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();
    // check error response
    verifyCommonErrorFields(result);
  }

  @Test
  void searchFacetsInvalidFacetValidationTest() throws Exception {
    String result = mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_FACETS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "test"))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();
    // check error response
    verifyCommonErrorFields(result);
  }

  @Test
  void searchFacetsEmptyFacetValidationTest() throws Exception {
    String result =  mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_FACETS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_FACET, ""))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();
    // check error response
    verifyCommonErrorFields(result);
  }

  @Test
  void searchFacetsMultipleFacetValidationTest() throws Exception {
    String result =  mockMvc
        .perform(get(SEARCH_URL)
            .param(CommonApiConstants.QUERY_PARAM_PROFILE, ProfileConstants.VALUE_PARAM_FACETS)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "item,visibility"))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();
    // check error response
    verifyCommonErrorFields(result);
  }

  // Multiple profile validation
  @Test
  void searchFacetsMultipleProfileInvalid() throws Exception {
    String profile = ProfileConstants.VALUE_PARAM_FACETS + "," + "test";
    String result = mockMvc
        .perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, profile)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "visibility"))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();
    // check error response
    verifyCommonErrorFields(result);
  }

  @Test
  void searchFacetsMultipleProfileWithoutFacets() throws Exception {
    String profile = ProfileConstants.VALUE_PARAM_META + "," + ProfileConstants.VALUE_PARAM_ITEMS;
    String result = mockMvc
        .perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, profile)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "visibility"))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();
    // check error response
    verifyCommonErrorFields(result);
  }

  @Test
  void searchFacetsMultipleInvalidProfileWithFacets() throws Exception {
    String profile = ProfileConstants.VALUE_PARAM_MINIMAL + ","
        + ProfileConstants.VALUE_PARAM_FACETS + "," + ProfileConstants.VALUE_PARAM_STANDARD;
    String result = mockMvc
        .perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, profile)
                .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "*")
            .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE)
            .queryParam(CommonApiConstants.QUERY_PARAM_FACET, "visibility"))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();
    // check error response
    verifyCommonErrorFields(result);
  }

}