package eu.europeana.set.mongo.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;

import eu.europeana.set.definitions.model.impl.BaseUserSet;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;

@Entity("userset")
@Indexes({@Index(fields = {@Field(WebUserSetFields.IDENTIFIER)}, unique = true),
    @Index(fields = {@Field(WebUserSetFields.CREATOR)}),
    @Index(fields = {@Field(WebUserSetFields.TYPE)}),
    @Index(fields = {@Field(WebUserSetFields.VISIBILITY)}),
    @Index(fields = {@Field(WebUserSetFields.CONTRIBUTOR)}),
    @Index(fields = {@Field(WebUserSetFields.SUBJECT)}),
    @Index(fields = {@Field(WebUserSetFields.ITEMS)}),
    @Index(fields = {@Field(WebUserSetModelFields.MODIFIED)})})
public class PersistentUserSetImpl extends BaseUserSet implements PersistentUserSet {

	@Id
	private ObjectId id;

    public ObjectId getObjectId() {
		return id;
	}

	public void setObjectId(ObjectId id) {
		this.id = id;
	}
		
	@Override
	public String toString() {
		return "PersistentUserSet [Title:" + getTitle() + ", created:" + getCreated() + 
				", Id:" + getObjectId() + ", Identifier:" + getIdentifier() + 
				", modified: " + getModified() + "]";
	}
}