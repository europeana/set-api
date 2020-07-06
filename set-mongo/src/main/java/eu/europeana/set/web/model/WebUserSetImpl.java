package eu.europeana.set.web.model;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.mongo.model.PersistentUserSetImpl;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldProperty;

@JsonPropertyOrder({ "id", "type", "title", "description", "collectionPage", "next", "prev", "creator", "created", "modified", "first", "last", "total", "items" })
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class WebUserSetImpl extends PersistentUserSetImpl {
		
	@JsonIgnore
	public String getIdentifier() {
		return super.getIdentifier();
	}
		
	@JsonldProperty("type")
	public void setType(String type) {
		super.setType(type);
	}
	
	@JsonIgnore
	public int getNext() {
		return super.getNext();
	}
	
	@JsonIgnore
	public int getPrev() {
		return super.getPrev();
	}
	
	@JsonldProperty("title")
	public Map<String, String> getTitle() {
		return super.getTitle();
	}

	@JsonldProperty("description")
	public Map<String, String> getDescription() {
		return super.getDescription();
	}		
	
	@JsonldProperty("total")
	@JsonInclude(value = JsonInclude.Include.ALWAYS)
	public int getTotal() {
		return super.getTotal();
	}		
	
    @JsonldProperty(WebUserSetFields.IS_DEFINED_BY)
    public String getIsDefinedBy() {
    	return super.getIsDefinedBy();
    }
    
	@JsonldProperty("@context")	
	@JsonIgnore
	public String getContext() {
		return super.getContext();
	}
	
	@JsonProperty("@context")
	@Override
	public void setContext(String context) {
		super.setContext(context);
	}
	
	@JsonldProperty("creator")
	@JsonIgnore
	public Agent getCreator() {
		return super.getCreator();
	}
	
	@JsonldProperty("items")
	public List<String> getItems() {
		return super.getItems();
	}
	
	@JsonIgnore
	public boolean isDisabled() {
		return super.isDisabled();		
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
	 * @param id   The user set id
	 * @param base The base URL
	 * @return string presenting ID as URL
	 */
	@JsonldProperty("id")
	public String getId() {
	    StringBuilder urlBuilder = new StringBuilder();
	    urlBuilder.append(WebUserSetFields.BASE_SET_URL);
	    urlBuilder.append(super.getIdentifier());
	    return urlBuilder.toString();
	}
	
	public void setId(String id) {
	    //this method is defined just for avoiding parse errors in update method, the URI is ignored as the identifier from URL is used
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
	
	@Override
	@JsonIgnore
	public boolean isOpenSet() {
	    // TODO Auto-generated method stub
	    return super.isOpenSet();
	}
}
