package eu.europeana.api.set.integration.migration;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.api.set.integration.BaseUserSetTestUtils;
import eu.europeana.api.set.integration.connection.http.EuropeanaOauthClient;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.mongo.service.PersistentUserSetService;
import eu.europeana.set.search.exception.SearchApiClientException;
import eu.europeana.set.web.model.WebResource;
import eu.europeana.set.web.search.UserSetQueryBuilder;
import eu.europeana.set.web.service.UserSetService;
import eu.europeana.set.web.service.authorization.UserSetAuthorizationUtils;

@SpringBootTest
public class GalleryDepictionMigration extends BaseUserSetTestUtils {

  @Resource
  UserSetConfiguration configuration;

  @Resource
  private UserSetService userSetService; 
  
  @Resource(name = UserSetConfiguration.BEAN_SET_PERSITENCE_SERVICE)
  PersistentUserSetService mongoPersistance;  

  @BeforeAll
  public static void initTokens() {
    if (DISABLE_AUTH) {
      return;
    }
    initRegularUserToken();
    editorUserToken = retrieveOatuhToken(EuropeanaOauthClient.EDITOR_USER);
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
   * Generate isShownBy field for all sets in the (local) db
   */
  @Test
  @Disabled
  public void generateGalleriesWithDepiction() throws Exception {
//    createTestUserSet(USER_SET_REGULAR, regularUserToken);
    
    // create object in database
    UserSetQueryBuilder queryBuilder = new UserSetQueryBuilder();
    
    Authentication adminAuth = UserSetAuthorizationUtils.createAuthentication(adminUserToken);
    
    final int pageSize = 200;
    //int page = Integer.valueOf(UserSetUtils.DEFAULT_PAGE);
    String sort = WebUserSetModelFields.CREATED + " asc";
    UserSetQuery searchQuery =
        queryBuilder.buildUserSetQuery("type:Collection", null, sort, 0, pageSize, getConfiguration());
    final ArrayList<LdProfiles> profiles = new ArrayList<LdProfiles>();
    profiles.add(LdProfiles.STANDARD);
    DepictionGenerationReport report = new DepictionGenerationReport();
    
    ResultSet<? extends UserSet> results = null;
    //page index startw with 1, but that is set at the beginning of the 
    int page= 0;
    do {
      //move to first/next page
      page++;
      searchQuery.setPageNr(page);
      results =
          getUserSetService().search(searchQuery, null, profiles, adminAuth);
      final int found = results.getResults() == null? 0 : results.getResults().size();
      System.out.println(found + " Items found on page: " + page);
      
      if(hasNoItems(results)) {
        break; // stop if no results found anymore
      }
      generateDepictions(results.getResults(), report);
      
      searchQuery.setPageNr(page);
      
      //brake
      //results = null;
    } while (hasItems(results));
    
    System.out.println("Completed Depiction Generation for result pages: " + page);
    
    System.out.println("Generated depictions: " + report.getGenerated());
    System.out.println("Skipped sets: " + report.getSkipped());
    System.out.println("Not generated: " + report.getNotGenerated()); 
    
  }

  private boolean hasItems(ResultSet<? extends UserSet> results) {
    return !hasNoItems(results);
  }

  private boolean hasNoItems(ResultSet<? extends UserSet> results) {
    return results == null || results.getResults() == null || results.getResults().isEmpty();
  }

  private void generateDepictions(List<? extends UserSet> results, DepictionGenerationReport report) {

    for (UserSet userSet : results) {
      if(userSet.isOpenSet() || userSet.isBookmarksFolder() || userSet.isEntityBestItemsSet()) {
        //bookmarks and entity best items sets is redundant, but we keep it for future
        //open/dynamic sets must not be migrated
        report.increaseSkipped();
        continue;
      }
      if(userSet.getIsShownBy() != null) {
        report.increaseSkipped();
        continue;
      }
      
      final WebResource isShownBy = generateGalleryDepiction(userSet);
      //do not update set if the depiction cannot be generated
      if(isShownBy != null) {
        userSet.setIsShownBy(isShownBy);
        userSet.setCollectionType(WebUserSetFields.TYPE_GALLERY);
        mongoPersistance.store(userSet);
        report.increaseGenerated();
      } else {
        report.increaseNotGenerated();
      }
    }
    System.out.println("Generated depictions: " + report.getGenerated());
    System.out.println("Skipped sets: " + report.getSkipped());
    System.out.println("Not generated: " + report.getNotGenerated());  
  }

  private WebResource generateGalleryDepiction(UserSet userSet){

    try {
      return getUserSetService().generateDepiction(userSet);
    } catch (SearchApiClientException e) {
      //work with best user effort
      System.out.println("Cannot generate depiciton for set: "+ userSet.getIdentifier() + ", " + e.getMessage());
      //e.printStackTrace();
      return null;
    }
  }
}
