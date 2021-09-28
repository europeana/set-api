package eu.europeana.set.web.model.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.api.commons.definitions.search.FacetFieldView;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;

@JsonPropertyOrder({ WebUserSetModelFields.TYPE, WebUserSetFields.FIELD, WebUserSetFields.VALUES})
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
//TODO: add extends  commons.FacetFieldViewImpl, only after removing dependency on solr
public class FacetFieldViewImpl implements FacetFieldView {

    private String type = WebUserSetFields.FACET_TYPE;
    private String field;
    @JsonIgnore
    private Map<String, Long> valueCountMap;
    private List<FacetValue> facetValues;

    public FacetFieldViewImpl(String field, Map<String, Long> valueCountMap) {
        this.field = field;
        this.valueCountMap = valueCountMap;
	if (valueCountMap != null && !valueCountMap.isEmpty()) {
	    facetValues = new ArrayList<FacetValue>();
	    for (Map.Entry<String, Long> entry : valueCountMap.entrySet()) {
		facetValues.add(new FacetValue(entry.getKey(), entry.getValue()));
	    }
	}        
    }

    @Override
    @JsonGetter(WebUserSetFields.FIELD)
    public String getName() {
        return field;
    }

    @Override
    public Map<String, Long> getValueCountMap() {
        return valueCountMap;
    }

    @JsonGetter(WebUserSetFields.TYPE)
    String getType() {
        return type;
    }
    
    @JsonGetter(WebUserSetFields.VALUES)
    public List<FacetValue> facetValues() {
    	return facetValues;
    }
}

