package eu.europeana.set.definitions.model.vocabulary;

import java.util.Locale;

/**
 * This enumeration lists supported user set types
 * 
 * @author GrafR
 *
 */
public enum UserSetTypes implements JsonKeyword {

    COLLECTION("Collection"), BOOKMARKSFOLDER("BookmarkFolder");
//    	, ORDERED_COLLECTION_PAGE("OrderedCollectionPage")

    private String jsonValue;

    UserSetTypes(String jsonValue) {
	this.jsonValue = jsonValue;
    }

    /**
     * Identifying user set type by the json value. For user friendliness the
     * comparison is case insensitive
     * 
     * @param jsonValue
     * @return
     */
    public static UserSetTypes getByJsonValue(String jsonValue) {
	return valueOf(jsonValue.toUpperCase(Locale.ROOT));
    }

    @Override
    public String getJsonValue() {
	return jsonValue;
    }

    @Override
    public String toString() {
	return getJsonValue();
    }

    public static boolean isValid(String jsonValue) {
	// TODO: update to use valueOfN
	for (UserSetTypes type : UserSetTypes.values()) {
	    if (type.getJsonValue().equals(jsonValue)) {
		return true;
	    }
	}
	return false;
    }

}
