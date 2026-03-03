package eu.europeana.set.web.service.controller.jsonld;

import static eu.europeana.api.commons_sb3.definitions.http.HttpHeaders.ALLOW;
import static eu.europeana.api.commons_sb3.definitions.http.HttpHeaders.CONTENT_TYPE_JSONLD_UTF8;
import static eu.europeana.api.commons_sb3.definitions.http.HttpHeaders.CONTENT_TYPE_JSON_UTF8;
import static eu.europeana.api.commons_sb3.definitions.http.HttpHeaders.LINK;
import static eu.europeana.api.commons_sb3.definitions.http.HttpHeaders.PREFER;
import static eu.europeana.api.commons_sb3.definitions.http.HttpHeaders.PREFERENCE_APPLIED;
import static eu.europeana.set.definitions.model.vocabulary.WebUserSetFields.*;
import static eu.europeana.set.web.http.UserSetHttpHeaders.CACHE_CONTROL;
import static eu.europeana.set.web.http.UserSetHttpHeaders.VALUE_NO_CAHCHE_STORE_REVALIDATE;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import eu.europeana.api.commons_sb3.definitions.oauth.Operations;
import eu.europeana.api.commons_sb3.definitions.oauth.exception.DateParsingException;
import eu.europeana.api.commons_sb3.definitions.utils.DateUtils;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.commons_sb3.error.EuropeanaI18nApiException;
import eu.europeana.api.commons_sb3.error.config.ErrorConfig;
import eu.europeana.api.commons_sb3.error.exceptions.ApplicationAuthenticationException;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidParamException;
import eu.europeana.set.definitions.config.UserSetConfigurationImpl;
import eu.europeana.set.definitions.exception.UserSetAttributeInstantiationException;
import eu.europeana.set.definitions.exception.UserSetInstantiationException;
import eu.europeana.set.definitions.exception.UserSetValidationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.SetPageProfile;
import eu.europeana.set.definitions.model.vocabulary.SetResourceProfile;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.search.service.SearchApiResponse;
import eu.europeana.set.web.config.UserSetI18nConstants;
import eu.europeana.set.web.exception.request.RequestBodyValidationException;
import eu.europeana.set.web.exception.request.RequestValidationException;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.http.UserSetHttpHeaders;
import eu.europeana.set.web.model.search.CollectionPage;
import eu.europeana.set.web.model.vocabulary.SetOperations;
import eu.europeana.set.web.service.controller.BaseRest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Pattern;

/**
 * This class implements the User Set - REST API
 */

@RestController
public class WebUserSetRest extends BaseRest {

  private static final String INVALID_RECORD_ID_MESSAGE =
      "Invalid record identifier. Only alpha-numeric characters and underscore are allowed";

  public WebUserSetRest() {
    super();
  }
  
  @PostMapping(value = "/set/",
      produces = {CONTENT_TYPE_JSONLD_UTF8, CONTENT_TYPE_JSON_UTF8})
  public ResponseEntity<String> createUserSet(@RequestBody String userSet,
      HttpServletRequest request) throws EuropeanaApiException {
    // validate user - check user credentials (all registered users can create)
    // if invalid respond with HTTP 401 or if unauthorized respond with HTTP 403;
    Authentication authentication = verifyWriteAccess(Operations.CREATE, request);
    return createUserSet(userSet, authentication, request);
  }

