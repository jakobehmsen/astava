package astava.java.parser;

import astava.tree.ParameterInfo;

public class UnresolvedParameterInfo {
    private String name;
    private UnresolvedType type;

    public UnresolvedParameterInfo(String name, UnresolvedType type) {
        this.name = name;
        this.type = type;
    }

    public ParameterInfo resolve(ClassResolver classResolver) {
        String typeName = type.resolveName(classResolver);

        return new ParameterInfo(Parser.parseTypeQualifier(classResolver, typeName), name);
    }
}
