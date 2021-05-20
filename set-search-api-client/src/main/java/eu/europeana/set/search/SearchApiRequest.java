package eu.europeana.set.search;


public class SearchApiRequest {

    private String query;
    private String[] qf;
    private int start = 1;
    private int rows ;
    private String[] sort;


    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String[] getQf() {
        return qf;
    }

    public void setQf(String[] qf) {
        this.qf = qf;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public String[] getSort() {
        return sort;
    }

    public void setSort(String[] sort) {
        this.sort = sort;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }
}
