package eu.europeana.set.mongo.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Criteria;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryResults;
import org.mongodb.morphia.query.Sort;
import org.springframework.stereotype.Component;

import com.mongodb.AggregationOptions;
import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DBObject;

import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.api.commons.nosql.service.impl.AbstractNoSqlServiceImpl;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.exception.UserSetValidationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.search.UserSetFacetQuery;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.mongo.dao.PersistentUserSetDao;
import eu.europeana.set.mongo.model.UserSetMongoConstants;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;

/**
 * A service for persistence operation of user sets.
 * @author GrafR
 *
 */
@Component
public class PersistentUserSetServiceImpl extends AbstractNoSqlServiceImpl<PersistentUserSet, String>
		implements PersistentUserSetService {

	protected final Logger logger = LogManager.getLogger(this.getClass());

	private static final String NOT_PERSISTENT_OBJECT = "User set object in not an instance of persistent user set.";
	private static final String FIELD_IDENTIFIER = WebUserSetModelFields.IDENTIFIER;
	private static final String FIELD_TYPE       = WebUserSetModelFields.TYPE;
	private static final String FIELD_CREATOR    = WebUserSetModelFields.CREATOR + ".httpUrl";
	
	List<String> publicPublishedList = Arrays.asList(VisibilityTypes.PUBLIC.getJsonValue(), VisibilityTypes.PUBLISHED.getJsonValue());
	
	@Resource
	private UserSetConfiguration configuration;

	public synchronized UserSetConfiguration getConfiguration() {
		return configuration;
	}

	public synchronized void setConfiguration(UserSetConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * This method validates persistent user set and generates ID if
	 * genarateId=true.
	 * 
	 * @param object
	 */
	private void validatePersistentUserSet(PersistentUserSet object) {

		if (object.getCreated() == null) {
			Date now = new Date();
			object.setCreated(now);
		}

		if (object.getModified() == null) {
			Date now = new Date();
			object.setModified(now);
		}

		// check creator
		if (object.getCreator() == null)
			throw new UserSetValidationException(UserSetValidationException.ERROR_NULL_CREATOR);

		long sequenceId = generateUserSetId(WebUserSetFields.USER_SET_PROVIDER); 
		object.setIdentifier("" + sequenceId);
		
		String notInitializedLongId = "-1";
		
		// validate user set ID
		if (StringUtils.isBlank(object.getIdentifier()) 
				|| notInitializedLongId.equals(object.getIdentifier()))
				throw new UserSetValidationException("UserSet.UserSetId.identifier must be a valid alpha-numeric value or a positive number!");
	}

	/* (non-Javadoc)
	 * @see eu.europeana.set.mongo.service.PersistentUserSetService#getByIdentifier(java.lang.String)
	 */
	public PersistentUserSet getByIdentifier(String identifier) {
		Query<PersistentUserSet> query = getUserSetDao().createQuery();
		query.filter(FIELD_IDENTIFIER, identifier);

		return getUserSetDao().findOne(query);
	}

	/* (non-Javadoc)
	 * @see eu.europeana.set.mongo.service.PersistentUserSetService#getByCreator(java.lang.String)
	 */
	public QueryResults<PersistentUserSet> getByCreator(String creatorId) {
		Query<PersistentUserSet> query = getUserSetDao().createQuery().disableValidation();
		query.filter(FIELD_CREATOR, creatorId);

		return getUserSetDao().find(query);
	}

	/* (non-Javadoc)
	 * @see eu.europeana.set.mongo.service.PersistentUserSetService#getBookmarksFolder(java.lang.String)
	 */
	public PersistentUserSet getBookmarkFolder(String creatorId) {
	    Query<PersistentUserSet> query = getUserSetDao().createQuery().disableValidation();
	    query.filter(FIELD_TYPE, UserSetTypes.BOOKMARKSFOLDER.getJsonValue());
	    query.filter(FIELD_CREATOR, creatorId);

	    return getUserSetDao().findOne(query);
	}
	
	/**
	 * Generate next sequence number for user set identifier in database
	 * @param collection The collection e.g. "userset"
	 * @return next sequence number
	 */
	public long generateUserSetId(String collection) {
		return getUserSetDao().generateNextUserSetId(collection);
	}

	@Override
	public UserSet store(UserSet userSet) {

	    PersistentUserSet persistentObject = null;

	    if (userSet instanceof PersistentUserSet) {
		persistentObject = (PersistentUserSet) userSet;
	    } else {
		throw new IllegalArgumentException(NOT_PERSISTENT_OBJECT);
	    }

	    validatePersistentUserSet(persistentObject);
	    return this.store(persistentObject);
	}

	protected PersistentUserSetDao<PersistentUserSet, String> getUserSetDao() {
		return (PersistentUserSetDao<PersistentUserSet, String>) getDao();
	}

	/* (non-Javadoc)
	 * @see eu.europeana.api.commons.nosql.service.impl.AbstractNoSqlServiceImpl#findByID(java.io.Serializable)
	 */
	@Override
	public PersistentUserSet findByID(String id) {
		return getDao().findOne(UserSetMongoConstants.MONGO_ID, new ObjectId(id));
	}

	@Override
	public long getDistinctCreators() {
		// create query : { type: { $eq: Collection } }
		DBObject match = new BasicDBObject(WebUserSetFields.TYPE,
				new BasicDBObject(UserSetMongoConstants.MONGO_EQUALS, UserSetTypes.COLLECTION.getJsonValue()));
		return getDao().getCollection().distinct(WebUserSetFields.CREATOR, match).size();
	}

	@Override
	public long count(UserSetQuery query) {
		Query<PersistentUserSet> mongoQuery = buildMongoQuery(query);
		return mongoQuery.count();
	}

	/**
	 * Returns a Map<String, Long> with the requested facets
	 * Aggregate Query :
	 * db.aggregate ([{"$facet":{
	 *         <outputField1>: [ <stage1>, <stage2>, ... ] } } ])
	 *
	 * @param facetQuery
	 * @return Map<String, Long>
	 */
	@Override
	public Map<String, Long> getFacets(UserSetFacetQuery facetQuery) {
		Map<String, Long> valueCountMap = new LinkedHashMap<>();
		// Cursor is needed in aggregate command
		AggregationOptions aggregationOptions = AggregationOptions.builder().outputMode(AggregationOptions.OutputMode.CURSOR).build();
		Cursor cursor =getDao().getCollection().aggregate(getFacetPipeline(facetQuery) , aggregationOptions);
		if (cursor != null) {
			while(cursor.hasNext()) {
				DBObject object = cursor.next();
				List<DBObject> facet = (List<DBObject>) object.get(facetQuery.getOutputField());
				for (DBObject o: facet) {
					valueCountMap.put(String.valueOf(o.get(UserSetMongoConstants.MONGO_ID)), Long.parseLong(String.valueOf(o.get(UserSetMongoConstants.MONGO_COUNT))));
				}
			}
		}
		return valueCountMap;
	}

	/**
	 * create a facet pipeline
	 * { $facet:
	 *    {<outputField1>: [ <stage1>, <stage2>, ... ]}
	 *
	 * @param facetQuery
	 * @return
	 */
	private List<DBObject> getFacetPipeline(UserSetFacetQuery facetQuery) {
		return Arrays.asList(new BasicDBObject(UserSetMongoConstants.MONGO_FACET, getOutputFieldAndStages(facetQuery)));
	}

	/**
	 * creates facets output field and stages :  <outputField>: [ <stage1>, <stage2>, ... ],
	 * example : "mostLikedItems": [
	 *   {$match : {type : 'BookmarkFolder'}},
	 *   { $unwind : "$items"},
	 *   {$sortByCount : "$items"},
	 *   {$limit : <topLikedItems> }]
	 *
	 * NOTE : we are currently not supporting multiple outputFields for faceting
	 *
	 * @param facetQuery
	 * @return
	 */
	private DBObject getOutputFieldAndStages(UserSetFacetQuery facetQuery) {
		List<DBObject> outputFieldAndStages = new ArrayList<>();
		if (facetQuery.getMatchField() != null && facetQuery.getMatchValue() != null) {
			outputFieldAndStages.add(new BasicDBObject(UserSetMongoConstants.MONGO_MATCH,
					new BasicDBObject(facetQuery.getMatchField(), facetQuery.getMatchValue())));
		}
		if (facetQuery.isUnwind()) {
			outputFieldAndStages.add(new BasicDBObject(UserSetMongoConstants.MONGO_UNWIND, facetQuery.getFacet()));
		}
		outputFieldAndStages.add(new BasicDBObject(UserSetMongoConstants.MONGO_SORTBYCOUNT, facetQuery.getFacet()));
		outputFieldAndStages.add(new BasicDBObject(UserSetMongoConstants.MONGO_LIMIT, facetQuery.getFacetLimit()));
		return new BasicDBObject(facetQuery.getOutputField(), outputFieldAndStages);
	}

	/**
	 *  creates a mongo query to count the total item present in BookmarkFolder
	 *  Mongo Query : db.getCollection('userset').aggregate([
	 *  {$match:{"type":"BookmarkFolder"}},{$group: {_id:null, totalLikes: {$sum: "$total"}}}
	 *  ])
	 * @return
	 */
	@Override
	public long countTotalLikes() {
		// Cursor is needed in aggregate command
		AggregationOptions aggregationOptions = AggregationOptions.builder().outputMode(AggregationOptions.OutputMode.CURSOR).build();

		long totalLikes =0;
		Cursor cursor =getDao().getCollection().aggregate(getAggregatePipeline(), aggregationOptions);
		if (cursor != null) {
			while(cursor.hasNext()) {
				DBObject object = cursor.next();
				totalLikes += Long.parseLong(String.valueOf(object.get(UserSetMongoConstants.MONGO_TOTAL_LIKES)));
			}
		}
		return totalLikes;
		}

	// create $match and $group for mongo query
	private List<DBObject> getAggregatePipeline() {

		DBObject match = new BasicDBObject(UserSetMongoConstants.MONGO_MATCH,
				new BasicDBObject(WebUserSetFields.TYPE, UserSetTypes.BOOKMARKSFOLDER.getJsonValue()));

		DBObject groupFields = new BasicDBObject(UserSetMongoConstants.MONGO_ID, null);
		groupFields.put(UserSetMongoConstants.MONGO_TOTAL_LIKES, new BasicDBObject(UserSetMongoConstants.MONGO_SUM, UserSetMongoConstants.MONGO_TOTAL));
		DBObject group = new BasicDBObject(UserSetMongoConstants.MONGO_GROUP, groupFields);
		return Arrays.asList(match, group);
	}

	/**
	 * creates a mongo query to get the items and entity reference for the entity sets
	 *
	 * @return
	 */
	@Override
	public List<PersistentUserSet> getEntitySetsItemAndSubject(UserSetQuery userSetQuery) {
		return buildMongoQuery(userSetQuery)
				.project(WebUserSetFields.ITEMS, true)
				.project(WebUserSetFields.TYPE, true)
				.project(WebUserSetModelFields.SUBJECT, true).asList();
	}

	@Override
	public ResultSet<PersistentUserSet> find(UserSetQuery query) {
	    Query<PersistentUserSet> mongoQuery = buildMongoQuery(query);
	    long totalInCollection = mongoQuery.count();

	    FindOptions options = buildMongoPaginationOptions(query);
	    List<PersistentUserSet> userSets = mongoQuery.asList(options);
	    ResultSet<PersistentUserSet> res = new ResultSet<>();
	    res.setResults(userSets);
	    res.setResultSize(totalInCollection);
	    return res;
	}

	private FindOptions buildMongoPaginationOptions(UserSetQuery query) {
	    FindOptions options = new FindOptions();
	    options.skip(query.getPageNr() * query.getPageSize());
	    options.limit(query.getPageSize());
	    return options;
	}

	private Query<PersistentUserSet> buildMongoQuery(UserSetQuery query) {
	    
	    Query<PersistentUserSet> searchQuery = buildUserConditionsQuery(query);
	    if (query.isAdmin()) {
		//admin can see all
		return searchQuery;
	    }
	    
//	    build the equivalent of (((visibility=private AND (creator=token OR user=admin)) OR visibility=public OR visibility=published) AND (other conditions)
	    if(query.getVisibility() == null) {
		//all public, published, and user's own sets (including private)	
		Criteria publicCriterion= searchQuery.criteria(WebUserSetModelFields.VISIBILITY).in(publicPublishedList);
		Criteria ownerCriterion= searchQuery.criteria(FIELD_CREATOR).equal(query.getUser());
		searchQuery.and(
			searchQuery.or(publicCriterion, ownerCriterion));
		
	    } else if (VisibilityTypes.PRIVATE.getJsonValue().equals(query.getVisibility())) {
		//private only, user can see only his private sets
		searchQuery.filter(FIELD_CREATOR, query.getUser());
	    } 
	    
	    return searchQuery;
	}

	private Query<PersistentUserSet> buildUserConditionsQuery(UserSetQuery query) {
	    Query<PersistentUserSet> mongoQuery = getUserSetDao().createQuery();
	    mongoQuery.disableValidation();
	    
	    if(query.getVisibility() != null) {
		mongoQuery.filter(WebUserSetModelFields.VISIBILITY, query.getVisibility());
	    }
	        
	    if(query.getType() != null) {
		mongoQuery.filter(WebUserSetModelFields.TYPE, query.getType());
	    }
	    
	    if(query.getCreator() != null) {
		mongoQuery.filter(WebUserSetModelFields.CREATOR + ".httpUrl", query.getCreator());
	    }
	    
	    if(query.getContributor() != null) {
		mongoQuery.filter(WebUserSetModelFields.CONTRIBUTOR + " in", query.getContributor());
	    }
	    
	    if(query.getSubject() != null) {
		mongoQuery.filter(WebUserSetModelFields.SUBJECT + " in", query.getSubject());
	    }
	    
	    if(query.getItem() != null) {
		mongoQuery.filter(WebUserSetModelFields.ITEMS + " in", query.getItem());
	    }

	    if (query.getSetId() != null) {
		mongoQuery.filter(WebUserSetModelFields.IDENTIFIER, query.getSetId());
	    }
	    
	    if (query.getText() != null) {
		mongoQuery.search(query.getText());
	    }
	    

	    if(query.getSortCriteria() == null) {
		//default ordering if none is defined by the user
		mongoQuery.order(Sort.descending(WebUserSetModelFields.MODIFIED));
	    } else {
		buildSortCriteria(query, mongoQuery);
	    }

	    return mongoQuery;
	}

	private void buildSortCriteria(UserSetQuery query, Query<PersistentUserSet> mongoQuery) {
	    for (String sortField : query.getSortCriteria()) {
	        if(!sortField.contains(" ")) {
	    	mongoQuery.order(Sort.ascending(sortField));
	        }else {
	    	String[] sortParts = sortField.split(" ", 2);
	    	if(!"desc".contentEquals(sortParts[1])) {
	    	    mongoQuery.order(Sort.ascending(sortParts[0]));
	    	}else {
	    	    mongoQuery.order(Sort.descending(sortParts[0]));
	    	}
	    	
	        }
	    }
	}

	@Override
	public void remove(String id) {
		PersistentUserSet userSet = getByIdentifier(id);
		if (userSet != null)
		    getDao().delete(userSet);
	}

	@Override
	public void removeAll(List<PersistentUserSet> userSets) {
		List<ObjectId> objectIds = new ArrayList<>();
		if (!userSets.isEmpty()) {
			for( PersistentUserSet userSet : userSets) {
				objectIds.add(userSet.getObjectId());
			}
		}
		getUserSetDao().deleteByObjectId(objectIds);
	}

	/**      
	 * @deprecated     
	 */
	@Override
	@Deprecated(since = "", forRemoval = true)
	//TODO: use store instead
	public PersistentUserSet update(PersistentUserSet userSet) throws  UserSetValidationException {
		return store(userSet);
	}
	
	/**
	 * Getting the ids of the duplicate sets.
	 */
	public List<String> getDuplicateUserSetsIds(UserSet userSet) {
	  
	     if(userSet.getSubject()==null || userSet.getSubject().size()==0) return null;
	       
	     Query<PersistentUserSet> query = getUserSetDao().createQuery().disableValidation();
	     query.filter(FIELD_TYPE, UserSetTypes.ENTITYBESTITEMSSET.toString());     
	     query.filter(WebUserSetModelFields.SUBJECT + " size", userSet.getSubject().size());
         query.filter(WebUserSetModelFields.SUBJECT + " all", userSet.getSubject());
	     query.project(WebUserSetModelFields.IDENTIFIER, true);
	     List<PersistentUserSet> resultMongo = getUserSetDao().find(query).asList();
	     List<String> result =  null;
	     if (resultMongo!=null && resultMongo.size()>0) {
	       result = new ArrayList<String>();
	       for (PersistentUserSet set : resultMongo) {
	           result.add(set.getIdentifier());
	       }
	     }
	     return result;
	}

}
