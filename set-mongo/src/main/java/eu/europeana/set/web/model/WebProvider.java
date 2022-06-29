package eu.europeana.set.web.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europeana.set.definitions.model.impl.Provider;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class WebProvider extends Provider {

  public WebProvider() {
    super();
  }

  @Override
  @JsonProperty(WebUserSetModelFields.NAME)
  public String getName() {
    return super.getName();
  }

  @Override
  @JsonProperty(WebUserSetModelFields.ID)
  public String getId() {
    return super.getId();
  }
}
