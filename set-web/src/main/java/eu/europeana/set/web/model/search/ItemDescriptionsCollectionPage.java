package eu.europeana.set.web.model.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;

import java.util.List;

public class ItemDescriptionsCollectionPage extends CollectionPage {

    List<String> itemList;

    public ItemDescriptionsCollectionPage(CollectionOverview partOf, int startIndex) {
        super(partOf, startIndex);
    }

    @JsonProperty(WebUserSetFields.ITEMS)
    @JsonRawValue
    public List<String> getItemList() {
        return itemList;
    }

    public void setItemList(List<String> itemList) {
        this.itemList = itemList;
    }
}
