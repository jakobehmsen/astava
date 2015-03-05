package astava.java.gen;

import astava.core.Tuple;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.Hashtable;

public class GenerateScope {
    private Hashtable<String, Integer> nameToVarIdMap = new Hashtable<>();
    private Hashtable<Integer, String> varIdToTypeMap = new Hashtable<>();

    public void declareVar(GeneratorAdapter generator, String type, String name) {
        int id = generator.newLocal(Type.getType(type));
        varIdToTypeMap.put(id, type);
        nameToVarIdMap.put(name, id);
    }

    public int getVarId(String name) {
        return nameToVarIdMap.get(name);
    }

    public String getVarType(String name) {
        int id = nameToVarIdMap.get(name);
        return varIdToTypeMap.get(id);
    }
}
