package eu.europeana.set.web.model.search;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;

public class ItemDescriptionsResultPage extends BaseUserSetResultPage<String> {
  List<String> itemList;

  @JsonProperty(WebUserSetFields.ITEMS)
  @JsonRawValue
  public List<String> getItemList() {
      return itemList;
  }

  public void setItemList(List<String> itemList) {
      this.itemList = itemList;
  }
}
