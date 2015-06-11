package astava.java.parser;

import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import static astava.java.Factory.ret;

public class MutableClassDomBuilder implements ClassDomBuilder {
    private int modifier;
    private String name;
    private String superName;
    private Hashtable<String, FieldDomBuilder> fieldBuilders = new Hashtable<>();
    private Hashtable<String, MethodDomBuilder> methodBuilders = new Hashtable<>();
    //private ArrayList<FieldDomBuilder> fieldBuilders = new ArrayList<>();
    //private ArrayList<MethodDomBuilder> methodBuilders = new ArrayList<>();

    public void setModifier(int modifier) {
        this.modifier = modifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<FieldDomBuilder> getFields() {
        return fieldBuilders.values().stream().collect(Collectors.toList());
    }

    @Override
    public List<MethodDomBuilder> getMethods() {
        return methodBuilders.values().stream().collect(Collectors.toList());
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSuperName(String superName) {
        this.superName = superName;
    }

    @Override
    public String getSuperName() {
        return superName;
    }

    public void addField(FieldDomBuilder fieldBuilder) {
        fieldBuilders.put(fieldBuilder.getName(), fieldBuilder);
    }

    public void addMethod(MethodDomBuilder methodBuilder) {
        methodBuilders.put(methodBuilder.getName(), methodBuilder);
    }



    @Override
    public ClassDeclaration build(ClassResolver classResolver) {
        List<FieldDeclaration> fieldDeclarations = getFields().stream().map(x -> x.declare(classResolver)).collect(Collectors.toList());
        List<MethodDeclaration> methodDeclarations = getMethods().stream().map(x -> x.declare(classResolver)).collect(Collectors.toList());

        /*// Add default constructor if necessary
        boolean hasConstructors = methodDeclarations.stream().anyMatch(x -> x.getName().equals("<init>"));
        if(!hasConstructors) {
            methodDeclarations.add(new MethodDeclaration() {
                @Override
                public int getModifier() {
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
                return modifier;
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
            public int getModifier() {
                return modifier;
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
