package eu.europeana.set.web.search;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.api.commons_sb3.definitions.statistics.set.SetMetric;
import eu.europeana.api.commons_sb3.definitions.utils.DateUtils;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.search.SearchApiRequest;
import eu.europeana.set.web.model.search.BaseUserSetResultPage;
import eu.europeana.set.web.model.search.CollectionPage;
import ioinformarics.oss.jackson.module.jsonld.JsonldModule;
import ioinformarics.oss.jackson.module.jsonld.JsonldResource;
import ioinformarics.oss.jackson.module.jsonld.JsonldResourceBuilder;

public class UserSetLdSerializer {

  ObjectMapper mapper = new ObjectMapper();
  JsonldResourceBuilder<UserSet> userSetResourceBuilder;
  JsonldResourceBuilder<BaseUserSetResultPage<?>> resultPageResourceBuilder;
  JsonldResourceBuilder<CollectionPage> collectionPageResourceBuilder;

  public UserSetLdSerializer() {
    SimpleDateFormat df = new SimpleDateFormat(DateUtils.DATE_FORMAT, Locale.ENGLISH);
    mapper.setDateFormat(df);
  }

  /**
   * This method provides full serialization of a user set
   * 
   * @param userSet to serialize
   * @return full user set view
   * @throws EuropeanaApiException if serialization fails
   */
  public String serialize(UserSet userSet) throws EuropeanaApiException {
    try {
      mapper.registerModule(new JsonldModule());
      return mapper.writer().writeValueAsString(getUserSetResourceBuilder().build(userSet));
    } catch (JsonProcessingException e) {
      throw new EuropeanaApiException("Error serialising user set", e);
    }
  }

  /**
   * This method provides full serialization of a user set
   * 
   * @param obj the domain object to serialize
   * @return full user set view
   * @throws EuropeanaApiException if serialization fails
   */
  public String serializeNonLd(Object obj) throws EuropeanaApiException {
    try {
      return mapper.writer().writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new EuropeanaApiException("Error serialising object", e);
    }
  }
  
  /**
   * This method provides full serialization of a result page (search results)
   * 
   * @param resultsPage to serialize
   * @return full user set view
   * @throws EuropeanaApiException if serialization fails
   */
  public String serialize(BaseUserSetResultPage<?> resultsPage) throws EuropeanaApiException {
    try {
      mapper.registerModule(new JsonldModule());
      return mapper.writer().writeValueAsString(getResultPageResourceBuilder().build(resultsPage));
    } catch (JsonProcessingException e) {
      throw new EuropeanaApiException("Error serialising BaseUserSetResultPage", e);
    }
  }

  /**
   * This method provides full serialization of a Metric View (Usage stats results)
   *
   * @param metricData
   * @return full metric view
   * @throws EuropeanaApiException if serialization fails
   */
  public String serialize(SetMetric metricData) throws EuropeanaApiException {
    try {
      mapper.registerModule(new JsonldModule());
      return mapper.writer().writeValueAsString(metricData);
    } catch (JsonProcessingException e) {
      throw new EuropeanaApiException("Error serailising the Set metric" +e.getMessage() ,e );
    }

  }

  /**
   * This method provides full serialization of a searchApiRequest
   *
   * @param searchApiRequest request to serialize
   * @return json String of searchApiRequest
   * @throws IOException if serialization fails
   */
  public String serialize(SearchApiRequest searchApiRequest) throws IOException {
    mapper.registerModule(new JsonldModule());
    return mapper.writer().writeValueAsString(searchApiRequest);
  }

  /**
   * This method provides full serialization of a CollectionPage
   * 
   * @param itemPage the page to serialize
   * @return full user set view
   * @throws IOException if serialization fails
   */
  public String serialize(CollectionPage itemPage) throws IOException {

    mapper.registerModule(new JsonldModule());
    return mapper.writer().writeValueAsString(getCollectionPageResourceBuilder().build(itemPage));
  }

  private JsonldResourceBuilder<CollectionPage> getCollectionPageResourceBuilder() {
    if (collectionPageResourceBuilder == null) {
      collectionPageResourceBuilder = JsonldResource.Builder.create();
      collectionPageResourceBuilder.context(WebUserSetFields.CONTEXT);
    }
    return collectionPageResourceBuilder;
  }

  public JsonldResourceBuilder<UserSet> getUserSetResourceBuilder() {
    if (userSetResourceBuilder == null) {
      userSetResourceBuilder = JsonldResource.Builder.create();
      userSetResourceBuilder.context(WebUserSetFields.CONTEXT);
    }
    return userSetResourceBuilder;
  }

  public JsonldResourceBuilder<BaseUserSetResultPage<?>> getResultPageResourceBuilder() {
    if (resultPageResourceBuilder == null) {
      resultPageResourceBuilder = JsonldResource.Builder.create();
      resultPageResourceBuilder.context(WebUserSetFields.CONTEXT);
    }
    return resultPageResourceBuilder;
  }

}