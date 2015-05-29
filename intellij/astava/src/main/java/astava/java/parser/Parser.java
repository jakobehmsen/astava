package astava.java.parser;

import astava.java.Descriptor;
import astava.java.parser.antlr4.JavaBaseVisitor;
import astava.java.parser.antlr4.JavaLexer;
import astava.java.parser.antlr4.JavaParser;
import astava.tree.*;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    private String parseTypeQualifier(ClassResolver classResolver, String typeQualifier) {
        if(!Descriptor.isPrimitiveName(typeQualifier)) {
            if (!classResolver.canResolveAmbiguous(typeQualifier))
                typeQualifier = classResolver.resolveSimpleName(typeQualifier);
        }

        return Descriptor.get(typeQualifier);
    }

    private int parseModifier(JavaParser.ModifierContext ctx) {
        int modifier = 0;

        if(ctx.accessModifier() != null) {
            if(ctx.accessModifier().KW_PRIVATE() != null)
                modifier |= Modifier.PRIVATE;
            else if(ctx.accessModifier().KW_PROTECTED() != null)
                modifier |= Modifier.PROTECTED;
            else if(ctx.accessModifier().KW_PUBLIC() != null)
                modifier |= Modifier.PUBLIC;
        }

        if(ctx.KW_ABSTRACT() != null)
            modifier |= Modifier.ABSTRACT;

        if(ctx.KW_STATIC() != null)
            modifier |= Modifier.STATIC;

        return modifier;
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
                int modifier = parseModifier(ctx.modifier());
                String name = ctx.name.getText();
                String superName = Descriptor.get(Object.class);

                classBuilder.setModifier(modifier);
                classBuilder.setName(name);
                classBuilder.setSuperName(superName);

                ArrayList<MethodDomBuilder> methodBuilders = new ArrayList<MethodDomBuilder>();

                ctx.classMember().forEach(m -> {
                    m.accept(new JavaBaseVisitor<Void>() {
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

    public MethodDomBuilder parseMethodBuilder() {
        return parseMethodBuilder(parser.methodDefinition());
    }

    public MethodDomBuilder parseMethodBuilder(JavaParser.MethodDefinitionContext ctx) {
        return (cr, cd) -> {
            String returnType = parseTypeQualifier(cr, ctx.returnType.getText());
            int modifier = parseModifier(ctx.modifier());
            String name = ctx.name.getText();
            List<String> parameterTypes = ctx.parameters().parameter().stream().map(x -> parseTypeQualifier(cr, x.type.getText())).collect(Collectors.toList());
            List<StatementDom> statements = ctx.statement().stream().map(x -> parseStatementBuilder(x).build(cr, cd)).collect(Collectors.toList());
            StatementDom body = block(statements);

            return methodDeclaration(modifier, name, parameterTypes, returnType, body);
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
                return (cr, cd) -> ret(expression.build(cr, cd));
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

                return (cr, cd) -> {
                    String type = parseTypeQualifier(cr, ctx.type.getText());
                    String name = ctx.name.getText();

                    StatementDom statement = declareVar(type, name);

                    if (ctx.value != null) {
                        ExpressionDomBuilder value = parseExpressionBuilder(ctx.value);
                        statement = block(Arrays.asList(statement, assignVar(name, value.build(cr, cd))));
                    }

                    return statement;
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

    public ExpressionDomBuilder parseExpressionBuilder(JavaParser.ExpressionContext ctx, boolean asStatement) {
        return ctx.accept(new JavaBaseVisitor<ExpressionDomBuilder>() {
            @Override
            public ExpressionDomBuilder visitAmbigousName(@NotNull JavaParser.AmbigousNameContext ctx) {
                return (cr, cd) -> {
                    // Find longest getName that can be resolved
                    String name;

                    if(ctx.ID().size() > 1) {
                        name = "";
                        int i = ctx.ID().size();

                        for (; i > 1; i--) {
                            name = ctx.ID().subList(0, i).stream().map(x -> x.getText()).collect(Collectors.joining("."));
                            if (cr.canResolveAmbiguous(name))
                                break;
                        }

                        if(i == 1) {
                            name = ctx.ID().get(0).getText();

                            // Try match with field first
                            String fieldName = name;
                            FieldDeclaration fieldDeclaration = cd.getFields().stream().filter(x -> x.getName().equals(fieldName)).findFirst().orElse(null);
                            if(fieldDeclaration == null) {
                                // What if simple getName cannot be resolved? Then it may be that getName is a field related to the class being defined
                                name = cr.resolveSimpleName(name);
                            }

                            // What is required to resolve an ambiguous getName?
                            // A set of field names and a class getName resolver

                            // So, a parser perhaps shouldn't yield Doms directly but rather something can yield Doms when requested?
                        }

                        if(i < ctx.ID().size()) {
                            // The rest should be considered field access
                            List<String> fieldAccessChain = ctx.ID().subList(i, ctx.ID().size()).stream().map(x -> x.getText()).collect(Collectors.toList());
                            new String();
                        }
                    } else
                        name = ctx.getText();

                    return accessVar(name);
                };
            }

            @Override
            public ExpressionDomBuilder visitIntLiteral(@NotNull JavaParser.IntLiteralContext ctx) {
                int value = Integer.parseInt(ctx.getText());
                return (cr, cd) -> literal(value);
            }

            @Override
            public ExpressionDomBuilder visitStringLiteral(@NotNull JavaParser.StringLiteralContext ctx) {
                String rawString = ctx.getText();
                String value = rawString.substring(1, rawString.length() - 1)
                    .replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
                return (cr, cd) -> literal(value);
            }
        });
    }
}
