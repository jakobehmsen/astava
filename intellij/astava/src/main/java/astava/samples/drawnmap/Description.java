package astava.samples.drawnmap;

import java.util.Map;

public class Description {
    private final Map<String, Cell> idToCellMap;
    private final String src;

    public Description(Map<String, Cell> idToCellMap, String src) {
        this.idToCellMap = idToCellMap;
        this.src = src;
    }

    public Map<String, Cell> getIdToCellMap() {
        return idToCellMap;
    }

    public String getSrc() {
        return src;
    }
}
