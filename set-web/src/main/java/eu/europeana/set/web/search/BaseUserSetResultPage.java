package eu.europeana.set.web.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.api.commons.definitions.search.result.impl.ResultsPageImpl;
import eu.europeana.api.commons.definitions.vocabulary.CommonLdConstants;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;

@JsonPropertyOrder({WebUserSetModelFields.ID,  WebUserSetFields.TYPE, WebUserSetFields.PART_OF, WebUserSetFields.ITEMS, WebUserSetFields.NEXT, WebUserSetFields.PREV})
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class BaseUserSetResultPage<T> extends ResultsPageImpl<T>{

    CollectionView partOf;
    
    public CollectionView getPartOf() {
        return partOf;
    }

    public void setPartOf(CollectionView partOf) {
        this.partOf = partOf;
    }

    public String getType() {
	return CommonLdConstants.RESULT_PAGE;
    }
    
    @Override
    @JsonProperty(WebUserSetModelFields.TOTAL)
//    @JsonIgnore
    public long getTotalInPage() {
        return super.getTotalInPage();
    }
 
    @Override
    @JsonIgnore
    public long getTotalInCollection() {
        return super.getTotalInCollection();
    }
    
    @Override
    @JsonIgnore
    public int getCurrentPage() {
        return super.getCurrentPage();
    }
    
    @Override
    @JsonIgnore
    public String getResultCollectionUri() {
        return super.getResultCollectionUri();
    }
    
    @Override
    @JsonIgnore
    public String getCollectionUri() {
        return super.getCollectionUri();
    }
    
    @Override
    @JsonProperty(WebUserSetModelFields.ID)
    public String getCurrentPageUri() {
        return super.getCurrentPageUri();
    }
    
    @Override
    @JsonProperty(WebUserSetFields.NEXT)
    public String getNextPageUri() {
        return super.getNextPageUri();
    }
    
    @Override
    @JsonProperty(WebUserSetFields.PREV)
    public String getPrevPageUri() {
        return super.getPrevPageUri();
    }

    
}
