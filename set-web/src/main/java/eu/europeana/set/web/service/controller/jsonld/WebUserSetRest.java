package eu.europeana.set.web.service.controller.jsonld;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
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

//import eu.europeana.annotation.definitions.model.search.Query;
//import eu.europeana.annotation.definitions.model.search.SearchProfiles;
//import eu.europeana.annotation.definitions.model.search.result.AnnotationPage;
//import eu.europeana.annotation.definitions.model.vocabulary.WebAnnotationFields;
//import eu.europeana.annotation.solr.vocabulary.search.SortFields;
//import eu.europeana.annotation.solr.vocabulary.search.SortOrder;
//import eu.europeana.annotation.utils.serialize.AnnotationPageSerializer;
//import eu.europeana.annotation.web.exception.InternalServerException;
//import eu.europeana.annotation.web.exception.request.ParamValidationException;
//import eu.europeana.annotation.web.http.AnnotationHttpHeaders;
import eu.europeana.api.common.config.I18nConstants;
import eu.europeana.api.common.config.swagger.SwaggerSelect;
//import eu.europeana.api.common.config.swagger.SwaggerSelect;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.exception.InternalServerException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.set.definitions.exception.UserSetAttributeInstantiationException;
import eu.europeana.set.definitions.exception.UserSetInstantiationException;
import eu.europeana.set.definitions.exception.UserSetValidationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.utils.serialize.UserSetLdSerializer;
import eu.europeana.set.web.exception.request.RequestBodyValidationException;
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
@Api(tags = "Web User Set API", description = " ")
public class WebUserSetRest extends BaseRest {
	
	private static Logger logger = Logger.getRootLogger();

	@RequestMapping(value = "/set/", method = RequestMethod.POST, 
			produces = { HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8})
	@ApiOperation(notes = SwaggerConstants.SAMPLES_JSONLD, value = "Create user set", nickname = "createUserSet", response = java.lang.Void.class)
	public ResponseEntity<String> createUserSet(
			@RequestParam(value = WebUserSetFields.PARAM_WSKEY) String wskey,
			@RequestBody String userSet,
			@RequestParam(value = WebUserSetFields.USER_TOKEN, required = false) String userToken,
			HttpServletRequest request)
					throws HttpException {

		userToken = getUserToken(userToken, request);
		
		return storeUserSet(wskey, userSet, userToken);
	}
	
	/**
	 * This method requests parsing of a user set in JsonLd format to a UserSet object
	 * @param wsKey
	 * @param userSetJsonLdStr The user set in JsonLd format
	 * @param userToken
	 * @return a UserSet object
	 * @throws HttpException
	 */
	protected ResponseEntity<String> storeUserSet(String wsKey, String userSetJsonLdStr, String userToken) throws HttpException {
		try {

			// SET DEFAULTS
//			Application app = getAuthenticationService().getByApiKey(wsKey);

			// 0. annotation id
//			AnnotationId annoId = buildAnnotationId(provider, identifier);

			// 1. authorize user
//			Agent user = getAuthorizationService().authorizeUser(userToken, wsKey, annoId, Operations.CREATE);

			// parse
			UserSet webUserSet = getUserSetService().parseUserSetLd(userSetJsonLdStr);

			// SET DEFAULTS
//			if (webUserSet.getCreator() == null)
//				webUserSet.setCreator(user);

			// 2. validate
			// check whether annotation with the given provider and identifier
			// already exist in the database
//			if (annoId.getIdentifier() != null && getAnnotationService().existsInDb(annoId))
//				throw new ParamValidationException(ParamValidationException.MESSAGE_ANNOTATION_ID_EXISTS,
//						I18nConstants.ANNOTATION_VALIDATION,
//						new String[]{"/provider/identifier", annoId.toRelativeUri()});
			// 2.1 validate annotation properties
//			getAnnotationService().validateWebAnnotation(webAnnotation);

			// 3-6 create ID and annotation + backend validation
//			webAnnotation.setAnnotationId(annoId);

			// validate api key ... and request limit only if the request is
			// correct (avoid useless DB requests)
			// Done in authorize user
			// validateApiKey(wsKey);

//			Annotation storedAnnotation = getAnnotationService().storeAnnotation(webAnnotation, indexOnCreate);

			//** serialize to JsonLd
			UserSetLdSerializer serializer = new UserSetLdSerializer(); 
	        String serializedUserSetJsonLdStr = serializer.serialize(webUserSet); 

			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
			headers.add(HttpHeaders.VARY, HttpHeaders.ACCEPT);
//			headers.add(HttpHeaders.ETAG, "" + storedAnnotation.getLastUpdate().hashCode());
			headers.add(HttpHeaders.LINK, HttpHeaders.VALUE_LDP_RESOURCE);
			headers.add(HttpHeaders.ALLOW, HttpHeaders.ALLOW_POST);

			ResponseEntity<String> response = new ResponseEntity<String>(
					serializedUserSetJsonLdStr, headers, HttpStatus.CREATED);

			return response;

		} catch (JsonParseException e) {
			throw new RequestBodyValidationException(
					I18nConstants.USERSET_CANT_PARSE_BODY, I18nConstants.USERSET_CANT_PARSE_BODY, e);
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
	
	@RequestMapping(value = { "/set/{identifier}.json" }, 
			method = {RequestMethod.GET},
			produces = {  HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8 }
			)
	@ApiOperation(notes = SwaggerConstants.SEARCH_HELP_NOTE, value = "Retrieve a user set", 
				nickname = "retrieve", response = java.lang.Void.class)
	public ResponseEntity<String> getUserSet(@RequestParam(value = WebUserSetFields.PARAM_WSKEY) String wskey,
			@PathVariable(value = WebUserSetFields.PATH_PARAM_SET_ID) String identifier,
			HttpServletRequest request) throws HttpException {

		String action = "get:/set/{identifier}.jsonld";

		return getUserSet(wskey, identifier, request, action);
	}

	private ResponseEntity<String> getUserSet(String wskey, String identifier, HttpServletRequest request, String action)
					throws HttpException {

		try {

			//** 2. Check client access (a valid “wskey” must be provided)
//			validateApiKey(wskey, WebUserSetFields.READ_METHOD);

			// process query
			UserSet userSet = getUserSetService().getUserSetById(identifier);
		
			//** serialize page
			UserSetLdSerializer serializer = new UserSetLdSerializer(); 
	        String userSetJsonLdStr = serializer.serialize(userSet); 

			//** build response
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
			headers.add(HttpHeaders.VARY, HttpHeaders.ACCEPT + ", "+ HttpHeaders.PREFER);
			headers.add(HttpHeaders.LINK, HttpHeaders.VALUE_LDP_RESOURCE);
			headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_CONSTRAINTS);
			headers.add(HttpHeaders.ALLOW, HttpHeaders.ALLOW_GET);
			headers.add(HttpHeaders.CONTENT_TYPE, UserSetHttpHeaders.VALUE_LDP_CONTENT_TYPE);

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
	
}