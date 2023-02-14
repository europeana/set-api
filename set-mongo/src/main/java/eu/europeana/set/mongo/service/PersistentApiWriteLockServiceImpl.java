package eu.europeana.set.mongo.service;

import java.util.Date;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import eu.europeana.api.commons.nosql.service.impl.AbstractNoSqlServiceImpl;
import eu.europeana.set.definitions.exception.ApiWriteLockException;
import eu.europeana.set.mongo.model.PersistentApiWriteLockImpl;
import eu.europeana.set.mongo.model.internal.PersistentApiWriteLock;

@Configuration
@EnableCaching
//@Component
public class PersistentApiWriteLockServiceImpl extends
		AbstractNoSqlServiceImpl<PersistentApiWriteLock, String> implements	
		PersistentApiWriteLockService {

	@Override
	public PersistentApiWriteLock lock(String action) throws ApiWriteLockException {
		
		try {
			PersistentApiWriteLock pij = new PersistentApiWriteLockImpl(action);
			return super.store(pij);
		} catch(Exception e) {
			throw new ApiWriteLockException("Unable to set lock.", e);
		}
	}
	
	@Override 
	public PersistentApiWriteLock getLastActiveLock(String name) throws ApiWriteLockException {
		
		try {
			Query<PersistentApiWriteLock> query = getDao().createQuery();
			
			query.criteria("name").contains(name);
			query.criteria("ended").doesNotExist();
			query.order("-started");
			return getDao().findOne(query);
		} catch(Exception e) {
			throw new ApiWriteLockException("Unable to get last lock.", e);
		}
	}
	
	@Override 
	public PersistentApiWriteLock getLastActiveLock() throws ApiWriteLockException {
		
		try {
			Query<PersistentApiWriteLock> query = getDao().createQuery();
			query.criteria("ended").doesNotExist();
			query.order("-started");
			return getDao().findOne(query);
		} catch(Exception e) {
			throw new ApiWriteLockException("Unable to get last lock.", e);
		}
	}

	@Override
	public PersistentApiWriteLock getLockById(String id) throws ApiWriteLockException {
		try {
			Query<PersistentApiWriteLock> query = getDao().createQuery();
			query.criteria("_id").equal(new ObjectId(id));
			return getDao().findOne(query);
		} catch(Exception e) {
			throw new ApiWriteLockException("Unable to get lock by id.", e);
		}
	}

	@Override
	public void unlock(PersistentApiWriteLock pij) throws ApiWriteLockException {
		try {
			pij.setEnded(new Date());
			super.store(pij);
		} catch(Exception e) {
			throw new ApiWriteLockException("Unable to unlock api.", e);
		}
	}
	
	@Override
	public void deleteAllLocks() throws ApiWriteLockException {
		try {
			getDao().deleteAll();
		} catch(Exception e) {
			throw new ApiWriteLockException("Unable to delete lock.", e);
		}
	}

}