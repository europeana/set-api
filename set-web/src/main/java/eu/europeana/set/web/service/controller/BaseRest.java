package eu.europeana.set.web.service.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import eu.europeana.api.common.config.I18nConstants;
import eu.europeana.api.commons.web.controller.BaseRestController;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.exception.ParamValidationException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.exception.UserSetProfileValidationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.utils.serialize.UserSetLdSerializer;
import eu.europeana.set.web.model.vocabulary.Roles;
import eu.europeana.set.web.service.UserSetService;
import eu.europeana.set.web.service.authentication.AuthenticationService;
import eu.europeana.set.web.service.authorization.AuthorizationService;


public class BaseRest extends BaseRestController { 

	@Resource
	UserSetConfiguration configuration;

	@Resource
	private UserSetService userSetService;

	@Resource
	AuthenticationService authenticationService;

	@Resource
	AuthorizationService authorizationService;

	Logger logger = LogManager.getLogger(getClass());

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
	 * @throws UserSetProfileValidationException
	 */
	public LdProfiles getProfile(String paramProfile, HttpServletRequest request) throws HttpException {

		LdProfiles profile = null;
		String preferHeader = request.getHeader(HttpHeaders.PREFER);
		if (preferHeader != null) {
			// identify profile by prefer header
			profile = getProfile(preferHeader);
			getLogger().debug("Profile identified by prefer header: " + profile.name());
		} else {
			// get profile from param
			try {
				profile = LdProfiles.getByName(paramProfile);
			} catch (UserSetProfileValidationException e) {
				throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
						new String[] { WebUserSetFields.PROFILE, paramProfile });
			}
		}
		return profile;
	}

	/**
	 * This method serializes user set and applies profile to the object.
	 * 
	 * @param profile
	 * @param storedUserSet
	 * @return serialized user set as a JsonLd string
	 * @throws IOException
	 */
	protected String serializeUserSet(LdProfiles profile, UserSet storedUserSet) throws IOException {
		// apply linked data profile from header
		UserSet resUserSet = applyProfile(storedUserSet, profile);

		// serialize Set description in JSON-LD and respond with HTTP 201 if
		// successful
		UserSetLdSerializer serializer = new UserSetLdSerializer();
		String serializedUserSetJsonLdStr = serializer.serialize(resUserSet);
		return serializedUserSetJsonLdStr;
	}

    protected String buildCreatorUri(String userId) {
    	return WebUserSetFields.DEFAULT_CREATOR_URL + userId;
    }
    
    /**
     * This method validates input values wsKey, identifier and userToken.
     * 
     * @param identifier
     * @param userId
     * @return
     * @return userSet object
     * @throws HttpException
     */
    protected UserSet verifyOwnerOrAdmin(UserSet userSet, Authentication authentication) throws HttpException {

		String userId = buildCreatorUri((String)authentication.getPrincipal());
		
		//verify ownership
		boolean isOwner = userSet.getCreator().getHttpUrl().equals(userId);
		if(isOwner || hasAdminRights(authentication)) {
		    //approve owner or admin
		    return userSet;
		}else {
		    //not authorized
		    throw new ApplicationAuthenticationException(I18nConstants.OPERATION_NOT_AUTHORIZED,
			    I18nConstants.OPERATION_NOT_AUTHORIZED, new String[] { "Only the creators of the annotation or admins are authorized to perform this operation."});
		}
    }

    protected boolean hasAdminRights(Authentication authentication) {
    	
		for (Iterator<? extends GrantedAuthority> iterator = authentication.getAuthorities().iterator(); iterator.hasNext();) {
		    //role based authorization
		    String role = iterator.next().getAuthority();
		    if(Roles.ADMIN.getName().equals(role)){
			return true;
		    }
		}
		return false;
    }

	@Override
	public Authentication verifyWriteAccess(String operation, HttpServletRequest request)
	        throws ApplicationAuthenticationException {
	    
	    Authentication auth = super.verifyWriteAccess(operation, request);
	    return auth;
	}
	
	/**
	 * This method checks if user is an owner of the user set
	 * 
	 * @param userSet
	 * @param queryUser
	 * @return true if user is owner of a user set
	 */
	public boolean isOwner(UserSet userSet, String queryUser) {
		return userSet.getCreator().getName().equals(queryUser);
	}

	/**
	 * This method retrieves view profile if provided within the "If-Match" HTTP
	 * header
	 * 
	 * @param request
	 * @return profile value
	 * @throws HttpException
	 */
	LdProfiles getProfile(String preferHeader) throws HttpException {
		LdProfiles ldProfile = null;
		String ldPreferHeaderStr = null;
		String INCLUDE = "include";

		if (StringUtils.isNotEmpty(preferHeader)) {
			// log header for debuging
			getLogger().debug("'Prefer' header value: " + preferHeader);
				
			try {
				Map<String, String> preferHeaderMap = parsePreferHeader(preferHeader);
				ldPreferHeaderStr = preferHeaderMap.get(INCLUDE).replace("\"", "");
				ldProfile = LdProfiles.getByHeaderValue(ldPreferHeaderStr.trim());
			} catch (UserSetProfileValidationException e) {
				throw new HttpException(I18nConstants.INVALID_HEADER_VALUE, I18nConstants.INVALID_HEADER_VALUE,
						new String[] {HttpHeaders.PREFER,  preferHeader}, HttpStatus.BAD_REQUEST, null);
			} catch (Throwable th){
				throw new HttpException(I18nConstants.INVALID_HEADER_FORMAT, I18nConstants.INVALID_HEADER_FORMAT,
						new String[] {HttpHeaders.PREFER,  preferHeader}, HttpStatus.BAD_REQUEST, null);
			}
		}

		return ldProfile;
	}

	/**
	 * This method parses prefer header in keys and values
	 * 
	 * @param preferHeader
	 * @return map of prefer header keys and values
	 */
	public Map<String, String> parsePreferHeader(String preferHeader) {
		String[] headerParts = null;
		String[] contentParts = null;
		int KEY_POS = 0;
		int VALUE_POS = 1;

		Map<String, String> resMap = new HashMap<String, String>();

		headerParts = preferHeader.split(";");
		for (String headerPart : headerParts) {
			contentParts = headerPart.split("=");
			resMap.put(contentParts[KEY_POS], contentParts[VALUE_POS]);
		}
		return resMap;
	}

	/**
	 * This methods applies Linked Data profile to a user set
	 * 
	 * @param userSet
	 *            The given user set
	 * @param profile
	 *            Provided Linked Data profile
	 * @return profiled user set value
	 */
	public UserSet applyProfile(UserSet userSet, LdProfiles profile) {

		// check that not more then maximal allowed number of items are
		// presented
		if (profile != LdProfiles.MINIMAL && userSet.getItems() != null) {
			int itemsCount = userSet.getItems().size();
			if (itemsCount > WebUserSetFields.MAX_ITEMS_TO_PRESENT) {
				List<String> itemsPage = userSet.getItems().subList(0, WebUserSetFields.MAX_ITEMS_TO_PRESENT);
				userSet.setItems(itemsPage);
				profile = LdProfiles.MINIMAL;
				getLogger().debug("Profile switched to minimal, due to set size!");
			}
		}

		// set unnecessary fields to null - the empty fields will not be
		// presented
		switch (profile) {
		case STANDARD:
			// not for stadard profile
			break;
		case MINIMAL:
		default:
			userSet.setItems(null);
			break;
		}

		return userSet;
	}

	/**
	 * This method is used for validation of the provided api key
	 * 
	 * @param wsKey
	 * @throws EntityAuthenticationException
	 */
	protected void validateApiKey(String wsKey) throws ApplicationAuthenticationException {

		// throws exception if the wskey is not found
		if (StringUtils.isEmpty(wsKey)) {
			throw new ApplicationAuthenticationException(I18nConstants.EMPTY_APIKEY, null);
		}
		getAuthenticationService().getByApiKey(wsKey);
	}

    public String getApiVersion() {
    	return getAuthorizationService().getConfiguration().getApiVersion();
    }
}