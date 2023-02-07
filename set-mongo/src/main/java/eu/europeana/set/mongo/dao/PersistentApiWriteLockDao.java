package eu.europeana.set.mongo.dao;

import java.io.Serializable;
import eu.europeana.api.commons.nosql.dao.NosqlDao;
import eu.europeana.set.mongo.model.internal.PersistentApiWriteLock;

/**
 *
 * @param <E>
 * @param <T>
 */
public interface PersistentApiWriteLockDao<E extends PersistentApiWriteLock, T extends Serializable > extends NosqlDao<E, T> {

}
