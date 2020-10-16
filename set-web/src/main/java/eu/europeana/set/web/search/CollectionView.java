package eu.europeana.set.web.search;

public class CollectionView {

    private String id;
    private Long total;
    private String first;
    private String last;

    public CollectionView() {
        super();
    }

    public CollectionView(String id, Long total) {
        this.id = id;
        this.total = total;
    }

    public CollectionView(String id, Long total, String first, String last) {
        this.id = id;
        this.total = total;
        this.first = first;
        this.last = last;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public Long getTotal() {
        return total;
    }
    public void setTotal(Long total) {
        this.total = total;
    } 
}
