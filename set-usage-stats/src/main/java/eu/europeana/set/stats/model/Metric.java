package eu.europeana.set.stats.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.europeana.set.stats.vocabulary.UsageStatsFields;

@JsonPropertyOrder({UsageStatsFields.TYPE, UsageStatsFields.CREATED, UsageStatsFields.NUMBER_OF_PUBLIC_SETS,
        UsageStatsFields.NUMBER_OF_PRIVATE_SETS, UsageStatsFields.NUMBER_OF_ITEMS_LIKED, UsageStatsFields.AVERAGE_SETS_PER_USER})
@JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Metric {

    @JsonProperty(UsageStatsFields.TYPE)
    private String type;

    @JsonProperty(UsageStatsFields.CREATED)
    private String timestamp;

    @JsonProperty(UsageStatsFields.NUMBER_OF_PRIVATE_SETS)
    private long noOfPrivateSets;

    @JsonProperty(UsageStatsFields.NUMBER_OF_PUBLIC_SETS)
    private long noOfPublicSets;

    @JsonProperty(UsageStatsFields.NUMBER_OF_ITEMS_LIKED)
    private long noOfItemsLiked;

    @JsonProperty(UsageStatsFields.AVERAGE_SETS_PER_USER)
    private long averageSetsPerUser;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public long getNoOfPrivateSets() {
        return noOfPrivateSets;
    }

    public void setNoOfPrivateSets(long noOfPrivateSets) {
        this.noOfPrivateSets = noOfPrivateSets;
    }

    public long getNoOfPublicSets() {
        return noOfPublicSets;
    }

    public void setNoOfPublicSets(long noOfPublicSets) {
        this.noOfPublicSets = noOfPublicSets;
    }

    public long getNoOfItemsLiked() {
        return noOfItemsLiked;
    }

    public void setNoOfItemsLiked(long noOfItemsLiked) {
        this.noOfItemsLiked = noOfItemsLiked;
    }

    public long getAverageSetsPerUser() {
        return averageSetsPerUser;
    }

    public void setAverageSetsPerUser(long averageSetsPerUser) {
        this.averageSetsPerUser = averageSetsPerUser;
    }

    @Override
    public String toString() {
        return "MetricData{" +
                "OverallTotal='" + type + '\'' +
                ", created='" + timestamp + '\'' +
                ", NumberOfPrivateSets=" + noOfPrivateSets +
                ", NumberOfPublicSets=" + noOfPublicSets +
                ", NumberOfItemsLiked=" + noOfItemsLiked +
                ", AverageSetsPerUser=" + averageSetsPerUser +
                '}';
    }
}
