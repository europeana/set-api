package eu.europeana.set.web.search;

import java.io.IOException;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.view.ItemInsertView;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import ioinformarics.oss.jackson.module.jsonld.JsonldModule;
import ioinformarics.oss.jackson.module.jsonld.JsonldResource;
import ioinformarics.oss.jackson.module.jsonld.JsonldResourceBuilder;


public class UserSetLdSerializer { 

    UserSetUtils userSetUtils = new UserSetUtils();

    public UserSetUtils getUserSetUtils() {
      return userSetUtils;
    }
    
	ObjectMapper mapper = new ObjectMapper(); 
		
	public UserSetLdSerializer() {
		SimpleDateFormat df = new SimpleDateFormat(WebUserSetFields.SET_DATE_FORMAT);
		mapper.setDateFormat(df);
	}
	
	/**
	 * This method provides full serialization of a user set
	 * @param userSet
	 * @return full user set view
	 * @throws IOException
	 */
	public String serialize(UserSet userSet) throws IOException {
		
		mapper.registerModule(new JsonldModule()); 
		JsonldResourceBuilder<UserSet> jsonResourceBuilder = JsonldResource.Builder.create();
		jsonResourceBuilder.context(WebUserSetFields.CONTEXT);
		JsonldResource resource = jsonResourceBuilder.build(userSet);
		String jsonString = mapper.writer().writeValueAsString(resource);
		return jsonString;
	}

	
	/**
	 * This method provides full serialization of a user set
	 * @param userSet
	 * @return full user set view
	 * @throws IOException
	 */
	public String serialize(BaseUserSetResultPage<?> resultsPage) throws IOException {
		
		mapper.registerModule(new JsonldModule()); 
		JsonldResourceBuilder<BaseUserSetResultPage<?>> jsonResourceBuilder = JsonldResource.Builder.create();
		jsonResourceBuilder.context(WebUserSetFields.CONTEXT);
		String jsonString = mapper.writer().writeValueAsString(jsonResourceBuilder.build(resultsPage));
		return jsonString;
	}
	
	/**
	 * This method provides response for item insert request.
	 * @param userSet
	 * @return specific view for user set after item inserting
	 * @throws IOException
	 */
	public String serialize(ItemInsertView userSet) throws IOException {		
		mapper.registerModule(new JsonldModule()); 
		JsonldResourceBuilder<ItemInsertView> jsonResourceBuilder = JsonldResource.Builder.create();
		String jsonString = mapper.writer().writeValueAsString(jsonResourceBuilder.build(userSet));
		return jsonString;
	}

}
