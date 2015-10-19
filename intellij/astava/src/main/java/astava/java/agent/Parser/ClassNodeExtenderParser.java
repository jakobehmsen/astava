package astava.java.agent.Parser;

import astava.java.Descriptor;
import astava.java.DomFactory;
import astava.java.agent.ClassNodeExtender;
import astava.java.agent.ClassNodeExtenderFactory;
import astava.java.agent.MethodNodeExtenderFactory;
import astava.java.parser.*;
import astava.tree.FieldDom;
import astava.tree.MethodDom;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class ClassNodeExtenderParser implements ClassNodeExtender {
    private ClassResolver classResolver;
    private ClassInspector classInspector;
    private ArrayList<DomBuilder> builders = new ArrayList<>();

    public ClassNodeExtenderParser(ClassResolver classResolver, ClassInspector classInspector) {
        this.classResolver = classResolver;
        this.classInspector = classInspector;
    }

    public void extend(String sourceCode, Function<ClassDeclaration, String>... arguments) {

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
        MutableClassDeclaration thisClass = new MutableClassDeclaration();

        thisClass.setName(classNode.name);
        thisClass.setSuperName(classNode.superName);

        // Include all fields and methods (members in general) of classNode
        ((List<String>)classNode.interfaces).forEach(x -> thisClass.addInterface(Descriptor.getName(x)));
        ASMClassDeclaration.getFields(classNode).forEach(x -> thisClass.addField(x));
        ASMClassDeclaration.getMethods(classNode).forEach(x -> thisClass.addMethod(x));

        builders.stream().forEach(d -> d.accept(new DomBuilderVisitor() {
            @Override
            public void visitClassBuilder(ClassDomBuilder classBuilder) {

            }

            @Override
            public void visitExpressionBuilder(ExpressionDomBuilder expressionBuilder) {

            }

            @Override
            public void visitFieldBuilder(FieldDomBuilder fieldBuilder) {
                thisClass.addField(fieldBuilder.declare(classResolver));
            }

            @Override
            public void visitMethodBuilder(MethodDomBuilder methodBuilder) {
                thisClass.addMethod(methodBuilder.declare(classResolver));
            }

            @Override
            public void visitStatementBuilder(StatementDomBuilder statementBuilder) {

            }

            @Override
            public void visitInitializer(StatementDomBuilder statement) {

            }

            @Override
            public void visitAnnotation(String typeName, Map<String, Object> values) {

            }
        }));

        ClassInspector classInspector = new ClassInspector() {
            @Override
            public ClassDeclaration getClassDeclaration(String name) {
                if(Descriptor.get(name).equals(thisClass.getName()))
                    return thisClass;
                return ClassNodeExtenderParser.this.classInspector.getClassDeclaration(name);
            }
        };

        // The class inspector should probably be decorated such that when the name of thisClass is
        // requested, then thisClass is returned

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
                    //setResult(MethodNodeExtenderFactory.setBody(DomFactory.block(Arrays.asList(
                    setResult(MethodNodeExtenderFactory.append(DomFactory.block(Arrays.asList(
                        // How to add initialization after method body? Method body seems to return
                        statement.build(classResolver, thisClass, classInspector, new Hashtable<>())/*,
                        DomFactory.methodBody()*/
                    ))).when((c, m) -> m.name.equals("<init>")));
                }

                @Override
                public void visitAnnotation(String typeName, Map<String, Object> values) {
                    setResult(ClassNodeExtenderFactory.addAnnotation(Descriptor.get(typeName), values));
                }
            }.visit(d);
        }).forEach(x ->
            x.transform(classNode));
    }
}
