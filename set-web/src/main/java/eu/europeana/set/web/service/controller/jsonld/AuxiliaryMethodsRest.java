package eu.europeana.set.web.service.controller.jsonld;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import eu.europeana.api.commons.definitions.statistics.UsageStatsFields;
import eu.europeana.api.commons.definitions.statistics.set.SetMetric;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.exception.InternalServerException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.set.definitions.exception.UserSetServiceException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.web.config.UserSetI18nConstants;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.http.SwaggerConstants;
import eu.europeana.set.web.http.UserSetHttpHeaders;
import eu.europeana.set.web.model.elevation.Doc;
import eu.europeana.set.web.model.elevation.Elevation;
import eu.europeana.set.web.model.elevation.Query;
import eu.europeana.set.web.search.UserSetLdSerializer;
import eu.europeana.set.web.service.controller.BaseRest;
import eu.europeana.set.web.utils.UserSetXMLSerializer;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Auxiliary Methods")
public class AuxiliaryMethodsRest extends BaseRest {

    /**
     * Generate Elevation File
     *
     * @param wsKey
     * @param request
     * @return
     * @throws HttpException
     */
    @GetMapping(value = { "/set/elevation" }, produces = {MediaType.APPLICATION_XML_VALUE})
    @Tag(description = "Generate Elevation file for best bets", name = "Generate elevation file")
    public ResponseEntity<String> generateElevationFile(
            @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = true) String wsKey,
            HttpServletRequest request) throws HttpException {
    verifyReadAccess(request);
    return generateElevation();
    }

    /**
     * Method to generate metric for User Set (Galleries)
     *
     * @param wsKey
     * @param request
     * @return
     * @throws UserSetServiceException 
     */
    @GetMapping(value = "/set/stats", produces = {HttpHeaders.CONTENT_TYPE_JSON_UTF8})
    @Tag(description = SwaggerConstants.SET_USAGE_STATS, name = "Generate usage statistics")
    public ResponseEntity<String> generateUsageStats(
            @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = true) String wsKey,
            HttpServletRequest request) throws IOException, ApplicationAuthenticationException, UserSetServiceException {
        return getUsageStats(request);
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
            return ResponseEntity.status(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE +";charset=UTF-8")
                    .body(xml);
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
                docList.add(new Doc(UserSetUtils.extractItemIdentifier(item, getConfiguration().getItemDataEndpoint())));
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
          } else if (s.contains(WebUserSetFields.CONCEPT)) {
              text.append(WebUserSetFields.ELEVATION_CONCEPT_QUERY);
          } else if (s.contains(WebUserSetFields.TIMESPAN)) {
              text.append(WebUserSetFields.ELEVATION_TIMESPAN_QUERY);
          } else if (s.contains(WebUserSetFields.ORGANIZATION)) {
              text.append(WebUserSetFields.ELEVATION_ORGANIZATION_QUERY);
          } else {
              return ""; // return empty for any other case
          }
          text.append('"').append(s).append('"');
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
            File elevationFile = new File(directoryLocation,  FilenameUtils.getName(WebUserSetFields.ELEVATION_FILENAME));
            elevationFile.mkdirs();
            FileUtils.write(elevationFile, xml, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new InternalServerException("Error creating the " + WebUserSetFields.ELEVATION_FILENAME + " file", e);
        }
    }
    }
    
   
    /**
     * Get the usage statistics for User Set API
     *
     * @return
     * @throws UserSetServiceException 
     */
    private ResponseEntity<String> getUsageStats(HttpServletRequest request) throws IOException, ApplicationAuthenticationException, UserSetServiceException {
        // authenticate and generate the new statistics
        verifyReadAccess(request);
        // create metric
        SetMetric metric = new SetMetric();
        metric.setType(UsageStatsFields.OVERALL_TOTAL_TYPE);
        getUsageStatsService().getPublicPrivateSetsCount(metric);
        getUsageStatsService().getTotalItemsLiked(metric);
        getUsageStatsService().getAverageSetsPerUser(metric);
        getUsageStatsService().getNumberOfUsersWithLike(metric);
        getUsageStatsService().getNumberOfEntitySets(metric);
        getUsageStatsService().getNumberOfItemsInEntitySets(metric);

        metric.setTimestamp(new Date());

        String json = serializeMetricView(metric);

        return buildUsageStatsResponse(json);
    }

    private ResponseEntity<String> buildUsageStatsResponse(String json) {
        // build response
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
        headers.add(UserSetHttpHeaders.CACHE_CONTROL, UserSetHttpHeaders.VALUE_NO_CAHCHE_STORE_REVALIDATE);
        headers.add(HttpHeaders.ALLOW, HttpHeaders.ALLOW_GET);

        return new ResponseEntity<>(json, headers, HttpStatus.OK);
    }

    private String serializeMetricView(SetMetric metricData) throws IOException {
        UserSetLdSerializer serializer = new UserSetLdSerializer();
        return serializer.serialize(metricData);
    }

}
