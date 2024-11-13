package eu.europeana.set.definitions.model.vocabulary;

import java.util.List;

public enum SetResourceProfile implements UserSetProfile{
  META(List.of(ProfileConstants.VALUE_PARAM_MINIMAL));
  
  List<String> aliases;

  SetResourceProfile(List<String> aliases) {
    this.aliases = aliases;
  }

  @Override
  public List<String> getAliases() {
    return aliases;
  }

  @Override
  public String getProfileParamValue() {
    return name();
  }
  
}
