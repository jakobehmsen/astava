package astava.java.ijava;

import astava.java.gen.ClassGenerator;
import astava.java.parser.ClassDeclaration;
import astava.java.parser.ClassDomBuilder;
import astava.java.parser.ClassInspector;
import astava.java.parser.ClassResolver;
import astava.tree.ClassDom;

import java.util.Hashtable;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class IJAVAClassLoader extends ClassLoader implements ClassResolver, ClassInspector {
    private ClassResolver classResolver;
    private Map<String, ClassDomBuilder> classBuilders;
    private Map<String, String> nameToSimpleNameMap;
    private Map<String, ClassDeclaration> classDeclarationCache;
    private Map<String, Class<?>> classCache;

    public IJAVAClassLoader(ClassResolver classResolver) {
        this.classResolver = classResolver;
        classBuilders = new Hashtable<>();
        nameToSimpleNameMap = new Hashtable<>();
        classDeclarationCache = new Hashtable<>();
        classCache = new Hashtable<>();
    }

    public void putClassBuilder(String name, ClassDomBuilder builder) {
        classBuilders.put(name, builder);
        String simpleName = getSimpleName(name);
        nameToSimpleNameMap.put(name, simpleName);
    }

    private static String getSimpleName(String name) {
        int indexOfLastDot = name.lastIndexOf('.');
        return indexOfLastDot != -1 ? name.substring(indexOfLastDot) : name;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> cachedClass = classCache.get(name);

        if(cachedClass != null)
            return cachedClass;

        ClassDomBuilder classBuilder = classBuilders.get(name);

        if(classBuilder != null) {
            ClassDeclaration classDeclaration = getClassDeclaration(name);

            ClassDom classDom = classDeclaration.build(this);
            ClassGenerator generator = new ClassGenerator(classDom);
            byte[] classBytes = generator.toBytes();
            cachedClass = defineClass(name, classBytes, 0, classBytes.length);

            classCache.put(name, cachedClass);

            return cachedClass;
        }

        return getParent().loadClass(name);
    }

    @Override
    public boolean canResolveAmbiguous(String className) {
        return classBuilders.containsKey(className) || classResolver.canResolveAmbiguous(className);
    }

    @Override
    public String resolveSimpleName(String className) {
        String simpleName = nameToSimpleNameMap.get(className);
        return simpleName != null ? simpleName : classResolver.resolveSimpleName(className);
    }

    @Override
    public ClassDeclaration getClassDeclaration(String name) {
        ClassDomBuilder classBuilder = classBuilders.get(name);

        ClassDeclaration classDeclaration = classDeclarationCache.get(name);

        if(classDeclaration == null) {
            classDeclaration = classBuilder.build(this);
            classDeclarationCache.put(name, classDeclaration);
        }

        return classDeclaration;
    }

    public Map<String, ClassDomBuilder> getClassBuilders() {
        return classBuilders;
    }
}
