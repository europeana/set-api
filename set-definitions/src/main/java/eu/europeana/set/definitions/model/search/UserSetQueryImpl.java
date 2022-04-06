package eu.europeana.set.definitions.model.search;

import eu.europeana.api.commons.definitions.search.impl.QueryImpl;

public class UserSetQueryImpl extends QueryImpl implements UserSetQuery{

    String creator;
    String contributor;
    String subject;
    String visibility;
    String type;
    boolean admin;
    String item;
    String user;
    String setId;
    String text;
    String provider;
    
    @Override
    public String getUser() {
        return user;
    }

    @Override
    public void setUser(String user) {
        this.user = user;
    }
    @Override
    public boolean isAdmin() {
        return admin;
    }
    @Override
    public void setAdmin(boolean admin) {
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

    @Override
    public String getSetId() {
        return setId;
    }

    @Override
    public void setSetId(String setId) {
        this.setId = setId;
    }

    @Override
    public String getContributor() {
        return contributor;
    }

    @Override
    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void setProvider(String provider) {
      this.provider = provider;
    }

    @Override
    public String getProvider() {
      return provider;
    }
}
