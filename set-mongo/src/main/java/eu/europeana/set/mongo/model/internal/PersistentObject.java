package eu.europeana.set.mongo.model.internal;

import java.util.Date;

public interface PersistentObject {

    Date getCreated();

    void setCreated(Date creationDate);

    Date getLastUpdate();

    void setLastUpdate(Date lastUpdate);
}
