package eu.europeana.set.web.model;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;

import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.mongo.model.PersistentUserSetImpl;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldProperty;

@JsonPropertyOrder({ WebUserSetModelFields.ID, WebUserSetModelFields.TYPE, WebUserSetModelFields.TITLE, WebUserSetFields.DESCRIPTION,
        WebUserSetModelFields.VISIBILITY, WebUserSetModelFields.IS_DEFINED_BY, WebUserSetModelFields.ITEMS, WebUserSetModelFields.CREATOR,
        WebUserSetModelFields.CREATED, WebUserSetModelFields.MODIFIED, WebUserSetModelFields.TOTAL, WebUserSetFields.NEXT,
	WebUserSetFields.PREV })
@JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
public class WebUserSetImpl extends PersistentUserSetImpl {

    List<String> serializedItems;

    @Override
    @JsonProperty(WebUserSetFields.ITEMS)
    public void setItems(List<String> items) {
	super.setItems(items);
    }

    @JsonProperty(WebUserSetFields.ITEMS)
    @JsonRawValue
    public List<String> getSerializedItems() {
	return serializedItems;
    }

    @JsonIgnore //use serializedItems for serialization
    public List<String> getItems() {
	return super.getItems();
    }

    @JsonIgnore //use setItems for deserialization
    public void setSerializedItems(List<String> serializedItems) {
	this.serializedItems = serializedItems;
    }

    @JsonIgnore
    public String getIdentifier() {
	return super.getIdentifier();
    }

    @JsonProperty(WebUserSetFields.TYPE)
    public void setType(String type) {
	super.setType(type);
    }

    @JsonProperty(WebUserSetModelFields.VISIBILITY)
    public void setVisibility(String visibility) {
	super.setVisibility(visibility);
    }

    @JsonIgnore
    public int getNext() {
	return super.getNext();
    }

    @JsonIgnore
    public int getPrev() {
	return super.getPrev();
    }

    @JsonProperty(WebUserSetFields.TITLE)
    public Map<String, String> getTitle() {
	return super.getTitle();
    }

    @JsonProperty(WebUserSetFields.DESCRIPTION)
    public Map<String, String> getDescription() {
	return super.getDescription();
    }

    @JsonProperty(WebUserSetModelFields.TOTAL)
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = PositiveIntegerFilter.class)
    public int getTotal() {
	return super.getTotal();
    }

    @JsonProperty(WebUserSetModelFields.IS_DEFINED_BY)
    public String getIsDefinedBy() {
	return super.getIsDefinedBy();
    }
    
//    @JsonldProperty(WebUserSetFields.CONTEXT_FIELD)
//    @JsonIgnore // avoid double serialization
//    public String getContext() {
//	return super.getContext();
//    }

    @JsonldProperty(WebUserSetFields.CONTEXT_FIELD)
    public void setContext(String context) {
	// do nothing, just to allow cotnext in input
    }

//    @JsonProperty(WebUserSetFields.CREATOR)
    @JsonIgnore //creator is set by the system, proper serialization postponed for next version
    public Agent getCreator() {
	return super.getCreator();
    }

    @Override
    @JsonIgnore //creator is set by the system
    public void setCreator(Agent creator) {
	super.setCreator(creator);
    }
  
    @JsonProperty(WebUserSetModelFields.CREATOR)
    public String getCreatorHttpUrl() {
	String res = null;
	if (super.getCreator() != null && super.getCreator().getHttpUrl() != null) {
	    res = super.getCreator().getHttpUrl();
	}
	return res;
    }

    @JsonIgnore
    public boolean isUgc() {
	return super.isUgc();
    }

    @JsonIgnore
    public String getFirst() {
	return super.getFirst();
    }

    @JsonIgnore
    public String getLast() {
	return super.getLast();
    }

    @JsonIgnore
    public int getCollectionPage() {
	return super.getCollectionPage();
    }

    /**
     * This method presents Id as URL.
     *
     * @return string presenting ID as URL
     */
    @JsonProperty(WebUserSetModelFields.ID)
    public String getId() {
	return UserSetUtils.buildUserSetId(super.getIdentifier());
    }

    public void setId(String id) {
	// this method is defined just for avoiding parse errors in update method, the
	// URI is ignored as the identifier from URL is used
    }

    @Override
    public String toString() {
	StringBuilder resBuilder = new StringBuilder();
	resBuilder.append("WebUserSet [");
	if (getTitle() != null && StringUtils.isNotEmpty(getTitle().toString())) {
	    resBuilder.append("Title: ");
	    resBuilder.append(getTitle());
	}
	if (StringUtils.isNotEmpty(getIdentifier())) {
	    resBuilder.append(", Identifier: ");
	    resBuilder.append(getIdentifier());
	}
	if (StringUtils.isNotEmpty(Integer.toString(getTotal()))) {
	    resBuilder.append(", Total items: ");
	    resBuilder.append(getTotal());
	}
	if (! getItems().isEmpty()) {
	    resBuilder.append(", Items: ");
	    resBuilder.append(getItems().size());
	}
	resBuilder.append("]");
	return resBuilder.toString();
    }

    @Override
    @JsonIgnore
    public ObjectId getObjectId() {
	return super.getObjectId();
    }

    @Override
    @JsonIgnore
    public boolean isOpenSet() {
	return super.isOpenSet();
    }

    @Override
    @JsonIgnore
    public boolean isPrivate() {
	return super.isPrivate();
    }

    @Override
    @JsonIgnore
    public boolean isPublic() {
	return super.isPublic();
    }

    @Override
    @JsonIgnore
    public boolean isPublished() {
	return super.isPublished();
    }
}
