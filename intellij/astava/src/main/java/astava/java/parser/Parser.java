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

    private String parseTypeQualifier(String typeQualifier) {
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

    public ClassDom parseClass() {
        return parser.classDefinition().accept(new JavaBaseVisitor<ClassDom>() {
            @Override
            public ClassDom visitClassDefinition(@NotNull JavaParser.ClassDefinitionContext ctx) {
                int modifier = parseModifier(ctx.modifier());
                String name = ctx.name.getText();
                String superName = Descriptor.get(Object.class);
                ArrayList<MethodDom> methods = new ArrayList<MethodDom>();

                ctx.classMember().forEach(m -> {
                    m.accept(new JavaBaseVisitor<Void>() {
                        @Override
                        public Void visitMethodDefinition(@NotNull JavaParser.MethodDefinitionContext ctx) {
                            MethodDom method = parseMethod(ctx);
                            methods.add(method);

                            return null;
                        }
                    });
                });

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
                        return Arrays.asList();
                    }

                    @Override
                    public List<MethodDom> getMethods() {
                        return methods;
                    }
                };
            }
        });
    }

    public MethodDom parseMethod() {
        return parseMethod(parser.methodDefinition());
    }

    public MethodDom parseMethod(JavaParser.MethodDefinitionContext ctx) {
        String returnType = parseTypeQualifier(ctx.returnType.getText());
        int modifier = parseModifier(ctx.modifier());
        String name = ctx.name.getText();
        List<String> parameterTypes = ctx.parameters().parameter().stream().map(x -> parseTypeQualifier(x.type.getText())).collect(Collectors.toList());
        List<StatementDom> statements = ctx.statement().stream().map(x -> parseStatement(x)).collect(Collectors.toList());
        StatementDom body = block(statements);

        return new MethodDom() {
            @Override
            public int getModifier() {
                return modifier;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public List<String> getParameterTypes() {
                return parameterTypes;
            }

            @Override
            public String getReturnTypeName() {
                return returnType;
            }

            @Override
            public StatementDom getBody() {
                return body;
            }
        };
    }

    public StatementDom parseStatement() {
        return parseStatement(parser.statement());
    }

    public StatementDom parseStatement(JavaParser.StatementContext ctx) {
        return ctx.accept(new JavaBaseVisitor<StatementDom>() {
            @Override
            public StatementDom visitStatement(@NotNull JavaParser.StatementContext ctx) {
                return ctx.getChild(0).accept(this);
            }

            @Override
            public StatementDom visitDelimitedStatement(@NotNull JavaParser.DelimitedStatementContext ctx) {
                return ctx.getChild(0).accept(this);
            }

            @Override
            public StatementDom visitReturnStatement(@NotNull JavaParser.ReturnStatementContext ctx) {
                ExpressionDom expression = parseExpression(ctx.expression());
                return ret(expression);
            }

            @Override
            public StatementDom visitVariableDeclaration(@NotNull JavaParser.VariableDeclarationContext ctx) {
                String type = ctx.type.getText();
                String name = ctx.name.getText();

                StatementDom statement = declareVar(type, name);

                if(ctx.value != null) {
                    ExpressionDom value = parseExpression(ctx.value);
                    statement = block(Arrays.asList(statement, assignVar(name, value)));
                }

                return statement;
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
}
