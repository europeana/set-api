package eu.europeana.set.web.model.search;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonApiConstants;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.search.SearchApiRequest;

/**
 * Creates the Search api urls and request body
 * @author Srishti Singh
 */
public class SearchApiUtils {

   private static final SearchApiUtils instance = new SearchApiUtils();
  
   private SearchApiUtils() {
     //hide default constructor
   }
   
   
   public static SearchApiUtils getInstance() {
     return instance;
   }
   
    /**
     * Will create the Search Api post request url
     * eg : https://api.europeana.eu/record/v2/search.json
     * 
     * @param userSet the user set for which the 
     * @param searchUrl serach api url
     * @param profile profile requested
     * @return sr api url with params
     */
    public String buildSearchApiUrl(UserSet userSet, String searchUrl, String profile) {
        StringBuilder url = new StringBuilder();
        if (!userSet.isOpenSet()) {
            url.append(getBaseSearchUrl(searchUrl));
        } else {
            url.append(StringUtils.substringBefore(userSet.getIsDefinedBy(), "?"));
        }
        // add profile
        if (profile != null) {
          url.append('?').append(CommonApiConstants.QUERY_PARAM_PROFILE).append('=').append(profile);
        }
        return url.toString();
    }
    
    /**
     * Will create the Search Api post request url
     * eg : https://api.europeana.eu/record/v2/search.json?wskey=api2demo
     * 
     * @param baseSearchApiUrl sr api url
     * @param baseItemUrl base item url
     * @param itemId item id
     * @param profile profile requested
     * @return sr api url for item
     */
    public String buildSearchApiUrlForItem(String baseSearchApiUrl, String baseItemUrl, String itemId, String profile) {
        StringBuilder url = new StringBuilder();
       url.append(getBaseSearchUrl(baseSearchApiUrl));
        // add profile
        if (profile != null) {
          url.append('?').append(CommonApiConstants.QUERY_PARAM_PROFILE).append('=').append(profile);
        }
        
        String europeanaId = itemId.startsWith(baseItemUrl) ? UserSetUtils.extractItemIdentifier(itemId) : itemId;
        final String searchQuery = "europeana_id:\"" + europeanaId + "\"";
        url.append("&query=").append(URLEncoder.encode(searchQuery, StandardCharsets.UTF_8));
        
        return url.toString();
    }

    /**
     * Returns the Search APi post Request body
     * For open set, items requested are only returned using pageNr and pageSize.
     *    The request if formed based on start and rows
     *    ex: pageNr = 0, pageSize, 10 -> {"query":<isDefinedBy>,"qf":null,"start":1,"rows":10,"sort":null}
     *
     * For close-set, Only requested items are queried based on pageNr and pageSize.
     *     Items are taken in the order of the items present in the user set
     *    ex:{"query":"europeana_id:(\"123\" OR \"xyz\" OR \"abc\")","qf":null,"start":1,"rows":3,"sort":null}
     *
     * @param userSet user set
     * @param sort sort fields
     * @param itemDataEndpoint item data endpoitn
     * @param profile  profile requested
     * @param sortOrder sorting order
     * @param pageNr page number
     * @param pageSize page size
     * @return Sr api request
     */
    public SearchApiRequest buildSearchApiPostBody(UserSet userSet, String itemDataEndpoint, String sort,
                                                   String sortOrder, int pageNr, int pageSize, String profile) {
        if (userSet.isOpenSet()) {
          return buildSearchApiPostBodyForOpenSets(userSet, pageNr, pageSize, profile);
        } else {
          return buildSearchApiPostBodyForClosedSets(userSet, itemDataEndpoint, pageNr, pageSize, profile);
        }
    }

