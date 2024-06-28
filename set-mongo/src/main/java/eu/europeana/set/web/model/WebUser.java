package eu.europeana.set.web.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europeana.set.definitions.model.agent.impl.Person;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class WebUser extends Person {

  public WebUser() {
    super();
  }

  @Override
  @JsonProperty(WebUserSetModelFields.TYPE)
  public String getType() {
    return super.getType();
  }

  @Override
  @JsonProperty(WebUserSetModelFields.ID)
  public String getHttpUrl() {
    return super.getHttpUrl();
  }

  @Override
  @JsonProperty(WebUserSetModelFields.NICKNAME)
  public String getNickname() {
    return super.getNickname();
  }
}
