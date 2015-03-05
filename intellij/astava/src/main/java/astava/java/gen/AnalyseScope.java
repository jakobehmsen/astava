package astava.java.gen;

import java.util.Hashtable;

public class AnalyseScope {
    private static class VariableInfo {
        public String type;
        public boolean isSet;

        public VariableInfo(String type) {
            this.type = type;
        }
    }

    private Hashtable<String, VariableInfo> nameToTypeMap = new Hashtable<>();

    public void declareVar(String type, String name) {
        nameToTypeMap.put(name, new VariableInfo(type));
    }

    public boolean varIsSet(String name) {
        return nameToTypeMap.get(name).isSet;
    }

    public void assignVar(String name) {
        nameToTypeMap.get(name).isSet = true;
    }

    public String getVarType(String name) {
        return nameToTypeMap.get(name).type;
    }
}
