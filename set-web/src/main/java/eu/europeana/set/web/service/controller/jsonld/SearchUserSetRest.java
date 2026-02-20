package eu.europeana.set.web.service.controller.jsonld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import eu.europeana.api.commons_sb3.definitions.iiif.AcceptUtils;
import eu.europeana.api.commons_sb3.definitions.search.ResultSet;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidBodyException;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidParamException;
import eu.europeana.set.definitions.config.UserSetConfigurationImpl;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.search.UserSetFacetQuery;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.ProfileConstants;
import eu.europeana.set.definitions.model.vocabulary.SetPageProfile;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.web.config.UserSetI18nConstants;
import eu.europeana.set.web.exception.request.RequestValidationException;
import eu.europeana.set.web.http.UserSetHttpHeaders;
import eu.europeana.set.web.model.search.BaseUserSetResultPage;
import eu.europeana.set.web.search.UserSetLdSerializer;
import eu.europeana.set.web.search.UserSetQueryBuilder;
import eu.europeana.set.web.service.controller.BaseRest;
import jakarta.servlet.http.HttpServletRequest;

import static eu.europeana.api.commons_sb3.definitions.http.HttpHeaders.*;

@RestController
public class SearchUserSetRest extends BaseRest {

  UserSetQueryBuilder queryBuilder;

  public synchronized UserSetQueryBuilder getQueryBuilder() {
    if (queryBuilder == null) {
      queryBuilder = new UserSetQueryBuilder();
    }
    return queryBuilder;
  }

  @GetMapping(value = {"/set/search", "/set/search.json", "/set/search.jsonld"},
      produces = {CONTENT_TYPE_JSONLD_UTF8, CONTENT_TYPE_JSON_UTF8})
  public ResponseEntity<String> searchUserSet(
      @RequestParam(value = CommonApiConstants.QUERY_PARAM_QUERY, required = true) String query,
      @RequestParam(value = CommonApiConstants.QUERY_PARAM_QF, required = false) String[] qf,
      @RequestParam(value = CommonApiConstants.QUERY_PARAM_SORT, required = false) String sort,
      @RequestParam(value = CommonApiConstants.QUERY_PARAM_PAGE, required = false,
          defaultValue = "" + WebUserSetFields.DEFAULT_PAGE) String page,
      @RequestParam(value = CommonApiConstants.QUERY_PARAM_PAGE_SIZE, required = false,
          defaultValue = "" + CommonApiConstants.DEFAULT_PAGE_SIZE) String pageSize,
      @RequestParam(value = CommonApiConstants.QUERY_PARAM_FACET, required = false) String facet,
      @RequestParam(value = "facet.limit", required = false, defaultValue = "50") int facetLimit,
      @RequestParam(value = CommonApiConstants.QUERY_PARAM_PROFILE, required = false,
          defaultValue = ProfileConstants.VALUE_PARAM_ITEMS_META) String profileStr,
      HttpServletRequest request) throws EuropeanaApiException {
     // authorization
      Authentication authentication = verifyReadAccess(request);

      //TODO: temporary fix to remove later
      if(profileStr.contains(ProfileConstants.VALUE_PARAM_STANDARD)) {
        profileStr=profileStr.replace(ProfileConstants.VALUE_PARAM_STANDARD, ProfileConstants.VALUE_PARAM_ITEMS_META);
      }
      
      // validate params - profile
      List<SetPageProfile> profiles = getProfilesFromRequest(profileStr, request);
      validateMultipleProfiles(profiles, profileStr);
   // get profile for pagination urls and item Page
      SetPageProfile serializationProfile = getUserSetService().getProfileForPagination(profiles);
      if(serializationProfile == null) {
        //if only technical profiles included in request, append the default profile
        serializationProfile = SetPageProfile.ITEMS_META;
        profiles.add(serializationProfile);
      }

      // create facet query and validate facet - if profile is facets
      UserSetFacetQuery facetQuery = null;
      if (profiles.contains(SetPageProfile.FACETS)) {
        facetQuery = getQueryBuilder().buildUserSetFacetQuery(facet, facetLimit);
      }
      
      Integer pageNr = WebUserSetRequestUtils.parsePageNumber(page, -1);
      
      int maxPageSize =
          getConfiguration().getMaxPageSize(serializationProfile.getProfileParamValue());
      
      Integer pageItems = WebUserSetRequestUtils.getPageSizeOrDefault(pageSize, maxPageSize,  UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE);

      
      //validate the search params and build the search query
      UserSetQuery searchQuery =
          getQueryBuilder().buildUserSetQuery(query, qf, sort, pageNr, pageItems, getConfiguration());
      ResultSet<? extends UserSet> results =
          getUserSetService().search(searchQuery, facetQuery, profiles, authentication);
      String requestURL = request.getRequestURL().toString();

      @SuppressWarnings("rawtypes")
      BaseUserSetResultPage resultsPage;
      resultsPage = getUserSetService().buildResultsPage(searchQuery, results, requestURL,
          request.getQueryString(), profiles, authentication);

      String jsonLd = serializeResultsPage(resultsPage);
      return buildSearchResponse(jsonLd);
  }

