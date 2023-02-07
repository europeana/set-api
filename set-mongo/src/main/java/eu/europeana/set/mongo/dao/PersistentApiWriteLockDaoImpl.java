package eu.europeana.set.mongo.dao;

import java.io.Serializable;
import org.mongodb.morphia.Datastore;
import eu.europeana.api.commons.nosql.dao.impl.NosqlDaoImpl;
import eu.europeana.set.mongo.model.internal.PersistentApiWriteLock;

public class PersistentApiWriteLockDaoImpl <E extends PersistentApiWriteLock, T extends Serializable>
extends NosqlDaoImpl<E, T> implements PersistentApiWriteLockDao<E, T>{

    public PersistentApiWriteLockDaoImpl(Class<E> clazz, Datastore datastore) {
        super(datastore, clazz);
    }

}
