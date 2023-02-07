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
//		public static ModelAndView toJson(Object object) {
		return toJson(object, null);
	}
	
//	public static ModelAndView toJson(String json, String callback) {
//		String resultPage = "json";
//		Map<String, Object> model = new HashMap<String, Object>();
//		model.put(resultPage, json);
//		if (StringUtils.isNotBlank(callback)) {
//			resultPage = "jsonp";
//			model.put("callback", callback);
//		}
//		return new ModelAndView(resultPage, model);
//	}

	public static String toJson(Object object, String callback) {
//		public static ModelAndView toJson(Object object, String callback) {
		return toJson(object, callback, false, -1);
	}
		
	public static String toJson(Object object, String callback, boolean shortObject, int objectId) {
//		public static ModelAndView toJson(Object object, String callback, boolean shortObject, int objectId) {
			
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		String errorMessage = null;
		try {
			String jsonStr = objectMapper.writeValueAsString(object);	
			if (shortObject) {
				String idBeginStr = "id\":{";
				int startIdPos = jsonStr.indexOf(idBeginStr);
				int endIdPos = jsonStr.indexOf("}", startIdPos);
				jsonStr = jsonStr.substring(0, startIdPos) + idBeginStr.substring(0, idBeginStr.length() - 1) 
				    + Integer.valueOf(objectId) + jsonStr.substring(endIdPos + 1);
			}
//			return toJson(jsonStr, callback);
			return jsonStr;
		} catch (JsonGenerationException e) {
			log.error("Json Generation Exception: " + e.getMessage(),e);
			errorMessage = "Json Generation Exception: " + e.getMessage() + " See error logs!";
		} catch (JsonMappingException e) {
			log.error("Json Mapping Exception: " + e.getMessage(),e);
			errorMessage = "Json Generation Exception: " + e.getMessage() + " See error logs!";
		} catch (IOException e) {
			log.error("I/O Exception: " + e.getMessage(),e);
			errorMessage = "I/O Exception: " + e.getMessage() + " See error logs!";
		}
		//Report technical errors...
//		String resultPage = "json";
//		return new ModelAndView(resultPage, "errorMessage", errorMessage);
		return errorMessage;
	}
	
}
