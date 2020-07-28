package eu.europeana.set.web.service.controller.jsonld;

import eu.europeana.api.common.config.I18nConstants;
import eu.europeana.api.common.config.swagger.SwaggerSelect;
import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.exception.InternalServerException;
import eu.europeana.api.commons.web.exception.ParamValidationException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.fields.WebUserSetModelFields;
import eu.europeana.set.web.http.SwaggerConstants;
import eu.europeana.set.web.search.BaseUserSetResultPage;
import eu.europeana.set.web.search.UserSetLdSerializer;
import eu.europeana.set.web.search.UserSetQueryBuilder;
import eu.europeana.set.web.service.controller.BaseRest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@SwaggerSelect
@Api(tags = "User Set Discovery API", description = " ")
public class SearchUserSetRest extends BaseRest {

    UserSetQueryBuilder queryBuilder;

    public UserSetQueryBuilder getQueryBuilder() {
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

            UserSetQuery searchQuery = buildUserSetQuery(query, sort, page, pageSize);

            ResultSet<? extends UserSet> results = getUserSetService().search(searchQuery, profile);
            @SuppressWarnings("rawtypes")
            BaseUserSetResultPage resultsPage;
            resultsPage = getUserSetService().buildResultsPage(searchQuery, results,
                    request.getRequestURL(), request.getQueryString(), profile, authentication);

            String jsonLd = serializeResultsPage(resultsPage, profile);

            // build response
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
            // removed in #EA-763 and specifications
            // //headers.add(HttpHeaders.VARY, HttpHeaders.ACCEPT);
            headers.add(HttpHeaders.ALLOW, HttpHeaders.ALLOW_GET);

            return new ResponseEntity<>(jsonLd, headers, HttpStatus.OK);

        } catch (HttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException(e);
        }
    }

    private UserSetQuery buildUserSetQuery(String query, String sort, int page, int pageSize) throws ParamValidationException {

        Map<String, String> queryParts = parse(query);
        String visibility = null;
        if (queryParts.containsKey(WebUserSetModelFields.VISIBILITY)) {
            // only for sorting WebUserSetFields.MODIFIED
            visibility = queryParts.get(WebUserSetModelFields.VISIBILITY);
            if (visibility != null && !VisibilityTypes.isValid(visibility)) {
                throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
                        new String[]{"invalid value for search field " + WebUserSetModelFields.VISIBILITY, visibility});
            }
        }

        String type = null;
        if (queryParts.containsKey(WebUserSetFields.TYPE)) {
            type = queryParts.get(WebUserSetFields.TYPE);
            if (type != null && !UserSetTypes.isValid(type)) {
                throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
                        new String[]{"invalid value for search field " + WebUserSetFields.TYPE, type});
            }
        }

        String creatorId = null;
        if (queryParts.containsKey(WebUserSetModelFields.CREATOR)) {
            creatorId = queryParts.get(WebUserSetModelFields.CREATOR);
            if (!creatorId.startsWith("http://")) {
                creatorId = getUserSetService().buildCreatorUri(creatorId);
            }
        }

        if (sort != null) {
            // TODO: exception handling
            // throw new RuntimeException();
            ;
        }
        return getQueryBuilder().buildSearchQuery(query, creatorId, visibility, type, sort, page, pageSize);
    }

    private Map<String, String> parse(String query) throws ParamValidationException {
        Map<String, String> res = new HashMap<>();
        if ("*".equals(query) || "*:*".equals(query)) {
            // no filters
            return res;
        }
        String toParse = query;
        String separator = ":";
        String space = " ";
        String field;
        String value;

        List<String> suportedFields = Arrays
                .asList(WebUserSetModelFields.CREATOR, WebUserSetModelFields.VISIBILITY, WebUserSetFields.TYPE);

        while (toParse.contains(separator)) {
            field = StringUtils.substringBefore(toParse, separator);
            toParse = StringUtils.substringAfter(toParse, separator);
            if (!suportedFields.contains(field)) {
                // invalid field name
                throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
                        new String[]{"invalid field name in search query", field});
            }

            value = StringUtils.substringBefore(toParse, space);
            toParse = StringUtils.substringAfter(toParse, space);
            if (value.contains(separator) || StringUtils.isBlank(value)) {
                // invalid seearch value
                throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
                        new String[]{"invalid formatting of search query for field " + field, value});
            }
            res.put(field, value);
        }

        return res;
    }

    @SuppressWarnings("rawtypes")
    private String serializeResultsPage(BaseUserSetResultPage resultsPage, LdProfiles profile) throws IOException {
        UserSetLdSerializer serializer = new UserSetLdSerializer();
        return serializer.serialize(resultsPage);
    }
}
