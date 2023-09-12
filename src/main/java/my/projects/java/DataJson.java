package my.projects.java;

import java.util.Map;

public class DataJson {
    private Map<Long, Boolean> ids;

    public DataJson() {
    }

    public DataJson(Map<Long, Boolean> ids) {
        this.ids = ids;
    }

    public Map<Long, Boolean> getIds() {
        return ids;
    }

    public void setIds(Map<Long, Boolean> ids) {
        this.ids = ids;
    }

    public boolean hasIdsSet() {
        return ids != null;
    }
}
