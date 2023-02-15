package eu.europeana.set.web.config;

import org.mongodb.morphia.Datastore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import eu.europeana.corelib.db.wrapper.ApiMongoConnector;
import eu.europeana.set.definitions.config.UserSetConfiguration;

/**
 * This mongo config crates a org.mongodb.morphia.Datastore instead of a dev.morphia.Datastore
 * because the created mongo dao objects are based on the NosqlDaoImpl class from the commons-api
 * which uses the given Datastore type.
 * 
 * @author StevaneticS
 */
@Configuration
@EnableCaching
@PropertySource(
    value = {"classpath:set.common.properties", "classpath:set.user.properties"},
    ignoreResourceNotFound = true)
public class MongoConfig {

  @Value("${mongodb.set.connectionUrl:}")
  private String mongoConnectionUrl;

  @Value("${mongodb.set.truststore:}")
  private String mongoTrustStore;

  @Value("${mongodb.set.truststorepass:}")
  private String mongoTrustStorePass;

  private static final String[] MODEL_PACKAGES = new String[]{"eu.europeana.set.definitions", "eu.europeana.api.commons.nosql.entity"};
  
  private ApiMongoConnector mongoConnector;
  
  @Bean(UserSetConfiguration.BEAN_SET_MONGO_STORE)
  public Datastore createDataStore() {
    return getMongoConnector().createDatastore(mongoConnectionUrl, mongoTrustStore, mongoTrustStorePass, -1, MODEL_PACKAGES );
  }

  @Bean("annotationMongoConnector")
  protected ApiMongoConnector getMongoConnector() {
    if(mongoConnector == null) {
      mongoConnector = new ApiMongoConnector();
    }
    return mongoConnector;
  }
}
