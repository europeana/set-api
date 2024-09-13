package eu.europeana.set.web.model;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonSetter;
import dev.morphia.annotations.Transient;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.impl.Provider;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.mongo.model.PersistentUserSetImpl;

@JsonPropertyOrder({WebUserSetModelFields.ID, WebUserSetModelFields.TYPE,
    WebUserSetModelFields.COLLECTION_TYPE, WebUserSetModelFields.TITLE,
    WebUserSetModelFields.SUBJECT, WebUserSetFields.DESCRIPTION, WebUserSetModelFields.VISIBILITY,
    WebUserSetModelFields.IS_DEFINED_BY, WebUserSetModelFields.PINNED, WebUserSetModelFields.ITEMS,
    WebUserSetModelFields.CREATOR, WebUserSetModelFields.CONTRIBUTOR,
    WebUserSetModelFields.PROVIDER, WebUserSetModelFields.CREATED, WebUserSetModelFields.MODIFIED,
    WebUserSetModelFields.TOTAL, WebUserSetFields.NEXT, WebUserSetFields.PREV})
@JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebUserSetImpl extends PersistentUserSetImpl {

  @Transient
  List<String> serializedItems;

  // do not store
  // used only for serialization purposes, must be overwritten with correct value in service
  @Transient
  String baseUrl = "/set/";

  @Override
  @JsonProperty(WebUserSetFields.ITEMS)
  public void setItems(List<String> items) {
    super.setItems(items);
  }

  @JsonProperty(WebUserSetFields.ITEMS)
  @JsonRawValue
  public List<String> getSerializedItems() {
    return serializedItems;
  }

  @JsonIgnore // use serializedItems for serialization
  public List<String> getItems() {
    return super.getItems();
  }

  @JsonIgnore // use setItems for deserialization
  public void setSerializedItems(List<String> serializedItems) {
    this.serializedItems = serializedItems;
  }

  @JsonIgnore
  public String getIdentifier() {
    return super.getIdentifier();
  }

  @Override
  @JsonProperty(WebUserSetModelFields.TYPE)
  public void setType(String type) {
    super.setType(type);
  }

  @Override
  @JsonProperty(WebUserSetModelFields.COLLECTION_TYPE)
  public void setCollectionType(String collType) {
    super.setCollectionType(collType);
  }

  @JsonProperty(WebUserSetModelFields.VISIBILITY)
  public void setVisibility(String visibility) {
    super.setVisibility(visibility);
  }

  @JsonProperty(WebUserSetFields.TITLE)
  public Map<String, String> getTitle() {
    return super.getTitle();
  }

  @JsonProperty(WebUserSetFields.DESCRIPTION)
  public Map<String, String> getDescription() {
    return super.getDescription();
  }

  @Override
  @JsonProperty(WebUserSetFields.SUBJECT)
  public List<String> getSubject() {
    return super.getSubject();
  }

  @Override
  @JsonGetter(WebUserSetFields.PINNED)
  @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = PositiveIntegerFilter.class)
  public int getPinned() {
    if (isEntityBestItemsSet()) {
      return super.getPinned();
    }
    return -1;
  }

  @Override
  @JsonProperty(WebUserSetFields.CONTRIBUTOR)
  public List<String> getContributor() {
    return super.getContributor();
  }

  @JsonProperty(value = WebUserSetModelFields.TOTAL, access = JsonProperty.Access.READ_ONLY)
  @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = PositiveIntegerFilter.class)
  public int getTotal() {
    return super.getTotal();
  }

  @JsonProperty(WebUserSetModelFields.IS_DEFINED_BY)
  public String getIsDefinedBy() {
    return super.getIsDefinedBy();
  }


  // TODO: change to use of getter and setter, or ingore unknown properties
  @JsonProperty(WebUserSetFields.CONTEXT_FIELD)
  public void setContext(String context) {
    // do nothing, just to allow context in input
  }

  @JsonProperty(WebUserSetFields.CREATOR)
  public Agent getCreator() {
    return super.getCreator();
  }

  @Override
  @JsonIgnore // creator is set by the system
  public void setCreator(Agent creator) {
    super.setCreator(creator);
  }

  @Override
  @JsonGetter(WebUserSetFields.PROVIDER)
  public Provider getProvider() {
    return super.getProvider();
  }

  @JsonSetter(WebUserSetFields.PROVIDER)
  public void setProvider(WebProvider provider) {
    super.setProvider(provider);
  }

  @JsonIgnore
  public boolean isUgc() {
    return super.isUgc();
  }

  @JsonProperty(WebUserSetFields.FIRST)
  public String getFirst() {
    return super.getFirst();
  }

  @JsonProperty(WebUserSetFields.LAST)
  public String getLast() {
    return super.getLast();
  }

  @JsonIgnore
  public int getCollectionPage() {
    return super.getCollectionPage();
  }

  /**
   * This method presents Id as URL.
   *
   * @return string presenting ID as URL
   */
  @JsonGetter(WebUserSetModelFields.ID)
  public String getId() {
    return UserSetUtils.buildUserSetId(getBaseUrl(), super.getIdentifier());
  }

  // TODO: switch to jsongetter to ignore deserialization of id field
  public void setId(String id) {
    // this method is defined just for avoiding parse errors in update method, the
    // URI is ignored as the identifier from URL is used
  }

  @Override
  public String toString() {
    StringBuilder resBuilder = new StringBuilder();
    resBuilder.append("WebUserSet [");
    if (getTitle() != null && StringUtils.isNotEmpty(getTitle().toString())) {
      resBuilder.append("Title: ");
      resBuilder.append(getTitle());
    }
    if (StringUtils.isNotEmpty(getIdentifier())) {
      resBuilder.append(", Identifier: ");
      resBuilder.append(getIdentifier());
    }
    if (StringUtils.isNotEmpty(Integer.toString(getTotal()))) {
      resBuilder.append(", Total items: ");
      resBuilder.append(getTotal());
    }
    if (!getItems().isEmpty()) {
      resBuilder.append(", Items: ");
      resBuilder.append(getItems().size());
    }
    resBuilder.append("]");
    return resBuilder.toString();
  }

  @Override
  @JsonIgnore
  public ObjectId getObjectId() {
    return super.getObjectId();
  }

  @Override
  @JsonIgnore
  public boolean isOpenSet() {
    return super.isOpenSet();
  }

  @Override
  @JsonIgnore
  public boolean isPrivate() {
    return super.isPrivate();
  }

  @Override
  @JsonIgnore
  public boolean isPublic() {
    return super.isPublic();
  }

  @Override
  @JsonIgnore
  public boolean isPublished() {
    return super.isPublished();
  }

  @Override
  @JsonIgnore
  public boolean isEntityBestItemsSet() {
    return super.isEntityBestItemsSet();
  }

  @Override
  @JsonIgnore
  public boolean isBookmarksFolder() {
    return super.isBookmarksFolder();
  }

  @Override
  @JsonIgnore
  public boolean isCollection() {
    return super.isCollection();
  }

  @Override
  @JsonIgnore
  public boolean isGallery() {
    return super.isGallery();
  }

  @JsonIgnore
  String getBaseUrl() {
    return baseUrl;
  }

  @JsonIgnore
  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }
}
