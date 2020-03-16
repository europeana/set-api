package eu.europeana.set.web.service.controller.jsonld;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.set.definitions.exception.UserSetAttributeInstantiationException;
import eu.europeana.set.definitions.exception.UserSetInstantiationException;
import eu.europeana.set.definitions.exception.UserSetValidationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
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
			@RequestBody String userSet,
			@RequestParam(value = WebUserSetFields.PROFILE, required = false, defaultValue = WebUserSetFields.PROFILE_MINIMAL) String profile,			
			HttpServletRequest request)
					throws HttpException {
				
		return storeUserSet(userSet, profile, request);
	}
	
	/**
	 * This method requests parsing of a user set in JsonLd format to a UserSet object
	 * @param userSetJsonLdStr The user set in JsonLd format
	 * @param profile The profile definition
	 * @param request HTTP request
	 * @return response entity that comprises response body, headers and status code
	 * @throws HttpException
	 */
	protected ResponseEntity<String> storeUserSet(String userSetJsonLdStr, 
			String profileStr, HttpServletRequest request) throws HttpException {
		try {
			// validate user - check user credentials (all registered users can create) 
			// if invalid respond with HTTP 401 or if unauthorized respond with HTTP 403;
		    // verify access rights
			Authentication authentication = verifyWriteAccess(Operations.CREATE, request);
		    String userId = authentication.getPrincipal().toString();

		    LdProfiles profile = getProfile(profileStr, request);

			// parse user set 
			UserSet webUserSet = getUserSetService().parseUserSetLd(userSetJsonLdStr);

			// validate and process the Set description for format and mandatory fields
			// if false respond with HTTP 400
			getUserSetService().validateWebUserSet(webUserSet);
			if(StringUtils.isEmpty(webUserSet.getContext()))
				webUserSet.setContext(WebUserSetFields.VALUE_CONTEXT_EUROPEANA_COLLECTION);

			Agent user = new WebSoftwareAgent();
			user.setName(userId);			
			
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
			headers.add(HttpHeaders.CACHE_CONTROL, UserSetHttpHeaders.VALUE_NO_CAHCHE_STORE_REVALIDATE);
			// generate “ETag”;
			headers.add(HttpHeaders.ETAG, "" + storedUserSet.getModified().hashCode());
			headers.add(UserSetHttpHeaders.PREFERENCE_APPLIED, profile.getPreferHeaderValue());
			
			ResponseEntity<String> response = new ResponseEntity<String>(
					serializedUserSetJsonLdStr, headers, HttpStatus.CREATED);

			return response;

		} catch (JsonParseException e) {
			throw new RequestBodyValidationException(I18nConstants.USERSET_CANT_PARSE_BODY, new String[]{e.getMessage()}, e);
		} catch (UserSetValidationException e) { 
			throw new RequestBodyValidationException(I18nConstants.USERSET_CANT_PARSE_BODY, new String[]{e.getMessage()}, e);
		} catch (UserSetAttributeInstantiationException e) {
			throw new RequestBodyValidationException(I18nConstants.USERSET_CANT_PARSE_BODY, new String[]{e.getMessage()}, e);
		} catch (UserSetInstantiationException e) {
			throw new HttpException(null, I18nConstants.USERSET_INVALID_BODY, null, HttpStatus.BAD_REQUEST, e); 
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
			@PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
			@RequestParam(value = WebUserSetFields.PROFILE, required = false, defaultValue = WebUserSetFields.PROFILE_MINIMAL) String profile,			
			HttpServletRequest request) throws HttpException {

		String action = "get:/set/{identifier}{.jsonld}";
		return getUserSet(profile, identifier, request, action);
	}

	/**
	 * This method retrieves an existing user set identified by given identifier, which is
	 * a number in string format.
	 * @param profile The profile definition
	 * @param identifier The identifier
	 * @param request HTTP request
	 * @param action The action describing the request
	 * @return response entity that comprises response body, headers and status code
	 * @throws HttpException
	 */
	private ResponseEntity<String> getUserSet(String profileStr, String identifier, 
			HttpServletRequest request, String action)
					throws HttpException {
		try {
			// check user credentials, if invalid respond with HTTP 401.
		    // verify access rights
		    verifyReadAccess(request);
			
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
			headers.add(UserSetHttpHeaders.PREFERENCE_APPLIED, profile.getPreferHeaderValue());
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
		} catch (Exception e) {
			throw new InternalServerException(e);
		}
	}
	
	@RequestMapping(value = {"/set/{identifier}"}, method = RequestMethod.PUT, 
			produces = {HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8})
	@ApiOperation(notes = SwaggerConstants.UPDATE_SAMPLES_JSONLD, value = "Update an existing user set", nickname = "update", response = java.lang.Void.class)
	public ResponseEntity<String> updateUserSet(
			@PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
			@RequestBody String userSet,
			@RequestParam(value = WebUserSetFields.PROFILE, required = false, defaultValue = WebUserSetFields.PROFILE_MINIMAL) String profile,			
			HttpServletRequest request
			) throws HttpException {
		
		String action = "put:/set/{identifier}";
		return updateUserSet(request, identifier, userSet, profile, action);
	}
		
	/**
	 * This method validates input values, retrieves user set object and
	 * updates it.
	 * 
	 * @param request
	 * @param identifier The identifier
	 * @param profile The profile definition
	 * @param action The action describing the request
	 * @return response entity that comprises response body, headers and status code
	 * @throws HttpException
	 */
	protected ResponseEntity<String> updateUserSet(HttpServletRequest request, String identifier,
			String userSetJsonLdStr, String profileStr, String action) throws HttpException {

		try {
			// check user credentials, if invalid respond with HTTP 401,
		    // verify access rights
			verifyWriteAccess(Operations.UPDATE, request);

		    LdProfiles profile = getProfile(profileStr, request);
			
			// check if the Set exists, if not respond with HTTP 404
			// retrieve an existing user set based on its identifier
			UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

			// check timestamp if provided within the “If-Match” HTTP header, if false respond with HTTP 412
		    checkIfMatchHeader(existingUserSet.getModified().hashCode(), request);

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
			headers.add(UserSetHttpHeaders.PREFERENCE_APPLIED, profile.getPreferHeaderValue());
			// generate “ETag”;
			headers.add(HttpHeaders.ETAG, "" + modifiedStr);

			ResponseEntity<String> response = new ResponseEntity<String>(
					serializedUserSetJsonLdStr, headers, httpStatus);

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
	public ResponseEntity<String> insertItemIntoUserSet(
			@PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_DATASET_ID) String datasetId,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_LOCAL_ID) String localId,
			@RequestParam(value = WebUserSetFields.PATH_PARAM_POSITION, required=false) String position,
			@RequestParam(value = WebUserSetFields.PROFILE, required = false, defaultValue = WebUserSetFields.PROFILE_MINIMAL) String profile,			
			HttpServletRequest request
			) throws HttpException {
		
		String action = "put:/set/{identifier}/{dataset_id}/{local_id}?position=POSITION";
		return insertItemIntoUserSet(request, identifier, datasetId, localId, position, 
				profile, action);
	}
	
	/**
	 * This method validates input values, retrieves user set object and
	 * inserts item within user set to given position or at the end if no valid position provided.
	 * 
	 * @param request
	 * @param identifier The identifier of a user set
	 * @param datasetId The identifier of the dataset, typically a number
	 * @param localId The local identifier within the provider
	 * @param position The position in the existin item list
	 * @param profile The profile definition
	 * @param action The action describing the request
	 * @return response entity that comprises response body, headers and status code
	 * @throws HttpException
	 */
	protected ResponseEntity<String> insertItemIntoUserSet(HttpServletRequest request,  
			String identifier, String datasetId, String localId, String position,  
			String profileStr, String action) throws HttpException {

		try {
			// check user credentials, if invalid respond with HTTP 401,
			//  or if unauthorized respond with HTTP 403
		    // verify access rights
			verifyWriteAccess(Operations.UPDATE, request);

			LdProfiles profile = getProfile(profileStr, request);
			
			// check if the Set exists, if not respond with HTTP 404
			// retrieve an existing user set based on its identifier
			UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

			// check timestamp if provided within the “If-Match” HTTP header, if false respond with HTTP 412
		    checkIfMatchHeader(existingUserSet.getModified().hashCode(), request);

			// check if the Set is disabled, respond with HTTP 410
			HttpStatus httpStatus = null;
			String serializedUserSetJsonLdStr = "";
			
			if (existingUserSet.isDisabled()) { 
				httpStatus = HttpStatus.GONE;
			} else {			
				UserSet extUserSet = getUserSetService().insertItem(
						datasetId, localId, position, existingUserSet);
				serializedUserSetJsonLdStr = serializeUserSet(profile, extUserSet); 
		        httpStatus = HttpStatus.OK;
			}
			
			// build response entity with headers
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
			headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_PPGHD);
			headers.add(HttpHeaders.VARY, UserSetHttpHeaders.PREFER);
			headers.add(UserSetHttpHeaders.PREFERENCE_APPLIED, profile.getPreferHeaderValue());
			
			ResponseEntity<String> response = new ResponseEntity<String>(
					serializedUserSetJsonLdStr, headers, httpStatus);

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

	@RequestMapping(value = {"/set/{identifier}/{datasetId}/{localId}"}, method = {RequestMethod.GET, RequestMethod.HEAD}, 
			produces = { HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8})
	@ApiOperation(notes = SwaggerConstants.CHECK_ITEM_NOTE, value = "Check if item is member of the Set", nickname = "check item", response = java.lang.Void.class)
	public ResponseEntity<String> isItemInUserSet(
			@PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_DATASET_ID) String datasetId,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_LOCAL_ID) String localId,
			HttpServletRequest request
			) throws HttpException {
		
		String action = "get:/set/{identifier}/{dataset_id}/{local_id}";
		return isItemInUserSet(request, identifier, datasetId, localId, action);
	}
	
	/**
	 * This method validates input values and checks if item is already in a user set.
	 * 
	 * @param request
	 * @param identifier The identifier of a user set
	 * @param datasetId The identifier of the dataset, typically a number
	 * @param localId The local identifier within the provider
	 * @param userSet The user set fields to update in JSON format e.g. title or description
	 * @param action The action describing the request
	 * @return response entity that comprises response body, headers and status code
	 * @throws HttpException
	 */
	protected ResponseEntity<String> isItemInUserSet(HttpServletRequest request,  
			String identifier, String datasetId, String localId,  
			String action) throws HttpException {

		try {
			// check user credentials, if invalid respond with HTTP 401,
			//  or if unauthorized respond with HTTP 403
		    // verify access rights
			verifyReadAccess(request);

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
	public ResponseEntity<String> deleteItemFromUserSet(
			@PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_DATASET_ID) String datasetId,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_LOCAL_ID) String localId,
			@RequestParam(value = WebUserSetFields.PROFILE, required = false, defaultValue = WebUserSetFields.PROFILE_MINIMAL) String profile,			
			HttpServletRequest request
			) throws HttpException {
		
		String action = "delete:/set/{identifier}/{dataset_id}/{local_id}";
		return deleteItemFromUserSet(request, identifier, datasetId, localId, profile, action);
	}
	
	/**
	 * This method validates input values and deletes item from a user set.
	 * 
	 * @param request
	 * @param identifier The identifier of a user set
	 * @param datasetId The identifier of the dataset, typically a number
	 * @param localId The local identifier within the provider
	 * @param userSet The user set fields to update in JSON format e.g. title or description
	 * @param profile The profile definition
	 * @param action The action describing the request
	 * @return response entity that comprises response body, headers and status code
	 * @throws HttpException
	 */
	protected ResponseEntity<String> deleteItemFromUserSet(HttpServletRequest request, 
			String identifier, String datasetId, String localId, 
			String profileStr, String action) throws HttpException {
		
		try {
			// check user credentials, if invalid respond with HTTP 401,
			//  or if unauthorized respond with HTTP 403
		    // verify access rights
		    verifyWriteAccess(Operations.DELETE, request);

			LdProfiles profile = getProfile(profileStr, request);

			// check if the Set exists, if not respond with HTTP 404
			// retrieve an existing user set based on its identifier
			UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

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
			headers.add(UserSetHttpHeaders.PREFERENCE_APPLIED, profile.getPreferHeaderValue());
			
			ResponseEntity<String> response = new ResponseEntity<String>(
					serializedUserSetJsonLdStr, headers, httpStatus);

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

	@RequestMapping(value = {"/set/{identifier}"}, method = RequestMethod.DELETE)
	@ApiOperation(value = "Delete an existing user set", nickname = "delete", response = java.lang.Void.class)
	public ResponseEntity<String> deleteUserSet(
			@PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
			HttpServletRequest request
			) throws HttpException {

		return deleteUserSet(request, identifier);
	}
	
	/**
	 * This method implements removal of a user set 
	 * @param request
	 * @param identifier
	 * @throws HttpException
	 */
	protected ResponseEntity<String> deleteUserSet(HttpServletRequest request, String identifier)
			throws HttpException {

		try {
			// check user credentials, if invalid respond with HTTP 401,
		    // verify access rights
		    verifyWriteAccess(Operations.DELETE, request);

			// retrieve a user set based on its identifier
			// if the Set doesn’t exist, respond with HTTP 404
			UserSet existingUserSet = getUserSetService().getUserSetById(identifier, false);

			// check timestamp if provided within the "If-Match" HTTP header, if false respond with HTTP 412
		    checkIfMatchHeader(existingUserSet.getModified().hashCode(), request);

			// if the user set is disabled and the user is not an admin, respond with HTTP 410
			HttpStatus httpStatus = null;

		    if (existingUserSet.isDisabled()) {
		    	getUserSetService().deleteUserSet(existingUserSet.getIdentifier());
		    	httpStatus = HttpStatus.NO_CONTENT;
		    } else {
		    	httpStatus = HttpStatus.NO_CONTENT;
		    	getUserSetService().disableUserSet(existingUserSet);
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