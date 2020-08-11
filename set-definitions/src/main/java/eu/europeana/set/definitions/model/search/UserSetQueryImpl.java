package eu.europeana.set.definitions.model.search;

import eu.europeana.api.commons.definitions.search.impl.QueryImpl;

public class UserSetQueryImpl extends QueryImpl implements UserSetQuery{

    String creator;
    String visibility;
    String type;
    Boolean admin;
    String item;
    
    @Override
    public Boolean isAdmin() {
        return admin;
    }
    @Override
    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }
    @Override
    public String getItem() {
        return item;
    }
    @Override
    public void setItem(String item) {
        this.item = item;
    }
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
    
}
