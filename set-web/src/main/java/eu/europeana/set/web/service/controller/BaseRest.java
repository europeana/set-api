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
import org.codehaus.jettison.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import eu.europeana.api.common.config.I18nConstants;
import eu.europeana.api.commons.definitions.search.Query;
import eu.europeana.api.commons.web.controller.BaseRestController;
import eu.europeana.api.commons.web.definitions.WebFields;
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
import eu.europeana.set.web.model.vocabulary.Operations;
import eu.europeana.set.web.model.vocabulary.Roles;
import eu.europeana.set.web.service.UserSetService;
import eu.europeana.set.web.service.authorization.AuthorizationService;

public class BaseRest extends BaseRestController {

	@Resource
	UserSetConfiguration configuration;

	@Resource
	private UserSetService userSetService;

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
	//TODO: consider moving to api-commons
	public LdProfiles getProfile(String paramProfile, HttpServletRequest request) throws HttpException {

		LdProfiles profile = null;		
		profile = getHeaderProfile(request);
		if (profile == null) {
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
	 * This method identifies profile from a HTTP header if it exists.
	 * 
	 * @param paramProfile
	 *            The HTTP request parameter
	 * @param request
	 *            The HTTP request with headers
	 * @return profile value
	 * @throws HttpException
	 * @throws UserSetProfileValidationException
	 */
	//TODO: consider moving to api-commons
	public LdProfiles getHeaderProfile(HttpServletRequest request) throws HttpException {

		LdProfiles profile = null;
		String preferHeader = request.getHeader(HttpHeaders.PREFER);
		if (preferHeader != null) {
			// identify profile by prefer header
			// retrieve profile if provided within the "If-Match" HTTP
			String ldPreferHeaderStr = null;
			String INCLUDE = "include";
			if (StringUtils.isNotEmpty(preferHeader)) {
				// log header for debuging
				getLogger().debug("'Prefer' header value: " + preferHeader);					
				try {
					Map<String, String> preferHeaderMap = parsePreferHeader(preferHeader);
					ldPreferHeaderStr = preferHeaderMap.get(INCLUDE).replace("\"", "");
					profile = LdProfiles.getByHeaderValue(ldPreferHeaderStr.trim());
				} catch (UserSetProfileValidationException e) {
					throw new HttpException(I18nConstants.INVALID_HEADER_VALUE, I18nConstants.INVALID_HEADER_VALUE,
							new String[] {HttpHeaders.PREFER,  preferHeader}, HttpStatus.BAD_REQUEST, null);
				} catch (Throwable th){
					throw new HttpException(I18nConstants.INVALID_HEADER_FORMAT, I18nConstants.INVALID_HEADER_FORMAT,
							new String[] {HttpHeaders.PREFER,  preferHeader}, HttpStatus.BAD_REQUEST, null);
				}
			}			
			
			getLogger().debug("Profile identified by prefer header: " + profile.name());
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

	/**
	 * This method checks if user is an owner of the user set
	 * 
	 * @param userSet
	 * @param queryUser
	 * @return true if user is owner of a user set
	 */
	public boolean isOwner(UserSet userSet, Authentication authentication) {
		String userId = buildCreatorUri((String)authentication.getPrincipal());
		return userSet.getCreator().getName().equals(userId);
	}
	
	/**
	 * This method retrieves user id from authentication object
	 * @param authentication
	 * @return the user id
	 */
	public String getUserId(Authentication authentication) {
		return buildCreatorUri((String)authentication.getPrincipal());
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
		boolean isOwner = userSet.getCreator().getName().equals(userId);

		if(isOwner || hasAdminRights(authentication)) {
		    //approve owner or admin
		    return userSet;
		}else {
		    //not authorized
		    throw new ApplicationAuthenticationException(I18nConstants.OPERATION_NOT_AUTHORIZED,
			    I18nConstants.OPERATION_NOT_AUTHORIZED, new String[] { "Only the creators of the annotation or admins are authorized to perform this operation."});
		}
    }

    protected String buildCreatorUri(String userId) {
    	return WebFields.DEFAULT_CREATOR_URL + userId;
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
    
	/**
	 * This method parses prefer header in keys and values
	 * 
	 * @param preferHeader
	 * @return map of prefer header keys and values
	 */
	public Map<String, String> parsePreferHeader(String preferHeader) {
	    //TODO: consider moving to api-commons
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
			if(userSet.getIsDefinedBy() == null)
				userSet.setItems(null);
			break;
		}

		return userSet;
	}

	public String getApiVersion() {
	    return getConfiguration().getApiVersion();
	}
	
    /**
     * This method performs query to Europeana API using URI defined in isDefinedBy parameter.
     * @param userSet
     * @return user set updated with items from Europeana API
     * @throws HttpException
     * @throws IOException
     * @throws JSONException
     */
    public UserSet fetchItemsPage(UserSet userSet, String sort, String sortOrder, int pageNr, int pageSize)
	    throws HttpException, IOException, JSONException {
	if (userSet.isOpenSet()) {
	    String apiKey = getConfiguration().getSearchApiKey();
	    
	    userSet = getUserSetService().fetchDynamicSetItems(userSet, apiKey, sort, sortOrder,
		    pageNr, pageSize);
	}
	return userSet;
    }
       
}