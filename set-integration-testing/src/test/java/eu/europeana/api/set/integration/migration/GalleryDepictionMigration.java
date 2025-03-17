package eu.europeana.api.set.integration.migration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.api.commons.exception.AuthorizationExtractionException;
import eu.europeana.api.commons.web.exception.ParamValidationException;
import eu.europeana.api.set.integration.BaseUserSetTestUtils;
import eu.europeana.api.set.integration.connection.http.EuropeanaOauthClient;
import eu.europeana.set.UserSetApp;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.model.BaseWebResource;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.vocabulary.SetPageProfile;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.mongo.service.PersistentUserSetService;
import eu.europeana.set.search.exception.SearchApiClientException;
import eu.europeana.set.web.model.WebResource;
import eu.europeana.set.web.search.UserSetQueryBuilder;
import eu.europeana.set.web.service.UserSetService;
import eu.europeana.set.web.service.authorization.UserSetAuthorizationUtils;

@SpringBootTest
@Disabled
public class GalleryDepictionMigration extends BaseUserSetTestUtils {

  private static final Logger LOG = LogManager.getLogger(UserSetApp.class);
  private boolean migrateToGallery = false;
  
  @Resource
  UserSetConfiguration configuration;

  @Resource
  private UserSetService userSetService; 
  
  @Resource(name = UserSetConfiguration.BEAN_SET_PERSITENCE_SERVICE)
  PersistentUserSetService mongoPersistanceService;

