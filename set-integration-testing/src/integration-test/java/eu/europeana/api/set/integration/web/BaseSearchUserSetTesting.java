package eu.europeana.api.set.integration.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.set.integration.IntegrationTestSetup;
import eu.europeana.api.set.integration.connection.http.EuropeanaOauthClient;
import eu.europeana.api.set.integration.exception.SetIntegrationException;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.web.model.SearchInSetQuery;
import eu.europeana.set.web.model.search.FacetValue;
import eu.europeana.set.web.search.UserSetLdSerializer;
import eu.europeana.set.web.search.UserSetQueryBuilder;

public class BaseSearchUserSetTesting  extends IntegrationTestSetup{

  static final String SEARCH_URL = "/set/search";
  static final String SEARCH_SET_ID = WebUserSetFields.SET_ID + ":";
  static final String SEARCH_INVALID_SET_ID = WebUserSetFields.SET_ID + ":" + "test";
  static final String PUBLIC_VISIBILITY =
      WebUserSetFields.VISIBILITY + ":" + VisibilityTypes.PUBLIC.getJsonValue();
  static final String PRIVATE_VISIBILITY =
      WebUserSetFields.VISIBILITY + ":" + VisibilityTypes.PRIVATE.getJsonValue();
  static final String PUBLISHED_VISIBILITY =
      WebUserSetFields.VISIBILITY + ":" + VisibilityTypes.PUBLISHED.getJsonValue();
  static final String SEARCH_CREATOR = WebUserSetFields.CREATOR + ":";
  static final String SEARCH_COLLECTION =
      WebUserSetFields.TYPE + ":" + UserSetTypes.COLLECTION;
  static final String SEARCH_ENTITY_SET =
      WebUserSetFields.TYPE + ":" + UserSetTypes.ENTITYBESTITEMSSET;
  static final String SEARCH_GALLERY =
      WebUserSetFields.COLLECTION_TYPE + ":" + WebUserSetFields.TYPE_GALLERY;
  static final String SEARCH_TITLE_LANG_EN = WebUserSetFields.LANG + ":" + "en";
  // private static final String SORT_MODIFIED_WebUserSetFields.MODIFIED
  static final String PAGE_SIZE = "100";
  
  @BeforeAll
  static void initTokens() throws SetIntegrationException {
    if (DISABLE_AUTH) {
      return;
    }
    initRegularUserToken();
    editorUserToken = retrieveOauthToken(EuropeanaOauthClient.EDITOR_USER);
    initPublisherUserToken();
  }

  @AfterEach
  protected void deleteCreatedSets() {
    super.deleteCreatedSets();
  }
  
  protected String callSearchItemsInSetWithPost(String setIdentifier, String query, List<String> items, int page,
      int pageSize, String profile, String regularUserToken, HttpStatus expectedStatus) throws Exception {
      
        List<String> profiles = (profile == null)? null: List.of(profile);
        String body = buildSearchInSetQuery(query, items, page, pageSize, profiles);
        
        return callSearchItemsInSetWithPost(setIdentifier, body, regularUserToken, expectedStatus);
      
      }

  String callSearchItemsInSetWithPost(String setIdentifier, String body, String regularUserToken, HttpStatus expectedStatus)
      throws EuropeanaApiException, UnsupportedEncodingException, Exception {
        MockHttpServletRequestBuilder searchRequest =
            buildSearchItemsInSetWithPostRequest(setIdentifier, body, regularUserToken);
            
        if(expectedStatus == null){
          expectedStatus =  HttpStatus.OK;
        }
        
        return mockMvc.perform(searchRequest).andExpect(status().is(expectedStatus.value())).andReturn()
            .getResponse().getContentAsString();
      }

  String callSearchItemsInSet(String setIdentifier, String[] qf, String page,
      String pageSize, String profile, String regularUserToken)
      throws Exception {

    MockHttpServletRequestBuilder searchRequest =
        buildSearchItemsInSetRequest(setIdentifier, qf, page, pageSize, profile, regularUserToken);

    return mockMvc.perform(searchRequest).andExpect(status().is(HttpStatus.OK.value())).andReturn()
        .getResponse().getContentAsString();

  }
  
  MockHttpServletRequestBuilder buildSearchItemsInSetWithPostRequest(String setIdentifier, String body, String regularUserToken)
      throws EuropeanaApiException {
        
        MockHttpServletRequestBuilder request = post("/set/" + setIdentifier + "/search");
        addAuthorizationHeader(request, regularUserToken);
        request.content( body );
        request.contentType(MediaType.APPLICATION_JSON);
        return request;
      }

  String buildSearchInSetQuery(String searchQuery, List<String> items, int page, int pageSize, List<String> profile)
      throws EuropeanaApiException {
        //add item:
        String[] filters = (String[]) items.stream().map(item ->  (WebUserSetFields.ITEM +":"+ item)).toArray(String[]::new);
        
        SearchInSetQuery query = new SearchInSetQuery(filters, page, pageSize, profile);
        query.setQuery(searchQuery);
        
        String body = (new UserSetLdSerializer()).serializeNonLd(query);
        return body;
      }

  protected MockHttpServletRequestBuilder buildSearchItemsInSetRequest(String setIdentifier, String[] qf, String page, String pageSize,
      String profile, String regularUserToken) {
        
        MockHttpServletRequestBuilder request = get("/set/" + setIdentifier + "/search");
        
        MockHttpServletRequestBuilder requestBuilder =
            addCommonRequestParams(request, page, pageSize, profile, regularUserToken);
      
        // add qf param
        if (qf != null) {
          for (int i = 0; i < qf.length; i++) {
            requestBuilder.queryParam(CommonApiConstants.QUERY_PARAM_QF, qf[i]);
          }
        }
        return request;
      }

  MockHttpServletRequestBuilder addCommonRequestParams(MockHttpServletRequestBuilder request, String page, String pageSize, String profile, String regularUserToken) {
    if (profile != null) {
      request.param(CommonApiConstants.QUERY_PARAM_PROFILE, profile);
    }
    addAuthorizationHeader(request, regularUserToken);
  
    if (page != null) {
      request.queryParam(CommonApiConstants.QUERY_PARAM_PAGE, page);
    }
    if (pageSize != null) {
      request.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, pageSize);
    }
  
    // apikey will be ignored
    return
        request.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, UserSetQueryBuilder.SEARCH_ALL);
  }

  void addAuthorizationHeader(MockHttpServletRequestBuilder request, String regularUserToken) {
    if (regularUserToken != null) {
      request.header(HttpHeaders.AUTHORIZATION, regularUserToken);
    }
  }

  protected void checkItemFacets(List<FacetValue> facetValueResultPages) {
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
