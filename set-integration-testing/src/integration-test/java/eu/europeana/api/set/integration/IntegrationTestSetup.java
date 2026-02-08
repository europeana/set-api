package eu.europeana.api.set.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

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
}
