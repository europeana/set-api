package eu.europeana.set.web.config;

import eu.europeana.api.commons_sb3.auth.AuthenticationBuilder;
import eu.europeana.api.commons_sb3.auth.AuthenticationConfig;
import eu.europeana.api.commons_sb3.error.config.ErrorConfig;
import eu.europeana.api.commons_sb3.error.i18n.I18nService;
import eu.europeana.api.commons_sb3.error.i18n.I18nServiceImpl;
import eu.europeana.api.commons_sb3.nosql.service.ApiWriteLockService;
import eu.europeana.api.commons_sb3.nosql.service.WriteLockAuthorizationService;
import eu.europeana.api.commons_sb3.oauth2.service.impl.EuropeanaClientDetailsService;
import java.nio.charset.StandardCharsets;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * CLass to autoconfigure application by instantiating required beans
 */
@Configuration()
@PropertySource(
    value = {"classpath:set.common.properties", "classpath:set.user.properties", "file:/opt/app/config/set.user.properties"},
    ignoreResourceNotFound = true)
public class UserSetAutoConfig{

  private static final Logger LOG = LogManager.getLogger(UserSetAutoConfig.class);

  @Value("${europeana.apikey.serviceurl}")
  private String apikeyServiceUrl;

  @Value("${keycloak.token.endpoint}")
  private String tokenEndpoint;

  @Value("${keycloak.token.grant.params}")
  private String grantParams;

  @Resource(name = "set_db_apilockService")
  private ApiWriteLockService apiWriteLockService;

  @Bean(name = BeanNames.BEAN_CLIENT_DETAILS_SERVICE)
  public EuropeanaClientDetailsService getApiKeyClientDetailsService(){
    EuropeanaClientDetailsService clientDetails = new EuropeanaClientDetailsService();
    clientDetails.setApiKeyServiceUrl(apikeyServiceUrl);
    if (StringUtils.isNotEmpty(tokenEndpoint) && StringUtils.isNotEmpty(grantParams)) {
      AuthenticationConfig config = new AuthenticationConfig(tokenEndpoint, grantParams);
      clientDetails.setAuthHandler(AuthenticationBuilder.newAuthentication(config));
    } else{
      LOG.error("Keycloak token-endpoint and/or grant-parameters NOT set !! ");
    }
    return clientDetails;
  }

  @Bean(name = ErrorConfig.BEAN_I18nService)
  public I18nService getI18nService() {
    ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
    messageSource.setBasenames(ErrorConfig.COMMON_MESSAGE_SOURCE, "classpath:messages");
    messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
    return new I18nServiceImpl(messageSource);
  }

  @Bean(name = BeanNames.BEAN_WRITE_LOCK_AUTH_SERVICE)
  public WriteLockAuthorizationService getWriteLockAuthorizationService() {
    return new WriteLockAuthorizationService(apiWriteLockService);
  }

}