package eu.europeana.set.web.service.controller.jsonld;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

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
import eu.europeana.set.definitions.exception.UserSetInstantiationException;
import eu.europeana.set.definitions.exception.UserSetValidationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.UserSetId;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.impl.BaseUserSetId;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.utils.serialize.UserSetLdSerializer;
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
			produces = { HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8})
	@ApiOperation(notes = SwaggerConstants.SAMPLES_JSONLD, value = "Create user set", nickname = "createUserSet", response = java.lang.Void.class)
	public ResponseEntity<String> createUserSet(
			@RequestParam(value = WebUserSetFields.PARAM_WSKEY) String wskey,
			@RequestBody String userSet,
			@RequestParam(value = WebUserSetFields.USER_TOKEN, required = false, defaultValue = WebUserSetFields.USER_ANONYMOUNS) String userToken,			
			HttpServletRequest request)
					throws HttpException {

		userToken = getUserToken(userToken, request);
		
		return storeUserSet(wskey, userSet, userToken, request);
	}
	
	/**
	 * This method requests parsing of a user set in JsonLd format to a UserSet object
	 * @param wsKey The API key
	 * @param userSetJsonLdStr The user set in JsonLd format
	 * @param userToken The user identifier
	 * @param request HTTP request
	 * @return response entity that comprises response body, headers and status code
	 * @throws HttpException
	 */
	protected ResponseEntity<String> storeUserSet(String wsKey, String userSetJsonLdStr, String userToken,
			HttpServletRequest request) throws HttpException {
		try {
			// validate user - check user credentials (all registered users can create) 
			// if invalid respond with HTTP 401 or if unauthorized respond with HTTP 403;
			validateApiKey(wsKey, WebUserSetFields.WRITE_METHOD);

			// authorize user
			UserSetId setId = new BaseUserSetId();
			getAuthorizationService().authorizeUser(userToken, wsKey, setId, Operations.CREATE);			
			
			// parse user set 
			UserSet webUserSet = getUserSetService().parseUserSetLd(userSetJsonLdStr);

			// validate and process the Set description for format and mandatory fields
			// if false respond with HTTP 400
			getUserSetService().validateWebUserSet(webUserSet);

			Agent user = new WebSoftwareAgent();
			user.setName("test agent");			
			
			// SET DEFAULTS
			if (webUserSet.getCreator() == null)
				webUserSet.setCreator(user);
			
			// store the new Set with its respective id, together with all the containing items 
			// following the order given by the list
			// generate an identifier (in sequence) for the Set
			// generate and add a created and modified timestamp to the Set
			UserSet storedUserSet = getUserSetService().storeUserSet(webUserSet);

			// apply linked data profile from header
			LdProfiles profile = getProfile(request);
			UserSet resUserSet = applyProfile(storedUserSet, profile);
			
			// serialize Set description in JSON-LD and respond with HTTP 201 if successful
			UserSetLdSerializer serializer = new UserSetLdSerializer(); 
	        String serializedUserSetJsonLdStr = serializer.serialize(resUserSet); 

			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
			headers.add(HttpHeaders.VARY, HttpHeaders.PREFER);
			headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
			headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_PG);
			// generate “ETag”;
			headers.add(HttpHeaders.ETAG, "" + storedUserSet.getModified().hashCode());

			ResponseEntity<String> response = new ResponseEntity<String>(
					serializedUserSetJsonLdStr, headers, HttpStatus.CREATED);

			return response;

		} catch (JsonParseException e) {
			throw new RequestBodyValidationException(
					I18nConstants.USERSET_CANT_PARSE_BODY, I18nConstants.USERSET_CANT_PARSE_BODY, e);
		} catch (ParamValidationException e) {
			throw new ParamValidationException(e.getMessage(), e.getMessage(), 
					null);
		} catch (UserSetValidationException e) { 
			throw new RequestBodyValidationException(
					I18nConstants.USERSET_CANT_PARSE_BODY, I18nConstants.USERSET_CANT_PARSE_BODY, e);
		} catch (UserSetAttributeInstantiationException e) {
			throw new RequestBodyValidationException(
					I18nConstants.USERSET_CANT_PARSE_BODY, I18nConstants.USERSET_CANT_PARSE_BODY, e);
		} catch (UserSetInstantiationException e) {
			throw new HttpException(null, I18nConstants.USERSET_INVALID_BODY, null, HttpStatus.BAD_REQUEST, e); 
		} catch (HttpException e) {
			// avoid wrapping HttpExceptions
			throw e;
		} catch (Exception e) {
			throw new InternalServerException(e);
		}

	}
	
	@RequestMapping(value = { "/set/{identifier}.jsonld" }, 
			method = {RequestMethod.GET},
			produces = {  HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8 }
			)
	@ApiOperation(notes = SwaggerConstants.SEARCH_HELP_NOTE, value = "Retrieve a user set", 
				nickname = "retrieve", response = java.lang.Void.class)
	public ResponseEntity<String> getUserSet(
			@RequestParam(value = WebUserSetFields.PARAM_WSKEY) String wskey,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
			@RequestParam(value = WebUserSetFields.USER_TOKEN, required = false, defaultValue = WebUserSetFields.USER_ANONYMOUNS) String userToken,			
			HttpServletRequest request) throws HttpException {

		String action = "get:/set/{identifier}.jsonld";

		return getUserSet(wskey, identifier, request, action);
	}

	/**
	 * This method retrieves an existing user set identified by given identifier, which is
	 * a number in string format.
	 * @param wskey The API key
	 * @param identifier The identifier
	 * @param request HTTP request
	 * @param action The action describing the request
	 * @return response entity that comprises response body, headers and status code
	 * @throws HttpException
	 */
	private ResponseEntity<String> getUserSet(String wsKey, String identifier, HttpServletRequest request, String action)
					throws HttpException {
		try {
			// check user credentials, if invalid respond with HTTP 401.
			// check client access (a valid "wskey" must be provided)
			validateApiKey(wsKey, WebUserSetFields.READ_METHOD);
			
			// retrieve a Set based on its identifier - process query
			// if the Set doesn’t exist, respond with HTTP 404
			// if the Set is disabled respond with HTTP 410
			UserSet userSet = getUserSetService().getUserSetById(identifier);
		
			// apply linked data profile from header
			LdProfiles profile = getProfile(request);
			UserSet resUserSet = applyProfile(userSet, profile);
						
			// serialize Set in JSON-LD according to the “Prefer” HTTP header 
			// (when present otherwise apply default) and respond with HTTP 200
			UserSetLdSerializer serializer = new UserSetLdSerializer(); 
	        String userSetJsonLdStr = serializer.serialize(resUserSet); 

			// build response
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
			headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
			headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_GPPD);
			// generate “ETag”;
			headers.add(HttpHeaders.ETAG, "" + userSet.getModified().hashCode());

			ResponseEntity<String> response = new ResponseEntity<String>(userSetJsonLdStr, headers, HttpStatus.OK);

			return response;

		} catch (RuntimeException e) {
			// not found ..
			throw new InternalServerException(e);
		} catch (UserSetNotFoundException e) {
			throw new UserSetNotFoundException(
					e.getMessage(), e.getMessage(), null);
		} catch (HttpException e) {
			// avoid wrapping http exception
			throw e;
		} catch (Exception e) {
			throw new InternalServerException(e);
		}
	}
	
	@RequestMapping(value = {"/set/{identifier}.jsonld"}, method = RequestMethod.PUT, 
			produces = { HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8})
	@ApiOperation(notes = SwaggerConstants.UPDATE_SAMPLES_JSONLD, value = "Update an existing user set", nickname = "update", response = java.lang.Void.class)
	public ResponseEntity<String> updateUserSet(@RequestParam(value = WebUserSetFields.PARAM_WSKEY) String wskey,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
			@RequestBody String userSet,
			@RequestParam(value = WebUserSetFields.USER_TOKEN, required = false, defaultValue = WebUserSetFields.USER_ANONYMOUNS) String userToken,
			HttpServletRequest request
			) throws HttpException {
		
		userToken = getUserToken(userToken, request);
		
		String action = "put:/set/{identifier}.jsonld";
		return updateUserSet(request, wskey, identifier, userSet, userToken, action);
	}
		
	/**
	 * This method validates input values, retrieves user set object and
	 * updates it.
	 * 
	 * @param request
	 * @param wskey The API key
	 * @param identifier The identifier
	 * @param userSet The user set fields to update in JSON format e.g. title or description
	 * @param action The action describing the request
	 * @return response entity that comprises response body, headers and status code
	 * @throws HttpException
	 */
	protected ResponseEntity<String> updateUserSet(HttpServletRequest request, String wsKey, String identifier,
			String userSetJsonLdStr, String userToken, String action) throws HttpException {

		try {
			// check user credentials, if invalid respond with HTTP 401,
			// check client access (a valid "wskey" must be provided)
			validateApiKey(wsKey, WebUserSetFields.READ_METHOD);

			// authorize user
			UserSetId setId = new BaseUserSetId();
			setId.setSequenceNumber(identifier);
			//  or if unauthorized respond with HTTP 403
			// TODO: EA-1148 implement exception handling, return 403 not 500
			getAuthorizationService().authorizeUser(userToken, wsKey, setId, Operations.UPDATE);

			// check if the Set exists, if not respond with HTTP 404
			// retrieve an existing user set based on its identifier
			UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

			// check timestamp if provided within the “If-Match” HTTP header, if false respond with HTTP 412
			checkHeaderTimestamp(request, existingUserSet.getModified().hashCode());

			// check if the Set is disabled, respond with HTTP 410
			HttpStatus httpStatus = null;
			int modifiedStr = 0;
			String serializedUserSetJsonLdStr = "";
			if (existingUserSet.isDisabled()) { 
				httpStatus = HttpStatus.GONE;
			} else {			
				// validate and process the Set description for format and mandatory fields
				// if false respond with HTTP 400
				getUserSetService().validateWebUserSet(existingUserSet);
				
				// parse fields of the new user set to an object
				UserSet newUserSet = getUserSetService().parseUserSetLd(userSetJsonLdStr);
	
				// update the Set based on its identifier (replace member items with the new items 
				// that are present in the Set description only when a profile is indicated and is 
				// different from "ldp:PreferMinimalContainer" is referred in the “Prefer” header);
				if (!checkHeaderProfile(request)) {
					throw new ApplicationAuthenticationException(
							I18nConstants.INVALID_UPDATE_HEADER_PROFILE, I18nConstants.INVALID_UPDATE_HEADER_PROFILE,
							new String[] {""}, HttpStatus.PRECONDITION_FAILED, null);
				}

				existingUserSet = getUserSetService().updateUserSetPagination(existingUserSet);
				
				// Respond with HTTP 200
	            // update an existing user set. merge user sets - insert new fields in existing object
				// generate and add a created and modified timestamp to the Set;
				existingUserSet.setModified(newUserSet.getModified());
				//TODO: EA-1148 the merge aspects for the usersets need to be clarified in the specifications document before closing this ticket 
				UserSet updatedUserSet = getUserSetService().updateUserSet(
						(PersistentUserSet) existingUserSet, newUserSet);
				modifiedStr = updatedUserSet.getModified().hashCode();
				
				//EA-1148 the move the serialization to own method, possibly in the base class
				// apply linked data profile from header
				LdProfiles profile = getProfile(request);
				UserSet resUserSet = applyProfile(updatedUserSet, profile);
								
				// serialize to JsonLd
				UserSetLdSerializer serializer = new UserSetLdSerializer(); 
		        serializedUserSetJsonLdStr = serializer.serialize(resUserSet); 
		        httpStatus = HttpStatus.OK;
			}
			
			// build response entity with headers
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
			headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
			headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_GPPD);
			// generate “ETag”;
			headers.add(HttpHeaders.ETAG, "" + modifiedStr);

			ResponseEntity<String> response = new ResponseEntity<String>(
					serializedUserSetJsonLdStr, headers, httpStatus);

			return response;
			
		} catch (JsonParseException e) {
			throw new RequestBodyValidationException(userSetJsonLdStr, I18nConstants.USERSET_CANT_PARSE_BODY, e);
		} catch (UserSetValidationException e) { 
			throw new RequestBodyValidationException(userSetJsonLdStr, I18nConstants.USERSET_CANT_PARSE_BODY, e);
		} catch (HttpException e) {
			//TODO: change this when OAUTH is implemented and the user information is available in service
			throw e;
		} catch (UserSetInstantiationException e) {
			throw new HttpException("The submitted user set content is invalid!", I18nConstants.USERSET_VALIDATION, null, HttpStatus.BAD_REQUEST, e);
		} catch (Exception e) {
			throw new InternalServerException(e);
		}
	}

	@RequestMapping(value = {"/set/{identifier}/{datasetId}/{localId}.jsonld"}, method = RequestMethod.PUT, 
			produces = { HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8})
	@ApiOperation(notes = SwaggerConstants.INSERT_ITEM_NOTE, value = "Insert item to an existing user set", nickname = "insert item", response = java.lang.Void.class)
	public ResponseEntity<String> insertItemIntoUserSet(@RequestParam(value = WebUserSetFields.PARAM_WSKEY) String wskey,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_DATASET_ID) String datasetId,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_LOCAL_ID) String localId,
			@RequestParam(value = WebUserSetFields.PATH_PARAM_POSITION, required=false) String position,
			@RequestParam(value = WebUserSetFields.USER_TOKEN, required = false, defaultValue = WebUserSetFields.USER_ANONYMOUNS) String userToken,
			HttpServletRequest request
			) throws HttpException {
		
		userToken = getUserToken(userToken, request);
		
		String action = "put:/set/{identifier}/{dataset_id}/{local_id}.jsonld?position=POSITION";
		return insertItemIntoUserSet(request, wskey, identifier, datasetId, localId, position, userToken, action);
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
	 * @param action The action describing the request
	 * @return response entity that comprises response body, headers and status code
	 * @throws HttpException
	 */
	protected ResponseEntity<String> insertItemIntoUserSet(HttpServletRequest request, String wsKey, 
			String identifier, String datasetId, String localId, String position, String userToken, 
			String action) throws HttpException {

		try {
			// check user credentials, if invalid respond with HTTP 401,
			//  or if unauthorized respond with HTTP 403
			// check client access (a valid "wskey" must be provided)
			validateApiKey(wsKey, WebUserSetFields.READ_METHOD);

			// authorize user
			UserSetId setId = new BaseUserSetId();
			setId.setSequenceNumber(identifier);
			getAuthorizationService().authorizeUser(userToken, wsKey, setId, Operations.UPDATE);

			// check if the Set exists, if not respond with HTTP 404
			// retrieve an existing user set based on its identifier
			//TODO: EA-1194 respond with 404 needs to be implemented...
			UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

			// check timestamp if provided within the “If-Match” HTTP header, if false respond with HTTP 412
			checkHeaderTimestamp(request, existingUserSet.getModified().hashCode());

			// check if the Set is disabled, respond with HTTP 410
			HttpStatus httpStatus = null;
			
			UserSetLdSerializer serializer = new UserSetLdSerializer();
			String serializedUserSetJsonLdStr = "";
			
			//TODO: EA-1194 refactor implementation to  reduce the dept of if else conditions. Extract methods to improve code readability.  
			if (existingUserSet.isDisabled()) { 
				httpStatus = HttpStatus.GONE;
			} else {			
				// validate position 
				int positionInt = validatePosition(position, existingUserSet.getItems());

				// build new item URL
				//TODO: EA-1194 refactor see EA1217 
				StringBuilder urlBuilder = new StringBuilder();
				urlBuilder.append(WebUserSetFields.BASE_ITEM_URL)
					.append(datasetId).append(WebUserSetFields.SLASH)
					.append(localId);
				String newItem = urlBuilder.toString();

				// check if item already exists in the Set, if so remove it
				//TODO: EA-1194 merge decoupled business logic. merge this code with the next if(!noAction) block
				boolean noAction = false;
				if (existingUserSet.getItems().contains(newItem)) {
					int currentPos = existingUserSet.getItems().indexOf(newItem);
					if (currentPos == positionInt) {
						noAction = true;
					} else {
						existingUserSet.getItems().remove(newItem);
					}
				}

				// insert item to Set in the indicated position (or last position if no position was indicated).
				UserSet extUserSet = null;
				if (!noAction) {
					if (positionInt == -1) {
						existingUserSet.getItems().add(newItem);
					} else {
						existingUserSet.getItems().add(positionInt, newItem);					
					}
					
					existingUserSet = getUserSetService().updateUserSetPagination(existingUserSet);
				
					// validate and process the Set description for format and mandatory fields
					// if false respond with HTTP 400
					//TODO: EA-1194 this method doesn't changes the userset metadata. Validation not needed
					getUserSetService().validateWebUserSet(existingUserSet);
					
					// generate and add a created and modified timestamp to the Set
					existingUserSet.setModified(new Date());
					
					// Respond with HTTP 200
		            // update an existing user set. merge user sets - insert new fields in existing object
					UserSet updatedUserSet = getUserSetService().updateUserSet(
							(PersistentUserSet) existingUserSet, null);
				
					// serialize to JsonLd
					extUserSet = serializer.fillPagination(updatedUserSet);
				} else {
					// serialize to JsonLd
					extUserSet = serializer.fillPagination(existingUserSet);					
				}
				
				// apply linked data profile from header
				LdProfiles profile = getProfile(request);
				UserSet resUserSet = applyProfile(extUserSet, profile);
								
		        serializedUserSetJsonLdStr = serializer.serialize(resUserSet); 
		        httpStatus = HttpStatus.OK;
			}
			
			// build response entity with headers
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
			headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_PPGHD);

			ResponseEntity<String> response = new ResponseEntity<String>(
					serializedUserSetJsonLdStr, headers, httpStatus);

			return response;

		} catch (UserSetValidationException e) { 
			//TODO: EA-1194 use message key as first param as well
			throw new RequestBodyValidationException("", I18nConstants.USERSET_CANT_PARSE_BODY, e);
		} catch (HttpException e) {
			//TODO: change this when OAUTH is implemented and the user information is available in service
			throw e;
		} catch (UserSetInstantiationException e) {
			throw new HttpException("The submitted user set content is invalid!", I18nConstants.USERSET_VALIDATION, null, HttpStatus.BAD_REQUEST, e);
		} catch (Exception e) {
			throw new InternalServerException(e);
		}
	}

	@RequestMapping(value = {"/set/{identifier}/{datasetId}/{localId}.jsonld"}, method = RequestMethod.GET, 
			produces = { HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8})
	@ApiOperation(notes = SwaggerConstants.CHECK_ITEM_NOTE, value = "Check if item is member of the Set", nickname = "check item", response = java.lang.Void.class)
	public ResponseEntity<String> isItemInUserSet(@RequestParam(value = WebUserSetFields.PARAM_WSKEY) String wskey,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_DATASET_ID) String datasetId,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_LOCAL_ID) String localId,
			@RequestParam(value = WebUserSetFields.USER_TOKEN, required = false, defaultValue = WebUserSetFields.USER_ANONYMOUNS) String userToken,
			HttpServletRequest request
			) throws HttpException {
		
		userToken = getUserToken(userToken, request);
		
		String action = "get:/set/{identifier}/{dataset_id}/{local_id}.jsonld";
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
			validateApiKey(wsKey, WebUserSetFields.READ_METHOD);

			// authorize user
			UserSetId setId = new BaseUserSetId();
			setId.setSequenceNumber(identifier);
			getAuthorizationService().authorizeUser(userToken, wsKey, setId, Operations.RETRIEVE);

			// check if the Set exists, if not respond with HTTP 404
			// retrieve an existing user set based on its identifier
			UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

			// check if the Set is disabled, respond with HTTP 410
			HttpStatus httpStatus = null;
			
			//TODO: EA1217 remove empty local variable it is use only once
			String serializedUserSetJsonLdStr = "";
			if (existingUserSet.isDisabled()) { 
				httpStatus = HttpStatus.GONE;
			} else {			
				// build new item URL
				//TODO: EA1217 refactor to own method 
				StringBuilder urlBuilder = new StringBuilder();
				urlBuilder.append(WebUserSetFields.BASE_ITEM_URL)
					.append(datasetId).append(WebUserSetFields.SLASH)
					.append(localId);
				String newItem = urlBuilder.toString();

				// check if item already exists in the Set, if so respond with 
				// HTTP 200, otherwise respond with HTTP 404.
				// check if item already exists in the Set, if so remove it
				if (existingUserSet.getItems().contains(newItem)) {
			        httpStatus = HttpStatus.OK;
				} else {
			        httpStatus = HttpStatus.NOT_FOUND;
				}
			}
			
			// build response entity with headers
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
			headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_PPGHD);

			ResponseEntity<String> response = new ResponseEntity<String>(
					serializedUserSetJsonLdStr, headers, httpStatus);

			return response;

		} catch (UserSetValidationException e) {
			//TODO: EA1217 use I18nConstants.USERSET_CANT_PARSE_BODY as first param as well, the message key will be displayed if the internationalization doesn't work
			throw new RequestBodyValidationException("", I18nConstants.USERSET_CANT_PARSE_BODY, e);
		} catch (HttpException e) {
			//TODO: change this when OAUTH is implemented and the user information is available in service
			throw e;
		} catch (UserSetInstantiationException e) {
			throw new HttpException("The submitted user set content is invalid!", I18nConstants.USERSET_VALIDATION, null, HttpStatus.BAD_REQUEST, e);
		} catch (Exception e) {
			throw new InternalServerException(e);
		}
	}

	@RequestMapping(value = {"/set/{identifier}/{datasetId}/{localId}.jsonld"}, method = RequestMethod.DELETE, 
			produces = { HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8})
	@ApiOperation(notes = SwaggerConstants.DELETE_ITEM_NOTE, value = "Delete a item from the set", nickname = "delete item", response = java.lang.Void.class)
	public ResponseEntity<String> deleteItemFromUserSet(@RequestParam(value = WebUserSetFields.PARAM_WSKEY) String wskey,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_DATASET_ID) String datasetId,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_LOCAL_ID) String localId,
			@RequestParam(value = WebUserSetFields.USER_TOKEN, required = false, defaultValue = WebUserSetFields.USER_ANONYMOUNS) String userToken,
			HttpServletRequest request
			) throws HttpException {
		
		userToken = getUserToken(userToken, request);
		
		String action = "delete:/set/{identifier}/{dataset_id}/{local_id}.jsonld";
		return deleteItemFromUserSet(request, wskey, identifier, datasetId, localId, userToken, action);
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
	 * @param action The action describing the request
	 * @return response entity that comprises response body, headers and status code
	 * @throws HttpException
	 */
	protected ResponseEntity<String> deleteItemFromUserSet(HttpServletRequest request, String wsKey, 
			String identifier, String datasetId, String localId, String userToken, 
			String action) throws HttpException {
		
		try {
			// check user credentials, if invalid respond with HTTP 401,
			//  or if unauthorized respond with HTTP 403
			// check client access (a valid "wskey" must be provided)
			validateApiKey(wsKey, WebUserSetFields.DELETE_METHOD);

			// authorize user
			UserSetId setId = new BaseUserSetId();
			setId.setSequenceNumber(identifier);
			getAuthorizationService().authorizeUser(userToken, wsKey, setId, Operations.RETRIEVE);

			// check if the Set exists, if not respond with HTTP 404
			// retrieve an existing user set based on its identifier
			UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

			// check if the Set is disabled, respond with HTTP 410
			HttpStatus httpStatus = null;
			String serializedUserSetJsonLdStr = "";
			if (existingUserSet.isDisabled()) { 
				httpStatus = HttpStatus.GONE;
			} else {			
				// build new item URL
				StringBuilder urlBuilder = new StringBuilder();
				urlBuilder.append(WebUserSetFields.BASE_ITEM_URL)
					.append(datasetId).append(WebUserSetFields.SLASH)
					.append(localId);
				String newItem = urlBuilder.toString();

				// check if item already exists in the Set, if not respond with HTTP 404
				if (existingUserSet.getItems().contains(newItem)) {
					// if already exists - remove item and update modified date
					existingUserSet.getItems().remove(newItem);
					Date now = new Date();				
					existingUserSet.setModified(now);
					
		            // update an existing user set
					UserSet updatedUserSet = getUserSetService().updateUserSet(
							(PersistentUserSet) existingUserSet, null);
				
					// serialize to JsonLd
					UserSetLdSerializer serializer = new UserSetLdSerializer(); 
					UserSet extUserSet = serializer.fillPagination(updatedUserSet);
				
					// apply linked data profile from header
					LdProfiles profile = getProfile(request);
					UserSet resUserSet = applyProfile(extUserSet, profile);
									
			        serializedUserSetJsonLdStr = serializer.serialize(resUserSet); 

			        // respond with HTTP 200 containing the updated Set description as body.
					// serialize Set in JSON-LD following the requested profile 
			        // (if not indicated assume the default, ie. minimal) 
			        httpStatus = HttpStatus.OK;
				} else {
			        httpStatus = HttpStatus.NOT_FOUND;
				}
			}
			
			// build response entity with headers
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
			headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_PPGHD);

			ResponseEntity<String> response = new ResponseEntity<String>(
					serializedUserSetJsonLdStr, headers, httpStatus);

			return response;

		} catch (UserSetValidationException e) { 
			throw new RequestBodyValidationException(I18nConstants.USERSET_CANT_PARSE_BODY, 
					I18nConstants.USERSET_CANT_PARSE_BODY, e);
		} catch (HttpException e) {
			//TODO: change this when OAUTH is implemented and the user information is available in service
			throw e;
		} catch (UserSetInstantiationException e) {
			throw new HttpException("The submitted user set content is invalid!", I18nConstants.USERSET_VALIDATION, null, HttpStatus.BAD_REQUEST, e);
		} catch (Exception e) {
			throw new InternalServerException(e);
		}
	}

	@RequestMapping(value = "/set/{identifier}.jsonld", method = RequestMethod.DELETE, produces = {
			HttpHeaders.CONTENT_TYPE_JSON_UTF8, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8 })
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
			validateApiKey(wsKey, WebUserSetFields.DELETE_METHOD);

			// authorize user
			UserSetId setId = new BaseUserSetId();
			setId.setSequenceNumber(identifier);
			//  or if unauthorized respond with HTTP 403
			//TODO: EA-1147 this exception handling is not implemented, must return 403 not 500
			getAuthorizationService().authorizeUser(userToken, wsKey, setId, Operations.DELETE);

			// retrieve a user set based on its identifier
			// if the Set doesn’t exist, respond with HTTP 404
			UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

			// check timestamp if provided within the "If-Match" HTTP header, if false respond with HTTP 412
			checkHeaderTimestamp(request, existingUserSet.getModified().hashCode());
						
			// if the user set is disabled and the user is not an admin, respond with HTTP 410
			HttpStatus httpStatus = null;
			if (existingUserSet.isDisabled() && !isAdmin(wsKey, userToken)) { 
				httpStatus = HttpStatus.GONE;
			} else {			
				// if the user is an Administrator then permanently remove item 
				// (and all items that are members of the user set)
				 if (isAdmin(wsKey, userToken)) {
					 getUserSetService().deleteUserSet(existingUserSet.getIdentifier());
				 } else { // otherwise flag it as disabled
					 //TODO: EA-1147 move this code to UserSetService.disableUserSet() and do not read again the user 					 
					 existingUserSet.setDisabled(true);
					 getUserSetService().updateUserSet(
							(PersistentUserSet) getUserSetService().getUserSetById(identifier)
							, existingUserSet);
				 }
				 httpStatus = HttpStatus.NO_CONTENT;
			}			
			// build response entity with headers
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
			headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
			headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_GPPD);

			ResponseEntity<String> response = new ResponseEntity<String>(
					identifier, headers, httpStatus);

			return response;			
		} catch (UserSetNotFoundException e) {
			throw new UserSetNotFoundException(
					e.getMessage(), e.getMessage(), null);
		} catch (HttpException e) {
			//TODO: change this when OAUTH is implemented and the user information is available in service
			throw e;
		} catch (Exception e) {
			throw new InternalServerException(e);
		}
	}

	/**
	 * This method validates whether user has admin rights to execute methods in
	 * management API.
	 * 
	 * @param apiKey
	 * @param userToken
	 * @return true if user has necessary permissions
	 */
	private boolean isAdmin(String apiKey, String userToken) {
		return (apiKey.equals("apiadmin") && userToken.equals("admin"));
	}
	
}