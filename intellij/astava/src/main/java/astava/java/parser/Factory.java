package astava.java.parser;

import astava.debug.Debug;
import astava.java.Descriptor;
import astava.java.DomFactory;
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
            public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
                List<StatementDom> statements =
                    statementBuilders.stream().map(x -> x.build(classResolver, classDeclaration, classInspector, locals, methodContext)).collect(Collectors.toList());
                return DomFactory.block(statements);
            }

            @Override
            public String toString() {
                return statementBuilders.stream().map(x -> x.toString()).collect(Collectors.joining("\n"));
            }

            @Override
            public boolean test(CodeDom code, List<Object> captures) {
                return false;
            }

            @Override
            public boolean test(StatementDom statement, List<Object> captures) {
                return Util.returnFrom(r -> statement.accept(new DefaultStatementDomVisitor() {
                    @Override
                    public void visitBlock(List<StatementDom> statements) {
                        r.accept(statementBuilders.size() == statements.size() &&
                            IntStream.range(0, statementBuilders.size()).allMatch(i ->
                                statementBuilders.get(i).test(statements.get(i), captures)));
                    }
                }), () -> {
                    if(statementBuilders.size() == 1)
                        return statementBuilders.get(0).test(statement, captures);
                    return false;
                });
            }
        };
    }

    public static StatementDomBuilder ret() {
        return new StatementDomBuilder() {
            @Override
            public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
                return DomFactory.ret();
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
            public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
                return DomFactory.ret(expression.build(classResolver, classDeclaration, classInspector, locals, methodContext));
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
            public ExpressionDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
                return DomFactory.literal(value);
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
            public ExpressionDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
                return DomFactory.literal(value);
            }

            @Override
            public String toString() {
                return "\"" + value + "\"";
            }
        };
    }

    public static ExpressionDomBuilder literal(boolean value) {
        return new ExpressionDomBuilder() {
            @Override
            public ExpressionDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
                return DomFactory.literal(value);
            }

            @Override
            public String toString() {
                return "" + value + "";
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
                        return DomFactory.fieldDeclaration(modifier, name, Descriptor.get(typeName));
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
            public ExpressionDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
                return DomFactory.nil();
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
            public ExpressionDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
                return DomFactory.self();
            }

            @Override
            public String toString() {
                return "this";
            }

            @Override
            public boolean test(ExpressionDom expression, List<Object> captures) {
                return Util.returnFrom(false, r -> expression.accept(new DefaultExpressionDomVisitor() {
                    @Override
                    public void visitThis() {
                        r.accept(true);
                    }
                }));
            }
        };
    }

    public static MethodDomBuilder method(int modifier, String name, List<ParameterInfo> tmpParameters, String returnTypeName, StatementDom body) {
        return method(modifier, name, tmpParameters, returnTypeName, Arrays.asList(new StatementDomBuilder() {
            @Override
            public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
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
                        List<StatementDom> statements = statementBuilders.stream().map(x -> x.build(classResolver, classDeclaration, classInspector, locals, this)).collect(Collectors.toList());
                        StatementDom body = DomFactory.block(statements);

                        String returnType = Descriptor.get(returnTypeName);

                        // Ugly hack
                        // Instead: every leaf statement should either be a ret statement or one is injected
                        // This logic probably shouldn't be located here?
                        if(returnType.equals(Descriptor.VOID)) {
                            statements.add(DomFactory.ret());
                        }

                        if(isConstructor) {
                            // Call super constructor
                            statements.add(0,
                                DomFactory.invokeSpecial(Descriptor.get(classDeclaration.getSuperName()), "<init>", Descriptor.getMethodDescriptor(Arrays.asList(), Descriptor.VOID), DomFactory.self(), Arrays.asList())
                            );
                        }

                        return DomFactory.methodDeclaration(modifier, name, parameters, returnType, body);
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
            public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
                ExpressionDom target = targetBuilder.build(classResolver, classDeclaration, classInspector, locals, methodContext);
                ExpressionDom value = valueBuilder.build(classResolver, classDeclaration, classInspector, locals, methodContext);

                Optional<FieldDeclaration> fieldDeclaration = classDeclaration.getFields().stream().filter(x -> x.getName().equals(name)).findFirst();
                String descriptor = Descriptor.get(fieldDeclaration.get().getTypeName());
                return DomFactory.assignField(target, fieldDeclaration.get().getName(), descriptor, value);
            }

            @Override
            public String toString() {
                return targetBuilder + "." + name + " = " + valueBuilder;
            }

            @Override
            public boolean test(StatementDom statement, List<Object> captures) {
                return Util.returnFrom(false, r -> statement.accept(new DefaultStatementDomVisitor() {
                    String nameTest = name;

                    @Override
                    public void visitFieldAssignment(ExpressionDom target, String name, String type, ExpressionDom value) {
                        if(!targetBuilder.test(target, captures))
                            r.accept(false);

                        if(nameTest != null && !nameTest.equals(name))
                            r.accept(false);

                        if(!valueBuilder.test(value, captures))
                            r.accept(false);

                        r.accept(true);
                    }
                }));
            }
        };
    }

    public static ExpressionDomBuilder ambiguousName(List<String> nameParts) {
        return new ExpressionDomBuilder() {
            @Override
            public ExpressionDom build(ClassResolver cr, ClassDeclaration cd, ClassInspector ci, Map<String, String> locals, MethodDeclaration methodContext) {
                return Parser.parseAmbiguousName(nameParts, cr, cd,
                    name -> {
                        if(locals.containsKey(name)) {
                            return DomFactory.accessVar(name);
                        } else {
                            Optional<FieldDeclaration> fieldDeclaration = cd.getFields().stream().filter(x -> x.getName().equals(name)).findFirst();

                            if (fieldDeclaration.isPresent()) {
                                String descriptor = Descriptor.get(fieldDeclaration.get().getTypeName());

                                // Should this be supported??? Static access?
                                if (Modifier.isStatic(fieldDeclaration.get().getModifier()))
                                    return DomFactory.accessStaticField(Descriptor.get(cd.getName()), name, descriptor);

                                return DomFactory.accessField(DomFactory.self(), name, descriptor);
                            }
                        }

                        return null;
                    },
                    (className, fieldName) -> {
                        ClassDeclaration classDeclaration = ci.getClassDeclaration(className);
                        Optional<FieldDeclaration> fieldDeclaration =
                            classDeclaration.getFields().stream().filter(x -> x.getName().equals(fieldName)).findFirst();

                        if (fieldDeclaration.isPresent()) {
                            String descriptor = Descriptor.get(fieldDeclaration.get().getTypeName());
                            return DomFactory.accessStaticField(Descriptor.get(classDeclaration.getName()), fieldName, descriptor);
                        }

                        return null;
                    },
                    (target, fieldChainAccess) -> {
                        for (String fieldName : fieldChainAccess)
                            target = Parser.fieldAccess(cr, cd, ci, target, fieldName, locals, methodContext);

                        return target;
                    },
                    locals);
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
            public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
                ExpressionDom value = valueBuilder.build(classResolver, classDeclaration, classInspector, locals, methodContext);

                Optional<FieldDeclaration> fieldDeclaration = classDeclaration.getFields().stream().filter(x -> x.getName().equals(name)).findFirst();
                if (fieldDeclaration.isPresent()) {
                    String descriptor = Descriptor.get(fieldDeclaration.get().getTypeName());

                    if (Modifier.isStatic(fieldDeclaration.get().getModifier()))
                        return DomFactory.assignStaticField(classDeclaration.getName(), fieldDeclaration.get().getName(), descriptor, value);

                    return DomFactory.assignField(DomFactory.self(), fieldDeclaration.get().getName(), descriptor, value);
                }

                return DomFactory.assignVar(name, value);
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
            public ExpressionDom build(ClassResolver cr, ClassDeclaration cd, ClassInspector ci, Map<String, String> locals, MethodDeclaration methodContext) {
                ExpressionDom value = valueBuilder.build(cr, cd, ci, locals, methodContext);

                Optional<FieldDeclaration> fieldDeclaration = cd.getFields().stream().filter(x -> x.getName().equals(name)).findFirst();
                if (fieldDeclaration.isPresent()) {
                    String descriptor = Descriptor.get(fieldDeclaration.get().getTypeName());

                    /*
                    if (Modifier.isStatic(fieldDeclaration.get().getModifier())) {
                        return astava.java.Factory.assignStaticField(cd.getName(), fieldDeclaration.get().getName(), descriptor, occurrences);
                    }

                    return astava.java.Factory.assignField(astava.java.Factory.self(), fieldDeclaration.get().getName(), descriptor, occurrences);
                    */

                    return DomFactory.top(DomFactory.self(), (newTarget, newTargetLast) -> {
                        return DomFactory.blockExpr(Arrays.asList(
                            DomFactory.assignField(newTarget, fieldDeclaration.get().getName(), descriptor, value),
                            Parser.fieldAccess(cr, cd, ci, newTargetLast, name, locals, methodContext)
                        ));
                    });
                }

                return DomFactory.blockExpr(Arrays.asList(
                    DomFactory.assignVar(name, value),
                    DomFactory.accessVar(name)
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
            public ExpressionDom build(ClassResolver cr, ClassDeclaration cd, ClassInspector ci, Map<String, String> locals, MethodDeclaration methodContext) {
                List<ExpressionDom> arguments = argumentBuilders.stream()
                    .map(x -> x.build(cr, cd, ci, locals, methodContext)).collect(Collectors.toList());

                ClassDeclaration targetClassDeclaration = ci.getClassDeclaration(name);

                List<ClassDeclaration> argumentTypes = arguments.stream().map(x -> {
                    String expressionResultType = Parser.expressionResultType(ci, cd, x, locals, Descriptor.get(methodContext.getReturnTypeName()));
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

                return DomFactory.newInstanceExpr(
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
            public StatementDom build(ClassResolver cr, ClassDeclaration cd, ClassInspector ci, Map<String, String> locals, MethodDeclaration methodContext) {
                ExpressionDom target = targetBuilder.build(cr, cd, ci, locals, methodContext);
                List<ExpressionDom> arguments = argumentBuilders.stream()
                    .map(x -> x.build(cr, cd, ci, locals, methodContext)).collect(Collectors.toList());

                String targetType = Parser.expressionResultType(ci, cd, target, locals, Descriptor.get(methodContext.getReturnTypeName()));
                ClassDeclaration targetClassDeclaration = ci.getClassDeclaration(Descriptor.getName(targetType));

                List<ClassDeclaration> argumentTypes = arguments.stream().map(x -> {
                    String expressionResultType = Parser.expressionResultType(ci, cd, x, locals, Descriptor.get(methodContext.getReturnTypeName()));
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
                    return DomFactory.invoke(invocation, declaringClassDescriptor, methodName, methodDescriptor, target, arguments);
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
            public ExpressionDom build(ClassResolver cr, ClassDeclaration cd, ClassInspector ci, Map<String, String> locals, MethodDeclaration methodContext) {
                ExpressionDom target = targetBuilder.build(cr, cd, ci, locals, methodContext);
                List<ExpressionDom> arguments = argumentBuilders.stream()
                    .map(x -> x.build(cr, cd, ci, locals, methodContext)).collect(Collectors.toList());

                String targetType = Parser.expressionResultType(ci, cd, target, locals, Descriptor.get(methodContext.getReturnTypeName()));
                ClassDeclaration targetClassDeclaration = ci.getClassDeclaration(Descriptor.getName(targetType));

                List<ClassDeclaration> argumentTypes = arguments.stream().map(x -> {
                    String expressionResultType = Parser.expressionResultType(ci, cd, x, locals, Descriptor.get(methodContext.getReturnTypeName()));
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
                    return DomFactory.invokeExpr(invocation, declaringClassDescriptor, methodName, methodDescriptor, target, arguments);
                });
            }

            @Override
            public String toString() {
                return targetBuilder + "." + methodName + "(" + argumentBuilders.stream().map(x -> x.toString()).collect(Collectors.joining(", ")) + ")";
            }
        };
    }

    public static DomBuilder initializer(StatementDomBuilder statement) {
        return new DomBuilder() {
            @Override
            public void accept(DomBuilderVisitor visitor) {
                visitor.visitInitializer(statement);
            }
        };
    }

    public static DomBuilder annotation(String typeName, Map<String, Object> values) {
        return new DomBuilder() {
            @Override
            public void accept(DomBuilderVisitor visitor) {
                visitor.visitAnnotation(typeName, values);
            }
        };
    }

    public static StatementDomBuilder ifElse(ExpressionDomBuilder conditionBuilder, StatementDomBuilder ifTrueBuilder, StatementDomBuilder ifFalseBuilder) {
        return new StatementDomBuilder() {
            @Override
            public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
                ExpressionDom condition = conditionBuilder.build(classResolver, classDeclaration, classInspector, locals, methodContext);
                StatementDom ifTrue = ifTrueBuilder.build(classResolver, classDeclaration, classInspector, locals, methodContext);
                StatementDom ifFalse = ifFalseBuilder.build(classResolver, classDeclaration, classInspector, locals, methodContext);

                return DomFactory.ifElse(condition, ifTrue, ifFalse);
            }
        };
    }

    public static ExpressionDomBuilder instanceOf(ExpressionDomBuilder targetBuilder, String typeName) {
        return new ExpressionDomBuilder() {
            @Override
            public ExpressionDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
                ExpressionDom target = targetBuilder.build(classResolver, classDeclaration, classInspector, locals, methodContext);

                return DomFactory.instanceOf(target, Descriptor.get(typeName));
            }

            @Override
            public String toString() {
                return targetBuilder + " instanceof " + typeName;
            }
        };
    }

    public static ExpressionDomBuilder logicalAnd(ExpressionDomBuilder lhsBuilder, ExpressionDomBuilder rhsBuilder, int operator) {
        return new ExpressionDomBuilder() {
            @Override
            public ExpressionDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
                ExpressionDom lhs = lhsBuilder.build(classResolver, classDeclaration, classInspector, locals, methodContext);
                ExpressionDom rhs = rhsBuilder.build(classResolver, classDeclaration, classInspector, locals, methodContext);

                return DomFactory.logical(lhs, rhs, operator);
            }
        };
    }

    public static ExpressionDomBuilder compare(ExpressionDomBuilder lhsBuilder, ExpressionDomBuilder rhsBuilder, int operator) {
        return new ExpressionDomBuilder() {
            @Override
            public ExpressionDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
                ExpressionDom lhs = lhsBuilder.build(classResolver, classDeclaration, classInspector, locals, methodContext);
                ExpressionDom rhs = rhsBuilder.build(classResolver, classDeclaration, classInspector, locals, methodContext);

                return DomFactory.compare(lhs, rhs, operator);
            }
        };
    }

    public static ExpressionDomBuilder typeCast(ExpressionDomBuilder expressionBuilder, String targetTypeName) {
        return new ExpressionDomBuilder() {
            @Override
            public ExpressionDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
                ExpressionDom expression = expressionBuilder.build(classResolver, classDeclaration, classInspector, locals, methodContext);

                return DomFactory.typeCast(expression, Descriptor.get(targetTypeName));
            }

            @Override
            public String toString() {
                return "(" + targetTypeName + ") " + expressionBuilder;
            }
        };
    }

    public static StatementDomBuilder throwStatement(ExpressionDomBuilder expressionBuilder) {
        return new StatementDomBuilder() {
            @Override
            public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
                ExpressionDom expression = expressionBuilder.build(classResolver, classDeclaration, classInspector, locals, methodContext);

                return DomFactory.throwStatement(expression);
            }

            @Override
            public String toString() {
                return "throw " + expressionBuilder;
            }
        };
    }

    public static StatementDomBuilder methodBodyStatement() {
        return new StatementDomBuilder() {
            @Override
            public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
                return DomFactory.methodBodyStatement();
            }

            @Override
            public String toString() {
                return "...";
            }
        };
    }

    public static ExpressionDomBuilder methodBodyExpression() {
        return new ExpressionDomBuilder() {
            @Override
            public ExpressionDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
                return DomFactory.methodBodyExpression();
            }

            @Override
            public String toString() {
                return "...";
            }
        };
    }

    public static CodeDomBuilder catchBlock(String type, String name, StatementDomBuilder blockBuilder) {
        return new CodeDomBuilder() {
            @Override
            public CodeDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
                StatementDom block = blockBuilder.build(classResolver, classDeclaration, classInspector, locals, methodContext);

                return DomFactory.catchBlock(type != null ? Descriptor.get(type) : null, name, block);
            }

            @Override
            public String toString() {
                String catchName = type != null ? "catch" : "finally";
                return catchName + "(" + type + " " + name + ") {\n" + blockBuilder + "\n}";
            }
        };
    }

    public static StatementDomBuilder tryCatch(StatementDomBuilder tryBlockBuilder, ArrayList<CodeDomBuilder> catchBlockBuilders) {
        return new StatementDomBuilder() {
            @Override
            public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
                StatementDom tryBlock = tryBlockBuilder.build(classResolver, classDeclaration, classInspector, locals, methodContext);
                List<CodeDom> catchBlocks = catchBlockBuilders.stream()
                    .map(x -> x.build(classResolver, classDeclaration, classInspector, locals, methodContext))
                        .collect(Collectors.toList());

                return DomFactory.tryCatchStatement(tryBlock, catchBlocks);
            }

            @Override
            public String toString() {
                return "try {\n" + tryBlockBuilder + "\n}\n" +
                    catchBlockBuilders.stream().map(x -> x.toString()).collect(Collectors.joining("\n"));
            }
        };
    }

    public static ExpressionDomBuilder expressionCapture() {
        return new ExpressionDomBuilder() {
            @Override
            public ExpressionDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
                throw new RuntimeException("Cannot build expression capture.");
            }

            @Override
            public String toString() {
                return "?";
            }

            @Override
            public boolean test(ExpressionDom expression, List<Object> captures) {
                captures.add(expression);
                return true;
            }
        };
    }
}
