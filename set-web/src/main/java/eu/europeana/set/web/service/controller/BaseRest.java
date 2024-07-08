package eu.europeana.set.web.service.controller;

import static eu.europeana.api.commons.web.http.HttpHeaders.ALLOW;
import static eu.europeana.api.commons.web.http.HttpHeaders.ALLOW_DELETE;
import static eu.europeana.api.commons.web.http.HttpHeaders.ALLOW_POST;
import static eu.europeana.api.commons.web.http.HttpHeaders.LINK;
import static eu.europeana.api.commons.web.http.HttpHeaders.PREFER;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.ETAG;
import static javax.ws.rs.core.HttpHeaders.VARY;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import eu.europeana.api.commons.definitions.config.i18n.I18nConstants;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.exception.AuthorizationExtractionException;
import eu.europeana.api.commons.web.controller.BaseRestController;
import eu.europeana.api.commons.web.definitions.WebFields;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.exception.ParamValidationException;
import eu.europeana.api.commons.web.model.vocabulary.Operations;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.exception.UserSetProfileValidationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.stats.service.UsageStatsService;
import eu.europeana.set.web.config.UserSetI18nConstants;
import eu.europeana.set.web.http.UserSetHttpHeaders;
import eu.europeana.set.web.model.search.CollectionPage;
import eu.europeana.set.web.search.UserSetLdSerializer;
import eu.europeana.set.web.service.UserSetService;
import eu.europeana.set.web.service.authorization.UserSetAuthorizationService;
import eu.europeana.set.web.service.authorization.UserSetAuthorizationServiceImpl;
import eu.europeana.set.web.service.authorization.UserSetAuthorizationUtils;

public class BaseRest extends BaseRestController {

    @Resource
    UserSetConfiguration configuration;

    @Resource
    private UserSetService userSetService;

    @Resource
    UserSetAuthorizationService authorizationService;

    @Resource
    UsageStatsService usageStatsService;
    
