package eu.europeana.api.set.integration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.mongodb.MongoContainerWrapper;

public class MongoContainerStarter {

  static Logger logger = LogManager.getLogger(MongoContainerStarter.class);

  protected static MongoContainerWrapper MONGO_CONTAINER;
  static {
    if (shouldStartMongo()) {
      logger.info("Starting mongo container!");
      startMongo();
    }
  }

  protected static boolean shouldStartMongo() {
    return true;
  }

  protected static void startMongo() {
    // MONGO_CONTAINER = new MongoDBContainer("mongo:6.0.14-jammy")
    final String appDb = "admin";
    // for debugging set the host port to 27017 or 27018
    int hostPort = -1;

    MONGO_CONTAINER = new MongoContainerWrapper(appDb, hostPort);
    MONGO_CONTAINER.start();
  }

}
