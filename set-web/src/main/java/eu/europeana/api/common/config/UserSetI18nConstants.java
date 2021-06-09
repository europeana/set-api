package eu.europeana.api.common.config;

public class UserSetI18nConstants {

    private UserSetI18nConstants() {
        //private constructor to hide implicit one
    }

    public static final String USERSET_INVALID_BODY   = "error.userset_invalid_body";
    public static final String USERSET_CANT_PARSE_BODY = "error.userset_cant_parse_body";
    public static final String USERSET_NOT_FOUND      = "error.userset_not_found";
    public static final String USERSET_ITEM_NOT_FOUND = "error.userset_item_not_found";
    public static final String BASE64_DECODING_FAIL   = "error.userset_base64_encoding_fail";
    public static final String UNSUPPORTED_TOKEN_TYPE = "error.userset_unsupported_token_type";
    public static final String INVALID_HEADER_FORMAT  = "error.userset_invalid_header_format";
    public static final String INVALID_HEADER_VALUE   = "error.userset_invalid_header_value";
    public static final String INVALID_SUBJECT_VALUE   = "error.userset_subject_invalid_value";

    public static final String USERSET_VALIDATION    			= "error.userset_validation";
    public static final String USERSET_VALIDATION_MANDATORY_PROPERTY    = "error.userset_validation_mandatory_property";
    public static final String USERSET_VALIDATION_PROPERTY_VALUE        = "error.userset_validation_property_value";
    public static final String USERSET_VALIDATION_PROPERTY_NOT_ALLOWED  = "error.userset_validation_property_not_allowed";
    public static final String USERSET_VALIDATION_BOOKMARKFOLDER_EXISTS = "error.userset_validation_bookmark_folder_exists";
    public static final String USERSET_VALIDATION_ENTITY_REFERENCE       = "error.userset_entity_reference_not_valid";
    public static final String INVALID_IF_MATCH_TIMESTAMP               = "error.userset_if_match_timestamp";
    public static final String INVALID_UPDATE_HEADER_PROFILE            = "error.userset_invalid_update_profile";
//    public static final String USERSET_VALIDATION_SEARCH_API_NOT_ACCESSIBLE = "error.userset_validation_search_api_not_accessible";

    public static final String APIKEY_FILE_NOT_FOUND  = "error.userset_apikey_file_not_found";
    public static final String INVALID_TOKEN          = "error.userset_invalid_token";
    public static final String USER_NOT_AUTHORIZED    = "error.userset_user_not_authorized";
    public static final String TEST_USER_FORBIDDEN    = "error.userset_test_user_forbidden";
    public static final String CLIENT_NOT_AUTHORIZED  = "error.userset_client_not_authorized";

    public static final String USERSET_CONTAINS_NO_ITEMS      = "error.userset_contains_no_items";
    public static final String USERSET_ALREADY_DISABLED       = "error.userset_already_disabled";
    public static final String USERSET_MINIMAL_UPDATE_PROFILE = "error.userset_minimal_update_profile";
    public static final String USERSET_PROFILE_MINIMAL_ALLOWED = "error.userset_entity_minimal_profile";

    public static final String USER_SET_NOT_AVAILABLE         = "error.userset_not_available";
    
    public static final String USER_SET_OPERATION_NOT_ALLOWED = "error.userset_operation_not_allowed";
    public static final String ENTITY_USER_SET_NOT_FOUND = "error.no_entity_userset_found";
    public static final String ELEVATION_NOT_GENERATED = "error.no_elevation_generated";
    public static final String SEARCH_API_REQUEST_INVALID = "error.userset_validation_search_request_not_valid";
}
