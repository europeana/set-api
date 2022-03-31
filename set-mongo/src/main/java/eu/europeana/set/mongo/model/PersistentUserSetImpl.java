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
    @Index(fields = {@Field(WebUserSetFields.PROVIDER)}),
    @Index(options = @IndexOptions(name = "text", disableValidation=true), fields = {
			@Field(value = WebUserSetFields.TITLE+".en", type = IndexType.TEXT),
			@Field(value = WebUserSetFields.TITLE+".nl", type = IndexType.TEXT),
			@Field(value = WebUserSetFields.TITLE+".fr", type = IndexType.TEXT),
			@Field(value = WebUserSetFields.TITLE+".de", type = IndexType.TEXT),
			@Field(value = WebUserSetFields.TITLE+".es", type = IndexType.TEXT),
			@Field(value = WebUserSetFields.TITLE+".sv", type = IndexType.TEXT),
			@Field(value = WebUserSetFields.TITLE+".it", type = IndexType.TEXT),
			@Field(value = WebUserSetFields.TITLE+".fi", type = IndexType.TEXT),
			@Field(value = WebUserSetFields.TITLE+".da", type = IndexType.TEXT),
			@Field(value = WebUserSetFields.TITLE+".el", type = IndexType.TEXT),
			@Field(value = WebUserSetFields.TITLE+".cs", type = IndexType.TEXT),
			@Field(value = WebUserSetFields.TITLE+".sk", type = IndexType.TEXT),
			@Field(value = WebUserSetFields.TITLE+".sl", type = IndexType.TEXT),
			@Field(value = WebUserSetFields.TITLE+".pt", type = IndexType.TEXT),
			@Field(value = WebUserSetFields.TITLE+".hu", type = IndexType.TEXT),
			@Field(value = WebUserSetFields.TITLE+".lt", type = IndexType.TEXT),
			@Field(value = WebUserSetFields.TITLE+".pl", type = IndexType.TEXT),
			@Field(value = WebUserSetFields.TITLE+".ro", type = IndexType.TEXT),
			@Field(value = WebUserSetFields.TITLE+".bg", type = IndexType.TEXT),
			@Field(value = WebUserSetFields.TITLE+".hr", type = IndexType.TEXT),
			@Field(value = WebUserSetFields.TITLE+".lv", type = IndexType.TEXT),
			@Field(value = WebUserSetFields.TITLE+".ga", type = IndexType.TEXT),
			@Field(value = WebUserSetFields.TITLE+".et", type = IndexType.TEXT)
    })

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

	@Override
	public void setBaseUrl(String baseUrl) {
	    //used only for web userset	    
	}
}