  /**
   * This method requests parsing of a user set in JsonLd format to a UserSet object
   * 
   * @param userSetJsonLdStr The user set in JsonLd format
   * @param authentication The authentication object with user identifier
   * @param request HTTP request
   * @return response entity that comprises response body, headers and status code
   * @throws EuropeanaI18nApiException
   */
  protected ResponseEntity<String> createUserSet(String userSetJsonLdStr,
      Authentication authentication, HttpServletRequest request) throws EuropeanaApiException {
    try {

      // parse user set
      UserSet webUserSet = getUserSetService().parseUserSetLd(userSetJsonLdStr);
      
      //ignore items submitted in request
      if(webUserSet.getItems() != null) {
        webUserSet.setItems(null);
      }

      // validate and process the Set description for format and mandatory fields
      // if false respond with HTTP 400
      // store the new Set with its respective id, together with all the containing
      // items
      // following the order given by the list
      // generate an identifier (in sequence) for the Set
      // generate and add a created and modified timestamp to the Set
      // type should be saved now in the database and not generated on the fly during
      // serialization
      UserSet storedUserSet = getUserSetService().createUserSet(webUserSet, authentication);

      doPostRetrieveProcessing(storedUserSet, authentication);

      // only one profile used as default, no validation required
      return buildResponseEntity(storedUserSet,
              SetResourceProfile.META,
              HttpStatus.CREATED,
              // Cache-Control: no-cache, no-store, must-revalidate
              // (could be changed to “private” once user sets can be private)
              Collections.singletonMap(CACHE_CONTROL, VALUE_NO_CAHCHE_STORE_REVALIDATE),
              request);
    } catch ( UserSetValidationException | UserSetAttributeInstantiationException e) {
      throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_CANT_PARSE_BODY,
          Arrays.asList(e.getMessage()), e);
    } catch (UserSetInstantiationException e) {
      throw new EuropeanaI18nApiException(null, null, null, HttpStatus.BAD_REQUEST, UserSetI18nConstants.USERSET_INVALID_BODY, null, e);
    }

  }

  @RequestMapping(value = {"/set/{identifier}", "/set/{identifier}.json", "/set/{identifier}.jsonld"},
          method = {RequestMethod.GET, RequestMethod.HEAD},
      produces = {CONTENT_TYPE_JSONLD_UTF8, CONTENT_TYPE_JSON_UTF8})
  public ResponseEntity<String> getUserSet(
      @PathVariable(value = PATH_PARAM_SET_ID) String identifier,
      @RequestParam(value = CommonApiConstants.QUERY_PARAM_SORT, required = false) String sortField,
      @RequestParam(value = PARAM_SORT_ORDER,
          required = false) String sortOrderField,
      @RequestParam(value = CommonApiConstants.QUERY_PARAM_PAGE, required = false) String page,
      @RequestParam(value = CommonApiConstants.QUERY_PARAM_PAGE_SIZE,
          required = false) String pageSize,
      @RequestParam(value = CommonApiConstants.QUERY_PARAM_PROFILE,
          required = false) String profile,
      HttpServletRequest request) throws EuropeanaApiException {

    Authentication authentication = verifyReadAccess(request);

    // if no pagination requested, apply minimal profile (profiles deprecation)
    if (isSetResourceRequestResponse(page)) {
      return processRetrieveSetRequest(identifier, authentication, request);
    } else {
      return processRetrieveSetPageRequest(identifier, sortField, sortOrderField, page, pageSize,
          profile, authentication, request);
    }
  }

  private ResponseEntity<String> processRetrieveSetPageRequest(String identifier, String sortField,
      String sortOrderField, String page, String pageSize, String profile,
      Authentication authentication, HttpServletRequest request)
      throws EuropeanaApiException {
    Integer pageNr;
    Integer pageItems;
    // validate params - profile
    List<SetPageProfile> profiles = getProfilesFromRequest(profile, request);
    // final boolean noSerializationProfile =
    // profiles.isEmpty() || (profiles.size() == 1 && profiles.contains(SetPageProfile.META));
    if (profiles.isEmpty()) {
      profiles.add(SetPageProfile.ITEMS);
    }

    validateMultipleProfiles(profiles, profile);
    SetPageProfile searializationProfile = getUserSetService().getProfileForPagination(profiles);

    pageNr = WebUserSetRequestUtils.parsePageNumber(page, -1);
    
    int maxPageSize =
        getConfiguration().getMaxPageSize(searializationProfile.getProfileParamValue());
    
    pageItems = WebUserSetRequestUtils.getPageSizeOrDefault(pageSize, maxPageSize,  UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE);

    return getUserSetPage(profiles, identifier, sortField, sortOrderField, pageNr, pageItems,
        authentication, request);
  }


  private ResponseEntity<String> processRetrieveSetRequest(String identifier,
      Authentication authentication, HttpServletRequest request) throws EuropeanaApiException {
    // validate params - profile
    // retrieve a Set based on its identifier - process query
    // if the Set doesn’t exist, respond with HTTP 404
    // if the Set is disabled respond with HTTP 410
      UserSet userSet = getSetAndVerifyAccess(identifier, authentication);
      doPostRetrieveProcessing(userSet, authentication);
      return buildResponseEntity(userSet, SetResourceProfile.META, HttpStatus.OK, null, request);
  }

  private void doPostRetrieveProcessing(UserSet userSet, Authentication authentication) throws  EuropeanaApiException{
    if (userSet.isOpenSet()) {
      SearchApiResponse apiResponse = getUserSetService().retrieveTotalForOpenSets(userSet, authentication);
      userSet.setTotal(apiResponse.getTotal());
    }
  }

  private UserSet getSetAndVerifyAccess(String identifier, Authentication authentication)
      throws EuropeanaI18nApiException {
    UserSet userSet = getUserSetService().getUserSetById(identifier);

    // check visibility level for given user
    if (userSet.isPrivate()) {
      getUserSetService().verifyOwnerOrAdmin(userSet, authentication, false);
    }
    return userSet;
  }

  /**
   * This method retrieves an existing user set identified by given identifier, which is a number in
   * string format.
   * 
   * @param profiles The profile definition
   * @param identifier The identifier
   * @param request HTTP request
   * @return response entity that comprises response body, headers and status code
   * @throws EuropeanaApiException
   */
  private ResponseEntity<String> getUserSetPage(List<SetPageProfile> profiles, String identifier,
      String sort, String sortOrder, Integer pageNr, Integer pageSize,
      Authentication authentication, HttpServletRequest request) throws EuropeanaApiException {
      UserSet userSet = getSetAndVerifyAccess(identifier, authentication);
      // get profile for pagination urls and item Page
      SetPageProfile profile = getProfileHelper().getProfileForPagination(profiles);

      if (mustFetchItems(userSet, profile)) {
        userSet = getUserSetService().fetchUserSetItems(userSet, sort, sortOrder, pageNr, pageSize,
            profile, authentication);
      }
      CollectionPage itemPage =
          getUserSetService().buildCollectionPage(userSet, profile, pageNr, pageSize, request);

      return buildSetPageResponse(itemPage, userSet, profile, request);
  }

  private boolean mustFetchItems(UserSet userSet, SetPageProfile profile) {
    boolean itemDescriptionsProfile = SetPageProfile.ITEMS_META == profile;
    boolean fetchItemsForOpenSet = userSet.isOpenSet() && SetPageProfile.META != profile;
    return itemDescriptionsProfile || fetchItemsForOpenSet;
  }

  @PutMapping(value = {"/set/{identifier}"},
      produces = {CONTENT_TYPE_JSONLD_UTF8, CONTENT_TYPE_JSON_UTF8})
  public ResponseEntity<String> updateUserSet(
      @PathVariable(value = PATH_PARAM_SET_ID) String identifier,
      @RequestBody String userSet, HttpServletRequest request) throws EuropeanaApiException {

    // check user credentials, if invalid respond with HTTP 401,
    Authentication authentication = verifyWriteAccess(Operations.UPDATE, request);
    return updateUserSet(request, authentication, identifier, userSet);
  }

  /**
   * This method validates input values, retrieves user set object and updates it.
   * 
   * @param request
   * @param authentication The Authentication object
   * @param identifier The identifier
   * @param userSetJsonLdStr The user set fields to update in JSON format e.g. title or description
   * @return response entity that comprises response body, headers and status code
   * @throws EuropeanaApiException
   */
  protected ResponseEntity<String> updateUserSet(HttpServletRequest request,
      Authentication authentication, String identifier, String userSetJsonLdStr) throws EuropeanaApiException {

    try {
      // check if the Set exists, if not respond with HTTP 404
      // retrieve an existing user set based on its identifier
      UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

      // only an editor can set the state of a set to "published"
      // and only if the set is in "public" visibility.
      // only owner or admin can update any user set
      getUserSetService().verifyPermissionToUpdate(existingUserSet, authentication, false);

      // check timestamp if provided within the “If-Match” HTTP header, if false
      // respond with HTTP 412
      String eTagOrigin =
          generateETag(existingUserSet.getModified(), FORMAT_JSONLD, getApiVersion());
      checkIfMatchHeader(eTagOrigin, request);

      // parse fields of the new user set to an object
      UserSet newUserSet = getUserSetService().parseUserSetLd(userSetJsonLdStr);
      
      //ignore items submitted in request
      if(newUserSet.getItems() != null) {
        throw new RequestValidationException(UserSetI18nConstants.USERSET_VALIDATION,
                Arrays.asList("Update method is not allowed to update the items list, please use the insert items method (/set/{identifier}/items)"));
      }
     
      // if the Set corresponds to a closed set, replace member items with the new
      // items
      // that are present in the Set description only when a profile is indicated and
      // modified date is set in the service;
      UserSet updatedUserSet =
          getUserSetService().updateUserSet((PersistentUserSet) existingUserSet, newUserSet, authentication);

      // no cache control headers in the response
      return buildResponseEntity(updatedUserSet,
              SetResourceProfile.META,
              HttpStatus.OK,
              Collections.singletonMap(CACHE_CONTROL, null),
              request);

    } catch (UserSetValidationException | UserSetInstantiationException e) {
      throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_CANT_PARSE_BODY, Arrays.asList(e.getMessage()), e);
    }
  }

  /**
   * Will add the last editor at the end of the contributor List
   * 
   * @param existingUserSet
   * @param authentication
   */
  private void addContributorForEntitySet(UserSet existingUserSet, Authentication authentication) {
    if (existingUserSet.isEntityBestItemsSet()
        && getUserSetService().hasEditorRole(authentication)) {
      String userId = getUserSetService().getUserId(authentication);
      // check if contributor is not null, in case if entity set was created by super user
      if (existingUserSet.getContributor() != null) {
        if (existingUserSet.getContributor().contains(userId)) {
          existingUserSet.getContributor().remove(userId);
        }
        existingUserSet.getContributor().add(userId);
      } else {
        existingUserSet.setContributor(Collections.singletonList(userId));
      }
    }

  }

  @PutMapping(value = {"/set/{identifier}/publish"},
      produces = {CONTENT_TYPE_JSONLD_UTF8, CONTENT_TYPE_JSON_UTF8})
  public ResponseEntity<String> publishUserSet(
      @PathVariable(value = PATH_PARAM_SET_ID) String identifier,
      @RequestParam(value = REQUEST_PARAM_ISSUED, required = false) String issued,
      HttpServletRequest request) throws EuropeanaApiException {
    // check user credentials, if invalid respond with HTTP 401,
    // or if unauthorized respond with HTTP 403
    Authentication authentication = verifyWriteAccess(SetOperations.PUBLISH, request);
    Date issuedDate = null;
    if (issued != null) {
      try {
        issuedDate = DateUtils.parseToDate(issued);
      } catch (DateParsingException e) {
        throw new InvalidParamException(Arrays.asList(REQUEST_PARAM_ISSUED, " ", issued), e);
      }
    }

    return publishUnpublishUserSet(identifier, authentication, true, issuedDate, request);
  }

  @PutMapping(value = {"/set/{identifier}/unpublish"},
      produces = {CONTENT_TYPE_JSONLD_UTF8, CONTENT_TYPE_JSON_UTF8})
  public ResponseEntity<String> unpublishUserSet(
      @PathVariable(value = PATH_PARAM_SET_ID) String identifier,
      HttpServletRequest request) throws EuropeanaApiException {
    // check user credentials, if invalid respond with HTTP 401,
    // or if unauthorized respond with HTTP 403
    Authentication authentication = verifyWriteAccess(SetOperations.PUBLISH, request);
    return publishUnpublishUserSet(identifier, authentication, false, null, request);
  }

  protected ResponseEntity<String> publishUnpublishUserSet(String identifier,
      Authentication authentication, boolean publish, Date issued, HttpServletRequest request)
      throws EuropeanaApiException {
      UserSet updatedUserSet =
          getUserSetService().publishUnpublishUserSet(identifier, issued, authentication, publish);

      // build response entity with headers ( no cacahe header)
      return buildResponseEntity(updatedUserSet, SetResourceProfile.META, HttpStatus.OK,
              Collections.singletonMap(CACHE_CONTROL, null),
          request);
  }

  @Deprecated
  @PutMapping(value = {"/set/{identifier}/{datasetId}/{localId}"},
      produces = {CONTENT_TYPE_JSONLD_UTF8, CONTENT_TYPE_JSON_UTF8})
  public ResponseEntity<String> insertItemIntoUserSet(
      @PathVariable(value = PATH_PARAM_SET_ID) String identifier,
      @PathVariable(value = PATH_PARAM_DATASET_ID) @Pattern(
          regexp = UserSetUtils.EUROPEANA_ID_FIELD_REGEX,
          message = INVALID_RECORD_ID_MESSAGE) String datasetId,
      @PathVariable(value = PATH_PARAM_LOCAL_ID) @Pattern(
          regexp = UserSetUtils.EUROPEANA_ID_FIELD_REGEX,
          message = INVALID_RECORD_ID_MESSAGE) String localId,
      @RequestParam(value = PATH_PARAM_POSITION, required = false) String position,
      HttpServletRequest request) throws EuropeanaApiException {
    // check user credentials, if invalid respond with HTTP 401,
    // or if unauthorized respond with HTTP 403
    Authentication authentication = verifyWriteAccess(Operations.UPDATE, request);
    return insertItemIntoUserSet(request, authentication, identifier, datasetId, localId, position);
  }

  @PutMapping(value = {"/set/{identifier}/items"},
      produces = {CONTENT_TYPE_JSONLD_UTF8, CONTENT_TYPE_JSON_UTF8})
  public ResponseEntity<String> insertMultipleItemsIntoUserSet(
      @PathVariable(value = PATH_PARAM_SET_ID) String identifier,
      @RequestParam(value = PATH_PARAM_POSITION, required = false) String position,
      @RequestBody List<String> items, HttpServletRequest request) throws EuropeanaApiException {
    // check user credentials, if invalid respond with HTTP 401,
    // or if unauthorized respond with HTTP 403
    Authentication authentication = verifyWriteAccess(Operations.UPDATE, request);
    return insertMultipleItemsIntoUserSet(request, authentication, identifier, items, position);
  }

  /**
   * This method validates input values, retrieves user set object and inserts item within user set
   * to given position or at the end if no valid position provided.
   * 
   * @param request
   * @param authentication The Authentication object
   * @param identifier The identifier of a user set
   * @param datasetId The identifier of the dataset, typically a number
   * @param localId The local identifier within the provider
   * @param position The position in the existin item list
   * @return response entity that comprises response body, headers and status code
   * @throws EuropeanaApiException
   */
  @Deprecated
  protected ResponseEntity<String> insertItemIntoUserSet(HttpServletRequest request,
      Authentication authentication, String identifier, String datasetId, String localId,
      String position) throws EuropeanaApiException {
    try {
      // check if the Set exists, if not respond with HTTP 404
      // retrieve an existing user set based on its identifier
      UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

      verifyIfClosedSet(existingUserSet);

      // if set is not entity set and position is "pin", throw exception
      if (!existingUserSet.isEntityBestItemsSet()
          && WebUserSetRequestUtils.isPinPosition(position)) {
        throw new RequestValidationException(UserSetI18nConstants.USER_SET_OPERATION_NOT_ALLOWED,
            Arrays.asList("Pinning item ", existingUserSet.getType()));
      }

      // check visibility level for given user
      getUserSetService().verifyPermissionToUpdate(existingUserSet, authentication, true);

      // for entity user sets, add users with 'editor' role as contributors
      addContributorForEntitySet(existingUserSet, authentication);

      // check timestamp if provided within the “If-Match” HTTP header, if false
      // respond with HTTP 412
      String eTagOrigin =
          generateETag(existingUserSet.getModified(), FORMAT_JSONLD, getApiVersion());
      checkIfMatchHeader(eTagOrigin, request);

      UserSet updatedUserSet =
          getUserSetService().insertItem(datasetId, localId, position, existingUserSet);

      String serializedUserSetJsonLdStr = serializeUserSet(SetPageProfile.META, updatedUserSet);

      String etag = generateETag(updatedUserSet.getModified(), FORMAT_JSONLD, getApiVersion());

      // build response entity with headers
      MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
      headers.add(ALLOW, createAllowHeader(request));
      headers.add(UserSetHttpHeaders.VARY, PREFER);
      headers.add(PREFERENCE_APPLIED,
          SetPageProfile.META.getPreferenceApplied());
      headers.add(UserSetHttpHeaders.ETAG, etag);
      return new ResponseEntity<>(serializedUserSetJsonLdStr, headers, HttpStatus.OK);

    } catch (UserSetValidationException e) {
      throw new RequestValidationException(UserSetI18nConstants.USERSET_VALIDATION, Arrays.asList(e.getMessage()), e);
    }
  }

  /**
   * This method validates input values, retrieves user set object and inserts multiple items within
   * user set to the given position or at the end if no valid position provided.
   * 
   * @param request
   * @param authentication The Authentication object
   * @param identifier The identifier of a user set
   * @param items Items to be added to the set
   * @param position The position in the existin item list
   * @return response entity that comprises response body, headers and status code
   * @throws EuropeanaApiException
   */
  protected ResponseEntity<String> insertMultipleItemsIntoUserSet(HttpServletRequest request,
      Authentication authentication, String identifier, List<String> items, String position)
      throws EuropeanaApiException {
    try {
      // 3. check if the Set exists, if not respond with HTTP 404
      // retrieve an existing user set based on its identifier
      UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

      // 4. check if user is authorized check visibility level for given user
      getUserSetService().verifyPermissionToUpdate(existingUserSet, authentication, true);

      // 5. assign contribtor for entityBestItemsSet
      // for entity user sets, add users with 'editor' role as contributors
      addContributorForEntitySet(existingUserSet, authentication);


      // 6. check if set is closed
      verifyIfClosedSet(existingUserSet);

      int itemsPosition = parseAndValidateItemPosition(position, existingUserSet);

      // 8. pinned is available only for entityBestItemsSet
      // if set is not entity best item set and position is "pin", throw exception
      if (!existingUserSet.isEntityBestItemsSet()
          && WebUserSetRequestUtils.isPinPosition(position)) {
        throw new RequestValidationException(UserSetI18nConstants.USER_SET_OPERATION_NOT_ALLOWED,
           Arrays.asList("Pinning item ", existingUserSet.getType()));
      }

      // 9. verify if position is higher than pinned
      if (!WebUserSetRequestUtils.isPinPosition(position) && itemsPosition > -1
          && itemsPosition < existingUserSet.getPinned()) {
        throw new RequestValidationException(UserSetI18nConstants.USER_SET_OPERATION_NOT_ALLOWED,
            Arrays.asList("Position smaller than pinned is not allowed for non pin request!",
                itemsPosition + " < " + existingUserSet.getPinned()));
      }


      // check timestamp if provided within the “If-Match” HTTP header, if false
      // respond with HTTP 412
      String eTagOrigin =
          generateETag(existingUserSet.getModified(), FORMAT_JSONLD, getApiVersion());
      checkIfMatchHeader(eTagOrigin, request);

      // 7. (verify size for Galleries) & 11-13. (process items)
      UserSet updatedUserSet =
          getUserSetService().insertMultipleItems(items, position, itemsPosition, existingUserSet, authentication);

      String serializedUserSetJsonLdStr = serializeUserSet(SetPageProfile.META, updatedUserSet);

      String etag = generateETag(updatedUserSet.getModified(), FORMAT_JSONLD, getApiVersion());

      // build response entity with headers
      MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
      headers.add(ALLOW, createAllowHeader(request));
      headers.add(UserSetHttpHeaders.VARY, PREFER);
      headers.add(PREFERENCE_APPLIED,
          SetPageProfile.META.getPreferenceApplied());
      headers.add(UserSetHttpHeaders.ETAG, etag);
      return new ResponseEntity<>(serializedUserSetJsonLdStr, headers, HttpStatus.OK);

    } catch (UserSetValidationException e) {
      throw new RequestValidationException(UserSetI18nConstants.USERSET_VALIDATION, Arrays.asList(e.getMessage()), e);
    }
  }

  private void verifyIfClosedSet(UserSet existingUserSet) throws RequestValidationException {
    if (existingUserSet.isOpenSet()) {
      // cannot add items to open sets
      throw new RequestValidationException(UserSetI18nConstants.USER_SET_OPERATION_NOT_ALLOWED,
          Arrays.asList("'Insert item to existing user set'", "open"));
    }
  }

  private int parseAndValidateItemPosition(String position, UserSet existingUserSet)
          throws EuropeanaApiException {
    int itemsPosition = parseItemsPosition(position);
    if (!WebUserSetRequestUtils.isPinPosition(position) && itemsPosition >= 0
        && itemsPosition < existingUserSet.getPinned()) {
      throw new RequestValidationException(UserSetI18nConstants.INVALID_UNPINNED_ITEMS_POSITION,
          null);
    }
    return itemsPosition;
  }

  // returns -1 if not provided
  private int parseItemsPosition(String position) throws InvalidParamException {
    if (WebUserSetRequestUtils.isPinPosition(position)) {
      return 0;
    }
    int positionFinal = -1;
    if (position != null) {
      try {
        positionFinal = Integer.parseInt(position);
        if (positionFinal < 0) {
          throw new InvalidParamException(Arrays.asList(PATH_PARAM_POSITION, "", position));
        }
      } catch (RuntimeException e) {
        throw new InvalidParamException(Arrays.asList(PATH_PARAM_POSITION, "", position), e);
      }
    }
    return positionFinal;
  }

  @RequestMapping(value = {"/set/{identifier}/{datasetId}/{localId}"},
          method = {RequestMethod.GET, RequestMethod.HEAD},
      produces = {CONTENT_TYPE_JSONLD_UTF8, CONTENT_TYPE_JSON_UTF8})
  public ResponseEntity<String> isItemInUserSet(
      @PathVariable(value = PATH_PARAM_SET_ID) String identifier,
      @PathVariable(value = PATH_PARAM_DATASET_ID) @Pattern(
          regexp = UserSetUtils.EUROPEANA_ID_FIELD_REGEX,
          message = INVALID_RECORD_ID_MESSAGE) String datasetId,
      @PathVariable(value = PATH_PARAM_LOCAL_ID) @Pattern(
          regexp = UserSetUtils.EUROPEANA_ID_FIELD_REGEX,
          message = INVALID_RECORD_ID_MESSAGE) String localId,
      HttpServletRequest request) throws EuropeanaApiException {

    // check user credentials, if invalid respond with HTTP 401,
    // or if unauthorized respond with HTTP 403
    // check client access (a valid "wskey" must be provided)
    Authentication authentication = verifyReadAccess(request);
    return isItemInUserSet(identifier, datasetId, localId, authentication, request);
  }

  /**
   * This method validates input values and checks if item is already in a user set.
   * 
   * @param identifier The identifier of a user set
   * @param datasetId The identifier of the dataset, typically a number
   * @param localId The local identifier within the provider
   * @param authentication
   * @return response entity that comprises response body, headers and status code
   * @throws EuropeanaApiException
   */
  protected ResponseEntity<String> isItemInUserSet(String identifier,
      String datasetId, String localId, Authentication authentication, HttpServletRequest request) throws EuropeanaApiException {

    try {
      // check if the Set exists, if not respond with HTTP 404
      // retrieve an existing user set based on its identifier
      UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

      // check visibility level for given user
      // for Entity sets, editors also can view the Items
      if (existingUserSet.isEntityBestItemsSet()) {
        getUserSetService().verifyPermissionToUpdate(existingUserSet, authentication, true);
      } else { // if not entity set and visibility private, then only owner and admins are allowed.
        if (existingUserSet.isPrivate()) {
          getUserSetService().verifyOwnerOrAdmin(existingUserSet, authentication, false);
        }
      }

      // for entity user sets, add users with 'editor' role as contributors
      addContributorForEntitySet(existingUserSet, authentication);


      // check if the Set is disabled, respond with HTTP 410
      HttpStatus httpStatus = null;

      String newItem =
          UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), datasetId, localId);

      // check if item already exists in the Set, if so respond with
      // HTTP 200, otherwise respond with HTTP 404.
      // check if item already exists in the Set, if so remove it
      if (existingUserSet.getItems()!=null && existingUserSet.getItems().contains(newItem)) {
        httpStatus = HttpStatus.NO_CONTENT;
      } else {
        httpStatus = HttpStatus.NOT_FOUND;
      }

      // build response entity with headers
      MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
      headers.add(ALLOW, createAllowHeader(request));

      return new ResponseEntity<>("", headers, httpStatus);
    } catch (UserSetValidationException | UserSetInstantiationException e) {
      throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_CANT_PARSE_BODY, Arrays.asList(e.getMessage()), e);
    }
  }


  @Deprecated(since = "EA-3869", forRemoval = true)
  @DeleteMapping(value = {"/set/{identifier}/{datasetId}/{localId}"},
      produces = {CONTENT_TYPE_JSONLD_UTF8, CONTENT_TYPE_JSON_UTF8})
  public ResponseEntity<String> deleteItemFromUserSet(
      @PathVariable(value = PATH_PARAM_SET_ID) String identifier,
      @PathVariable(value = PATH_PARAM_DATASET_ID) @Pattern(
          regexp = UserSetUtils.EUROPEANA_ID_FIELD_REGEX,
          message = INVALID_RECORD_ID_MESSAGE) String datasetId,
      @PathVariable(value = PATH_PARAM_LOCAL_ID) @Pattern(
          regexp = UserSetUtils.EUROPEANA_ID_FIELD_REGEX,
          message = INVALID_RECORD_ID_MESSAGE) String localId,
      HttpServletRequest request) throws EuropeanaApiException {

    // check user credentials, if invalid respond with HTTP 401,
    // or if unauthorized respond with HTTP 403
    Authentication authentication = verifyWriteAccess(Operations.DELETE, request);
    return deleteItemFromUserSet(request, authentication, identifier, datasetId, localId);
  }

  /**
   * This method validates input values and deletes item from a user set.
   *
   * @param authentication The Authentication object
   * @param identifier The identifier of a user set
   * @param datasetId The identifier of the dataset, typically a number
   * @param localId The local identifier within the provider
   * @return response entity that comprises response body, headers and status code
   * @throws EuropeanaApiException
   */
  protected ResponseEntity<String> deleteItemFromUserSet(HttpServletRequest request,
           Authentication authentication, String identifier, String datasetId, String localId) throws EuropeanaApiException {
    try {
      // check if the Set exists, if not respond with HTTP 404
      // retrieve an existing user set based on its identifier
      UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

      // check if the user is the owner/creator of the set or admin,
      // OR Editor for Entity sets, otherwise respond with
      // 403
      getUserSetService().verifyPermissionToUpdate(existingUserSet, authentication, true);

      // for entity user sets, add users with 'editor' role as contributors
      addContributorForEntitySet(existingUserSet, authentication);

      String newItem =
          UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), datasetId, localId);

      // check if item already exists in the Set, if not respond with HTTP 404
      boolean hasItem =
          existingUserSet.getItems() != null && existingUserSet.getItems().contains(newItem);
      if (!hasItem) {
        // TODO: consider changing to generateItemNotFoundResponse
        throw new UserSetNotFoundException(UserSetI18nConstants.USERSET_ITEM_NOT_FOUND,
                Arrays.asList(datasetId + "/" + localId, identifier));
      }

      UserSet updatedUserSet = getUserSetService().deleteItem(newItem, existingUserSet);

      // serialize to JsonLd
      String serializedUserSetJsonLdStr = serializeUserSet(SetPageProfile.META, updatedUserSet);
      String etag = generateETag(updatedUserSet.getModified(), FORMAT_JSONLD, getApiVersion());

      // respond with HTTP 200 containing the updated Set description as body.
      // serialize Set in JSON-LD following the requested profile
      // (if not indicated assume the default, ie. minimal)
      // build response entity with headers
      MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
      headers.add(ALLOW, createAllowHeader(request));
      headers.add(PREFERENCE_APPLIED,
          SetPageProfile.META.getPreferenceApplied());
      headers.add(UserSetHttpHeaders.ETAG, etag);

      return new ResponseEntity<>(serializedUserSetJsonLdStr, headers, HttpStatus.OK);
    } catch (UserSetValidationException | UserSetInstantiationException e) {
      throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_CANT_PARSE_BODY, Arrays.asList(e.getMessage()), e);
    }
  }

  @DeleteMapping(value = {"/set/{identifier}/items"},
      produces = {CONTENT_TYPE_JSONLD_UTF8, CONTENT_TYPE_JSON_UTF8})
  public ResponseEntity<String> deleteMultipleItemsFromUserSet(
      @PathVariable(value = PATH_PARAM_SET_ID) String identifier,
      @RequestBody List<String> items, HttpServletRequest request) throws EuropeanaApiException {
    // check user credentials, if invalid respond with HTTP 401,
    // or if unauthorized respond with HTTP 403
    Authentication authentication = verifyWriteAccess(Operations.DELETE, request);
    return deleteMultipleItemsFromUserSet(request, authentication, identifier, items);
  }

  protected ResponseEntity<String> deleteMultipleItemsFromUserSet(HttpServletRequest request, Authentication authentication,
      String identifier, List<String> items) throws EuropeanaApiException {
    try {
      // 3. check if the Set exists, if not respond with HTTP 404
      // retrieve an existing user set based on its identifier
      UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

      // 4. Check if the Set it is a closed set
      verifyIfClosedSet(existingUserSet);

      // 5. Check if the user is authorised
      // check if the user is the owner/creator of the set or admin,
      // OR Editor for Entity sets, otherwise respond with
      // 403
      getUserSetService().verifyPermissionToUpdate(existingUserSet, authentication, true);

      // 6. If the “type” of the set is “EntityBestItemsSet” assign the user associated to the JWT
      // token
      // for entity user sets, add users with 'editor' role as contributors
      addContributorForEntitySet(existingUserSet, authentication);

      // 7. & 8. remove items, update pinned, update modified
      UserSet updatedUserSet = getUserSetService().deleteMultipleItems(items, existingUserSet, authentication);

      // serialize to JsonLd
      String serializedUserSetJsonLdStr = serializeUserSet(SetPageProfile.META, updatedUserSet);
      String etag = generateETag(updatedUserSet.getModified(), FORMAT_JSONLD, getApiVersion());

      // respond with HTTP 200 containing the updated Set description as body.
      // serialize Set in JSON-LD following the requested profile
      // (if not indicated assume the default, ie. minimal)
      // build response entity with headers
      MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
      headers.add(ALLOW, createAllowHeader(request));
      headers.add(PREFERENCE_APPLIED,
          SetPageProfile.META.getPreferenceApplied());
      headers.add(UserSetHttpHeaders.ETAG, etag);

      return new ResponseEntity<>(serializedUserSetJsonLdStr, headers, HttpStatus.OK);
    } catch (UserSetValidationException | UserSetInstantiationException e) {
      throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_CANT_PARSE_BODY, Arrays.asList(e.getMessage()), e);
    }
  }


  @DeleteMapping(value = {"/set/{identifier}"})
  public ResponseEntity<String> deleteUserSet(
      @PathVariable(value = PATH_PARAM_SET_ID) String identifier,
      HttpServletRequest request) throws EuropeanaApiException {

    // check user credentials, if invalid respond with HTTP 401,
    // or if unauthorized respond with HTTP 403
    Authentication authentication = verifyWriteAccess(Operations.DELETE, request);
    return deleteUserSet(request, identifier, authentication);
  }

  /**
   * This method implements removal of a user set
   * 
   * @param request
   * @param identifier
   * @throws EuropeanaApiException
   */
  protected ResponseEntity<String> deleteUserSet(HttpServletRequest request, String identifier,
      Authentication authentication) throws EuropeanaApiException {
      // retrieve a user set based on its identifier
      // if the Set doesn’t exist, respond with HTTP 404
      UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

      // check that only the admins and the owners of the user sets are allowed to
      // delete the user set.
      // in the case of regular users (not admins), the autorization method must check
      // if the users
      // that calls the deletion (i.e. identified by provided user token) is the same
      // user as the creator
      // of the user set
      getUserSetService().verifyOwnerOrAdmin(existingUserSet, authentication, false);

      // check visibility level for given user - currently
      // checkStatus(existingUserSet, authentication);

      // check timestamp if provided within the "If-Match" HTTP header, if false
      // respond with HTTP 412
      String eTagOrigin =
          generateETag(existingUserSet.getModified(), FORMAT_JSONLD, getApiVersion());
      checkIfMatchHeader(eTagOrigin, request);

      // if the user set is disabled and the user is not an admin, respond with HTTP
      // 410
      HttpStatus httpStatus = null;
      // if the user is an Administrator then permanently remove item
      // (and all items that are members of the user set)
      httpStatus = HttpStatus.NO_CONTENT;
      getUserSetService().deleteUserSet(existingUserSet.getIdentifier());

      // build response entity with headers
      MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
      headers.add(LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
      headers.add(LINK, UserSetHttpHeaders.VALUE_BASIC_RESOURCE);
      // headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_GPPD);
      headers.add(ALLOW, createAllowHeader(request));

      return new ResponseEntity<>(null, headers, httpStatus);
  }

  /**
   * Deletes the user sets : 1) With admin role and creator. Deletes all the associated user sets OR
   * 2) Deletes my user sets by getting the creatorId from the userToken
   *
   * @param creator
   * @param request
   * @return
   * @throws EuropeanaApiException
   */
  @DeleteMapping(value = {"/set/"})
  public ResponseEntity<String> deleteUserAssociatedSet(
      @RequestParam(value = PATH_PARAM_CREATOR_ID,
          required = false) String creator,
      HttpServletRequest request) throws EuropeanaApiException {
    // check user credentials, if invalid respond with HTTP 401,
    // or if unauthorized respond with HTTP 403
    Authentication authentication = verifyWriteAccess(Operations.DELETE, request);
    return deleteUserAssociatedSets(getCreatorId(authentication, creator), request);
  }

  /**
   * gets the creatorId for the delete functionality if creator is null, gets the userId from the
   * token OR if creator is empty , throws 400 Bad request if creator is passed and role is not
   * admin, throws 403 Forbidden if creator is passed and role is admin, returns the creatorId
   *
   * @param authentication
   * @param creatorId
   * @return
   * @throws EuropeanaApiException
   */
  private String getCreatorId(Authentication authentication, String creatorId) throws EuropeanaApiException {
    // if creator is empty : Delete my sets is invoked.
    // get the creatorId from the userToken
    if (creatorId == null) {
      return getUserSetService().getUserId(authentication);
    } else {
      // if creatorId is empty, return 400 Bad Request
      if (creatorId.isEmpty()) {
        throw new InvalidParamException(Arrays.asList("Creator Id", "no empty" , "empty"));
      }
      // if creator is passed, verify if the user is admin.
      // Owner/User can not perform this action
      if (!getUserSetService().isAdmin(authentication)) {
        throw new ApplicationAuthenticationException(null, ErrorConfig.OPERATION_NOT_AUTHORIZED,
            Arrays.asList("Only admins are authorized to perform this operation."),
            HttpStatus.FORBIDDEN);
      }
      if (!StringUtils.startsWith(creatorId, "http")) {
        return UserSetUtils.buildUserUri(getConfiguration().getUserDataEndpoint(), creatorId);
      }
    }
    return creatorId;
  }

  /**
   * This method implements removal of all sets associated to a user
   *
   * @param creatorId
   * @throws EuropeanaApiException
   */
  protected ResponseEntity<String> deleteUserAssociatedSets(String creatorId, HttpServletRequest request)
          throws EuropeanaApiException {
      List<PersistentUserSet> userSets = getUserSetService().getUserSetByCreatorId(creatorId);

      // verify if the user sets are associated with the creatorId
      for (UserSet userset : userSets) {
        if (!StringUtils.equals(creatorId, userset.getCreator().getHttpUrl())) {
          throw new ApplicationAuthenticationException(null, ErrorConfig.OPERATION_NOT_AUTHORIZED,
                  Arrays.asList("Only user associated sets can be deleted"),
                  HttpStatus.FORBIDDEN);
        }
      }
      // if the user set is disabled and the user is not an admin, respond with HTTP
      // 410
      HttpStatus httpStatus = null;
      httpStatus = HttpStatus.NO_CONTENT;

      // delete user sets
      getUserSetService().deleteUserSets(creatorId, userSets);

      // build response entity with headers
      MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
      headers.add(LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
      headers.add(LINK, UserSetHttpHeaders.VALUE_BASIC_RESOURCE);
      headers.add(ALLOW, createAllowHeader(request));
      return new ResponseEntity<>(headers, httpStatus);
  }


  @Deprecated
  /**
   * SG: we might need to add back the verification of page size for dereference profile
   * 
   * @deprecated need to verify specs to see if the page for items dereferencing stays the same as
   *             the size for standard profile
   * @param userSet
   * @param pageSize
   * @return
   */
  private int getDerefItemsCount(UserSet userSet, int pageSize) {
    if (userSet.isOpenSet()) {
      // limit to max deref items
      return Math.min(pageSize, getConfiguration().getMaxRetrieveDereferencedItems());
    } else {
      // for closed set dereference all items
      // limit to max deref items
      return Math.min(userSet.getTotal(), getConfiguration().getMaxRetrieveDereferencedItems());
    }
  }


}