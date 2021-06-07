package eu.europeana.set.web.model.search;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.annotation.JsonRawValue;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;

import java.util.List;

public class CollectionPage extends BaseUserSetResultPage<String>{

//    UserSet set;
    int startIndex;
    List<String> itemList;
    public static final String COLLECTION_PAGE = "CollectionPage";
	
    
    public CollectionPage(CollectionOverview partOf, int startIndex) {
	this.partOf = partOf;
	this.startIndex = startIndex;
	super.setType(COLLECTION_PAGE);
    }
    
    @JsonProperty(WebUserSetFields.START_INDEX)
    public int getStartIndex() {
        return startIndex;
    }
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
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
