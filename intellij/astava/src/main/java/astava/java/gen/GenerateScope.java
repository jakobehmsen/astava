package astava.java.gen;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.Hashtable;

public class GenerateScope {
    private GenerateScope outerScope;
    private Hashtable<String, Integer> nameToVarIdMap = new Hashtable<>();
    private Hashtable<Integer, String> varIdToTypeMap = new Hashtable<>();

    public GenerateScope() { }

    public GenerateScope(GenerateScope outerScope) {
        this.outerScope = outerScope;
    }

    public void declareVar(GeneratorAdapter generator, String type, String name) {
        int id = generator.newLocal(Type.getType(type));
        varIdToTypeMap.put(id, type);
        nameToVarIdMap.put(name, id);
    }

    public int getVarId(String name) {
        Integer varId = nameToVarIdMap.get(name);
        if(varId != null)
            return varId.intValue();
        return outerScope != null ? outerScope.getVarId(name) : null;
    }

    public String getVarType(String name) {
        int id = nameToVarIdMap.get(name);
        return varIdToTypeMap.get(id);
    }
}
