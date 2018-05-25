package eu.europeana.set.web.service;

import com.fasterxml.jackson.core.JsonParseException;

import eu.europeana.api.commons.web.exception.HttpException;
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
	 * This method stores UserSet object in database and in Solr if 'indexing' is true.
	 * @param UserSet
	 * @param indexing
	 * @return UserSet object
	 */
//	public UserSet storeUserSet(UserSet UserSet, boolean indexing);

	/**
	 * update (stored) <code>persistentUserSet</code> with values from <code>webUserSet</code>
	 * @param persistentUserSet
	 * @param webUserSet
	 * @return
	 */
	public UserSet updateUserSet(PersistentUserSet persistentUserSet, UserSet webUserSet);
	
	/**
	 * This method returns UserSet object for given UserSetId that
	 * comprises provider and identifier.
	 * @param
	 * @return UserSet object
	 */
	public UserSet getUserSetById(String userSetId) throws UserSetNotFoundException; //, UserAuthorizationException;
		
	/**
	 * Search for UserSets by the given text query.
	 * @param query
	 * @return
	 * @throws UserSetServiceException 
	 */
//	public List<? extends UserSet> searchUserSets(String query) throws UserSetServiceException;
	
	/**
	 * Search for UserSets by the given text query, row start position and rows limit. 	 
	 * @param query
	 * @param startOn
	 * @param limit
	 * @return
	 * @throws UserSetServiceException 
	 */
	//TODO: change parameters to integers
//	public List<? extends UserSet> searchUserSets(String query, String startOn, String limit) 
//			throws UserSetServiceException;
	
	/**
	 * Check whether UserSet for given provider and identifier already exist in database.
	 */
//	public boolean existsInDb(UserSetId annoId); 
	
	/**
	 * This methods converts user set object from JsonLd string format to a UserSet object
	 * @param userSetJsonLdStr
	 * @return a UserSet object
	 * @throws JsonParseException
	 * @throws HttpException
	 */
	public UserSet parseUserSetLd(String userSetJsonLdStr) throws JsonParseException, HttpException;
	
}
