package eu.europeana.set.web.config;

import org.mongodb.morphia.Datastore;
import org.springframework.beans.factory.annotation.Value;
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
@PropertySource(
    value = {"classpath:config/set.properties", "classpath:config/set.user.properties"},
    ignoreResourceNotFound = true)
public class MongoConfig {

  @Value("${mongodb.set.connectionUrl:}")
  private String mongoConnectionUrl;

  @Value("${mongodb.set.truststore:}")
  private String mongoTrustStore;

  @Value("${mongodb.set.truststorepass:}")
  private String mongoTrustStorePass;

  private static final String MODEL_PACKAGE = "eu.europeana.set.definitions";
  
  //
  private ApiMongoConnector mongoConnector;
  
  /**
  <bean id="set_db_mongoConnector" class="eu.europeana.corelib.db.wrapper.ApiMongoConnector" destroy-method="close"/>
  
  <bean id="set_db_morphia_datastore_set" factory-bean="set_db_mongoConnector" factory-method="createDatastore">
      <constructor-arg value="#{setProperties['mongodb.set.connectionUrl']}" type="java.lang.String" />
      <constructor-arg value="#{setProperties['mongodb.set.truststore']}" type="java.lang.String" />
      <constructor-arg value="#{setProperties['mongodb.set.truststorepass']}" type="java.lang.String" />
  </bean>
   **/
  
  @Bean(UserSetConfiguration.BEAN_SET_MONGO_STORE)
  public Datastore createDataStore() {
    return getMongoConnector().createDatastore(mongoConnectionUrl, mongoTrustStore, mongoTrustStorePass, -1, MODEL_PACKAGE );
  }

  @Bean("annotationMongoConnector")
  protected ApiMongoConnector getMongoConnector() {
    if(mongoConnector == null) {
      mongoConnector = new ApiMongoConnector();
    }
    return mongoConnector;
  }
}
