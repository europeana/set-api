package org.testcontainers.mongodb;

/** This class creates a Mongo container using the dockerfile in the docker-scripts directory. */
public class MongoContainerWrapper extends MongoDBContainer {

  private final String appDb;
  private final String adminUsername = "admin_user";
  private final String adminPassword = "admin_password";
  public static final String MONGO_IMAGE = "mongo:7.0";
  
  int hostPort;
  int defaultMongoPort = 27017;
  
  /**
   * Creates a new Mongo container instance
   *
   * @param adminDb entity database
   */
  public MongoContainerWrapper(String adminDb, int hostPort) {
    super(MONGO_IMAGE);
    //MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0")
    
    if (hostPort > 0) {
      this.addFixedExposedPort(hostPort, defaultMongoPort);
    } else {
      this.withExposedPorts(defaultMongoPort);
    }

    this.withEnv("MONGO_INITDB_ROOT_USERNAME", adminUsername);
    this.withEnv("MONGO_INITDB_ROOT_PASSWORD", adminPassword);
    this.withEnv("MONGO_INITDB_DATABASE", adminDb);
    this.appDb = adminDb;

  }
  
  public String getConnectionUrl() {
    if (!this.isRunning()) {
      throw new IllegalStateException("MongoDBContainer should be started first");
    } else {
      String connectionUrl = String.format(
        "mongodb://%s:%s@%s:%d/%s?authSource=admin&ssl=false",
        adminUsername, adminPassword, this.getHost(), this.getMappedPort(defaultMongoPort), this.getAppDbName());
        return connectionUrl;  
      }   
  }

  public String getAppDbName() {
    return appDb;
  }

  public String getAdminUsername() {
    return adminUsername;
  }

  public String getAdminPassword() {
    return adminPassword;
  }
  
}
