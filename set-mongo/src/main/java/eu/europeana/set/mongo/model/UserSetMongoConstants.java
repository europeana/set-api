package eu.europeana.set.mongo.model;

public abstract class UserSetMongoConstants {

    // Mongo Constants
    public static final String MONGO_ID                   = "_id";
    public static final String MONGO_MATCH                = "$match";
    public static final String MONGO_GROUP                = "$group";
    public static final String MONGO_TOTAL_LIKES          = "totalLikes";
    public static final String MONGO_SUM                  = "$sum";
    public static final String MONGO_TOTAL                = "$total";
    public static final String MONGO_EQUALS               = "$eq";
    public static final String MONGO_PROJECT              = "$project";
    public static final String MONGO_ITEMS                = "$items";
    public static final String MONGO_UNWIND               = "$unwind";
    public static final String MONGO_SORTBYCOUNT          = "$sortByCount";
    public static final String MONGO_LIMIT                = "$limit";
    public static final String MONGO_FACET                = "$facet";
    public static final String MONGO_COUNT                = "count";
    public static final String MONGO_VISIBILITY           = "$visibility";
}
