package eu.europeana.set.mongo.service;

import java.util.List;
import java.util.Map;
import org.mongodb.morphia.query.QueryResults;
import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.api.commons.nosql.service.AbstractNoSqlService;
import eu.europeana.set.definitions.exception.UserSetServiceException;
import eu.europeana.set.definitions.exception.UserSetValidationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.search.UserSetFacetQuery;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;

public interface PersistentUserSetService extends AbstractNoSqlService<PersistentUserSet, String>{

	/**
	 * This method stores user set in a database
	 * @param object
	 * @return
	 * @throws UserSetValidationException
	 */
	UserSet store(UserSet object) throws UserSetValidationException;
		
	/**
	 * This method retrieves user set from database by set identifier string
	 * @param identifier The set identifier e.g. http://localhost:8080/set6
	 * @return user set object
	 */
	PersistentUserSet getByIdentifier(String identifier);


	/**
	 * This method retrieves user sets from database by creatorId string
	 * @param creatorId The creator id  e.g. http://localhost:8080/user/12345
	 * @return QueryResults of user set object
	 */
	QueryResults<PersistentUserSet> getByCreator(String creatorId);

	/**
	 * This method checks if a user set with provided type and creator already exists in
	 * database
	 * @param creatorid
	 * @return user set object
	 */
	PersistentUserSet getBookmarkFolder(String creatorid);
	
	/** 
	 * This method retrieves user set from database by database Id provided as a string
	 * @param identifier The database object ID e.g. "15"
	 * @return user set object
	 */
	PersistentUserSet findByID(String identifier);
	
	/**
	 * This method performs update for the passed user set object
	 * @param userSet
	 */
	PersistentUserSet update(PersistentUserSet userSet) throws UserSetValidationException;

	/** 
	 * This method removes user set from database by database Id provided as a string
	 * @param identifier The database object ID e.g. "15"
	 * @return user set object
	 */
	void remove(String identifier);

	/**
	 * This method removes List of user sets from database
	 * @param userSets
	 * @return user set object
	 */
	void removeAll(List<PersistentUserSet> userSets);

	/** 
	 * Retrieve user sets for the given search query 
	 * @param searchQuery
	 * @return
	 */
	ResultSet<PersistentUserSet> find(UserSetQuery searchQuery);

	/**
	 * Retrieve distinct objects present in the DB.
	 * @return
	 * @throws UserSetServiceException 
	 */
	public long getDistinct(String field, boolean fieldIsArray, String collectionType) throws UserSetServiceException;

	/**
	 * Retrieve distinct creators present in the DB
	 * @return
	 */
	long count(UserSetQuery searchQuery);

	/**
	 *  creates a mongo query to count the total item present in BookmarkFolder
	 * @return
	 */
	long countTotalLikes();


	/**
	 *  creates a mongo query to get the items and entity reference for the entity sets
	 * @return
	 */
	List<PersistentUserSet> getEntitySetsItemAndSubject(UserSetQuery query);


	/**
	 *  gets facets results from a facets query
	 * @return
	 */
	Map<String, Long> getFacets(UserSetFacetQuery facetQuery);

	/**
	 * verifies if the given user set is a duplicate of an existing set. Applicable only for BestItemsUserSet
	 * @param userSet the set to verify for duplicates
	 * @return list of existing duplicates
	 */
	List<String> getDuplicateUserSetsIds(UserSet userSet);

}

