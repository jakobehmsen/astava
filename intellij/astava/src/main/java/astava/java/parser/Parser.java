package astava.java.parser;

import astava.java.Descriptor;
import astava.java.Invocation;
import astava.java.parser.antlr4.JavaBaseVisitor;
import astava.java.parser.antlr4.JavaLexer;
import astava.java.parser.antlr4.JavaParser;
import astava.tree.*;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static astava.java.Factory.*;

public class Parser {
    private JavaParser parser;

    public Parser(String sourceCode) throws IOException {
        this(new ByteArrayInputStream(sourceCode.getBytes()));
    }

    public Parser(InputStream sourceCode) throws IOException {
        CharStream charStream = new ANTLRInputStream(sourceCode);
        JavaLexer lexer = new JavaLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        parser = new JavaParser(tokenStream);
    }

    public static String expressionResultType(ClassInspector classInspector, ClassDeclaration self, ExpressionDom expr) {
        return new ExpressionDomVisitor.Return<String>() {
            @Override
            public void visitBooleanLiteral(boolean value) {
                setResult(Descriptor.BOOLEAN);
            }

            @Override
            public void visitByteLiteral(byte value) {
                setResult(Descriptor.BYTE);
            }

            @Override
            public void visitShortLiteral(short value) {
                setResult(Descriptor.SHORT);
            }

            @Override
            public void visitIntLiteral(int value) {
                setResult(Descriptor.INT);
            }

            @Override
            public void visitLongLiteral(long value) {
                setResult(Descriptor.LONG);
            }

            @Override
            public void visitFloatLiteral(float value) {
                setResult(Descriptor.FLOAT);
            }

            @Override
            public void visitDoubleLiteral(double value) {
                setResult(Descriptor.DOUBLE);
            }

            @Override
            public void visitCharLiteral(char value) {
                setResult(Descriptor.CHAR);
            }

            @Override
            public void visitStringLiteral(String value) {
                setResult(Descriptor.STRING);
            }

            @Override
            public void visitNull() {
                setResult(Descriptor.get(Object.class));
            }

            @Override
            public void visitArithmetic(int operator, ExpressionDom lhs, ExpressionDom rhs) {

            }

            @Override
            public void visitShift(int operator, ExpressionDom lhs, ExpressionDom rhs) {

            }

            @Override
            public void visitBitwise(int operator, ExpressionDom lhs, ExpressionDom rhs) {

            }

            @Override
            public void visitCompare(int operator, ExpressionDom lhs, ExpressionDom rhs) {

            }

            @Override
            public void visitLogical(int operator, ExpressionDom lhs, ExpressionDom rhs) {

            }

            @Override
            public void visitVariableAccess(String name) {

            }

            @Override
            public void visitFieldAccess(ExpressionDom target, String name, String fieldTypeName) {
                setResult(fieldTypeName);
            }

            @Override
            public void visitStaticFieldAccess(String typeName, String name, String fieldTypeName) {
                setResult(fieldTypeName);
            }

            @Override
            public void visitNot(ExpressionDom expression) {

            }

            @Override
            public void visitInstanceOf(ExpressionDom expression, String type) {

            }

            @Override
            public void visitBlock(List<CodeDom> codeList) {

            }

            @Override
            public void visitIfElse(ExpressionDom condition, ExpressionDom ifTrue, ExpressionDom ifFalse) {

            }

            @Override
            public void visitInvocation(int invocation, ExpressionDom target, String type, String name, String descriptor, List<ExpressionDom> arguments) {
                // Only instance invocations
                String targetResultType = expressionResultType(classInspector, self, target);
                ClassDeclaration targetClass = classInspector.getClassDeclaration(Descriptor.getName(targetResultType));
                List<ClassDeclaration> argumentTypes = arguments.stream().map(x -> {
                    String expressionResultType = expressionResultType(classInspector, self, x);
                    String expressionResultTypeName = Descriptor.getName(expressionResultType);

                    return classInspector.getClassDeclaration(expressionResultTypeName);
                }).collect(Collectors.toList());
                String resultTypeName = resolveMethod(classInspector, targetClass, name, argumentTypes, (c, m) -> m.getReturnTypeName());
                setResult(Descriptor.get(resultTypeName));
            }

            @Override
            public void visitNewInstance(String type, List<String> parameterTypes, List<ExpressionDom> arguments) {
                setResult(type);
            }

            @Override
            public void visitThis() {
                setResult(Descriptor.get(self.getName()));
            }
        }.returnFrom(expr);
    }

