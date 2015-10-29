package astava.java.parser;

import astava.java.Descriptor;
import astava.tree.FieldDom;
import astava.tree.MethodDom;
import astava.tree.ParameterInfo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static astava.java.DomFactory.fieldDeclaration;

public class DefaultClassInspector implements ClassInspector {
    private ClassLoader classLoader;

    public DefaultClassInspector(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public ClassDeclaration getClassDeclaration(String name) {
        // Inspect virtual classes in class builders and physical classes in class loader
        try {
            System.out.println("Getting class " + name);
            Class<?> physicalClass;

            switch(name) {
                case "boolean": physicalClass = boolean.class; break;
                case "byte": physicalClass = byte.class; break;
                case "short": physicalClass = short.class; break;
                case "int": physicalClass = int.class; break;
                case "long": physicalClass = long.class; break;
                case "float": physicalClass = float.class; break;
                case "double": physicalClass = double.class; break;
                case "char": physicalClass = char.class; break;
                case "void":  physicalClass = void.class; break;
                default: physicalClass = classLoader.loadClass(name);
            }

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
                    return physicalClass.getSuperclass() != null ? physicalClass.getSuperclass().getName() : null;
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
}
