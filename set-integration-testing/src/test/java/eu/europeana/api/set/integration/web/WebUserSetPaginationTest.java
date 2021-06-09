package eu.europeana.api.set.integration.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.definitions.vocabulary.CommonLdConstants;
import eu.europeana.set.definitions.config.UserSetConfigurationImpl;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.web.model.WebUserSetImpl;
import eu.europeana.set.web.model.search.CollectionPage;
import eu.europeana.set.web.service.controller.jsonld.WebUserSetRest;

/**
 * Test class for UserSet controller.
 * <p>
 * For all the methods createUserSet , getUserSet , updateUserSet,
 * deleteUserSet, deleteItemFromUserSet, insertItemIntoUserSet, isItemInUserSet
 * <p>
 * MockMvc test for the Main entry point for server-side Spring MVC. Should
 * check for 200 Ok, 400 bad request (if required paremter are not passed), 401
 * unauthorized (if authentication provided is wrong), and 404 Not found
 * scenarios. Should also check all the headers added using the
 * UserSetHttpHeaders constants
 *
 * @author Roman Graf on 10-09-2020.
 */
@WebMvcTest(WebUserSetRest.class)
@ContextConfiguration(locations = { "classpath:set-web-mvc.xml" })
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class WebUserSetPaginationTest extends BaseUserSetTestUtils {

    @BeforeAll
    public static void initTokens() {
	initRegularUserToken();
    }

    @BeforeEach
    public void initApplication() {
	super.initApplication();
    }

    @Test
    public void getUserSetPagination() throws Exception {
	WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE, regularUserToken);

	// get the identifier
	MockHttpServletResponse response = mockMvc.perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
		.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE, "1")
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, "5")
		.header(HttpHeaders.AUTHORIZATION, regularUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).andReturn().getResponse();

	//
	String result = response.getContentAsString();
	assertNotNull(result);
	assertEquals(HttpStatus.OK.value(), response.getStatus());
	// build collection uri?
//	String collectionUrl = buildCollectionUrl(null, request.getRequestURL().toString(), request.getQueryString());	
//	assertTrue(constainsKey(result, collectionUrl));

	assertTrue(containsKeyOrValue(result, WebUserSetFields.PART_OF));
	assertTrue(containsKeyOrValue(result, CommonLdConstants.COLLECTION));
	assertTrue(containsKeyOrValue(result, CollectionPage.COLLECTION_PAGE));
	assertTrue(containsKeyOrValue(result, WebUserSetFields.START_INDEX));
	assertTrue(containsKeyOrValue(result, WebUserSetFields.FIRST));
	assertTrue(containsKeyOrValue(result, WebUserSetFields.LAST));
	assertTrue(containsKeyOrValue(result, WebUserSetFields.PREV));
	assertTrue(containsKeyOrValue(result, WebUserSetFields.NEXT));
	assertTrue(containsKeyOrValue(result, WebUserSetFields.ITEMS));
	//verify that the ids are not escaped
	assertTrue(containsKeyOrValue(result, "http://data.europeana.eu/item/11648/_Botany_L_1444437")); 
//	assertTrue(constainsKeyOrValue(result, WebUserSetFields.ITEMS));

	int idCount = StringUtils.countMatches(result, "\"id\"");
	// 1 id part of and one for collection page
	assertEquals(2, idCount);

	int total = StringUtils.countMatches(result, "\"total\"");
	// 1 id part of and one for collection page
	assertEquals(2, total);

	getUserSetService().deleteUserSet(userSet.getIdentifier());
    }

    
    @Test
    public void getEmptyUserSet() throws Exception {
	WebUserSetImpl userSet = createTestUserSet(USER_SET_MANDATORY, regularUserToken);

	// get the identifier
	MockHttpServletResponse response = mockMvc.perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
		.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
//		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE, "1")
//		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, "5")
		.header(HttpHeaders.AUTHORIZATION, regularUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).andReturn().getResponse();

	//
	String result = response.getContentAsString();
	assertNotNull(result);
	assertEquals(HttpStatus.OK.value(), response.getStatus());
	//not available for empty sets
	assertFalse(containsKeyOrValue(result, WebUserSetFields.PREV));
	assertFalse(containsKeyOrValue(result, WebUserSetFields.NEXT));
	assertFalse(containsKeyOrValue(result, WebUserSetFields.ITEMS));

	int idCount = StringUtils.countMatches(result, "\"id\"");
	// 1 id for userset and one for creator
	assertEquals(2, idCount);

	int total = StringUtils.countMatches(result, "\"total\"");
	// 1 total only for the set
	assertEquals(1, total);

	getUserSetService().deleteUserSet(userSet.getIdentifier());
    }
    
    @Test
    public void getPageForEmptyUserSet() throws Exception {
	WebUserSetImpl userSet = createTestUserSet(USER_SET_MANDATORY, regularUserToken);

	// get the identifier
	MockHttpServletResponse response = mockMvc.perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
		.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE, "0")
