package eu.europeana.set.client.model.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author GrafR
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AbstractUserSetApiResponse {

	private String action;
	private String success;
	private String error;
	private String message;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getSuccess() {
		return success;
	}

	public void setSuccess(String success) {
		this.success = success;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