  @BeforeAll
  public static void initTokens() {
    if (DISABLE_AUTH) {
      return;
    }
    initRegularUserToken();
    editorUserToken = retrieveOauthToken(EuropeanaOauthClient.EDITOR_USER);
    initPublisherUserToken();
  }

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
//    registry.add("mongodb.set.connectionUrl", MONGO_CONTAINER::getConnectionUrl);
    registry.add("mongodb.set.connectionUrl",  () -> "mongodb://127.0.0.1:27017/set_test");
    //registry.add("mongodb.set.truststore", () -> "");
    //registry.add("mongodb.set.truststorepass", () -> "");
    

  }
  
  /*
   * Generate isShownBy field for all sets of type collection
   */
  @Test
  // @Disabled
  public void generateDepictionForCollections() throws Exception {
    // createTestUserSet(USER_SET_REGULAR, regularUserToken);
    final String collectionsQuery = "type:" + UserSetTypes.COLLECTION.getJsonValue();

    DepictionGenerationReport report = generateDepictionsAndUpdateCollectionType(collectionsQuery);

    LOG.info("Generated depictions: {}", report.getGenerated());
    LOG.info("Skipped sets: {}", report.getSkipped());
    LOG.info("Not generated: {}", report.getNotGenerated());

    assertTrue(report.getGenerated() > 0);
  }



  /*
   * Generate isShownBy field for all sets of type EntityBestItemsSet
   */
  @Test
  // @Disabled
  public void generateDepictionForEntityBestItemsSet() throws Exception {
//    createTestUserSet(USER_SET_REGULAR, regularUserToken);
    final String collectionsQuery = "type:"+UserSetTypes.ENTITYBESTITEMSSET.getJsonValue();
    
    DepictionGenerationReport report = generateDepictionsAndUpdateCollectionType(collectionsQuery);

    LOG.info("Generated depictions: {}", report.getGenerated());
    LOG.info("Skipped sets: {}", report.getSkipped());
    LOG.info("Not generated: {}", report.getNotGenerated());
    
    assertTrue(report.getGenerated() > 0);
  }

  private DepictionGenerationReport generateDepictionsAndUpdateCollectionType(
      final String collectionsQuery)
      throws AuthorizationExtractionException, ParamValidationException {
    // create object in database
    UserSetQueryBuilder queryBuilder = new UserSetQueryBuilder();
    
    Authentication adminAuth = UserSetAuthorizationUtils.createAuthentication(adminUserToken);
    
    final int pageSize = 200;
    //int page = Integer.valueOf(UserSetUtils.DEFAULT_PAGE);
    String sort = WebUserSetModelFields.CREATED + " asc";
    UserSetQuery searchQuery = queryBuilder.buildUserSetQuery(collectionsQuery, null, sort, 0,
        pageSize, getConfiguration());
    final ArrayList<SetPageProfile> profiles = new ArrayList<>();
    profiles.add(SetPageProfile.ITEMS);
    DepictionGenerationReport report = new DepictionGenerationReport();
    
    ResultSet<? extends UserSet> results = null;
    //page index startw with 1, but that is set at the beginning of the 
    int page= 0;
    do {
      //move to first/next page
      page++;
      searchQuery.setPageNr(page);
      results = getUserSetService().search(searchQuery, null, profiles, adminAuth);
      final int found = results.getResults() == null? 0 : results.getResults().size();
      LOG.info(found + " Items found on page: " + page);
      
      if(hasNoItems(results)) {
        break; // stop if no results found anymore
      }
      generateDepictions(results.getResults(), report);
      
      LOG.info("Completed Depiction Generation for result pages: {}", page);

      searchQuery.setPageNr(page);
      
      //brake
      //results = null;
    } while (hasItems(results));
    return report;
  }
  

  private boolean hasItems(ResultSet<? extends UserSet> results) {
    return !hasNoItems(results);
  }

  private boolean hasNoItems(ResultSet<? extends UserSet> results) {
    return results == null || results.getResults() == null || results.getResults().isEmpty();
  }

  private void generateDepictions(List<? extends UserSet> results,
      DepictionGenerationReport report) {

    for (UserSet userSet : results) {
      if (userSet.isOpenSet() || userSet.isBookmarksFolder()) {
        // bookmarks is redundant, but we keep it for future
        //open/dynamic sets must not be migrated
        report.increaseSkipped();
        continue;
      }
      
      final boolean hasNoItems = userSet.getItems() == null || userSet.getItems().isEmpty();
      if(hasNoItems) {
        LOG.debug("Skip update for set without items id: {}",
            userSet.getIdentifier());
        report.increaseSkipped();
        continue;
      }
      
      if (hasDepiction(userSet.getIsShownBy())) {
        report.increaseSkipped();
        continue;
      }
      
      if(userSet.getItems().size()==1) {
        LOG.debug("Set has only 1 item id:{}", userSet.getIdentifier());
      }else {
        LOG.debug("Set has no depiction and more items id:{}", userSet.getIdentifier());
      }

      
      final WebResource isShownBy = generateDepiction(userSet);
      //do not update set if the depiction cannot be generated
      final boolean shouldSkip = (isShownBy == null && !userSet.isCollection()) || (isShownBy != null && !isShownBy.hasThumbnail());
      if (shouldSkip) {
        report.increaseNotGenerated();
        LOG.debug("Skip update, could not generate depiction for set with id:type:collectionType - {}:{}:{}",
            userSet.getIdentifier(), userSet.getType(), userSet.getCollectionType());
        continue;
      }

      if(hasDepiction(isShownBy)) {
        userSet.setIsShownBy(isShownBy);
      }
      final boolean hasCollectionTypeUpdate = migrateToGallery && hasCollectionTypeUpdate(userSet);
      if (hasCollectionTypeUpdate) {
        // update collection type to gallery if the collection has less than 100 items
        userSet.setCollectionType(WebUserSetFields.TYPE_GALLERY);
        report.increaseUpdatedCollectionType();
      }
      
      //update into database
      if(hasCollectionTypeUpdate || hasDepiction(isShownBy)) {
        UserSet updatedSet = mongoPersistanceService.store((PersistentUserSet) userSet);
        if(hasDepiction(updatedSet.getIsShownBy())) {
          //updated is shownBy
        report.increaseGenerated();
          LOG.debug("Generated depiction for set with id {}: {}", updatedSet.getIdentifier(),
              updatedSet.toString());
      } else {
          //note the migrateToGallery to enable/disable promotion to galleries
          LOG.debug("Updated collection type, but didn't Generated depiction for set with id {}: {}", updatedSet.getIdentifier(),
              updatedSet.toString());
        report.increaseNotGenerated();
      }
      } else {
        //should probably not come here, but if it happens, the set was not updated
        LOG.debug("The set was not updated into the database, depiction and collection type were not updated for set with id: {}", userSet.getIdentifier());
    }
      

      
    }
    LOG.info("Generated depictions: {}", report.getGenerated());
    LOG.info("Skipped sets: {}", report.getSkipped());
    LOG.info("Not generated: {}", report.getNotGenerated());
    LOG.info("Updated collectionType: {}", report.getUpdatedCollectionType());
  }

  private boolean hasCollectionTypeUpdate(UserSet userSet) {
    return userSet.isCollection() 
        && !WebUserSetFields.TYPE_GALLERY.equals(userSet.getCollectionType())
        && (userSet.getItems() == null || userSet.getItems().size() < 100);
  }

  private boolean hasDepiction(final BaseWebResource isShownBy) {
    return isShownBy != null && isShownBy.hasThumbnail();
  }

  private WebResource generateDepiction(UserSet userSet) {

    try {
      return getUserSetService().generateDepiction(userSet);
    } catch (SearchApiClientException e) {
      //work with best user effort
      LOG.info("Cannot generate depiction for set: {}, {}", userSet.getIdentifier(),
          e.getMessage());
      //e.printStackTrace();
      return null;
    }
  }
}
