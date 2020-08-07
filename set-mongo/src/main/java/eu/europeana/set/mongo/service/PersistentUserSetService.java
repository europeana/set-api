package eu.europeana.set.mongo.service;

import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.api.commons.nosql.service.AbstractNoSqlService;
import eu.europeana.set.definitions.exception.UserSetValidationException;
import eu.europeana.set.definitions.model.UserSet;
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
	 * This method checks if a user set with provided type and creator already exists in
	 * database
	 * @param creatorid
	 * @return user set object
	 */
	PersistentUserSet getBookmarksFolder(String creatorid);
	
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
	 * Retrieve user sets for the given search query 
	 * @param searchQuery
	 * @return
	 */
	ResultSet<PersistentUserSet> find(UserSetQuery searchQuery);
	
}

