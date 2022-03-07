package eu.europeana.set.web.service.controller.jsonld;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
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

import com.fasterxml.jackson.core.JsonParseException;

import eu.europeana.api.common.config.UserSetI18nConstants;
import eu.europeana.api.common.config.swagger.SwaggerSelect;
import eu.europeana.api.commons.definitions.config.i18n.I18nConstants;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.web.definitions.WebFields;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.exception.InternalServerException;
import eu.europeana.api.commons.web.exception.ParamValidationException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.api.commons.web.model.vocabulary.Operations;
import eu.europeana.set.definitions.config.UserSetConfigurationImpl;
import eu.europeana.set.definitions.exception.UserSetAttributeInstantiationException;
import eu.europeana.set.definitions.exception.UserSetInstantiationException;
import eu.europeana.set.definitions.exception.UserSetValidationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.web.exception.authorization.OperationAuthorizationException;
import eu.europeana.set.web.exception.request.RequestBodyValidationException;
import eu.europeana.set.web.exception.request.RequestValidationException;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.http.SwaggerConstants;
import eu.europeana.set.web.http.UserSetHttpHeaders;
import eu.europeana.set.web.service.controller.BaseRest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * This class implements the User Set - REST API
 */

@Controller
@SwaggerSelect
@Api(tags = "Web User Set API")
public class WebUserSetRest extends BaseRest {

    public WebUserSetRest() {
	super();
    }

