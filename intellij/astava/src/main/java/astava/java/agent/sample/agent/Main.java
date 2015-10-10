package astava.java.agent.sample.agent;

import astava.java.Descriptor;
import astava.java.DomFactory;
import astava.java.agent.*;
import astava.java.agent.Parser.ClassNodeExtenderParser;
import astava.java.parser.*;
import astava.tree.FieldDom;
import astava.tree.MethodDom;
import astava.tree.ParameterInfo;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static astava.java.DomFactory.fieldDeclaration;

/**
 * Created by jakob on 09-10-15.
 */
public class Main {
    public static void premain(String agentArgument, Instrumentation instrumentation) {
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



        ClassNodeExtenderParser myClassNodeExtenderParser = new ClassNodeExtenderParser(classResolver, classInspector);

        myClassNodeExtenderParser.extend("public java.lang.String myField = \"Hello1\";");
        myClassNodeExtenderParser.extend("public int myField3 = 8;");
        myClassNodeExtenderParser.extend("public java.lang.String toString() {return myField;}");

        SequenceClassNodeExtender myClassNodeExtender = new SequenceClassNodeExtender();

        //myClassNodeExtender.extend(ClassNodeExtenderFactory.addField(DomFactory.fieldDeclaration(Modifier.PUBLIC, "myField", "java/lang/String")));
        /*myClassNodeExtender.extend(ClassNodeExtenderFactory.addMethod(DomFactory.methodDeclaration(Modifier.PUBLIC, "toString", Arrays.asList(), "java/lang/String", DomFactory.ret(
            DomFactory.accessField(DomFactory.self(), "myField", "java/lang/String")
        ))));*/
        /*myClassNodeExtender.extend(MethodNodeExtenderFactory.setBody(DomFactory.block(Arrays.asList(
            DomFactory.assignField(DomFactory.self(), "myField", "java/lang/String", DomFactory.literal("Hello")),
            DomFactory.methodBody()
        ))).when((c, m) -> m.name.equals("<init>")));*/

        ConditionalClassNodeExtender extender = new ConditionalClassNodeExtender();

        extender.extend(x -> x.name.equals("astava/java/agent/sample/MyClass"), myClassNodeExtenderParser);
        extender.extend(x -> x.name.equals("astava/java/agent/sample/MyClass"), myClassNodeExtender);

        instrumentation.addTransformer(new ClassNodeTransformer(extender));
    }
}
