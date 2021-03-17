package eu.europeana.api.set.integration.web;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.definitions.vocabulary.CommonLdConstants;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.web.search.UserSetQueryBuilder;
import eu.europeana.set.web.service.controller.jsonld.SearchUserSetRest;

@WebMvcTest(SearchUserSetRest.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration()
@ContextConfiguration(locations = { "classpath:set-web-mvc.xml" })
public class SearchUserSetRestTest extends BaseUserSetTestUtils {

    private static final String API_KEY = "api2demo";
    private static final String SEARCH_URL = "/set/search";
    private static final String SEARCH_SET_ID = WebUserSetFields.SET_ID + ":";
    private static final String SEARCH_INVALID_SET_ID = WebUserSetFields.SET_ID + ":" + "test";
    private static final String PUBLIC_VISIBILITY = WebUserSetFields.VISIBILITY + ":"
	    + VisibilityTypes.PUBLIC.getJsonValue();
    private static final String PRIVATE_VISIBILITY = WebUserSetFields.VISIBILITY + ":"
	    + VisibilityTypes.PRIVATE.getJsonValue();
    private static final String PUBLISHED_VISIBILITY = WebUserSetFields.VISIBILITY + ":"
	    + VisibilityTypes.PUBLISHED.getJsonValue();
    private static final String SEARCH_CREATOR = WebUserSetFields.CREATOR + ":";
    private static final String SEARCH_COLLECTION = WebUserSetFields.TYPE + ":" + UserSetTypes.COLLECTION;
    private static final String SEARCH_ENTITY_SET = WebUserSetFields.TYPE + ":" + UserSetTypes.ENTITYBESTITEMSSET;

//    private static final String SORT_MODIFIED_WebUserSetFields.MODIFIED
    
    private static final String PAGE_SIZE = "100";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @BeforeEach
    public void initApplication() {
	this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void searchEmptyApiKey() throws Exception {
//        UserSet set = createTestUserSet(USER_SET_BOOKMARK_FOLDER, regularUserToken);
	mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.queryParam(CommonApiConstants.PARAM_WSKEY, "").queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "")
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void searchInvalidApiKey() throws Exception {
	mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.queryParam(CommonApiConstants.PARAM_WSKEY, "invalid_api_key")
		.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "")
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void searchWithoutApiKey() throws Exception {
	mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "")
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void searchWithValidSetId() throws Exception {
	UserSet set = createTestUserSet(USER_SET_REGULAR, regularUserToken);
	mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
		.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_SET_ID + set.getIdentifier())
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.OK.value()));
	// delete item created by test
	getUserSetService().deleteUserSet(set.getIdentifier());

    }

    @Test
    public void searchWithInvalidSetId() throws Exception {
	mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
		.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_INVALID_SET_ID)
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void searchWithPublicVisibility() throws Exception {
	// create object in database
	UserSet set = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);

	mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
		.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, PUBLIC_VISIBILITY)
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.OK.value()));

	// delete item created by test
	getUserSetService().deleteUserSet(set.getIdentifier());
    }

    @Test
    public void searchEntitySet() throws Exception {
	// create object in database
	UserSet set = createTestUserSet(USER_SET_BEST_ITEMS, editorUserToken);
	String query = SEARCH_ENTITY_SET;
	String result = mockMvc
		.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
			.queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
			.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
			.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse().getContentAsString();

	assertNotNull(result);
	// check id
	//default sorting should include the id on the first position
	final String buildUserSetId = UserSetUtils.buildUserSetId(set.getIdentifier());
	assertTrue(containsKeyOrValue(result, buildUserSetId));

	// delete item created by test
	getUserSetService().deleteUserSet(set.getIdentifier());
    }

    @Test
    public void searchEntitySetByContributor() throws Exception {
	// create object in database
	UserSet set = createTestUserSet(USER_SET_BEST_ITEMS, editorUserToken);
	String contributor =  (String) getAuthentication(editorUserToken).getPrincipal();
	String query = "contributor:"+ contributor;
	String result = mockMvc
		.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
			.queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
			.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
			.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse().getContentAsString();

	assertNotNull(result);
	// check id
	//default sorting should include the id on the first position
	final String userSetId = UserSetUtils.buildUserSetId(set.getIdentifier());
	assertTrue(containsKeyOrValue(result, userSetId));

	// delete item created by test
	getUserSetService().deleteUserSet(set.getIdentifier());
    }
    
    @Test
    public void searchEntitySetByContributorUri() throws Exception {
	// create object in database
	UserSet set = createTestUserSet(USER_SET_BEST_ITEMS, editorUserToken);
	String contributor =  (String) getAuthentication(editorUserToken).getPrincipal();
	final String contributorId = UserSetUtils.buildUserUri(contributor);
	String query = "contributor:"+ contributorId;
	String result = mockMvc
		.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
			.queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
			.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
			.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse().getContentAsString();

	assertNotNull(result);
	// check id
	//default sorting should include the id on the first position
	final String userSetId = UserSetUtils.buildUserSetId(set.getIdentifier());
	assertTrue(containsKeyOrValue(result, userSetId));
	
	//check contributor
	assertTrue(containsKeyOrValue(result, contributorId));

	// delete item created by test
	getUserSetService().deleteUserSet(set.getIdentifier());
    }
    
    @Test
    public void searchEntitySetBySubject() throws Exception {
	// create object in database
	UserSet set = createTestUserSet(USER_SET_BEST_ITEMS, editorUserToken);
//	String contributor =  (String) getAuthentication(editorUserToken).getPrincipal();
	//subject in json file: http://data.europeana.eu/concept/base/114
	final String subject = set.getSubject().get(0);
	String query = "subject:"+ subject;
	String result = mockMvc
		.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
			.queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
			.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, query)
			.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse().getContentAsString();

	assertNotNull(result);
	// check id
	//default sorting should include the id on the first position
	assertTrue(containsKeyOrValue(result, UserSetUtils.buildUserSetId(set.getIdentifier())));

	//check subject
	assertTrue(containsKeyOrValue(result, subject));

	// delete item created by test
	getUserSetService().deleteUserSet(set.getIdentifier());
    }
    
    @Test
    public void searchWithPublicVisibility_ItemsDescription() throws Exception {
	// create object in database
	UserSet set = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);

	mockMvc.perform(
		get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.ITEMDESCRIPTIONS.name())
			.queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
			.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, PUBLIC_VISIBILITY)
			.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.OK.value()));

	// delete item created by test
	getUserSetService().deleteUserSet(set.getIdentifier());
    }

    @Test
    public void searchWithPrivateVisibility() throws Exception {
	deleteBookmarkFolder(regularUserToken);
	UserSet set1 = createTestUserSet(USER_SET_MANDATORY, regularUserToken);
	UserSet set2 = createTestUserSet(USER_SET_REGULAR, regularUserToken);
	// Update tests to delete sets before test and enable bookmark folder creation
	UserSet set3 = createTestUserSet(USER_SET_BOOKMARK_FOLDER, regularUserToken);
	mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
		.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, PRIVATE_VISIBILITY)
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.OK.value()));

	// delete item created by test
	getUserSetService().deleteUserSet(set1.getIdentifier());
	getUserSetService().deleteUserSet(set2.getIdentifier());
	getUserSetService().deleteUserSet(set3.getIdentifier());
    }

    @Test
    public void searchWithPublishedVisibility() throws Exception {
	UserSet set = createTestUserSet(USER_SET_REGULAR_PUBLISHED, regularUserToken);
	mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
		.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, PUBLISHED_VISIBILITY)
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.OK.value()));

	// delete item created by test
	getUserSetService().deleteUserSet(set.getIdentifier());
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
		.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
			.header(HttpHeaders.AUTHORIZATION, regularUserToken)
