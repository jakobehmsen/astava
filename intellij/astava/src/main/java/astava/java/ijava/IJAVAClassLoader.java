package astava.java.ijava;

import astava.java.Descriptor;
import astava.java.gen.ClassGenerator;
import astava.java.parser.*;
import astava.tree.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static astava.java.Factory.fieldDeclaration;
import static astava.java.Factory.methodDeclaration;

public class IJAVAClassLoader extends ClassLoader implements ClassResolver, ClassInspector {
    private ClassResolver classResolver;
    //private Map<String, ClassDomBuilder> classBuilders;
    private Map<String, MutableClassDomBuilder> classBuilders;
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

    public IJAVAClassLoader reset() {
        IJAVAClassLoader resetLoader = new IJAVAClassLoader(this.classResolver);

        resetLoader.classBuilders.putAll(this.classBuilders);

        return resetLoader;
    }

    public void putClassBuilder(String name, ClassDomBuilder builder) {
        //builder = extendClass(name, builder);

        MutableClassDomBuilder classBuilder = classBuilders.get(name);

        if(classBuilder == null) {
            classBuilder = new MutableClassDomBuilder();
            classBuilder.setName(name);
            classBuilder.setModifier(Modifier.PUBLIC);
            classBuilder.setSuperName(builder.getSuperName());
            classBuilders.put(name, classBuilder);
        }

        for (FieldDomBuilder f : builder.getFields())
            classBuilder.addField(f);

        for (MethodDomBuilder m : builder.getMethods())
            classBuilder.addMethod(m);

        //classBuilders.put(name, builder);
        classDeclarationCache.remove(name);
        String simpleName = getSimpleName(name);
        nameToSimpleNameMap.put(name, simpleName);
    }

    /*private ClassDomBuilder extendClass(String name, ClassDomBuilder builder) {
        ClassDomBuilder currentBuilderTmp = classBuilders.get(name);
        if(currentBuilderTmp == null) {
            try {
                ClassDeclaration wrappedClassDeclaration = wrapClassDeclaration(name);
                currentBuilderTmp = new ClassDomBuilder() {
                    @Override
                    public ClassDeclaration build(ClassResolver classResolver) {
                        return wrappedClassDeclaration;
                    }

                    @Override
                    public String getName() {
                        return name;
                    }
                };
            } catch (ClassNotFoundException e) {
                //e.printStackTrace();
                return builder;
            }
        }

        ClassDomBuilder currentBuilder = currentBuilderTmp;

        return new ClassDomBuilder() {
            @Override
            public ClassDeclaration build(ClassResolver classResolver) {
                return currentBuilder.build(classResolver).extend(builder.build(classResolver));
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }*/

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
        ClassDeclaration classDeclaration;

        ClassDomBuilder classBuilder = classBuilders.get(name);

        if(classBuilder != null) {
            classDeclaration = classDeclarationCache.get(name);

            if (classDeclaration == null) {
                classDeclaration = classBuilder.build(this);
                classDeclarationCache.put(name, classDeclaration);
            }
        } else {
            try {
                Class<?> c = getParent().loadClass(name);
                classDeclaration = wrapClassDeclaration(name);
                classDeclarationCache.put(name, classDeclaration);

                return classDeclaration;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                classDeclaration = null;
            }
        }

        return classDeclaration.withDefaultConstructor();
    }

    public ClassDeclaration wrapClassDeclaration(String name) throws ClassNotFoundException {
        Class<?> c = getParent().loadClass(name);

        ClassNode classNode;

        // Could some of the classes within the java.* packages somehow be bypassed?
        // E.g., such that java.* names are changed to JAVA.* when type usages of java.* classes
        // and thus requested as JAVA.* classes within the class loader
        // Probably not all classes, such as java.lang.Object and java.lang.String, but some classes possibly?
        if(!name.startsWith("java")) {
            InputStream classInputStream = c.getResourceAsStream(c.getSimpleName() + ".class");

            ClassReader cr = null;
            try {
                cr = new ClassReader(classInputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            classNode = new ClassNode(Opcodes.ASM5);
            //ClassNode is a ClassVisitor
            cr.accept(classNode, ClassReader.EXPAND_FRAMES);
        } else
            classNode = null;

        return new ClassDeclaration() {
            @Override
            public List<FieldDeclaration> getFields() {
                return Arrays.asList(c.getDeclaredFields()).stream().map(x -> new FieldDeclaration() {
                    @Override
                    public int getModifier() {
                        return x.getModifiers();
                    }

                    @Override
                    public String getTypeName() {
                        return x.getType().getName();
                    }

                    @Override
                    public String getName() {
                        return x.getName();
                    }

                    @Override
                    public FieldDom build(ClassDeclaration classDeclaration) {
                        return fieldDeclaration(getModifier(), getName(), getTypeName());
                    }
                }).collect(Collectors.toList());
            }

            @Override
            public List<MethodDeclaration> getMethods() {
                return Arrays.asList(c.getDeclaredMethods()).stream().map(x -> new MethodDeclaration() {
                    @Override
                    public int getModifier() {
                        return x.getModifiers();
                    }

                    @Override
                    public String getName() {
                        return x.getName();
                    }

                    @Override
                    public List<ParameterInfo> getParameterTypes() {
                        return Arrays.asList(x.getParameterTypes()).stream()
                            .map(x -> new ParameterInfo(Descriptor.get(x), "<NA>"))
                            .collect(Collectors.toList());
                    }

                    @Override
                    public String getReturnTypeName() {
                        return x.getReturnType().getName();
                    }

                    @Override
                    public MethodDom build(ClassDeclaration classDeclaration, ClassInspector classInspector) {
                        MethodNode asmMethod = (MethodNode)classNode.methods.stream().filter(x -> ((MethodNode)x).name.equals(getName())).findFirst().get();
                        StatementDom body = v ->
                            v.visitASM(asmMethod);
                        return methodDeclaration(getModifier(), getName(), getParameterTypes(), getReturnTypeName(), body);
                    }
                }).collect(Collectors.toList());
            }

            @Override
            public int getModifiers() {
                return c.getModifiers();
            }

            @Override
            public String getName() {
                return c.getName();
            }

            @Override
            public String getSuperName() {
                return c.getSuperclass().getName();
            }

            @Override
            public List<String> getInterfaces() {
                return Arrays.asList(c.getInterfaces()).stream().map(x -> x.getName()).collect(Collectors.toList());
            }

            @Override
            public boolean isInterface() {
                return c.isInterface();
            }
        };
    }
}
