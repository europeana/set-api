package eu.europeana.set.web.http;

public interface SwaggerConstants {

	/**
	 * Implementation Notes
	 */
	public static final String SAMPLES_JSONLD = "Please find JSON-LD samples for user set in <a href=\"../jsp/template/jsonld.jsp\" target=\"_blank\">templates</a>. ";
	public static final String SEARCH_HELP_NOTE = "Identifier is a number.";
	public static final String UPDATE_SAMPLES_JSONLD = "Please find JSON-LD samples for user set in <a href=\"../jsp/template/jsonld.jsp\" target=\"_blank\">templates</a>. " +
			"Please create your JSON update request using selected fields you are going to update. E.g. 'title' and 'description' example:  { \"title\": {\r\n" + 
			"     \"en\": \"New Title\"\r\n" + 
			"  }\r\n }";
	public static final String INSERT_ITEM_NOTE = "Please create your insert item request using selected parameters.";
	public static final String CHECK_ITEM_NOTE = "Check if item is already in a user set";
	public static final String DELETE_ITEM_NOTE = "Delete a item from the set ";
}
