package eu.europeana.api.set.integration.web;

import eu.europeana.api.commons_sb3.definitions.utils.DateUtils;
import eu.europeana.api.commons_sb3.error.config.ErrorMessage;
import eu.europeana.api.set.integration.IntegrationTestSetup;
import eu.europeana.api.set.integration.exception.SetIntegrationException;
import eu.europeana.set.web.model.WebUserSetImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static eu.europeana.api.commons_sb3.definitions.http.HttpHeaders.CONTENT_TYPE_JSONLD_UTF8;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class HeadersTestIT extends IntegrationTestSetup {

    @BeforeAll
    static void initTokens() throws SetIntegrationException {
        if (DISABLE_AUTH) {
            return;
        }
        initRegularUserToken();
    }


    @Override
    @AfterEach
    protected void deleteCreatedSets() {
        super.deleteCreatedSets();
    }

    @Test
    void getUserSet_SuccessWithCaseSensitiveToken() throws Exception {
        WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

        String caseSensitiveToken = StringUtils.replace(regularUserToken, "Bearer", "bearer");
        mockMvc
                .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
                        .header(HttpHeaders.AUTHORIZATION, caseSensitiveToken)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(HttpHeaders.ALLOW, "HEAD,DELETE,GET,PUT"))
                .andExpect(header().exists(HttpHeaders.CONTENT_TYPE))
                .andExpect(header().stringValues(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSONLD_UTF8))
                .andExpect(header().exists(HttpHeaders.ETAG))
                .andReturn().getResponse();
    }

    @Test
    void getUserSet_SuccessHeaders() throws Exception {
        WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

        MockHttpServletResponse response = mockMvc
                .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
                        .header(HttpHeaders.AUTHORIZATION, regularUserToken)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(HttpHeaders.ALLOW, "HEAD,DELETE,GET,PUT"))
                .andExpect(header().exists(HttpHeaders.CONTENT_TYPE))
                .andExpect(header().stringValues(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSONLD_UTF8))
                .andExpect(header().exists(HttpHeaders.ETAG))
                .andReturn().getResponse();

        assertEquals(new Date(response.getHeader(HttpHeaders.LAST_MODIFIED)),
                new Date(DateUtils.getRFC_1123_FormatDate(userSet.getModified())));

    }


    @Test
    void getUserSet_If_None_Match_header() throws Exception {
        WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

        //  // check it has caching headers - etag and last modified
        MockHttpServletResponse response = mockMvc
                .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
                        .header(HttpHeaders.AUTHORIZATION, regularUserToken)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ETAG))
                .andExpect(header().exists(HttpHeaders.LAST_MODIFIED))
                .andReturn().getResponse();

        assertEquals(new Date(response.getHeader(HttpHeaders.LAST_MODIFIED)),
                new Date(DateUtils.getRFC_1123_FormatDate(userSet.getModified())));

        // IF_NONE_MATCH header with etag value obtained in the previous response
        mockMvc
                .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
                        .header(HttpHeaders.AUTHORIZATION, regularUserToken)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.IF_NONE_MATCH, response.getHeader(HttpHeaders.ETAG)))
                .andExpect(status().is(HttpStatus.NOT_MODIFIED.value()));

        // IF_NONE_MATCH header with the wrong etag value
        mockMvc
                .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
                        .header(HttpHeaders.AUTHORIZATION, regularUserToken)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.IF_NONE_MATCH, "test"))
                .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    void getUserSet_If_Modified_Since_header() throws Exception {
        WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

        // check it has caching headers - etag and last modified
        MockHttpServletResponse response = mockMvc
                .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
                        .header(HttpHeaders.AUTHORIZATION, regularUserToken)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ETAG))
                .andExpect(header().exists(HttpHeaders.LAST_MODIFIED))
                .andReturn().getResponse();

        assertEquals(new Date(response.getHeader(HttpHeaders.LAST_MODIFIED)),
                new Date(DateUtils.getRFC_1123_FormatDate(userSet.getModified())));


        // IF_MODIFIED_SINCE header with LAST_MODIFIED value obtained in the previous response
        mockMvc
                .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
                        .header(HttpHeaders.AUTHORIZATION, regularUserToken)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.IF_MODIFIED_SINCE, response.getHeader(HttpHeaders.LAST_MODIFIED)))
                .andExpect(status().is(HttpStatus.NOT_MODIFIED.value()));

        ZonedDateTime dateTime = DateUtils.parseRFCToZonedDateTime(response.getHeader(HttpHeaders.LAST_MODIFIED));

        // IF_MODIFIED_SINCE header with one day before than LAST_MODIFIED
        mockMvc
                .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
                        .header(HttpHeaders.AUTHORIZATION, regularUserToken)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.IF_MODIFIED_SINCE, dateTime.minusDays(1).format(DateTimeFormatter.RFC_1123_DATE_TIME)))
                .andExpect(status().is(HttpStatus.OK.value()));

        // IF_MODIFIED_SINCE header with one day after than LAST_MODIFIED
        mockMvc
                .perform(get(BASE_URL + "{identifier}", userSet.getIdentifier())
                        .header(HttpHeaders.AUTHORIZATION, regularUserToken)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.IF_MODIFIED_SINCE, dateTime.plusDays(1).format(DateTimeFormatter.RFC_1123_DATE_TIME)))
                .andExpect(status().is(HttpStatus.NOT_MODIFIED.value()));
    }

    @Test
    void updateUserSet_PreconditionFailed() throws Exception {
        WebUserSetImpl userSet = createTestUserSet(USER_SET_REGULAR, regularUserToken);

        String updatedRequestJson = getJsonStringInput(UPDATED_USER_SET_CONTENT);
        // update the userset
        MockHttpServletResponse response = mockMvc
                .perform(put(BASE_URL + "{identifier}", userSet.getIdentifier())
                        .content(updatedRequestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ETAG)) //check etag present in the response
                .andReturn().getResponse();

        // IF_MATCH with matching etag - 200 ok
        mockMvc
                .perform(put(BASE_URL + "{identifier}", userSet.getIdentifier())
                        .content(updatedRequestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.IF_MATCH, response.getHeader(HttpHeaders.ETAG)))
                .andExpect(status().isOk());

        // IF_MATCH with wrong etag - 412 response
        String content = mockMvc
                .perform(put(BASE_URL + "{identifier}", userSet.getIdentifier())
                        .content(updatedRequestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.IF_MATCH, "test"))
                .andExpect(status().isPreconditionFailed()).andReturn().getResponse().getContentAsString();

        // check response body
        assertTrue(containsKeyOrValue(content, ErrorMessage.ETAG_MISMATCH_412.getError()));
        assertTrue(containsKeyOrValue(content, ErrorMessage.ETAG_MISMATCH_412.getCode()));
    }

}
