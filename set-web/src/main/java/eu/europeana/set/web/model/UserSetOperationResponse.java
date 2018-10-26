package eu.europeana.set.web.model;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.api.commons.web.model.ApiResponse;

@JsonSerialize(include = Inclusion.NON_EMPTY)
public class UserSetOperationResponse extends ApiResponse{
	
	UserSet userSet;
	
//	private BatchReportable operationReport;

	public static String ERROR_NO_OBJECT_FOUND = "No Object Found!";
	public static String ERROR_VISIBILITY_CHECK = "This user set object is marked as not visible!";
	public static String ERROR_RESOURCE_ID_DOES_NOT_MATCH = 
			"Passed 'collection' or 'object' parameter does not match to the ResourceId given in the JSON string!";	
	public static String ERROR_PROVIDER_DOES_NOT_MATCH = 
			"Passed 'provider' parameter does not match to the provider given in the JSON string!";	
	
	public static String ERROR_USERSET_EXISTS_IN_DB = 
			"Cannot store object! An object with the given id already exists in the database: ";
	
	public static String ERROR_STATUS_TYPE_NOT_REGISTERED = 
			"Cannot set user set status! A given status type is not registered: ";
	
	public static String ERROR_STATUS_ALREADY_SET = 
			"A given status type is already set: ";

	
	public UserSetOperationResponse(String apiKey, String action){
		super(apiKey, action);
	}
	
	public UserSet getUserSet() {
		return userSet;
	}

	public void setUserSet(UserSet userSet) {
		this.userSet = userSet;
	}

}
