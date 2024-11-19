package eu.europeana.set.definitions.model.vocabulary;

import java.util.List;

public interface UserSetProfile {

  List<String> getAliases();
  
  String name();
  
  String getProfileParamValue();
}
