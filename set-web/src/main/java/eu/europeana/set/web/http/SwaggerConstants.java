package eu.europeana.set.web.http;

public class SwaggerConstants {

    private SwaggerConstants() {
        //private constructor to hide implicit one
    }

    /**
     * Implementation Notes
     */
    public static final String SAMPLES_JSONLD   = "Please find JSON-LD samples for user set in <a href=\"../jsp/template/jsonld.jsp\" target=\"_blank\">templates</a>. ";
    public static final String SEARCH_HELP_NOTE = "Identifier is a number.";
    public static final String INSERT_ITEM_NOTE = "Please create your insert item request using selected parameters.";
    public static final String INSERT_MULTIPLE_ITEM_NOTE = "Please create your insert multiple items request using selected parameters.";
    public static final String PUBLISH_SET_NOTE = "Please create the request for publishing the set using the provided parameters.";
    public static final String CHECK_ITEM_NOTE  = "Check if item is already in a user set";
    public static final String DELETE_ITEM_NOTE = "Delete a item from the set ";
    public static final String SEARCH           = "Searching user sets. The criteria for filtering results is provided in form of <field>:<value> pairs, using the query and qf request parameters. Currently supported fields are: type, visibility, creator, item, provider and lang (title language).";
    public static final String SEARCH_ITEMS_IN_SET           = "Searching items within a set indicated by identifier parameter. Use the use * for query and item:<itemId> filter in qf parameter. (Multiple item filters are build an OR search criteria)";
    public static final String UPDATE_SAMPLES_JSONLD = SAMPLES_JSONLD + "Please create your JSON update request using selected fields you are going " +
            "to update. E.g. 'title' and 'description' example:  { \"title\": {\r\n \"en\": \"New Title\"\r\n" + "  }\r\n }";
    public static final String SET_USAGE_STATS = "Generating set API usage statistics. Pushing metrics to Databox ";

}
