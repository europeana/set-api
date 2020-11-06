package eu.europeana.api.set.integration.web;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
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
//        UserSet set = createTestUserSet(USER_SET_BOOKMARK_FOLDER, token);
	mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.queryParam(CommonApiConstants.PARAM_WSKEY, "").queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "")
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void searchInvalidApiKey() throws Exception {
	mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.queryParam(CommonApiConstants.PARAM_WSKEY, "invalid_api_key").queryParam(CommonApiConstants.QUERY_PARAM_QUERY, "")
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
	UserSet set = createTestUserSet(USER_SET_REGULAR, token);
	mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
		.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_SET_ID + set.getIdentifier())
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.OK.value()));
	//delete item created by test
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
	UserSet set = createTestUserSet(USER_SET_REGULAR_PUBLIC, token);

	mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
		.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, PUBLIC_VISIBILITY)
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.OK.value()));
	
	//delete item created by test
	getUserSetService().deleteUserSet(set.getIdentifier());
    }

    @Test
    public void searchWithPublicVisibility_ItemsDescription() throws Exception {
	// create object in database
	UserSet set = createTestUserSet(USER_SET_REGULAR_PUBLIC, token);

	mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.ITEMDESCRIPTIONS.name())
		.queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
		.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, PUBLIC_VISIBILITY)
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.OK.value()));
	
	//delete item created by test
	getUserSetService().deleteUserSet(set.getIdentifier());
    }
    
    @Test
    public void searchWithPrivateVisibility() throws Exception {
	deleteBookmarkFolder(token);
	UserSet set1 = createTestUserSet(USER_SET_MANDATORY, token);
	UserSet set2 = createTestUserSet(USER_SET_REGULAR, token);
	//Update tests to delete sets before test and enable bookmark folder creation
	UserSet set3 = createTestUserSet(USER_SET_BOOKMARK_FOLDER, token);
	mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
		.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, PRIVATE_VISIBILITY)
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.OK.value()));
	
	//delete item created by test
	getUserSetService().deleteUserSet(set1.getIdentifier());
	getUserSetService().deleteUserSet(set2.getIdentifier());
	getUserSetService().deleteUserSet(set3.getIdentifier());
    }

    

    @Test
    public void searchWithPublishedVisibility() throws Exception {
	UserSet set = createTestUserSet(USER_SET_REGULAR_PUBLISHED, token);
	mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
		.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, PUBLISHED_VISIBILITY)
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.OK.value()));

	//delete item created by test
	getUserSetService().deleteUserSet(set.getIdentifier());
    }

    @Test
    public void searchWithCreator() throws Exception {
	deleteBookmarkFolder(token);
	UserSet set1 = createTestUserSet(USER_SET_REGULAR, token);
	UserSet set2 = createTestUserSet(USER_SET_MANDATORY, token);
	//Update tests to delete sets before test and enable bookmark folder creation
	UserSet set3 = createTestUserSet(USER_SET_BOOKMARK_FOLDER, token);
	String creator = (String) getAuthentication(token).getPrincipal();
	String result = mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.header(HttpHeaders.AUTHORIZATION, token)
//		apikey will be ignored
		.queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
		.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_CREATOR + creator)
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse().getContentAsString();
	//check ids
	assertTrue(StringUtils.contains(result, UserSetUtils.buildUserSetId(set1.getIdentifier())));
	assertTrue(StringUtils.contains(result, UserSetUtils.buildUserSetId(set2.getIdentifier())));
	assertTrue(StringUtils.contains(result, UserSetUtils.buildUserSetId(set3.getIdentifier())));

	//delete item created by test
	getUserSetService().deleteUserSet(set1.getIdentifier());
	getUserSetService().deleteUserSet(set2.getIdentifier());
	getUserSetService().deleteUserSet(set3.getIdentifier());
    }

    @Test
    public void searchTypeCollection() throws Exception {
	UserSet set1 = createTestUserSet(USER_SET_REGULAR, token);
	UserSet set2 = createTestUserSet(USER_SET_MANDATORY, token);
	mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
		.queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
		.queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_COLLECTION)
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
		.andExpect(status().is(HttpStatus.OK.value()));
	//delete item created by test
	getUserSetService().deleteUserSet(set1.getIdentifier());
	getUserSetService().deleteUserSet(set2.getIdentifier());	
    }
}
