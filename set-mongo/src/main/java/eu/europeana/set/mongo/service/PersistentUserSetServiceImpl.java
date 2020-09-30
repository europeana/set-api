package eu.europeana.set.mongo.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Criteria;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.Sort;
import org.springframework.stereotype.Component;

import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.api.commons.nosql.service.impl.AbstractNoSqlServiceImpl;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.exception.UserSetValidationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.mongo.dao.PersistentUserSetDao;
import eu.europeana.set.mongo.model.PersistentUserSetImpl;
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
	
	List<String> publicPublishedList = Arrays.asList(new String[]{
		VisibilityTypes.PUBLIC.getJsonValue(), VisibilityTypes.PUBLISHED.getJsonValue()});  
	
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
		
		String NOT_INITIALIZED_LONG_ID = "-1";
		
		// validate user set ID
		if (StringUtils.isBlank(object.getIdentifier()) 
				|| NOT_INITIALIZED_LONG_ID.equals(object.getIdentifier()))
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
	 * @see eu.europeana.set.mongo.service.PersistentUserSetService#getBookmarksFolder(java.lang.String)
	 */
	public PersistentUserSet getBookmarksFolder(String creatorId) {
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
		return getDao().findOne(WebUserSetFields.MONGO_ID, new ObjectId(id));
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
		//all public, published, and user's private
//		searchQuery.filter(WebUserSetModelFields.VISIBILITY+" in", publicPublishedList);
		
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
	        
	    if(query.getType() == null) {
		mongoQuery.filter(WebUserSetModelFields.TYPE, UserSetTypes.COLLECTION.getJsonValue());
	    }else {
		mongoQuery.filter(WebUserSetModelFields.TYPE, query.getType());
	    }
	    
	    if(query.getCreator() != null) {
		mongoQuery.filter("creator.httpUrl", query.getCreator());
	    }
	    
	    if(query.getItem() != null) {
		mongoQuery.filter(WebUserSetModelFields.ITEMS + " in", query.getItem());
	    }

		if(query.getSetId() != null) {
			mongoQuery.filter(WebUserSetModelFields.IDENTIFIER + " in",   query.getSetId());
		}
	    
	    if(query.getSortCriteria() == null) {
		//default ordering if none is defined by the user
		mongoQuery.order(Sort.descending(WebUserSetModelFields.MODIFIED));
	    } else {
		buildSortCriteria(query, mongoQuery);
	    }

	    System.out.println("MONGO QUERY " +mongoQuery);
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

	/**      
	 * @deprecated     
	 */
	@Override
	@Deprecated
	//TODO: use store instead
	public PersistentUserSet update(PersistentUserSet userSet) throws  UserSetValidationException {
		return store(userSet);
	}

}