//		apikey will be ignored
			.queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
			.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_CREATOR + creator)
			.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse().getContentAsString();
	// check ids
	assertTrue(containsKeyOrValue(result, UserSetUtils.buildUserSetId(set1.getIdentifier())));
	assertTrue(containsKeyOrValue(result, UserSetUtils.buildUserSetId(set2.getIdentifier())));
	assertTrue(containsKeyOrValue(result, UserSetUtils.buildUserSetId(set3.getIdentifier())));

	// delete item created by test
	getUserSetService().deleteUserSet(set1.getIdentifier());
	getUserSetService().deleteUserSet(set2.getIdentifier());
	getUserSetService().deleteUserSet(set3.getIdentifier());
    }

    @Test
    public void searchItemsInSet() throws Exception {
	UserSet set1 = createTestUserSet(USER_SET_REGULAR_PUBLIC, regularUserToken);

	String setIdentifier = set1.getIdentifier();
	String[] qf = new String[] { "item:/08641/1037479000000476467", "item:/08641/1037479000000476875",
		"item:/11654/_Botany_U_1419207", "item:/2048128/618580", "item:/2048128/618580",
		"item:/2048128/notexisting", "item:/2048128/notexisting1" };
	String result = callSearchItemsInSet(setIdentifier, qf, "1", "2", null);
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

	result = callSearchItemsInSet(setIdentifier, qf, "0", "2", null);
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

	// delete item created by test
	getUserSetService().deleteUserSet(setIdentifier);
    }

    @Test
    public void searchItemsInSetPrivate() throws Exception {
	UserSet set1 = createTestUserSet(USER_SET_REGULAR, regularUserToken);

	String setIdentifier = set1.getIdentifier();
	String[] qf = new String[] { "item:/08641/1037479000000476467", "item:/08641/1037479000000476875",
		"item:/11654/_Botany_U_1419207", "item:/2048128/618580", "item:/2048128/618580",
		"item:/2048128/notexisting", "item:/2048128/notexisting1" };
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
	getUserSetService().deleteUserSet(setIdentifier);
    }

    @Test
    public void searchItemsInSet_empty_response() throws Exception {
	UserSet set1 = createTestUserSet(USER_SET_REGULAR, regularUserToken);

	String setIdentifier = set1.getIdentifier();
	String result = callSearchItemsInSet(setIdentifier, new String[] { "item:/nonexisting/item" }, null, null,
		regularUserToken);
	// check ids
	String searchUri = "/set/" + setIdentifier + "/search";
	assertTrue(StringUtils.contains(result, searchUri));
	// total should be 0
	assertTrue(containsKeyOrValue(result, WebUserSetFields.TOTAL));
	assertTrue(containsKeyOrValue(result, CommonLdConstants.RESULT_PAGE));
	assertTrue(containsKeyOrValue(result, CommonLdConstants.ID));

	// delete item created by test
	getUserSetService().deleteUserSet(setIdentifier);
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
	getUserSetService().deleteUserSet(setIdentifier);
    }

    private String callSearchItemsInSet(String setIdentifier, String[] qf, String page, String pageSize,
	    String regularUserToken) throws UnsupportedEncodingException, Exception {

	MockHttpServletRequestBuilder searchRequest = buildSearchItemsInSetRequest(setIdentifier, qf, page, pageSize,
		regularUserToken);

	return mockMvc.perform(searchRequest).andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse()
		.getContentAsString();

    }

    private MockHttpServletRequestBuilder buildSearchItemsInSetRequest(String setIdentifier, String[] qf, String page,
	    String pageSize, String regularUserToken) {
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

//		apikey will be ignored
	MockHttpServletRequestBuilder requestBuilder = getRequest.queryParam(CommonApiConstants.QUERY_PARAM_QUERY,
		UserSetQueryBuilder.SEARCH_ALL);

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
	mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
		.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_COLLECTION)
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.OK.value()));
	// delete item created by test
	getUserSetService().deleteUserSet(set1.getIdentifier());
	getUserSetService().deleteUserSet(set2.getIdentifier());
    }
}
