package eu.europeana.set.web.service.controller.jsonld;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonParseException;

import eu.europeana.api.common.config.I18nConstants;
import eu.europeana.api.common.config.swagger.SwaggerSelect;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.exception.InternalServerException;
import eu.europeana.api.commons.web.exception.ParamValidationException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.set.definitions.exception.UserSetAttributeInstantiationException;
import eu.europeana.set.definitions.exception.UserSetHeaderValidationException;
import eu.europeana.set.definitions.exception.UserSetInstantiationException;
import eu.europeana.set.definitions.exception.UserSetValidationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.web.exception.authorization.OperationAuthorizationException;
import eu.europeana.set.web.exception.request.RequestBodyValidationException;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.http.SwaggerConstants;
import eu.europeana.set.web.http.UserSetHttpHeaders;
import eu.europeana.set.web.model.WebSoftwareAgent;
import eu.europeana.set.web.model.vocabulary.Operations;
import eu.europeana.set.web.service.controller.BaseRest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * This class implements the User Set - REST API
 */

@Controller
@SwaggerSelect
@Api(tags = "Web User Set API", description = " ")
public class WebUserSetRest extends BaseRest {
	
	@RequestMapping(value = "/set/", method = RequestMethod.POST, 
			produces = {HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8})
	@ApiOperation(notes = SwaggerConstants.SAMPLES_JSONLD, value = "Create user set", nickname = "createUserSet", response = java.lang.Void.class)
	public ResponseEntity<String> createUserSet(
			@RequestParam(value = WebUserSetFields.PARAM_WSKEY, required = false) String wskey,
			@RequestBody String userSet,
			@RequestParam(value = WebUserSetFields.USER_TOKEN, required = false, defaultValue = WebUserSetFields.USER_ANONYMOUNS) String userToken,			
			@RequestParam(value = WebUserSetFields.PROFILE, required = false, defaultValue = WebUserSetFields.PROFILE_MINIMAL) String profile,			
			HttpServletRequest request)
					throws HttpException {
				
		return storeUserSet(wskey, userSet, userToken, profile, request);
	}
	
	/**
	 * This method requests parsing of a user set in JsonLd format to a UserSet object
	 * @param wsKey The API key
	 * @param userSetJsonLdStr The user set in JsonLd format
	 * @param userToken The user identifier
	 * @param profile The profile definition
	 * @param request HTTP request
	 * @return response entity that comprises response body, headers and status code
	 * @throws HttpException
	 */
	protected ResponseEntity<String> storeUserSet(String wsKey, String userSetJsonLdStr, String userToken,
			String profileStr, HttpServletRequest request) throws HttpException {
		try {
			// validate user - check user credentials (all registered users can create) 
			// if invalid respond with HTTP 401 or if unauthorized respond with HTTP 403;
			// Check client access (a valid "wskey" must be provided)
			validateApiKey(wsKey);

			userToken = getUserToken(userToken, request);
			LdProfiles profile = getProfile(profileStr, request);

			// authorize user
			getAuthorizationService().authorizeUser(userToken, wsKey, null, Operations.CREATE);			
			
			// parse user set 
			UserSet webUserSet = getUserSetService().parseUserSetLd(userSetJsonLdStr);

			// validate and process the Set description for format and mandatory fields
			// if false respond with HTTP 400
			getUserSetService().validateWebUserSet(webUserSet);
			if(StringUtils.isEmpty(webUserSet.getContext()))
				webUserSet.setContext(WebUserSetFields.VALUE_CONTEXT_EUROPEANA_COLLECTION);

			Agent user = new WebSoftwareAgent();
			user.setName(userToken);			
			
			// SET DEFAULTS
			if (webUserSet.getCreator() == null)
				webUserSet.setCreator(user);
			
			// store the new Set with its respective id, together with all the containing items 
			// following the order given by the list
			// generate an identifier (in sequence) for the Set
			// generate and add a created and modified timestamp to the Set
			UserSet storedUserSet = getUserSetService().storeUserSet(webUserSet);

			String serializedUserSetJsonLdStr = serializeUserSet(profile, storedUserSet); 

			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
			headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
			headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_RESOURCE);
			headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_PG);
			headers.add(HttpHeaders.CACHE_CONTROL, UserSetHttpHeaders.VALUE_PRIVATE);
			// generate “ETag”;
			headers.add(HttpHeaders.ETAG, "" + storedUserSet.getModified().hashCode());

