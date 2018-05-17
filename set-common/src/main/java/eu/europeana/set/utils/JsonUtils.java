package eu.europeana.set.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

import eu.europeana.set.definitions.exception.UserSetInstantiationException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.utils.parse.BaseJsonParser;
import eu.europeana.set.utils.serialization.AgentDeserializer;
import eu.europeana.set.utils.serialization.ListDeserializer;
import eu.europeana.set.utils.serialization.UserSetDeserializer;

/**
 * This class implements parsing methods and definitions.
 */
public class JsonUtils extends BaseJsonParser{
	
	/**
	 * This method converts JsonLd string in a UserSet object.
	 * @param json The JsonLd string representing UserSet object
	 * @return user set object
	 */
	public static UserSet toUserSetObject(String json, Class<? extends UserSet> userSetClass) {
		JsonParser parser;
		UserSet userSet = null;
		try {
//			jsonFactory.createParser(json);           
//			SimpleTest simple = mapper.readValue(parser, SimpleTest.class);
			
//			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			
			parser = jsonFactory.createParser(json);
			//DeserializationConfig cfg = DeserializerFactory.Config.
			SimpleModule module =  
//				      new SimpleModule();  
//						      new SimpleModule("UserSetDeserializer",  
						      new SimpleModule("UserSetDeserializerModule",  
			          new Version(1, 0, 0, null));  
			    
			module.addDeserializer(UserSet.class, new UserSetDeserializer());  
			module.addDeserializer(Agent.class, new AgentDeserializer());
//			module.addDeserializer(Map.class, new MapDeserializer());
			module.addDeserializer(List.class, new ListDeserializer());
			
//			mapper.registerModule(new JsonldModule()); 
			mapper.registerModule(module); 
						
			parser.setCodec(mapper);
			userSet = mapper.readValue(parser, userSetClass); //UserSet.class);
		} catch (JsonParseException e) {
			throw new UserSetInstantiationException("Json formating exception!", e);
		} catch (IOException e) {
			throw new UserSetInstantiationException("Json reading exception!", e);
		}
		
		return userSet;
	}
	
    /**
     *  use public static List<String> toStringList(String json)
     * This method converts JSON string to List<String>.
     * @param value The input string
     * @return resulting List<String>
     */
    public static List<String> stringToList(String value) {
    	String reg = ",";
    	value = value.replace(" ", ""); // remove blanks
        List<String> res = new ArrayList<String>();
        if (!value.isEmpty()) {
			value = value.substring(1, value.length() - 1); // remove braces
	        String[] arrValue = value.split(reg);
	        for (String string : arrValue) {
	        	res.add(string);
	    	}
        }
        return res;
    }
    
    /**
     * This method converts JSON string to map.
     * @param value The input string
     * @return resulting map
     */
    public static Map<String, String> stringToMap(String value) {
    	String reg = ",";
        Map<String,String> res = new HashMap<String, String>();
        if (!value.isEmpty()) {
			value = value.substring(1, value.length() - 1); // remove braces
	        String[] arrValue = value.split(reg);
	        for (String string : arrValue) {
	            String[] mapPair = string.split(WebUserSetFields.SEPARATOR_SEMICOLON);
	            res.put(mapPair[0], mapPair[1]);
	    	}
        }
        return res;
    }    
}
