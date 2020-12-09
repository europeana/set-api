package eu.europeana.set.web.model.search;

import com.fasterxml.jackson.annotation.JsonInclude;

import eu.europeana.api.commons.definitions.vocabulary.CommonLdConstants;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ResultList extends CollectionPreview{
    
    public ResultList() {
        super();
    }

    public ResultList(String id, Long total, String first, String last) {
        super(id, total, first, last, CommonLdConstants.RESULT_LIST);
    }
}
