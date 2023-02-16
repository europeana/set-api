package eu.europeana.api2.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
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
	
	private static final String SEE_ERROR_LOGS = " See error logs!";
    private static final Logger log = LogManager.getLogger(JsonWebUtils.class);
    private static final String DATE_FORMAT="yyyy-MM-dd'T'HH:mm:ss.SSS";
	private static ObjectMapper objectMapper;
	static {
	  objectMapper = new ObjectMapper();
      SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
      objectMapper.setDateFormat(df);	  
	}
		
	/**
	 * Hide default contructor
	 */
	private JsonWebUtils() {}
	
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
			errorMessage = "Json Generation Exception: " + e.getMessage() + SEE_ERROR_LOGS;
			log.warn(errorMessage,e);
		} catch (JsonMappingException e) {
			errorMessage = "Json Mapping Exception: " + e.getMessage() + SEE_ERROR_LOGS;
			log.warn(errorMessage,e);
		} catch (IOException e) {
			errorMessage = "I/O Exception: " + e.getMessage() + SEE_ERROR_LOGS;
			log.warn(errorMessage,e);
		}
		return errorMessage;
	}
	
}
