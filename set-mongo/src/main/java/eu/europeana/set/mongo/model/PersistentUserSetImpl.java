package eu.europeana.set.mongo.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;

import eu.europeana.set.definitions.model.impl.BaseUserSet;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;

@Entity("userset")
@Indexes(@Index(value = PersistentUserSet.FIELD_IDENTIFIER, unique = true))
public class PersistentUserSetImpl extends BaseUserSet implements PersistentUserSet {

	@Id
	private ObjectId id;

	private String identifier;

    public ObjectId getObjectId() {
		return id;
	}

	public void setObjectId(ObjectId id) {
		this.id = id;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(String sequenceIdentifier) {
		this.identifier = sequenceIdentifier;
	}
	
		@Override
	public String toString() {
		return "PersistentUserSet [Title:" + getTitle() + ", created:" + getCreated() + 
				", Id:" + getObjectId() + ", Identifier:" + getIdentifier() + 
				", modified: " + getModified() + "]";
	}

}