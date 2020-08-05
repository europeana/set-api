package eu.europeana.set.definitions.model.impl;

import eu.europeana.set.definitions.model.UserSetId;

public class BaseUserSetId implements UserSetId {

	private static final long serialVersionUID = 1L;
	
	String sequenceNumber = "";
	String collection = "";

	public void setSequenceNumber(String sequenceNr) {
		this.sequenceNumber = sequenceNr;
	}

	public String getSequenceNumber() {
		return sequenceNumber;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}
	
	
}
