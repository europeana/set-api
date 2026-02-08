package eu.europeana.api.set.integration;

import org.springframework.security.core.Authentication;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import eu.europeana.api.commons_sb3.oauth2.model.impl.EuropeanaApiCredentials;

public class IntegrationTestSetup extends BaseUserSetTestUtils {

  protected static boolean START_MONGO = true;
    
  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    if(shouldStartMongo()) {
      registry.add("mongodb.set.connectionUrl", MONGO_CONTAINER::getConnectionUrl);
    } else {
      //if local database installation is used
      registry.add("mongodb.set.connectionUrl",  () -> "mongodb://127.0.0.1:27017/set_test");
    } 
  }
  
  protected String getUserName(Authentication authetication) {
    return ((EuropeanaApiCredentials)authetication.getCredentials()).getUserName();
  }

}
