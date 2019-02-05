package cs.art.ia.utils;

public class GuiResult {

    private Integer id;
    private String query;
    private String result;

    public GuiResult(String query, String result) {
        this.query = query;
        this.result = result;
    }

    public GuiResult() {
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
