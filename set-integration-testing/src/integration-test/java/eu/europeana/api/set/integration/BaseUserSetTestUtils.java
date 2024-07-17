package eu.europeana.api.set.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.core.Authentication;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.containers.output.WaitingConsumer;
import eu.europeana.api.commons.exception.ApiKeyExtractionException;
import eu.europeana.api.commons.exception.AuthorizationExtractionException;
import eu.europeana.api.commons.oauth2.utils.OAuthUtils;
import eu.europeana.api.set.integration.connection.http.EuropeanaOauthClient;
import eu.europeana.set.UserSetApp;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.config.UserSetConfigurationImpl;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.mongo.service.PersistentUserSetService;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.model.WebUserSetImpl;
import eu.europeana.set.web.model.search.FacetValue;
import eu.europeana.set.web.service.UserSetService;
import eu.europeana.set.web.service.authorization.UserSetAuthorizationUtils;
import eu.europeana.set.web.service.impl.UserSetServiceImpl;

/**
 * This is a base test class for UserSet testing, which contains base supporting functionality, such
 * as JWT token generation.
 * 
 * @author Roman Graf on 23-09-2020.
 */
@SuppressWarnings("deprecation")
@AutoConfigureMockMvc
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ComponentScan(basePackageClasses = UserSetApp.class)
@ContextConfiguration(locations = {"classpath:set-web-context.xml"})
public abstract class BaseUserSetTestUtils {

  protected static final String BASE_URL = "/set/";
  public static final String USER_SET_REGULAR = "/content/userset_regular.json";
  public static final String USER_SET_MANDATORY = "/content/userset_mandatory.json";
  public static final String USER_SET_OPEN = "/content/userset_open.json";
  public static final String USER_SET_MULTIPLE_QUERY_OPEN =
      "/content/userset_open_multiple_query.json";
  public static final String USER_SET_LARGE_QUERY_OPEN = "/content/userset_open_large_query.json";
  public static final String USER_SET_LARGE = "/content/userset_large.json";
  public static final String USER_SET_LARGE2 = "/content/userset_large2.json";
  public static final String USER_SET_TATTOOS = "/content/userset_tattoos.json";
  public static final String USER_SET_REGULAR_PUBLIC = "/content/userset_regular_public.json";
  public static final String USER_SET_REGULAR_PUBLISHED = "/content/userset_regular_published.json";
  public static final String USER_SET_REGULAR_UPDATED = "/content/userset_regular_updated.json";
  public static final String USER_SET_COMPLETE_PUBLIC = "/content/userset_complete.json";
  public static final String USER_SET_BOOKMARK_FOLDER = "/content/userset_bookmark_folder.json";
  public static final String USER_SET_BOOKMARK_FOLDER_1 = "/content/userset_bookmark_folder_1.json";
  public static final String USER_SET_BEST_ITEMS = "/content/userset_entity_best_items.json";
  public static final String USER_SET_INVALID_ITEMS = "/content/userset_invalid_items.json";
  public static final String UPDATED_USER_SET_CONTENT = "/content/updated_regular.json";
  public static final String ENTITY_USER_SET_REGULAR = "/content/entity_userset.json";
  public static final String ENTITY_USER_SET_PROVIDER_ID =
      "/content/entity_userset_provider_id.json";
  public static final String ENTITY_USER_SET_REGULAR_2 = "/content/entity_userset_2.json";
  public static final String ENTITY_USER_SET_INVALID_SUBJECT =
      "/content/entity_userset_invalid_subject.json";
  public static final String ENTITY_USER_SET_INVALID_MULTIPLE_SUBJECTS =
      "/content/entity_userset_invalid_multiple_subjects.json";
  public static final String ENTITY_USER_SET_UPDATE = "/content/entity_userset_update.json";
  public static final String ENTITY_USER_SET_UPDATE_2 = "/content/entity_userset_update_2.json";
  public static final String ENTITY_USER_SET_NO_SUBJECT =
      "/content/entity_userset_invalid_subject.json";

  protected MockMvc mockMvc;

  @Autowired
  protected WebApplicationContext wac;

  @Autowired
  private UserSetService userSetService;

  @Autowired
  @Qualifier(UserSetConfiguration.BEAN_SET_PERSITENCE_SERVICE)
  PersistentUserSetService mongoPersistance;
  