  @SuppressWarnings("rawtypes")
  private String serializeResultsPage(BaseUserSetResultPage resultsPage) throws EuropeanaApiException {
    UserSetLdSerializer serializer = new UserSetLdSerializer();
    return serializer.serialize(resultsPage);
  }

  private ResponseEntity<String> buildSearchResponse(String jsonLd) {
    // build response
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
    headers.add(UserSetHttpHeaders.VARY, AcceptUtils.ACCEPT);
    headers.add(UserSetHttpHeaders.VARY, PREFER);
    headers.add(ALLOW, ALLOW_GET);

    return new ResponseEntity<>(jsonLd, headers, HttpStatus.OK);
  }

  @GetMapping(
      value = {"/set/{identifier}/search", "/set/{identifier}/search.json",
          "/set/{identifier}/search.jsonld"},
      produces = {CONTENT_TYPE_JSONLD_UTF8, CONTENT_TYPE_JSON_UTF8})
  public ResponseEntity<String> searchItemsInSet(
      @PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
      @RequestParam(value = CommonApiConstants.QUERY_PARAM_QUERY, required = true,
          defaultValue = UserSetQueryBuilder.SEARCH_ALL) String query,
      @RequestParam(value = CommonApiConstants.QUERY_PARAM_QF, required = false) String[] qf,
      @RequestParam(value = CommonApiConstants.QUERY_PARAM_PAGE, required = false,
          defaultValue = "" + WebUserSetFields.DEFAULT_PAGE) String page,
      @RequestParam(value = CommonApiConstants.QUERY_PARAM_PAGE_SIZE, required = false,
          defaultValue = "" + UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE) String pageSize,
       @RequestParam(value = CommonApiConstants.QUERY_PARAM_PROFILE, required = false,
       defaultValue = ProfileConstants.VALUE_PARAM_ITEMS) String profileStr,
      HttpServletRequest request) throws EuropeanaApiException {
     // authorization
      Authentication authentication = verifyReadAccess(request);

      if (!UserSetQueryBuilder.isSearchAllQuery(query)) {
        throw new InvalidBodyException(Collections.singletonMap(UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE,
            Arrays.asList("query", query
                + " Currently only * is supported as query, use qf for provinding the items list.")));
      }
      
      // validate params - profile
      List<SetPageProfile> profiles = getProfilesFromRequest(profileStr, request);
      validateMultipleProfiles(profiles, profileStr);
      
      // get profile for pagination urls and item Page
      SetPageProfile profile = getUserSetService().getProfileForPagination(profiles);
      if(profile == null) {
        //if only technical profiles included in request, append the default profile
        profile = SetPageProfile.ITEMS;
        profiles.add(profile);
      }

      // parses and validates qf
      List<String> itemIds = buildItemIdsList(qf);

      // retrieve an existing user set based on its identifier
      UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

      // for the time being not supported for open sets
      if (existingUserSet.isOpenSet()) {
        throw new RequestValidationException(UserSetI18nConstants.USER_SET_OPERATION_NOT_ALLOWED,
                Arrays.asList("Search item in set", "open"));
      }

      // check visibility level for given user
      if (existingUserSet.isPrivate()) {
        getUserSetService().verifyOwnerOrAdmin(existingUserSet, authentication, false);
      }

      List<String> filtered;
      if (itemIds != null && !itemIds.isEmpty()) {
        filtered = new ArrayList<String>(existingUserSet.getItems());
        filtered.retainAll(itemIds);   
      } else {
        filtered = Collections.emptyList();
      }
 
      Integer pageNr = WebUserSetRequestUtils.parsePageNumber(page, -1);
      
      int maxPageSize =
          getConfiguration().getMaxPageSize(profile.getProfileParamValue());
      
      Integer pageItems = WebUserSetRequestUtils.getPageSizeOrDefault(pageSize, maxPageSize,  UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE);
      
      BaseUserSetResultPage<String> resultPage = getUserSetService().buildRecodsResultsPage(identifier,
          filtered, pageNr, pageItems, profile, request, authentication);
      
      UserSetLdSerializer serializer = new UserSetLdSerializer();
      String jsonLd = serializer.serialize(resultPage);

      // build response
      MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
      headers.add(LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
      headers.add(LINK, UserSetHttpHeaders.VALUE_BASIC_RESOURCE);
      headers.add(ALLOW, ALLOW_GET);

      return new ResponseEntity<>(jsonLd, headers, HttpStatus.OK);

  }

  private List<String> buildItemIdsList(String[] qf) throws InvalidParamException {
    if (qf == null || qf.length == 0) {
      return null;
    }

    final String ITEM_PREFIX = WebUserSetFields.ITEM + ":";
    List<String> itemIds = new ArrayList<String>(qf.length);
    String recordId;
    for (int i = 0; i < qf.length; i++) {
      if (!qf[i].contains(ITEM_PREFIX)) {
        throw new InvalidParamException(Arrays.asList("qf", "valid value", qf[i]));
      }
      recordId = qf[i].replace(ITEM_PREFIX, "").trim();
      itemIds.add(UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), recordId));
    }
    return itemIds;
  }

}