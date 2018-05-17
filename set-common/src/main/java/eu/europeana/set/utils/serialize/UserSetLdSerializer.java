package eu.europeana.set.utils.serialize;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
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
		
	public String serialize(UserSet userSet) throws IOException {
		
		UserSet extUserSet = getUserSetUtils().fillPagination(userSet);
		
		mapper.registerModule(new JsonldModule()); 
		JsonldResourceBuilder<UserSet> jsonResourceBuilder = JsonldResource.Builder.create();
		jsonResourceBuilder.context(WebUserSetFields.CONTEXT);
		String jsonString = mapper.writer().writeValueAsString(jsonResourceBuilder.build(extUserSet));
		return jsonString;
	}

}
