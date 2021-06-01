package eu.europeana.set.web.utils;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.search.SearchApiRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class UserSetSearchApiUtils {

    /**
     * Will create the Serach Api post request url
     * eg : https://api.europeana.eu/record/v2/search.json?wskey=api2demo
     *
     * @param userSet
     * @param apiKey
     * @return
     */
    public String buildSearchApiPostUrl(UserSet userSet, String apiKey, String searchUrl) {
        StringBuilder url = new StringBuilder();
        if (!userSet.isOpenSet()) {
            url.append(getBaseSearchUrl(searchUrl));
        } else {
            url.append(StringUtils.substringBefore(userSet.getIsDefinedBy(), "?"));
        }
        // add apikey
        url.append('?').append(CommonApiConstants.PARAM_WSKEY).append('=').append(apiKey);
        return url.toString();
    }

    /**
     * Returns the Search APi post Request body
     * @param userSet
     * @param pageSize
     * @return
     */
    public SearchApiRequest buildSearchApiPostBody(UserSet userSet, String sort, String sortOrder, int pageNr, int pageSize) {
        if (!userSet.isOpenSet()) {
            return buildSearchApiPostBodyForClosedSets(userSet, pageSize);
        }
        SearchApiRequest searchApiRequest = new SearchApiRequest();
        // remove pagination and ordering
        Integer start = pageNr * pageSize + 1;

        searchApiRequest.setQuery(getQueryParamFromURL(userSet.getIsDefinedBy()));
        searchApiRequest.setStart(start);
        searchApiRequest.setRows(pageSize);

        if(sort != null && sortOrder == null) {
            searchApiRequest.setSort(new String[]{sort});
        }
        if (sort != null && sortOrder != null) {
            searchApiRequest.setSort(new String[]{sort + " " + sortOrder});
        }
        return searchApiRequest;
    }

    /**
     * This method retrieves item ids from the closed userSet to build SearchApiRequest.
     * e.g.
     * {query='europeana_id:("/165/https___bibdigital_rjb_csic_es_idviewer_11929_40" OR "/2020903/KKSgb2947_97")',
     * start=1, rows=5, sort=europeana_id desc}
     *
     * @param userSet
     * @param pageSize
     * @return
     * @throws HttpException
     */
    SearchApiRequest buildSearchApiPostBodyForClosedSets(UserSet userSet, int pageSize) {
        // use them to build the search query for retrieving item descriptions
        // europeana_id is in format /collectionId/recordId, this can be easily
        // extracted from the
        // full record ID by removing the base URL http://data.europeana.eu/item
        // e.g. europeana_id:("/08641/1037479000000476635" OR
        // "/08641/1037479000000476943")
        SearchApiRequest searchApiRequest = new SearchApiRequest();
        String id;
        String fullId;
        int maxItems = Math.min(userSet.getItems().size(), pageSize);

        StringBuilder query = new StringBuilder(100);
        query.append("europeana_id:(");
        for (int i = 0; i < maxItems; i++) {
            fullId = userSet.getItems().get(i);
            if (i > 0) {
                query.append(" OR ");
            }
            id = fullId.replace(WebUserSetFields.BASE_ITEM_URL, ""); // .replace("/", "%2F");
            query.append('"').append('/').append(id).append('"');
        }
        // close bracket
        query.append(')');
        searchApiRequest.setQuery(query.toString());
        searchApiRequest.setRows(maxItems);

        return searchApiRequest;
    }

    /**
     * This method extracts base URL from the search URL
     *
     * @param searchUrl
     * @return base URL
     */
    public String getBaseSearchUrl(String searchUrl) {
        String res = searchUrl;

        int endPos = searchUrl.indexOf('?');
        if (endPos >= 0) {
            res = searchUrl.substring(0, endPos);
        }
        return res;
    }

    /**
     * Returns the query param value from the url passed
     * @param url
     * @return
     */
    private static String getQueryParamFromURL(String url) {
        // decode the url
        String decodedUrl = java.net.URLDecoder.decode(url, StandardCharsets.UTF_8);
        // get the query param value from the getIsDefinedBy
        List<String> queryParam = UriComponentsBuilder.fromUriString(decodedUrl).build().getQueryParams()
                .get(CommonApiConstants.QUERY_PARAM_QUERY);

        StringBuilder query = new StringBuilder();
        if(queryParam != null && !queryParam.isEmpty()) {
            // form the query param for Search
            for(String queryValue : queryParam) {
                query.append(queryValue);
            }
        }
        return query.toString();
    }

}
