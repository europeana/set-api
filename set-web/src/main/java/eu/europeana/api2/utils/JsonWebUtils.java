package eu.europeana.api2.utils;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author GordeaS
 * This class is also in the annotation-api, and can be moved to the commons api.
 */
public class JsonWebUtils {
	
	private static final Logger log = LogManager.getLogger(JsonWebUtils.class);
	private static ObjectMapper objectMapper = new ObjectMapper();	
	
	public static String toJson(Object object) {
		return toJson(object, false, -1);
	}
		
	public static String toJson(Object object, boolean shortObject, int objectId) {
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		String errorMessage = null;
		try {
			String jsonStr = objectMapper.writeValueAsString(object);	
			if (shortObject) {
				String idBeginStr = "id\":{";
				int startIdPos = jsonStr.indexOf(idBeginStr);
				int endIdPos = jsonStr.indexOf('}', startIdPos);
				jsonStr = jsonStr.substring(0, startIdPos) + idBeginStr.substring(0, idBeginStr.length() - 1) 
				    + Integer.valueOf(objectId) + jsonStr.substring(endIdPos + 1);
			}
			return jsonStr;
		} catch (JsonGenerationException e) {
			log.error("Json Generation Exception: " + e.getMessage(),e);
			errorMessage = "Json Generation Exception: " + e.getMessage() + " See error logs!";
		} catch (JsonMappingException e) {
			log.error("Json Mapping Exception: " + e.getMessage(),e);
			errorMessage = "Json Mapping Exception: " + e.getMessage() + " See error logs!";
		} catch (IOException e) {
			log.error("I/O Exception: " + e.getMessage(),e);
			errorMessage = "I/O Exception: " + e.getMessage() + " See error logs!";
		}
		return errorMessage;
	}
	
}
