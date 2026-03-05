package eu.europeana.set.web.service.controller;

import static eu.europeana.api.commons_sb3.definitions.http.HttpHeaders.ALLOW;
import static eu.europeana.api.commons_sb3.definitions.http.HttpHeaders.ALLOW_DELETE;
import static eu.europeana.api.commons_sb3.definitions.http.HttpHeaders.ALLOW_POST;
import static eu.europeana.api.commons_sb3.definitions.http.HttpHeaders.LINK;
import static eu.europeana.api.commons_sb3.definitions.http.HttpHeaders.PREFER;
import static eu.europeana.api.commons_sb3.definitions.http.HttpHeaders.PREFERENCE_APPLIED;
import static eu.europeana.set.definitions.model.vocabulary.WebUserSetFields.FORMAT_JSONLD;
import static jakarta.ws.rs.core.HttpHeaders.ACCEPT;
import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static jakarta.ws.rs.core.HttpHeaders.ETAG;
import static jakarta.ws.rs.core.HttpHeaders.LAST_MODIFIED;
import static jakarta.ws.rs.core.HttpHeaders.VARY;
import eu.europeana.api.commons_sb3.error.config.ErrorMessage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import eu.europeana.api.commons_sb3.definitions.oauth.Operations;
import eu.europeana.api.commons_sb3.definitions.oauth.exception.ApiWriteLockException;
import eu.europeana.api.commons_sb3.definitions.utils.DateUtils;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.commons_sb3.error.EuropeanaI18nApiException;
import eu.europeana.api.commons_sb3.error.config.ErrorConfig;
import eu.europeana.api.commons_sb3.error.exceptions.ApplicationAuthenticationException;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidParamException;
import eu.europeana.api.commons_sb3.exception.AuthorizationExtractionException;
import eu.europeana.api.commons_sb3.nosql.service.WriteLockAuthorizationService;
import eu.europeana.api.commons_sb3.oauth2.BaseRestController;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.exception.UserSetProfileValidationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.vocabulary.SetPageProfile;
import eu.europeana.set.definitions.model.vocabulary.SetProfileHelper;
import eu.europeana.set.definitions.model.vocabulary.SetResourceProfile;
import eu.europeana.set.stats.service.UsageStatsService;
import eu.europeana.set.web.config.BeanNames;
import eu.europeana.set.web.config.BuildInfo;
import eu.europeana.set.web.http.UserSetHttpHeaders;
import eu.europeana.set.web.model.search.CollectionPage;
import eu.europeana.set.web.search.UserSetLdSerializer;
import eu.europeana.set.web.service.RequestPathMethodService;
import eu.europeana.set.web.service.UserSetService;
import eu.europeana.set.web.service.authorization.UserSetAuthorizationService;
import eu.europeana.set.web.service.authorization.UserSetAuthorizationServiceImpl;
import eu.europeana.set.web.service.authorization.UserSetAuthorizationUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

public class BaseRest extends BaseRestController {

  @Resource
  UserSetConfiguration configuration;

  @Resource
  private UserSetService userSetService;

  @Resource
  UserSetAuthorizationService authorizationService;

  @Resource(name = BeanNames.BEAN_WRITE_LOCK_AUTH_SERVICE)
  WriteLockAuthorizationService apiWriteLockAuthService;

  @Resource
  UsageStatsService usageStatsService;

  @Resource
  protected BuildInfo buildInfo;