  @Autowired
  private UserSetConfiguration configuration;
  // format: user
  public static final String USER_REGULAR = "userid1:username1:USER";
  public static final String USER_EDITOR = "editor1:editoruser1:EDITOR";
  public static final String USER_EDITOR2 = "editor2:editoruser2:EDITOR";
  public static final String USER_ENTITY_GALLERIES =
      "entity-galleries-userid:entity-galleries-userid:PUBLISHER";
  public static final String USER_PUBLISHER = "publisher-userid:publisher-username:PUBLISHER";
  public static final String USER_ADMIN = "admin-userid1:admin-username1:ADMIN";

  protected static String regularUserToken = OAuthUtils.TYPE_BEARER + " " + USER_REGULAR;
  protected static String editorUserToken = OAuthUtils.TYPE_BEARER + " " + USER_EDITOR;
  protected static String editor2UserToken = OAuthUtils.TYPE_BEARER + " " + USER_EDITOR2;
  protected static String creatorEntitySetUserToken =
      OAuthUtils.TYPE_BEARER + " " + USER_ENTITY_GALLERIES;
  protected static String publisherUserToken = USER_PUBLISHER;
  protected static String adminUserToken = OAuthUtils.TYPE_BEARER + " " + USER_ADMIN;
  protected static List<PersistentUserSet> createdUserSets = new ArrayList<>();
  /**
   * can be used to enable AUTH for local environment
   */
  protected static boolean DISABLE_AUTH = true;
  private static final MongoContainer MONGO_CONTAINER;

