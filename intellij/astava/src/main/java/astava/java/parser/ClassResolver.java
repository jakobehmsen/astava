package astava.java.parser;

public interface ClassResolver {
    void importPackage(String packageName);
    boolean canResolveAmbiguous(String className);
    String resolveSimpleName(String className);
}
