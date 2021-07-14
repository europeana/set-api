package eu.europeana.set.web.model.bestbets;

import java.util.List;

public class BestBetsUserSet {

    String entityId;
    List<String> items;

    public BestBetsUserSet() {
    }

    public BestBetsUserSet(String entityId, List<String> items) {
        this.entityId = entityId;
        this.items = items;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

}
