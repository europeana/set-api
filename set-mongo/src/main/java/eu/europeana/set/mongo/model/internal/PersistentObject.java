package eu.europeana.set.mongo.model.internal;

import java.util.Date;

public interface PersistentObject{

	public Date getCreated();

	public void setCreated(Date creationDate);

	public Date getLastUpdate();

	public void setLastUpdate(Date lastUpdate);

	

}
