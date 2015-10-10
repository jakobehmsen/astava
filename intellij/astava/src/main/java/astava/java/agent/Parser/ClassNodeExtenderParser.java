package astava.java.agent.Parser;

import astava.java.DomFactory;
import astava.java.agent.ClassNodeExtender;
import astava.java.agent.ClassNodeExtenderFactory;
import astava.java.agent.MethodNodeExtenderFactory;
import astava.java.agent.SequenceClassNodeExtender;
import astava.java.parser.*;
import astava.tree.FieldDom;
import astava.tree.MethodDom;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;

public class ClassNodeExtenderParser implements ClassNodeExtender {
    private ClassResolver classResolver;
    private ClassInspector classInspector;
    private ArrayList<DomBuilder> builders = new ArrayList<>();

    public ClassNodeExtenderParser(ClassResolver classResolver, ClassInspector classInspector) {
        this.classResolver = classResolver;
        this.classInspector = classInspector;
    }

    public void extend(String sourceCode) {
        try {
            new Parser(sourceCode).parse().forEach(d -> {
                builders.add(d);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void transform(ClassNode classNode) {
        MutableClassDomBuilder thisBuilder = new MutableClassDomBuilder();

        builders.stream().forEach(d -> d.accept(new DomBuilderVisitor() {
            @Override
            public void visitClassBuilder(ClassDomBuilder classBuilder) {

            }

            @Override
            public void visitExpressionBuilder(ExpressionDomBuilder expressionBuilder) {

            }

            @Override
            public void visitFieldBuilder(FieldDomBuilder fieldBuilder) {
                thisBuilder.addField(fieldBuilder);
            }

            @Override
            public void visitMethodBuilder(MethodDomBuilder methodBuilder) {
                thisBuilder.addMethod(methodBuilder);
            }

            @Override
            public void visitStatementBuilder(StatementDomBuilder statementBuilder) {

            }

            @Override
            public void visitInitializer(StatementDomBuilder statement) {

            }
        }));

        ClassDeclaration thisClass = thisBuilder.build(classResolver);

        builders.stream().map(d -> {
            return new DomBuilderVisitor.Return<ClassNodeExtender>() {
                @Override
                public void visitClassBuilder(ClassDomBuilder classBuilder) {

                }

                @Override
                public void visitExpressionBuilder(ExpressionDomBuilder expressionBuilder) {

                }

                @Override
                public void visitFieldBuilder(FieldDomBuilder fieldBuilder) {
                    FieldDom fieldDom = fieldBuilder.declare(classResolver).build(thisClass);
                    setResult(ClassNodeExtenderFactory.addField(fieldDom));
                }

                @Override
                public void visitMethodBuilder(MethodDomBuilder methodBuilder) {
                    MethodDom methodDom = methodBuilder.declare(classResolver).build(thisClass, classInspector);
                    setResult(ClassNodeExtenderFactory.addMethod(methodDom));
                }

                @Override
                public void visitStatementBuilder(StatementDomBuilder statementBuilder) {

                }

                @Override
                public void visitInitializer(StatementDomBuilder statement) {
                    setResult(MethodNodeExtenderFactory.setBody(DomFactory.block(Arrays.asList(
                        // How to add initialization after method body? Method body seems to return
                        statement.build(classResolver, thisClass, classInspector, new Hashtable<>()),
                        DomFactory.methodBody()
                    ))).when((c, m) -> m.name.equals("<init>")));
                }
            }.visit(d);
        }).forEach(x -> x.transform(classNode));
    }
}
