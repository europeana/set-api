package eu.europeana.set.definitions.model;

import java.util.Date;
import java.util.List;

import eu.europeana.set.definitions.model.agent.Agent;

public interface UserSet {

	String getType();

	void setType(String type);

	String getTitle();

	void setTitle(String title);

	String getDescription();

	void setDescription(String description);

	String getItemType();

	void setItemType(String itemType);

	List<String> getSubject();

	void setSubject(List<String> subject);

	String getSpatial();

	void setSpatial(String spatial);

	boolean isUgc();

	void setUgc(boolean ugc);

	Agent getCreator();

	void setCreator(Agent creator);

	Date getCreated();

	void setCreated(Date created);

	Date getModified();

	void setModified(Date modified);

	int getFirst();

	void setFirst(int first);

	int getLast();

	void setLast(int last);

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