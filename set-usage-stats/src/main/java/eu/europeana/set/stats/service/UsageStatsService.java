package eu.europeana.set.stats.service;

import eu.europeana.api.commons.definitions.statistics.set.SetMetric;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.search.UserSetQueryImpl;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;
import eu.europeana.set.mongo.service.PersistentUserSetService;

import javax.annotation.Resource;

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
     * @param metric
     */
    public void getPublicPrivateSetsCount(SetMetric metric) {
      long publicSetsCount = getMongoPersistance().count(buildUserSetQuery(
                 null,
                        UserSetTypes.COLLECTION.getJsonValue(),
                        VisibilityTypes.PUBLIC.getJsonValue()));

      long privateSetsCount = getMongoPersistance().count(buildUserSetQuery(
                 null,
                        UserSetTypes.COLLECTION.getJsonValue(),
                        VisibilityTypes.PRIVATE.getJsonValue()));

      metric.setNoOfPublicSets(publicSetsCount);
      metric.setNoOfPrivateSets(privateSetsCount);
    }

    /**
     * Gets the total item liked which is the total number
     * of item in all BookmarkFolders
     *
     * @param metric
     */
    public void getTotalItemsLiked(SetMetric metric) {
      metric.setNoOfItemsLiked(getMongoPersistance().countTotalLikes());
    }

    /**
     * Gets the average user sets per user.
     * Also sets the NumberOfUsersWithGallery in the metric
     * @param metric
     */
    public void getAverageSetsPerUser(SetMetric metric) {
      long averageUserSetsPerUser = 0;
      long distinctUsers = getMongoPersistance().getDistinctCreators(UserSetTypes.COLLECTION.getJsonValue());
      // set the number of user with gallery here.
      metric.setNumberOfUsersWithGallery(distinctUsers);
      long totalUserSets =  getMongoPersistance().count(
                buildUserSetQuery(null,
                        UserSetTypes.COLLECTION.getJsonValue(),
                        null));
      if (distinctUsers != 0 && totalUserSets != 0) {
          averageUserSetsPerUser = totalUserSets / distinctUsers;
      }
      metric.setAverageSetsPerUser(averageUserSetsPerUser);
    }

    public void getNumberOfUsersWithLike(SetMetric metric){
        metric.setNumberOfUsersWithLike(getMongoPersistance().getDistinctCreators(UserSetTypes.BOOKMARKSFOLDER.getJsonValue()));
        metric.setNumberOfUsersWithLikeOrGallery(getMongoPersistance().getDistinctCreators(null));

    }
    /**
     * Build the user set query
     * admin is set to 'true' to get all the results including private sets
     * @param creator
     * @param type
     * @param visibility
     * @return UsersSetQuery
     */
    public UserSetQuery buildUserSetQuery(String creator, String type, String visibility) {
        UserSetQuery userSetQuery = new UserSetQueryImpl();
        userSetQuery.setCreator(creator);
        userSetQuery.setType(type);
        userSetQuery.setVisibility(visibility);
        userSetQuery.setAdmin(true);

        return userSetQuery;
    }
}
