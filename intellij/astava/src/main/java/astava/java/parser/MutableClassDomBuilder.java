package astava.java.parser;

import astava.java.Descriptor;
import astava.tree.ClassDom;
import astava.tree.FieldDom;
import astava.tree.MethodDom;
import astava.tree.ParameterInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static astava.java.Factory.*;
import static astava.java.Factory.ret;

public class MutableClassDomBuilder implements ClassDomBuilder {
    private int modifiers;
    private String name;
    private String superName;
    private ArrayList<FieldDomBuilder> fieldBuilders = new ArrayList<>();
    private ArrayList<MethodDomBuilder> methodBuilders = new ArrayList<>();

    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSuperName(String superName) {
        this.superName = superName;
    }

    public void addField(FieldDomBuilder fieldBuilder) {
        fieldBuilders.add(fieldBuilder);
    }

    public void addMethod(MethodDomBuilder methodBuilder) {
        methodBuilders.add(methodBuilder);
    }



    @Override
    public ClassDeclaration build(ClassResolver classResolver) {
        List<FieldDeclaration> fieldDeclarations = fieldBuilders.stream().map(x -> x.declare(classResolver)).collect(Collectors.toList());
        List<MethodDeclaration> methodDeclarations = methodBuilders.stream().map(x -> x.declare(classResolver)).collect(Collectors.toList());

        /*// Add default constructor if necessary
        boolean hasConstructors = methodDeclarations.stream().anyMatch(x -> x.getName().equals("<init>"));
        if(!hasConstructors) {
            methodDeclarations.add(new MethodDeclaration() {
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
                public MethodDom build(ClassDeclaration classDeclaration) {
                    return methodDeclaration(Modifier.PUBLIC, "<init>", Arrays.asList(), Descriptor.VOID, block(Arrays.asList(
                        invokeSpecial(Descriptor.get("java/lang/Object"), "<init>", Descriptor.getMethodDescriptor(Arrays.asList(), Descriptor.VOID), self(), Arrays.asList()),
                        ret()
                    )));
                }
            });
        }*/

        return new ClassDeclaration() {
            @Override
            public List<FieldDeclaration> getFields() {
                return fieldDeclarations;
            }

            @Override
            public List<MethodDeclaration> getMethods() {
                return methodDeclarations;
            }

            @Override
            public int getModifiers() {
                return modifiers;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getSuperName() {
                return superName;
            }

            @Override
            public boolean isInterface() {
                return false;
            }
        };

        /*List<FieldDom> fields = fieldDeclarations.stream().map(x -> x.build(cd)).collect(Collectors.toList());
        List<MethodDom> methods = methodDeclarations.stream().map(x -> x.build(cd)).collect(Collectors.toList());

        return new ClassDom() {
            @Override
            public int getModifiers() {
                return modifiers;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getSuperName() {
                return superName;
            }

            @Override
            public List<FieldDom> getFields() {
                return fields;
            }

            @Override
            public List<MethodDom> getMethods() {
                return methods;
            }
        };*/
    }
}