    public static String statementReturnType(ClassInspector classInspector, ClassDeclaration self, StatementDom stmt) {
        String returnType = new StatementDomVisitor.Return<String>() {
            @Override
            public void visitVariableDeclaration(String type, String name) {

            }

            @Override
            public void visitVariableAssignment(String name, ExpressionDom value) {

            }

            @Override
            public void visitFieldAssignment(ExpressionDom target, String name, String type, ExpressionDom value) {

            }

            @Override
            public void visitStaticFieldAssignment(String typeName, String name, String type, ExpressionDom value) {

            }

            @Override
            public void visitIncrement(String name, int amount) {

            }

            @Override
            public void visitReturnValue(ExpressionDom expression) {
                String resultType = expressionResultType(classInspector, self, expression);
                setResult(resultType);
            }

            @Override
            public void visitBlock(List<StatementDom> statements) {

            }

            @Override
            public void visitIfElse(ExpressionDom condition, StatementDom ifTrue, StatementDom ifFalse) {

            }

            @Override
            public void visitBreakCase() {

            }

            @Override
            public void visitReturn() {

            }

            @Override
            public void visitInvocation(int invocation, ExpressionDom target, String type, String name, String descriptor, List<ExpressionDom> arguments) {

            }

            @Override
            public void visitNewInstance(String type, List<String> parameterTypes, List<ExpressionDom> arguments) {

            }

            @Override
            public void visitLabel(String name) {

            }

            @Override
            public void visitGoTo(String name) {

            }

            @Override
            public void visitSwitch(ExpressionDom expression, Map<Integer, StatementDom> cases, StatementDom defaultBody) {

            }
        }.returnFrom(stmt);

        return returnType != null ? returnType : Descriptor.VOID;
    }

    public List<DomBuilder> parse() {
        return parser.script().element().stream().map(x -> x.accept(new JavaBaseVisitor<List<DomBuilder>>() {
            @Override
            public List<DomBuilder> visitClassDefinition(@NotNull JavaParser.ClassDefinitionContext ctx) {
                MutableClassDomBuilder classBuilder = new MutableClassDomBuilder();
                parseClass(ctx, classBuilder);
                return Arrays.asList(classBuilder);
            }

            @Override
            public List<DomBuilder> visitFieldDefinition(@NotNull JavaParser.FieldDefinitionContext ctx) {
                DomBuilder fieldBuilder = parseFieldBuilder(ctx, true);
                if (ctx.value != null) {
                    DomBuilder fieldAssignBuilder = buildAssignment(Arrays.asList(ctx.ID()), ctx.value, true);
                    return Arrays.asList(fieldBuilder, fieldAssignBuilder);
                }
                return Arrays.asList(fieldBuilder);
            }

            @Override
            public List<DomBuilder> visitMethodDefinition(@NotNull JavaParser.MethodDefinitionContext ctx) {
                return Arrays.asList(parseMethodBuilder(ctx));
            }

            @Override
            public List<DomBuilder> visitStatement(@NotNull JavaParser.StatementContext ctx) {
                return Arrays.asList(parseStatementBuilder(ctx, true));
            }

            @Override
            public List<DomBuilder> visitExpression(@NotNull JavaParser.ExpressionContext ctx) {
                return Arrays.asList(parseExpressionBuilder(ctx, true));
            }
        })).flatMap(x -> x.stream()).collect(Collectors.toList());
    }

