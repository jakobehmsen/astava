package astava.java.ijava;

import astava.java.gen.ClassGenerator;
import astava.java.parser.ClassDeclaration;
import astava.java.parser.ClassDomBuilder;
import astava.java.parser.ClassResolver;
import astava.tree.ClassDom;

import java.util.Hashtable;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class IJAVAClassLoader extends ClassLoader {
    private ClassResolver classResolver;
    private Map<String, ClassDomBuilder> classBuilders;
    private Map<String, ClassDeclaration> classDeclarationCache;
    private Map<String, Class<?>> classCache;

    public IJAVAClassLoader(ClassResolver classResolver) {
        this.classResolver = classResolver;
        this.classBuilders = new Hashtable<>();
        this.classDeclarationCache = new Hashtable<>();
        this.classCache = new Hashtable<>();
    }

    public void putClassBuilder(String name, ClassDomBuilder builder) {
        classBuilders.put(name, builder);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> cachedClass = classCache.get(name);

        if(cachedClass != null)
            return cachedClass;

        ClassDomBuilder classBuilder = classBuilders.get(name);

        if(classBuilder != null) {
            ClassDeclaration classDeclaration = getClassDeclaration(name);

            ClassDom classDom = classDeclaration.build();
            ClassGenerator generator = new ClassGenerator(classDom);
            byte[] classBytes = generator.toBytes();
            cachedClass = defineClass(name, classBytes, 0, classBytes.length);

            classCache.put(name, cachedClass);

            return cachedClass;
        }

        return getParent().loadClass(name);
    }

    public ClassDeclaration getClassDeclaration(String name) {
        ClassDomBuilder classBuilder = classBuilders.get(name);

        ClassDeclaration classDeclaration = classDeclarationCache.get(name);

        if(classDeclaration == null) {
            classDeclaration = classBuilder.build(classResolver);
            classDeclarationCache.put(name, classDeclaration);
        }

        return classDeclaration;
    }
}
