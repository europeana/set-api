package eu.europeana.api.set.integration.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.containers.output.WaitingConsumer;
import eu.europeana.api.commons.oauth2.utils.OAuthUtils;
import eu.europeana.api.set.integration.MongoContainer;
import eu.europeana.api.set.integration.config.SetIntegrationConfiguration;
import eu.europeana.api.set.integration.connection.http.EuropeanaOauthClient;
import eu.europeana.set.UserSetApp;
import eu.europeana.set.client.UserSetApiClient;
import eu.europeana.set.client.config.ClientConfiguration;
import eu.europeana.set.client.exception.SetApiClientException;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.config.UserSetConfigurationImpl;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.mongo.service.PersistentUserSetService;
import eu.europeana.set.web.model.WebUserSetImpl;

@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ComponentScan(basePackageClasses = UserSetApp.class)
@ContextConfiguration(locations = {"classpath:set-web-context.xml"})
@EnableAutoConfiguration
public class BaseUserSetClientTest {

  private static MongoContainer MONGO_CONTAINER;
  static {
    if(shouldStartMongo()) {
      startMongo();
    }
  }

  private static boolean shouldStartMongo() {
    return true;
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

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    if(shouldStartMongo()) {
      registry.add("mongodb.set.connectionUrl", MONGO_CONTAINER::getConnectionUrl);
    } else {
      registry.add("mongodb.set.connectionUrl",  () -> "mongodb://127.0.0.1:27017/set_test");
    } 
  }
  
  @Autowired
  @Qualifier(UserSetConfiguration.BEAN_SET_PERSITENCE_SERVICE)
  PersistentUserSetService mongoPersistance;
  
  @Autowired
  private UserSetConfiguration configuration;
      
  protected static List<PersistentUserSet> createdUserSets = new ArrayList<>();

  protected Logger log = LogManager.getLogger(getClass());

  public static final String USER_SET_CONTENT = "/content/userset.json";
  public static final String USER_SET_UPDATE_CONTENT = "/content/usersetupdate.json";

  protected UserSetApiClient apiClient;
  public static final String USER_REGULAR = "userid1:username1:USER";
  protected static String regularUserToken = OAuthUtils.TYPE_BEARER + " " + USER_REGULAR;
  
  protected static boolean DISABLE_AUTH = true;
  protected static String START = "{";
  protected static String END = "}";

  protected void initObjects(int port) throws SetApiClientException {
    /*this needs to be called before the initialization of the apiClient 
    (because the token is used in the apiClient)
    */
    if(DISABLE_AUTH) {
      ((UserSetConfigurationImpl) configuration).getSetProperties()
      .put(UserSetConfigurationImpl.KEY_AUTH_DISABLED, "true");
    }
    else {
      regularUserToken = retrieveOatuhToken(EuropeanaOauthClient.REGULAR_USER);
    }

    apiClient = new UserSetApiClient(new ClientConfiguration(loadClientProperties(port)));
  }

  /**
   * Should be called every time after creating a new set.
   * @param identifier
   */
  protected void addToCreatedSets(String identifier) {
    final WebUserSetImpl userSet = new WebUserSetImpl();
    userSet.setIdentifier(identifier);
    createdUserSets.add(userSet);
  }
	  
  /**
   * Should be called after each test.
   */
  protected void deleteCreatedSets() {
    mongoPersistance.removeAll(createdUserSets);
    createdUserSets.clear();
  }

  protected Properties loadClientProperties(int port) {
    Properties properties = new Properties();
    properties.put(ClientConfiguration.PROP_SET_SERVICE_URI, "http://localhost:" + port + "/set");
    properties.put(ClientConfiguration.PROP_SET_API_KEY, "test");
    properties.put(ClientConfiguration.PROP_OAUTH_REGULAR_USER_TOKEN, regularUserToken);
    properties.put(ClientConfiguration.PROP_OAUTH_SERVICE_URI, SetIntegrationConfiguration.getInstance().getOauthServiceUri());
    properties.put(ClientConfiguration.PROP_OAUTH_REQUEST_PARAMS, SetIntegrationConfiguration.getInstance().getOauthRequestParamsRegular());

    return properties;
  }

	protected static String retrieveOatuhToken(String user) {
	  EuropeanaOauthClient oauthClient = new EuropeanaOauthClient();
	  return oauthClient.getOauthToken(user);
	}

	/**
	 * This method creates test set object
	 * 
	 * @param resource JSON test file
	 * @parem profile
	 * @return response entity that contains response body, headers and status code.
	 * @throws IOException
	 */
	protected String storeTestUserSet(String resource, String profile) throws SetApiClientException, IOException {
		String requestBody = getJsonStringInput(resource);
		UserSet uSet= apiClient.getWebUserSetApi().createUserSet(requestBody, profile);
		addToCreatedSets(uSet.getIdentifier());
		return uSet.getIdentifier();
	}

	protected String getJsonStringInput(String resource) throws IOException {
		InputStream resourceAsStream = getClass().getResourceAsStream(resource);

		StringBuilder out = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream));
		for (String line = br.readLine(); line != null; line = br.readLine())
			out.append(line);
		br.close();
		return out.toString();

	}

	protected void deleteUserSet(UserSet set) throws SetApiClientException {
		String re = apiClient.getWebUserSetApi().deleteUserSet(set.getIdentifier());
		assertEquals(String.valueOf(HttpStatus.SC_OK), re);
		log.trace("User set deleted: /" + set.getIdentifier());
	}

	protected UserSet getUserSet(UserSet set) throws SetApiClientException {
		return apiClient.getWebUserSetApi().getUserSet(set.getIdentifier(), null);
	}
	
	
}
