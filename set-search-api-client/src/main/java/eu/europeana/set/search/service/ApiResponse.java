/**
 * 
 */
package eu.europeana.set.search.service;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Deprecated
/** refactor code to remove dependency on web modules and change this class */
@JsonInclude(Include.NON_NULL)
public abstract class ApiResponse {

	public String apikey;

	public String action;

	public boolean success = true;

	public String error;

	public Date statsStartTime;

	public Long statsDuration;

	public Long requestNumber;

	String status;

	String stackTrace;

	public ApiResponse(String apikey, String action) {
		this.apikey = apikey;
		this.action = action;
	}

	public ApiResponse() {
		// used by Jackson
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}
}