    private String parseTypeQualifier(ClassResolver classResolver, String typeQualifier) {
        if(!Descriptor.isPrimitiveName(typeQualifier)) {
            if (!classResolver.canResolveAmbiguous(typeQualifier))
                typeQualifier = classResolver.resolveSimpleName(typeQualifier);
        }

        return Descriptor.get(typeQualifier);
    }

    private int parseModifiers(JavaParser.ModifiersContext ctx) {
        int modifiers = 0;

        if(ctx.accessModifier() != null) {
            if(ctx.accessModifier().KW_PRIVATE() != null)
                modifiers |= Modifier.PRIVATE;
            else if(ctx.accessModifier().KW_PROTECTED() != null)
                modifiers |= Modifier.PROTECTED;
            else if(ctx.accessModifier().KW_PUBLIC() != null)
                modifiers |= Modifier.PUBLIC;
        }

        if(ctx.KW_ABSTRACT() != null)
            modifiers |= Modifier.ABSTRACT;

        if(ctx.KW_STATIC() != null)
            modifiers |= Modifier.STATIC;

        return modifiers;
    }

    public MutableClassDomBuilder parseClass() {
        MutableClassDomBuilder classBuilder = new MutableClassDomBuilder();
        parseClass(parser.classDefinition(), classBuilder);
        return classBuilder;
    }

    public void parseClass(JavaParser.ClassDefinitionContext ctx, MutableClassDomBuilder classBuilder) {
        ctx.accept(new JavaBaseVisitor<Void>() {
            @Override
            public Void visitClassDefinition(@NotNull JavaParser.ClassDefinitionContext ctx) {
                int modifiers = parseModifiers(ctx.modifiers());
                String name = ctx.name.getText();
                String superName = Object.class.getName();

                classBuilder.setModifiers(modifiers);
                classBuilder.setName(name);
                classBuilder.setSuperName(superName);

                ArrayList<MethodDomBuilder> methodBuilders = new ArrayList<MethodDomBuilder>();

                ctx.classMember().forEach(m -> {
                    m.accept(new JavaBaseVisitor<Void>() {
                        @Override
                        public Void visitFieldDefinition(@NotNull JavaParser.FieldDefinitionContext ctx) {
                            FieldDomBuilder field = parseFieldBuilder(ctx, false);
                            classBuilder.addField(field);

                            return null;
                        }

                        @Override
                        public Void visitMethodDefinition(@NotNull JavaParser.MethodDefinitionContext ctx) {
                            MethodDomBuilder method = parseMethodBuilder(ctx);
                            classBuilder.addMethod(method);

                            return null;
                        }
                    });
                });

                return null;
            }
        });
    }

    public FieldDomBuilder parseFieldBuilder() {
        return parseFieldBuilder(parser.fieldDefinition(), true);
    }

    public FieldDomBuilder parseFieldBuilder(JavaParser.FieldDefinitionContext ctx, boolean atRoot) {
        return new FieldDomBuilder() {
            @Override
            public FieldDeclaration declare(ClassResolver classResolver) {
                String typeName = parseTypeQualifier(classResolver, ctx.type.getText());
                int modifiersTmp = parseModifiers(ctx.modifiers());

                int modifiers;
                if(atRoot)
                    modifiers = modifiersTmp | Modifier.PUBLIC;
                else
                    modifiers = modifiersTmp;

                String name = ctx.name.getText();

                return new FieldDeclaration() {
                    @Override
                    public int getModifiers() {
                        return modifiers;
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
                        return fieldDeclaration(modifiers, name, typeName);
                    }
                };
            }
        };
    }

    public MethodDomBuilder parseMethodBuilder() {
        return parseMethodBuilder(parser.methodDefinition());
    }

