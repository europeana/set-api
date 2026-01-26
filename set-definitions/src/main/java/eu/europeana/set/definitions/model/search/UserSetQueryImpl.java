package eu.europeana.set.definitions.model.search;

import java.util.List;
import eu.europeana.api.commons_sb3.definitions.search.impl.QueryImpl;

public class UserSetQueryImpl extends QueryImpl implements UserSetQuery{

    List<String> creator;
    List<String> contributor;
    List<String> subject;
    List<String> visibility;
    List<String> type;
    boolean admin;
    List<String> item;
    String user;
    List<String> setId;
    String text;
    List<String> provider;
    String titleLang;
    List<String> collectionType;
    
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
    public List<String> getItem() {
        return item;
    }
    @Override
    public void setItem(List<String> item) {
        this.item = item;
    }
    @Override
    public List<String> getCreator() {
        return creator;
    }
    @Override
    public void setCreator(List<String> creator) {
        this.creator = creator;
    }
    @Override
    public List<String> getVisibility() {
        return visibility;
    }
    @Override
    public void setVisibility(List<String> visibility) {
        this.visibility = visibility;
    }
    @Override
    public List<String> getType() {
        return type;
    }
    @Override
    public void setType(List<String> type) {
        this.type = type;
    }

    @Override
    public List<String> getSetId() {
        return setId;
    }

    @Override
    public void setSetId(List<String> setId) {
        this.setId = setId;
    }

    @Override
    public List<String> getContributor() {
        return contributor;
    }

    @Override
    public void setContributor(List<String> contributor) {
        this.contributor = contributor;
    }

    @Override
    public List<String> getSubject() {
        return subject;
    }

    @Override
    public void setSubject(List<String> subject) {
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
    public void setProvider(List<String> provider) {
      this.provider = provider;
    }

    @Override
    public List<String> getProvider() {
      return provider;
    }

    @Override
    public void setTitleLang(String lang) {
      this.titleLang = lang;
    }

    @Override
    public String getTitleLang() {
      return titleLang;
    }

    @Override
    public List<String> getCollectionType() {
      return collectionType;
    }

    @Override
    public void setCollectionType(List<String> collectionType) {
      this.collectionType = collectionType;
    }
}