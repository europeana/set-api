package eu.europeana.set.web.model;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europeana.api.commons.web.model.ApiResponse;

@JsonInclude(Include.NON_NULL)
public class SetOperationResponse extends ApiResponse{
  
    @JsonProperty("since")
    private Date since;
    
    @JsonProperty("end")
    private Date end;
	
	public static final String ERROR_NO_OBJECT_FOUND = "No Object Found!";
	public static final String ERROR_VISIBILITY_CHECK = "This annotation object is marked as not visible!";
	public static final String ERROR_RESOURCE_ID_DOES_NOT_MATCH = 
			"Passed 'collection' or 'object' parameter does not match to the ResourceId given in the JSON string!";	
	public static final String ERROR_PROVIDER_DOES_NOT_MATCH = 
			"Passed 'provider' parameter does not match to the provider given in the JSON string!";	
	
	public static final String ERROR_ANNOTATION_EXISTS_IN_DB = 
			"Cannot store object! An object with the given id already exists in the database: ";
	
	public static final String ERROR_STATUS_TYPE_NOT_REGISTERED = 
			"Cannot set annotation status! A given status type is not registered: ";
	
	public static final String ERROR_STATUS_ALREADY_SET = 
			"A given status type is already set: ";

	
	public SetOperationResponse(String apiKey, String action){
		super(apiKey, action);
	}
	
	@Override
	@JsonGetter("message")
	public String getStatus() {
	  return super.getStatus();
	}

    public Date getSince() {
      return since;
    }

    public void setSince(Date since) {
      this.since = since;
    }

    public Date getEnd() {
      return end;
    }

    public void setEnd(Date end) {
      this.end = end;
    }
	
}
