package eu.europeana.set.mongo.service;

import eu.europeana.api.commons.nosql.service.AbstractNoSqlService;
import eu.europeana.set.definitions.exception.ApiWriteLockException;
import eu.europeana.set.mongo.model.internal.PersistentApiWriteLock;

public interface PersistentApiWriteLockService extends AbstractNoSqlService<PersistentApiWriteLock, String> {

	PersistentApiWriteLock lock(String action) throws ApiWriteLockException;
	
	void unlock(PersistentApiWriteLock pij) throws ApiWriteLockException;
	
	PersistentApiWriteLock getLastActiveLock(String name) throws ApiWriteLockException;
	
	PersistentApiWriteLock getLockById(String id) throws ApiWriteLockException;

	PersistentApiWriteLock getLastActiveLock() throws ApiWriteLockException;
	
	void deleteAllLocks() throws ApiWriteLockException;
	
}