    public MethodDomBuilder parseMethodBuilder(JavaParser.MethodDefinitionContext ctx) {
        return new MethodDomBuilder() {
            @Override
            public MethodDeclaration declare(ClassResolver classResolver) {
                boolean isConstructor = ctx.returnType == null;

                String returnType = isConstructor ? Descriptor.VOID : parseTypeQualifier(classResolver, ctx.returnType.getText());
                int modifiers = parseModifiers(ctx.modifiers());
                // Somehow, the name should checked as to the class name
                String name = isConstructor ? "<init>" : ctx.name.getText();
                List<ParameterInfo> parameters = ctx.parameters().parameter().stream()
                    .map(x -> new ParameterInfo(parseTypeQualifier(classResolver, x.type.getText()), x.name.getText()))
                    .collect(Collectors.toList());

                return new MethodDeclaration() {
                    @Override
                    public int getModifiers() {
                        return modifiers;
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
                        return returnType;
                    }

                    @Override
                    public MethodDom build(ClassDeclaration classDeclaration, ClassInspector classInspector) {
                        HashSet<String> locals = new HashSet<>();
                        locals.addAll(parameters.stream().map(x -> x.name).collect(Collectors.toList()));
                        List<StatementDomBuilder> statementBuilders = ctx.statement().stream().map(x -> parseStatementBuilder(x, false)).collect(Collectors.toList());
                        statementBuilders.forEach(x -> x.appendLocals(locals));
                        List<StatementDom> statements = statementBuilders.stream().map(x -> x.build(classResolver, classDeclaration, classInspector, locals)).collect(Collectors.toList());
                        StatementDom body = block(statements);

                        // Ugly hack
                        // Instead: every leaf statement should either be a ret statement or one is injected
                        // This logic probably shouldn't be located here?
                        if(returnType.equals(Descriptor.VOID)) {
                            statements.add(ret());
                        }

                        if(isConstructor) {
                            // Call super constructor
                            statements.add(0,
                                invokeSpecial(Descriptor.get(classDeclaration.getSuperName()), "<init>", Descriptor.getMethodDescriptor(Arrays.asList(), Descriptor.VOID), self(), Arrays.asList())
                            );
                        }

                        return methodDeclaration(modifiers, name, parameters, returnType, body);
                    }
                };
            }
        };
    }

