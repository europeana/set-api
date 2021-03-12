package eu.europeana.set.web.service.controller.jsonld;

import eu.europeana.api.common.config.swagger.SwaggerSelect;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.set.stats.model.MetricData;
import eu.europeana.set.stats.vocabulary.UsageStatsFields;
import eu.europeana.set.web.http.SwaggerConstants;
import eu.europeana.set.web.search.UserSetLdSerializer;
import eu.europeana.set.web.service.controller.BaseRest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Controller
@SwaggerSelect
@Api(tags = "User Set Usage Statistics API")
@ComponentScan("eu.europeana.set.stats")
public class UsageStatsUserSetRest extends BaseRest {

    Logger LOG = LogManager.getLogger(getClass().getName());

    /**
     * Method to generate metric for User Set (Galleries)
     *
     * @param wsKey
     * @param request
     * @return
     */
    @GetMapping(value = "/set/metrics/push", produces = {HttpHeaders.CONTENT_TYPE_JSONLD_UTF8,
            HttpHeaders.CONTENT_TYPE_JSON_UTF8})
    @ApiOperation(notes = SwaggerConstants.SET_USAGE_STATS, value = "Generate usage stats", nickname = "generateUserStats", response = java.lang.Void.class)
    public ResponseEntity<String> generateUsageStats(
            @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = true) String wsKey,
            @
            HttpServletRequest request) throws IOException {
        return getUsageStats();
    }

    /**
     * Get the usage statistics for User Set API
     *
     * @return
     */
    private ResponseEntity<String> getUsageStats() throws IOException {
        MetricData metricData = new MetricData();
        metricData.setType(UsageStatsFields.OVERALL_TOTAL_TYPE);
        getUsageStatsService().getPublicPrivateSetsCount(metricData);
        getUsageStatsService().getTotalItemsLiked(metricData);
        getUsageStatsService().getAverageSetsPerUser(metricData);
        System.out.println(metricData);

        String json = serializeMetricView(metricData);
        return buildUsageStatsResponse(json);

    }

    private ResponseEntity<String> buildUsageStatsResponse(String json) {
        // build response
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
        headers.add(HttpHeaders.VARY, HttpHeaders.ACCEPT);
        headers.add(HttpHeaders.VARY, HttpHeaders.PREFER);
        headers.add(HttpHeaders.ALLOW, HttpHeaders.ALLOW_GET);

        return new ResponseEntity<>(json, headers, HttpStatus.OK);
    }

    @SuppressWarnings("rawtypes")
    private String serializeMetricView(MetricData metricData) throws IOException {
        UserSetLdSerializer serializer = new UserSetLdSerializer();
        return serializer.serialize(metricData);
    }

}
