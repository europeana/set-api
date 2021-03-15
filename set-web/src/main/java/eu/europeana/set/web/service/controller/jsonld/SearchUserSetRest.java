package eu.europeana.set.web.service.controller.jsonld;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.ListUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import eu.europeana.api.common.config.UserSetI18nConstants;
import eu.europeana.api.common.config.swagger.SwaggerSelect;
import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.exception.InternalServerException;
import eu.europeana.api.commons.web.exception.ParamValidationException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.set.definitions.config.UserSetConfigurationImpl;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.web.exception.request.RequestValidationException;
import eu.europeana.set.web.http.SwaggerConstants;
import eu.europeana.set.web.http.UserSetHttpHeaders;
import eu.europeana.set.web.model.search.BaseUserSetResultPage;
import eu.europeana.set.web.model.search.ItemIdsResultPage;
import eu.europeana.set.web.search.UserSetLdSerializer;
import eu.europeana.set.web.search.UserSetQueryBuilder;
import eu.europeana.set.web.service.controller.BaseRest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Controller
@SwaggerSelect
@Api(tags = "User Set Discovery API")
public class SearchUserSetRest extends BaseRest {

    UserSetQueryBuilder queryBuilder;

    public synchronized UserSetQueryBuilder getQueryBuilder() {
	if (queryBuilder == null) {
	    queryBuilder = new UserSetQueryBuilder();
	}
	return queryBuilder;
    }

    @GetMapping(value = { "/set/search", "/set/search.json", "/set/search.jsonld" }, produces = {
	    HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8 })
    @ApiOperation(notes = SwaggerConstants.SEARCH, value = "Search user sets", nickname = "searchUserSet", response = java.lang.Void.class)
    public ResponseEntity<String> searchUserSet(
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_QUERY, required = true) String query,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_QF, required = false) String[] qf,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_SORT, required = false) String sort,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_PAGE, required = false, defaultValue = "0") int page,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_PAGE_SIZE, required = false, defaultValue = ""
		    + CommonApiConstants.DEFAULT_PAGE_SIZE) int pageSize,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_PROFILE, required = false, defaultValue = CommonApiConstants.PROFILE_MINIMAL) String profileStr,
	    HttpServletRequest request) throws HttpException {

	try {

	    // authorization
	    Authentication authentication = verifyReadAccess(request);

	    // valdidate params
	    LdProfiles profile = getProfile(profileStr, request);

	    UserSetQuery searchQuery = getQueryBuilder().buildUserSetQuery(query, qf, sort, page, pageSize);

	    ResultSet<? extends UserSet> results = getUserSetService().search(searchQuery, profile, authentication);
	    String requestURL = request.getRequestURL().toString();

	    @SuppressWarnings("rawtypes")
	    BaseUserSetResultPage resultsPage;
	    resultsPage = getUserSetService().buildResultsPage(searchQuery, results, requestURL,
		    request.getQueryString(), profile, authentication);

	    String jsonLd = serializeResultsPage(resultsPage);
	    return buildSearchResponse(jsonLd);

	} catch (HttpException e) {
	    throw e;
	} catch (IOException | RuntimeException e) {
	    throw new InternalServerException(e);
	}
    }

    @SuppressWarnings("rawtypes")
    private String serializeResultsPage(BaseUserSetResultPage resultsPage) throws IOException {
	UserSetLdSerializer serializer = new UserSetLdSerializer();
	return serializer.serialize(resultsPage);
    }

    private ResponseEntity<String> buildSearchResponse(String jsonLd) {
	// build response
	MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
	headers.add(HttpHeaders.VARY, HttpHeaders.ACCEPT);
	headers.add(HttpHeaders.VARY, HttpHeaders.PREFER);
	headers.add(HttpHeaders.ALLOW, HttpHeaders.ALLOW_GET);

	return new ResponseEntity<>(jsonLd, headers, HttpStatus.OK);
    }

    @GetMapping(value = { "/set/{identifier}/search", "/set/{identifier}/search.json",
	    "/set/{identifier}/search.jsonld" }, produces = { HttpHeaders.CONTENT_TYPE_JSONLD_UTF8,
		    HttpHeaders.CONTENT_TYPE_JSON_UTF8 })
    @ApiOperation(notes = SwaggerConstants.SEARCH, value = "Search user sets", nickname = "searchUserSet", response = java.lang.Void.class)
    public ResponseEntity<String> searchItemsInSet(
	    @PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_QUERY, required = true, defaultValue = UserSetQueryBuilder.SEARCH_ALL) String query,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_QF, required = false) String[] qf,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_PAGE, required = false, defaultValue = "0") int page,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_PAGE_SIZE, required = false, defaultValue = ""
		    + UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE) int pageSize,
//    @RequestParam(value = CommonApiConstants.QUERY_PARAM_PROFILE, required = false, defaultValue = CommonApiConstants.PROFILE_MINIMAL) String profileStr,
	    HttpServletRequest request) throws HttpException {

	try {
	    // authorization
	    Authentication authentication = verifyReadAccess(request);

	    if (!UserSetQueryBuilder.isSearchAllQuery(query)) {
		throw new ParamValidationException(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
			UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE, new String[] { "query", query
				+ " Currently only * is supported as query, use qf for provinding the items list." });
	    }

	    // parses and validates qf
	    List<String> itemIds = buildItemIdsList(query, qf);

	    // retrieve an existing user set based on its identifier
	    UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

	    // for the time being not supported for open sets
	    if (existingUserSet.isOpenSet()) {
		throw new RequestValidationException(UserSetI18nConstants.USER_SET_OPERATION_NOT_ALLOWED,
			new String[] { "Search item in set", "open" });
	    }

	    // check visibility level for given user
	    if (existingUserSet.isPrivate()) {
		getUserSetService().verifyOwnerOrAdmin(existingUserSet, authentication, false);
	    }

	    @SuppressWarnings("unchecked")
	    List<String> filtered = (itemIds != null)? ListUtils.retainAll(existingUserSet.getItems(), itemIds) : existingUserSet.getItems();
	    ItemIdsResultPage resultPage = getUserSetService().buildItemIdsResultsPage(filtered, page, pageSize,
		    request);

	    UserSetLdSerializer serializer = new UserSetLdSerializer();
	    String jsonLd = serializer.serialize(resultPage);

	    // build response
	    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
	    headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
	    headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_RESOURCE);
	    headers.add(HttpHeaders.ALLOW,  HttpHeaders.ALLOW_GET);

	    return new ResponseEntity<>(jsonLd, headers, HttpStatus.OK);
	} catch (HttpException e) {
	    throw e;
	} catch (IOException | RuntimeException e) {
	    throw new InternalServerException(e);
	}
    }

    private List<String> buildItemIdsList(String query, String[] qf) throws ParamValidationException {
	if (qf == null || qf.length == 0) {
	    return null;
	}

	final String ITEM_PREFIX = WebUserSetFields.ITEM + ":";
	List<String> itemIds = new ArrayList<String>(qf.length);
	String recordId;
	for (int i = 0; i < qf.length; i++) {
	    if (!qf[i].contains(ITEM_PREFIX)) {
		throw new ParamValidationException(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
			UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE, new String[] { "qf", qf[i] });
	    }
	    recordId = qf[i].replace(ITEM_PREFIX, "").trim();
	    itemIds.add(UserSetUtils.buildItemUrl(recordId));
	}
	return itemIds;
    }

}
