package eu.europeana.set.web.service.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.europeana.api.common.config.I18nConstants;
import eu.europeana.api.commons.config.i18n.I18nService;
import eu.europeana.api.commons.web.controller.ApiResponseBuilder;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.api.commons.web.model.ApiResponse;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.web.http.UserSetHttpHeaders;
import eu.europeana.set.web.service.UserSetService;
import eu.europeana.set.web.service.authentication.AuthenticationService;
import eu.europeana.set.web.service.authorization.AuthorizationService;


public class BaseRest extends ApiResponseBuilder {

	@Resource
	UserSetConfiguration configuration;

	@Resource
	private UserSetService userSetService;

	@Resource
	AuthenticationService authenticationService;
	
	@Resource
	AuthorizationService authorizationService;
	
	@Resource
	I18nService i18nService;

	@Override
	protected I18nService getI18nService() {
		return i18nService;
	}

	@Override
	public ApiResponse buildErrorResponse(String errorMessage, String action, String apiKey) {
		// TODO Auto-generated method stub
		return null;
	}

	Logger logger = Logger.getLogger(getClass());

	public Logger getLogger() {
		return logger;
	}

	protected UserSetConfiguration getConfiguration() {
		return configuration;
	}

	protected UserSetService getUserSetService() {
		return userSetService;
	}

	public void setUserSetService(UserSetService userSetService) {
		this.userSetService = userSetService;
	}