    public StatementDomBuilder parseStatementBuilder(JavaParser.StatementContext ctx, boolean atRoot) {
        return ctx.accept(new JavaBaseVisitor<StatementDomBuilder>() {
            @Override
            public StatementDomBuilder visitStatement(@NotNull JavaParser.StatementContext ctx) {
                return ctx.getChild(0).accept(this);
            }

            @Override
            public StatementDomBuilder visitDelimitedStatement(@NotNull JavaParser.DelimitedStatementContext ctx) {
                return ctx.getChild(0).accept(this);
            }

            @Override
            public StatementDomBuilder visitReturnStatement(@NotNull JavaParser.ReturnStatementContext ctx) {
                ExpressionDomBuilder expression = parseExpressionBuilder(ctx.expression(), atRoot);
                return new StatementDomBuilder() {
                    @Override
                    public void appendLocals(Set<String> locals) {

                    }

                    @Override
                    public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Set<String> locals) {
                        return ret(expression.build(classResolver, classDeclaration, classInspector, locals));
                    }
                };
            }

            @Override
            public StatementDomBuilder visitVariableDeclaration(@NotNull JavaParser.VariableDeclarationContext ctx) {
                String name = ctx.name.getText();

                ExpressionDomBuilder valueBuilder = ctx.value != null ? parseExpressionBuilder(ctx.value, atRoot) : null;

                return new StatementDomBuilder() {
                    @Override
                    public void appendLocals(Set<String> locals) {
                        locals.add(name);
                    }

                    @Override
                    public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Set<String> locals) {
                        String type = parseTypeQualifier(classResolver, ctx.type.getText());

                        StatementDom statement = declareVar(type, name);

                        if (valueBuilder != null) {
                            statement = block(Arrays.asList(statement, assignVar(name, valueBuilder.build(classResolver, classDeclaration, classInspector, locals))));
                        }

                        return statement;
                    }
                };
            }

            @Override
            public StatementDomBuilder visitAssignment(@NotNull JavaParser.AssignmentContext ctx) {
                return buildAssignment(ctx.name.ID(), ctx.value, atRoot);
            }
        });
    }

    private StatementDomBuilder buildAssignment(List<TerminalNode> name, JavaParser.ExpressionContext valueCtx, boolean atRoot) {
        ExpressionDomBuilder valueBuilder = parseExpressionBuilder(valueCtx, atRoot);

        return new StatementDomBuilder() {
            @Override
            public void appendLocals(Set<String> locals) {

            }

            @Override
            public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Set<String> locals) {
                ExpressionDom value = valueBuilder.build(classResolver, classDeclaration, classInspector, locals);

                return parseAmbiguousName(name, classResolver, classDeclaration,
                    name -> {
                        Optional<FieldDeclaration> fieldDeclaration = classDeclaration.getFields().stream().filter(x -> x.getName().equals(name)).findFirst();
                        if (fieldDeclaration.isPresent()) {
                            if (Modifier.isStatic(fieldDeclaration.get().getModifiers()))
                                return assignStaticField(classDeclaration.getName(), fieldDeclaration.get().getName(), fieldDeclaration.get().getTypeName(), value);

                            if(!atRoot)
                                return assignField(self(), fieldDeclaration.get().getName(), fieldDeclaration.get().getTypeName(), value);
                            else
                                return assignField(accessVar("self"), fieldDeclaration.get().getName(), fieldDeclaration.get().getTypeName(), value); // "self" is passed as argument
                        }

                        return assignVar(name, value);
                    },
                    (target, fieldChainAccess) -> {
                        return target;
                    }
                );
            }
        };
    }

    public ExpressionDomBuilder parseExpressionBuilder(JavaParser.ExpressionContext ctx, boolean atRoot) {
        return parseExpressionBuilder(ctx, atRoot, false);
    }

    private static final int NAME_VARIABLE = 0;
    private static final int NAME_PARAMETER = 1;
    private static final int NAME_FIELD = 2;

    private <T> T parseAmbiguousName(List<TerminalNode> ids, ClassResolver cr, ClassDeclaration cd, Function<String, T> nameHandler, BiFunction<T, List<String>, T> fieldChainHandler) {
        // Find longest getName that can be resolved
        String name = "";

        if(ids.size() > 1) {
            int i = ids.size();

            for (; i > 1; i--) {
                name = ids.subList(0, i).stream().map(x -> x.getText()).collect(Collectors.joining("."));
                if (cr.canResolveAmbiguous(name))
                    break;
            }

            if (i == 1) {
                name = ids.get(0).getText();

                // Try match with field first
                String fieldName = name;
                FieldDeclaration fieldDeclaration = cd.getFields().stream().filter(x -> x.getName().equals(fieldName)).findFirst().orElse(null);
                if (fieldDeclaration == null) {
                    // What if simple getName cannot be resolved? Then it may be that getName is a field related to the class being defined
                    name = cr.resolveSimpleName(name);
                }

                // What is required to resolve an ambiguous getName?
                // A set of field names and a class getName resolver

                // So, a parser perhaps shouldn't yield Doms directly but rather something can yield Doms when requested?
            }

            List<String> fieldAccessChain;

            if (i < ids.size()) {
                // The rest should be considered field access
                fieldAccessChain = ids.subList(i, ids.size()).stream().map(x -> x.getText()).collect(Collectors.toList());
            } else
                fieldAccessChain = Arrays.asList();

            T handledName = nameHandler.apply(name);
            return fieldChainHandler.apply(handledName, fieldAccessChain);
        } else
            name = ids.get(0).getText();

        return nameHandler.apply(name);
    }

    public ExpressionDomBuilder parseExpressionBuilder(ParserRuleContext ctx, boolean atRoot, boolean asStatement) {
        return ctx.accept(new JavaBaseVisitor<ExpressionDomBuilder>() {
            @Override
            public ExpressionDomBuilder visitLeafExpression(@NotNull JavaParser.LeafExpressionContext ctx) {
                ExpressionDomBuilder expressionBuilder = parseExpressionBuilder((ParserRuleContext) ctx.getChild(0), atRoot, asStatement);

                for (JavaParser.ChainElementContext chainElement : ctx.chainElement()) {
                    ExpressionDomBuilder targetBuilder = expressionBuilder;
                    expressionBuilder = chainElement.accept(new JavaBaseVisitor<ExpressionDomBuilder>() {
                        @Override
                        public ExpressionDomBuilder visitInvocation(@NotNull JavaParser.InvocationContext ctx) {
                            List<ExpressionDomBuilder> argumentBuilders = ctx.arguments().expression().stream()
                                .map(x -> parseExpressionBuilder(x, atRoot, false)).collect(Collectors.toList());

                            return (cr, cd, ci, locals) -> {
                                String methodName = ctx.ID().getText();
                                ExpressionDom target = targetBuilder.build(cr, cd, ci, locals);
                                List<ExpressionDom> arguments = argumentBuilders.stream()
                                    .map(x -> x.build(cr, cd, ci, locals)).collect(Collectors.toList());

                                String targetType = expressionResultType(ci, cd, target);
                                ClassDeclaration targetClassDeclaration = ci.getClassDeclaration(Descriptor.getName(targetType));

                                List<ClassDeclaration> argumentTypes = arguments.stream().map(x -> {
                                    String expressionResultType = expressionResultType(ci, cd, x);
                                    String expressionResultTypeName = Descriptor.getName(expressionResultType);

                                    return ci.getClassDeclaration(expressionResultTypeName);
                                }).collect(Collectors.toList());

                                return resolveMethod(ci, targetClassDeclaration, methodName, argumentTypes, (c, m) -> {
                                    int invocation = c.isInterface() ? Invocation.INTERFACE : Invocation.VIRTUAL;

                                    String methodDescriptor =
                                        Descriptor.getMethodDescriptor(m.getParameterTypes().stream().map(x -> x.descriptor).collect(Collectors.toList()),
                                            Descriptor.get(m.getReturnTypeName())
                                        );

                                    String declaringClassDescriptor = Descriptor.get(c.getName());
                                    return invokeExpr(invocation, declaringClassDescriptor, methodName, methodDescriptor, target, arguments);
                                });
                            };
                        }

                        @Override
                        public ExpressionDomBuilder visitFieldAccess(@NotNull JavaParser.FieldAccessContext ctx) {
                            return super.visitFieldAccess(ctx);
                        }
                    });
                }

                return expressionBuilder;
            }

            @Override
            public ExpressionDomBuilder visitAmbigousName(@NotNull JavaParser.AmbigousNameContext ctx) {
                return (cr, cd, ci, locals) -> {
                    return parseAmbiguousName(ctx.ID(), cr, cd,
                        name -> {
                            Optional<FieldDeclaration> fieldDeclaration = cd.getFields().stream().filter(x -> x.getName().equals(name)).findFirst();
                            if (fieldDeclaration.isPresent()) {
                                if (Modifier.isStatic(fieldDeclaration.get().getModifiers()))
                                    return accessStaticField(cd.getName(), name, fieldDeclaration.get().getTypeName());

                                if (!atRoot)
                                    return accessField(self(), name, fieldDeclaration.get().getTypeName());
                                else
                                    return accessField(accessVar("self"), name, fieldDeclaration.get().getTypeName());
                            }

                            return accessVar(name);
                        },
                        (target, fieldChainAccess) -> {
                            return target;
                        }
                    );
                };
            }

            @Override
            public ExpressionDomBuilder visitIntLiteral(@NotNull JavaParser.IntLiteralContext ctx) {
                int value = Integer.parseInt(ctx.getText());

                return (cr, cd, ci, locals) -> literal(value);
            }

            @Override
            public ExpressionDomBuilder visitStringLiteral(@NotNull JavaParser.StringLiteralContext ctx) {
                String rawString = ctx.getText();
                String value = rawString.substring(1, rawString.length() - 1)
                    .replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
                return (cr, cd, ci, locals) -> literal(value);
            }

            @Override
            public ExpressionDomBuilder visitNullLiteral(@NotNull JavaParser.NullLiteralContext ctx) {
                return (cr, cd, ci, locals) -> nil();
            }

            @Override
            public ExpressionDomBuilder visitNewInstance(@NotNull JavaParser.NewInstanceContext ctx) {
                List<ExpressionDomBuilder> argumentBuilders = ctx.arguments().expression().stream()
                    .map(x -> parseExpressionBuilder(x, atRoot, false)).collect(Collectors.toList());

                return (cr, cd, ci, locals) -> {
                    List<ExpressionDom> arguments = argumentBuilders.stream()
                        .map(x -> x.build(cr, cd, ci, locals)).collect(Collectors.toList());

                    ClassDeclaration targetClassDeclaration = parseAmbiguousName(ctx.name.ID(), cr, cd,
                        name -> ci.getClassDeclaration(name), (x, fieldChain) -> x);

                    List<ClassDeclaration> argumentTypes = arguments.stream().map(x -> {
                        String expressionResultType = expressionResultType(ci, cd, x);
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

                    return newInstanceExpr(
                        targetClassDeclaration.getName(),
                        constructor.getParameterTypes().stream().map(x -> x.descriptor).collect(Collectors.toList()),
                        arguments);
                };
            }
        });
    }

    private static <T> T resolveDeclaredMethod(ClassInspector classInspector, ClassDeclaration targetClass, String methodName, List<ClassDeclaration> argumentTypes, BiFunction<ClassDeclaration, MethodDeclaration, T> reducer) {
        List<MethodDeclaration> methods = targetClass.getMethods().stream()
            .filter(x -> x.getName().equals(methodName))
            .filter(x -> x.getParameterTypes().size() == argumentTypes.size())
            .filter(x -> IntStream.range(0, argumentTypes.size()).allMatch(i ->
                // Compare full inheritance
                Descriptor.getName(x.getParameterTypes().get(0).descriptor).equals(argumentTypes.get(i).getName())))
            .collect(Collectors.toList());

        if(methods.size() > 0) {
            // For now, just pick the first
            MethodDeclaration method = methods.get(0);
            return reducer.apply(targetClass, method);
        }

        return null;
    }

    private static <T> T resolveMethod(ClassInspector classInspector, ClassDeclaration targetClass, String methodName, List<ClassDeclaration> argumentTypes, BiFunction<ClassDeclaration, MethodDeclaration, T> reducer) {
        List<MethodDeclaration> methods = targetClass.getMethods().stream()
            .filter(x -> x.getName().equals(methodName))
            .filter(x -> x.getParameterTypes().size() == argumentTypes.size())
            .filter(x -> IntStream.range(0, argumentTypes.size()).allMatch(i ->
                // Compare full inheritance
                Descriptor.getName(x.getParameterTypes().get(0).descriptor).equals(argumentTypes.get(i).getName())))
            .collect(Collectors.toList());

        if(methods.size() > 0) {
            // For now, just pick the first
            MethodDeclaration method = methods.get(0);
            return reducer.apply(targetClass, method);
        }

        if(targetClass.getSuperName() != null) {
            ClassDeclaration superClass = classInspector.getClassDeclaration(targetClass.getSuperName());
            return resolveMethod(classInspector, superClass, methodName, argumentTypes, reducer);
        }

        return null;
    }
}
