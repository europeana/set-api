package eu.europeana.set.definitions.model.impl;

import eu.europeana.set.definitions.model.PageInfo;

public class BasePageInfo implements PageInfo{

    // Indicates the furthest preceding page of items in the Set
    private String first;

    // Indicates the furthest proceeding page of the collection
    private String last;

    // A non-negative integer specifying the total number of items that are
    // contained within a Set
    private int total = 0;

    // For OrderedCollectionPage class

    // Used to represent an ordered subsets of items from an OrderedCollection
    private int collectionPage;

    // Identifies the Collection to which CollectionPage objects items belong
    private String partOf;

    /*
     * (non-Javadoc)
     * 
     * @see eu.europeana.set.definitions.model.PageInfo#getFirst()
     */
    @Override
    public String getFirst() {
	return first;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europeana.set.definitions.model.PageInfo#setFirst(int)
     */
    @Override
    public void setFirst(String first) {
	this.first = first;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.eu.europeana.set.definitions.model.PageInfo#getLast()
     */
    @Override
    public String getLast() {
	return last;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europeana.set.definitions.model.PageInfo#setLast(int)
     */
    @Override
    public void setLast(String last) {
	this.last = last;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europeana.set.definitions.model.PageInfo#getTotal()
     */
    @Override
    public int getTotal() {
	return total;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europeana.set.definitions.model.PageInfo#setTotal(int)
     */
    @Override
    public void setTotal(int total) {
	this.total = total;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europeana.set.definitions.model.PageInfo#getCollectionPage()
     */
    @Override
    public int getCollectionPage() {
	return collectionPage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europeana.set.definitions.model.PageInfo#setCollectionPage(int)
     */
    @Override
    public void setCollectionPage(int collectionPage) {
	this.collectionPage = collectionPage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europeana.set.definitions.model.PageInfo#getNext()
     */
//    @Override
//    public int getNext() {
//	return next;
//    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europeana.set.definitions.model.PageInfo#setNext(int)
     */
//    @Override
//    public void setNext(int next) {
//	this.next = next;
//    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europeana.set.definitions.model.PageInfo#getPrev()
     */
//    @Override
//    public int getPrev() {
//	return prev;
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see eu.europeana.set.definitions.model.PageInfo#setPrev(int)
//     */
//    @Override
//    public void setPrev(int prev) {
//	this.prev = prev;
//    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.europeana.set.definitions.model.PageInfo#getPartOf()
     */
    @Override
    public String getPartOf() {
	return partOf;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eu.europeana.set.definitions.model.PageInfo#setPartOf(java.lang.String)
     */
    @Override
    public void setPartOf(String partOf) {
	this.partOf = partOf;
    }
}
