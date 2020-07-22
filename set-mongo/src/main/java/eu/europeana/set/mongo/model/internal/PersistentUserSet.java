package eu.europeana.set.mongo.model.internal;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import eu.europeana.api.commons.nosql.entity.NoSqlEntity;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.vocabulary.fields.WebUserSetModelFields;

/**
 * @author GrafR
 *
 */
public interface PersistentUserSet extends UserSet, NoSqlEntity {

		public final static String FIELD_IDENTIFIER = WebUserSetModelFields.IDENTIFIER;
		
		public abstract ObjectId getObjectId();		
		public abstract String getIdentifier();		
		public abstract void setIdentifier(String sequenceIdentifier);
		public abstract String getType();
		public abstract void setType(String type);
		public abstract Map<String, String> getTitle();
		public abstract void setTitle(Map<String, String> title);
		public abstract Map<String, String> getDescription();
		public abstract void setDescription(Map<String, String> description);
		public abstract String getItemType();
		public abstract void setItemType(String itemType);
		public abstract List<String> getSubject();
		public abstract void setSubject(List<String> subject);
		public abstract String getSpatial();
		public abstract void setSpatial(String spatial);
		public abstract boolean isUgc();
		public abstract void setUgc(boolean ugc);
		public abstract Agent getCreator();
		public abstract void setCreator(Agent creator);
		public abstract Date getCreated();
		public abstract void setCreated(Date created);
		public abstract Date getModified();
		public abstract void setModified(Date modified);
		public abstract String getFirst();
		public abstract void setFirst(String first);
		public abstract String getLast();
		public abstract void setLast(String last);
		public abstract int getTotal();
		public abstract void setTotal(int total);
		public abstract int getCollectionPage();
		public abstract void setCollectionPage(int collectionPage);
		public abstract List<String> getItems();
		public abstract void setItems(List<String> items);
		public abstract int getNext();
		public abstract void setNext(int next);
		public abstract int getPrev();
		public abstract void setPrev(int prev);
		public abstract String getPartOf();
		public abstract void setPartOf(String partOf);			
}