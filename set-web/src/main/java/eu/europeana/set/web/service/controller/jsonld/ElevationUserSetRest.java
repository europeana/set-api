package eu.europeana.set.web.service.controller.jsonld;

import eu.europeana.api.common.config.UserSetI18nConstants;
import eu.europeana.api.common.config.swagger.SwaggerSelect;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.exception.InternalServerException;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.model.elevation.Doc;
import eu.europeana.set.web.model.elevation.Elevation;
import eu.europeana.set.web.model.elevation.Query;
import eu.europeana.set.web.utils.UserSetXMLSerializer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import eu.europeana.set.definitions.model.UserSet;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
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
    @GetMapping(value = { "/set/elevation" }, produces = {MediaType.APPLICATION_XML_VALUE})
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
        List<PersistentUserSet> usersets = getUserSetService().getEntitySetBestBetsItems(
                getUsageStatsService().buildUserSetQuery(null, UserSetTypes.ENTITYBESTITEMSSET.getJsonValue(),null));
        Elevation elevation = buildElevation(usersets);
        if (elevation != null) {
            UserSetXMLSerializer xmlSerializer = new UserSetXMLSerializer();
            String xml = xmlSerializer.serialize(elevation);
            
            //TODO: enable writing to file, when the behaviour and server configurations with regard to write permissions are clarified
//            writeElevation(getConfiguration().getElevationFileLocation(), xml);
            // returning the elevation response
            //TODO - remove the body, once we know how elevation file will be used
            return new ResponseEntity<>(xml, HttpStatus.OK);
        } else {
            throw new UserSetNotFoundException(UserSetI18nConstants.ELEVATION_NOT_GENERATED,
                    UserSetI18nConstants.ELEVATION_NOT_GENERATED,
                    null);
        }
    } catch (IOException e) {
        throw new InternalServerException(e);
    }
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
        if (userset.getItems() != null) {
            for(String item : userset.getItems()) {
            docList.add(new Doc(item));
            }
        }
        if (!text.isEmpty() && !docList.isEmpty()) {
            queries.add(new Query(text, docList));
        }
    }
    if (!queries.isEmpty()) {
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
      String s = subject.get(0);
          if (s.contains(WebUserSetFields.AGENT)) {
              text.append(WebUserSetFields.ELEVATION_AGENT_QUERY);
          }
          else if (s.contains(WebUserSetFields.CONCEPT)) {
              text.append(WebUserSetFields.ELEVATION_CONCEPT_QUERY);
          }
          else if(s.contains(WebUserSetFields.TIMESPAN)) {
              text.append(WebUserSetFields.ELEVATION_TIMESPAN_QUERY);
          } else {
              return ""; // return empty for any other case
          }
          text.append("\"");
          text.append(s);
          text.append("\"");
      return text.toString();
    }

    /**
     * writes the elevation in a file
     *
     * @param directoryLocation folder location
     */
    public static void writeElevation(String directoryLocation, String xml) throws InternalServerException {
    if (!directoryLocation.isEmpty()) {
        try {
            File file = new File(directoryLocation);
            file.mkdirs();
            FileUtils.write(new File(directoryLocation + WebUserSetFields.SLASH + WebUserSetFields.ELEVATION_FILENAME), xml, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new InternalServerException("Error creating the " + WebUserSetFields.ELEVATION_FILENAME + " file", e);
        }
    }
    }
}
