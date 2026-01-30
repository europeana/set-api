package eu.europeana.set.web.service.authorization;

import java.util.List;

import eu.europeana.api.commons_sb3.auth.AuthenticationHandler;
import eu.europeana.api.commons_sb3.auth.apikey.ApikeyBasedAuthentication;
import eu.europeana.api.commons_sb3.definitions.oauth.Role;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import eu.europeana.api.commons_sb3.exception.AuthorizationExtractionException;
import eu.europeana.api.commons_sb3.oauth2.model.impl.EuropeanaApiCredentials;
import eu.europeana.api.commons_sb3.oauth2.model.impl.EuropeanaAuthenticationToken;
import eu.europeana.api.commons_sb3.oauth2.utils.OAuthUtils;
import eu.europeana.set.web.model.vocabulary.Roles;

public class UserSetAuthorizationUtils {

  public static final Logger LOG = LogManager.getLogger(UserSetAuthorizationUtils.class);

  /**
   * Only used for IT
   */
  public static ThreadLocal<AuthenticationHandler> fallBackAuth = new ThreadLocal();

  /**
   * Authentication for api requests.
   * @param authHandler fall back authentication
   */
  public void setFallBackAuth(AuthenticationHandler authHandler) {
      fallBackAuth.set(authHandler);
  }

  /**
   * Close / purge from memory
   */
  public void removeFallBackAuth() {
    fallBackAuth.remove();
  }

  /**
   * Create Authentication for the given user
   * @param userId user id
   * @param userName name of the user
   * @param role role of the user
   * @return Authentication
   */
  public static Authentication createAuthentication(String userId, String userName, Role role) {
    return createAuthentication(userId, userName, role.getName());
  }

  private static Authentication createAuthentication(String userId, String userName,
                                                     final String roleName) {
    GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(roleName);
    return new EuropeanaAuthenticationToken(List.of(grantedAuthority), "usersets", userId,
        new EuropeanaApiCredentials(userName, "unknown-client"));

  }

  public static Authentication createAuthentication(String plainTextToken)
      throws AuthorizationExtractionException {
    final int SEPARATOR_COUNT = 2;
    if (StringUtils.isBlank(plainTextToken)
        || StringUtils.countMatches(plainTextToken, ':') != SEPARATOR_COUNT) {
      throw new AuthorizationExtractionException("invalid plain text token: " + plainTextToken);
    } 
    String plainToken = plainTextToken.replace(OAuthUtils.TYPE_BEARER, "");
    String[] parts = plainToken.trim().split("\\:");
    return createAuthentication(parts[0], parts[1], Roles.valueOf(parts[SEPARATOR_COUNT]));
  }

  /** Method to fetch ApiKey from authentication token
   * @param authentication Authentication object
   * @return apikey String
   */
  public static String extractApiKeyFromAuthorization(Authentication authentication) {
    Object credentials = (authentication != null ? authentication.getCredentials() : null);
    if (credentials instanceof EuropeanaApiCredentials europeanaCredentials) {
      return europeanaCredentials.getApiKey();
    }
    LOG.error("Unable to extract key after Authorization !");
    return null;
  }

  public static AuthenticationHandler getAuthHandler(Authentication authentication) {
    /**
     * Only used for IT
     */
    if (fallBackAuth != null) {
      LOG.info("Using fallback Auth.... !!");
      return fallBackAuth.get();
    }
    return new ApikeyBasedAuthentication(extractApiKeyFromAuthorization(authentication));
  }
}