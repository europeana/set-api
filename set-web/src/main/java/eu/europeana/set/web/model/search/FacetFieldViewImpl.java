package eu.europeana.set.web.model.search;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.europeana.api.commons.definitions.search.FacetFieldView;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;

@JsonPropertyOrder({ WebUserSetModelFields.TYPE, WebUserSetFields.FIELD, WebUserSetFields.VALUES})
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class FacetFieldViewImpl implements FacetFieldView {

    @JsonProperty(WebUserSetFields.TYPE)
    private String type;

    @JsonProperty(WebUserSetFields.FIELD)
    private String field;

    @JsonProperty(WebUserSetFields.VALUES)
    private List<FacetValueResultPage> values;

    private Map<String, Long> valueCountMap;

    public FacetFieldViewImpl(String field, List<FacetValueResultPage> values, Map<String, Long> valueCountMap) {
        this.type = WebUserSetFields.FACET_TYPE;
        this.field = field;
        this.values = values;
        this.valueCountMap = valueCountMap;
    }

    @Override
    @JsonIgnore
    public String getName() {
        return field;
    }

    @Override
    @JsonIgnore
    public Map<String, Long> getValueCountMap() {
        return valueCountMap;
    }
}
