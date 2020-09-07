package eu.europeana.set.web.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.europeana.set.definitions.model.agent.impl.SoftwareAgent;
import eu.europeana.set.definitions.model.vocabulary.fields.WebUserSetModelFields;

//@JsonldType("http://europeana.eu/schemas/context/collection/Agent")
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class WebSoftwareAgent extends SoftwareAgent{

	public WebSoftwareAgent(){
		super();
	}
	
	    @Override
	    @JsonIgnore
	    public String getType() {
		return super.getType();
	    }
	    
	    @Override
	    @JsonProperty(WebUserSetModelFields.ID)
	    public String getHttpUrl() {
	        return super.getHttpUrl();
	    }
	    
	    @Override
	    @JsonProperty(WebUserSetModelFields.NICKNAME)
	    public String getNickname() {
	        return super.getNickname();
	    }
}
