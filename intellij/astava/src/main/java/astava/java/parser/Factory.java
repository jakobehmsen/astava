package astava.java.parser;

import astava.debug.Debug;
import astava.java.Descriptor;
import astava.java.Invocation;
import astava.tree.*;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public static MethodDomBuilder method(int modifier, String name, List<ParameterInfo> tmpParameters, String returnTypeName, StatementDom body) {
        return method(modifier, name, tmpParameters, returnTypeName, Arrays.asList(new StatementDomBuilder() {
            @Override
            public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals) {
                return body;
            }
        }));
    }

    public static MethodDomBuilder method(int modifier, String name, List<ParameterInfo> tmpParameters, String returnTypeName, List<StatementDomBuilder> statementBuilders) {
        return new MethodDomBuilder() {
            @Override
            public MethodDeclaration declare(ClassResolver classResolver) {
                List<ParameterInfo> parameters = tmpParameters.stream()
                    .map(x -> new ParameterInfo(Parser.parseTypeQualifier(classResolver, x.descriptor), x.name))
                    .collect(Collectors.toList());

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
                        //return astava.java.Factory.methodDeclaration(modifier, name, parameters, Descriptor.get(returnTypeName), body.build(classResolver, classDeclaration, classInspector, new HashSet<>()));

                        boolean isConstructor = name.equals("<init>");

                        Hashtable<String, String> locals = new Hashtable<>();
                        locals.putAll(parameters.stream().collect(Collectors.toMap(x -> x.name, x -> x.descriptor)));
                        //locals.addAll(parameters.stream().map(x -> x.name).collect(Collectors.toList()));
                        //List<StatementDomBuilder> statementBuilders = ctx.statement().stream().map(x -> parseStatementBuilder(x, false)).collect(Collectors.toList());
                        statementBuilders.forEach(x -> x.appendLocals(locals));
                        List<StatementDom> statements = statementBuilders.stream().map(x -> x.build(classResolver, classDeclaration, classInspector, locals)).collect(Collectors.toList());
                        StatementDom body = astava.java.Factory.block(statements);

                        String returnType = Descriptor.get(returnTypeName);

                        // Ugly hack
                        // Instead: every leaf statement should either be a ret statement or one is injected
                        // This logic probably shouldn't be located here?
                        if(returnType.equals(Descriptor.VOID)) {
                            statements.add(astava.java.Factory.ret());
                        }

                        if(isConstructor) {
                            // Call super constructor
                            statements.add(0,
                                astava.java.Factory.invokeSpecial(Descriptor.get(classDeclaration.getSuperName()), "<init>", Descriptor.getMethodDescriptor(Arrays.asList(), Descriptor.VOID), astava.java.Factory.self(), Arrays.asList())
                            );
                        }

                        return astava.java.Factory.methodDeclaration(modifier, name, parameters, returnType, body);
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

    public static ExpressionDomBuilder ambiguousName(List<String> nameParts) {
        return new ExpressionDomBuilder() {
            @Override
            public ExpressionDom build(ClassResolver cr, ClassDeclaration cd, ClassInspector ci, Map<String, String> locals) {
                return Parser.parseAmbiguousName(nameParts, cr, cd,
                    name -> {
                        Optional<FieldDeclaration> fieldDeclaration = cd.getFields().stream().filter(x -> x.getName().equals(name)).findFirst();

                        if (fieldDeclaration.isPresent()) {
                            String descriptor = Descriptor.get(fieldDeclaration.get().getTypeName());

                            if (Modifier.isStatic(fieldDeclaration.get().getModifier()))
                                return astava.java.Factory.accessStaticField(cd.getName(), name, descriptor);

                            return astava.java.Factory.accessField(astava.java.Factory.self(), name, descriptor);
                        }

                        return astava.java.Factory.accessVar(name);
                    },
                    (target, fieldChainAccess) -> {
                        for (String fieldName : fieldChainAccess)
                            target = Parser.fieldAccess(cr, cd, ci, target, fieldName, locals);

                        return target;
                    }
                );
            }

            @Override
            public String toString() {
                return nameParts.stream().collect(Collectors.joining("."));
            }
        };
    }

    public static StatementDomBuilder assign(String name, ExpressionDomBuilder valueBuilder) {
        return new StatementDomBuilder() {
            @Override
            public void appendLocals(Map<String, String> locals) {

            }

            @Override
            public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals) {
                ExpressionDom value = valueBuilder.build(classResolver, classDeclaration, classInspector, locals);

                Optional<FieldDeclaration> fieldDeclaration = classDeclaration.getFields().stream().filter(x -> x.getName().equals(name)).findFirst();
                if (fieldDeclaration.isPresent()) {
                    String descriptor = Descriptor.get(fieldDeclaration.get().getTypeName());

                    if (Modifier.isStatic(fieldDeclaration.get().getModifier()))
                        return astava.java.Factory.assignStaticField(classDeclaration.getName(), fieldDeclaration.get().getName(), descriptor, value);

                    return astava.java.Factory.assignField(astava.java.Factory.self(), fieldDeclaration.get().getName(), descriptor, value);
                }

                return astava.java.Factory.assignVar(name, value);
            }

            @Override
            public String toString() {
                return name + " = " + valueBuilder + ";";
            }
        };
    }

    public static ExpressionDomBuilder assignExpr(String name, ExpressionDomBuilder valueBuilder) {
        return new ExpressionDomBuilder() {
            @Override
            public ExpressionDom build(ClassResolver cr, ClassDeclaration cd, ClassInspector ci, Map<String, String> locals) {
                ExpressionDom value = valueBuilder.build(cr, cd, ci, locals);

                Optional<FieldDeclaration> fieldDeclaration = cd.getFields().stream().filter(x -> x.getName().equals(name)).findFirst();
                if (fieldDeclaration.isPresent()) {
                    String descriptor = Descriptor.get(fieldDeclaration.get().getTypeName());

                    /*
                    if (Modifier.isStatic(fieldDeclaration.get().getModifier())) {
                        return astava.java.Factory.assignStaticField(cd.getName(), fieldDeclaration.get().getName(), descriptor, value);
                    }

                    return astava.java.Factory.assignField(astava.java.Factory.self(), fieldDeclaration.get().getName(), descriptor, value);
                    */

                    return astava.java.Factory.top(astava.java.Factory.self(), (newTarget, newTargetLast) -> {
                        return astava.java.Factory.blockExpr(Arrays.asList(
                            astava.java.Factory.assignField(newTarget, fieldDeclaration.get().getName(), descriptor, value),
                            Parser.fieldAccess(cr, cd, ci, newTargetLast, name, locals)
                        ));
                    });
                }

                return astava.java.Factory.blockExpr(Arrays.asList(
                    astava.java.Factory.assignVar(name, value),
                    astava.java.Factory.accessVar(name)
                ));
            }

            @Override
            public String toString() {
                return name + " = " + valueBuilder;
            }
        };
    }

    public static ExpressionDomBuilder newInstanceExpr(String name, List<ExpressionDomBuilder> argumentBuilders) {
        return new ExpressionDomBuilder() {
            @Override
            public ExpressionDom build(ClassResolver cr, ClassDeclaration cd, ClassInspector ci, Map<String, String> locals) {
                List<ExpressionDom> arguments = argumentBuilders.stream()
                    .map(x -> x.build(cr, cd, ci, locals)).collect(Collectors.toList());

                ClassDeclaration targetClassDeclaration = ci.getClassDeclaration(name);

                List<ClassDeclaration> argumentTypes = arguments.stream().map(x -> {
                    String expressionResultType = Parser.expressionResultType(ci, cd, x, locals);
                    String expressionResultTypeName = Descriptor.getName(expressionResultType);

                    return ci.getClassDeclaration(expressionResultTypeName);
                }).collect(Collectors.toList());

                // Find best matching constructor
                List<MethodDeclaration> constructors = targetClassDeclaration.getMethods().stream()
                    .filter(x -> x.getName().equals("<init>"))
                    .filter(x -> x.getParameterTypes().size() == arguments.size())
                    .filter(x -> IntStream.range(0, arguments.size()).allMatch(i ->
                        // Compare full inheritance
                        Descriptor.getName(x.getParameterTypes().get(0).descriptor).equals(argumentTypes.get(i).getName())))
                    .collect(Collectors.toList());

                // For now, just pick the first
                MethodDeclaration constructor = constructors.get(0);

                return astava.java.Factory.newInstanceExpr(
                    Descriptor.get(targetClassDeclaration.getName()),
                    constructor.getParameterTypes().stream().map(x -> x.descriptor).collect(Collectors.toList()),
                    arguments);
            }

            @Override
            public String toString() {
                return "new " + name + "(" + argumentBuilders.stream().map(x -> x.toString()).collect(Collectors.joining(", ")) + ")";
            }
        };
    }

    public static StatementDomBuilder invocation(ExpressionDomBuilder targetBuilder, String methodName, List<ExpressionDomBuilder> argumentBuilders) {
        return new StatementDomBuilder() {
            @Override
            public StatementDom build(ClassResolver cr, ClassDeclaration cd, ClassInspector ci, Map<String, String> locals) {
                ExpressionDom target = targetBuilder.build(cr, cd, ci, locals);
                List<ExpressionDom> arguments = argumentBuilders.stream()
                    .map(x -> x.build(cr, cd, ci, locals)).collect(Collectors.toList());

                String targetType = Parser.expressionResultType(ci, cd, target, locals);
                ClassDeclaration targetClassDeclaration = ci.getClassDeclaration(Descriptor.getName(targetType));

                List<ClassDeclaration> argumentTypes = arguments.stream().map(x -> {
                    String expressionResultType = Parser.expressionResultType(ci, cd, x, locals);
                    String expressionResultTypeName = Descriptor.getName(expressionResultType);

                    return ci.getClassDeclaration(expressionResultTypeName);
                }).collect(Collectors.toList());

                return Parser.resolveMethod(ci, targetClassDeclaration, methodName, argumentTypes, (c, m) -> {
                    int invocation = c.isInterface() ? Invocation.INTERFACE : Invocation.VIRTUAL;

                    String methodDescriptor =
                        Descriptor.getMethodDescriptor(m.getParameterTypes().stream().map(x -> x.descriptor).collect(Collectors.toList()),
                            Descriptor.get(m.getReturnTypeName())
                        );

                    Debug.getPrintStream(Debug.LEVEL_HIGH).println("@invocationExpr: methodDescriptor=" + methodDescriptor);

                    String declaringClassDescriptor = Descriptor.get(c.getName());
                    return astava.java.Factory.invoke(invocation, declaringClassDescriptor, methodName, methodDescriptor, target, arguments);
                });
            }

            @Override
            public String toString() {
                return targetBuilder + "." + methodName + "(" + argumentBuilders.stream().map(x -> x.toString()).collect(Collectors.joining(", ")) + ")";
            }
        };
    }

    public static ExpressionDomBuilder invocationExpr(ExpressionDomBuilder targetBuilder, String methodName, List<ExpressionDomBuilder> argumentBuilders) {
        return new ExpressionDomBuilder() {
            @Override
            public ExpressionDom build(ClassResolver cr, ClassDeclaration cd, ClassInspector ci, Map<String, String> locals) {
                ExpressionDom target = targetBuilder.build(cr, cd, ci, locals);
                List<ExpressionDom> arguments = argumentBuilders.stream()
                    .map(x -> x.build(cr, cd, ci, locals)).collect(Collectors.toList());

                String targetType = Parser.expressionResultType(ci, cd, target, locals);
                ClassDeclaration targetClassDeclaration = ci.getClassDeclaration(Descriptor.getName(targetType));

                List<ClassDeclaration> argumentTypes = arguments.stream().map(x -> {
                    String expressionResultType = Parser.expressionResultType(ci, cd, x, locals);
                    String expressionResultTypeName = Descriptor.getName(expressionResultType);

                    return ci.getClassDeclaration(expressionResultTypeName);
                }).collect(Collectors.toList());

                return Parser.resolveMethod(ci, targetClassDeclaration, methodName, argumentTypes, (c, m) -> {
                    int invocation = c.isInterface() ? Invocation.INTERFACE : Invocation.VIRTUAL;

                    String methodDescriptor =
                        Descriptor.getMethodDescriptor(m.getParameterTypes().stream().map(x -> x.descriptor).collect(Collectors.toList()),
                            Descriptor.get(m.getReturnTypeName())
                        );

                    Debug.getPrintStream(Debug.LEVEL_HIGH).println("@invocationExpr: methodDescriptor=" + methodDescriptor);

                    String declaringClassDescriptor = Descriptor.get(c.getName());
                    return astava.java.Factory.invokeExpr(invocation, declaringClassDescriptor, methodName, methodDescriptor, target, arguments);
                });
            }

            @Override
            public String toString() {
                return targetBuilder + "." + methodName + "(" + argumentBuilders.stream().map(x -> x.toString()).collect(Collectors.joining(", ")) + ")";
            }
        };
    }
}