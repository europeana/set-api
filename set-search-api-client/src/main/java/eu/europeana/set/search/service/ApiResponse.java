/**
 * 
 */
package eu.europeana.set.search.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**      
 * refactor code to remove dependency on web modules and change this class
 * @deprecated
 */
@Deprecated
@JsonInclude(Include.NON_NULL)
public abstract class ApiResponse {

	private String apikey;

	private String action;

	private String status;

	private String stackTrace;

	public ApiResponse(String apikey, String action) {
		this.apikey = apikey;
		this.action = action;
	}

	public ApiResponse() {
		// used by Jackson
	}

	public String getApikey() {
		return apikey;
	}

	public void setApikey(String apikey) {
		this.apikey = apikey;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
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
