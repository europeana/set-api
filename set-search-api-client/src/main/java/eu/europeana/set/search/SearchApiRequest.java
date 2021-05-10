package eu.europeana.set.search;


public class SearchApiRequest {

    private String query;
    private String[] qf;
    private String[] reusability;

    // default value for Search API post request
    private String[] profile = {"standard"};
    private int start = 1;
    private int rows ;
    private String theme;
    private String[] sort;
    private String[] colourPalette;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String[] getProfile() {
        return profile;
    }

    public void setProfile(String[] profile) {
        this.profile = profile;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
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

    public String[] getQf() {
        return qf;
    }

    public void setQf(String[] qf) {
        this.qf = qf;
    }

    public String[] getReusability() {
        return reusability;
    }

    public void setReusability(String[] reusability) {
        this.reusability = reusability;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String[] getColourPalette() {
        return colourPalette;
    }

    public void setColourPalette(String[] colourPalette) {
        this.colourPalette = colourPalette;
    }
}
