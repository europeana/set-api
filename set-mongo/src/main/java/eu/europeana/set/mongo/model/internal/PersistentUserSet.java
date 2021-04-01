package eu.europeana.set.mongo.model.internal;

import eu.europeana.api.commons.nosql.entity.NoSqlEntity;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author GrafR
 */
@Entity
@Indexes({@Index(fields = {@Field(WebUserSetFields.IDENTIFIER)}),
        @Index(fields = {@Field(WebUserSetFields.CREATOR)}),
        @Index(fields = {@Field(WebUserSetFields.TYPE)}),
        @Index(fields = {@Field(WebUserSetFields.VISIBILITY)}),
        @Index(fields = {@Field(WebUserSetFields.CONTRIBUTOR)}),
        @Index(fields = {@Field(WebUserSetFields.SUBJECT)}),
        @Index(fields = {@Field(WebUserSetModelFields.MODIFIED)})})
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

//    String getItemType();
//
//    void setItemType(String itemType);

    List<String> getSubject();

    void setSubject(List<String> subject);

//    List<String> getContributor();
//
//    void setContributor(List<String> contributorList);

//    String getSpatial();
//
//    void setSpatial(String spatial);

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

    int getCollectionPage();

    void setCollectionPage(int collectionPage);

    List<String> getItems();

    void setItems(List<String> items);

    int getNext();

    void setNext(int next);

    int getPrev();

    void setPrev(int prev);

    String getPartOf();

    void setPartOf(String partOf);
}