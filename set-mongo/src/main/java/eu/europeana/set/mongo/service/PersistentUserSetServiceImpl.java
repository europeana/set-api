package eu.europeana.set.mongo.service;

import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;
import org.springframework.stereotype.Component;

import eu.europeana.api.commons.nosql.service.impl.AbstractNoSqlServiceImpl;
import eu.europeana.set.config.UserSetConfiguration;
import eu.europeana.set.definitions.exception.UserSetValidationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.UserSetId;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
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

	protected final Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private UserSetConfiguration configuration;

	public UserSetConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(UserSetConfiguration configuration) {
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

		// check creator
		if (object.getCreator() == null)
			throw new UserSetValidationException(UserSetValidationException.ERROR_NULL_CREATOR);

		long sequenceId = generateUserSetId(WebUserSetFields.USER_SET_PROVIDER); //initializeUserSetId(object);
		object.setIdentifier(getConfiguration().getUserSetBaseUrl() + sequenceId);

		// validate user set ID
		if (StringUtils.isBlank(object.getIdentifier()) 
				|| UserSetId.NOT_INITIALIZED_LONG_ID.equals(object.getIdentifier()))
				throw new UserSetValidationException("UserSet.UserSetId.identifier must be a valid alpha-numeric value or a positive number!");
	}

	/* (non-Javadoc)
	 * @see eu.europeana.set.mongo.service.PersistentUserSetService#getByIdentifier(java.lang.String)
	 */
	public PersistentUserSet getByIdentifier(String identifier) {
		Query<PersistentUserSet> query = getUserSetDao().createQuery();
		query.filter(PersistentUserSet.FIELD_IDENTIFIER, identifier);

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
		validatePersistentUserSet((PersistentUserSet) userSet);
		return this.store((PersistentUserSetImpl) userSet);
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
	public void remove(String id) {
		PersistentUserSet userSet = findByID(id);
		getDao().delete(userSet);
	}

	@Override
	public PersistentUserSet update(PersistentUserSet userSet) throws UserSetValidationException {
		return store(userSet);
	}

}
