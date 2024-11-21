package eu.europeana.set.web.service.controller.jsonld;

import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;

public class WeUserSetRequestUtils {

  public static boolean isPinnRequest(String position) {
    return WebUserSetModelFields.PINNED_POSITION.equals(position);
  }
}