  static {
//    MONGO_CONTAINER = new MongoDBContainer("mongo:6.0.14-jammy")
    final String serviceDB = "admin";   // to change to "set-api-test"
    MONGO_CONTAINER = new MongoContainer(serviceDB)
        .withLogConsumer(new WaitingConsumer()
        .andThen(new ToStringConsumer()));

    MONGO_CONTAINER.start();
  }
  
  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("mongodb.set.connectionUrl", MONGO_CONTAINER::getConnectionUrl);
  }

  @BeforeAll
  protected void initApplication() {
    if (mockMvc == null) {
      this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    disableOauth();
    changeProperiesForTests();
  }

  private void disableOauth() {
    if (DISABLE_AUTH) {
      ((UserSetConfigurationImpl) configuration).getSetProperties()
          .put(UserSetConfigurationImpl.KEY_AUTH_DISABLED, "true");
    }
  }
  
  private void changeProperiesForTests() {
    ((UserSetConfigurationImpl) configuration).getSetProperties()
          .put(UserSetConfigurationImpl.COLLECTION_SIZE_MAX, "249");
  }

  public static void initRegularUserToken() {
    if (DISABLE_AUTH) {
      return;
    }
    regularUserToken = retrieveOatuhToken(EuropeanaOauthClient.REGULAR_USER);
  }

  public static void initPublisherUserToken() {
    if (DISABLE_AUTH) {
      return;
    }
    publisherUserToken = retrieveOatuhToken(EuropeanaOauthClient.PUBLISHER_USER);
  }

  public static void initEntitySetTokens() {
    if (DISABLE_AUTH) {
      return;
    }
    editorUserToken = retrieveOatuhToken(EuropeanaOauthClient.EDITOR_USER);
    editor2UserToken = retrieveOatuhToken(EuropeanaOauthClient.EDITOR2_USER);
    creatorEntitySetUserToken = retrieveOatuhToken(EuropeanaOauthClient.CREATOR_ENTITYSETS);
  }
  
  public static void initAdminUserToken() {
    if (DISABLE_AUTH) {
      return;
    }
    adminUserToken = retrieveOatuhToken(EuropeanaOauthClient.ADMIN_USER);
  }  

  protected void deleteCreatedSets() {
    getMongoPersistance().removeAll(createdUserSets);
    createdUserSets.clear();
  }

  public UserSetServiceImpl getUserSetService() {
    return (UserSetServiceImpl) userSetService;
  }

  public UserSetConfiguration getConfiguration() {
    return configuration;
  }

  public static String retrieveOatuhToken(String user) {
    EuropeanaOauthClient oauthClient = new EuropeanaOauthClient();
    return oauthClient.getOauthToken(user);
  }

  /**
   * This method extracts JSON content from a file
   * 
   * @param resource
   * @return JSON string
   * @throws IOException
   */
  protected String getJsonStringInput(String resource) throws IOException {

    try (InputStream resourceAsStream = getClass().getResourceAsStream(resource)) {
      List<String> lines = IOUtils.readLines(resourceAsStream, StandardCharsets.UTF_8);
      StringBuilder out = new StringBuilder();
      for (String line : lines) {
        out.append(line);
      }
      return out.toString();
    }
  }

  protected WebUserSetImpl createTestUserSet(String testFile, String token) throws Exception {
    String requestJson = getJsonStringInput(testFile);
    UserSet set = getUserSetService().parseUserSetLd(requestJson);
    Authentication authentication = getAuthentication(token);
    WebUserSetImpl createdSet =
        (WebUserSetImpl) getUserSetService().storeUserSet(set, authentication);
    //keep the list of created sets to delete in the end
    createdUserSets.add(createdSet);
    return createdSet;
  }

  protected void deleteBookmarkFolder(String token)
      throws ApiKeyExtractionException, AuthorizationExtractionException, UserSetNotFoundException {
    Authentication authentication = getAuthentication(token);
    String creatorId = UserSetUtils.buildUserUri(getConfiguration().getUserDataEndpoint(),
        (String) authentication.getPrincipal());
    UserSet bookmarkFolder = getUserSetService().getBookmarkFolder(creatorId);
    if (bookmarkFolder != null) {
      getUserSetService().deleteUserSet(bookmarkFolder.getIdentifier());
    }

  }


  protected Authentication getAuthentication(String token)
      throws ApiKeyExtractionException, AuthorizationExtractionException {

    if (DISABLE_AUTH) {
      return UserSetAuthorizationUtils.createAuthentication(token);
    }

    RsaVerifier signatureVerifier = new RsaVerifier(getConfiguration().getJwtTokenSignatureKey());
    String authorizationApiName = getConfiguration().getAuthorizationApiName();
    List<? extends Authentication> oauthList =
        OAuthUtils.extractAuthenticationList(token, signatureVerifier, authorizationApiName);
    for (Authentication authentication : oauthList) {
      if (authorizationApiName.equals(authentication.getDetails())) {
        return authentication;
      }
    }

    return null;
  }

  protected boolean containsKeyOrValue(String jsonString, String propertyOrValue) {
    return StringUtils.contains(jsonString, "\"" + propertyOrValue + "\"");
  }

  protected int noOfOccurance(String jsonString, String property) {
    return StringUtils.countMatches(jsonString, "\"" + property + "\"");
  }

  protected String getvalueOfkey(String jsonString, String property) throws JSONException {
    assertNotNull(jsonString);
    JSONObject json = new JSONObject(jsonString);
    String value = json.getString(property);
    assertNotNull(value);
    return value;
  }

  protected String getSetIdentifier(String baseUrl, String result) throws JSONException {
    assertNotNull(result);
    JSONObject json = new JSONObject(result);
    String id = json.getString("id");
    assertNotNull(id);
    String identifier = id.replace(baseUrl, "");
    return identifier;
  }

  protected List<FacetValue> getFacetResultPage(String result) throws JSONException {
    List<FacetValue> facetValueResultPages = new ArrayList<>();
    assertNotNull(result);
    JSONObject json = new JSONObject(result);
    JSONArray facets = json.getJSONArray(WebUserSetFields.FACETS);
    // for now we have only single faceting- hence only one facet will be present
    JSONArray values = ((JSONObject) facets.get(0)).getJSONArray(WebUserSetFields.VALUES);
    for (int i = 0; i < values.length(); i++) {
      JSONObject o = (JSONObject) values.get(i);
      facetValueResultPages.add(new FacetValue(o.getString("label"), o.getLong("count")));
    }
    return facetValueResultPages;
  }

  protected PersistentUserSetService getMongoPersistance() {
    return mongoPersistance;
  }

  protected void addToCreatedSets(String identifier) {
    final WebUserSetImpl userSet = new WebUserSetImpl();
    userSet.setIdentifier(identifier);
    createdUserSets.add(userSet);
  }
  
  protected String getStringValue(String jsonBody, String fieldName) throws JSONException {
    JSONObject json = new JSONObject(jsonBody);
    return json.getString(fieldName);
  }
  
  protected List<String> getStringListValues(String jsonBody, String fieldName) throws JSONException {
    assertNotNull(jsonBody);
    JSONObject json = new JSONObject(jsonBody);
    return Collections.singletonList(json.getString(fieldName));
  }


}
