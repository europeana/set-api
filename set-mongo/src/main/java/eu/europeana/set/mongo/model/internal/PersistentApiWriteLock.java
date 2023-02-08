package eu.europeana.set.mongo.model.internal;

import java.util.Date;
import org.bson.types.ObjectId;
import eu.europeana.api.commons.nosql.entity.NoSqlEntity;

public interface PersistentApiWriteLock extends NoSqlEntity {	
	
	ObjectId getId();

	String getName();

	void setName(String name);
	
	void setId(ObjectId id);

	void setStarted(Date started);

	Date getStarted();

	Date getEnded();

	void setEnded(Date ended);

}