  @Resource
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
      throws EuropeanaI18nApiException {
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
        throw new InvalidParamException(Arrays.asList(PREFER + " header", " ", preferHeader), e);
      } else {
        throw new InvalidParamException(Arrays.asList(CommonApiConstants.QUERY_PARAM_PROFILE, " ", profile), e);
      }
    }
    return profiles;
  }

  /**
   * Method validates the multiple profile combinations
   * 
   * @param profiles
   * @param profileStr
   * @throws EuropeanaI18nApiException
   */
  protected void validateMultipleProfiles(List<SetPageProfile> profiles, String profileStr)
      throws EuropeanaI18nApiException {
    // remove profile 'debug' as it's only used for stackTrace purpose
    // For now maximum two profile-combinations are possible
    // profile=facets OR profile=facets,minimal OR profile=standard,facets OR
    // profile=itemDescription,facets
    if (profiles.size() > 2) {
      throw new InvalidParamException(Arrays.asList(CommonApiConstants.QUERY_PARAM_PROFILE,
              "Only one of these should be provided - " + StringUtils.remove(profileStr, SetPageProfile.FACETS.getProfileParamValue()),
               profiles.toString()));
    }
    // For now - if multiple profile then one of them has to be facets
    if (profiles.size() == 2 && !profiles.contains(SetPageProfile.FACETS)) {
      throw new InvalidParamException(Arrays.asList(CommonApiConstants.QUERY_PARAM_PROFILE,
          "These profiles are not supported together " + profileStr ,
    profiles.toString()));
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
  protected String serializeUserSet(SetPageProfile profile, UserSet storedUserSet) throws EuropeanaApiException {
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
  protected String serializeUserSet(SetResourceProfile profile, UserSet storedUserSet) throws EuropeanaApiException {
      // prepare data for serialization according to the profile
      getUserSetService().applyProfile(storedUserSet, profile);
      UserSetLdSerializer serializer = new UserSetLdSerializer();
      return serializer.serialize(storedUserSet);
  }

  protected String serializeCollectionPage(CollectionPage itemPage) throws EuropeanaApiException {
    try {
      // prepare data for serialization according to the profile
      UserSetLdSerializer serializer = new UserSetLdSerializer();
      return serializer.serialize(itemPage);
    } catch (IOException e) {
      throw new EuropeanaApiException("Error serialising Collection Page", e);
    }
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
    return buildInfo.getAppVersion();
  }

  protected ResponseEntity<String> buildResponseEntity(UserSet storedUserSet,
      final SetResourceProfile profile, final HttpStatusCode responseStatus,
      Map<String, String> additionalHeaders, HttpServletRequest request) throws EuropeanaApiException {
    String serializedUserSetJsonLdStr = serializeUserSet(profile, storedUserSet);

    String etag = generateETag(storedUserSet.getModified(), FORMAT_JSONLD, getApiVersion());

    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
    headers.add(LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
    headers.add(LINK, UserSetHttpHeaders.VALUE_BASIC_RESOURCE);
    headers.add(ALLOW, createAllowHeader(request));
    // headers.add(HttpHeaders.ALLOW, UserSetHttpHeaders.ALLOW_PG);
    if (additionalHeaders != null) {
      for (Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
        headers.add(entry.getKey(), entry.getValue());
      }
    }

    // generate “ETag”;
    headers.add(UserSetHttpHeaders.ETAG, etag);
    // Last Modified date has to be of RFC 1123 format or else it would be emitted from the response
    headers.add(LAST_MODIFIED, DateUtils.getRFC_1123_FormatDate(storedUserSet.getModified()));

    return new ResponseEntity<>(serializedUserSetJsonLdStr, headers, responseStatus);
  }



  /**
   * Builds the Set Paginated response
   * @param setPage collection page
   * @param modified userSet.getModified() date
   * @param profile profile requested
   * @param request http request
   * @return
   * @throws EuropeanaApiException
   */
  protected ResponseEntity<String> buildSetPageResponse(CollectionPage setPage, Date modified,
      SetPageProfile profile,HttpServletRequest request) throws EuropeanaApiException {
    String jsonBody = "";
    jsonBody = serializeCollectionPage(setPage);
    String etag = generateETag(modified, FORMAT_JSONLD, getApiVersion());

    // build response
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(7);
    headers.add(LINK, UserSetHttpHeaders.VALUE_BASIC_CONTAINER);
    headers.add(LINK, UserSetHttpHeaders.VALUE_BASIC_RESOURCE);
    // headers.add(ALLOW, UserSetHttpHeaders.ALLOW_GPD);
    headers.add(ALLOW, createAllowHeader(request));
    headers.add(VARY, PREFER);
    headers.add(PREFERENCE_APPLIED, profile.getPreferenceApplied());
    // generate “ETag”;
    headers.add(ETAG, etag);
    // Last Modified date has to be of RFC 1123 format or else it would be emitted from the response
    headers.add(LAST_MODIFIED, DateUtils.getRFC_1123_FormatDate(modified));
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
          throws EuropeanaI18nApiException {

    // prevent write operations when the application is locked
    try {
      apiWriteLockAuthService.checkWriteLockInEffect(operation);
    } catch (ApiWriteLockException e) {
      throw new EuropeanaI18nApiException(e.getMessage(), "423_locked_maintenance",
              "Locked for maintenance",
              HttpStatus.LOCKED,
              ErrorConfig.LOCKED_MAINTENANCE, Arrays.asList(e.getMessage()), e);
    }
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
      throw new ApplicationAuthenticationException(ErrorMessage.TOKEN_INVALID_401, null,
          HttpStatus.UNAUTHORIZED, e);
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

  protected ResponseEntity<String> buildResponse(String jsonStr, HttpStatusCode httpStatus) {
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