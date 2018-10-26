package eu.europeana.set.definitions.model;

import java.io.Serializable;

public interface UserSetId extends Serializable {

	String NOT_INITIALIZED_LONG_ID = "-1";
	
	/**
	 * unanbiguous identifier of the resource for a given provider
	 * @return
	 */
	public void setSequenceNumber(String sequenceNr);

	public String getSequenceNumber();

	/**
	 * collection  e.g. 'userset'
	 * @return
	 */
	public String getCollection();

	public void setCollection(String collection);
	
	
}
