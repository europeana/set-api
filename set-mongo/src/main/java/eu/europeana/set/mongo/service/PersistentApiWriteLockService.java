package eu.europeana.set.mongo.service;

import eu.europeana.api.commons.nosql.service.AbstractNoSqlService;
import eu.europeana.set.definitions.exception.ApiWriteLockException;
import eu.europeana.set.mongo.model.internal.PersistentApiWriteLock;

public interface PersistentApiWriteLockService extends AbstractNoSqlService<PersistentApiWriteLock, String> {

	public PersistentApiWriteLock lock(String action) throws ApiWriteLockException;
	
	public void unlock(PersistentApiWriteLock pij) throws ApiWriteLockException;
	
	public PersistentApiWriteLock getLastActiveLock(String name) throws ApiWriteLockException;
	
	public PersistentApiWriteLock getLockById(String id) throws ApiWriteLockException;

	public PersistentApiWriteLock getLastActiveLock() throws ApiWriteLockException;
	
	public void deleteAllLocks() throws ApiWriteLockException;
	
}
