package eu.europeana.api.set.integration.migrations;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import eu.europeana.set.client.UserSetApiClient;
import eu.europeana.set.client.config.ClientConfiguration;
import eu.europeana.set.client.exception.SetApiClientException;
import eu.europeana.set.client.web.WebUserSetApi;
import eu.europeana.set.definitions.model.UserSet;

public class GenerateDepictions {

  private static final Logger LOG = LogManager.getLogger(GenerateDepictions.class);

  public static void main(String args[]) throws Exception {

    ClientConfiguration clientConfig = new ClientConfiguration();
    UserSetApiClient apiClient = new UserSetApiClient(clientConfig);
    WebUserSetApi webUserSetApi = apiClient.getWebUserSetApi();

    List<String> ids = getSetIdsNoDepiction();

    for (String id : ids) {
      try {
        updateDepiction(webUserSetApi, id);
      } catch (SetApiClientException e) {
        LOG.error("Cannot update depiction for userset:  {}, error: {}", id, e.getMessage());
      }
    }
  }

  static List<String> getSetIdsNoDepiction() throws JSONException, IOException {
    InputStream idsStream =
        GenerateDepictions.class.getResourceAsStream("/migration/set_no_depiction_prod.txt");
    String content = new String(idsStream.readAllBytes());
    JSONArray objs = new JSONArray(content);
    List<String> ids = new ArrayList<String>(objs.length());
    for (int ix = 0; ix < objs.length(); ix++) {
      ids.add(objs.getJSONObject(ix).getString("identifier"));
    }
    return ids;
  }

  static void updateDepiction(WebUserSetApi webUserSetApi, String setIdentifier)
      throws SetApiClientException {
    Optional<UserSet> setOptional =
        webUserSetApi.getUserSet(setIdentifier, Optional.empty(), Optional.empty());
    if (setOptional.isEmpty()) {
      LOG.error("Cannot fetch userset:  {}", setIdentifier);
      
      return;
    }

    if (setOptional.get().getIsShownBy() != null) {
      String depiction = setOptional.get().getIsShownBy().getThumbnail();
      LOG.error("The set allready has a depiction: {} - {}", depiction);
    }

    List<String> items = List.of("http://data.europeana.eu/item/2020737/object_KUAS_2720630");

    // first add the test item, should generate isShownBy
    UserSet set = webUserSetApi.addItems(setIdentifier, items, null, null);
    if (set.getIsShownBy() == null || set.getIsShownBy().getThumbnail() == null) {
      LOG.error("Depiction not generated for userset: {} ", setIdentifier);
    } else {
      String thumbnail = set.getIsShownBy().getThumbnail();
      LOG.info("Generated depiction for set: {} - {}", setIdentifier, thumbnail);
    }
    // remove test item
    UserSet setItemsRestored = webUserSetApi.removeItems(setIdentifier, items, null);
    if (setItemsRestored.getTotal() + 1 != set.getTotal()
        || setItemsRestored.getTotal() != setOptional.get().getTotal()) {
      LOG.error("Item list was not restored correctly for userset: {}", setIdentifier);
    }
  }
}
