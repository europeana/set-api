package eu.europeana.set.web.service.controller.jsonld;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europeana.api.common.config.UserSetI18nConstants;
import eu.europeana.api.common.config.swagger.SwaggerSelect;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.exception.InternalServerException;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.model.elevation.Doc;
import eu.europeana.set.web.model.elevation.Elevation;
import eu.europeana.set.web.model.elevation.Query;
import eu.europeana.set.web.utils.UserSetXMLSerializer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import eu.europeana.set.definitions.model.UserSet;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import eu.europeana.set.web.service.controller.BaseRest;

@Controller
@SwaggerSelect
@Api(tags = "User Set Best Bets Items API")
public class ElevationUserSetRest extends BaseRest {

    /**
     * Generate Elevation File
     *
     * @param wsKey
     * @param request
     * @return
     * @throws HttpException
     */
    @GetMapping(value = { "/set/elevation" })
    @ApiOperation(value = "Generate Elevation file", nickname = "generate elevation file", response = java.lang.Void.class)
    public ResponseEntity<String> generateElevationFile(
            @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = true) String wsKey,
            HttpServletRequest request) throws HttpException {
    verifyReadAccess(request);
    return generateElevation();
    }

    /**
     * generate elevation file
     * @return
     * @throws HttpException
     */
    private ResponseEntity<String> generateElevation() throws HttpException {
    try {
        List<PersistentUserSet> usersets = getUserSetService().getEntitySetBestBetsItems();
        Elevation elevation = buildElevation(usersets);
        if (elevation != null) {
            UserSetXMLSerializer xmlSerializer = new UserSetXMLSerializer();
            String xml = xmlSerializer.serialize(elevation);
            writeElevation(getConfiguration().getElevationFileLocation(), xml);
        } else {
            return new ResponseEntity<>("No elevation file generated" , HttpStatus.INTERNAL_SERVER_ERROR);
        }
    } catch (JsonProcessingException e) {
        throw new InternalServerException(e);
    }
    return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Build elevation object
     * @param userSets
     * @return
     */
    private Elevation buildElevation(List<PersistentUserSet> userSets) throws UserSetNotFoundException {
        if (userSets.isEmpty()) {
            throw new UserSetNotFoundException(UserSetI18nConstants.ENTITY_USER_SET_NOT_FOUND,
                    UserSetI18nConstants.ENTITY_USER_SET_NOT_FOUND,
                    null);
        }
        Elevation elevation = new Elevation();
        List<Query> queries = new ArrayList<>();
        for (UserSet userset : userSets) {
            String text = generateQueryText(userset.getSubject());
            List<Doc> docList = new ArrayList<>();
            for(String item : userset.getItems()) {
                docList.add(new Doc(item));
            }
            if (!text.isEmpty() && docList.size() > 0) {
                queries.add(new Query(text, docList));
            }
        }
        if (queries.size() > 0) {
            elevation.setQuery(queries);
            return elevation;
        }
        return null;
    }

    /**
     * get the entity reference from the subject list
     * @param subject
     * @return
     */
    private String generateQueryText(List<String> subject) {
        StringBuilder text = new StringBuilder();
      for(String s : subject) {
          if (s.contains(WebUserSetFields.AGENT)) {
              text.append(WebUserSetFields.ELEVATION_AGENT_QUERY);
          }
          if (s.contains(WebUserSetFields.CONCEPT)) {
              text.append(WebUserSetFields.ELEVATION_CONCEPT_QUERY);
          }
          if(s.contains(WebUserSetFields.TIMESPAN)) {
              text.append(WebUserSetFields.ELEVATION_TIMESPAN_QUERY);
          }
          text.append("\"");
          text.append(s);
          text.append("\"");

      }
      return URLEncoder.encode(text.toString(), StandardCharsets.UTF_8);
    }

    /**
     * writes the elevation in a file
     *
     * @param directoryLocation folder location
     */
    public static void writeElevation(String directoryLocation, String xml) throws InternalServerException {
        try (PrintWriter writer = new PrintWriter(directoryLocation + WebUserSetFields.SLASH + WebUserSetFields.ELEVATION_FILENAME, StandardCharsets.UTF_8)) {
            writer.write(xml);
        } catch (IOException e) {
            throw new InternalServerException("Error creating the " + WebUserSetFields.ELEVATION_FILENAME + " file", e);
        }
    }
}
