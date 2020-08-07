package eu.europeana.set.client.model.result;

import eu.europeana.set.definitions.model.UserSet;

public class UserSetOperationResponse extends AbstractUserSetApiResponse{

	private UserSet set;
	
	private String json;
	
	public UserSet getUserSet() {
		return set;
	}
	public void setUserSet(UserSet set) {
		this.set = set;
	}
	public String getJson() {
		return json;
	}
	public void setJson(String json) {
		this.json = json;
	}
}
