package astava.java.parser;

import astava.java.Descriptor;
import astava.tree.ClassDom;
import astava.tree.FieldDom;
import astava.tree.MethodDom;
import astava.tree.ParameterInfo;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static astava.java.Factory.*;
import static astava.java.Factory.ret;

public interface ClassDeclaration {
    List<FieldDeclaration> getFields();
    List<MethodDeclaration> getMethods();
    int getModifiers();
    String getName();
    String getSuperName();
    boolean isInterface();

    default ClassDeclaration extend(ClassDeclaration extension) {
        return new Mod(ClassDeclaration.this) {
            @Override
            protected List<FieldDeclaration> newFields() {
                return extension.getFields();
            }

            @Override
            protected List<MethodDeclaration> newMethods() {
                return extension.getMethods();
            }
        };
    };

    class Primitive implements ClassDeclaration {
        private String name;

        public Primitive(String name) {
            this.name = name;
        }

        @Override
        public List<FieldDeclaration> getFields() {
            return Collections.emptyList();
        }

        @Override
        public List<MethodDeclaration> getMethods() {
            return Collections.emptyList();
        }

        @Override
        public int getModifiers() {
            return Modifier.PUBLIC;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getSuperName() {
            return null;
        }

        @Override
        public boolean isInterface() {
            return false;
        }
    }

    class Mod implements ClassDeclaration {
        private ClassDeclaration source;

        public Mod(ClassDeclaration source) {
            this.source = source;
        }

        @Override
        public List<FieldDeclaration> getFields() {
            return Stream.concat(source.getFields().stream(), newFields().stream()).collect(Collectors.toList());
        }

        protected List<FieldDeclaration> newFields() {
            return Collections.emptyList();
        }

        @Override
        public List<MethodDeclaration> getMethods() {
            return Stream.concat(source.getMethods().stream(), newMethods().stream()).collect(Collectors.toList());
        }

        protected List<MethodDeclaration> newMethods() {
            return Collections.emptyList();
        }

        @Override
        public int getModifiers() {
            return source.getModifiers();
        }

        @Override
        public String getName() {
            return source.getName();
        }

        @Override
        public String getSuperName() {
            return source.getSuperName();
        }

        @Override
        public boolean isInterface() {
            return source.isInterface();
        }
    }

    default ClassDom build(ClassInspector classInspector) {
        List<FieldDom> fields = getFields().stream().map(x -> x.build(this)).collect(Collectors.toList());
        List<MethodDom> methods = getMethods().stream().map(x -> x.build(this, classInspector)).collect(Collectors.toList());

        return new ClassDom() {
            @Override
            public int getModifiers() {
                return ClassDeclaration.this.getModifiers();
            }

            @Override
            public String getName() {
                return ClassDeclaration.this.getName();
            }

            @Override
            public String getSuperName() {
                return ClassDeclaration.this.getSuperName();
            }

            @Override
            public List<FieldDom> getFields() {
                return fields;
            }

            @Override
            public List<MethodDom> getMethods() {
                return methods;
            }
        };
    }

    default ClassDeclaration withDefaultConstructor() {
        // Add default constructor if necessary
        boolean hasConstructors = getMethods().stream().anyMatch(x -> x.getName().equals("<init>"));
        if(!hasConstructors) {
            return new Mod(ClassDeclaration.this) {
                @Override
                protected List<MethodDeclaration> newMethods() {
                    return Arrays.asList(new MethodDeclaration() {
                        @Override
                        public int getModifiers() {
                            return Modifier.PUBLIC;
                        }

                        @Override
                        public String getName() {
                            return "<init>";
                        }

                        @Override
                        public List<ParameterInfo> getParameterTypes() {
                            return Arrays.asList();
                        }

                        @Override
                        public String getReturnTypeName() {
                            return Descriptor.VOID;
                        }

                        @Override
                        public MethodDom build(ClassDeclaration classDeclaration, ClassInspector classInspector) {
                            return methodDeclaration(Modifier.PUBLIC, "<init>", Arrays.asList(), Descriptor.VOID, block(Arrays.asList(
                                invokeSpecial(Descriptor.get("java/lang/Object"), "<init>", Descriptor.getMethodDescriptor(Arrays.asList(), Descriptor.VOID), self(), Arrays.asList()),
                                ret()
                            )));
                        }
                    });
                }
            };
        }

        return this;
    }
}
