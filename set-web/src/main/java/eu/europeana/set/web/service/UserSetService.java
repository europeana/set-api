package eu.europeana.set.web.service;

import com.fasterxml.jackson.core.JsonParseException;

import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.exception.ParamValidationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;

public interface UserSetService {

	/**
	 * This method stores UserSet object in database and in Solr.
	 * @param UserSet
	 * @return UserSet object
	 */
	public UserSet storeUserSet(UserSet UserSet);

	/**
	 * update (stored) <code>persistentUserSet</code> with values from <code>webUserSet</code>
	 * @param persistentUserSet
	 * @param webUserSet
	 * @return
	 */
	public UserSet updateUserSet(PersistentUserSet persistentUserSet, UserSet webUserSet);
	
	/**
	 * This method updates user set pagination values. 
	 * @param newUserSet
	 * @return user set with updated pagination values
	 */
	//TODO: EA-1148 if this method is only updating the given userset, it should return void
	public UserSet updateUserSetPagination(UserSet newUserSet);		
	
	/**
	 * This method returns UserSet object for given UserSetId that
	 * comprises provider and identifier.
	 * @param
	 * @return UserSet object
	 */
	public UserSet getUserSetById(String userSetId) throws UserSetNotFoundException; 
		
	/**
	 * This methods converts user set object from JsonLd string format to a UserSet object
	 * @param userSetJsonLdStr
	 * @return a UserSet object
	 * @throws JsonParseException
	 * @throws HttpException
	 */
	public UserSet parseUserSetLd(String userSetJsonLdStr) throws JsonParseException, HttpException;

	/**
	 * This method validates and processes the Set description for format and mandatory fields
     * if false responds with HTTP 400
	 * @param webUserSet
	 * @throws ParamValidationException
	 */
	public void validateWebUserSet(UserSet webUserSet) throws ParamValidationException;
	
	/**
	 * This method deletes user set by user set Id value.
	 * @param userSetId The id of the user set
	 * @throws UserSetNotFoundException 
	 */
	public void deleteUserSet(String userSetId) throws UserSetNotFoundException;
		
}
