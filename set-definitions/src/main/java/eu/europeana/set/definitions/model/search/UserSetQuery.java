package eu.europeana.set.definitions.model.search;

import eu.europeana.api.commons.definitions.search.Query;

public interface UserSetQuery extends Query{

    void setType(String type);

    String getType();

    void setVisibility(String visibility);

    String getVisibility();

    void setCreator(String creator);

    String getCreator();
    
}
