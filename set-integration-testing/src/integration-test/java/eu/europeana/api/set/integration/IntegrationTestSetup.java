package eu.europeana.api.set.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.containers.output.WaitingConsumer;

public class IntegrationTestSetup extends BaseUserSetTestUtils {

  protected static boolean START_MONGO = true;
  private static MongoContainer MONGO_CONTAINER;

  static {
    if(shouldStartMongo()) {
      startMongo();
    }
  }
  
  private static void startMongo() {
    // MONGO_CONTAINER = new MongoDBContainer("mongo:6.0.14-jammy")
    final String serviceDB = "admin"; // to change to "set-api-test"
    // for debugging set the host port to 27017 or 27018
    int hostPort = -1;

    MONGO_CONTAINER = new MongoContainer(serviceDB, hostPort)
        .withLogConsumer(new WaitingConsumer().andThen(new ToStringConsumer()));

    MONGO_CONTAINER.start();
  }

  protected static boolean shouldStartMongo() {
    return true;
  }
  
  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    if(shouldStartMongo()) {
      registry.add("mongodb.set.connectionUrl", MONGO_CONTAINER::getConnectionUrl);
    } else {
      registry.add("mongodb.set.connectionUrl",  () -> "mongodb://127.0.0.1:27017/set_test");
    } 
  }
}
