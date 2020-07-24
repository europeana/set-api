package eu.europeana.set.definitions.model.search;

import eu.europeana.api.commons.definitions.search.impl.QueryImpl;

public class UserSetQueryImpl extends QueryImpl implements UserSetQuery{

    String creator;
    @Override
    public String getCreator() {
        return creator;
    }
    @Override
    public void setCreator(String creator) {
        this.creator = creator;
    }
    @Override
    public String getVisibility() {
        return visibility;
    }
    @Override
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
    @Override
    public String getType() {
        return type;
    }
    @Override
    public void setType(String type) {
        this.type = type;
    }
    String visibility;
    String type;
    
}
