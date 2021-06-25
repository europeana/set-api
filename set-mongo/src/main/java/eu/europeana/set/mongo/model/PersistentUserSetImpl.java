package eu.europeana.set.mongo.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.utils.IndexType;

import eu.europeana.set.definitions.model.impl.BaseUserSet;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;

@Entity("userset")
@Indexes({@Index(fields = {@Field(WebUserSetFields.IDENTIFIER)},options = @IndexOptions(unique = true)),
    @Index(fields = {@Field(WebUserSetFields.CREATOR)}),
    @Index(fields = {@Field(WebUserSetFields.TYPE)}),
    @Index(fields = {@Field(WebUserSetFields.VISIBILITY)}),
    @Index(fields = {@Field(WebUserSetFields.CONTRIBUTOR)}),
    @Index(fields = {@Field(WebUserSetFields.SUBJECT)}),
    @Index(fields = {@Field(WebUserSetFields.ITEMS)}),
    @Index(fields = {@Field(WebUserSetModelFields.MODIFIED)}),
    @Index(options = @IndexOptions(name = "text"), fields = {@Field(value = WebUserSetFields.SUBJECT, type = IndexType.TEXT), @Field(value = WebUserSetFields.DESCRIPTION, type = IndexType.TEXT)})

})
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