package eu.europeana.set.definitions.model;

import java.util.Date;
import java.util.List;
import java.util.Map;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.impl.Provider;

public interface UserSet extends PageInfo {

    String getType();

    void setType(String type);

    String getCollectionType();

    void setCollectionType(String collectionType);

    String getVisibility();

    void setVisibility(String visibility);

    Map<String, String> getTitle();

    void setTitle(Map<String, String> title);

    Map<String, String> getDescription();

    void setDescription(Map<String, String> setDescription);

    List<String> getSubject();

    void setSubject(List<String> subject);

    int getPinned();

    void setPinned(int pinned);

    List<String> getContributor();

    void setContributor(List<String> contributorList);

    Agent getCreator();

    void setCreator(Agent creator);

    Date getCreated();

    void setCreated(Date created);

    Date getModified();

    void setModified(Date modified);

    Date getIssued();

    void setIssued(Date issued);

    List<String> getItems();

    void setItems(List<String> items);

    public String getIdentifier();

    public void setIdentifier(String sequenceIdentifier);

    /**
     * @return Holds a search request to the User Set API (complete URL pointing to
     *         production). To reduce complexity on the API, the exhaustive listing
     *         could also be expressed as a query.
     */
    String getIsDefinedBy();

    void setIsDefinedBy(String query);

    /**
     * This method detects if it is open or closed set
     * 
     * @return true if it is an open set
     */
    public boolean isOpenSet();

    public boolean isBookmarksFolder();

    public boolean isEntityBestItemsSet();
    
    public boolean isCollection();
    
    public boolean isGallery();

    public boolean isPrivate();

    public boolean isPublic();

    public boolean isPublished();

    /**
     * set base URL for set id (URI)
     * @param baseUrl
     */
    public void setBaseUrl(String baseUrl);

    void setProvider(Provider provider);

    Provider getProvider();

    boolean hasItem(String itemId);
}