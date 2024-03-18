package eu.europeana.set.web.config;

/**
 * Abstract class to keep an inventory of bean names
 */
public abstract class BeanNames {
  public static final String BEAN_SET_MONGO_STORE = "set_db_morphia_datastore_set";
  public static final String BEAN_SET_PERSITENCE_SERVICE = "set_db_setService";
  public static final String BEAN_I18N_SERVICE = "i18nService";
  
  private BeanNames() {}

  }
