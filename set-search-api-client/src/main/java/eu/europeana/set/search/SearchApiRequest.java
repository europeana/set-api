package eu.europeana.set.search;


public class SearchApiRequest {

    private String query;
    private String[] qf;
    private int rows ;

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
}
