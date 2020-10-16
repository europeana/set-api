package eu.europeana.set.web.service.impl;

import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.search.UserSetQueryImpl;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.web.search.BaseUserSetResultPage;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ContextConfiguration(locations = {"classpath:set-web-test.xml"})
@ExtendWith(MockitoExtension.class)
public class UserSetServiceImplTest {

    private static final String REQUEST_URL = "http://set-api-test/set/search";

    UserSetServiceImpl userSetService;
    UserSetQuery userSetQuery;
    ResultSet<? extends UserSet> resultSet;
    Authentication authentication;

    @BeforeEach
    void setup() {
        resultSet = new ResultSet<>();
        userSetService = new UserSetServiceImpl();
        userSetQuery = new UserSetQueryImpl();
        authentication = Mockito.mock(Authentication.class);

        //userSetQuery.
        userSetQuery.setPageSize(10);
        resultSet.setResults(new ArrayList<>());
        resultSet.setResultSize(50);
    }

    // test to verify the last Page logic
    @Test
    public void TestGetLastPage() {
        assertTrue(userSetService.getLastPage(55, 10) == 5);
        assertTrue(userSetService.getLastPage(50, 10) == 4);
        assertTrue(userSetService.getLastPage(55, 11) == 4);
        assertTrue(userSetService.getLastPage(0, 10) == 0);
    }

    // test to verify the pagination fields

    @Test
    public void testPaginationForFirstPage() throws Exception {
        int lastPage = userSetService.getLastPage(resultSet.getResultSize(), userSetQuery.getPageSize());
        assertTrue(lastPage == 4);

        userSetQuery.setPageNr(0);

        String first = userSetService.buildPageUrl(REQUEST_URL + "?", 0, userSetQuery.getPageSize());
        String last = userSetService.buildPageUrl(REQUEST_URL + "?", lastPage, userSetQuery.getPageSize());
        String next = userSetService.buildPageUrl(REQUEST_URL + "?", userSetQuery.getPageNr() + 1, userSetQuery.getPageSize());
        String curr = userSetService.buildPageUrl(REQUEST_URL + "?", userSetQuery.getPageNr(), userSetQuery.getPageSize());

        StringBuffer buffer = new StringBuffer(REQUEST_URL);
        BaseUserSetResultPage<?> result = userSetService.buildResultsPage(userSetQuery, resultSet,
                buffer, "",
                LdProfiles.STANDARD, authentication
        );

        assertTrue(StringUtils.equals(first, result.getPartOf().getFirst()));
        assertTrue(StringUtils.equals(last, result.getPartOf().getLast()));
        assertNull(result.getPrevPageUri());
        assertTrue(StringUtils.equals(next, result.getNextPageUri()));
        assertTrue(StringUtils.equals(curr, result.getCurrentPageUri()));
    }

    @Test
    public void testPaginationForPage2() throws Exception {
        int lastPage = userSetService.getLastPage(resultSet.getResultSize(), userSetQuery.getPageSize());
        assertTrue(lastPage == 4);

        userSetQuery.setPageNr(2);

        String first = userSetService.buildPageUrl(REQUEST_URL + "?", 0, userSetQuery.getPageSize());
        String last = userSetService.buildPageUrl(REQUEST_URL + "?", lastPage, userSetQuery.getPageSize());
        String next = userSetService.buildPageUrl(REQUEST_URL + "?", userSetQuery.getPageNr() + 1, userSetQuery.getPageSize());
        String prev = userSetService.buildPageUrl(REQUEST_URL + "?", userSetQuery.getPageNr() - 1, userSetQuery.getPageSize());
        String curr = userSetService.buildPageUrl(REQUEST_URL + "?", userSetQuery.getPageNr(), userSetQuery.getPageSize());

        StringBuffer buffer = new StringBuffer(REQUEST_URL);
        BaseUserSetResultPage<?> result = userSetService.buildResultsPage(userSetQuery, resultSet,
                buffer, "",
                LdProfiles.MINIMAL, authentication
        );

        assertTrue(StringUtils.equals(first, result.getPartOf().getFirst()));
        assertTrue(StringUtils.equals(last, result.getPartOf().getLast()));
        assertTrue(StringUtils.equals(next, result.getNextPageUri()));
        assertTrue(StringUtils.equals(prev, result.getPrevPageUri()));
        assertTrue(StringUtils.equals(curr, result.getCurrentPageUri()));
    }

    @Test
    public void testPaginationForLastPage() throws Exception {
        int lastPage = userSetService.getLastPage(resultSet.getResultSize(), userSetQuery.getPageSize());
        assertTrue(lastPage == 4);

        userSetQuery.setPageNr(lastPage);

        String first = userSetService.buildPageUrl(REQUEST_URL + "?", 0, userSetQuery.getPageSize());
        String last = userSetService.buildPageUrl(REQUEST_URL + "?", lastPage, userSetQuery.getPageSize());
        String prev = userSetService.buildPageUrl(REQUEST_URL + "?", userSetQuery.getPageNr() - 1, userSetQuery.getPageSize());
        String curr = userSetService.buildPageUrl(REQUEST_URL + "?", userSetQuery.getPageNr(), userSetQuery.getPageSize());

        StringBuffer buffer = new StringBuffer(REQUEST_URL);
        BaseUserSetResultPage<?> result = userSetService.buildResultsPage(userSetQuery, resultSet,
                buffer, "",
                LdProfiles.ITEMDESCRIPTIONS, authentication
        );

        assertTrue(StringUtils.equals(first, result.getPartOf().getFirst()));
        assertTrue(StringUtils.equals(last, result.getPartOf().getLast()));
        assertNull(result.getNextPageUri());
        assertTrue(StringUtils.equals(prev, result.getPrevPageUri()));
        assertTrue(StringUtils.equals(curr, result.getCurrentPageUri()));
    }

    @Test
    public void testPaginationForOddNumberResults() throws Exception {
        resultSet.setResultSize(57);
        int lastPage = userSetService.getLastPage(resultSet.getResultSize(), userSetQuery.getPageSize());
        assertTrue(lastPage == 5);

        userSetQuery.setPageNr(3);

        String first = userSetService.buildPageUrl(REQUEST_URL + "?", 0, userSetQuery.getPageSize());
        String last = userSetService.buildPageUrl(REQUEST_URL + "?", lastPage, userSetQuery.getPageSize());
        String next = userSetService.buildPageUrl(REQUEST_URL + "?", userSetQuery.getPageNr() + 1, userSetQuery.getPageSize());
        String prev = userSetService.buildPageUrl(REQUEST_URL + "?", userSetQuery.getPageNr() - 1, userSetQuery.getPageSize());
        String curr = userSetService.buildPageUrl(REQUEST_URL + "?", userSetQuery.getPageNr(), userSetQuery.getPageSize());

        StringBuffer buffer = new StringBuffer(REQUEST_URL);
        BaseUserSetResultPage<?> result = userSetService.buildResultsPage(userSetQuery, resultSet,
                buffer, "",
                LdProfiles.ITEMDESCRIPTIONS, authentication
        );

        assertTrue(StringUtils.equals(first, result.getPartOf().getFirst()));
        assertTrue(StringUtils.equals(last, result.getPartOf().getLast()));
        assertTrue(StringUtils.equals(next, result.getNextPageUri()));
        assertTrue(StringUtils.equals(prev, result.getPrevPageUri()));
        assertTrue(StringUtils.equals(curr, result.getCurrentPageUri()));
    }

}
