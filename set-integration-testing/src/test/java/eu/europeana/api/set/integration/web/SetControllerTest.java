package eu.europeana.api.set.integration.web;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.web.service.controller.jsonld.WebUserSetRest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
@ContextConfiguration(locations = {"classpath:set-web-mvc.xml"})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class SetControllerTest extends BaseUserSetUtils {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    private static String token;

    @BeforeAll
    public static void initToken() {
        token = getToken();
    }

    @BeforeEach
    public void initApplication() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    String BASE_URL = "/set/";

    public static final String USER_SET_CONTENT = "/content/userset.json";
    public static final String UPDATED_USER_SET_CONTENT = "/content/updatedUserset.json";

    // Create User Set Tests
    @Test
    public void testCreate_UserSet_201Created() throws Exception {
        String requestJson = getJsonStringInput(USER_SET_CONTENT);

        mockMvc.perform(post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .content(requestJson).header(HttpHeaders.AUTHORIZATION, token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andReturn();
    }

    @Test
    public void create_UserSet_401_bad_request_InvalidInput() throws Exception {
        mockMvc.perform(post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .content("").header(HttpHeaders.AUTHORIZATION, token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void create_UserSet_400_unauthorized_InvalidJWTToken() throws Exception {
        String requestJson = getJsonStringInput(USER_SET_CONTENT);

        mockMvc.perform(post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .content(requestJson).header(HttpHeaders.AUTHORIZATION, "")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    // Get user sets Tests

    @Test
    public void testGetUserSet_NotAuthorised() throws Exception {
        mockMvc.perform(get(BASE_URL + "{identifier}", 100)
                .header(HttpHeaders.AUTHORIZATION, "")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void testGetUserSet_Success() throws Exception {
        String requestJson = getJsonStringInput(USER_SET_CONTENT);
        // create a set and get the identifier
        MvcResult result = mockMvc.perform(post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .content(requestJson).header(HttpHeaders.AUTHORIZATION, token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andReturn();

        String identifier = getIdentifier(result.getResponse().getContentAsString());

        // get the identifier
        mockMvc.perform(get(BASE_URL + "{identifier}", identifier)
                .header(HttpHeaders.AUTHORIZATION, token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.OK.value()));
    }

    //Update user set Tests

    @Test
    public void testUpdateUserSet_NotAuthorised() throws Exception {
        mockMvc.perform(put(BASE_URL + "{identifier}", "test")
                .content("updatedRequestJson")
                .header(HttpHeaders.AUTHORIZATION, "")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void testUpdateUserSet_UserSetNotFound() throws Exception {
        mockMvc.perform(put(BASE_URL + "{identifier}", "test")
                .content("updatedRequestJson")
                .header(HttpHeaders.AUTHORIZATION, token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void testUpdateUserSet_Success() throws Exception {
        String requestJson = getJsonStringInput(USER_SET_CONTENT);
        // create a set and get the identifier
        MvcResult result = mockMvc.perform(post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .content(requestJson).header(HttpHeaders.AUTHORIZATION, token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andReturn();

        String identifier = getIdentifier(result.getResponse().getContentAsString());

        String updatedRequestJson = getJsonStringInput(UPDATED_USER_SET_CONTENT);
        // update the userset
        mockMvc.perform(put(BASE_URL + "{identifier}", identifier)
                .content(updatedRequestJson)
                .header(HttpHeaders.AUTHORIZATION, token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.OK.value()));
    }

    // Delete User associated Tests
    @Test
    public void testDeleteUserAssociatedSets_Success() throws Exception {
        mockMvc.perform(delete(BASE_URL)
                .header(HttpHeaders.AUTHORIZATION, token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    }

    @Test
    public void testDeleteUserAssociatedSets_NotAuthorised() throws Exception {
        mockMvc.perform(delete(BASE_URL)
                .header(HttpHeaders.AUTHORIZATION, "")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    // Delete User set via identifier Tests
    @Test
    public void testDeleteUserSet_NotAuthorised() throws Exception {
        mockMvc.perform(delete(BASE_URL + "{identifier}", 100)
                .header(HttpHeaders.AUTHORIZATION, "")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void testDeleteUserSet_OperationNotAuthorised() throws Exception {
        mockMvc.perform(delete(BASE_URL + "{identifier}", 100)
                .header(HttpHeaders.AUTHORIZATION, token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void testDeleteUserSet_UserSetNotFound() throws Exception {
        mockMvc.perform(delete(BASE_URL + "{identifier}",  "test")
                .header(HttpHeaders.AUTHORIZATION, token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }


    @Test
    public void testDeleteUserSet_Success() throws Exception {
        String requestJson = getJsonStringInput(USER_SET_CONTENT);
        // create a set and get the identifier
        MvcResult result = mockMvc.perform(post(BASE_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .content(requestJson).header(HttpHeaders.AUTHORIZATION, token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andReturn();

        String identifier = getIdentifier(result.getResponse().getContentAsString());

        // delete the identifier
        mockMvc.perform(delete(BASE_URL + "{identifier}", identifier)
                .header(HttpHeaders.AUTHORIZATION, token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    }

    /**
     * Gets the identifier value from a responseString
     *
     * @param responseString
     * @return identifier
     */
    private static String getIdentifier(String responseString) {
        String value = StringUtils.substringsBetween(responseString, "\"id\":", ",\"type\"")[0];
        String identifier = StringUtils.remove(value, WebUserSetFields.BASE_SET_URL);
        return identifier.replace("\"", "").trim();
    }

}
