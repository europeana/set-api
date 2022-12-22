package eu.europeana.set.web.service.impl;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import eu.europeana.set.web.model.search.BaseUserSetResultPage;

@ContextConfiguration(locations = { "classpath:set-web-context.xml" })
//@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
@Disabled("needs configuration file")
public class UserSetServiceImplTest {

	private static String REQUEST_URL;

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
//	authentication = Mockito.mock(Authentication.class);
	REQUEST_URL = userSetService.getConfiguration().getSetApiEndpoint() + "search";
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
	LdProfiles profile = LdProfiles.STANDARD;
	// check with standard profile
	String requestUrl = REQUEST_URL + "?profile=" + profile.getRequestParamValue();
	String first = userSetService.buildPageUrl(requestUrl, 0, userSetQuery.getPageSize(), null);
	String last = userSetService.buildPageUrl(requestUrl, lastPage, userSetQuery.getPageSize(), null);
	String next = userSetService.buildPageUrl(requestUrl, userSetQuery.getPageNr() + 1,
		userSetQuery.getPageSize(), profile);
	String curr = userSetService.buildPageUrl(requestUrl, userSetQuery.getPageNr(),
		userSetQuery.getPageSize(), profile);

	BaseUserSetResultPage<?> result = userSetService.buildResultsPage(userSetQuery, resultSet, requestUrl, "",
			Collections.singletonList(profile), authentication);

	assertTrue(StringUtils.equals(first, result.getPartOf().getFirst()));
	assertTrue(StringUtils.equals(last, result.getPartOf().getLast()));
	assertNull(result.getPrevPageUri());
	assertTrue(StringUtils.equals(next, result.getNextPageUri()));
	assertTrue(StringUtils.equals(curr, result.getCurrentPageUri()));
	checkPartOfUrls(result, profile);
	}

    @Test
    public void testPaginationForPage2() throws Exception {

	updateResultSetItems(50);
	int lastPage = userSetService.getLastPage(resultSet.getResultSize(), userSetQuery.getPageSize());
	assertTrue(lastPage == 4);

	userSetQuery.setPageNr(2);
	LdProfiles profile = LdProfiles.MINIMAL;
	// check with minimal profile
	String requestUrl = REQUEST_URL + "?profile=" + profile.getRequestParamValue();
	String first = userSetService.buildPageUrl(requestUrl , 0, userSetQuery.getPageSize(), profile);
	String last = userSetService.buildPageUrl(requestUrl, lastPage, userSetQuery.getPageSize(), profile);
	String next = userSetService.buildPageUrl(requestUrl, userSetQuery.getPageNr() + 1,
		userSetQuery.getPageSize(), profile);
	String prev = userSetService.buildPageUrl(requestUrl, userSetQuery.getPageNr() - 1,
		userSetQuery.getPageSize(), profile);
	String curr = userSetService.buildPageUrl(requestUrl, userSetQuery.getPageNr(),
		userSetQuery.getPageSize(), profile);

	BaseUserSetResultPage<?> result = userSetService.buildResultsPage(userSetQuery, resultSet, requestUrl, "",
			Collections.singletonList(profile), authentication);

	assertTrue(StringUtils.equals(first, result.getPartOf().getFirst()));
	assertTrue(StringUtils.equals(last, result.getPartOf().getLast()));
	assertTrue(StringUtils.equals(next, result.getNextPageUri()));
	assertTrue(StringUtils.equals(prev, result.getPrevPageUri()));
	assertTrue(StringUtils.equals(curr, result.getCurrentPageUri()));
	checkPartOfUrls(result, profile);
    }

    @Test
    public void testPaginationForLastPage() throws Exception {
	updateResultSetItems(50);
	int lastPage = userSetService.getLastPage(resultSet.getResultSize(), userSetQuery.getPageSize());
	assertTrue(lastPage == 4);

	userSetQuery.setPageNr(lastPage);
	LdProfiles profile = LdProfiles.ITEMDESCRIPTIONS;
    // check with item description profile
	String requestUrl = REQUEST_URL + "?profile=" + profile.getRequestParamValue();
    String first = userSetService.buildPageUrl(requestUrl, 0, userSetQuery.getPageSize(), profile);
	String last = userSetService.buildPageUrl(requestUrl, lastPage, userSetQuery.getPageSize(), profile);
	String prev = userSetService.buildPageUrl(requestUrl, userSetQuery.getPageNr() - 1,
		userSetQuery.getPageSize(), profile);
	String curr = userSetService.buildPageUrl(requestUrl, userSetQuery.getPageNr(),
		userSetQuery.getPageSize(), profile);

	BaseUserSetResultPage<?> result = userSetService.buildResultsPage(userSetQuery, resultSet, requestUrl, "",
			Collections.singletonList(profile), authentication);

	assertTrue(StringUtils.equals(first, result.getPartOf().getFirst()));
	assertTrue(StringUtils.equals(last, result.getPartOf().getLast()));
	assertNull(result.getNextPageUri());
	assertTrue(StringUtils.equals(prev, result.getPrevPageUri()));
	assertTrue(StringUtils.equals(curr, result.getCurrentPageUri()));
	checkPartOfUrls(result, profile);

	}

    @Test
    public void testPaginationForOddNumberResults() throws Exception {
	updateResultSetItems(57);
	int lastPage = userSetService.getLastPage(resultSet.getResultSize(), userSetQuery.getPageSize());
	assertTrue(lastPage == 5);

	userSetQuery.setPageNr(3);

	LdProfiles profile = LdProfiles.ITEMDESCRIPTIONS;
	String requestUrl = REQUEST_URL + "?profile=" + profile.getRequestParamValue();
	String first = userSetService.buildPageUrl(requestUrl, 0, userSetQuery.getPageSize(), profile);
	String last = userSetService.buildPageUrl(requestUrl, lastPage, userSetQuery.getPageSize(), profile);
	String next = userSetService.buildPageUrl(requestUrl, userSetQuery.getPageNr() + 1,
		userSetQuery.getPageSize(), profile);
	String prev = userSetService.buildPageUrl(requestUrl , userSetQuery.getPageNr() - 1,
		userSetQuery.getPageSize(), profile);
	String curr = userSetService.buildPageUrl(requestUrl, userSetQuery.getPageNr(),
		userSetQuery.getPageSize(), profile);

	BaseUserSetResultPage<?> result = userSetService.buildResultsPage(userSetQuery, resultSet, requestUrl, "",
			Collections.singletonList(profile), authentication);

	assertTrue(StringUtils.equals(first, result.getPartOf().getFirst()));
	assertTrue(StringUtils.equals(last, result.getPartOf().getLast()));
	assertTrue(StringUtils.equals(next, result.getNextPageUri()));
	assertTrue(StringUtils.equals(prev, result.getPrevPageUri()));
	assertTrue(StringUtils.equals(curr, result.getCurrentPageUri()));
	checkPartOfUrls(result, profile);
    }

	/**
	 * 'partOf' should also contain profile
	 * @param result
	 * @param profile
	 */
	private void checkPartOfUrls(BaseUserSetResultPage<?> result, LdProfiles profile) {
    	assertTrue(StringUtils.contains(result.getPartOf().getFirst(), "profile="+profile.getRequestParamValue()));
		assertTrue(StringUtils.contains(result.getPartOf().getLast(), "profile="+profile.getRequestParamValue()));
    }

}