    @PostMapping(value = "/set/", produces = { HttpHeaders.CONTENT_TYPE_JSONLD_UTF8,
	    HttpHeaders.CONTENT_TYPE_JSON_UTF8 })
    @ApiOperation(notes = SwaggerConstants.SAMPLES_JSONLD, value = "Create user set", nickname = "createUserSet", response = java.lang.Void.class)
    public ResponseEntity<String> createUserSet(@RequestBody String userSet,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_PROFILE, required = false, defaultValue = CommonApiConstants.PROFILE_MINIMAL) String profile,
	    HttpServletRequest request) throws HttpException {

	// validate user - check user credentials (all registered users can create)
	// if invalid respond with HTTP 401 or if unauthorized respond with HTTP 403;
	Authentication authentication = verifyWriteAccess(Operations.CREATE, request);
	return storeUserSet(userSet, authentication, profile, request);
    }

    /**
     * This method requests parsing of a user set in JsonLd format to a UserSet
     * object
     * 
     * @param userSetJsonLdStr The user set in JsonLd format
     * @param authentication   The authentication object with user identifier
     * @param profileStr       The profile definition
     * @param request          HTTP request
     * @return response entity that comprises response body, headers and status code
     * @throws HttpException
     */
    protected ResponseEntity<String> storeUserSet(String userSetJsonLdStr, Authentication authentication,
	    String profileStr, HttpServletRequest request) throws HttpException {
	try {

	    LdProfiles profile = getProfile(profileStr, request);

	    // parse user set
	    UserSet webUserSet = getUserSetService().parseUserSetLd(userSetJsonLdStr);

	    // validate and process the Set description for format and mandatory fields
	    // if false respond with HTTP 400
	    // store the new Set with its respective id, together with all the containing
	    // items
	    // following the order given by the list
	    // generate an identifier (in sequence) for the Set
	    // generate and add a created and modified timestamp to the Set
	    // type should be saved now in the database and not generated on the fly during
	    // serialization
	    UserSet storedUserSet = getUserSetService().storeUserSet(webUserSet, authentication);

	    if (mustFetchItems(storedUserSet, profile)) {
		int derefItems = getDerefItemsCount(storedUserSet, UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE);
		storedUserSet = getUserSetService().fetchItems(storedUserSet, null, null,
			CommonApiConstants.DEFAULT_PAGE, derefItems, profile);
	    }

	    String serializedUserSetJsonLdStr = serializeUserSet(profile, storedUserSet);

	    String etag = generateETag(storedUserSet.getModified(), WebFields.FORMAT_JSONLD, getApiVersion());

	    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
	    headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
	    headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_RESOURCE);
	    headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_PG);
	    headers.add(UserSetHttpHeaders.CACHE_CONTROL, UserSetHttpHeaders.VALUE_NO_CAHCHE_STORE_REVALIDATE);
	    // generate “ETag”;
	    headers.add(UserSetHttpHeaders.ETAG, etag);
	    headers.add(UserSetHttpHeaders.PREFERENCE_APPLIED, profile.getPreferHeaderValue());

	    return new ResponseEntity<>(serializedUserSetJsonLdStr, headers, HttpStatus.CREATED);
	} catch (JsonParseException | UserSetValidationException | UserSetAttributeInstantiationException e) {
	    throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_CANT_PARSE_BODY,
		    new String[] { e.getMessage() }, e);
	} catch (UserSetInstantiationException e) {
	    throw new HttpException(null, UserSetI18nConstants.USERSET_INVALID_BODY, null, HttpStatus.BAD_REQUEST, e);
	} catch (HttpException e) {
	    // avoid wrapping HttpExceptions
	    throw e;
	} catch (RuntimeException | IOException | JSONException e) {
	    throw new InternalServerException(e);
	}

    }

    @GetMapping(value = { "/set/{identifier}", "/set/{identifier}.jsonld" }, produces = {
	    HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8 })
    @ApiOperation(notes = SwaggerConstants.SEARCH_HELP_NOTE, value = "Retrieve a user set", nickname = "retrieve", response = java.lang.Void.class)
    public ResponseEntity<String> getUserSet(
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_SORT, required = false) String sortField,
	    @RequestParam(value = WebUserSetFields.PARAM_SORT_ORDER, required = false) String sortOrderField,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_PAGE, required = false) String page,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_PAGE_SIZE, defaultValue = ""
		    + UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE) String pageSize,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_PROFILE, required = false, defaultValue = CommonApiConstants.PROFILE_MINIMAL) String profile,
	    HttpServletRequest request) throws HttpException {

	Authentication authentication = verifyReadAccess(request);
	Integer pageNr = parseIntegerParam(CommonApiConstants.QUERY_PARAM_PAGE, page, -1);
	int maxPageSize = getConfiguration().getMaxPageSize(profile);
	Integer pageItems = parseIntegerParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, pageSize, maxPageSize);
	
	return getUserSet(profile, identifier, request, sortField, sortOrderField, pageNr, pageItems, authentication);
    }

    private Integer parseIntegerParam(String paramName, String paramValue, int maxValue) throws ParamValidationException {
	if (paramValue != null) {
	    try {
		Integer value = Integer.valueOf(paramValue);
		if(maxValue > 0 && value > maxValue) {
		    throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
				new String[] { paramName, paramValue });
		}
		return value;
	    }catch(NumberFormatException e){
		throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
			new String[] { paramName, paramValue }, e);
	    }
	}
	return null;
    }

    /**
     * This method retrieves an existing user set identified by given identifier,
     * which is a number in string format.
     * 
     * @param profileStr The profile definition
     * @param identifier The identifier
     * @param request    HTTP request
     * @return response entity that comprises response body, headers and status code
     * @throws HttpException
     */
    private ResponseEntity<String> getUserSet(String profileStr, String identifier, HttpServletRequest request,
	    String sort, String sortOrder, Integer pageNr, int pageSize, Authentication authentication)
	    throws HttpException {
	try {
	    LdProfiles profile = getProfile(profileStr, request);

	    // retrieve a Set based on its identifier - process query
	    // if the Set doesn’t exist, respond with HTTP 404
	    // if the Set is disabled respond with HTTP 410
	    UserSet userSet = getUserSetService().getUserSetById(identifier);

	    // check visibility level for given user
	    if (userSet.isPrivate()) {
		getUserSetService().verifyOwnerOrAdmin(userSet, authentication, false);
	    }

	    if (mustFetchItems(userSet, profile)) {
	    // pageNr, if empty default value 0 is sent	for fetching items
		int page = (pageNr  != null) ? pageNr : CommonApiConstants.DEFAULT_PAGE;
		userSet = getUserSetService().fetchItems(userSet, sort, sortOrder, page, pageSize, profile);
	    }
	    return buildGetResponse(userSet, profile, pageNr, pageSize, request);

	} catch (HttpException e) {
	    // avoid wrapping http exception
	    throw e;
	} catch (RuntimeException | IOException | JSONException e) {
	    throw new InternalServerException(e);
	}
    }

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

    private boolean mustFetchItems(UserSet userSet, LdProfiles profile) {
	boolean itemDescriptionsProfile = LdProfiles.ITEMDESCRIPTIONS == profile;
	boolean fetchItemsForOpenSet = userSet.isOpenSet() && LdProfiles.MINIMAL != profile;
	return itemDescriptionsProfile || fetchItemsForOpenSet;
    }

    @PutMapping(value = { "/set/{identifier}" }, produces = { HttpHeaders.CONTENT_TYPE_JSONLD_UTF8,
	    HttpHeaders.CONTENT_TYPE_JSON_UTF8 })
    @ApiOperation(notes = SwaggerConstants.UPDATE_SAMPLES_JSONLD, value = "Update an existing user set", nickname = "update", response = java.lang.Void.class)
    public ResponseEntity<String> updateUserSet(
	    @PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier, @RequestBody String userSet,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_PROFILE, required = false, defaultValue = CommonApiConstants.PROFILE_MINIMAL) String profile,
	    HttpServletRequest request) throws HttpException {

	// check user credentials, if invalid respond with HTTP 401,
	Authentication authentication = verifyWriteAccess(Operations.UPDATE, request);
	return updateUserSet(request, authentication, identifier, userSet, profile);
    }

    /**
     * This method validates input values, retrieves user set object and updates it.
     * 
     * @param request
     * @param authentication   The Authentication object
     * @param identifier       The identifier
     * @param userSetJsonLdStr The user set fields to update in JSON format e.g.
     *                         title or description
     * @param profileStr       The profile definition
     * @return response entity that comprises response body, headers and status code
     * @throws HttpException
     */
    protected ResponseEntity<String> updateUserSet(HttpServletRequest request, Authentication authentication,
	    String identifier, String userSetJsonLdStr, String profileStr) throws HttpException {

	try {
	    LdProfiles profile = getProfile(profileStr, request);

	    // check if the Set exists, if not respond with HTTP 404
	    // retrieve an existing user set based on its identifier
	    UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

	    // only an editor can set the state of a set to "published"
	    // and only if the set is in "public" visibility.
		// only owner or admin can update any user set
	   getUserSetService().verifyOwnerOrAdmin(existingUserSet,authentication,false);

	    // check timestamp if provided within the “If-Match” HTTP header, if false
	    // respond with HTTP 412
	    String eTagOrigin = generateETag(existingUserSet.getModified(), WebFields.FORMAT_JSONLD, getApiVersion());
	    checkIfMatchHeader(eTagOrigin, request);

	    // parse fields of the new user set to an object
	    UserSet newUserSet = getUserSetService().parseUserSetLd(userSetJsonLdStr);

	    // validate and process the Set description for format and mandatory fields
	    // if false respond with HTTP 400
	    // set immutable fields before validation
	    newUserSet.setCreator(existingUserSet.getCreator());
	    newUserSet.setIdentifier(existingUserSet.getIdentifier());
//	    newUserSet.setSubject(existingUserSet.getSubject());
	    if (newUserSet.getVisibility() == null) {
		newUserSet.setVisibility(existingUserSet.getVisibility());
	    }
	    newUserSet.setContributor(existingUserSet.getContributor());
	    getUserSetService().validateWebUserSet(newUserSet);
	    // TODO: move verification to validateMethod when new specs are available
	    if (existingUserSet.isOpenSet() && !newUserSet.isOpenSet()) {
		// isDefinedBy is mandatory for open sets
		throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
			new String[] { WebUserSetModelFields.IS_DEFINED_BY + " (for open sets)" });
	    }
	    // validate items
	    validateAndSetItems(existingUserSet, newUserSet, profile);
	    // remove duplicated items
	    getUserSetService().removeItemDuplicates(newUserSet);

	    // Respond with HTTP 200
	    // update an existing user set. merge user sets - insert new fields in existing
	    // object
	    // update pagination
	    // generate and add a created and modified timestamp to the Set;

	    // if the Set corresponds to a closed set, replace member items with the new
	    // items
	    // that are present in the Set description only when a profile is indicated and
	    // modified date is set in the service;
	    UserSet updatedUserSet = getUserSetService().updateUserSet((PersistentUserSet) existingUserSet, newUserSet);

	    if (mustFetchItems(updatedUserSet, profile)) {
		int derefItems = getDerefItemsCount(updatedUserSet, UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE);
		updatedUserSet = getUserSetService().fetchItems(updatedUserSet, null, null,
			CommonApiConstants.DEFAULT_PAGE, derefItems, profile);
	    }

	    return buildGetResponse(updatedUserSet, profile, -1, -1, request);

	} catch (UserSetValidationException | UserSetInstantiationException e) {
	    throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_CANT_PARSE_BODY,
		    new String[] { e.getMessage() }, e);
	} catch (HttpException e) {
	    throw e;
	} catch (RuntimeException | IOException | JSONException e) {
	    throw new InternalServerException(e);
	}
    }

	/**
	 * Will add the last editor at the end of the contributor List
	 * @param existingUserSet
	 * @param authentication
	 */
	private void addContributorForEntitySet(UserSet existingUserSet, Authentication authentication) {
		if(existingUserSet.isEntityBestItemsSet() &&
				getUserSetService().hasEditorRole(authentication)) {
			String userId= getUserSetService().getUserId(authentication);
			// check if contributor is not null, in case if entity set was created by super user
			if(existingUserSet.getContributor() != null) {
				if (existingUserSet.getContributor().contains(userId)) {
					existingUserSet.getContributor().remove(userId);
				}
				existingUserSet.getContributor().add(userId);
			} else {
				existingUserSet.setContributor(Collections.singletonList(userId));
			}
		}

	}

    private void validateAndSetItems(UserSet storedUserSet, UserSet updateUserSet, LdProfiles profile)
	    throws ApplicationAuthenticationException {
	// no validation of items for open sets, they are retrieved dynamically
	if (storedUserSet.isOpenSet()) {
	    return;
	}

	// for entity sets update :profile should be minimal and
	// there must not be any items present in new user set
	// only metadata can be update for entity sets
	if(storedUserSet.isEntityBestItemsSet()) {
		if (LdProfiles.MINIMAL != profile) {
			throw new ApplicationAuthenticationException(UserSetI18nConstants.USERSET_PROFILE_MINIMAL_ALLOWED,
					UserSetI18nConstants.USERSET_PROFILE_MINIMAL_ALLOWED, new String[] {},
					HttpStatus.PRECONDITION_FAILED, null);
		}
		if (updateUserSet.getItems() != null && updateUserSet.getItems().size() > 0) {
			throw new ApplicationAuthenticationException(UserSetI18nConstants.USERSET_MINIMAL_UPDATE_PROFILE,
					UserSetI18nConstants.USERSET_MINIMAL_UPDATE_PROFILE, new String[] {},
					HttpStatus.BAD_REQUEST, null);
		}
	}

	// update the Set based on its identifier (replace member items with the new
	// items
	// that are present in the Set description only when a profile is indicated and
	// is
	// different from "ldp:PreferMinimalContainer" is referred in the "Prefer"
	// header)
	// if the provided userset contains a list of items and the profile is set to
	// minimal,
	// respond with HTTP 412)
	if (LdProfiles.MINIMAL == profile) {
	    if (updateUserSet.getItems() != null && updateUserSet.getItems().size() > 0) { // new user set contains
											   // items
		throw new ApplicationAuthenticationException(UserSetI18nConstants.USERSET_MINIMAL_UPDATE_PROFILE,
			UserSetI18nConstants.USERSET_MINIMAL_UPDATE_PROFILE, new String[] {},
			HttpStatus.PRECONDITION_FAILED, null);
	    }
	} else { // it is a Standard profile
	    if (updateUserSet.getItems() == null || updateUserSet.getItems().size() == 0) { // new user set contains no
											    // items
		throw new ApplicationAuthenticationException(UserSetI18nConstants.USERSET_CONTAINS_NO_ITEMS,
			UserSetI18nConstants.USERSET_CONTAINS_NO_ITEMS, new String[] {}, HttpStatus.PRECONDITION_FAILED,
			null);
	    }
	    storedUserSet.setItems(updateUserSet.getItems());
	}
    }
    
    @PutMapping(value = { "/set/{identifier}/publish" }, produces = {
        HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8 })
    @ApiOperation(notes = SwaggerConstants.PUBLISH_SET_NOTE, value = "Publish an existing user set", nickname = "publish set", response = java.lang.Void.class)
    public ResponseEntity<String> publishUserSet(
        @PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
        @RequestParam(value = CommonApiConstants.QUERY_PARAM_PROFILE, required = false, defaultValue = CommonApiConstants.PROFILE_MINIMAL) String profileStr,
        HttpServletRequest request) throws HttpException {
      // check user credentials, if invalid respond with HTTP 401,
      // or if unauthorized respond with HTTP 403
      Authentication authentication = verifyWriteAccess(Operations.UPDATE, request);
      return publishUnpublishUserSetApiLevel(identifier, authentication, true, profileStr, request);
    }

    @PutMapping(value = { "/set/{identifier}/unpublish" }, produces = {
        HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8 })
    @ApiOperation(notes = SwaggerConstants.PUBLISH_SET_NOTE, value = "Unpublish an existing user set", nickname = "unpublish set", response = java.lang.Void.class)
    public ResponseEntity<String> unpublishUserSet(
        @PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
        @RequestParam(value = CommonApiConstants.QUERY_PARAM_PROFILE, required = false, defaultValue = CommonApiConstants.PROFILE_MINIMAL) String profileStr,
        HttpServletRequest request) throws HttpException {
      // check user credentials, if invalid respond with HTTP 401,
      // or if unauthorized respond with HTTP 403
      Authentication authentication = verifyWriteAccess(Operations.UPDATE, request);
      return publishUnpublishUserSetApiLevel(identifier, authentication, false, profileStr, request);
    }
    
    protected ResponseEntity<String> publishUnpublishUserSetApiLevel (String identifier, Authentication authentication, boolean publishYesUnpublishNo, String profileStr, HttpServletRequest request) throws HttpException {
      try {

        UserSet updatedUserSet = getUserSetService().publishUnpublishUserSet(identifier, authentication, publishYesUnpublishNo);
    
        // serialize to JsonLd
        LdProfiles profile = getProfile(profileStr, request);
        String serializedUserSetJsonLdStr = serializeUserSet(profile, updatedUserSet);
        String etag = generateETag(updatedUserSet.getModified(), WebFields.FORMAT_JSONLD, getApiVersion());
    
        // build response entity with headers
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
        headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_PPGHD);
        headers.add(UserSetHttpHeaders.VARY, HttpHeaders.PREFER);
        headers.add(UserSetHttpHeaders.PREFERENCE_APPLIED, profile.getPreferHeaderValue());
        headers.add(UserSetHttpHeaders.ETAG, etag);
    
        return new ResponseEntity<>(serializedUserSetJsonLdStr, headers, HttpStatus.OK);
        
      } catch (HttpException e) {
        throw e;
      } catch (RuntimeException | IOException e) {
        throw new InternalServerException(e);
      }
    }    

    @PutMapping(value = { "/set/{identifier}/{datasetId}/{localId}" }, produces = {
	    HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8 })
    @ApiOperation(notes = SwaggerConstants.INSERT_ITEM_NOTE, value = "Insert item to an existing user set", nickname = "insert item", response = java.lang.Void.class)
    public ResponseEntity<String> insertItemIntoUserSet(
	    @PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
	    @PathVariable(value = WebUserSetFields.PATH_PARAM_DATASET_ID) String datasetId,
	    @PathVariable(value = WebUserSetFields.PATH_PARAM_LOCAL_ID) String localId,
	    @RequestParam(value = WebUserSetFields.PATH_PARAM_POSITION, required = false) String position,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_PROFILE, required = false, defaultValue = CommonApiConstants.PROFILE_MINIMAL) String profile,
	    HttpServletRequest request) throws HttpException {

	// check user credentials, if invalid respond with HTTP 401,
	// or if unauthorized respond with HTTP 403
	Authentication authentication = verifyWriteAccess(Operations.UPDATE, request);
	return insertItemIntoUserSet(request, authentication, identifier, datasetId, localId, position, profile);
    }

    /**
     * This method validates input values, retrieves user set object and inserts
     * item within user set to given position or at the end if no valid position
     * provided.
     * 
     * @param request
     * @param authentication The Authentication object
     * @param identifier     The identifier of a user set
     * @param datasetId      The identifier of the dataset, typically a number
     * @param localId        The local identifier within the provider
     * @param position       The position in the existin item list
     * @param profileStr     The profile definition
     * @return response entity that comprises response body, headers and status code
     * @throws HttpException
     */
    protected ResponseEntity<String> insertItemIntoUserSet(HttpServletRequest request, Authentication authentication,
	    String identifier, String datasetId, String localId, String position, String profileStr)
	    throws HttpException {

	try {
	    LdProfiles profile = getProfile(profileStr, request);

	    // check if the Set exists, if not respond with HTTP 404
	    // retrieve an existing user set based on its identifier
	    UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

	    if (existingUserSet.isOpenSet()) {
		// cannot add items to open sets
		throw new RequestValidationException(UserSetI18nConstants.USER_SET_OPERATION_NOT_ALLOWED,
			new String[] { "'Insert item to existing user set'", "open" });
	    }

	    // if set is not entity set and position is "pin", throw exception
	    if (!existingUserSet.isEntityBestItemsSet() && StringUtils.equals(position, WebUserSetFields.PINNED_POSITION)) {
			throw new RequestValidationException(UserSetI18nConstants.USER_SET_OPERATION_NOT_ALLOWED,
					new String[] { "Pinning item ", existingUserSet.getType() });
		}

	    // check visibility level for given user
	    getUserSetService().checkPermissionToUpdate(existingUserSet, authentication, true);

		// for entity user sets, add users with 'editor' role as contributors
		addContributorForEntitySet(existingUserSet, authentication);

	    // check timestamp if provided within the “If-Match” HTTP header, if false
	    // respond with HTTP 412
	    String eTagOrigin = generateETag(existingUserSet.getModified(), WebFields.FORMAT_JSONLD, getApiVersion());
	    checkIfMatchHeader(eTagOrigin, request);
	    UserSet updatedUserSet = getUserSetService().insertItem(datasetId, localId, position, existingUserSet);
		String serializedUserSetJsonLdStr = serializeUserSet(profile, updatedUserSet);

	    String etag = generateETag(updatedUserSet.getModified(), WebFields.FORMAT_JSONLD, getApiVersion());

	    // build response entity with headers
	    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
	    headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_PPGHD);
	    headers.add(UserSetHttpHeaders.VARY, HttpHeaders.PREFER);
	    headers.add(UserSetHttpHeaders.PREFERENCE_APPLIED, profile.getPreferHeaderValue());
	    headers.add(UserSetHttpHeaders.ETAG, etag);
	    return new ResponseEntity<>(serializedUserSetJsonLdStr, headers, HttpStatus.OK);

	} catch (UserSetValidationException e) {
	    throw new RequestValidationException(UserSetI18nConstants.USERSET_VALIDATION,
		    new String[] { e.getMessage() }, e);
	} catch (HttpException e) {
	    throw e;
	} catch (RuntimeException | IOException e) {
	    throw new InternalServerException(e);
	}
    }

    @RequestMapping(value = { "/set/{identifier}/{datasetId}/{localId}" }, method = { RequestMethod.GET }, produces = {
	    HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8 })
    @ApiOperation(notes = SwaggerConstants.CHECK_ITEM_NOTE, value = "Check if item is member of the Set", nickname = "check item", response = java.lang.Void.class)
    public ResponseEntity<String> isItemInUserSet(
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
	    @PathVariable(value = WebUserSetFields.PATH_PARAM_DATASET_ID) String datasetId,
	    @PathVariable(value = WebUserSetFields.PATH_PARAM_LOCAL_ID) String localId, HttpServletRequest request)
	    throws HttpException {

	// check user credentials, if invalid respond with HTTP 401,
	// or if unauthorized respond with HTTP 403
	// check client access (a valid "wskey" must be provided)
	Authentication authentication = verifyReadAccess(request);
	return isItemInUserSet(wskey, identifier, datasetId, localId, authentication);
    }

    /**
     * This method validates input values and checks if item is already in a user
     * set.
     * 
     * @param wsKey          The API key
     * @param identifier     The identifier of a user set
     * @param datasetId      The identifier of the dataset, typically a number
     * @param localId        The local identifier within the provider
     * @param authentication
     * @return response entity that comprises response body, headers and status code
     * @throws HttpException
     */
    protected ResponseEntity<String> isItemInUserSet(String wsKey, String identifier, String datasetId, String localId,
	    Authentication authentication) throws HttpException {

	try {
	    // check if the Set exists, if not respond with HTTP 404
	    // retrieve an existing user set based on its identifier
	    UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

	    // check visibility level for given user
		// for Entity sets, editors also can view the Items
		if(existingUserSet.isEntityBestItemsSet()) {
			getUserSetService().checkPermissionToUpdate(existingUserSet,authentication,true);
		} else { // if not entity set and visibility private, then only owner and admins are allowed.
			   if (existingUserSet.isPrivate()) {
				getUserSetService().verifyOwnerOrAdmin(existingUserSet, authentication, false);
			}
		}

		// for entity user sets, add users with 'editor' role as contributors
		addContributorForEntitySet(existingUserSet, authentication);


	    // check if the Set is disabled, respond with HTTP 410
	    HttpStatus httpStatus = null;

	    String newItem = UserSetUtils.buildItemUrl(WebUserSetFields.BASE_ITEM_URL, datasetId, localId);

	    // check if item already exists in the Set, if so respond with
	    // HTTP 200, otherwise respond with HTTP 404.
	    // check if item already exists in the Set, if so remove it
	    if (existingUserSet.getItems().contains(newItem)) {
		httpStatus = HttpStatus.NO_CONTENT;
	    } else {
		httpStatus = HttpStatus.NOT_FOUND;
	    }

	    // build response entity with headers
	    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
	    headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_PPGHD);

	    return new ResponseEntity<>("", headers, httpStatus);
	} catch (UserSetValidationException | UserSetInstantiationException e) {
	    throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_CANT_PARSE_BODY,
		    new String[] { e.getMessage() }, e);
	} catch (HttpException e) {
	    // TODO: change this when OAUTH is implemented and the user information is
	    // available in service
	    throw e;
	} catch (RuntimeException e) {
	    throw new InternalServerException(e);
	}
    }

   
    @DeleteMapping(value = { "/set/{identifier}/{datasetId}/{localId}" }, produces = {
	    HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8 })
    @ApiOperation(notes = SwaggerConstants.DELETE_ITEM_NOTE, value = "Delete a item from the set", nickname = "delete item", response = java.lang.Void.class)
    public ResponseEntity<String> deleteItemFromUserSet(
	    @PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
	    @PathVariable(value = WebUserSetFields.PATH_PARAM_DATASET_ID) String datasetId,
	    @PathVariable(value = WebUserSetFields.PATH_PARAM_LOCAL_ID) String localId,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_PROFILE, required = false, defaultValue = CommonApiConstants.PROFILE_MINIMAL) String profile,
	    HttpServletRequest request) throws HttpException {

	// check user credentials, if invalid respond with HTTP 401,
	// or if unauthorized respond with HTTP 403
	Authentication authentication = verifyWriteAccess(Operations.DELETE, request);
	return deleteItemFromUserSet(request, authentication, identifier, datasetId, localId, profile);
    }

    /**
     * This method validates input values and deletes item from a user set.
     * 
     * @param request
     * @param authentication The Authentication object
     * @param identifier     The identifier of a user set
     * @param datasetId      The identifier of the dataset, typically a number
     * @param localId        The local identifier within the provider
     * @param userSet        The user set fields to update in JSON format e.g. title
     *                       or description
     * @param profile        The profile definition
     * @return response entity that comprises response body, headers and status code
     * @throws HttpException
     */
    protected ResponseEntity<String> deleteItemFromUserSet(HttpServletRequest request, Authentication authentication,
	    String identifier, String datasetId, String localId, String profileStr) throws HttpException {

	try {
	    LdProfiles profile = getProfile(profileStr, request);

	    // check if the Set exists, if not respond with HTTP 404
	    // retrieve an existing user set based on its identifier
	    UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

	    // check if the user is the owner/creator of the set or admin,
		// OR Editor for Entity sets, otherwise respond with
	    // 403
	    getUserSetService().checkPermissionToUpdate(existingUserSet, authentication, true);

		// for entity user sets, add users with 'editor' role as contributors
		addContributorForEntitySet(existingUserSet, authentication);

	    String newItem = UserSetUtils.buildItemUrl(WebUserSetFields.BASE_ITEM_URL, datasetId, localId);

	    // check if item already exists in the Set, if not respond with HTTP 404
	    boolean hasItem = existingUserSet.getItems() != null && existingUserSet.getItems().contains(newItem);
	    if (!hasItem) {
		// TODO: consider changing to generateItemNotFoundResponse
		throw new UserSetNotFoundException(UserSetI18nConstants.USERSET_ITEM_NOT_FOUND,
			UserSetI18nConstants.USERSET_ITEM_NOT_FOUND,
			new String[] { datasetId + "/" + localId, identifier });
	    }

	    // check if it is a pinned item, decrease the counter by 1 for entity sets
        if(existingUserSet.isEntityBestItemsSet()) {
            int currentPosition = existingUserSet.getItems().indexOf(newItem);
            if (currentPosition < existingUserSet.getPinned()) {
                existingUserSet.setPinned(existingUserSet.getPinned() - 1);
            }
        }
	    // if already exists - remove item and update modified date
	    existingUserSet.getItems().remove(newItem);

	    // update an existing user set
	    UserSet updatedUserSet = getUserSetService().updateItemList(existingUserSet);

	    // serialize to JsonLd
	    String serializedUserSetJsonLdStr = serializeUserSet(profile, updatedUserSet);
	    String etag = generateETag(updatedUserSet.getModified(), WebFields.FORMAT_JSONLD, getApiVersion());

	    // respond with HTTP 200 containing the updated Set description as body.
	    // serialize Set in JSON-LD following the requested profile
	    // (if not indicated assume the default, ie. minimal)
	    // build response entity with headers
	    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
	    headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_PPGHD);
	    headers.add(UserSetHttpHeaders.PREFERENCE_APPLIED, profile.getPreferHeaderValue());
	    headers.add(UserSetHttpHeaders.ETAG, etag);

	    return new ResponseEntity<>(serializedUserSetJsonLdStr, headers, HttpStatus.OK);
	} catch (UserSetValidationException | UserSetInstantiationException e) {
	    throw new RequestBodyValidationException(UserSetI18nConstants.USERSET_CANT_PARSE_BODY,
		    new String[] { e.getMessage() }, e);
	} catch (HttpException e) {
	    throw e;
	} catch (RuntimeException | IOException e) {
	    throw new InternalServerException(e);
	}
    }

    @DeleteMapping(value = { "/set/{identifier}" })
    @ApiOperation(value = "Delete an existing user set", nickname = "delete", response = java.lang.Void.class)
    public ResponseEntity<String> deleteUserSet(
	    @PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier, HttpServletRequest request)
	    throws HttpException {

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
     * @param wsKey
     * @param userToken
     * @throws HttpException
     */
    protected ResponseEntity<String> deleteUserSet(HttpServletRequest request, String identifier,
	    Authentication authentication) throws HttpException {

	try {
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
	    String eTagOrigin = generateETag(existingUserSet.getModified(), WebFields.FORMAT_JSONLD, getApiVersion());
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
	    headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
	    headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_RESOURCE);
	    headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_GPPD);

	    return new ResponseEntity<>(identifier, headers, httpStatus);
	} catch (HttpException e) {
	    throw e;
	} catch (RuntimeException e) {
	    throw new InternalServerException(e);
	}
    }

	/**
	 * Deletes the user sets :
	 * 1) With admin role and creator. Deletes all the associated user sets
	 * OR
	 * 2) Deletes my user sets by getting the creatorId from the userToken
	 *
	 * @param creator
	 * @param request
	 * @return
	 * @throws HttpException
	 */
	@DeleteMapping(value = { "/set/" })
	@ApiOperation(value = "Delete sets associated with user", nickname = "delete associated user's sets", response = java.lang.Void.class)
	public ResponseEntity<String> deleteUserAssociatedSet(
			@RequestParam(value = WebUserSetFields.PATH_PARAM_CREATOR_ID, required = false) String creator,
			HttpServletRequest request) throws HttpException {
		// check user credentials, if invalid respond with HTTP 401,
		// or if unauthorized respond with HTTP 403
		Authentication authentication = verifyWriteAccess(Operations.DELETE, request);
		return deleteUserAssociatedSets(getCreatorId(authentication, creator));
	}

	/**
	 * gets the creatorId for the delete functionality
	 * if creator is null, gets the userId from the token OR
	 * if creator is empty , throws 400 Bad request
	 * if creator is passed and role is not admin, throws 403 Forbidden
	 * if creator is passed and role is admin, returns the creatorId
	 *
	 * @param authentication
	 * @param creatorId
	 * @return
	 * @throws RequestValidationException, ApplicationAuthenticationException
	 */
	private String getCreatorId(Authentication authentication, String creatorId) throws RequestValidationException, ApplicationAuthenticationException {
		// if creator is empty : Delete my sets is invoked.
		// get the creatorId from the userToken
		if (creatorId == null) {
			return getUserSetService().getUserId(authentication);
		} else {
			// if creatorId is empty, return 400 Bad Request
			if (creatorId.isEmpty()){
				throw new RequestValidationException(I18nConstants.INVALID_PARAM_VALUE,
						new String[]{"Creator Id is empty"});
			}
			// if creator is passed, verify if the user is admin.
			// Owner/User can not perform this action
			if (! getUserSetService().isAdmin(authentication)) {
				throw new ApplicationAuthenticationException(I18nConstants.OPERATION_NOT_AUTHORIZED,
						I18nConstants.OPERATION_NOT_AUTHORIZED, new String[]{
						"Only admins are authorized to perform this operation."},
						HttpStatus.FORBIDDEN);
			}
			if (! StringUtils.startsWith(creatorId, "http")) {
				return UserSetUtils.buildUserUri(creatorId);
			}
		}
		return creatorId;
	}
    /**
     * This method implements removal of all sets associated to a user
     *
     * @param creatorId
     * @throws HttpException
     */
    protected ResponseEntity<String> deleteUserAssociatedSets(String creatorId) throws HttpException {
	try {
	    List<PersistentUserSet> userSets = getUserSetService().getUserSetByCreatorId(creatorId);

	    // verify if the user sets are associated with the creatorId
	    for (UserSet userset : userSets){
	    	if (! StringUtils.equals(creatorId, userset.getCreator().getHttpUrl())) {
	    		throw new OperationAuthorizationException(I18nConstants.OPERATION_NOT_AUTHORIZED,
						I18nConstants.OPERATION_NOT_AUTHORIZED, new String[]{
								"Only user associated sets can be deleted"},
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

	    headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_PGD);
	    headers.add(UserSetHttpHeaders.CACHE_CONTROL, UserSetHttpHeaders.VALUE_NO_CAHCHE_STORE_REVALIDATE);

	    return new ResponseEntity<>(headers, httpStatus);
	} catch (HttpException e) {
	    throw e;
	} catch (RuntimeException e) {
	    throw new InternalServerException(e);
	}
    }
}