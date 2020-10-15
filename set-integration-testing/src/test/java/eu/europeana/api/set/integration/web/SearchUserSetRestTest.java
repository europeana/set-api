package eu.europeana.api.set.integration.web;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.web.service.controller.jsonld.SearchUserSetRest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;


@WebMvcTest(SearchUserSetRest.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration()
@ContextConfiguration(locations = {"classpath:set-web-mvc.xml"})
public class SearchUserSetRestTest {

    private static final String API_KEY = "api2demo";
    private static final String SEARCH_URL = "/set/search";
    private static final String SEARCH_BY_SET_ID = WebUserSetFields.SET_ID + ":" +"100" ;
    private static final String SEARCH_BY_INVALID_SET_ID = WebUserSetFields.SET_ID + ":" +"test" ;
    private static final String SEARCH_BY_VISIBILITY = WebUserSetFields.VISIBILITY + ":" + VisibilityTypes.PUBLIC.getJsonValue();


    private static final String PAGE_SIZE = "100";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;


    @BeforeEach
    public void initApplication() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }


    @Test
    public void testSearch_EmptyApiKey() throws Exception {

        mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, "")
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_BY_SET_ID)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void testSearch_InvalidApiKey() throws Exception {

        mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, "test")
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_BY_SET_ID)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void testSearch_WithoutApiKey() throws Exception {
        mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_BY_SET_ID)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void testSearch_WithValidSetId() throws Exception {

    mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_BY_SET_ID)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
                .andExpect(status().is(HttpStatus.OK.value()));

    }

    @Test
    public void testSearch_WithInvalidSetId() throws Exception {
        mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_BY_INVALID_SET_ID)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void testSearch_WithValidVisibility() throws Exception {
        mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_BY_VISIBILITY)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
                .andExpect(status().is(HttpStatus.OK.value()));
    }


    private static  void checkSearchResults(String responseString) {
        assertNotNull(responseString);
        String total = StringUtils.substringBetween(responseString , "\"total\"" , "}" );
        System.out.println("TOTAL"+total);
        List<String> itemList;


    }

    private static void checkErrorResponse(String errorResponse) {
        assertNotNull(errorResponse);
        String success = StringUtils.substringBetween(errorResponse, "\"success\":", ",\"error\":");
        System.out.println(success);
        String error =  StringUtils.substringBetween(errorResponse, "\"error\":\"", "\"}");
        System.out.println(error);

    }

}
