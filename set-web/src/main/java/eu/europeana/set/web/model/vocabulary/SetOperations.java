package eu.europeana.set.web.model.vocabulary;

import eu.europeana.api.commons.web.model.vocabulary.Operations;

public interface SetOperations extends Operations {

  //publisher
  public static final String PUBLISH = "publish";
  
  //admin
  public static final String WRITE_LOCK = "write_lock";
  public static final String WRITE_UNLOCK = "write_unlock";
  public static final String ADMIN_REINDEX = "admin_reindex"; 

}
