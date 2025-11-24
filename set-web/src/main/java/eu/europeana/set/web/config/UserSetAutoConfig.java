package eu.europeana.set.web.config;

import eu.europeana.api.commons_sb3.auth.AuthenticationBuilder;
import eu.europeana.api.commons_sb3.auth.AuthenticationConfig;
import eu.europeana.api.commons_sb3.error.i18n.I18nService;
import eu.europeana.api.commons_sb3.error.i18n.I18nServiceImpl;
import eu.europeana.api.commons_sb3.oauth2.service.impl.EuropeanaClientDetailsService;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * CLass to autoconfigure application by instantiating required beans
 */
@Configuration()
@PropertySource(
    value = {"classpath:set.common.properties", "classpath:set.user.properties"},
    ignoreResourceNotFound = true)
public class UserSetAutoConfig{

  @Value("${europeana.apikey.serviceurl}")
  private String apikeyServiceUrl;

  @Value("${keycloak.token.endpoint:}")
  private String tokenEndpoint;

  @Value("${keycloak.token.grant.params:}")
  private String grantParams;

  @Bean(name = BeanNames.BEAN_CLIENT_DETAILS_SERVICE)
  public EuropeanaClientDetailsService getApiKeyClientDetailsService(){
    EuropeanaClientDetailsService clientDetails = new EuropeanaClientDetailsService();
    clientDetails.setApiKeyServiceUrl(apikeyServiceUrl);
    AuthenticationConfig config = new AuthenticationConfig(loadProperties());
    clientDetails.setAuthHandler(AuthenticationBuilder.newAuthentication(config));
    return clientDetails;
  }

  private Properties loadProperties() {
    Properties properties = new Properties();
    properties.setProperty(AuthenticationConfig.CONFIG_TOKEN_ENDPOINT,tokenEndpoint);
    properties.setProperty(AuthenticationConfig.CONFIG_GRANT_PARAMS,grantParams);
    return properties;
  }

  @Bean(name = BeanNames.BEAN_I18N_MESAGE_SOURCE)
  public MessageSource i18nMessagesSource(){
    ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
    source.setBasename("classpath:messages");
    source.setDefaultEncoding(StandardCharsets.UTF_8.name());
    return source;
  }

  @Bean(name =BeanNames.BEAN_I18N_SERVICE)
  public I18nService getI18nService() {
    return new I18nServiceImpl(i18nMessagesSource());
  }
}