			ResponseEntity<String> response = new ResponseEntity<String>(
					serializedUserSetJsonLdStr, headers, HttpStatus.CREATED);

			return response;

		} catch (JsonParseException e) {
			throw new RequestBodyValidationException(I18nConstants.USERSET_CANT_PARSE_BODY, new String[]{e.getMessage()}, e);
		} catch (ParamValidationException e) {
			throw new ParamValidationException(e.getMessage(), e.getMessage(), 
					null);
		} catch (UserSetValidationException e) { 
			throw new RequestBodyValidationException(I18nConstants.USERSET_CANT_PARSE_BODY, new String[]{e.getMessage()}, e);
		} catch (UserSetAttributeInstantiationException e) {
			throw new RequestBodyValidationException(I18nConstants.USERSET_CANT_PARSE_BODY, new String[]{e.getMessage()}, e);
		} catch (UserSetInstantiationException e) {
			throw new HttpException(null, I18nConstants.USERSET_INVALID_BODY, null, HttpStatus.BAD_REQUEST, e); 
		} catch (UserSetHeaderValidationException e) {
			throw new HttpException(e.getMessage(),e.getMessage(),HttpStatus.BAD_REQUEST);
		} catch (HttpException e) {
			// avoid wrapping HttpExceptions
			throw e;
		} catch (Exception e) {
			throw new InternalServerException(e);
		}

	}

	@RequestMapping(value = { "/set/{identifier}", "/set/{identifier}.jsonld" }, 
			method = {RequestMethod.GET},
			produces = { HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8 }
			)
	@ApiOperation(notes = SwaggerConstants.SEARCH_HELP_NOTE, value = "Retrieve a user set", 
				nickname = "retrieve", response = java.lang.Void.class)
	public ResponseEntity<String> getUserSet(
			@RequestParam(value = WebUserSetFields.PARAM_WSKEY, required = false) String wskey,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
			@RequestParam(value = WebUserSetFields.USER_TOKEN, required = false, defaultValue = WebUserSetFields.USER_ANONYMOUNS) String userToken,			
			@RequestParam(value = WebUserSetFields.PROFILE, required = false, defaultValue = WebUserSetFields.PROFILE_MINIMAL) String profile,			
			HttpServletRequest request) throws HttpException {

		String action = "get:/set/{identifier}{.jsonld}";
		return getUserSet(wskey, profile, identifier, request, action);
	}

	/**
	 * This method retrieves an existing user set identified by given identifier, which is
	 * a number in string format.
	 * @param wskey The API key
	 * @param profile The profile definition
	 * @param identifier The identifier
	 * @param request HTTP request
	 * @param action The action describing the request
	 * @return response entity that comprises response body, headers and status code
	 * @throws HttpException
	 */
	private ResponseEntity<String> getUserSet(String wsKey, String profileStr, String identifier, 
			HttpServletRequest request, String action)
					throws HttpException {
		try {
			// check user credentials, if invalid respond with HTTP 401.
			// check client access (a valid "wskey" must be provided)
			validateApiKey(wsKey);
			
			LdProfiles profile = getProfile(profileStr, request);

			// retrieve a Set based on its identifier - process query
			// if the Set doesn’t exist, respond with HTTP 404
			// if the Set is disabled respond with HTTP 410
			UserSet userSet = getUserSetService().getUserSetById(identifier);
			String userSetJsonLdStr = serializeUserSet(profile, userSet); 

			// build response
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
			headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
			headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_RESOURCE);
			headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_GPD);
			headers.add(HttpHeaders.VARY, UserSetHttpHeaders.PREFER);
			headers.add(HttpHeaders.PREFER, profile.getPreferHeaderValue());
			// generate “ETag”;
			headers.add(HttpHeaders.ETAG, "" + userSet.getModified().hashCode());

			ResponseEntity<String> response = new ResponseEntity<String>(userSetJsonLdStr, headers, HttpStatus.OK);

			return response;

		} catch (RuntimeException e) {
			// not found ..
			throw new InternalServerException(e);
		} catch (HttpException e) {
			// avoid wrapping http exception
			throw e;
		} catch (UserSetHeaderValidationException e) {
			throw new HttpException(e.getMessage(),e.getMessage(),HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			throw new InternalServerException(e);
		}
	}
	
	@RequestMapping(value = {"/set/{identifier}"}, method = RequestMethod.PUT, 
			produces = {HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8})
	@ApiOperation(notes = SwaggerConstants.UPDATE_SAMPLES_JSONLD, value = "Update an existing user set", nickname = "update", response = java.lang.Void.class)
	public ResponseEntity<String> updateUserSet(@RequestParam(value = WebUserSetFields.PARAM_WSKEY, required = false) String wskey,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
			@RequestBody String userSet,
			@RequestParam(value = WebUserSetFields.USER_TOKEN, required = false, defaultValue = WebUserSetFields.USER_ANONYMOUNS) String userToken,
			@RequestParam(value = WebUserSetFields.PROFILE, required = false, defaultValue = WebUserSetFields.PROFILE_MINIMAL) String profile,			
			HttpServletRequest request
			) throws HttpException {
		
		String action = "put:/set/{identifier}";
		return updateUserSet(request, wskey, identifier, userSet, userToken, profile, action);
	}
		
	/**
	 * This method validates input values, retrieves user set object and
	 * updates it.
	 * 
	 * @param request
	 * @param wskey The API key
	 * @param identifier The identifier
	 * @param userSet The user set fields to update in JSON format e.g. title or description
	 * @param profile The profile definition
	 * @param action The action describing the request
	 * @return response entity that comprises response body, headers and status code
	 * @throws HttpException
	 */
	protected ResponseEntity<String> updateUserSet(HttpServletRequest request, String wsKey, String identifier,
			String userSetJsonLdStr, String userToken, String profileStr, String action) throws HttpException {

		try {
			// check user credentials, if invalid respond with HTTP 401,
			// check client access (a valid "wskey" must be provided)
			// Check client access (a valid "wskey" must be provided)
			validateApiKey(wsKey);

			userToken = getUserToken(userToken, request);
			LdProfiles profile = getProfile(profileStr, request);
			
			// authorize user
			getAuthorizationService().authorizeUser(userToken, wsKey, identifier, Operations.UPDATE);

			// check if the Set exists, if not respond with HTTP 404
			// retrieve an existing user set based on its identifier
			UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

			// check if the user is the owner of the set or admin, otherwise respond with 403
			hasModifyRights(existingUserSet, wsKey, userToken);
			
			// check timestamp if provided within the “If-Match” HTTP header, if false respond with HTTP 412
			checkHeaderTimestamp(request, existingUserSet);

			// check if the Set is disabled, respond with HTTP 410
			HttpStatus httpStatus = null;
			int modifiedStr = 0;
			String serializedUserSetJsonLdStr = "";
			if (existingUserSet.isDisabled()) { 
				httpStatus = HttpStatus.GONE;
			} else {			
				// parse fields of the new user set to an object
				UserSet newUserSet = getUserSetService().parseUserSetLd(userSetJsonLdStr);
				
				// validate and process the Set description for format and mandatory fields
				// if false respond with HTTP 400
				getUserSetService().validateWebUserSet(newUserSet);
				//validate items 
				validateUpdateItemsByProfile(existingUserSet, newUserSet, profile);
				//remove duplicated items
				getUserSetService().removeItemDuplicates(newUserSet);
				
				// Respond with HTTP 200
	            // update an existing user set. merge user sets - insert new fields in existing object
				// update pagination
				// generate and add a created and modified timestamp to the Set;
				existingUserSet.setModified(newUserSet.getModified());
				UserSet updatedUserSet = getUserSetService().updateUserSet(
						(PersistentUserSet) existingUserSet, newUserSet);
				
				modifiedStr = updatedUserSet.getModified().hashCode();			
				serializedUserSetJsonLdStr = serializeUserSet(profile, updatedUserSet); 
		        httpStatus = HttpStatus.OK;
			}
			
			// build response entity with headers
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
			headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
			headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_RESOURCE);
			headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_GPD);
			headers.add(HttpHeaders.VARY, UserSetHttpHeaders.PREFER);
			headers.add(HttpHeaders.PREFER, profile.getPreferHeaderValue());
			// generate “ETag”;
			headers.add(HttpHeaders.ETAG, "" + modifiedStr);

			ResponseEntity<String> response = new ResponseEntity<String>(
					serializedUserSetJsonLdStr, headers, httpStatus);

			return response;
			
		} catch (UserSetValidationException e) { 
			throw new RequestBodyValidationException(I18nConstants.USERSET_CANT_PARSE_BODY, new String[]{e.getMessage()}, e);
		} catch (UserSetInstantiationException e) {
			throw new RequestBodyValidationException(I18nConstants.USERSET_CANT_PARSE_BODY, new String[]{e.getMessage()}, e);
		} catch (UserSetHeaderValidationException e) {
			throw new HttpException(e.getMessage(),e.getMessage(),HttpStatus.BAD_REQUEST);
		} catch (HttpException e) {
				//TODO: change this when OAUTH is implemented and the user information is available in service
				throw e;
		} catch (Exception e) {
			throw new InternalServerException(e);
		}
	}

	private void validateUpdateItemsByProfile(UserSet storedUserSet, UserSet updateUserSet, LdProfiles profile)
			throws ApplicationAuthenticationException {
		// update the Set based on its identifier (replace member items with the new items 
		// that are present in the Set description only when a profile is indicated and is 
		// different from "ldp:PreferMinimalContainer" is referred in the "Prefer" header)
		// if the provided userset contains a list of items and the profile is set to minimal, 
		// respond with HTTP 412)
		if (LdProfiles.MINIMAL.equals(profile)) {
			if (updateUserSet.getItems() != null && updateUserSet.getItems().size() > 0) { // new user set contains items
				throw new ApplicationAuthenticationException(
					I18nConstants.USERSET_MINIMAL_UPDATE_PROFILE, I18nConstants.USERSET_MINIMAL_UPDATE_PROFILE,
					new String[] {}, HttpStatus.PRECONDITION_FAILED, null);	
			}		
		} else { // it is a minimal profile
			if (updateUserSet.getItems() == null || updateUserSet.getItems().size() == 0) { // new user set contains no items
				throw new ApplicationAuthenticationException(
					I18nConstants.USERSET_CONTAINS_NO_ITEMS, I18nConstants.USERSET_CONTAINS_NO_ITEMS,
					new String[] {}, HttpStatus.PRECONDITION_FAILED, null);	
			}
			storedUserSet.setItems(updateUserSet.getItems());
		}
	}

	@RequestMapping(value = {"/set/{identifier}/{datasetId}/{localId}"}, method = RequestMethod.PUT, 
			produces = {HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8})
	@ApiOperation(notes = SwaggerConstants.INSERT_ITEM_NOTE, value = "Insert item to an existing user set", nickname = "insert item", response = java.lang.Void.class)
	public ResponseEntity<String> insertItemIntoUserSet(@RequestParam(value = WebUserSetFields.PARAM_WSKEY, required = false) String wskey,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_DATASET_ID) String datasetId,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_LOCAL_ID) String localId,
			@RequestParam(value = WebUserSetFields.PATH_PARAM_POSITION, required=false) String position,
			@RequestParam(value = WebUserSetFields.USER_TOKEN, required = false, defaultValue = WebUserSetFields.USER_ANONYMOUNS) String userToken,
			@RequestParam(value = WebUserSetFields.PROFILE, required = false, defaultValue = WebUserSetFields.PROFILE_MINIMAL) String profile,			
			HttpServletRequest request
			) throws HttpException {
		
		String action = "put:/set/{identifier}/{dataset_id}/{local_id}?position=POSITION";
		return insertItemIntoUserSet(request, wskey, identifier, datasetId, localId, position, userToken, 
				profile, action);
	}
	
	/**
	 * This method validates input values, retrieves user set object and
	 * inserts item within user set to given position or at the end if no valid position provided.
	 * 
	 * @param request
	 * @param wskey The API key
	 * @param identifier The identifier of a user set
	 * @param datasetId The identifier of the dataset, typically a number
	 * @param localId The local identifier within the provider
	 * @param position The position in the existin item list
	 * @param userSet The user set fields to update in JSON format e.g. title or description
	 * @param profile The profile definition
	 * @param action The action describing the request
	 * @return response entity that comprises response body, headers and status code
	 * @throws HttpException
	 */
	protected ResponseEntity<String> insertItemIntoUserSet(HttpServletRequest request, String wsKey, 
			String identifier, String datasetId, String localId, String position, String userToken, 
			String profileStr, String action) throws HttpException {

		try {
			// check user credentials, if invalid respond with HTTP 401,
			//  or if unauthorized respond with HTTP 403
			// check client access (a valid "wskey" must be provided)
			validateApiKey(wsKey);

			userToken = getUserToken(userToken, request);
			LdProfiles profile = getProfile(profileStr, request);
			
			// authorize user
			getAuthorizationService().authorizeUser(userToken, wsKey, identifier, Operations.UPDATE);

			// check if the Set exists, if not respond with HTTP 404
			// retrieve an existing user set based on its identifier
			UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

			// check if the user is the owner of the set or admin, otherwise respond with 403
			hasModifyRights(existingUserSet, wsKey, userToken);
			
			// check timestamp if provided within the “If-Match” HTTP header, if false respond with HTTP 412
			checkHeaderTimestamp(request, existingUserSet);

			// check if the Set is disabled, respond with HTTP 410
			HttpStatus httpStatus = null;
			String serializedUserSetJsonLdStr = "";
			
			if (existingUserSet.isDisabled()) { 
				httpStatus = HttpStatus.GONE;
			} else {			
				UserSet extUserSet = insertItem(datasetId, localId, position, existingUserSet);
				serializedUserSetJsonLdStr = serializeUserSet(profile, extUserSet); 
		        httpStatus = HttpStatus.OK;
			}
			
			// build response entity with headers
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
			headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_PPGHD);

			ResponseEntity<String> response = new ResponseEntity<String>(
					serializedUserSetJsonLdStr, headers, httpStatus);

			return response;

		} catch (UserSetValidationException e) { 
			throw new RequestBodyValidationException(I18nConstants.USERSET_CANT_PARSE_BODY, new String[]{e.getMessage()}, e);		
		} catch (UserSetInstantiationException e) {
			throw new RequestBodyValidationException(I18nConstants.USERSET_CANT_PARSE_BODY, new String[]{e.getMessage()}, e);
		} catch (UserSetHeaderValidationException e) {
			throw new HttpException(e.getMessage(),e.getMessage(),HttpStatus.BAD_REQUEST);
		} catch (HttpException e) {
			//TODO: change this when OAUTH is implemented and the user information is available in service
			throw e;
		} catch (Exception e) {
			throw new InternalServerException(e);
		}
	}

	/**
	 * This method enriches user set by provided item
	 * @param datasetId The id of dataset
	 * @param localId The id in collection
	 * @param position The position in item list
	 * @param existingUserSet
	 * @return user set enriched by new item
	 * @throws ApplicationAuthenticationException
	 */
	private UserSet insertItem(String datasetId, String localId, String position, UserSet existingUserSet)
			throws ApplicationAuthenticationException {
		// validate position 
		int positionInt = validatePosition(position, existingUserSet.getItems());

		// build new item URL
		String newItem = getUserSetService().buildIdentifierUrl(
				datasetId + "/" + localId, WebUserSetFields.BASE_ITEM_URL);

		// check if item already exists in the Set, if so remove it
		// insert item to Set in the indicated position (or last position if no position was indicated).
		UserSet extUserSet = null;
		if (existingUserSet.getItems().contains(newItem)) {
			int currentPos = existingUserSet.getItems().indexOf(newItem);
			if (currentPos == positionInt) {
				extUserSet = getUserSetService().fillPagination(existingUserSet);											
			} else {
				replaceItem(existingUserSet, positionInt, newItem);				
				extUserSet = updateItemList(existingUserSet);
			}
		} else {
			addNewItemToList(existingUserSet, positionInt, newItem);
			extUserSet = updateItemList(existingUserSet);			
		}
		return extUserSet;
	}

	private UserSet updateItemList(UserSet existingUserSet) {
		UserSet extUserSet;
		getUserSetService().updateUserSetPagination(existingUserSet);

		// generate and add a created and modified timestamp to the Set
		existingUserSet.setModified(new Date());
		
		// Respond with HTTP 200
		// update an existing user set. merge user sets - insert new fields in existing object
		UserSet updatedUserSet = getUserSetService().updateUserSet(
				(PersistentUserSet) existingUserSet, null);
		extUserSet = getUserSetService().fillPagination(updatedUserSet);
		return extUserSet;
	}

	/**
	 * This method replaces item in user set
	 * @param existingUserSet
	 * @param positionInt
	 * @param newItem
	 */
	private void replaceItem(UserSet existingUserSet, int positionInt, String newItem) {
		existingUserSet.getItems().remove(newItem);
		addNewItemToList(existingUserSet, positionInt, newItem);
	}

	/**
	 * Add item to the list in given position if provided.
	 * @param existingUserSet
	 * @param positionInt
	 * @param newItem
	 */
	private void addNewItemToList(UserSet existingUserSet, int positionInt, String newItem) {
		if (positionInt == -1) {
			existingUserSet.getItems().add(newItem);
		} else {
			existingUserSet.getItems().add(positionInt, newItem);					
		}
	}

	@RequestMapping(value = {"/set/{identifier}/{datasetId}/{localId}"}, method = {RequestMethod.GET, RequestMethod.HEAD}, 
			produces = { HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8})
	@ApiOperation(notes = SwaggerConstants.CHECK_ITEM_NOTE, value = "Check if item is member of the Set", nickname = "check item", response = java.lang.Void.class)
	public ResponseEntity<String> isItemInUserSet(@RequestParam(value = WebUserSetFields.PARAM_WSKEY, required = false) String wskey,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_DATASET_ID) String datasetId,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_LOCAL_ID) String localId,
			@RequestParam(value = WebUserSetFields.USER_TOKEN, required = false, defaultValue = WebUserSetFields.USER_ANONYMOUNS) String userToken,
			@RequestParam(value = WebUserSetFields.PROFILE, required = false, defaultValue = WebUserSetFields.PROFILE_MINIMAL) String profile,			
			HttpServletRequest request
			) throws HttpException {
		
		String action = "get:/set/{identifier}/{dataset_id}/{local_id}";
		return isItemInUserSet(request, wskey, identifier, datasetId, localId, userToken, action);
	}
	
	/**
	 * This method validates input values and checks if item is already in a user set.
	 * 
	 * @param request
	 * @param wskey The API key
	 * @param identifier The identifier of a user set
	 * @param datasetId The identifier of the dataset, typically a number
	 * @param localId The local identifier within the provider
	 * @param userSet The user set fields to update in JSON format e.g. title or description
	 * @param action The action describing the request
	 * @return response entity that comprises response body, headers and status code
	 * @throws HttpException
	 */
	protected ResponseEntity<String> isItemInUserSet(HttpServletRequest request, String wsKey, 
			String identifier, String datasetId, String localId, String userToken, 
			String action) throws HttpException {

		try {
			// check user credentials, if invalid respond with HTTP 401,
			//  or if unauthorized respond with HTTP 403
			// check client access (a valid "wskey" must be provided)
			validateApiKey(wsKey);

			// authorize user
			getAuthorizationService().authorizeUser(userToken, wsKey, identifier, Operations.RETRIEVE);

			// check if the Set exists, if not respond with HTTP 404
			// retrieve an existing user set based on its identifier
			UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

			// check if the Set is disabled, respond with HTTP 410
			HttpStatus httpStatus = null;
			
			if (existingUserSet.isDisabled()) { 
				httpStatus = HttpStatus.GONE;
			} else {			
				String newItem = getUserSetService().buildIdentifierUrl(
						datasetId + "/" + localId, WebUserSetFields.BASE_ITEM_URL);

				// check if item already exists in the Set, if so respond with 
				// HTTP 200, otherwise respond with HTTP 404.
				// check if item already exists in the Set, if so remove it
				if (existingUserSet.getItems().contains(newItem)) {
			        httpStatus = HttpStatus.NO_CONTENT;
				} else {
			        httpStatus = HttpStatus.NOT_FOUND;
				}
			}
			
			// build response entity with headers
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
			headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_PPGHD);

			ResponseEntity<String> response = new ResponseEntity<String>(
					"", headers, httpStatus);

			return response;

		} catch (UserSetValidationException e) {
			throw new RequestBodyValidationException(I18nConstants.USERSET_CANT_PARSE_BODY, new String[]{e.getMessage()}, e);		
		} catch (UserSetInstantiationException e) {
			throw new RequestBodyValidationException(I18nConstants.USERSET_CANT_PARSE_BODY, new String[]{e.getMessage()}, e);
		} catch (HttpException e) {
			//TODO: change this when OAUTH is implemented and the user information is available in service
			throw e;
		} catch (Exception e) {
			throw new InternalServerException(e);
		}
	}

	@RequestMapping(value = {"/set/{identifier}/{datasetId}/{localId}"}, method = RequestMethod.DELETE, 
			produces = { HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8})
	@ApiOperation(notes = SwaggerConstants.DELETE_ITEM_NOTE, value = "Delete a item from the set", nickname = "delete item", response = java.lang.Void.class)
	public ResponseEntity<String> deleteItemFromUserSet(@RequestParam(value = WebUserSetFields.PARAM_WSKEY, required = false) String wskey,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_DATASET_ID) String datasetId,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_LOCAL_ID) String localId,
			@RequestParam(value = WebUserSetFields.USER_TOKEN, required = false, defaultValue = WebUserSetFields.USER_ANONYMOUNS) String userToken,
			@RequestParam(value = WebUserSetFields.PROFILE, required = false, defaultValue = WebUserSetFields.PROFILE_MINIMAL) String profile,			
			HttpServletRequest request
			) throws HttpException {
		
		String action = "delete:/set/{identifier}/{dataset_id}/{local_id}";
		return deleteItemFromUserSet(request, wskey, identifier, datasetId, localId, userToken, profile, action);
	}
	
	/**
	 * This method validates input values and deletes item from a user set.
	 * 
	 * @param request
	 * @param wskey The API key
	 * @param identifier The identifier of a user set
	 * @param datasetId The identifier of the dataset, typically a number
	 * @param localId The local identifier within the provider
	 * @param userSet The user set fields to update in JSON format e.g. title or description
	 * @param profile The profile definition
	 * @param action The action describing the request
	 * @return response entity that comprises response body, headers and status code
	 * @throws HttpException
	 */
	protected ResponseEntity<String> deleteItemFromUserSet(HttpServletRequest request, String wsKey, 
			String identifier, String datasetId, String localId, String userToken, 
			String profileStr, String action) throws HttpException {
		
		try {
			// check user credentials, if invalid respond with HTTP 401,
			//  or if unauthorized respond with HTTP 403
			// check client access (a valid "wskey" must be provided)
			validateApiKey(wsKey);

			userToken = getUserToken(userToken, request);
			LdProfiles profile = getProfile(profileStr, request);
			
			// authorize user
			getAuthorizationService().authorizeUser(userToken, wsKey, identifier, Operations.DELETE);

			// check if the Set exists, if not respond with HTTP 404
			// retrieve an existing user set based on its identifier
			UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

			// check if the user is the owner of the set or admin, otherwise respond with 403
			hasModifyRights(existingUserSet, wsKey, userToken);
			
			// check if the Set is disabled, respond with HTTP 410
			HttpStatus httpStatus = null;
			String serializedUserSetJsonLdStr = "";
			if (existingUserSet.isDisabled()) { 
				httpStatus = HttpStatus.GONE;
			} else {			
				String newItem = getUserSetService().buildIdentifierUrl(
						datasetId + "/" + localId, WebUserSetFields.BASE_ITEM_URL);

				// check if item already exists in the Set, if not respond with HTTP 404
				if (existingUserSet.getItems() != null && existingUserSet.getItems().contains(newItem)) {
					// if already exists - remove item and update modified date
					existingUserSet.getItems().remove(newItem);
					Date now = new Date();				
					existingUserSet.setModified(now);
					
		            // update an existing user set
					UserSet existingUserSetPaginated = getUserSetService().updatePagination(existingUserSet);				
					UserSet updatedUserSet = getUserSetService().updateUserSet(
							(PersistentUserSet) existingUserSetPaginated, null);
				
					// serialize to JsonLd
					serializedUserSetJsonLdStr = serializeUserSet(profile, updatedUserSet); 

			        // respond with HTTP 200 containing the updated Set description as body.
					// serialize Set in JSON-LD following the requested profile 
			        // (if not indicated assume the default, ie. minimal) 
			        httpStatus = HttpStatus.OK;
				} else {
					throw new UserSetNotFoundException(I18nConstants.USERSET_ITEM_NOT_FOUND, 
							I18nConstants.USERSET_ITEM_NOT_FOUND, new String[] {datasetId + "/" + localId, identifier});
				}
			}
			
			// build response entity with headers
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
			headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_PPGHD);

			ResponseEntity<String> response = new ResponseEntity<String>(
					serializedUserSetJsonLdStr, headers, httpStatus);

			return response;

		} catch (UserSetValidationException e) { 
			throw new RequestBodyValidationException(I18nConstants.USERSET_CANT_PARSE_BODY, new String[]{e.getMessage()}, e);
		} catch (UserSetInstantiationException e) {
			throw new RequestBodyValidationException(I18nConstants.USERSET_CANT_PARSE_BODY, new String[]{e.getMessage()}, e);
		} catch (UserSetHeaderValidationException e) {
			throw new HttpException(e.getMessage(),e.getMessage(),HttpStatus.BAD_REQUEST);
		} catch (HttpException e) {
			//TODO: change this when OAUTH is implemented and the user information is available in service
			throw e;
		} catch (Exception e) {
			throw new InternalServerException(e);
		}
	}

	@RequestMapping(value = {"/set/{identifier}"}, method = RequestMethod.DELETE)
	@ApiOperation(value = "Delete an existing user set", nickname = "delete", response = java.lang.Void.class)
	public ResponseEntity<String> deleteUserSet(
			@RequestParam(value = WebUserSetFields.PARAM_WSKEY, required = false) String apiKey,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
			@RequestParam(value = WebUserSetFields.USER_TOKEN, required = false, defaultValue = WebUserSetFields.USER_ANONYMOUNS) String userToken,
			HttpServletRequest request
			) throws HttpException {

		userToken = getUserToken(userToken, request);
				
		return deleteUserSet(request, identifier, apiKey, userToken);
	}
	
	/**
	 * This method implements removal of a user set 
	 * @param request
	 * @param identifier
	 * @param wsKey
	 * @param userToken
	 * @throws HttpException
	 */
	protected ResponseEntity<String> deleteUserSet(HttpServletRequest request, String identifier, String wsKey, String userToken)
			throws HttpException {

		try {
			// check user credentials, if invalid respond with HTTP 401,
			// check client access (a valid "wskey" must be provided)
			// Check client access (a valid "wskey" must be provided)
			validateApiKey(wsKey);

			// authorize user or if unauthorized respond with HTTP 403
			getAuthorizationService().authorizeUser(userToken, wsKey, identifier, Operations.DELETE);

			// retrieve a user set based on its identifier
			// if the Set doesn’t exist, respond with HTTP 404
			UserSet existingUserSet = getUserSetService().getUserSetById(identifier, false);

			// check that only the admins and the owners of the user sets are allowed to delete the user set. 
			// in the case of regular users (not admins), the autorization method must check if the users 
			// that calls the deletion (i.e. identified by provided user token) is the same user as the creator 
			// of the user set
			hasModifyRights(existingUserSet, wsKey, userToken);

			// check timestamp if provided within the "If-Match" HTTP header, if false respond with HTTP 412
			checkHeaderTimestamp(request, existingUserSet);
						
			// if the user set is disabled and the user is not an admin, respond with HTTP 410
			HttpStatus httpStatus = null;
			if (existingUserSet.isDisabled()) {
				if (!isAdmin(wsKey, userToken)) { 
					// if the user is the owner, the response should be 410
					if (isOwner(existingUserSet, userToken)) {
//						httpStatus = HttpStatus.GONE;	
						throw new OperationAuthorizationException(I18nConstants.USERSET_ALREADY_DISABLED, 
								I18nConstants.USERSET_ALREADY_DISABLED, 
								new String[]{existingUserSet.getIdentifier()},
								HttpStatus.GONE);
						
					} else {
						// if the user is a registered user but not the owner, the response should be 401 (unathorized)
						httpStatus = HttpStatus.UNAUTHORIZED;					
					}
				} else {
					// if the user is admin, the set should be permanently deleted and 204 should be returned
					getUserSetService().deleteUserSet(existingUserSet.getIdentifier());
					httpStatus = HttpStatus.NO_CONTENT;
				}
			} else {			
				// if the user is an Administrator then permanently remove item 
				// (and all items that are members of the user set)
				 httpStatus = HttpStatus.NO_CONTENT;
				 if (isAdmin(wsKey, userToken)) {
					 getUserSetService().deleteUserSet(existingUserSet.getIdentifier());
				 } else { // otherwise flag it as disabled
  					 if (isOwner(existingUserSet, userToken)) {
  						 getUserSetService().disableUserSet(existingUserSet);
  					 } else {
 						// if the user is a registered user but not the owner, the response should be 401 (unathorized)
 						httpStatus = HttpStatus.UNAUTHORIZED;					
  					 }
				 }
			}			
			// build response entity with headers
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
			headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
			headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_RESOURCE);
			headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_GPPD);

			ResponseEntity<String> response = new ResponseEntity<String>(
					identifier, headers, httpStatus);

			return response;			
		} catch (HttpException e) {
			//TODO: change this when OAUTH is implemented and the user information is available in service
			throw e;
		} catch (Exception e) {
			throw new InternalServerException(e);
		}
	}
	
}