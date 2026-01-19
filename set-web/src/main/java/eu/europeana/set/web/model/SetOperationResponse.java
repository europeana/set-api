package eu.europeana.set.web.model;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class SetOperationResponse {

	@JsonProperty("action")
	private String action;

	@JsonProperty("success")
	public boolean success;

	@JsonProperty("message")
	private String message;

	@JsonProperty("status")
	private int status;

    @JsonProperty("since")
    private Date since;
    
    @JsonProperty("end")
    private Date end;

	public SetOperationResponse(String action){
		this.action = action;
	}

	public SetOperationResponse(String action, String message, boolean success, int status){
		this.action = action;
		this.message = message;
		this.success = success;
		this.status = status;
	}

	public boolean isSuccess() {
		return this.success;
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}