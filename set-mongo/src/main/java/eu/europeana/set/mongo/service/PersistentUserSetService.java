package eu.europeana.set.mongo.service;

import eu.europeana.api.commons.nosql.service.AbstractNoSqlService;
import eu.europeana.set.definitions.exception.UserSetValidationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;

public interface PersistentUserSetService extends AbstractNoSqlService<PersistentUserSet, String>{

	public abstract UserSet store(UserSet object) throws UserSetValidationException;
		
	/**
	 * This method retrieves user set from database by set identifier string
	 * @param identifier The set identifier e.g. http://localhost:8080/set6
	 * @return user set object
	 */
	public abstract PersistentUserSet getByIdentifier(String identifier);
	
	/** 
	 * This method retrieves user set from database by database ObjectId provided as a string
	 * @param objectId The database object ID e.g. ObjectId("5af1ccab5a398b254c93855a")
	 * @return user set object
	 */
	public abstract PersistentUserSet findByID(String objectId);
	
	/**
	 * This method performs update for the passed annotation object
	 * @param annotation
	 */
	public PersistentUserSet update(PersistentUserSet annotation) throws UserSetValidationException;

}

