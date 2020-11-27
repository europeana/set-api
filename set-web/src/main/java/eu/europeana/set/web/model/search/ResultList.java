package eu.europeana.set.web.model.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.api.commons.definitions.vocabulary.CommonLdConstants;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;

@JsonPropertyOrder({ WebUserSetModelFields.ID, WebUserSetModelFields.TYPE, WebUserSetModelFields.TOTAL,
	WebUserSetFields.FIRST, WebUserSetFields.LAST})
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ResultList extends CollectionPreview{
    
    public ResultList() {
        super();
    }

    public ResultList(String id, Long total, String first, String last) {
        super(id, total, first, last, CommonLdConstants.RESULT_LIST);
    }
}
