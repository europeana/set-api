package eu.europeana.set.definitions.model.search;

import java.util.List;
import eu.europeana.api.commons_sb3.definitions.search.Query;

public interface UserSetQuery extends Query{

    void setType(List<String> type);

    List<String> getType();

    void setVisibility(List<String> visibility);

    List<String> getVisibility();

    void setCreator(List<String> creator);

    List<String> getCreator();

    void setItem(List<String> item);

    List<String> getItem();

    void setAdmin(boolean admin);

    boolean isAdmin();

    void setUser(String user);

    String getUser();

    List<String> getSetId();

    void setSetId(List<String> setId);

    void setContributor(List<String> contributor);

    List<String> getContributor();

    void setSubject(List<String> subject);

    List<String> getSubject();

    void setText(String text);

    String getText();
    
    void setProvider(List<String> provider);

    List<String> getProvider();
    
    void setTitleLang(String lang);

    String getTitleLang();

    void setCollectionType(List<String> collectionType);

    List<String> getCollectionType();
    
}