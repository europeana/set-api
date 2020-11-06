package eu.europeana.set.web.service.impl;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.search.UserSetQueryImpl;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.web.model.WebUserSetImpl;
import eu.europeana.set.web.search.BaseUserSetResultPage;

@ContextConfiguration(locations = { "classpath:set-web-test.xml" })
//@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
public class UserSetServiceImplTest {

    private static final String REQUEST_URL = "http://set-api-test/set/search";

    @Autowired
    UserSetServiceImpl userSetService;
    UserSetQuery userSetQuery;
    ResultSet<? extends UserSet> resultSet;
    Authentication authentication;

    @BeforeEach
    void setup() throws IOException {
	if(resultSet != null) {
	    //test objects are already initialized
	    return;
	}
	resultSet = new ResultSet<>();
	userSetQuery = new UserSetQueryImpl();
	authentication = Mockito.mock(Authentication.class);

	// userSetQuery.
	userSetQuery.setPageSize(10);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void updateResultSetItems(int resultSetSize) {
	List items = new ArrayList<WebUserSetImpl>();
	UserSet set;
	for (int i = 1; i <= resultSetSize; i++) {
	    set = new WebUserSetImpl();
	    set.setIdentifier(String.valueOf(i));
	    items.add(set);
	}
	resultSet.setResults(items);
	resultSet.setResultSize(resultSetSize);
    }

    // test to verify the last Page logic
    @Test
    public void TestGetLastPage() {
	updateResultSetItems(55);
	assertTrue(userSetService.getLastPage(55, 10) == 5);
	assertTrue(userSetService.getLastPage(55, 11) == 4);
	updateResultSetItems(50);
	assertTrue(userSetService.getLastPage(50, 10) == 4);
	assertTrue(userSetService.getLastPage(0, 10) == 0);
    }

    // test to verify the pagination fields

    @Test
    public void testPaginationForFirstPage() throws Exception {
	updateResultSetItems(50);
	int lastPage = userSetService.getLastPage(resultSet.getResultSize(), userSetQuery.getPageSize());
	assertTrue(lastPage == 4);

	userSetQuery.setPageNr(0);

	String first = userSetService.buildPageUrl(REQUEST_URL + "?", 0, userSetQuery.getPageSize());
	String last = userSetService.buildPageUrl(REQUEST_URL + "?", lastPage, userSetQuery.getPageSize());
	String next = userSetService.buildPageUrl(REQUEST_URL + "?", userSetQuery.getPageNr() + 1,
		userSetQuery.getPageSize());
	String curr = userSetService.buildPageUrl(REQUEST_URL + "?", userSetQuery.getPageNr(),
		userSetQuery.getPageSize());

	StringBuilder buffer = new StringBuilder(REQUEST_URL);
	BaseUserSetResultPage<?> result = userSetService.buildResultsPage(userSetQuery, resultSet, buffer, "",
		LdProfiles.STANDARD, authentication);

	assertTrue(StringUtils.equals(first, result.getPartOf().getFirst()));
	assertTrue(StringUtils.equals(last, result.getPartOf().getLast()));
	assertNull(result.getPrevPageUri());
	assertTrue(StringUtils.equals(next, result.getNextPageUri()));
	assertTrue(StringUtils.equals(curr, result.getCurrentPageUri()));
    }

    @Test
    public void testPaginationForPage2() throws Exception {

	updateResultSetItems(50);
	int lastPage = userSetService.getLastPage(resultSet.getResultSize(), userSetQuery.getPageSize());
	assertTrue(lastPage == 4);

	userSetQuery.setPageNr(2);

	String first = userSetService.buildPageUrl(REQUEST_URL + "?", 0, userSetQuery.getPageSize());
	String last = userSetService.buildPageUrl(REQUEST_URL + "?", lastPage, userSetQuery.getPageSize());
	String next = userSetService.buildPageUrl(REQUEST_URL + "?", userSetQuery.getPageNr() + 1,
		userSetQuery.getPageSize());
	String prev = userSetService.buildPageUrl(REQUEST_URL + "?", userSetQuery.getPageNr() - 1,
		userSetQuery.getPageSize());
	String curr = userSetService.buildPageUrl(REQUEST_URL + "?", userSetQuery.getPageNr(),
		userSetQuery.getPageSize());

	StringBuilder buffer = new StringBuilder(REQUEST_URL);
	BaseUserSetResultPage<?> result = userSetService.buildResultsPage(userSetQuery, resultSet, buffer, "",
		LdProfiles.MINIMAL, authentication);

	assertTrue(StringUtils.equals(first, result.getPartOf().getFirst()));
	assertTrue(StringUtils.equals(last, result.getPartOf().getLast()));
	assertTrue(StringUtils.equals(next, result.getNextPageUri()));
	assertTrue(StringUtils.equals(prev, result.getPrevPageUri()));
	assertTrue(StringUtils.equals(curr, result.getCurrentPageUri()));
    }

    @Test
    public void testPaginationForLastPage() throws Exception {
	updateResultSetItems(50);
	int lastPage = userSetService.getLastPage(resultSet.getResultSize(), userSetQuery.getPageSize());
	assertTrue(lastPage == 4);

	userSetQuery.setPageNr(lastPage);

	String first = userSetService.buildPageUrl(REQUEST_URL + "?", 0, userSetQuery.getPageSize());
	String last = userSetService.buildPageUrl(REQUEST_URL + "?", lastPage, userSetQuery.getPageSize());
	String prev = userSetService.buildPageUrl(REQUEST_URL + "?", userSetQuery.getPageNr() - 1,
		userSetQuery.getPageSize());
	String curr = userSetService.buildPageUrl(REQUEST_URL + "?", userSetQuery.getPageNr(),
		userSetQuery.getPageSize());

	StringBuilder buffer = new StringBuilder(REQUEST_URL);
	BaseUserSetResultPage<?> result = userSetService.buildResultsPage(userSetQuery, resultSet, buffer, "",
		LdProfiles.ITEMDESCRIPTIONS, authentication);

	assertTrue(StringUtils.equals(first, result.getPartOf().getFirst()));
	assertTrue(StringUtils.equals(last, result.getPartOf().getLast()));
	assertNull(result.getNextPageUri());
	assertTrue(StringUtils.equals(prev, result.getPrevPageUri()));
	assertTrue(StringUtils.equals(curr, result.getCurrentPageUri()));
    }

    @Test
    public void testPaginationForOddNumberResults() throws Exception {
	updateResultSetItems(57);
//	resultSet.setResultSize(57);
	int lastPage = userSetService.getLastPage(resultSet.getResultSize(), userSetQuery.getPageSize());
	assertTrue(lastPage == 5);

	userSetQuery.setPageNr(3);

	String first = userSetService.buildPageUrl(REQUEST_URL + "?", 0, userSetQuery.getPageSize());
	String last = userSetService.buildPageUrl(REQUEST_URL + "?", lastPage, userSetQuery.getPageSize());
	String next = userSetService.buildPageUrl(REQUEST_URL + "?", userSetQuery.getPageNr() + 1,
		userSetQuery.getPageSize());
	String prev = userSetService.buildPageUrl(REQUEST_URL + "?", userSetQuery.getPageNr() - 1,
		userSetQuery.getPageSize());
	String curr = userSetService.buildPageUrl(REQUEST_URL + "?", userSetQuery.getPageNr(),
		userSetQuery.getPageSize());

	StringBuilder buffer = new StringBuilder(REQUEST_URL);
	BaseUserSetResultPage<?> result = userSetService.buildResultsPage(userSetQuery, resultSet, buffer, "",
		LdProfiles.ITEMDESCRIPTIONS, authentication);

	assertTrue(StringUtils.equals(first, result.getPartOf().getFirst()));
	assertTrue(StringUtils.equals(last, result.getPartOf().getLast()));
	assertTrue(StringUtils.equals(next, result.getNextPageUri()));
	assertTrue(StringUtils.equals(prev, result.getPrevPageUri()));
	assertTrue(StringUtils.equals(curr, result.getCurrentPageUri()));
    }

}
