package eu.europeana.set.definitions.model;

import java.util.Objects;

public class BaseWebResource {

  public static final String TYPE = "WebResource";
  private String source;
  private String id;
  private String thumbnail;

  public BaseWebResource() {
    super();
  }

  public BaseWebResource(BaseWebResource copy) {
    this.source = copy.getSource();
    this.id = copy.getId();
    this.thumbnail = copy.getThumbnail();
  }

  public BaseWebResource(String id, String source, String thumbnail) {
    this.id = id;
    this.source = source;
    this.thumbnail = thumbnail;
  }

  public String getId() {
    return id;
  }

  public String getSource() {
    return source;
  }

  public String getThumbnail() {
    return thumbnail;
  }

  public String getType() {
    return TYPE;
  }

  public void setThumbnail(String thumbnailParam) {
    thumbnail = thumbnailParam;
  }

  public void setSource(String sourceParam) {
    source = sourceParam;
  }

  public void setId(String idParam) {
    id = idParam;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BaseWebResource that = (BaseWebResource) o;

    return Objects.equals(source, that.getSource()) 
        && id.equals(that.getId()) 
        && Objects.equals(thumbnail, that.getThumbnail());
  }

  public int hashCode() {
    int result = (id == null) ? 0 : id.hashCode();
    result += (thumbnail == null) ? 0 : thumbnail.hashCode();
    result += (source == null) ? 0 : source.hashCode();
    return result;
  }

}
