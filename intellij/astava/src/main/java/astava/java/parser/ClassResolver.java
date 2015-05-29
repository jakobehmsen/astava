package astava.java.parser;

public interface ClassResolver {
    boolean canResolveAmbiguous(String className);
    String resolveSimpleName(String className);
}
