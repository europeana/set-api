package eu.europeana.set.web.model.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europeana.api.commons.definitions.vocabulary.CommonLdConstants;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;

public class CollectionPage extends BaseUserSetResultPage<String>{

    UserSet set;
    int startIndex;

    public CollectionPage(UserSet set, CollectionOverview partOf, int startIndex) {
    this.set = set;
	this.partOf = partOf;
	this.startIndex = startIndex;
	super.setType(CommonLdConstants.COLLECTION_PAGE);
    }

    @JsonProperty(WebUserSetFields.START_INDEX)
    public int getStartIndex() {
        return startIndex;
    }
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }
}
