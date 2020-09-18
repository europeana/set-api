package eu.europeana.set.definitions.model.vocabulary;

import java.util.Locale;

/**
 *	The following states cover different levels of visibility:
 *   
 *  private: only visible to the owner
 *  public: visible to the owner and anyone else that the owner shared with (also editors)
 *  published: visible to all users on Collections (can only be set by an editor)
 */
public enum VisibilityTypes {
    PRIVATE("private"), PUBLIC("public"), PUBLISHED("published");

    private String jsonValue;

    VisibilityTypes(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    public String getJsonValue() {
        return jsonValue;
    }
    
    public static final boolean isValid(String visibility) {
	//TODO: update to use valueOfN
	for (VisibilityTypes type : VisibilityTypes.values()) {
	    if(type.getJsonValue().equals(visibility)) {
		return true;
	    }
	}
	return false;
    }
    
    public static final VisibilityTypes getByJsonValue(String visibility) {
	return VisibilityTypes.valueOf(visibility.toUpperCase(Locale.ROOT));
    }
}