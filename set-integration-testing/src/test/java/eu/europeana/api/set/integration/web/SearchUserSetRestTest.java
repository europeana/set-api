package eu.europeana.api.set.integration.web;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.web.service.controller.jsonld.SearchUserSetRest;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchUserSetRest.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration()
@ContextConfiguration(locations = {"classpath:set-web-mvc.xml"})
public class SearchUserSetRestTest {

    private static final String API_KEY               = "api2demo";
    private static final String SEARCH_URL            = "/set/search";
    private static final String SEARCH_SET_ID         = WebUserSetFields.SET_ID + ":" + "100";
    private static final String SEARCH_INVALID_SET_ID = WebUserSetFields.SET_ID + ":" + "test";
    private static final String PUBLIC_VISIBILITY     = WebUserSetFields.VISIBILITY + ":" + VisibilityTypes.PUBLIC.getJsonValue();
    private static final String PRIVATE_VISIBILITY    = WebUserSetFields.VISIBILITY + ":" + VisibilityTypes.PRIVATE.getJsonValue();
    private static final String PUBLISHED_VISIBILITY  = WebUserSetFields.VISIBILITY + ":" + VisibilityTypes.PUBLISHED.getJsonValue();
    private static final String CREATOR               = WebUserSetFields.CREATOR + ":test";
    private static final String COLLECTION_TYPE       = WebUserSetFields.TYPE + ":" + UserSetTypes.COLLECTION;

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
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_SET_ID)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void testSearch_InvalidApiKey() throws Exception {

        mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, "test")
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_SET_ID)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void testSearch_WithoutApiKey() throws Exception {
        mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_SET_ID)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void testSearch_WithValidSetId() throws Exception {

        mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_SET_ID)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
                .andExpect(status().is(HttpStatus.OK.value()));

    }

    @Test
    public void testSearch_WithInvalidSetId() throws Exception {
        mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, SEARCH_INVALID_SET_ID)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    // TODO once the set-mongo unit test are updated to use WebUserSetImpl
    //@Test
    public void testSearch_WithPublicVisibility() throws Exception {
        mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, PUBLIC_VISIBILITY)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
                .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    public void testSearch_WithPrivateVisibility() throws Exception {
        mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, PRIVATE_VISIBILITY)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
                .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    public void testSearch_WithPublishedVisibility() throws Exception {
        mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, PUBLISHED_VISIBILITY)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
                .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    public void testSearch_WithCreator() throws Exception {
        mockMvc.perform(get(SEARCH_URL).param(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.MINIMAL.name())
                .queryParam(CommonApiConstants.PARAM_WSKEY, API_KEY)
                .queryParam(CommonApiConstants.QUERY_PARAM_QUERY, CREATOR)
                .queryParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, PAGE_SIZE))
                .andExpect(status().is(HttpStatus.OK.value()));
    }

}
