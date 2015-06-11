package astava.java.parser;

import astava.java.Descriptor;
import astava.tree.*;

import java.util.List;
import java.util.Map;

import static astava.java.Factory.fieldDeclaration;
import static astava.java.Factory.methodDeclaration;

public class Factory {
    public static StatementDomBuilder ret(ExpressionDomBuilder expression) {
        return new StatementDomBuilder() {
            @Override
            public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals) {
                return astava.java.Factory.ret(expression.build(classResolver, classDeclaration, classInspector, locals));
            }
        };
    }

    public static ExpressionDomBuilder literal(int value) {
        return new ExpressionDomBuilder() {
            @Override
            public ExpressionDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals) {
                return astava.java.Factory.literal(value);
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
                        return fieldDeclaration(modifier, name, Descriptor.get(typeName));
                    }
                };
            }

            @Override
            public String getName() {
                return name;
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
                        return methodDeclaration(modifier, name, parameters, Descriptor.get(returnTypeName), body);
                    }
                };
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }
}
