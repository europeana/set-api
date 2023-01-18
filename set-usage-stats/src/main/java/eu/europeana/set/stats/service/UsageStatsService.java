package eu.europeana.set.stats.service;

import javax.annotation.Resource;
import eu.europeana.api.commons.definitions.statistics.set.SetMetric;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.exception.UserSetServiceException;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.search.UserSetQueryImpl;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;
import eu.europeana.set.mongo.model.UserSetMongoConstants;
import eu.europeana.set.mongo.service.PersistentUserSetService;

public class UsageStatsService {

    @Resource(name = UserSetConfiguration.BEAN_SET_PERSITENCE_SERVICE)
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
     * @throws UserSetServiceException 
     */
    public void getAverageSetsPerUser(SetMetric metric) throws UserSetServiceException {
      long averageUserSetsPerUser = 0;
      long distinctUsers = getMongoPersistance().getDistinct(UserSetMongoConstants.MONGO_CREATOR_URL, false, UserSetTypes.COLLECTION.getJsonValue());
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

    public void getNumberOfUsersWithLike(SetMetric metric) throws UserSetServiceException{
        metric.setNumberOfUsersWithLike(getMongoPersistance().getDistinct(UserSetMongoConstants.MONGO_CREATOR_URL, false, UserSetTypes.BOOKMARKSFOLDER.getJsonValue()));
        metric.setNumberOfUsersWithLikeOrGallery(getMongoPersistance().getDistinct(UserSetMongoConstants.MONGO_CREATOR_URL, false, null));

    }
    
    public void getNumberOfEntitySets(SetMetric metric) {
      long numEntitySets =  getMongoPersistance().count(
          buildUserSetQuery(null, UserSetTypes.ENTITYBESTITEMSSET.getJsonValue(), null));
      metric.setNumberOfEntitySets(numEntitySets);
    }
    
    public void getNumberOfItemsInEntitySets(SetMetric metric) throws UserSetServiceException {
      long numItemsInEntitySets = getMongoPersistance().countItemsInEntitySets();
      metric.setNumberOfItemsInEntitySets(numItemsInEntitySets);
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
