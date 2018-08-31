package eu.europeana.set.web.service;

import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.web.exception.request.RequestBodyValidationException;
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
	 * This method disables user set in database
	 * @param existingUserSet
	 * @return disabled user set
	 */
	public UserSet disableUserSet(UserSet existingUserSet);					 
	
	/**
	 * This method updates user set pagination values. 
	 * @param newUserSet
	 * @return user set with updated pagination values
	 */
	public void updateUserSetPagination(UserSet newUserSet);		
	
	/**
	 * This method returns UserSet object for given user set identifier.
	 * @param
	 * @return UserSet object
	 */
	public UserSet getUserSetById(String userSetId) throws UserSetNotFoundException; 
		
	/**
	 * This method returns UserSet object for given user set identifier and filter fields. Additionally
	 * this method allows to define whether disabled user sets should be searched.
	 * Use false if disabled user sets should be returned.
	 * @param user set ID
	 * @param user set filter fields
	 * @return UserSet object
	 */
	public UserSet getUserSetByIdAndFilters(String userSetId, String[] filters) throws UserSetNotFoundException; 
		
	/**
	 * This method returns UserSet object for given user set identifier and filter fields. Additionally
	 * this method allows to define whether disabled user sets should be searched.
	 * Use false if disabled user sets should be returned.
	 * @param user set ID
	 * @param user set filter fields
	 * @param true if disabled user sets should be checked (default true)
	 * @return UserSet object
	 */
	public UserSet getUserSetByIdAndFilters(String userSetId, String[] filters, boolean checkDisabled) throws UserSetNotFoundException; 
		
	/**
	 * This method returns UserSet object for given user set identifier. Additionally
	 * this method allows to define whether disabled user sets should be searched.
	 * Use false if disabled user sets should be returned.
	 * @param user set ID
	 * @param true if disabled user sets should be checked (default true)
	 * @return UserSet object
	 */
	public UserSet getUserSetById(String userSetId, boolean checkDisabled) throws UserSetNotFoundException; 
		
	/**
	 * This method forms an identifier URL
	 * @param id The sequential ID
	 * @param base The base URL
	 * @return identifier URL
	 */
	public String buildIdentifierUrl(String id, String base);
	
    /**
     * This method computes pagination values for user set
     * @param userSet
     * @return enriched user set
     */
    public UserSet fillPagination(UserSet userSet);
	
    /**
     * This method computes pagination values for user set without update of identifier
     * @param userSet
     * @return enriched user set
     */
    public UserSet updatePagination(UserSet userSet);
	
	/**
	 * This methods converts user set object from JsonLd string format to a UserSet object
	 * @param userSetJsonLdStr
	 * @return a UserSet object
	 * @throws HttpException
	 */
	public UserSet parseUserSetLd(String userSetJsonLdStr) throws HttpException;

	/**
	 * This method validates and processes the Set description for format and mandatory fields
     * if false responds with HTTP 400
	 * @param webUserSet
	 * @throws RequestBodyValidationException 
	 */
	public void validateWebUserSet(UserSet webUserSet) throws RequestBodyValidationException;
	
	/**
	 * This method deletes user set by user set Id value.
	 * @param userSetId The id of the user set
	 * @throws UserSetNotFoundException 
	 */
	public void deleteUserSet(String userSetId) throws UserSetNotFoundException;
	
	/**
	 * remove duplicate items in the user set by preserving the order of items
	 * @param userSet
	 */
	public void removeItemDuplicates(UserSet userSet);
}
