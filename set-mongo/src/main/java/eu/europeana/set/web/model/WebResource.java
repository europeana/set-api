package eu.europeana.set.web.model;

import static eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields.ID;
import static eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields.SOURCE;
import static eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields.THUMBNAIL;
import static eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields.TYPE;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import dev.morphia.annotations.Embedded;
import eu.europeana.set.definitions.model.BaseWebResource;

@Embedded
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ID, TYPE, SOURCE, THUMBNAIL})
public class WebResource extends BaseWebResource {

  public static final String TYPE = "WebResource";
  
  public WebResource() {
    super();
  }

  public WebResource(BaseWebResource copy) {
    super(copy);
  }

  public WebResource(String id, String source, String thumbnail) {
    super(id, source, thumbnail);
  }

  @JsonSetter(THUMBNAIL)
  public void setThumbnail(String thumbnailParam) {
    super.setThumbnail(thumbnailParam);
  }

  @JsonSetter(SOURCE)
  public void setSource(String sourceParam) {
   super.setSource(sourceParam);;
  }

  @JsonSetter(ID)
  public void setId(String idParam) {
    super.setId(idParam);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    WebResource that = (WebResource) o;

    return Objects.equals(getSource(), that.getSource()) 
        && Objects.equals(getId(), that.getId()) 
        && Objects.equals(getThumbnail(), that.getThumbnail());
  }

  public int hashCode() {
    int result = (getId() == null) ? 0 : getId().hashCode();
    result += (getThumbnail() == null) ? 0 : getThumbnail().hashCode();
    result += (getSource() == null) ? 0 : getSource().hashCode();
    return result;
  }

}
