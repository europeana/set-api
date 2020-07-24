package eu.europeana.set.web.search;

public class CollectionView {

    String id;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    Long total;
    
    public CollectionView() {
	super();
    }
    
    public CollectionView(String id, Long total) {
	this.id = id;
	this.total = total;
    }
    
    public Long getTotal() {
        return total;
    }
    public void setTotal(Long total) {
        this.total = total;
    } 
}
