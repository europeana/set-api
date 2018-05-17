package eu.europeana.set.web.model;

import java.util.Map;

//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.europeana.set.mongo.model.PersistentUserSetImpl;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldId;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldProperty;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;

import eu.europeana.set.utils.serialization.UserSetDeserializer;


@JsonldType("http://schema.org/UserSet")
//@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = UserSetDeserializer.class)
//@Deprecated
public class WebUserSetImpl extends PersistentUserSetImpl {
	
	public String getIdentifier() {
		return getIdentifier();
	}
	
	@JsonldId
	public void setIdentifier(String sequenceIdentifier) {
		setIdentifier(sequenceIdentifier);
	}
	
	public String getType() {
		return getType();
	}

	@JsonldProperty("http://schema.org/type")
	public void setType(String type) {
		setType(type);;
	}

	public Map<String, String> getTitle() {
		return getTitle();
	}

	@JsonldProperty("http://schema.org/title")
	public void setTitle(Map<String, String> title) {
		setTitle(title);
	}

	public Map<String, String> getDescription() {
		return getDescription();
	}

	@JsonldProperty("http://schema.org/description")
	public void setDescription(Map<String, String> description) {
		setDescription(description);
	}	
	
	public String getContext() {
		return getContext();
	}
	
	@JsonldProperty("@context")
	public void setContext(String context) {
		setContext(context);
	}

	public String toString() {
		return "WebUserSet [Title:" + getTitle() + ", Identifier:" + getIdentifier() + "]";
	}
	
}
