package astava.java.parser;

import astava.java.Descriptor;
import astava.java.parser.antlr4.JavaBaseVisitor;
import astava.java.parser.antlr4.JavaLexer;
import astava.java.parser.antlr4.JavaParser;
import astava.samples.drawnmap.lang.antlr4.DrawNMapParser;
import astava.tree.*;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
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

    public List<DomBuilder> parse() {
        return parser.script().element().stream().map(x -> x.accept(new JavaBaseVisitor<DomBuilder>() {
            @Override
            public DomBuilder visitClassDefinition(@NotNull JavaParser.ClassDefinitionContext ctx) {
                MutableClassDomBuilder classBuilder = new MutableClassDomBuilder();
                parseClass(classBuilder);
                return classBuilder;
            }

            @Override
            public DomBuilder visitFieldDefinition(@NotNull JavaParser.FieldDefinitionContext ctx) {
                return parseFieldBuilder(ctx);
            }

            @Override
            public DomBuilder visitMethodDefinition(@NotNull JavaParser.MethodDefinitionContext ctx) {
                return parseMethodBuilder(ctx);
            }

            @Override
            public DomBuilder visitStatement(@NotNull JavaParser.StatementContext ctx) {
                return parseStatementBuilder(ctx);
            }

            @Override
            public DomBuilder visitExpression(@NotNull JavaParser.ExpressionContext ctx) {
                return parseExpressionBuilder(ctx);
            }
        })).collect(Collectors.toList());
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
        parseClass(classBuilder);
        return classBuilder;
    }

    public void parseClass(MutableClassDomBuilder classBuilder) {
        parser.classDefinition().accept(new JavaBaseVisitor<Void>() {
            @Override
            public Void visitClassDefinition(@NotNull JavaParser.ClassDefinitionContext ctx) {
                int modifiers = parseModifiers(ctx.modifiers());
                String name = ctx.name.getText();
                String superName = Descriptor.get(Object.class);

                classBuilder.setModifiers(modifiers);
                classBuilder.setName(name);
                classBuilder.setSuperName(superName);

                ArrayList<MethodDomBuilder> methodBuilders = new ArrayList<MethodDomBuilder>();

                ctx.classMember().forEach(m -> {
                    m.accept(new JavaBaseVisitor<Void>() {
                        @Override
                        public Void visitFieldDefinition(@NotNull JavaParser.FieldDefinitionContext ctx) {
                            FieldDomBuilder field = parseFieldBuilder(ctx);
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
        return parseFieldBuilder(parser.fieldDefinition());
    }

    public FieldDomBuilder parseFieldBuilder(JavaParser.FieldDefinitionContext ctx) {
        return new FieldDomBuilder() {
            @Override
            public FieldDeclaration declare(ClassResolver classResolver) {
                String typeName = parseTypeQualifier(classResolver, ctx.type.getText());
                int modifiers = parseModifiers(ctx.modifiers());
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
                String returnType = parseTypeQualifier(classResolver, ctx.returnType.getText());
                int modifiers = parseModifiers(ctx.modifiers());
                String name = ctx.name.getText();
                //List<String> parameterTypes = ctx.parameters().parameter().stream().map(x -> parseTypeQualifier(classResolver, x.type.getText())).collect(Collectors.toList());
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
                    public MethodDom build(ClassDeclaration classDeclaration) {
                        HashSet<String> locals = new HashSet<>();
                        locals.addAll(parameters.stream().map(x -> x.name).collect(Collectors.toList()));
                        List<StatementDomBuilder> statementBuilders = ctx.statement().stream().map(x -> parseStatementBuilder(x)).collect(Collectors.toList());
                        statementBuilders.forEach(x -> x.appendLocals(locals));
                        List<StatementDom> statements = statementBuilders.stream().map(x -> x.build(classResolver, classDeclaration, locals)).collect(Collectors.toList());
                        StatementDom body = block(statements);

                        // Ugly hack
                        // Instead: every leaf statement should either be a ret statement or one is injected
                        // This logic probably shouldn't be located here?
                        if(returnType.equals(Descriptor.VOID)) {
                            statements.add(ret());
                        }

                        return methodDeclaration(modifiers, name, parameters, returnType, body);
                    }
                };
            }
        };
    }

    public StatementDomBuilder parseStatementBuilder(JavaParser.StatementContext ctx) {
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
                ExpressionDomBuilder expression = parseExpressionBuilder(ctx.expression());
                return new StatementDomBuilder() {
                    @Override
                    public void appendLocals(Set<String> locals) {

                    }

                    @Override
                    public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, Set<String> locals) {
                        return ret(expression.build(classResolver, classDeclaration, locals));
                    }
                };
            }

            @Override
            public StatementDomBuilder visitVariableDeclaration(@NotNull JavaParser.VariableDeclarationContext ctx) {
                /*String type = parseTypeQualifier(null, ctx.type.getText());
                String name = ctx.name.getText();

                StatementDom statement = declareVar(type, name);

                if (ctx.value != null) {
                    ExpressionDom value = parseExpression(ctx.value);
                    statement = block(Arrays.asList(statement, assignVar(name, value)));
                }*/
                String name = ctx.name.getText();

                ExpressionDomBuilder valueBuilder = ctx.value != null ? parseExpressionBuilder(ctx.value) : null;

                return new StatementDomBuilder() {
                    @Override
                    public void appendLocals(Set<String> locals) {
                        locals.add(name);
                    }

                    @Override
                    public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, Set<String> locals) {
                        String type = parseTypeQualifier(classResolver, ctx.type.getText());

                        StatementDom statement = declareVar(type, name);

                        if (valueBuilder != null) {
                            statement = block(Arrays.asList(statement, assignVar(name, valueBuilder.build(classResolver, classDeclaration, locals))));
                        }

                        return statement;
                    }
                };
            }

            @Override
            public StatementDomBuilder visitAssignment(@NotNull JavaParser.AssignmentContext ctx) {
                ExpressionDomBuilder valueBuilder = parseExpressionBuilder(ctx.value);

                return new StatementDomBuilder() {
                    @Override
                    public void appendLocals(Set<String> locals) {

                    }

                    @Override
                    public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, Set<String> locals) {
                        ExpressionDom value = valueBuilder.build(classResolver, classDeclaration, locals);

                        return parseAmbiguousName(ctx.name.ID(), classResolver, classDeclaration,
                            name -> {
                                Optional<FieldDeclaration> fieldDeclaration = classDeclaration.getFields().stream().filter(x -> x.getName().equals(name)).findFirst();
                                if (fieldDeclaration.isPresent()) {
                                    if (Modifier.isStatic(fieldDeclaration.get().getModifiers()))
                                        return assignStaticField(classDeclaration.getName(), name, value);
                                    return assignField(self(), name, value);
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
        });
    }

    public ExpressionDom parseExpression() {
        return parseExpression(parser.expression());
    }

    public ExpressionDom parseExpression(JavaParser.ExpressionContext ctx) {
        return parseExpression(ctx, false);
    }

    public ExpressionDom parseExpression(JavaParser.ExpressionContext ctx, boolean asStatement) {
        return ctx.accept(new JavaBaseVisitor<ExpressionDom>() {
            @Override
            public ExpressionDom visitAmbigousName(@NotNull JavaParser.AmbigousNameContext ctx) {
                // Only support for variables

                String name = ctx.getText();

                return accessVar(name);
            }

            @Override
            public ExpressionDom visitIntLiteral(@NotNull JavaParser.IntLiteralContext ctx) {
                int value = Integer.parseInt(ctx.getText());
                return literal(value);
            }

            @Override
            public ExpressionDom visitStringLiteral(@NotNull JavaParser.StringLiteralContext ctx) {
                String rawString = ctx.getText();
                String value = rawString.substring(1, rawString.length() - 1)
                    .replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
                return literal(value);
            }
        });
    }

    public ExpressionDomBuilder parseExpressionBuilder(JavaParser.ExpressionContext ctx) {
        return parseExpressionBuilder(ctx, false);
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

    public ExpressionDomBuilder parseExpressionBuilder(JavaParser.ExpressionContext ctx, boolean asStatement) {
        return ctx.accept(new JavaBaseVisitor<ExpressionDomBuilder>() {
            @Override
            public ExpressionDomBuilder visitAmbigousName(@NotNull JavaParser.AmbigousNameContext ctx) {
                return (cr, cd, locals) -> {
                    return parseAmbiguousName(ctx.ID(), cr, cd,
                        name -> {
                            Optional<FieldDeclaration> fieldDeclaration = cd.getFields().stream().filter(x -> x.getName().equals(name)).findFirst();
                            if (fieldDeclaration.isPresent()) {
                                if (Modifier.isStatic(fieldDeclaration.get().getModifiers()))
                                    return accessStaticField(cd.getName(), name, fieldDeclaration.get().getTypeName());
                                return accessField(self(), name, fieldDeclaration.get().getTypeName());
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

                return (cr, cd, locals) -> literal(value);
            }

            @Override
            public ExpressionDomBuilder visitStringLiteral(@NotNull JavaParser.StringLiteralContext ctx) {
                String rawString = ctx.getText();
                String value = rawString.substring(1, rawString.length() - 1)
                    .replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
                return (cr, cd, locals) -> literal(value);
            }
        });
    }
}
