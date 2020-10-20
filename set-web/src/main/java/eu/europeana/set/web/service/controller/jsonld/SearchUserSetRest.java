package eu.europeana.set.web.service.controller.jsonld;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.europeana.api.common.config.swagger.SwaggerSelect;
import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.exception.InternalServerException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.web.http.SwaggerConstants;
import eu.europeana.set.web.search.BaseUserSetResultPage;
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

    @GetMapping(value = {"/set/search", "/set/search.json",
            "/set/search.jsonld"}, produces = {HttpHeaders.CONTENT_TYPE_JSONLD_UTF8,
            HttpHeaders.CONTENT_TYPE_JSON_UTF8})
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
            StringBuilder requestURL = new StringBuilder(request.getRequestURL().toString());

            @SuppressWarnings("rawtypes")
            BaseUserSetResultPage resultsPage;
            resultsPage = getUserSetService().buildResultsPage(searchQuery, results,
                    requestURL, request.getQueryString(), profile, authentication);

            String jsonLd = serializeResultsPage(resultsPage);

            // build response
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
            // removed in #EA-763 and specifications
            // //headers.add(HttpHeaders.VARY, HttpHeaders.ACCEPT);
            headers.add(HttpHeaders.ALLOW, HttpHeaders.ALLOW_GET);

            return new ResponseEntity<>(jsonLd, headers, HttpStatus.OK);

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
}
