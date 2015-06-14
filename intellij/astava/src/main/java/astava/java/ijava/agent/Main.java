package astava.java.ijava.agent;

import astava.debug.Debug;
import astava.java.Descriptor;
import astava.java.gen.ClassGenerator;
import astava.java.ijava.DebugClassLoader;
import astava.java.parser.*;
import astava.tree.ClassDom;
import astava.tree.FieldDom;
import astava.tree.MethodDom;
import astava.tree.ParameterInfo;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static astava.java.Factory.fieldDeclaration;

public class Main {
    public static void premain(String agentArgument, Instrumentation instrumentation) {

        DataInputStream input = new DataInputStream(System.in);
        Field outField = null;
        try {
            outField = FilterOutputStream.class.getDeclaredField("out");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        outField.setAccessible(true);

        OutputStream outputStream = null;
        try {
            outputStream = (OutputStream)outField.get(System.out);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        DataOutputStream output = new DataOutputStream(outputStream);

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        ClassResolver classResolver = new ClassResolver() {
            private Map<String, String> simpleNameToNameMap = Arrays.asList(
                String.class,
                Modifier.class,
                Object.class
            )
                .stream().map(x -> new AbstractMap.SimpleImmutableEntry<>(x.getSimpleName(), x.getName()))
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
            private Set<String> nameSet = simpleNameToNameMap.values().stream().collect(Collectors.toSet());

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
        };

        ClassInspector classInspector = new ClassInspector() {
            @Override
            public ClassDeclaration getClassDeclaration(String name) {
                // Inspect virtual classes in class builders and physical classes in class loader
                try {
                    Class<?> physicalClass = classLoader.loadClass(name);

                    //logln("Loaded physical class " + name);

                    return new ClassDeclaration() {
                        @Override
                        public List<FieldDeclaration> getFields() {
                            return Arrays.asList(physicalClass.getDeclaredFields()).stream().map(x -> new FieldDeclaration() {
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
                            List<MethodDeclaration> methods = Arrays.asList(physicalClass.getDeclaredMethods()).stream().map(x -> new MethodDeclaration() {
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
                                    return null;
                                }
                            }).collect(Collectors.toList());

                            List<MethodDeclaration> constructors = Arrays.asList(physicalClass.getConstructors()).stream().map(x -> new MethodDeclaration() {
                                @Override
                                public int getModifier() {
                                    return x.getModifiers();
                                }

                                @Override
                                public String getName() {
                                    return "<init>";
                                }

                                @Override
                                public List<ParameterInfo> getParameterTypes() {
                                    return Arrays.asList(x.getParameterTypes()).stream()
                                        .map(x -> new ParameterInfo(Descriptor.get(x), "<NA>"))
                                        .collect(Collectors.toList());
                                }

                                @Override
                                public String getReturnTypeName() {
                                    return "void";
                                }

                                @Override
                                public MethodDom build(ClassDeclaration classDeclaration, ClassInspector classInspector) {
                                    return null;
                                }
                            }).collect(Collectors.toList());

                            return Stream.concat(methods.stream(), constructors.stream()).collect(Collectors.toList());
                        }

                        @Override
                        public int getModifiers() {
                            return physicalClass.getModifiers();
                        }

                        @Override
                        public String getName() {
                            return physicalClass.getName();
                        }

                        @Override
                        public String getSuperName() {
                            return physicalClass.getSuperclass().getName();
                        }

                        @Override
                        public List<String> getInterfaces() {
                            return Arrays.asList(physicalClass.getInterfaces()).stream().map(x -> x.getName()).collect(Collectors.toList());
                        }

                        @Override
                        public boolean isInterface() {
                            return physicalClass.isInterface();
                        }
                    };
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };

        /*System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        }));*/

        try {
            //log("Initializing agent...");

            ObjectInputStream objectInputStream = new ObjectInputStream(input);
            Map<String, ClassDomBuilder> classBuilders = (Map<String, ClassDomBuilder>)objectInputStream.readObject();

            instrumentation.addTransformer(new ClassFileTransformer() {
                @Override
                public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                    if (className != null && className.contains("ArrayList"))
                        new String();

                    String classNameName = Descriptor.getName(className);
                    ClassDomBuilder classBuilder = classBuilders.get(classNameName);

                    if (loader instanceof DebugClassLoader) {
                        ((DebugClassLoader) loader).println("***DEBUG***");
                    }

                    //Debug.getPrintStream(Debug.LEVEL_HIGH).println("sgfs");

                    if (classBuilder != null) {
                        ClassReader cr = new ClassReader(classfileBuffer);
                        ClassNode classNode = new ClassNode(Opcodes.ASM5);
                        cr.accept(classNode, ClassReader.EXPAND_FRAMES);

                        MutableClassDomBuilder combinedBuilder = new MutableClassDomBuilder();

                        // Populate from class node
                        combinedBuilder.setName(className);
                        combinedBuilder.setSuperName(classNode.superName);
                        combinedBuilder.setModifier(classNode.access);
                        classNode.interfaces.forEach(x -> combinedBuilder.addInterface(Descriptor.getName((String) x)));

                        for (Object field : classNode.fields) {
                            FieldDomBuilder fieldBuilder = Factory.field(
                                ((FieldNode) field).access,
                                ((FieldNode) field).name,
                                Descriptor.getName(((FieldNode) field).desc)
                            );
                            combinedBuilder.addField(fieldBuilder);
                        }

                        for (Object method : classNode.methods) {
                            MethodNode methodNode = (MethodNode) method;
                            String returnTypeName = Type.getReturnType(methodNode.desc).getClassName();
                            Type[] parameterTypes = Type.getArgumentTypes(methodNode.desc);
                            List<ParameterInfo> parameters = IntStream.range(0, parameterTypes.length).mapToObj(i -> {
                                String name;
                                if(methodNode.parameters != null) {
                                    ParameterNode pm = (ParameterNode) methodNode.parameters.get(i);
                                    name = pm.name;
                                } else name = "p" + i;

                                //String descriptor = Descriptor.get(parameterTypes[i].getClassName());
                                //String typeName = Descriptor.getName(descriptor);
                                String typeName = parameterTypes[i].getClassName();

                                return new ParameterInfo(typeName, name);
                            }).collect(Collectors.toList());
                            MethodDomBuilder methodBuilder = Factory.method(
                                ((MethodNode) method).access,
                                ((MethodNode) method).name,
                                parameters,
                                returnTypeName,
                                v -> v.visitASM(methodNode));
                            combinedBuilder.addMethod(methodBuilder);
                        }

                        // Populate from class builder
                        classBuilder.getFields().forEach(x -> combinedBuilder.addField(x));
                        classBuilder.getMethods().forEach(x -> combinedBuilder.addMethod(x));

                        try {
                            ClassDeclaration classDeclaration = combinedBuilder.build(classResolver);
                            ClassDom classDom = classDeclaration.build(classInspector);
                            ClassGenerator classGenerator = new ClassGenerator(classDom);

                            return classGenerator.toBytes();
                        } catch(Exception e) {
                            e.toString();
                        }
                    }

                    return classfileBuffer;
                }
            }, true);

            Class<?>[] classesToReTransform = classBuilders.values().stream()
                .map(x -> {
                    try {
                        return ClassLoader.getSystemClassLoader().loadClass(x.getName());
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        return null;
                    }
                }).toArray(s -> new Class<?>[s]);
            instrumentation.retransformClasses(classesToReTransform);

            //log("Initialized agent.");
        } catch (IOException e) {
            //log("Error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            //log("Error: " + e.getMessage());
        } catch (UnmodifiableClassException e) {
            new String();
            //log("Error: " + e.getMessage());
        } catch (Throwable e) {
            //log("Error: " + e.getMessage());
            new String();
        }

        /*try {
            String message = input.readUTF();
            output.writeUTF("Received: " + message);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public static void log(String message) {
        //System.console().printf(message + "\n");
        //astava.java.ijava.server.Main.log(message);
    }
}
