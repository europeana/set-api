package eu.europeana.set.utils.serialization;

import java.io.IOException;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.impl.BaseUserSet;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;


@SuppressWarnings("serial")
public class UserSetDeserializer extends JsonDeserializer<UserSet> {
//	public class UserSetDeserializer extends StdDeserializer<UserSet> {
	
    UserSetUtils userSetUtils = new UserSetUtils();

    public UserSetUtils getUserSetUtils() {
      return userSetUtils;
    }
    
	public UserSetDeserializer() {
//		super(UserSet.class);		
	}

	@Override
	public UserSet deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		
//		ObjectMapper mapper = (ObjectMapper) jp.getCodec();
//		ObjectNode root = (ObjectNode) mapper.readTree(jp);
//		Class<? extends UserSet> realClass = null;
		
        JsonNode node = jp.getCodec().readTree(jp);     
//		return mapper.readValue(node, BaseUserSet.class);
        try {       
            String context = node.get(WebUserSetFields.CONTEXT_FIELD).asText();
            String id = node.get(WebUserSetFields.IDENTIFIER).asText();
            String type = node.get(WebUserSetFields.TYPE).asText();           
            String description = node.get(WebUserSetFields.DESCRIPTION).asText();
            String title = node.get(WebUserSetFields.TITLE).asText();
//            String items = node.get(WebUserSetFields.ITEMS).asText();
            UserSet userSet = new BaseUserSet();
//            UserSet userSet = new WebUserSetImpl();
//            userSet.setId(title);
            
            userSet.setTitle(getUserSetUtils().createMap(Locale.ENGLISH.getLanguage(), title));
            userSet.setType(type);
            userSet.setDescription(getUserSetUtils().createMap(Locale.ENGLISH.getLanguage(), description));
            return userSet;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e);
        }		
		/*
		Iterator<Entry<String, JsonNode>> elementsIterator = root.getFields();
		while (elementsIterator.hasNext()) {
			Entry<String, JsonNode> element = elementsIterator.next();
			if (ModelConst.TYPE.equals(element.getKey())) {
				String typeValue = element.getValue().getTextValue();
//				String typeValue = TypeUtils.getEuTypeFromTypeArrayStatic(element.getValue().getTextValue());
				realClass = UserSetObjectFactory.getInstance()
						.getClassForType(typeValue);
//				.getUserSetClass(element.getValue().getTextValue());
				break;
			}
		}
		
		if (realClass == null)
			return null;
		
		return mapper.readValue(root, realClass);
		*/
	}
	
	
}
