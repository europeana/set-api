package eu.europeana.api.set.integration.migration;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeAll;
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
    //  registry.add("mongodb.set.connectionUrl", MONGO_CONTAINER::getConnectionUrl);
      registry.add("mongodb.set.connectionUrl",  () -> "mongodb://127.0.0.1:27017/set_test"); 
  }

  @Test
  public void generateGalleriesWithDepiction() throws Exception {
    createTestUserSet(USER_SET_REGULAR, regularUserToken);
    
    // create object in database
    UserSetQueryBuilder queryBuilder = new UserSetQueryBuilder();
    
    Authentication adminAuth = UserSetAuthorizationUtils.createAuthentication(adminUserToken);
    
    final int pageSize = 200;
    int page = Integer.valueOf(UserSetUtils.DEFAULT_PAGE);
    String sort = WebUserSetModelFields.CREATED + " asc";
    UserSetQuery searchQuery =
        queryBuilder.buildUserSetQuery("type:Collection", null, sort, page, pageSize, getConfiguration());
    final ArrayList<LdProfiles> profiles = new ArrayList<LdProfiles>();
    profiles.add(LdProfiles.STANDARD);
    
    ResultSet<? extends UserSet> results = null;
    do {
      results =
          getUserSetService().search(searchQuery, null, profiles, adminAuth);
      generateDepictions(results.getResults());
      
      //move to next page
      page++;
      searchQuery.setPageNr(page);
      
      //brake
      results = null;
    } while (results != null && hasNext(pageSize, results));
    
  }


  private void generateDepictions(List<? extends UserSet> results) {
    for (UserSet userSet : results) {
      final WebResource isShownBy = generateGalleryDepiction(userSet);
      //do not update set if the depiction cannot be generated
      if(isShownBy != null) {
        userSet.setIsShownBy(isShownBy);
        userSet.setCollectionType(WebUserSetFields.TYPE_GALLERY);
        mongoPersistance.store(userSet);
      }
    }
  }


  private WebResource generateGalleryDepiction(UserSet userSet){
    if(userSet.isOpenSet() || userSet.isBookmarksFolder() || userSet.isEntityBestItemsSet()) {
      //bookmarks and entity best items sets is redundant, but we keep it for future
      //open/dynamic sets must not be migrated
      return null;
    }
    
    try {
      return getUserSetService().generateDepiction(userSet);
    } catch (SearchApiClientException e) {
      //work with best user effort
      System.out.println(e.getMessage());
      e.printStackTrace();
      return null;
    }
  }


  


  private boolean hasNext(final int pageSize, ResultSet<? extends UserSet> results) {
    return results.getResultSize() < pageSize;
  }


}
