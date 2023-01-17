package eu.europeana.set.mongo.dao;

import java.io.Serializable;
import java.util.List;

import eu.europeana.api.commons.nosql.dao.NosqlDao;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import org.bson.types.ObjectId;

public interface PersistentUserSetDao<E extends PersistentUserSet, T extends Serializable > extends NosqlDao<E, T> {
	
	long generateNextUserSetId(String provider);

	void deleteByObjectId(List<ObjectId> objectIds);
	void deleteByIdentifier(List<String> setIds);
}
