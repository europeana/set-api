package eu.europeana.set.mongo.service;

import java.util.List;

import org.mongodb.morphia.query.Query;

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
	public abstract UserSet store(UserSet object) throws UserSetValidationException;
		
	/**
	 * This method retrieves user set from database by set identifier string
	 * @param identifier The set identifier e.g. http://localhost:8080/set6
	 * @return user set object
	 */
	public abstract PersistentUserSet getByIdentifier(String identifier);
	
	/**
	 * This method checks if a user set with provided type and creator already exists in
	 * database
	 * @param creatorid
	 * @return user set object
	 */
	public abstract PersistentUserSet getBookmarksFolder(String creatorid);
	
	/** 
	 * This method retrieves user set from database by database Id provided as a string
	 * @param identifier The database object ID e.g. "15"
	 * @return user set object
	 */
	public abstract PersistentUserSet findByID(String identifier);
	
	/**
	 * This method performs update for the passed user set object
	 * @param userSet
	 */
	public PersistentUserSet update(PersistentUserSet userSet) throws UserSetValidationException;

	/** 
	 * This method removes user set from database by database Id provided as a string
	 * @param identifier The database object ID e.g. "15"
	 * @return user set object
	 */
	public abstract void remove(String identifier);

	/** 
	 * Retrieve user sets for the given search query 
	 * @param searchQuery
	 * @return
	 */
	ResultSet<PersistentUserSet> find(UserSetQuery searchQuery);
	
}

