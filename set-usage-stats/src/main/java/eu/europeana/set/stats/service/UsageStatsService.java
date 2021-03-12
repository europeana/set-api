package eu.europeana.set.stats.service;

import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.search.UserSetQueryImpl;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.mongo.service.PersistentUserSetService;
import eu.europeana.set.stats.model.MetricData;
import eu.europeana.set.stats.vocabulary.UsageStatsFields;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Service
public class UsageStatsService {

    @Resource
    PersistentUserSetService mongoPersistance;

    public PersistentUserSetService getMongoPersistance() {
        return mongoPersistance;
    }

    /**
     * Gets the count of total public and private Sets for type Collection
     * This data is excluding BookmarkFolder and EntityBestItemsSet sets
     *
     * @param metricData
     */
    public void getPublicPrivateSetsCount(MetricData metricData) {
      long publicSetsCount = getMongoPersistance().find(buildUserSetQuery(
                 null,
                        UserSetTypes.COLLECTION.getJsonValue(),
                        VisibilityTypes.PUBLIC.getJsonValue())).getResultSize();

      long privateSetsCount = getMongoPersistance().find(buildUserSetQuery(
                 null,
                        UserSetTypes.COLLECTION.getJsonValue(),
                        VisibilityTypes.PRIVATE.getJsonValue())).getResultSize();

      metricData.setNoOfPublicSets(publicSetsCount);
      metricData.setNoOfPrivateSets(privateSetsCount);
    }

    /**
     * Gets the total item liked which is the total number
     * of item in all BookmarkFolders
     *
     * @param metricData
     */
    public void getTotalItemsLiked(MetricData metricData) {
      long totalItemsLiked = 0;
      ResultSet<PersistentUserSet> res = getMongoPersistance().find(buildUserSetQuery(null,
             UserSetTypes.BOOKMARKSFOLDER.getJsonValue(),null));

      for(PersistentUserSet userSet : res.getResults()) {
       totalItemsLiked +=userSet.getItems().size();
      }
      metricData.setNoOfItemsLiked(totalItemsLiked);
    }

    /**
     * Gets the average user sets per user
     * @param metricData
     */
    public void getAverageSetsPerUser(MetricData metricData) {
      long averageUserSetsPerUser = 0;
      long distinctUsers = getMongoPersistance().getDistinctCreators().size();
      long totalUserSets =  getMongoPersistance().find(
                buildUserSetQuery(null,
                        UserSetTypes.COLLECTION.getJsonValue(),
                        null)).getResultSize();

      if(distinctUsers != 0 && totalUserSets != 0) {
          averageUserSetsPerUser = totalUserSets / distinctUsers;
      }
      metricData.setAverageSetsPerUser(averageUserSetsPerUser);
    }

    /**
     * Returns the current date time in ISO format
     * @return
     */
    public String getCurrentISODate() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat(UsageStatsFields.DATE_FORMAT);
        df.setTimeZone(tz);
        return df.format(new Date());
    }

    /**
     * Build the user set query
     * admin is set to 'true' to get all the results including private sets
     * @param creator
     * @param type
     * @param visibility
     * @return UsersSetQuery
     */
    private UserSetQuery buildUserSetQuery(String creator, String type, String visibility) {
        UserSetQuery userSetQuery = new UserSetQueryImpl();
        userSetQuery.setCreator(creator);
        userSetQuery.setType(type);
        userSetQuery.setVisibility(visibility);
        userSetQuery.setAdmin(true);

        return userSetQuery;
    }
}
