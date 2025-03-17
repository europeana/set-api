package eu.europeana.set.web.service.controller;

import static eu.europeana.api.commons.web.definitions.WebFields.FORMAT_JSONLD;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.api.commons.web.model.vocabulary.Operations;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.exception.UserSetProfileValidationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.vocabulary.SetPageProfile;
import eu.europeana.set.definitions.model.vocabulary.SetProfileHelper;
import eu.europeana.set.definitions.model.vocabulary.SetResourceProfile;
import eu.europeana.set.stats.service.UsageStatsService;
import eu.europeana.set.web.config.UserSetI18nConstants;
import eu.europeana.set.web.http.UserSetHttpHeaders;
import eu.europeana.set.web.model.search.CollectionPage;
import eu.europeana.set.web.search.UserSetLdSerializer;
import eu.europeana.set.web.service.RequestPathMethodService;
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

  @Autowired
  private RequestPathMethodService requestMethodService;

  SetProfileHelper profileHelper = new SetProfileHelper();

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

  protected List<SetPageProfile> getProfilesFromRequest(String profile, HttpServletRequest request)
      throws ParamValidationException {
    String preferHeader = request.getHeader(PREFER);
    if (preferHeader != null && getLogger().isDebugEnabled()) {
      getLogger().debug("'Prefer' header value: {} ", preferHeader);
    }

    // parse and validate profiles
    List<SetPageProfile> profiles = null;
    try {
      profiles = getProfileHelper().getSetPageProfiles(profile, preferHeader);
    } catch (UserSetProfileValidationException e) {
      if (StringUtils.isNotEmpty(preferHeader)) {
        throw new ParamValidationException(UserSetI18nConstants.INVALID_HEADER_VALUE,
            UserSetI18nConstants.INVALID_HEADER_VALUE, new String[] {PREFER, preferHeader}, e);
      } else {
        throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE,
            I18nConstants.INVALID_PARAM_VALUE,
            new String[] {CommonApiConstants.QUERY_PARAM_PROFILE, profile}, e);
      }
    }
    return profiles;
  }

  /**
   * Method validates the multiple profile combinations
   * 
   * @param ldProfiles
   * @return
   * @throws HttpException
   */
  protected void validateMultipleProfiles(List<SetPageProfile> profiles, String profileStr)
      throws HttpException {
    // remove profile 'debug' as it's only used for stackTrace purpose
    // For now maximum two profile-combinations are possible
    // profile=facets OR profile=facets,minimal OR profile=standard,facets OR
    // profile=itemDescription,facets
    if (profiles.size() > 2) {
      throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE,
          I18nConstants.INVALID_PARAM_VALUE, new String[] {"Only one of these should be provided ",
              StringUtils.remove(profileStr, SetPageProfile.FACETS.getProfileParamValue())});
    }
    // For now - if multiple profile then one of them has to be facets
    if (profiles.size() == 2 && !profiles.contains(SetPageProfile.FACETS)) {
      throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE,
          I18nConstants.INVALID_PARAM_VALUE,
          new String[] {"These profiles are not supported together ", profileStr});
    }
  }

  /**
   * This method serializes user set and applies profile to the object.
   *
   * @param profile
   * @param storedUserSet
   * @return serialized user set as a JsonLd string
   * @throws IOException
   */
  protected String serializeUserSet(SetPageProfile profile, UserSet storedUserSet)
      throws IOException {
    // prepare data for serialization according to the profile
    getUserSetService().applyProfile(storedUserSet, profile);

    UserSetLdSerializer serializer = new UserSetLdSerializer();
    return serializer.serialize(storedUserSet);
  }

  /**
   * This method serializes user set and applies profile to the object.
   * Used to serialize set update delete and insert operations
   *
   * @param profile
   * @param storedUserSet
   * @return serialized user set as a JsonLd string
   * @throws IOException
   */
  protected String serializeUserSet(SetResourceProfile profile, UserSet storedUserSet)
      throws IOException {
    // prepare data for serialization according to the profile
    getUserSetService().applyProfile(storedUserSet, profile);

    UserSetLdSerializer serializer = new UserSetLdSerializer();
    return serializer.serialize(storedUserSet);
  }

  protected String serializeCollectionPage(CollectionPage itemPage) throws IOException {
    // prepare data for serialization according to the profile
    UserSetLdSerializer serializer = new UserSetLdSerializer();
    return serializer.serialize(itemPage);
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
      if (contentParts.length == 2) {
        resMap.put(contentParts[keyPos], contentParts[valuePos]);
      }
    }
    return resMap;
  }

  public String getApiVersion() {
    return buildInfo.getVersion();
  }

  protected ResponseEntity<String> buildResponseEntity(UserSet storedUserSet,
      final SetResourceProfile profile, final HttpStatus responseStatus,
      Map<String, String> additionalHeaders, HttpServletRequest request) throws IOException {
    String serializedUserSetJsonLdStr = serializeUserSet(profile, storedUserSet);

    String etag = generateETag(storedUserSet.getModified(), FORMAT_JSONLD, getApiVersion());

    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
    headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
    headers.add(HttpHeaders.LINK, UserSetHttpHeaders.VALUE_BASIC_RESOURCE);
    headers.add(HttpHeaders.ALLOW, createAllowHeader(request));
    // headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_PG);
    if (additionalHeaders != null) {
      for (Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
        headers.add(entry.getKey(), entry.getValue());
      }
    }

    // generate “ETag”;
    headers.add(UserSetHttpHeaders.ETAG, etag);
    // headers.add(UserSetHttpHeaders.PREFERENCE_APPLIED,
    // LdProfiles.MINIMAL.getPreferHeaderValue());

    return new ResponseEntity<>(serializedUserSetJsonLdStr, headers, responseStatus);
  }

  protected ResponseEntity<String> buildSetPageResponse(CollectionPage setPage, Date modified,
      SetPageProfile profile,HttpServletRequest request)
      throws IOException, HttpException {
    String jsonBody = "";
    jsonBody = serializeCollectionPage(setPage);
    String etag = generateETag(modified, WebFields.FORMAT_JSONLD, getApiVersion());

    // build response
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(7);
    headers.add(LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
    headers.add(LINK, UserSetHttpHeaders.VALUE_BASIC_RESOURCE);
    // headers.add(ALLOW, UserSetHttpHeaders.ALLOW_GPD);
    headers.add(ALLOW, createAllowHeader(request));
    headers.add(VARY, PREFER);
    headers.add(UserSetHttpHeaders.PREFERENCE_APPLIED, profile.getPreferenceApplied());
    // generate “ETag”;
    headers.add(ETAG, etag);

    return new ResponseEntity<>(jsonBody, headers, HttpStatus.OK);
  }

  /**
   * Indicated the type of the response, if it is metadata only (minimal profile) or items page
   * (page is not null)
   * 
   * @param pageNr page number from request
   * @return true if pageNr is null
   */
  protected boolean isSetResourceRequestResponse(Object pageNr) {
    return pageNr == null;
  }

  @Override
  public Authentication verifyWriteAccess(String operation, HttpServletRequest request)
      throws ApplicationAuthenticationException {

    // prevent write operations when the application is locked
    getAuthorizationService().checkWriteLockInEffect(operation);

    Authentication auth = null;
    // verify if auth is enabled
    if (getConfiguration().isAuthEnabled()) {
      auth = super.verifyWriteAccess(operation, request);
    } else {
      auth = authorizeByPlainTextToken(operation, request);
    }

    return auth;
  }

  private Authentication authorizeByPlainTextToken(String operation, HttpServletRequest request)
      throws ApplicationAuthenticationException {

    Authentication auth = null;
    try {
      auth = UserSetAuthorizationUtils.createAuthentication(request.getHeader(AUTHORIZATION));
      auth = ((UserSetAuthorizationServiceImpl) getAuthorizationService()).checkPermissions(auth,
          operation);
    } catch (AuthorizationExtractionException e) {
      throw new ApplicationAuthenticationException("Authentication error: " + e.getMessage(),
          I18nConstants.OPERATION_NOT_AUTHORIZED, new String[] {operation}, HttpStatus.UNAUTHORIZED,
          e);
    }
    return auth;
  }

  @Override
  public Authentication verifyReadAccess(HttpServletRequest request)
      throws ApplicationAuthenticationException {
    final boolean hasToken = request.getHeader(AUTHORIZATION) != null;
    // verify if auth is enabled
    if (getConfiguration().isAuthEnabled() || !hasToken) {
      // regular authorization procedure
      return super.verifyReadAccess(request);
    } else {
      // authorize by plain text token
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


  protected String createAllowHeader(HttpServletRequest request) {
    String allowHeaderValue;

    Optional<String> methodsForRequestPattern =
        requestMethodService.getMethodsForRequestPattern(request);
    if (methodsForRequestPattern.isEmpty()) {
      logger.warn(
          "Could not find other matching methods for {}. Using current request method in Allow header",
          request.getRequestURL());
      allowHeaderValue = request.getMethod();
    } else {
      allowHeaderValue = methodsForRequestPattern.get();
    }

    return allowHeaderValue;
  }

  protected SetProfileHelper getProfileHelper() {
    return profileHelper;
  }


}
