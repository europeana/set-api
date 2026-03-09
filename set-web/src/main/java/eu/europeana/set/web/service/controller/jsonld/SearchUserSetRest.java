package eu.europeana.set.web.service.controller.jsonld;

import static eu.europeana.api.commons_sb3.definitions.http.HttpHeaders.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import eu.europeana.api.commons_sb3.definitions.iiif.AcceptUtils;
import eu.europeana.api.commons_sb3.definitions.search.Query;
import eu.europeana.api.commons_sb3.definitions.search.ResultSet;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.commons_sb3.error.EuropeanaI18nApiException;
import eu.europeana.api.commons_sb3.error.config.ErrorConfig;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidBodyException;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidParamException;
import eu.europeana.api.commons_sb3.error.exceptions.MissingParamException;
import eu.europeana.set.definitions.config.UserSetConfigurationImpl;
import eu.europeana.set.definitions.exception.UserSetProfileValidationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.search.UserSetFacetQuery;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.ProfileConstants;
import eu.europeana.set.definitions.model.vocabulary.SetPageProfile;
import eu.europeana.set.definitions.model.vocabulary.UserSetProfile;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.web.config.UserSetI18nConstants;
import eu.europeana.set.web.exception.request.RequestValidationException;
import eu.europeana.set.web.http.UserSetHttpHeaders;
import eu.europeana.set.web.model.SearchInSetQuery;
import eu.europeana.set.web.model.search.BaseUserSetResultPage;
import eu.europeana.set.web.search.UserSetLdSerializer;
import eu.europeana.set.web.search.UserSetQueryBuilder;
import eu.europeana.set.web.service.controller.BaseRest;
import jakarta.servlet.http.HttpServletRequest;

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

    List<SetPageProfile> profiles = getSearchProfiles(profileStr, request);
    SetPageProfile serializationProfile = getSerializationProfile(profiles);

    // create facet query and validate facet - if profile is facets
    UserSetFacetQuery facetQuery = null;
    if (profiles.contains(SetPageProfile.FACETS)) {
      facetQuery = getQueryBuilder().buildUserSetFacetQuery(facet, facetLimit);
    }

    Integer pageNr = WebUserSetRequestUtils.parsePageNumber(page, -1);

    int maxPageSize =
        getConfiguration().getMaxPageSize(serializationProfile.getProfileParamValue());

    Integer pageItems = WebUserSetRequestUtils.getPageSizeOrDefault(pageSize, maxPageSize,
        UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE);


    // validate the search params and build the search query
    UserSetQuery searchQuery =
        getQueryBuilder().buildUserSetQuery(query, qf, sort, pageNr, pageItems, getConfiguration());
    ResultSet<? extends UserSet> results =
        getUserSetService().search(searchQuery, facetQuery, profiles, authentication);
    String requestURL = request.getRequestURL().toString();

    @SuppressWarnings("rawtypes")
    BaseUserSetResultPage resultsPage;
    resultsPage = getUserSetService().buildResultsPage(searchQuery, results, requestURL,
        request.getQueryString(), serializationProfile, profiles, authentication);

    String jsonLd = serializeResultsPage(resultsPage);
    return buildSearchResponse(jsonLd);
  }

  SetPageProfile getSerializationProfile(List<SetPageProfile> profiles) {
    // get profile for pagination urls and item Page
    SetPageProfile serializationProfile = getUserSetService().getProfileForPagination(profiles);
    if (serializationProfile == null) {
      // if only technical profiles included in request, append the default profile
      serializationProfile = SetPageProfile.ITEMS_META;
      profiles.add(serializationProfile);
    }
    return serializationProfile;
  }

  List<SetPageProfile> getSearchProfiles(String profileStr, HttpServletRequest request)
      throws EuropeanaI18nApiException {
    // TODO: temporary fix to remove later
    if (profileStr.contains(ProfileConstants.VALUE_PARAM_STANDARD)) {
      profileStr = profileStr.replace(ProfileConstants.VALUE_PARAM_STANDARD,
          ProfileConstants.VALUE_PARAM_ITEMS_META);
    }

    // validate params - profile
    List<SetPageProfile> profiles = getProfilesFromRequest(profileStr, request);
    validateMultipleProfiles(profiles, profileStr);
    return profiles;
  }

  @SuppressWarnings("rawtypes")
  private String serializeResultsPage(BaseUserSetResultPage resultsPage)
      throws EuropeanaApiException {
    UserSetLdSerializer serializer = new UserSetLdSerializer();
    return serializer.serialize(resultsPage);
  }

  private ResponseEntity<String> buildSearchResponse(String jsonLd, HttpServletRequest request) {
    // build response
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
    headers.add(UserSetHttpHeaders.VARY, AcceptUtils.ACCEPT);
    headers.add(UserSetHttpHeaders.VARY, PREFER);
    headers.add(ALLOW, createAllowHeader(request));

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
      throw new InvalidBodyException(Collections.singletonMap(
          UserSetI18nConstants.USERSET_VALIDATION_PROPERTY_VALUE, Arrays.asList("query", query
              + " Currently only * is supported as query, use qf for provinding the items list.")));
    }

    // validate params - profile
    List<SetPageProfile> profiles = getProfilesFromRequest(profileStr, request);
    validateMultipleProfiles(profiles, profileStr);

    // get profile for pagination urls and item Page
    SetPageProfile profile = getUserSetService().getProfileForPagination(profiles, SetPageProfile.ITEMS);
    

    // parses and validates qf
    List<String> itemIds = buildItemIdsList(qf);

    List<String> filtered = getItemsInSet(identifier, itemIds, authentication);

    Integer pageNr = WebUserSetRequestUtils.parsePageNumber(page, -1);

    Integer pageItems = validatePageSize(pageSize, profile);
      // build response
      MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
      headers.add(LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
      headers.add(LINK, UserSetHttpHeaders.VALUE_BASIC_RESOURCE);
      headers.add(ALLOW, createAllowHeader(request));

    BaseUserSetResultPage<String> resultPage = getUserSetService().buildRecordsResultsPage(
        identifier, filtered, pageNr, pageItems, profile, request, authentication);

    return buildSetPageResponse(resultPage);

  }

  ResponseEntity<String> buildSetPageResponse(BaseUserSetResultPage<String> resultPage)
      throws EuropeanaApiException {
    UserSetLdSerializer serializer = new UserSetLdSerializer();
    String jsonLd = serializer.serialize(resultPage);

    // build response
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
    headers.add(LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
    headers.add(LINK, UserSetHttpHeaders.VALUE_BASIC_RESOURCE);
    headers.add(ALLOW, ALLOW_GET);

    return new ResponseEntity<>(jsonLd, headers, HttpStatus.OK);
  }

  /**
   * verification of the page size
   * @param pageSize the requested page size
   * @param profile the serialization profile (SetPageProfile) 
   * @return valid page size, or default if pageSize is not provided
   * @throws InvalidParamException if requested pare is out of range
   */
  Integer validatePageSize(String pageSize, UserSetProfile profile) throws InvalidParamException {
    int maxPageSize = getConfiguration().getMaxPageSize(profile.getProfileParamValue());

    return WebUserSetRequestUtils.getPageSizeOrDefault(pageSize, maxPageSize,
        UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE);
  }

  List<String> getItemsInSet(String identifier, List<String> itemIds, Authentication authentication)
      throws EuropeanaI18nApiException {
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
    return filtered;
  }

  
  /**
   * Search items in set using post method
   * @param identifier set id
   * @param inSetQuery the search query
   * @param request the original http request
   * @return a Set page response
   * @throws EuropeanaApiException in case of authentication or processing failures
   */
  @PostMapping(
      value = {"/set/{identifier}/search", "/set/{identifier}/search.json",
          "/set/{identifier}/search.jsonld"},
      produces = {CONTENT_TYPE_JSONLD_UTF8, CONTENT_TYPE_JSON_UTF8})
  public ResponseEntity<String> searchInSet(
      @PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
      @RequestBody SearchInSetQuery inSetQuery,
      HttpServletRequest request) throws EuropeanaApiException {
    // authorization
    Authentication authentication = verifyReadAccess(request);

    //verify query param, must be iunset or *
    if(inSetQuery.getQuery() == null) {
      inSetQuery.setQuery(UserSetQueryBuilder.SEARCH_ALL);
    }else if (!UserSetQueryBuilder.isSearchAllQuery(inSetQuery.getQuery())) {
      throw new InvalidBodyException(Collections.singletonMap(
              ErrorConfig.INVALID_PARAM,
              Arrays.asList("query",
                      "Currently only * is supported as query, use qf for providing the items list.",
                      inSetQuery.getQuery())));
    }

    // parses and validates items
    List<String> itemIds = buildItemIdsList(inSetQuery);
    
    SetPageProfile profile = validateProfile(inSetQuery);
    
    Integer pageNr = inSetQuery.getPageNr() > WebUserSetFields.DEFAULT_PAGE ?
        inSetQuery.getPageNr() : WebUserSetFields.DEFAULT_PAGE;
    
    validatePageSize(inSetQuery);
      
    Integer pageItems = validatePageSize(""+ inSetQuery.getPageSize(), profile);

    //verify items in set
    List<String> filtered = getItemsInSet(identifier, itemIds, authentication);
    BaseUserSetResultPage<String> resultPage = getUserSetService().buildRecordsResultsPage(
        identifier, filtered, pageNr, pageItems, profile, request, authentication);

    return buildSetPageResponse(resultPage);

 
  }

  void validatePageSize(Query inSetQuery) {
    //set default pageSize if not provided in request
    final int noPageSize = -1;
    if(noPageSize == inSetQuery.getPageSize()) {
      inSetQuery.setPageSize( UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE);
    }
  }

  SetPageProfile validateProfile(SearchInSetQuery inSetQuery)
      throws EuropeanaI18nApiException {
    SetPageProfile profile;
    if(inSetQuery.getProfile() == null) {
      profile = SetPageProfile.ITEMS;
    } else {
      // validate params - profile
      List<SetPageProfile> profiles;
      try {
        profiles = getProfileHelper().parseProfiles(inSetQuery.getProfile());
      } catch (UserSetProfileValidationException e) {
        List<String> params = List.of(CommonApiConstants.QUERY_PARAM_PROFILE, inSetQuery.getProfile().toString()); 
        throw new InvalidParamException( params, e);
      }
      validateMultipleProfiles(profiles, inSetQuery.getProfile().toString());
      // get profile for pagination urls and item Page
      // when profile param contains only technical profiles, use items as default 
      profile = getUserSetService().getProfileForPagination(profiles, SetPageProfile.ITEMS);   
    }
    return profile;
  }
   
  /**
   * build the list of Item Ids based on the record ids included query 
   * @param inSetQuery containing list of record ids
   * @return list of userset item ids
   * @throws InvalidParamException if one of the filters is does not contain "item" as field name, 
   * or MissingParamException if "qf" is not present in the request   
   */
  private List<String> buildItemIdsList(@NonNull SearchInSetQuery inSetQuery) throws EuropeanaApiException {
    if (inSetQuery.getFilters() == null || inSetQuery.getFilters().length == 0) {
      //qf is mandatory for search in set
      throw new MissingParamException(List.of("qf"));
    }
    
    List<String> itemIds = buildItemIdsList(inSetQuery.getFilters());
    for (String recordId : inSetQuery.getFilters()) {
      itemIds.add(UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), recordId));
    }
    
    return itemIds;
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
        throw new InvalidParamException(Arrays.asList("qf", "entries with format 'item:<identifier>'", qf[i]));
      }
      recordId = qf[i].replace(ITEM_PREFIX, "").trim();
      itemIds.add(UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), recordId));
    }
    return itemIds;
  }

}
