package eu.europeana.set.definitions.model;

public interface PageInfo {

	String getFirst();

	void setFirst(String first);

	String getLast();

	void setLast(String last);

	int getTotal();

	void setTotal(int total);

	int getCollectionPage();

	void setCollectionPage(int collectionPage);

	int getNext();

	void setNext(int next);

	int getPrev();

	void setPrev(int prev);

	String getPartOf();

	void setPartOf(String partOf);

}
