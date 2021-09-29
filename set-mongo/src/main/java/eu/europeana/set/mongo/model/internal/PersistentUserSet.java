package eu.europeana.set.mongo.model.internal;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import eu.europeana.api.commons.nosql.entity.NoSqlEntity;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.agent.Agent;

/**
 * @author GrafR
 */
//@Entity
public interface PersistentUserSet extends UserSet, NoSqlEntity {

    ObjectId getObjectId();

    String getIdentifier();

    void setIdentifier(String sequenceIdentifier);

    String getType();

    void setType(String type);

    Map<String, String> getTitle();

    void setTitle(Map<String, String> title);

    Map<String, String> getDescription();

    void setDescription(Map<String, String> description);

    List<String> getSubject();

    void setSubject(List<String> subject);

    int getPinned();

    void setPinned(int pinned);


    boolean isUgc();

    void setUgc(boolean ugc);

    Agent getCreator();

    void setCreator(Agent creator);

    Date getCreated();

    void setCreated(Date created);

    Date getModified();

    void setModified(Date modified);

    String getFirst();

    void setFirst(String first);

    String getLast();

    void setLast(String last);

    int getTotal();

    void setTotal(int total);

    List<String> getItems();

    void setItems(List<String> items);

    String getPartOf();

    void setPartOf(String partOf);
}