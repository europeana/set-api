package eu.europeana.set.web.model;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.mongo.model.PersistentUserSetImpl;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldId;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldProperty;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;


@JsonldType("http://schema.org/UserSet")
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class WebUserSetImpl extends PersistentUserSetImpl {
		
	@JsonldId
	public void setIdentifier(String sequenceIdentifier) {
		super.setIdentifier(sequenceIdentifier);
	}
		
	@JsonldProperty("http://schema.org/type")
	public void setType(String type) {
		super.setType(type);
	}
	
	@JsonldProperty("http://schema.org/title")
	public void setTitle(Map<String, String> title) {
		super.setTitle(title);
	}

	@JsonldProperty("http://schema.org/description")
	public void setDescription(Map<String, String> description) {
		super.setDescription(description);
	}		
	
	@JsonldProperty("@context")
	public void setContext(String context) {
		super.setContext(context);
	}
	
	@JsonldProperty("http://schema.org/creator")
	@JsonIgnore
	public void setCreator(Agent creator) {
		super.setCreator(creator);
	}
	
	@JsonldProperty("http://schema.org/items")
	public void setItems(List<String> items) {
		super.setItems(items);
	}
	
	@JsonIgnore
	public void setDisabled(boolean disabled) {
		super.setDisabled(disabled);		
	}

	@JsonIgnore
	public void setUgc(boolean ugc) {
		super.setUgc(ugc);		
	}

	@JsonIgnore
	public void setFirst(String first) {
		super.setFirst(first);		
	}

	@JsonIgnore
	public void setLast(String last) {
		super.setLast(last);		
	}

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
		if (getItems() != null && getItems().size() > 0) {
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
	
}
