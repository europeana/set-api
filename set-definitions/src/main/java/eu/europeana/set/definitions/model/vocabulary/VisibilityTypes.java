package eu.europeana.set.definitions.model.vocabulary;

/**
 *	The following states cover different levels of visibility:
 *   
 *  private: only visible to the owner
 *  public: visible to the owner and anyone else that the owner shared with (also editors)
 *  published: visible to all users on Collections (can only be set by an editor)
 */
public enum VisibilityTypes {
    PRIVATE("private"), PUBLIC("public"), PUBLISHED("published");

    private String name;

    VisibilityTypes(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public static final boolean isValid(String visibility) {
	for (VisibilityTypes type : VisibilityTypes.values()) {
	    if(type.getName().equals(visibility)) {
		return true;
	    }
	}
	return false;
    }
}