package eu.europeana.set.mongo.dao;

import java.io.Serializable;

import eu.europeana.api.commons.nosql.dao.NosqlDao;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;

public interface PersistentUserSetDao<E extends PersistentUserSet, T extends Serializable > extends NosqlDao<E, T> {
	
	long generateNextUserSetId(String provider);
}