    private SearchApiRequest buildSearchApiPostBodyForOpenSets(UserSet userSet, int pageNr, int pageSize, String profile) {
      SearchApiRequest searchApiRequest = new SearchApiRequest();
      
      //overwrite pagination (do no use the one from isDefinedBy)
      Integer start = (pageNr - WebUserSetFields.DEFAULT_PAGE) * pageSize + 1;
      searchApiRequest.setStart(start);
      searchApiRequest.setRows(pageSize);
      
      final MultiValueMap<String, String> queryParams = getQueryParamsFromUrl(userSet.getIsDefinedBy());
      
      //query has cardinality 1 
      searchApiRequest.setQuery(queryParams.getFirst(CommonApiConstants.QUERY_PARAM_QUERY));
      //QF has cardinality 0..n
      List<String> qf = queryParams.get(WebUserSetFields.REQUEST_PARAM_QF);
      searchApiRequest.setQf(qf);
      //reusability has cardinality 1
      searchApiRequest.setReusability(queryParams.get(WebUserSetFields.REQUEST_PARAM_REUSABILITY));
      
      List<String> sort = queryParams.get(CommonApiConstants.QUERY_PARAM_SORT);
      searchApiRequest.setSort(sort);
      
      setProfile(searchApiRequest, profile);
      return searchApiRequest;
    }

    /**
     * This method builds the body of the post request to search API to retireve the descriptions of the records included in user set 
     * e.g.
     * {query='europeana_id:("/165/https___bibdigital_rjb_csic_es_idviewer_11929_40" OR "/2020903/KKSgb2947_97")',
     * start=1, rows=5, sort=europeana_id desc}
     *
     * @param userSet the user set
     * @param itemDataEndpoint the base URL for the item ids
     * @param pageNr the results page to retrieve
     * @param pageSize the number of results to retrieve per page
     * @param profile the profile used for record descriptions
     
     * @param pageSize the number of retrieved results
     * @return the SearchApi request 
     */
    SearchApiRequest buildSearchApiPostBodyForClosedSets(UserSet userSet, String itemDataEndpoint, int pageNr, int pageSize, String profile) {
        final List<String> items = userSet.getItems();
        return buildSearchApiPostBodyForItemIds(items, itemDataEndpoint, pageNr, pageSize, profile);
    }

    /**
     * This method builds the body of the post request to the search API for retrieving the item descriptions
     * @param items list of item ids
     * @param itemDataEndpoint the base URL for the item ids
     * @param pageNr the results page to retrieve
     * @param pageSize the number of results to retrieve per page
     * @param profile the profile used for record descriptions
     * @return the search api request
     */
    public SearchApiRequest buildSearchApiPostBodyForItemIds(final List<String> items,
        String itemDataEndpoint, int pageNr, int pageSize, String profile) {
      SearchApiRequest searchApiRequest = new SearchApiRequest();
      String id;
      String fullId;
      // calculate the indices for the start and end of the items page
      Integer start = (pageNr - WebUserSetFields.DEFAULT_PAGE) * pageSize;
      Integer end = Math.min((start +  pageSize), items.size()); // should not exceed the size of item list

      StringBuilder query = new StringBuilder(100);
      query.append("europeana_id:(");
      for (int i = start; i < end; i++) {
          fullId = items.get(i);
          if (i != start) {
              query.append(" OR ");
          }
          id = fullId.replace(itemDataEndpoint, ""); // replace "/" with "%2F"
          query.append('"').append('/').append(id).append('"');
      }
      // close bracket
      query.append(')');
      searchApiRequest.setQuery(query.toString());
      searchApiRequest.setRows(pageSize);
      setProfile(searchApiRequest, profile);
      return searchApiRequest;
    }

    private void setProfile(SearchApiRequest searchApiRequest, String profile) {
      if(!StringUtils.isEmpty(profile)) {
        String[] searchApiProfile = profile.split(","); 
        searchApiRequest.setProfile(List.of(searchApiProfile));
      }
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
     * Decodes the URL params and extracts the parameter map
     * @param url the string representation of the URL  
     * @return the parameters
     */
    public static MultiValueMap<String, String> getQueryParamsFromUrl(String url) {
      // decode the url
      String decodedUrl = java.net.URLDecoder.decode(url, StandardCharsets.UTF_8);
      // get the query param value from the getIsDefinedBy
      return UriComponentsBuilder.fromUriString(decodedUrl).build().getQueryParams();
    }
}