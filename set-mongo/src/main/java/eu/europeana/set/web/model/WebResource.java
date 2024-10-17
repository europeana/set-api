package eu.europeana.set.web.model;

import static eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields.ID;
import static eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields.SOURCE;
import static eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields.THUMBNAIL;
import static eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields.TYPE;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import dev.morphia.annotations.Embedded;
import eu.europeana.set.definitions.model.BaseWebResource;

@Embedded
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ID, TYPE, SOURCE, THUMBNAIL})
public class WebResource extends BaseWebResource {

  public static final String TYPE = "WebResource";
  @JsonProperty(SOURCE)
  private String source;
  @JsonProperty(ID)
  private String id;
  @JsonProperty(THUMBNAIL)
  private String thumbnail;

  public WebResource() {
    super();
  }

  public WebResource(BaseWebResource copy) {
    super(copy);
  }

  public WebResource(String id, String source, String thumbnail) {
    super(id, source, thumbnail);
  }

  
  public void setThumbnail(String thumbnailParam) {
    super.setThumbnail(thumbnailParam);
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

    WebResource that = (WebResource) o;

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
