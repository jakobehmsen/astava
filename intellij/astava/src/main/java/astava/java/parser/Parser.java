package astava.java.parser;

import astava.debug.Debug;
import astava.java.Descriptor;
import astava.java.LogicalOperator;
import astava.java.RelationalOperator;
import astava.java.agent.*;
import astava.java.gen.ByteCodeToTree;
import astava.java.gen.MethodGenerator;
import astava.java.parser.antlr4.JavaBaseVisitor;
import astava.java.parser.antlr4.JavaLexer;
import astava.java.parser.antlr4.JavaParser;
import astava.tree.*;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static astava.java.DomFactory.*;

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

    public static String expressionResultType(ClassInspector classInspector, ClassDeclaration self, ExpressionDom expr, Map<String, String> locals, String returnType) {
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
                String type = locals.get(name);
                setResult(type);
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
                ExpressionDom singleExpression = (ExpressionDom)codeList.stream().filter(x -> x instanceof ExpressionDom).findFirst().get();
                setResult(expressionResultType(classInspector, self, singleExpression, locals, returnType));
            }

            @Override
            public void visitIfElse(ExpressionDom condition, ExpressionDom ifTrue, ExpressionDom ifFalse) {

            }

            @Override
            public void visitInvocation(int invocation, ExpressionDom target, String type, String name, String descriptor, List<ExpressionDom> arguments) {
                String returnType = Descriptor.getReturnType(descriptor);
                Debug.getPrintStream(Debug.LEVEL_HIGH).println("@visitInvocation returnType=" + returnType);
                setResult(returnType);
            }

            @Override
            public void visitNewInstance(String type, List<String> parameterTypes, List<ExpressionDom> arguments) {
                setResult(type);
            }

            @Override
            public void visitThis() {
                setResult(Descriptor.get(self.getName()));
            }

            @Override
            public void visitTop(ExpressionDom expression, BiFunction<ExpressionDom, ExpressionDom, ExpressionDom> usage) {
                String topResultType = expressionResultType(classInspector, self, expression, locals, returnType);
                ExpressionDom dup = v -> {
                    // Some checks most be made here to ensure that the stack is maintained properly?
                    v.visitDup(topResultType);
                };
                ExpressionDom last = v -> {
                    // Some checks most be made here to ensure that the stack is maintained properly?
                    v.visitLetBe(topResultType);
                };
                ExpressionDom usageExpression = usage.apply(dup, last);
                String resultType = expressionResultType(classInspector, self, usageExpression, locals, returnType);
                setResult(resultType);
            }

            @Override
            public void visitDup(String type) {
                setResult(type);
            }

            @Override
            public void visitLetBe(String type) {
                setResult(type);
            }

            @Override
            public void visitTypeCast(ExpressionDom expression, String targetType) {
                setResult(targetType);
            }

            @Override
            public void visitMethodBody() {
                setResult(returnType);
            }
        }.returnFrom(expr);
    }

    public static String statementReturnType(ClassInspector classInspector, ClassDeclaration self, StatementDom stmt, Map<String, String> locals, String declaredReturnType) {
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
                String resultType = expressionResultType(classInspector, self, expression, locals, declaredReturnType);
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

            @Override
            public void visitASM(MethodNode methodNode) {

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
                    String name = ctx.name.getText();

                    ExpressionDomBuilder valueBuilder = parseExpressionBuilder(ctx.value, true);

                    StatementDomBuilder fieldAssignBuilder = Factory.assignField(Factory.self(), name, valueBuilder);

                    return Arrays.asList(fieldBuilder, Factory.initializer(fieldAssignBuilder));
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

            @Override
            public List<DomBuilder> visitAnnotation(JavaParser.AnnotationContext ctx) {
                return Arrays.asList(parseAnnotationBuilder(ctx));
            }
        })).flatMap(x -> x.stream()).collect(Collectors.toList());
    }

    private DomBuilder parseAnnotationBuilder(JavaParser.AnnotationContext ctx) {
        Map<String, Object> values = parseAnnotationValues(ctx);

        return Factory.annotation(ctx.typeQualifier().getText(), values);
    }

    private Map<String, Object> parseAnnotationValues(JavaParser.AnnotationContext ctx) {
        Map<String, Object> values = new Hashtable<>();
        if(ctx.valueArgument != null) {
            Object value = parsePrimitive(ctx.valueArgument);
            values.put("occurrences", value);
        }
        ctx.annotationArgument().forEach(a -> {
            Object value = parsePrimitive(a.value);
            values.put(a.name.getText(), value);
        });
        return values;
    }

    private Object parsePrimitive(JavaParser.ExpressionContext context) {
        return context.accept(new JavaBaseVisitor<Object>() {
            @Override
            public Object visitIntLiteral(JavaParser.IntLiteralContext ctx) {
                return parseIntValue(ctx);
            }

            @Override
            public Object visitStringLiteral(JavaParser.StringLiteralContext ctx) {
                return parseStringValue(ctx);
            }
        });
    }

    public static String parseTypeQualifier(ClassResolver classResolver, String typeQualifier) {
        if(!Descriptor.isPrimitiveName(typeQualifier)) {
            if (!classResolver.canResolveAmbiguous(typeQualifier)) {
                if(classResolver.resolveSimpleName(typeQualifier) == null)
                    new String();

                typeQualifier = classResolver.resolveSimpleName(typeQualifier);
            }
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

    private int parseAccessModifier(JavaParser.AccessModifierContext ctx) {
        if(ctx == null)
            return -1;

        if(ctx.KW_PRIVATE() != null)
            return Modifier.PRIVATE;
        else if(ctx.KW_PROTECTED() != null)
            return Modifier.PROTECTED;
        else if(ctx.KW_PUBLIC() != null)
            return Modifier.PUBLIC;

        return -1;
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

                classBuilder.setModifier(modifiers);
                classBuilder.setName(name);
                classBuilder.setSuperName(superName);

                ArrayList<MethodDomBuilder> methodBuilders = new ArrayList<MethodDomBuilder>();

                ctx.classMember().forEach(m -> {
                    m.accept(new JavaBaseVisitor<Void>() {
                        @Override
                        public Void visitFieldDefinition(@NotNull JavaParser.FieldDefinitionContext ctx) {
                            FieldDomBuilder field = parseFieldBuilder(ctx, false);
                            classBuilder.addField(field);

                            if(ctx.value != null) {
                                StatementDomBuilder fieldInitializer = parseFieldInitializer(ctx, false);
                                // How to handle initializers in class definitions?
                            }

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
        String name = ctx.name.getText();
        String rawTypeName = ctx.type.getText();
        int modifiersTmp = parseModifiers(ctx.modifiers());

        int modifiers;
        if(atRoot)
            modifiers = modifiersTmp | Modifier.PUBLIC;
        else
            modifiers = modifiersTmp;

        ExpressionDomBuilder valueBuilder = ctx.value != null ? parseExpressionBuilder(ctx.value, atRoot) : null;

        return Factory.field(modifiers, name, rawTypeName);
    }

    public StatementDomBuilder parseFieldInitializerBuilder() {
        return parseFieldInitializer(parser.fieldDefinition(), true);
    }

    public StatementDomBuilder parseFieldInitializer(JavaParser.FieldDefinitionContext ctx, boolean atRoot) {
        String name = ctx.name.getText();
        ExpressionDomBuilder valueBuilder = parseExpressionBuilder(ctx.value, atRoot);
        return Factory.assignField(Factory.self(), name, valueBuilder);
    }

    public MethodDomBuilder parseMethodBuilder() {
        return parseMethodBuilder(parser.methodDefinition());
    }

    public MethodDomBuilder parseMethodBuilder(JavaParser.MethodDefinitionContext ctx) {
        boolean isConstructor = ctx.returnType == null;
        String name = isConstructor ? "<init>" : ctx.name.getText();
        int modifiers = parseModifiers(ctx.modifiers());
        List<ParameterInfo> tmpParameters = ctx.parameters().parameter().stream()
            .map(x -> new ParameterInfo(x.type.getText(), x.name.getText()))
            .collect(Collectors.toList());
        String returnType = isConstructor ? "void" : ctx.returnType.getText();
        List<StatementDomBuilder> statementBuilders = ctx.statement().stream().map(x -> parseStatementBuilder(x, false)).collect(Collectors.toList());

        return Factory.method(modifiers, name, tmpParameters, returnType, statementBuilders);
    }

    public StatementDomBuilder parseStatementBuilder() {
        return parseStatementBuilder(parser.statement(), true);
    }

    public StatementDomBuilder parseStatementBuilder(JavaParser.StatementContext ctx, boolean atRoot) {
        return ctx.accept(new JavaBaseVisitor<StatementDomBuilder>() {
            @Override
            public StatementDomBuilder visitStatement(@NotNull JavaParser.StatementContext ctx) {
                return ctx.getChild(0).accept(this);
            }

            @Override
            public StatementDomBuilder visitNonDelimitedStatement(JavaParser.NonDelimitedStatementContext ctx) {
                return ctx.getChild(0).accept(this);
            }

            @Override
            public StatementDomBuilder visitIfElseStatement(JavaParser.IfElseStatementContext ctx) {
                ExpressionDomBuilder conditionBuilder = parseExpressionBuilder(ctx.condition, true);
                StatementDomBuilder ifTrueBlockBuilder = parseBlock(ctx.ifTrueBlock.statement());
                StatementDomBuilder ifFalseBlockBuilder = ctx.ifFalseBlock != null ? parseBlock(ctx.ifFalseBlock.statement()) : Factory.block(Arrays.asList());

                return Factory.ifElse(conditionBuilder, ifTrueBlockBuilder, ifFalseBlockBuilder);
            }

            @Override
            public StatementDomBuilder visitTryCatchStatement(JavaParser.TryCatchStatementContext ctx) {
                StatementDomBuilder tryBlockBuilder = parseBlock(ctx.tryBlock().statement());
                ArrayList<CodeDomBuilder> catchBlocks = new ArrayList<>();

                ctx.catchBlock().forEach(c -> {
                    StatementDomBuilder blockBuilder = parseBlock(c.statement());
                    String type = c.type.getText();
                    String name = c.name.getText();
                    catchBlocks.add(Factory.catchBlock(type, name, blockBuilder));
                });

                if(ctx.finallyBlock() != null) {
                    StatementDomBuilder blockBuilder = parseBlock(ctx.finallyBlock().statement());
                    catchBlocks.add(Factory.catchBlock(null, null, blockBuilder));
                }

                return Factory.tryCatch(tryBlockBuilder, catchBlocks);
            }

            @Override
            public StatementDomBuilder visitMethodBodyStatement(JavaParser.MethodBodyStatementContext ctx) {
                return Factory.methodBodyStatement();
            }

            @Override
            public StatementDomBuilder visitDelimitedStatement(@NotNull JavaParser.DelimitedStatementContext ctx) {
                return ctx.getChild(0).accept(this);
            }

            @Override
            public StatementDomBuilder visitExpression(@NotNull JavaParser.ExpressionContext ctx) {
                //// How to convert expression into statement?

                return parseExpressionAsStatement((ParserRuleContext)ctx.getChild(0), atRoot);
            }

            @Override
            public StatementDomBuilder visitReturnStatement(@NotNull JavaParser.ReturnStatementContext ctx) {
                if(ctx.expression() != null) {
                    ExpressionDomBuilder expression = parseExpressionBuilder(ctx.expression(), atRoot);
                    return Factory.ret(expression);
                } else
                    return Factory.ret();
            }

            @Override
            public StatementDomBuilder visitVariableDeclaration(@NotNull JavaParser.VariableDeclarationContext ctx) {
                String name = ctx.name.getText();

                ExpressionDomBuilder valueBuilder = ctx.value != null ? parseExpressionBuilder(ctx.value, atRoot) : null;

                return new StatementDomBuilder() {
                    @Override
                    public void appendLocals(Map<String, String> locals) {
                        //locals.add(name);
                        //locals.put(name, )
                    }

                    @Override
                    public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext) {
                        String type = parseTypeQualifier(classResolver, ctx.type.getText());

                        locals.put(name, type);

                        StatementDom statement = declareVar(type, name);

                        if (valueBuilder != null) {
                            statement = block(Arrays.asList(statement, assignVar(name, valueBuilder.build(classResolver, classDeclaration, classInspector, locals, methodContext))));
                        }

                        return statement;
                    }
                };
            }

            @Override
            public StatementDomBuilder visitAssignment(@NotNull JavaParser.AssignmentContext ctx) {
                return buildAssignment(ctx.name.getText(), ctx.value, atRoot);
            }

            @Override
            public StatementDomBuilder visitThrowStatement(JavaParser.ThrowStatementContext ctx) {
                ExpressionDomBuilder expression = parseExpressionBuilder(ctx.expression(), atRoot);

                return Factory.throwStatement(expression);
            }
        });
    }

    private StatementDomBuilder parseBlock(List<JavaParser.StatementContext> statementContexts) {
        return Factory.block(
            statementContexts.stream().map(x -> parseStatementBuilder(x, false)).collect(Collectors.toList())
        );
    }

    private StatementDomBuilder buildAssignment(String name, JavaParser.ExpressionContext valueCtx, boolean atRoot) {
        ExpressionDomBuilder valueBuilder = parseExpressionBuilder(valueCtx, atRoot);

        return Factory.assign(name, valueBuilder);
    }

    public ExpressionDomBuilder parseExpressionBuilder() {
        return parseExpressionBuilder(parser.expression(), true);
    }

    public ExpressionDomBuilder parseExpressionBuilder(ParserRuleContext ctx, boolean atRoot) {
        return parseExpressionBuilder(ctx, atRoot, false);
    }

    public static <T> T parseAmbiguousName(List<String> ids, ClassResolver cr, ClassDeclaration cd, Function<String, T> nameHandler, BiFunction<String, String, T> classFieldAccessHandler, BiFunction<T, List<String>, T> fieldChainHandler, Map<String, String> locals) {
        // Find longest getName that can be resolved
        String name = "";

        if(ids.size() > 1) {
            int i = ids.size();

            T handledName = null;

            boolean isClass = false;
            for (; i > 1; i--) {
                name = ids.subList(0, i).stream().collect(Collectors.joining("."));
                if (cr.canResolveAmbiguous(name)) {
                    isClass = true;
                    break;
                }
            }

            if (!isClass &&  i == 1) {
                name = ids.get(0);

                // Try match with variable first
                if(locals.containsKey(name)) {

                } else {

                    // Then try match with field first
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

                handledName = nameHandler.apply(name);
            } else {
                handledName = classFieldAccessHandler.apply(name, ids.get(i));
                i++;
            }

            List<String> fieldAccessChain;

            if (i < ids.size()) {
                // The rest should be considered field access
                fieldAccessChain = ids.subList(i, ids.size()).stream().collect(Collectors.toList());
            } else
                fieldAccessChain = Arrays.asList();

            //T handledName = nameHandler.apply(name);
            return fieldChainHandler.apply(handledName, fieldAccessChain);
        } else
            name = ids.get(0);

        return nameHandler.apply(name);
    }

    public StatementDomBuilder parseExpressionAsStatement(ParserRuleContext ctx, boolean atRoot) {
        return ctx.accept(new JavaBaseVisitor<StatementDomBuilder>() {
            @Override
            public StatementDomBuilder visitLeafExpression(@NotNull JavaParser.LeafExpressionContext ctx) {
                if (ctx.chainElement().size() == 0)
                    return parseExpressionAsStatement((ParserRuleContext) ctx.getChild(0), atRoot);

                ExpressionDomBuilder expressionBuilder = parseExpressionBuilder((ParserRuleContext) ctx.getChild(0), atRoot, false);

                for (int i = 0; i < ctx.chainElement().size() - 1; i++) {
                    JavaParser.ChainElementContext chainElement = ctx.chainElement(i);
                    ExpressionDomBuilder targetBuilder = expressionBuilder;
                    expressionBuilder = chainElement.accept(new JavaBaseVisitor<ExpressionDomBuilder>() {
                        @Override
                        public ExpressionDomBuilder visitFieldAssignment(@NotNull JavaParser.FieldAssignmentContext ctx) {
                            return fieldAssignmentExpression(ctx, atRoot, false, targetBuilder);
                        }

                        @Override
                        public ExpressionDomBuilder visitFieldAccess(@NotNull JavaParser.FieldAccessContext ctx) {
                            String name = ctx.identifier().ID().getText();

                            return (cr, cd, ci, locals, methodContext) ->
                                fieldAccess(cr, cd, ci, targetBuilder.build(cr, cd, ci, locals, methodContext), name, locals, methodContext);
                        }

                        @Override
                        public ExpressionDomBuilder visitInvocation(@NotNull JavaParser.InvocationContext ctx) {
                            return invocationExpression(ctx, atRoot, targetBuilder);
                        }
                    });
                }

                JavaParser.ChainElementContext chainElement = ctx.chainElement(ctx.chainElement().size() - 1);
                ExpressionDomBuilder targetBuilder = expressionBuilder;
                return chainElement.accept(new JavaBaseVisitor<StatementDomBuilder>() {
                    @Override
                    public StatementDomBuilder visitFieldAssignment(@NotNull JavaParser.FieldAssignmentContext ctx) {
                        return fieldAssignmentStatement(ctx, atRoot, false, targetBuilder);
                    }

                    @Override
                    public StatementDomBuilder visitFieldAccess(@NotNull JavaParser.FieldAccessContext ctx) {
                        return null;
                    }

                    @Override
                    public StatementDomBuilder visitInvocation(@NotNull JavaParser.InvocationContext ctx) {
                        return invocationStatement(ctx, atRoot, targetBuilder);
                    }
                });
            }

            @Override
            public StatementDomBuilder visitAssignment(@NotNull JavaParser.AssignmentContext ctx) {
                return buildAssignment(ctx.name.getText(), ctx.value, atRoot);
            }

            @Override
            public StatementDomBuilder visitInvocation(@NotNull JavaParser.InvocationContext ctx) {
                ExpressionDomBuilder target = Factory.self();
                return invocationStatement(ctx, atRoot, target);
            }
        });
    }

    public ExpressionDomBuilder parseExpressionBuilder(ParserRuleContext ctx, boolean atRoot, boolean asStatement) {
        return ctx.accept(new JavaBaseVisitor<ExpressionDomBuilder>() {
            @Override
            public ExpressionDomBuilder visitExpression(JavaParser.ExpressionContext ctx) {
                return ctx.anyExpression == null
                    ? parseExpressionBuilder((ParserRuleContext) ctx.getChild(0), atRoot, asStatement)
                    : Factory.expressionCapture();
            }

            @Override
            public ExpressionDomBuilder visitLeafExpression(@NotNull JavaParser.LeafExpressionContext ctx) {
                ExpressionDomBuilder expressionBuilder = parseExpressionBuilder((ParserRuleContext) ctx.getChild(0), atRoot, asStatement);

                for (JavaParser.ChainElementContext chainElement : ctx.chainElement()) {
                    ExpressionDomBuilder targetBuilder = expressionBuilder;
                    expressionBuilder = chainElement.accept(new JavaBaseVisitor<ExpressionDomBuilder>() {
                        @Override
                        public ExpressionDomBuilder visitFieldAssignment(@NotNull JavaParser.FieldAssignmentContext ctx) {
                            return fieldAssignmentExpression(ctx, atRoot, asStatement, targetBuilder);
                        }

                        @Override
                        public ExpressionDomBuilder visitFieldAccess(@NotNull JavaParser.FieldAccessContext ctx) {
                            String name = ctx.identifier().ID().getText();

                            return (cr, cd, ci, locals, methodContext) ->
                                fieldAccess(cr, cd, ci, targetBuilder.build(cr, cd, ci, locals, methodContext), name, locals, methodContext);
                        }

                        @Override
                        public ExpressionDomBuilder visitInvocation(@NotNull JavaParser.InvocationContext ctx) {
                            return invocationExpression(ctx, atRoot, targetBuilder);
                        }
                    });
                }

                return expressionBuilder;
            }

            @Override
            public ExpressionDomBuilder visitAssignment(@NotNull JavaParser.AssignmentContext ctx) {
                String name = ctx.ID().getText();
                ExpressionDomBuilder valueBuilder = parseExpressionBuilder(ctx.value, atRoot, false);

                return Factory.assignExpr(name, valueBuilder);
            }

            @Override
            public ExpressionDomBuilder visitAmbigousName(@NotNull JavaParser.AmbigousNameContext ctx) {
                List<String> nameParts = ctx.ID().stream().map(x -> x.getText()).collect(Collectors.toList());

                return Factory.ambiguousName(nameParts);
            }

            @Override
            public ExpressionDomBuilder visitIntLiteral(@NotNull JavaParser.IntLiteralContext ctx) {
                int value = parseIntValue(ctx);

                return Factory.literal(value);
            }

            @Override
            public ExpressionDomBuilder visitStringLiteral(@NotNull JavaParser.StringLiteralContext ctx) {
                String value = parseStringValue(ctx);

                return Factory.literal(value);
            }

            @Override
            public ExpressionDomBuilder visitNullLiteral(@NotNull JavaParser.NullLiteralContext ctx) {
                return Factory.nil();
            }

            @Override
            public ExpressionDomBuilder visitInvocation(@NotNull JavaParser.InvocationContext ctx) {
                ExpressionDomBuilder target = Factory.self();
                return invocationExpression(ctx, atRoot, target);
            }

            @Override
            public ExpressionDomBuilder visitNewInstance(@NotNull JavaParser.NewInstanceContext ctx) {
                String name = ctx.name.getText();
                List<ExpressionDomBuilder> argumentBuilders = ctx.arguments().expression().stream()
                    .map(x -> parseExpressionBuilder(x, atRoot, false)).collect(Collectors.toList());

                return Factory.newInstanceExpr(name, argumentBuilders);
            }

            @Override
            public ExpressionDomBuilder visitMethodBodyExpression(JavaParser.MethodBodyExpressionContext ctx) {
                return Factory.methodBodyExpression();
            }

            @Override
            public ExpressionDomBuilder visitExpressionLogicalAnd(JavaParser.ExpressionLogicalAndContext ctx) {
                ExpressionDomBuilder expressionBuilder = parseExpressionBuilder(ctx.first, false);

                for (JavaParser.ExpressionContext rhsCtx : ctx.expression()) {
                    ExpressionDomBuilder lhsBuilder = expressionBuilder;
                    ExpressionDomBuilder rhsBuilder = parseExpressionBuilder(rhsCtx, false);
                    expressionBuilder = Factory.logicalAnd(lhsBuilder, rhsBuilder, LogicalOperator.AND);
                }

                return expressionBuilder;
            }

            @Override
            public ExpressionDomBuilder visitRelationalExpression(JavaParser.RelationalExpressionContext ctx) {
                ExpressionDomBuilder expressionBuilder = parseExpressionBuilder(ctx.first, false);

                for (JavaParser.ExpressionContext rhsCtx : ctx.expression()) {
                    ExpressionDomBuilder lhsBuilder = expressionBuilder;
                    ExpressionDomBuilder rhsBuilder = parseExpressionBuilder(rhsCtx, false);
                    expressionBuilder = Factory.compare(lhsBuilder, rhsBuilder, RelationalOperator.EQ);
                }

                return expressionBuilder;
            }

            @Override
            public ExpressionDomBuilder visitInstanceOfExpression(JavaParser.InstanceOfExpressionContext ctx) {
                ExpressionDomBuilder target = parseExpressionBuilder(ctx.expression13(), false);
                String typeName = ctx.typeQualifier().getText();

                return Factory.instanceOf(target, typeName);
            }

            @Override
            public ExpressionDomBuilder visitTypeCastExpression(JavaParser.TypeCastExpressionContext ctx) {
                String targetTypeName = ctx.typeQualifier().getText();
                ExpressionDomBuilder expression = parseExpressionBuilder(ctx.expression(), false);

                return Factory.typeCast(expression, targetTypeName);
            }

            @Override
            public ExpressionDomBuilder visitThisLiteral(JavaParser.ThisLiteralContext ctx) {
                return Factory.self();
            }

            @Override
            public ExpressionDomBuilder visitTrueLiteral(JavaParser.TrueLiteralContext ctx) {
                return Factory.literal(true);
            }

            @Override
            public ExpressionDomBuilder visitFalseLiteral(JavaParser.FalseLiteralContext ctx) {
                return Factory.literal(false);
            }
        });
    }

    private int parseIntValue(@NotNull JavaParser.IntLiteralContext ctx) {
        return Integer.parseInt(ctx.getText());
    }

    private String parseStringValue(@NotNull JavaParser.StringLiteralContext ctx) {
        String rawString = ctx.getText();
        return rawString.substring(1, rawString.length() - 1)
            .replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
    }


    private StatementDomBuilder fieldAssignmentStatement(@NotNull JavaParser.FieldAssignmentContext ctx, boolean atRoot, boolean asStatement, ExpressionDomBuilder targetBuilder) {
        String name = ctx.identifier().ID() != null ? ctx.identifier().ID().getText() : null;
        ExpressionDomBuilder valueBuilder = parseExpressionBuilder(ctx.value, atRoot, asStatement);

        return new StatementDomBuilder() {
            @Override
            public StatementDom build(ClassResolver cr, ClassDeclaration cd, ClassInspector ci, Map<String, String> locals, MethodDeclaration methodContext) {
                ExpressionDom value = valueBuilder.build(cr, cd, ci, locals, methodContext);
                ExpressionDom target = targetBuilder.build(cr, cd, ci, locals, methodContext);
                String targetType = expressionResultType(ci, cd, target, locals, Descriptor.get(methodContext.getReturnTypeName()));
                ClassDeclaration targetClassDeclaration = ci.getClassDeclaration(Descriptor.getName(targetType));
                Optional<FieldDeclaration> fieldDeclaration = targetClassDeclaration.getFields().stream().filter(x -> x.getName().equals(name)).findFirst();

                return assignField(target, fieldDeclaration.get().getName(), fieldDeclaration.get().getTypeName(), value);
            }

            @Override
            public String toString() {
                String id = name != null ? name : "?";
                return targetBuilder + "." + id + " = " + valueBuilder + ";";
            }

            @Override
            public boolean test(StatementDom statement, List<Object> captures) {
                String nameTest = name;

                return Util.returnFrom(true, r -> statement.accept(new DefaultStatementDomVisitor() {
                    @Override
                    public void visitBlock(List<StatementDom> statements) {
                        statements.forEach(x -> x.accept(this));
                    }

                    private boolean testName(String name) {
                        if(nameTest == null) {
                            captures.add(name);
                            return true;
                        }

                        return nameTest.equals(name);
                    }

                    @Override
                    public void visitFieldAssignment(ExpressionDom target, String name, String type, ExpressionDom value) {
                        if(targetBuilder.test(target, captures) &&
                            testName(name) &&
                            valueBuilder.test(value, captures)) {
                            // For each match, the replacement code should be injected and should only replace the matched
                            // code
                            r.accept(true);
                        }
                    }
                }));
            }
        };
    }

    private ExpressionDomBuilder fieldAssignmentExpression(@NotNull JavaParser.FieldAssignmentContext ctx, boolean atRoot, boolean asStatement, ExpressionDomBuilder targetBuilder) {
        String name = ctx.identifier().ID() != null ? ctx.identifier().ID().getText() : null;
        ExpressionDomBuilder valueBuilder = parseExpressionBuilder(ctx.value, atRoot, asStatement);

        return new ExpressionDomBuilder() {
            @Override
            public ExpressionDom build(ClassResolver cr, ClassDeclaration cd, ClassInspector ci, Map<String, String> locals, MethodDeclaration methodContext) {
                ExpressionDom value = valueBuilder.build(cr, cd, ci, locals, methodContext);
                ExpressionDom target = targetBuilder.build(cr, cd, ci, locals, methodContext);
                String targetType = expressionResultType(ci, cd, target, locals, Descriptor.get(methodContext.getReturnTypeName()));
                ClassDeclaration targetClassDeclaration = ci.getClassDeclaration(Descriptor.getName(targetType));
                Optional<FieldDeclaration> fieldDeclaration = targetClassDeclaration.getFields().stream().filter(x -> x.getName().equals(name)).findFirst();

                return top(target, (newTarget, newTargetLast) -> {
                    return blockExpr(Arrays.asList(
                        assignField(newTarget, fieldDeclaration.get().getName(), fieldDeclaration.get().getTypeName(), value),
                        fieldAccess(cr, cd, ci, newTargetLast, name, locals, methodContext)
                    ));
                });
            }

            @Override
            public String toString() {
                String id = name != null ? name : "?";
                return targetBuilder + "." + id + " = " + valueBuilder;
            }

            @Override
            public boolean test(ExpressionDom expression, List<Object> captures) {
                String nameTest = name;

                return Util.returnFrom(false, r -> expression.accept(new DefaultExpressionDomVisitor() {
                    @Override
                    public void visitBlock(List<CodeDom> codeList) {
                        if(codeList.size() == 2) {
                            boolean isMatch = Util.returnFrom(false, r -> codeList.get(0).accept(new DefaultCodeDomVisitor() {
                                @Override
                                public void visitStatement(StatementDom statementDom) {
                                    statementDom.accept(new DefaultStatementDomVisitor() {
                                        @Override
                                        public void visitFieldAssignment(ExpressionDom target, String name, String type, ExpressionDom value) {
                                            r.accept(
                                                targetBuilder.test(target, captures) &&
                                                (nameTest != null ? nameTest.equals(name) : true) &&
                                                valueBuilder.test(value, captures)
                                            );
                                        }
                                    });
                                }
                            })) && Util.returnFrom(false, r -> codeList.get(1).accept(new DefaultCodeDomVisitor() {
                                @Override
                                public void visitExpression(ExpressionDom expressionDom) {
                                    expressionDom.accept(new DefaultExpressionDomVisitor() {
                                        @Override
                                        public void visitFieldAccess(ExpressionDom target, String name, String fieldTypeName) {
                                            r.accept(
                                                targetBuilder.test(target, captures) &&
                                                (nameTest != null ? nameTest.equals(name) : true)/* &&
                                                valueBuilder.test(value, captures)*/
                                            );
                                        }
                                    });
                                }
                            }));
                        }
                    }
                }));
            }
        };
    }

    private ExpressionDomBuilder invocationExpression(@NotNull JavaParser.InvocationContext ctx, boolean atRoot, ExpressionDomBuilder targetBuilder) {
        List<ExpressionDomBuilder> argumentBuilders = ctx.arguments().expression().stream()
            .map(x -> parseExpressionBuilder(x, atRoot, false)).collect(Collectors.toList());

        String methodName = ctx.identifier().ID().getText();
        return Factory.invocationExpr(targetBuilder, methodName, argumentBuilders);
    }

    private StatementDomBuilder invocationStatement(@NotNull JavaParser.InvocationContext ctx, boolean atRoot, ExpressionDomBuilder targetBuilder) {
        List<ExpressionDomBuilder> argumentBuilders = ctx.arguments().expression().stream()
            .map(x -> parseExpressionBuilder(x, atRoot, false)).collect(Collectors.toList());

        String methodName = ctx.identifier().ID().getText();
        return Factory.invocation(targetBuilder, methodName, argumentBuilders);
    }

    public static ExpressionDom fieldAccess(ClassResolver cr, ClassDeclaration cd, ClassInspector ci, ExpressionDom target, String fieldName, Map<String, String> locals, MethodDeclaration methodContext) {
        String targetType = expressionResultType(ci, cd, target, locals, Descriptor.get(methodContext.getReturnTypeName()));
        ClassDeclaration targetClassDeclaration = ci.getClassDeclaration(Descriptor.getName(targetType));

        // Should investigate hierarchy
        Optional<FieldDeclaration> field = targetClassDeclaration.getFields().stream().filter(x -> x.getName().equals(fieldName)).findFirst();

        return accessField(target, fieldName, Descriptor.get(field.get().getTypeName()));
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

    public static <T> T resolveMethod(ClassInspector classInspector, ClassDeclaration targetClass, String methodName, List<ClassDeclaration> argumentTypes, BiFunction<ClassDeclaration, MethodDeclaration, T> reducer) {
        List<MethodDeclaration> methods = targetClass.getMethods().stream()
            .filter(x -> x.getName().equals(methodName))
            .filter(x -> x.getParameterTypes().size() == argumentTypes.size())
            .filter(x -> IntStream.range(0, argumentTypes.size()).allMatch(i ->
                // Compare full inheritance
                isCompatible(classInspector, argumentTypes.get(i), x.getParameterTypes().get(0))
                //Descriptor.getName(x.getParameterTypes().get(0).descriptor).equals(argumentTypes.get(i).getName())
            ))
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

    private static boolean isCompatible(ClassInspector classInspector, ClassDeclaration argumentType, ParameterInfo parameterType) {
        while(true) {
            if (Descriptor.getName(parameterType.descriptor).equals(argumentType.getName())) {
                return true;
            }

            if (argumentType.getSuperName() != null)
                argumentType = classInspector.getClassDeclaration(argumentType.getSuperName());
            else
                break;
        }

        return false;
    }

    public List<ClassNodePredicate> parseClassPredicates(ClassInspector classInspector) {
        JavaParser.ClassPredicateContext ctx = parser.classPredicate();

        ArrayList<ClassNodePredicate> predicates = new ArrayList<>();

        ctx.classPredicateElement().forEach(x -> {
            x.accept(new JavaBaseVisitor<Void>() {
                @Override
                public Void visitAnnotation(JavaParser.AnnotationContext ctx) {
                    Predicate<List<AnnotationNode>> hasAnnotation = hasAnnotationPredicate(ctx);
                    predicates.add(classNode -> hasAnnotation.test((List<AnnotationNode>)classNode.visibleAnnotations));

                    return null;
                }

                /*@Override
                public Void visitClassPredicateAccessModifier(JavaParser.ClassPredicateAccessModifierContext ctx) {
                    int accessModifier = parseAccessModifier(ctx.accessModifier());
                    predicates.add(classNode ->
                        classNode.access == accessModifier);

                    return null;
                }*/

                @Override
                public Void visitClassPredicateName(JavaParser.ClassPredicateNameContext ctx) {
                    String name = Descriptor.get(ctx.name.getText());
                    predicates.add(classNode ->
                        classNode.name.equals(name));

                    return null;
                }

                @Override
                public Void visitClassPredicateExtends(JavaParser.ClassPredicateExtendsContext ctx) {
                    String superClassName = Descriptor.get(ctx.superClassName.getText());
                    // What about indirect inheritance?
                    predicates.add(classNode -> {
                        if(classNode.superName.equals(superClassName))
                            return true;

                        if(classNode.superName != null) {
                            ClassDeclaration superClass = classInspector.getClassDeclarationFromDescriptor(classNode.superName);

                            while (true) {
                                if (superClass.getSuperName() != null && Descriptor.get(superClass.getSuperName()).equals(superClassName))
                                    return true;
                                else {
                                    if (superClass.getSuperName() != null)
                                        superClass = classInspector.getClassDeclaration(superClass.getSuperName());
                                    else
                                        return false;
                                }
                            }
                        } else
                            return false;
                    });

                    return null;
                }

                @Override
                public Void visitClassPredicateImplements(JavaParser.ClassPredicateImplementsContext ctx) {
                    List<String> interfaceNames =
                        ctx.classPredicateInterface().stream().map(x -> Descriptor.get(x.getText())).collect(Collectors.toList());
                    // What about indirect implementors?
                    predicates.add(classNode -> {
                        if(interfaceNames.stream().allMatch(interfaceName -> classNode.interfaces.contains(interfaceName)))
                            return true;

                        if(classNode.superName != null) {
                            ClassDeclaration superClass = classInspector.getClassDeclarationFromDescriptor(classNode.superName);

                            while(true) {
                                final ClassDeclaration superClassToTest = superClass;
                                if(interfaceNames.stream().map(x -> Descriptor.getName(x)).allMatch(interfaceName -> superClassToTest.getInterfaces().contains(interfaceName)))
                                    return true;
                                else {
                                    if(superClass.getSuperName() != null)
                                        superClass = classInspector.getClassDeclaration(superClass.getSuperName());
                                    else
                                        return false;
                                }
                            }

                        } else
                            return false;
                    });

                    return null;
                }

                @Override
                public Void visitClassPredicateField(JavaParser.ClassPredicateFieldContext ctx) {
                    int modifier = parseModifiers(ctx.modifiers());
                    String name = ctx.name != null ? ctx.name.getText() : null;
                    String type = ctx.type != null ? Descriptor.getFieldDescriptor(Descriptor.get(ctx.type.getText())) : null;

                    predicates.add(classNode -> ((List<FieldNode>)classNode.fields).stream().anyMatch(x -> {
                        if(modifier != 0 && x.access != modifier)
                            return false;

                        if(name != null && !x.name.equals(name))
                            return false;

                        if(type != null && !x.desc.equals(type))
                            return false;

                        return true;
                    }));

                    return null;
                }

                @Override
                public Void visitClassPredicateMethod(JavaParser.ClassPredicateMethodContext ctx) {
                    int modifier = parseModifiers(ctx.modifiers());
                    String name = ctx.name != null ? ctx.name.getText() : null;
                    String returnType = ctx.returnType != null ? Descriptor.getFieldDescriptor(Descriptor.get(ctx.returnType.getText())) : null;

                    boolean anyParams = ctx.classPredicateMethodParameters().anyParams != null;
                    List<String> parameterTypes = ctx.classPredicateMethodParameters().typeQualifier().stream()
                        .map(x -> Descriptor.getFieldDescriptor(Descriptor.get(x.getText()))).collect(Collectors.toList());

                    predicates.add(classNode -> ((List<MethodNode>)classNode.methods).stream().anyMatch(x -> {
                        if(modifier != 0 && x.access != modifier)
                            return false;

                        if(name != null && !x.name.equals(name))
                            return false;

                        if(returnType != null && !Type.getReturnType(x.desc).getDescriptor().equals(returnType))
                            return false;

                        if(!anyParams) {
                            Type[] xParameters = Type.getArgumentTypes(x.desc);
                            return parameterTypes.size() == xParameters.length &&
                                IntStream.range(0, parameterTypes.size()).allMatch(i -> parameterTypes.get(i).equals(xParameters[i].getDescriptor()));
                        }

                        return true;
                    }));

                    return null;
                }
            });
        });

        return predicates;
    }

    private Predicate<List<AnnotationNode>> hasAnnotationPredicate(JavaParser.AnnotationContext annotationContext) {
        String typeName = annotationContext.typeQualifier().getText();
        Map<String, Object> values = parseAnnotationValues(annotationContext);

        return visibleAnnotations -> {
            if(visibleAnnotations != null && visibleAnnotations.size() > 0) {
                return ((List<AnnotationNode>)visibleAnnotations).stream()
                    .anyMatch(x -> {
                        if(Descriptor.getDescriptorName(x.desc).equals(typeName)) {
                            if(values.size() > 0) {
                                if(x.values != null) {
                                    return IntStream
                                        .iterate(0, i -> i + 2).limit(values.size())
                                        .allMatch(i -> {
                                            String name = (String)x.values.get(i);
                                            Object value = x.values.get(i + 1);

                                            if(values.containsKey(name)) {
                                                return values.get(name).equals(value);
                                            }

                                            return true;
                                        });
                                }
                            } else
                                return true;
                        }

                        return false;
                    });
            }

            return false;
        };
    }

    public List<DeclaringClassNodeExtenderElementMethodNodePredicate> parseMethodPredicates() {
        JavaParser.MethodPredicateContext ctx = parser.methodPredicate();

        return ctx.methodPredicateElement().stream()
            .map(x -> x.accept(new JavaBaseVisitor<DeclaringClassNodeExtenderElementMethodNodePredicate>() {
                @Override
                public DeclaringClassNodeExtenderElementMethodNodePredicate visitMethodPredicateAnnotation(JavaParser.MethodPredicateAnnotationContext ctx) {
                    Predicate<List<AnnotationNode>> hasAnnotation = hasAnnotationPredicate(ctx.annotation());

                    return (classNode, thisClass, classResolver, methodNode) ->
                        hasAnnotation.test((List<AnnotationNode>) methodNode.visibleAnnotations);
                }

                @Override
                public DeclaringClassNodeExtenderElementMethodNodePredicate visitMethodPredicateAccessModifiers(JavaParser.MethodPredicateAccessModifiersContext ctx) {
                    int modifiers = ctx.modifier().stream()
                        .mapToInt(x -> mapModifier(x.getText()))
                        .reduce((x, y) -> x & y).getAsInt();

                    return (classNode, thisClass, classResolver, methodNode) ->
                        methodNode.access == modifiers;
                }

                @Override
                public DeclaringClassNodeExtenderElementMethodNodePredicate visitMethodPredicateAccessTypeAndName(JavaParser.MethodPredicateAccessTypeAndNameContext ctx) {
                    String returnType = ctx.returnType != null ? Descriptor.getMethodDescriptorPart(Descriptor.get(ctx.returnType.getText())) : null;
                    String name = ctx.name != null ? ctx.name.getText() : null;

                    return (classNode, thisClass, classResolver, methodNode) -> {
                        if (returnType != null && !Type.getReturnType(methodNode.desc).getDescriptor().equals(returnType))
                            return false;

                        if (name != null && !methodNode.name.equals(name))
                            return false;

                        return true;
                    };
                }

                @Override
                public DeclaringClassNodeExtenderElementMethodNodePredicate visitMethodPredicateParameters(JavaParser.MethodPredicateParametersContext ctx) {
                    List<String> parameterTypes = ctx.typeQualifier().stream()
                        .map(x -> Descriptor.getFieldDescriptor(Descriptor.get(x.getText()))).collect(Collectors.toList());

                    return (classNode, thisClass, classResolver, methodNode) -> {
                        Type[] xParameters = Type.getArgumentTypes(methodNode.desc);
                        return parameterTypes.size() == xParameters.length &&
                            IntStream.range(0, parameterTypes.size()).allMatch(i -> parameterTypes.get(i).equals(xParameters[i].getDescriptor()));
                    };
                }
            }))
            .collect(Collectors.toList());
    }

    private int mapModifier(String str) {
        switch (str) {
            case "private":
                return Modifier.PRIVATE;
            case "protected":
                return Modifier.PROTECTED;
            case "public":
                return Modifier.PUBLIC;
            case "static":
                return Modifier.STATIC;
            case "abstract":
                return Modifier.ABSTRACT;
        }

        return -1;
    }

    public List<DeclaringMethodNodeExtenderElement> parseMethodModifications(ClassInspector classInspector) {
        JavaParser.MethodModificationContext ctx = parser.methodModification();

        return ctx.methodModificationElement().stream()
            .map(x -> x.accept(new JavaBaseVisitor<DeclaringMethodNodeExtenderElement>() {
                @Override
                public DeclaringMethodNodeExtenderElement visitMethodModificationAnnotation(JavaParser.MethodModificationAnnotationContext ctx) {
                    String typeName = ctx.annotation().typeQualifier().getText();
                    String desc = Descriptor.getTypeDescriptor(Descriptor.get(typeName));
                    Map<String, Object> values = parseAnnotationValues(ctx.annotation());

                    return new DeclaringMethodNodeExtenderElement() {
                        @Override
                        public DeclaringMethodNodeExtenderTransformer declare(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, MethodNode methodNode) {
                            return new DeclaringMethodNodeExtenderTransformer() {
                                @Override
                                public void transform(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, ClassInspector classInspector, MethodNode methodNode, GeneratorAdapter generator, InsnList originalInstructions) {
                                    AnnotationVisitor annotation = methodNode.visitAnnotation(desc, true);
                                    values.entrySet().stream().forEach(v -> annotation.visit(v.getKey(), v.getValue()));
                                }
                            };
                        }
                    };
                }

                @Override
                public DeclaringMethodNodeExtenderElement visitMethodModificationBody(JavaParser.MethodModificationBodyContext ctx) {
                    StatementDomBuilder statementDomBuilder =
                        Factory.block(ctx.statement().stream().map(x -> parseStatementBuilder(x, true)).collect(Collectors.toList()));

                    /*List<List<JavaParser.StatementContext>> bodySplit = ctx.statement().stream()
                        .reduce(new ArrayList<>(Arrays.asList(new ArrayList<>())),
                            (list, s) -> {
                                boolean isMethodBody = s.accept(new JavaBaseVisitor<Boolean>() {
                                    @Override
                                    public Boolean visitMethodBody(JavaParser.MethodBodyContext ctx) {
                                        return true;
                                    }
                                }) != null;
                                if (isMethodBody)
                                    list.add(new ArrayList<>());
                                else
                                    list.get(list.size() - 1).add(s);

                                return list;
                            },
                            (list1, list2) -> {
                                list1.addAll(list2);
                                return list1;
                            });

                    if (bodySplit.size() > 1) {
                        // There are method body constructs
                        return IntStream.range(0, bodySplit.size())
                            .mapToObj(i -> {
                                StatementDomBuilder part =
                                    Factory.block(bodySplit.get(i).stream().map(x -> parseStatementBuilder(x, true)).collect(Collectors.toList()));

                                return i == 0
                                    ? MethodNodeExtenderFactory.prepend(part, classInspector)
                                    : MethodNodeExtenderFactory.append(part, classInspector);
                            }).reduce((x, y) -> x.andThen(y)).get();
                    } else {
                        // Simply replace body
                    }*/

                    return new DeclaringMethodNodeExtenderElement() {
                        @Override
                        public DeclaringMethodNodeExtenderTransformer declare(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, MethodNode methodNode) {
                            return new DeclaringMethodNodeExtenderTransformer() {
                                @Override
                                public void transform(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, ClassInspector classInspector, MethodNode methodNode, GeneratorAdapter generator, InsnList originalInstructions) {
                                    Map<String, String> locals = ASMClassDeclaration.getMethod(methodNode).getParameterTypes().stream()
                                        .collect(Collectors.toMap(p -> p.getName(), p -> Descriptor.get(p.getTypeName())));
                                    StatementDom statement = statementDomBuilder.build(classResolver, thisClass, classInspector, locals, ASMClassDeclaration.getMethod(methodNode));

                                    MethodGenerator methodGenerator = new MethodGenerator(
                                        classNode.name,
                                        ASMClassDeclaration.getMethod(methodNode).getParameterTypes(),
                                        statement);

                                    methodGenerator.populateMethodBody(methodNode, originalInstructions, generator);
                                }
                            };
                        }
                    };
                }
            }))
            .collect(Collectors.toList());
    }

    public DeclaringClassNodeExtenderElementBodyNodePredicate parseBodyPredicates() {
        JavaParser.StatementOrExpressionContext body = parser.statementOrExpression();

        DomBuilder bodyBuilder;

        if(body.statement() != null) {
            bodyBuilder = parseStatementBuilder(body.statement(), true);
        } else if(body.expression() != null) {
            bodyBuilder = parseExpressionBuilder(body.expression(), true);
        } else
            bodyBuilder = null;

        return new DeclaringClassNodeExtenderElementBodyNodePredicate() {
            @Override
            public boolean test(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver, MethodNode methodNode, List<Object> captures) {
                Textifier textifier = new Textifier();
                methodNode.accept(new TraceMethodVisitor(textifier));
                textifier.getText().forEach(x -> System.out.print(x));

                ByteCodeToTree byteCodeToTree = new ByteCodeToTree(methodNode);
                methodNode.instructions.accept(byteCodeToTree);
                StatementDom body = byteCodeToTree.getBlock();

                return bodyBuilder.test(body, captures);
            }
        };
    }

    public List<DeclaringBodyNodeExtenderElement> parseBodyModifications(ClassInspector classInspector) {
        JavaParser.StatementOrExpressionContext body = parser.statementOrExpression();

        DomBuilder bodyBuilder = null;

        if(body.statement() != null) {
            bodyBuilder = parseStatementBuilder(body.statement(), true);
        } else if(body.expression() != null) {
            bodyBuilder = parseExpressionBuilder(body.statement(), true);
        }

        return null;
    }
}
