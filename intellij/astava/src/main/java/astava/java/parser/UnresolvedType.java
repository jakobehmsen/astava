package astava.java.parser;

public class UnresolvedType {
    private String name;

    public UnresolvedType(String name) {
        this.name = name;
    }

    public String resolveName(ClassResolver classResolver) {
        String resolvedName = classResolver.resolveSimpleName(name);
        return resolvedName != null ? resolvedName : name;
    }

    @Override
    public String toString() {
        return name;
    }
}
