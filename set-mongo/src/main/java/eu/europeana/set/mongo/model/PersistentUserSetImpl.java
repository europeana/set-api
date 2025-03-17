package eu.europeana.set.mongo.model;

import org.bson.types.ObjectId;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import dev.morphia.utils.IndexType;
import eu.europeana.set.definitions.model.impl.BaseUserSet;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;

@Entity("userset")
@Indexes({@Index(fields = {@Field(WebUserSetModelFields.IDENTIFIER)},options = @IndexOptions(unique = true)),
    @Index(fields = {@Field(WebUserSetModelFields.CREATOR)}),
    @Index(fields = {@Field(WebUserSetModelFields.TYPE)}),
    @Index(fields = {@Field(WebUserSetModelFields.COLLECTION_TYPE)}),
    @Index(fields = {@Field(WebUserSetModelFields.VISIBILITY)}),
    @Index(fields = {@Field(WebUserSetModelFields.CONTRIBUTOR)}),
    @Index(fields = {@Field(WebUserSetModelFields.SUBJECT)}),
    @Index(fields = {@Field(WebUserSetModelFields.ITEMS)}),
    @Index(fields = {@Field(WebUserSetModelFields.MODIFIED)}),
    @Index(fields = {@Field(WebUserSetModelFields.PROVIDER)}),
    @Index(options = @IndexOptions(name = "text", disableValidation=true), fields = {
			@Field(value = WebUserSetModelFields.TITLE+".en", type = IndexType.TEXT),
			@Field(value = WebUserSetModelFields.TITLE+".nl", type = IndexType.TEXT),
			@Field(value = WebUserSetModelFields.TITLE+".fr", type = IndexType.TEXT),
			@Field(value = WebUserSetModelFields.TITLE+".de", type = IndexType.TEXT),
			@Field(value = WebUserSetModelFields.TITLE+".es", type = IndexType.TEXT),
			@Field(value = WebUserSetModelFields.TITLE+".sv", type = IndexType.TEXT),
			@Field(value = WebUserSetModelFields.TITLE+".it", type = IndexType.TEXT),
			@Field(value = WebUserSetModelFields.TITLE+".fi", type = IndexType.TEXT),
			@Field(value = WebUserSetModelFields.TITLE+".da", type = IndexType.TEXT),
			@Field(value = WebUserSetModelFields.TITLE+".el", type = IndexType.TEXT),
			@Field(value = WebUserSetModelFields.TITLE+".cs", type = IndexType.TEXT),
			@Field(value = WebUserSetModelFields.TITLE+".sk", type = IndexType.TEXT),
			@Field(value = WebUserSetModelFields.TITLE+".sl", type = IndexType.TEXT),
			@Field(value = WebUserSetModelFields.TITLE+".pt", type = IndexType.TEXT),
			@Field(value = WebUserSetModelFields.TITLE+".hu", type = IndexType.TEXT),
			@Field(value = WebUserSetModelFields.TITLE+".lt", type = IndexType.TEXT),
			@Field(value = WebUserSetModelFields.TITLE+".pl", type = IndexType.TEXT),
			@Field(value = WebUserSetModelFields.TITLE+".ro", type = IndexType.TEXT),
			@Field(value = WebUserSetModelFields.TITLE+".bg", type = IndexType.TEXT),
			@Field(value = WebUserSetModelFields.TITLE+".hr", type = IndexType.TEXT),
			@Field(value = WebUserSetModelFields.TITLE+".lv", type = IndexType.TEXT),
			@Field(value = WebUserSetModelFields.TITLE+".ga", type = IndexType.TEXT),
			@Field(value = WebUserSetModelFields.TITLE+".et", type = IndexType.TEXT)
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

  @Override
  public boolean hasItem(String itemId) {
    return getItems() != null && getItems().contains(itemId); 
  }
}