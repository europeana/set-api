package eu.europeana.set.web.search;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.stats.model.Metric;
import eu.europeana.set.web.model.search.BaseUserSetResultPage;
import eu.europeana.set.web.model.search.CollectionPage;
import ioinformarics.oss.jackson.module.jsonld.JsonldModule;
import ioinformarics.oss.jackson.module.jsonld.JsonldResource;
import ioinformarics.oss.jackson.module.jsonld.JsonldResourceBuilder;

public class UserSetLdSerializer {

    UserSetUtils userSetUtils = new UserSetUtils();
    ObjectMapper mapper = new ObjectMapper();
    JsonldResourceBuilder<UserSet> userSetResourceBuilder;
    JsonldResourceBuilder<BaseUserSetResultPage<?>> resultPageResourceBuilder;
    JsonldResourceBuilder<CollectionPage> collectionPageResourceBuilder;

    public UserSetUtils getUserSetUtils() {
	return userSetUtils;
    }

    public UserSetLdSerializer() {
	SimpleDateFormat df = new SimpleDateFormat(WebUserSetFields.SET_DATE_FORMAT, Locale.ENGLISH);
	mapper.setDateFormat(df);
    }

    /**
     * This method provides full serialization of a user set
     * 
     * @param userSet
     * @return full user set view
     * @throws IOException
     */
    public String serialize(UserSet userSet) throws IOException {

	mapper.registerModule(new JsonldModule());
	return mapper.writer().writeValueAsString(getUserSetResourceBuilder().build(userSet));
    }

    /**
     * This method provides full serialization of a result page (search results)
     * 
     * @param resultsPage
     * @return full user set view
     * @throws IOException
     */
    public String serialize(BaseUserSetResultPage<?> resultsPage) throws IOException {

	mapper.registerModule(new JsonldModule());
	return mapper.writer().writeValueAsString(getResultPageResourceBuilder().build(resultsPage));
    }

    /**
     * This method provides full serialization of a Metric View (Usage stats results)
     *
     * @param metricData
     * @return full metric view
     * @throws IOException
     */
    public String serialize(Metric metricData) throws IOException {
        mapper.registerModule(new JsonldModule());
        return mapper.writer().writeValueAsString(metricData);
    }

    /**
     * This method provides full serialization of a CollectionPage
     * 
     * @param itemPage
     * @return full user set view
     * @throws IOException
     */
    public String serialize(CollectionPage itemPage) throws IOException {

	mapper.registerModule(new JsonldModule());
	return mapper.writer().writeValueAsString(getCollectionPageResourceBuilder().build(itemPage));
    }

    private JsonldResourceBuilder<CollectionPage> getCollectionPageResourceBuilder() {
	if (collectionPageResourceBuilder == null) {
	    collectionPageResourceBuilder = JsonldResource.Builder.create();
	    collectionPageResourceBuilder.context(WebUserSetFields.CONTEXT);
	}
	return collectionPageResourceBuilder;
    }

    public JsonldResourceBuilder<UserSet> getUserSetResourceBuilder() {
	if (userSetResourceBuilder == null) {
	    userSetResourceBuilder = JsonldResource.Builder.create();
	    userSetResourceBuilder.context(WebUserSetFields.CONTEXT);
	}
	return userSetResourceBuilder;
    }

    public JsonldResourceBuilder<BaseUserSetResultPage<?>> getResultPageResourceBuilder() {
	if (resultPageResourceBuilder == null) {
	    resultPageResourceBuilder = JsonldResource.Builder.create();
	    resultPageResourceBuilder.context(WebUserSetFields.CONTEXT);
	}
	return resultPageResourceBuilder;
    }

}
