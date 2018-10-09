package eu.europeana.set.web.service.controller;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import eu.europeana.api.common.config.I18nConstants;
import eu.europeana.api.commons.config.i18n.I18nService;
import eu.europeana.api.commons.web.controller.ApiResponseBuilder;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.exception.UserSetHeaderValidationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.utils.serialize.UserSetLdSerializer;
import eu.europeana.set.web.exception.authorization.OperationAuthorizationException;
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
			getLogger().trace("'Authorization' header value: " + userTokenHeader);
			String[] headerElems = userTokenHeader.split(" ");
			if(headerElems.length < 2 )
				throw new ApplicationAuthenticationException(
						I18nConstants.INVALID_HEADER_FORMAT, I18nConstants.INVALID_HEADER_FORMAT, new String[]{userTokenHeader});

			String userTokenType = headerElems[USER_TOKEN_TYPE_POS];
			if (!UserSetHttpHeaders.BEARER.equals(userTokenType)) {
				throw new ApplicationAuthenticationException(
						I18nConstants.UNSUPPORTED_TOKEN_TYPE, I18nConstants.UNSUPPORTED_TOKEN_TYPE,
						new String[] {userTokenType});
			}

			String encodedUserToken = headerElems[BASE64_ENCODED_STRING_POS];
			
			userToken = decodeBase64(encodedUserToken);
			getLogger().debug("Decoded user token: " + userToken);

		} else {
			//@deprecated to be removed in the next versions
			//fallback to URL param 
			userToken = paramUserToken;
		}
		return userToken;
	}
	
	/**
	 * This method takes profile from a HTTP header if it exists or from the
	 * passed request parameter.
	 * 
	 * @param paramProfile
	 *            The HTTP request parameter
	 * @param request
	 *            The HTTP request with headers
	 * @return profile value
	 * @throws HttpException 
	 */
	public LdProfiles getProfile(String paramProfile, HttpServletRequest request)
			throws HttpException {

		LdProfiles profile = null;
		String preferHeader = request.getHeader(HttpHeaders.PREFER);
		if (preferHeader != null) {
			profile = getProfile(preferHeader);
		} else {
			profile = LdProfiles.getByName(paramProfile);
		}

		if (profile == null) {
			throw new ApplicationAuthenticationException(
					I18nConstants.USERSET_INVALID_PROFILE_VALUE, I18nConstants.USERSET_INVALID_PROFILE_VALUE,
					new String[] {paramProfile}, HttpStatus.PRECONDITION_FAILED, null);
		}
		return profile;
	}
	
	/**
	 * This method serializes user set and applies profile to the object.
	 * @param profile
	 * @param storedUserSet
	 * @return serialized user set as a JsonLd string
	 * @throws IOException
	 */
	protected String serializeUserSet(LdProfiles profile, UserSet storedUserSet) throws IOException {
		// apply linked data profile from header
		UserSet resUserSet = applyProfile(storedUserSet, profile);
		
		// serialize Set description in JSON-LD and respond with HTTP 201 if successful
		UserSetLdSerializer serializer = new UserSetLdSerializer(); 
		String serializedUserSetJsonLdStr = serializer.serialize(resUserSet);
		return serializedUserSetJsonLdStr;
	}
		
	/**
	 * This method checks timestamp if provided within the "If-Match" HTTP header, if false responds with HTTP 412
	 * @param request
	 * @param modified
	 * @throws ApplicationAuthenticationException
	 */
	public void checkHeaderTimestamp(HttpServletRequest request, UserSet userSet) 
			throws ApplicationAuthenticationException {
		int modified = userSet.getModified().hashCode();
		String ifMatchHeader = request.getHeader(HttpHeaders.IF_MATCH);
		if (ifMatchHeader != null) {
			getLogger().trace("'If-Match' header value: " + ifMatchHeader);	
			String modifiedStr = String.valueOf(modified);
			if (!ifMatchHeader.equals(modifiedStr)) {
				throw new ApplicationAuthenticationException(
						I18nConstants.INVALID_IF_MATCH_TIMESTAMP, I18nConstants.INVALID_IF_MATCH_TIMESTAMP,
						new String[] {modifiedStr}, HttpStatus.PRECONDITION_FAILED, null);
			}
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
	protected boolean isAdmin(String apiKey, String userToken) {
		return (apiKey.equals("apidemo") && userToken.equals("admin"));
	}
	 
	/**
	 * This method checks that only the admins and the owners of the user sets are allowed to delete the user set. 
	 * in the case of regular users (not admins), the autorization method must check if the users 
	 * that calls the deletion (i.e. identified by provided user token) is the same user as the creator 
	 * of the user set
	 * @param userSet
	 * @param wsKey
	 * @param queryUser
	 * @throws ApplicationAuthenticationException
	 */
	public void hasModifyRights(UserSet userSet, String wsKey, String queryUser) 
			throws OperationAuthorizationException {

		if (!(isAdmin(wsKey, queryUser) || userSet.getCreator().getName().equals(queryUser))) {
			throw new OperationAuthorizationException(I18nConstants.USER_NOT_AUTHORIZED, 
					I18nConstants.USER_NOT_AUTHORIZED, 
					new String[]{"User ID: "+ queryUser},
					HttpStatus.FORBIDDEN);
		}
	}
	
	/**
	 * This method checks if user is an owner of the user set
	 * @param userSet
	 * @param queryUser
	 * @return true if user is owner of a user set
	 */
	public boolean isOwner(UserSet userSet, String queryUser) {
		return userSet.getCreator().getName().equals(queryUser);
	}
	
	/**
	 * This method retrieves view profile if provided within the "If-Match" HTTP header
	 * @param request
	 * @return profile value
	 * @throws HttpException 
	 * @throws ApplicationAuthenticationException
	 */
	/**
	 * @param request
	 * @return
	 * @throws HttpException
	 */
	public LdProfiles getProfile(String preferHeader) throws HttpException {
		LdProfiles ldProfile = null;
		String[] headerParts = null;
		String[] includeParts = null;
		String includeStr = null;
		String ldPreferHeaderStr = null;
		
		if (preferHeader != null) {
			getLogger().trace("'Prefer' header value: " + preferHeader);	
			if (StringUtils.isNotEmpty(preferHeader)) {
				try {
					headerParts = preferHeader.split(";");
					includeStr = headerParts[headerParts.length -1];
					includeParts = includeStr.split("=");
					ldPreferHeaderStr = includeParts[includeParts.length -1].replace("\"", "");					
					ldProfile = LdProfiles.getByHeaderValue(ldPreferHeaderStr);
				} catch (UserSetHeaderValidationException e) {
					throw new HttpException(I18nConstants.INVALID_HEADER_FORMAT, 
							I18nConstants.INVALID_HEADER_FORMAT, new String[] {preferHeader}, 
							HttpStatus.BAD_REQUEST, null); 					
				}
				if (ldProfile == null) {
					throw new HttpException(I18nConstants.INVALID_HEADER_FORMAT, 
							I18nConstants.INVALID_HEADER_FORMAT, new String[] {preferHeader}, 
							HttpStatus.BAD_REQUEST, null); 					
				}
			}
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
		
		// check that not more then maximal allowed number of items are presented
		if (profile != LdProfiles.MINIMAL && userSet.getItems() != null) {
			int itemsCount = userSet.getItems().size();
			if (itemsCount > WebUserSetFields.MAX_ITEMS_TO_PRESENT) {
				 List<String> itemsPage = userSet.getItems().subList(0, WebUserSetFields.MAX_ITEMS_TO_PRESENT);
				 userSet.setItems(itemsPage);
				 profile = LdProfiles.MINIMAL;
			}
		}

		// set unnecessary fields to null - the empty fields will not be presented
		switch(profile) {
		case STANDARD:
			break;
		case MINIMAL:
		default:
			userSet.setItems(null);
			break;
		}
		
		return userSet;
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
				getLogger().trace("Position validation warning: " + e.getMessage());
			}
		}
		return positionInt;
	}
	
	/**
	 * This method checks profile and responds with true only when a profile is indicated and is different 
	 * from "ldp:PreferMinimalContainer" referred in the "Prefer" HTTP header
	 * @param profile
	 * @return
	 * @throws ApplicationAuthenticationException
	 */
	public boolean checkHeaderProfile(LdProfiles profile) throws ApplicationAuthenticationException {
		boolean res = false;
		if (!profile.getHeaderValue().equals(WebUserSetFields.PREFER_MINIMAL_CONTAINER_HEADER)) {
			res = true;
		}
		return res;
	}
	
	/**
	 * This method is used for validation of the provided api key
	 * @param wsKey
	 * @throws EntityAuthenticationException
	 */
	protected void validateApiKey(String wsKey) throws ApplicationAuthenticationException {
		
		// throws exception if the wskey is not found
		if (wsKey == null || StringUtils.isEmpty(wsKey)) {
			throw new ApplicationAuthenticationException(I18nConstants.EMPTY_APIKEY, null);
		}
		getAuthenticationService().getByApiKey(wsKey);
	}
		
}