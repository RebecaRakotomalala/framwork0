package mg.itu.prom16;

import java.util.HashMap;
import java.util.Map;

public class ModelView {
    private String url;
    private Map<String, Object> data;

    public ModelView() {
        this.data = new HashMap<>();
    }

    public ModelView(String url) {
        this.url = url;
        this.data = new HashMap<>();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void addObject(String key, Object value) {
        this.data.put(key, value);
    }
}