	public void setConfiguration(UserSetConfiguration configuration) {
		this.configuration = configuration;
	}

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}
		
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public AuthorizationService getAuthorizationService() {
		return authorizationService;
	}

	public void setAuthorizationService(AuthorizationService authorizationService) {
		this.authorizationService = authorizationService;
	}
	
	public String toResourceId(String collection, String object) {
		return "/" + collection + "/" + object;
	}
	
	protected ResponseEntity <String> buildResponseEntityForJsonString(String jsonStr) {
		
		HttpStatus httpStatus = HttpStatus.OK;
		ResponseEntity<String> response = buildResponseEntityForJsonString(jsonStr, httpStatus);
		
		return response;		
	}
	
	protected ResponseEntity<String> buildResponseEntityForJsonString(String jsonStr, HttpStatus httpStatus) {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
		headers.add(HttpHeaders.VARY, HttpHeaders.ACCEPT);
		headers.add(HttpHeaders.ETAG, Integer.toString(hashCode()));
		headers.add(HttpHeaders.ALLOW, HttpHeaders.ALLOW_GET);

		ResponseEntity<String> response = new ResponseEntity<String>(jsonStr, headers, httpStatus);
		return response;
	}

	/**
	 * This method performs decoding of base64 string
	 * 
	 * @param base64Str
	 * @return decoded string
	 * @throws ApplicationAuthenticationException
	 */
	public String decodeBase64(String base64Str) throws ApplicationAuthenticationException {
		String res = null;
		try {
			byte[] decodedBase64Str = Base64.decodeBase64(base64Str);
			res = new String(decodedBase64Str);
		} catch (Exception e) {
			throw new ApplicationAuthenticationException(
					I18nConstants.BASE64_DECODING_FAIL, I18nConstants.BASE64_DECODING_FAIL, null);			
		}
		return res;
	}

	/**
	 * This method takes user token from a HTTP header if it exists or from the
	 * passed request parameter.
	 * 
	 * @param paramUserToken
	 *            The HTTP request parameter
	 * @param request
	 *            The HTTP request with headers
	 * @return user token
	 * @throws ApplicationAuthenticationException
	 */
	public String getUserToken(String paramUserToken, HttpServletRequest request)
			throws ApplicationAuthenticationException {
		int USER_TOKEN_TYPE_POS = 0;
		int BASE64_ENCODED_STRING_POS = 1;
		String userToken = null;
		String userTokenHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (userTokenHeader != null) {
			logger.trace("'Authorization' header value: " + userTokenHeader);
			String[] headerElems = userTokenHeader.split(" ");
			if(headerElems.length < 2 )
				throw new ApplicationAuthenticationException(
						I18nConstants.INVALID_HEADER_FORMAT, I18nConstants.INVALID_HEADER_FORMAT, null);

			String userTokenType = headerElems[USER_TOKEN_TYPE_POS];
			if (!UserSetHttpHeaders.BEARER.equals(userTokenType)) {
				throw new ApplicationAuthenticationException(
						I18nConstants.UNSUPPORTED_TOKEN_TYPE, I18nConstants.UNSUPPORTED_TOKEN_TYPE,
						new String[] {userTokenType});
			}

			String encodedUserToken = headerElems[BASE64_ENCODED_STRING_POS];
			
			userToken = decodeBase64(encodedUserToken);
			logger.debug("Decoded user token: " + userToken);

		} else {
			//@deprecated to be removed in the next versions
			//fallback to URL param 
			userToken = paramUserToken;
		}
		return userToken;
	}
	
	/**
	 * This method checks timestamp if provided within the "If-Match" HTTP header, if false responds with HTTP 412
	 * @param request
	 * @param modified
	 * @throws ApplicationAuthenticationException
	 */
	//TODO: EA-1194 change method to get the UserSet object as param
	public void checkHeaderTimestamp(HttpServletRequest request, int modified) 
			throws ApplicationAuthenticationException {
		String ifMatchHeader = request.getHeader(HttpHeaders.IF_MATCH);
		if (ifMatchHeader != null) {
			logger.trace("'If-Match' header value: " + ifMatchHeader);	
			String modifiedStr = String.valueOf(modified);
			if (!ifMatchHeader.equals(modifiedStr)) {
				throw new ApplicationAuthenticationException(
						I18nConstants.INVALID_IF_MATCH_TIMESTAMP, I18nConstants.INVALID_IF_MATCH_TIMESTAMP,
						new String[] {modifiedStr}, HttpStatus.PRECONDITION_FAILED, null);
			}
		}
	}
	
	/**
	 * This method retrieves view profile if provided within the "If-Match" HTTP header
	 * @param request
	 * @return profile value
	 * @throws ApplicationAuthenticationException
	 */
	public LdProfiles getProfile(HttpServletRequest request) {
		LdProfiles ldProfile = LdProfiles.MINIMAL;
		
		String preferHeader = request.getHeader(HttpHeaders.PREFER);
		if (preferHeader != null) {
			logger.trace("'Prefer' header value: " + preferHeader);	
			if (StringUtils.isNotEmpty(preferHeader))
				ldProfile = LdProfiles.getByHeaderValue(preferHeader);
		}
		return ldProfile;
	}
	
	/**
	 * This methods applies Linked Data profile to a user set
	 * @param userSet The given user set
	 * @param profile Provided Linked Data profile
	 * @return profiled user set value
	 */
	public UserSet applyProfile(UserSet userSet, LdProfiles profile) {
		//TODO: EA-1194 remove res, it is the same as the input param userSet
		UserSet res = null;
		
		// check that not more then maximal allowed number of items are presented
		if (profile != LdProfiles.MINIMAL && userSet.getItems() != null) {
			int itemsCount = userSet.getItems().size();
			if (itemsCount > WebUserSetFields.MAX_ITEMS_TO_PRESENT) {
				 List<String> itemsPage = userSet.getItems().subList(0, WebUserSetFields.MAX_ITEMS_TO_PRESENT);
				 userSet.setItems(itemsPage);
			}
		}

		// set unnecessary fields to null - the empty fields will not be presented
		switch(profile) {
		case STANDARD:
			res = userSet;
			break;
		case MINIMAL:
		default:
			userSet.setItems(null);
			res = userSet;
			break;
		}
		return res;
	}
	
	/**
	 * This method validates position input, if false responds with -1
	 * @param position The given position
	 * @param items The item list
	 * @return position The validated position in list to insert
	 * @throws ApplicationAuthenticationException
	 */
	public int validatePosition(String position, List<String> items) throws ApplicationAuthenticationException {
		int positionInt = -1;
		if (StringUtils.isNotEmpty(position)) {
			try {
				positionInt = Integer.parseInt(position);
				if (positionInt > items.size()) {
					positionInt = -1;
				}
			} catch (Exception e) {
				logger.trace("Position validation warning: " + e.getMessage());
			}
		}
		return positionInt;
	}
	
	/**
	 * This method checks profile if provided within the "Prefer" HTTP header, 
	 * and responds with true only when a profile is indicated and is different 
	 * from "ldp:PreferMinimalContainer"
	 * @param request
	 * @return
	 * @throws ApplicationAuthenticationException
	 */
	public boolean checkHeaderProfile(HttpServletRequest request) throws ApplicationAuthenticationException {
		boolean res = false;
		String preferHeader = request.getHeader(HttpHeaders.PREFER);
		if (preferHeader != null) {
			logger.trace("'Prefer' header value: " + preferHeader);	
			if (!preferHeader.equals(WebUserSetFields.PREFER_MINIMAL_CONTAINER_HEADER)) {
				res = true;
			}
		}
		return res;
	}
	
	//TODO: EA-1194 remove method param as it is not used
	protected void validateApiKey(String wsKey, String method) throws ApplicationAuthenticationException {
		
		// throws exception if the wskey is not found
		getAuthenticationService().getByApiKey(wsKey);
	}
	
}