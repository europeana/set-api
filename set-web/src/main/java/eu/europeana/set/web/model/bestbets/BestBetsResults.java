package eu.europeana.set.web.model.bestbets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BestBetsResults {

    private int success;
    private  int failed;
    private List<String> setIds;
    private List<String> entitiesFailed;

    public BestBetsResults(int success, int failed, List<String> setIds, List<String> entitiesFailed) {
        this.success = success;
        this.failed = failed;
        this.setIds = setIds;
        this.entitiesFailed = entitiesFailed;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public List<String> getSetIds() {
        return setIds;
    }

    public void setSetIds(List<String> setIds) {
        this.setIds = setIds;
    }

    public List<String> getEntitiesFailed() {
        return entitiesFailed;
    }

    public void setEntitiesFailed(List<String> entitiesFailed) {
        this.entitiesFailed = entitiesFailed;
    }
}
