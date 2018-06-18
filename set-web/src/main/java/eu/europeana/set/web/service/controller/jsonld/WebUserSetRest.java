package eu.europeana.set.web.service.controller.jsonld;

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
import eu.europeana.set.definitions.model.agent.impl.SoftwareAgent;
import eu.europeana.set.definitions.model.impl.BaseUserSetId;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.utils.serialize.UserSetLdSerializer;
import eu.europeana.set.web.exception.request.RequestBodyValidationException;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.http.SwaggerConstants;
import eu.europeana.set.web.http.UserSetHttpHeaders;
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
		
		return storeUserSet(wskey, userSet, userToken);
	}
	
	/**
	 * This method requests parsing of a user set in JsonLd format to a UserSet object
	 * @param wsKey The API key
	 * @param userSetJsonLdStr The user set in JsonLd format
	 * @param userToken The user identifier
	 * @return response entity that comprises response body, headers and status code
	 * @throws HttpException
	 */
	protected ResponseEntity<String> storeUserSet(String wsKey, String userSetJsonLdStr, String userToken) throws HttpException {
		try {
			// validate user - check user credentials (all registered users can create) 
			// if invalid respond with HTTP 401 or if unauthorized respond with HTTP 403;

			// authorize user
//			Agent user = getAuthorizationService().authorizeUser(userToken, wsKey, annoId, Operations.CREATE);
			
			// parse user set 
			UserSet webUserSet = getUserSetService().parseUserSetLd(userSetJsonLdStr);

			// validate and process the Set description for format and mandatory fields
			// if false respond with HTTP 400
			getUserSetService().validateWebUserSet(webUserSet);

			Agent user = new SoftwareAgent();
			user.setName("test agent");			
			
			// SET DEFAULTS
			if (webUserSet.getCreator() == null)
				webUserSet.setCreator(user);
			
			// store the new Set with its respective id, together with all the containing items 
			// following the order given by the list
			// generate an identifier (in sequence) for the Set
			// generate and add a created and modified timestamp to the Set
			UserSet storedUserSet = getUserSetService().storeUserSet(webUserSet);

			// serialize Set description in JSON-LD and respond with HTTP 201 if successful
			UserSetLdSerializer serializer = new UserSetLdSerializer(); 
	        String serializedUserSetJsonLdStr = serializer.serialize(storedUserSet); 

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
	private ResponseEntity<String> getUserSet(String wskey, String identifier, HttpServletRequest request, String action)
					throws HttpException {
		try {
			// check user credentials, if invalid respond with HTTP 401.
			// check client access (a valid “wskey” must be provided)
//			validateApiKey(wskey, WebUserSetFields.READ_METHOD);

			// retrieve a Set based on its identifier - process query
			// if the Set doesn’t exist, respond with HTTP 404
			// if the Set is disabled respond with HTTP 410
			UserSet userSet = getUserSetService().getUserSetById(identifier);
		
			// serialize Set in JSON-LD according to the “Prefer” HTTP header 
			// (when present otherwise apply default) and respond with HTTP 200
			UserSetLdSerializer serializer = new UserSetLdSerializer(); 
	        String userSetJsonLdStr = serializer.serialize(userSet); 

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
		return updateUserSet(wskey, identifier, userSet, userToken, action);
	}
		
	/**
	 * This method validates input values, retrieves user set object and
	 * updates it.
	 * 
	 * @param wskey The API key
	 * @param identifier The identifier
	 * @param userSet The user set fields to update in JSON format e.g. title or description
	 * @param action The action describing the request
	 * @return response entity that comprises response body, headers and status code
	 * @throws HttpException
	 */
	protected ResponseEntity<String> updateUserSet(String wsKey, String identifier,
			String userSetJsonLdStr, String userToken, String action) throws HttpException {

		try {
			// check user credentials, if invalid respond with HTTP 401,
			//  or if unauthorized respond with HTTP 403
			// check client access (a valid “wskey” must be provided)
//			validateApiKey(wskey, WebUserSetFields.READ_METHOD);

			// authorize user
//			getAuthorizationService().authorizeUser(userToken, wsKey, annoId, Operations.UPDATE);

			// check timestamp if provided within the “If-Match” HTTP header, if false respond with HTTP 412
			
			// check if the Set exists, if not respond with HTTP 404
			// retrieve an existing user set based on its identifier
			UserSet existingUserSet = getUserSetService().getUserSetById(identifier);

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
	
				// generate and add a created and modified timestamp to the Set;
				
				// update the Set based on its identifier (replace member items with the new items 
				// that are present in the Set description only when a profile is indicated and is 
				// different from “ldp:PreferMinimalContainer” is referred in the “Prefer” header);
				// Respond with HTTP 200
	            // update an existing user set. merge user sets - insert new fields in existing object
				UserSet updatedUserSet = getUserSetService().updateUserSet(
						(PersistentUserSet) existingUserSet, newUserSet);
				modifiedStr = updatedUserSet.getModified().hashCode();
				
				// serialize to JsonLd
				UserSetLdSerializer serializer = new UserSetLdSerializer(); 
		        serializedUserSetJsonLdStr = serializer.serialize(updatedUserSet); 
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
	 * @param identifier
	 * @param wsKey
	 * @param userToken
	 * @throws HttpException
	 */
	protected ResponseEntity<String> deleteUserSet(HttpServletRequest request, String identifier, String wsKey, String userToken)
			throws HttpException {

		try {
			// check user credentials, if invalid respond with HTTP 401,
			//  or if unauthorized respond with HTTP 403
			// check client access (a valid "wskey" must be provided)
			validateApiKey(wsKey, WebUserSetFields.DELETE_METHOD);

			// authorize user
			UserSetId setId = new BaseUserSetId();
			setId.setSequenceNumber(identifier);
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
					 existingUserSet.setDisabled(true);
					 getUserSetService().updateUserSet(
							(PersistentUserSet) getUserSetService().getUserSetById(identifier)
							, existingUserSet);
				 }
			}			
			// build response entity with headers
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
			headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
			headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_GPPD);

			if (httpStatus == null) {
				httpStatus = HttpStatus.NO_CONTENT;
			}
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