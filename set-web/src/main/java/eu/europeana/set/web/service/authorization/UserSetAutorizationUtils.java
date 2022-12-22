package eu.europeana.set.web.service.authorization;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import eu.europeana.api.commons.exception.AuthorizationExtractionException;
import eu.europeana.api.commons.oauth2.model.impl.EuropeanaApiCredentials;
import eu.europeana.api.commons.oauth2.model.impl.EuropeanaAuthenticationToken;
import eu.europeana.api.commons.oauth2.utils.OAuthUtils;
import eu.europeana.set.web.model.vocabulary.Roles;

public class UserSetAutorizationUtils {

  public static Authentication createAuthentication(String userId, String userName, Roles role) {
    Authentication auth;
    
    GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(role.getName());
    auth = new EuropeanaAuthenticationToken(List.of(grantedAuthority), "usersets", userId,
        new EuropeanaApiCredentials(userName, "unknown-client"));
    
    return auth;
  }
  
  public static Authentication createAuthentication(String plainTextToken) throws AuthorizationExtractionException {
    final int SEPARATOR_COUNT = 2;
    if(StringUtils.isBlank(plainTextToken) || StringUtils.countMatches(plainTextToken, ':') != SEPARATOR_COUNT) {
      throw new AuthorizationExtractionException("invalid plain text token: " + plainTextToken);
    }
    ;
    String plainToken = plainTextToken.replace(OAuthUtils.TYPE_BEARER, "");
    String[] parts = plainToken.trim().split("\\:");
    return UserSetAutorizationUtils.createAuthentication(parts[0], parts[1], Roles.valueOf(parts[SEPARATOR_COUNT]));
  }
}
