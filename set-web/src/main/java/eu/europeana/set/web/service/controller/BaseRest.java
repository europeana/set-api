package eu.europeana.set.web.service.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import eu.europeana.set.stats.service.UsageStatsService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.europeana.api.common.config.UserSetI18nConstants;
import eu.europeana.api.commons.definitions.config.i18n.I18nConstants;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.web.controller.BaseRestController;
import eu.europeana.api.commons.web.definitions.WebFields;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.exception.ParamValidationException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.exception.UserSetProfileValidationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.web.http.UserSetHttpHeaders;
import eu.europeana.set.web.model.search.CollectionPage;
import eu.europeana.set.web.search.UserSetLdSerializer;
import eu.europeana.set.web.service.UserSetService;
import eu.europeana.set.web.service.authorization.UserSetAuthorizationService;

public class BaseRest extends BaseRestController {

    @Resource
    UserSetConfiguration configuration;

    @Resource
    private UserSetService userSetService;

    @Resource
    UserSetAuthorizationService authorizationService;

    @Resource
    UsageStatsService usageStatsService;

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
            try {
                profile = LdProfiles.getByName(paramProfile);
            } catch (UserSetProfileValidationException e) {
                throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
                        new String[]{CommonApiConstants.QUERY_PARAM_PROFILE, paramProfile}, e);
            }
        }
        return profile;
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
        String preferHeader = request.getHeader(HttpHeaders.PREFER);
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
                            new String[]{HttpHeaders.PREFER, preferHeader}, HttpStatus.BAD_REQUEST, e);
                } catch (RuntimeException e) {
                    throw new HttpException(UserSetI18nConstants.INVALID_HEADER_FORMAT, UserSetI18nConstants.INVALID_HEADER_FORMAT,
                            new String[]{HttpHeaders.PREFER, preferHeader}, HttpStatus.BAD_REQUEST, e);
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
	UserSet set = getUserSetService().applyProfile(storedUserSet, profile);

	UserSetLdSerializer serializer = new UserSetLdSerializer();
	return serializer.serialize(set);
    }
    
    protected String serializeCollectionPage(CollectionPage itemPage) throws IOException {
	//prepare data for serialization according to the profile
	
	UserSetLdSerializer serializer = new UserSetLdSerializer();
	return serializer.serialize(itemPage);
    }
    
    
    protected String serializeResultPage(LdProfiles profile, UserSet storedUserSet) throws IOException {
	//prepare data for serialization according to the profile
	UserSet set = getUserSetService().applyProfile(storedUserSet, profile);

	UserSetLdSerializer serializer = new UserSetLdSerializer();
	return serializer.serialize(set);
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
        return getConfiguration().getApiVersion();
    }
    
    protected ResponseEntity<String> buildGetResponse(UserSet userSet, LdProfiles profile, Integer pageNr, int pageSize, HttpServletRequest request) throws IOException, HttpException {
	String jsonBody = "";
	if(pageNr == null || pageNr < 0) {
	    jsonBody = serializeUserSet(profile, userSet);    
	}else {
	    CollectionPage itemPage = getUserSetService().buildCollectionPage(userSet, profile, pageNr, pageSize, request);
	    jsonBody = serializeCollectionPage(itemPage);
	}
	
	
	String etag = generateETag(userSet.getModified(), WebFields.FORMAT_JSONLD, getApiVersion());

	// build response
	MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
	headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
	headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_RESOURCE);
	headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_GPD);
	headers.add(UserSetHttpHeaders.VARY, HttpHeaders.PREFER);
	headers.add(UserSetHttpHeaders.PREFERENCE_APPLIED, profile.getPreferHeaderValue());
	// generate “ETag”;
	headers.add(UserSetHttpHeaders.ETAG, etag);

	return new ResponseEntity<>(jsonBody, headers, HttpStatus.OK);
    }

}