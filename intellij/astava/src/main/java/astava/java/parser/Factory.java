package astava.java.parser;

import astava.java.Descriptor;
import astava.tree.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Factory {
    public static StatementDomBuilder block(List<StatementDomBuilder> statementBuilders) {
        return new StatementDomBuilder() {
            @Override
            public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals) {
                List<StatementDom> statements =
                    statementBuilders.stream().map(x -> x.build(classResolver, classDeclaration, classInspector, locals)).collect(Collectors.toList());
                return astava.java.Factory.block(statements);
            }

            @Override
            public String toString() {
                return statementBuilders.stream().map(x -> x.toString()).collect(Collectors.joining("\n"));
            }
        };
    }

    public static StatementDomBuilder ret() {
        return new StatementDomBuilder() {
            @Override
            public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals) {
                return astava.java.Factory.ret();
            }

            @Override
            public String toString() {
                return "return;";
            }
        };
    }

    public static StatementDomBuilder ret(ExpressionDomBuilder expression) {
        return new StatementDomBuilder() {
            @Override
            public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals) {
                return astava.java.Factory.ret(expression.build(classResolver, classDeclaration, classInspector, locals));
            }

            @Override
            public String toString() {
                return "return " + expression + ";";
            }
        };
    }

    public static ExpressionDomBuilder literal(int value) {
        return new ExpressionDomBuilder() {
            @Override
            public ExpressionDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals) {
                return astava.java.Factory.literal(value);
            }

            @Override
            public String toString() {
                return "" + value;
            }
        };
    }

    public static ExpressionDomBuilder literal(String value) {
        return new ExpressionDomBuilder() {
            @Override
            public ExpressionDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals) {
                return astava.java.Factory.literal(value);
            }

            @Override
            public String toString() {
                return "\"" + value + "\"";
            }
        };
    }

    public static FieldDomBuilder field(int modifier, String name, String typeName) {
        return new FieldDomBuilder() {
            @Override
            public FieldDeclaration declare(ClassResolver classResolver) {
                return new FieldDeclaration() {
                    @Override
                    public int getModifier() {
                        return modifier;
                    }

                    @Override
                    public String getTypeName() {
                        return typeName;
                    }

                    @Override
                    public String getName() {
                        return name;
                    }

                    @Override
                    public FieldDom build(ClassDeclaration classDeclaration) {
                        return astava.java.Factory.fieldDeclaration(modifier, name, Descriptor.get(typeName));
                    }
                };
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    public static ExpressionDomBuilder nil() {
        return new ExpressionDomBuilder() {
            @Override
            public ExpressionDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals) {
                return astava.java.Factory.nil();
            }

            @Override
            public String toString() {
                return "null";
            }
        };
    }

    public static ExpressionDomBuilder self() {
        return new ExpressionDomBuilder() {
            @Override
            public ExpressionDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals) {
                return astava.java.Factory.self();
            }

            @Override
            public String toString() {
                return "this";
            }
        };
    }

    public static MethodDomBuilder method(int modifier, String name, List<ParameterInfo> parameters, String returnTypeName, StatementDom body) {
        return new MethodDomBuilder() {
            @Override
            public MethodDeclaration declare(ClassResolver classResolver) {
                return new MethodDeclaration() {
                    @Override
                    public int getModifier() {
                        return modifier;
                    }

                    @Override
                    public String getName() {
                        return name;
                    }

                    @Override
                    public List<ParameterInfo> getParameterTypes() {
                        return parameters;
                    }

                    @Override
                    public String getReturnTypeName() {
                        return returnTypeName;
                    }

                    @Override
                    public MethodDom build(ClassDeclaration classDeclaration, ClassInspector classInspector) {
                        return astava.java.Factory.methodDeclaration(modifier, name, parameters, Descriptor.get(returnTypeName), body);
                    }
                };
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    public static StatementDomBuilder assignField(ExpressionDomBuilder targetBuilder, String name, ExpressionDomBuilder valueBuilder) {
        return new StatementDomBuilder() {
            @Override
            public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals) {
                ExpressionDom target = targetBuilder.build(classResolver, classDeclaration, classInspector, locals);
                ExpressionDom value = valueBuilder.build(classResolver, classDeclaration, classInspector, locals);

                Optional<FieldDeclaration> fieldDeclaration = classDeclaration.getFields().stream().filter(x -> x.getName().equals(name)).findFirst();
                String descriptor = Descriptor.get(fieldDeclaration.get().getTypeName());
                return astava.java.Factory.assignField(target, fieldDeclaration.get().getName(), descriptor, value);
            }

            @Override
            public String toString() {
                return targetBuilder + "." + name + " = " + valueBuilder;
            }
        };
    }
}
