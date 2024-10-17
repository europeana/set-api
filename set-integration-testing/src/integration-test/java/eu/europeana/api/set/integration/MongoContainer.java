package eu.europeana.api.set.integration;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

/** This class creates a Mongo container using the dockerfile in the docker-scripts directory. */
public class MongoContainer extends GenericContainer<MongoContainer> {

  private final String annotationDb;
  private final String adminUsername = "admin_user";
  private final String adminPassword = "admin_password";
  //MUST BE KEPT INLINE WITH THE IMAGE FROM THE DOCKERFILE
  public static final String MONGO_IMAGE = "library/mongo:6.0.14-jammy";
  
  int hostPort;
  int defaultMongoPort = 27017;
  
  /**
   * Creates a new Mongo container instance
   *
   * @param annotationDb entity database
   */
  public MongoContainer(String annotationDb, int hostPort) {
    this(
        //SG: deleteOnExit doesn't seems to make a difference in current version 
        //new ImageFromDockerfile(MONGO_IMAGE, false)
        new ImageFromDockerfile()
            // in test/resources directory
            .withFileFromClasspath("Dockerfile", "mongo-docker/Dockerfile")
            .withFileFromClasspath("init-mongo.sh", "mongo-docker/init-mongo.sh"),
        annotationDb, hostPort);
  }

  private MongoContainer(
      ImageFromDockerfile dockerImageName, String annotationDb, int hostPort) {
    super(dockerImageName);

    if (hostPort > 0) {
      this.addFixedExposedPort(hostPort, defaultMongoPort);
    } else {
      this.withExposedPorts(defaultMongoPort);
    }

    this.withEnv("MONGO_INITDB_ROOT_USERNAME", adminUsername);
    this.withEnv("MONGO_INITDB_ROOT_PASSWORD", adminPassword);
    this.withEnv("MONGO_INITDB_DATABASE", annotationDb);

    this.waitingFor(Wait.forLogMessage("(?i).*Waiting for connections.*", 1));
    this.annotationDb = annotationDb;
  }

  public String getConnectionUrl() {
    if (!this.isRunning()) {
      throw new IllegalStateException("MongoDBContainer should be started first");
    } else {
      String connectionUrl = String.format(
        "mongodb://%s:%s@%s:%d/%s?authSource=admin&ssl=false",
        adminUsername, adminPassword, this.getHost(), this.getMappedPort(defaultMongoPort), this.getAnnotationDb());
        return connectionUrl;  
      }
      
  }

  public String getAnnotationDb() {
    return annotationDb;
  }

  public String getAdminUsername() {
    return adminUsername;
  }

  public String getAdminPassword() {
    return adminPassword;
  }
}