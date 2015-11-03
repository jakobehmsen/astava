package astava.java.parser;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultClassResolver implements ClassResolver {
    private ClassLoader classLoader;
    private Map<String, String> simpleNameToNameMap;
    private Set<String> nameSet;

    public DefaultClassResolver(ClassLoader classLoader) {
        this(classLoader, Arrays.asList());
    }

    public DefaultClassResolver(ClassLoader classLoader, List<Class<?>> classes) {
        this(classLoader, classes.stream()
            .map(x -> new AbstractMap.SimpleImmutableEntry<>(x.getSimpleName(), x.getName()))
            .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue())));
    }

    public DefaultClassResolver(ClassLoader classLoader, Map<String, String> simpleNameToNameMap) {
        this.classLoader = classLoader;
        this.simpleNameToNameMap = simpleNameToNameMap;
        nameSet = simpleNameToNameMap.values().stream().collect(Collectors.toSet());
    }

    @Override
    public boolean canResolveAmbiguous(String className) {
        if(nameSet.contains(className))
            return true;

        if(className.endsWith("[]")) {
            try {
                Class.forName("[L" + className.substring(0, className.length() - 2) + ";");
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }

        try {
            classLoader.loadClass(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public String resolveSimpleName(String className) {
        return simpleNameToNameMap.get(className);
    }
}