//		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, "5")
		.header(HttpHeaders.AUTHORIZATION, regularUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).andReturn().getResponse();

	//
	String result = response.getContentAsString();
	assertNotNull(result);
	assertEquals(HttpStatus.OK.value(), response.getStatus());
	// build collection uri?
//	String collectionUrl = buildCollectionUrl(null, request.getRequestURL().toString(), request.getQueryString());	
//	assertTrue(containsKeyOrValue(result, collectionUrl));

	assertTrue(containsKeyOrValue(result, WebUserSetFields.PART_OF));
	assertTrue(containsKeyOrValue(result, CommonLdConstants.COLLECTION));
	assertTrue(containsKeyOrValue(result, CollectionPage.COLLECTION_PAGE));
	assertTrue(containsKeyOrValue(result, WebUserSetFields.START_INDEX));
	//for empty collections, isPartOf must not contain first and last 
	assertFalse(containsKeyOrValue(result, WebUserSetFields.FIRST));
	assertFalse(containsKeyOrValue(result, WebUserSetFields.LAST));
	//not available for empty sets
	assertFalse(containsKeyOrValue(result, WebUserSetFields.PREV));
	assertFalse(containsKeyOrValue(result, WebUserSetFields.NEXT));
	assertFalse(containsKeyOrValue(result, WebUserSetFields.ITEMS));

	int idCount = StringUtils.countMatches(result, "\"id\"");
	// 3 ids: collection page, set, creator?
	assertEquals(2, idCount);

	int total = StringUtils.countMatches(result, "\"total\"");
	//2 totals: collection page and set 
	assertEquals(2, total);

	getUserSetService().deleteUserSet(userSet.getIdentifier());
    }
    
    @Test
    public void getUserSetPaginationDefaultPageSize() throws Exception {
	WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE, regularUserToken);

	// get the identifier
	MockHttpServletResponse response = mockMvc.perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
		.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE, "1")
		.header(HttpHeaders.AUTHORIZATION, regularUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).andReturn().getResponse();

	//
	String result = response.getContentAsString();
	assertNotNull(result);
	assertEquals(HttpStatus.OK.value(), response.getStatus());
	//verify that ids are not escaped, use one item from second page
	assertTrue(containsKeyOrValue(result, "http://data.europeana.eu/item/11647/_Botany_AMD_87140"));

	int defaultPageSize = UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE;
	int pageSize = StringUtils.countMatches(result, "http://data.europeana.eu/item/");
	assertEquals(defaultPageSize, pageSize);

	getUserSetService().deleteUserSet(userSet.getIdentifier());
    }

    @Test
    public void getUserSetPaginationItemDescriptions() throws Exception {
	WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE, regularUserToken);

	// get the identifier
	MockHttpServletResponse response = mockMvc.perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
		.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.ITEMDESCRIPTIONS.name())
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE, "1")
		.header(HttpHeaders.AUTHORIZATION, regularUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).andReturn().getResponse();

	//
	String result = response.getContentAsString();
	assertNotNull(result);
	assertEquals(HttpStatus.OK.value(), response.getStatus());
	//verify that ids are not escaped, use one item from second page
	assertTrue(containsKeyOrValue(result, "\\/11647\\/_Botany_AMD_87140"));

	int defaultPageSize = UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE;
	int pageSize = StringUtils.countMatches(result, "\\/item\\/");
	assertEquals(defaultPageSize, pageSize);

	getUserSetService().deleteUserSet(userSet.getIdentifier());
    }
    
    @Test
    public void getUserSetPaginationEmptyPageNr() throws Exception {
	WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE, regularUserToken);

	// get the identifier
	MockHttpServletResponse response = mockMvc.perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
		.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE, "")
//		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, "5")
		.header(HttpHeaders.AUTHORIZATION, regularUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).andReturn().getResponse();

	//
	String result = response.getContentAsString();
	assertNotNull(result);
	assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
	assertTrue(StringUtils.contains(result, CommonApiConstants.QUERY_PARAM_PAGE));
	
	getUserSetService().deleteUserSet(userSet.getIdentifier());
    }

    @Test
    public void getUserSetPaginationPageSizeExceeded() throws Exception {
	WebUserSetImpl userSet = createTestUserSet(USER_SET_LARGE, regularUserToken);

	// get the identifier
	MockHttpServletResponse response = mockMvc.perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
		.queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE, "0")
		.queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, "200")
		.header(HttpHeaders.AUTHORIZATION, regularUserToken)
		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)).andReturn().getResponse();

	//
	String result = response.getContentAsString();
	assertNotNull(result);
	assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
	assertTrue(StringUtils.contains(result, CommonApiConstants.QUERY_PARAM_PAGE_SIZE));

	getUserSetService().deleteUserSet(userSet.getIdentifier());
    }

}