    @Resource
    protected BuildProperties buildInfo;
    
    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
    }

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

    public UserSetAuthorizationService getAuthorizationService() {
        return authorizationService;
    }

    protected UsageStatsService getUsageStatsService() {
        return usageStatsService;
    }


    public void setAuthorizationService(UserSetAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    public String toResourceId(String collection, String object) {
        return "/" + collection + "/" + object;
    }

    /**
     * This method takes profile string and validates the profiles
     * and return the List of LdProfiles
     * NOTE : Multiple profiles are only supported in profile param
     *        string only for serach
     *
     * @param profileStr
     * @param request
     * @return
     * @throws HttpException
     */
    public List<LdProfiles> getProfiles(String profileStr, HttpServletRequest request) throws HttpException {
        List<LdProfiles> ldProfiles = new ArrayList<>();
        // if multiple profiles present seperated by comma
        if (profileStr.contains(WebUserSetFields.COMMA)) {
            for(String profile : Arrays.asList(StringUtils.split(profileStr, WebUserSetFields.COMMA))) {
                ldProfiles.add(getProfileFromParam(profile));
            }
            validateMultipleProfile(ldProfiles, profileStr);
        } else {
            ldProfiles.add(getProfile(profileStr, request));
        }
        return ldProfiles;
    }

    // TODO - This should be refactored once other profiles are deprecated
    /**
     * Method validates the multiple profile combinations
     * @param ldProfiles
     * @return
     * @throws HttpException
     */
    private void validateMultipleProfile(List<LdProfiles> ldProfiles, String profileStr) throws HttpException {
        // remove profile 'debug' as it's only used for stackTrace purpose
        if (ldProfiles.contains(LdProfiles.DEBUG)) {
            ldProfiles.remove(LdProfiles.DEBUG);
        }
        // For now maximum two profile-combinations are possible
        // profile=facets OR profile=facets,minimal OR profile=standard,facets OR profile=itemDescription,facets
        if(ldProfiles.size() > 2) {
            throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
                    new String[]{"Either of these should be provided ", StringUtils.remove(profileStr, LdProfiles.FACETS.getHeaderValue() + ",")});
        }
        // For now - if multiple profile then one of them has to be facets
        if (ldProfiles.size() == 2 && !ldProfiles.contains(LdProfiles.FACETS)) {
            throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
                    new String[]{"These profiles are not supported together ", profileStr });
        }
    }

    /**
     * This method takes profile from a HTTP header if it exists or from the passed
     * request parameter.
     *
     * @param paramProfile The HTTP request parameter
     * @param request      The HTTP request with headers
     * @return profile value
     * @throws HttpException
     * @throws UserSetProfileValidationException
     */
    // TODO: consider moving to api-commons
    public LdProfiles getProfile(String paramProfile, HttpServletRequest request) throws HttpException {
        LdProfiles profile = null;
        profile = getHeaderProfile(request);
        if (profile == null) {
            // get profile from param
            profile = getProfileFromParam(paramProfile);
        }
        return profile;
    }

    private LdProfiles getProfileFromParam(String paramProfile) throws HttpException {
        try {
            return LdProfiles.getByName(paramProfile);
        } catch (UserSetProfileValidationException e) {
            throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
                    new String[]{CommonApiConstants.QUERY_PARAM_PROFILE, paramProfile}, e);
        }
    }

    /**
     * This method identifies profile from a HTTP header if it exists.
     *
     * @param request The HTTP request with headers
     * @return profile value
     * @throws HttpException
     * @throws UserSetProfileValidationException
     */
    // TODO: consider moving to api-commons
    public LdProfiles getHeaderProfile(HttpServletRequest request) throws HttpException {

        LdProfiles profile = null;
        String preferHeader = request.getHeader(PREFER);
        if (preferHeader != null) {
            // identify profile by prefer header
            // retrieve profile if provided within the "If-Match" HTTP
            String ldPreferHeaderStr = null;
            String include = "include";
            if (StringUtils.isNotEmpty(preferHeader)) {
                // log header for debuging
                getLogger().debug("'Prefer' header value: {} ", preferHeader);
                try {
                    Map<String, String> preferHeaderMap = parsePreferHeader(preferHeader);
                    ldPreferHeaderStr = preferHeaderMap.get(include).replace("\"", "");
                    profile = LdProfiles.getByHeaderValue(ldPreferHeaderStr.trim());
                } catch (UserSetProfileValidationException e) {
                    throw new HttpException(UserSetI18nConstants.INVALID_HEADER_VALUE, UserSetI18nConstants.INVALID_HEADER_VALUE,
                            new String[]{PREFER, preferHeader}, HttpStatus.BAD_REQUEST, e);
                } catch (RuntimeException e) {
                    throw new HttpException(UserSetI18nConstants.INVALID_HEADER_FORMAT, UserSetI18nConstants.INVALID_HEADER_FORMAT,
                            new String[]{PREFER, preferHeader}, HttpStatus.BAD_REQUEST, e);
                }
            }
            getLogger().debug("Profile identified by prefer header: {} ", profile);

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
	//prepare data for serialization according to the profile
	getUserSetService().applyProfile(storedUserSet, profile);

	UserSetLdSerializer serializer = new UserSetLdSerializer();
	return serializer.serialize(storedUserSet);
    }
    
    protected String serializeCollectionPage(CollectionPage itemPage) throws IOException {
	//prepare data for serialization according to the profile
	
	UserSetLdSerializer serializer = new UserSetLdSerializer();
	return serializer.serialize(itemPage);
    }
    
    
    protected String serializeResultPage(LdProfiles profile, UserSet storedUserSet) throws IOException {
	//prepare data for serialization according to the profile
	getUserSetService().applyProfile(storedUserSet, profile);

	UserSetLdSerializer serializer = new UserSetLdSerializer();
	return serializer.serialize(storedUserSet);
    }

    /**
     * This method parses prefer header in keys and values
     *
     * @param preferHeader
     * @return map of prefer header keys and values
     */
    public Map<String, String> parsePreferHeader(String preferHeader) {
        // TODO: consider moving to api-commons
        String[] headerParts = null;
        String[] contentParts = null;
        int keyPos = 0;
        int valuePos = 1;

        Map<String, String> resMap = new HashMap<>(3);

        headerParts = preferHeader.split(";");
        for (String headerPart : headerParts) {
            contentParts = headerPart.split("=");
            if(contentParts.length == 2) {
                resMap.put(contentParts[keyPos], contentParts[valuePos]);
            }
        }
        return resMap;
    }

    public String getApiVersion() {
        return buildInfo.getVersion();
    }
    
    protected ResponseEntity<String> buildGetResponse(UserSet userSet, LdProfiles profile, Integer pageNr, Integer pageSize, HttpServletRequest request) throws IOException, HttpException {
	String jsonBody = "";
	if(pageNr==null && pageSize==null) {
	    jsonBody = serializeUserSet(profile, userSet);    
	}else {
	    CollectionPage itemPage = getUserSetService().buildCollectionPage(userSet, profile, pageNr, pageSize, request);
	    jsonBody = serializeCollectionPage(itemPage);
	}
	
	
	String etag = generateETag(userSet.getModified(), WebFields.FORMAT_JSONLD, getApiVersion());

	// build response
	MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
	headers.add(LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
	headers.add(LINK, UserSetHttpHeaders.VALUE_BASIC_RESOURCE);
	headers.add(ALLOW, UserSetHttpHeaders.ALLOW_GPD);
	headers.add(VARY, PREFER);
	headers.add(UserSetHttpHeaders.PREFERENCE_APPLIED, profile.getPreferHeaderValue());
	// generate “ETag”;
	headers.add(ETAG, etag);

	return new ResponseEntity<>(jsonBody, headers, HttpStatus.OK);
    }

    @Override
    public Authentication verifyWriteAccess(String operation, HttpServletRequest request)
        throws ApplicationAuthenticationException {

      // prevent write operations when the application is locked
      getAuthorizationService().checkWriteLockInEffect(operation);

      Authentication auth = null;
      //verify if auth is enabled
      if (getConfiguration().isAuthEnabled()) {
        auth = super.verifyWriteAccess(operation, request);
      } else {
        auth = authorizeByPlainTextToken(operation,request);
      }
      
      return auth;
    }

    private Authentication authorizeByPlainTextToken(String operation, HttpServletRequest request) throws ApplicationAuthenticationException {
      
      Authentication auth = null; 
      try {
        auth = UserSetAuthorizationUtils.createAuthentication(request.getHeader(AUTHORIZATION));
        auth = ((UserSetAuthorizationServiceImpl) getAuthorizationService()).checkPermissions(auth, operation);
      } catch (AuthorizationExtractionException e) {
        throw new ApplicationAuthenticationException("Authentication error: " + e.getMessage(), I18nConstants.OPERATION_NOT_AUTHORIZED, new String[] {operation}, HttpStatus.UNAUTHORIZED, e);
      }
      return auth;
    }

    @Override
    public Authentication verifyReadAccess(HttpServletRequest request) throws ApplicationAuthenticationException {
      final boolean hasToken = request.getHeader(AUTHORIZATION) != null;
      //verify if auth is enabled
      if(getConfiguration().isAuthEnabled() || !hasToken) {
        //regular authorization procedure
        return super.verifyReadAccess(request);
      } else {
          //authorize by plain text token
          return authorizeByPlainTextToken(Operations.RETRIEVE, request); 
      }
    }
    
    protected ResponseEntity<String> buildResponse(String jsonStr, HttpStatus httpStatus) {
      MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
      headers.add(VARY, ACCEPT);
      headers.add(ETAG, Integer.toString(hashCode()));
      headers.add(ALLOW, ALLOW_POST + "," + ALLOW_DELETE);

      return new ResponseEntity<>(jsonStr, headers, httpStatus);
    }
    
    
}