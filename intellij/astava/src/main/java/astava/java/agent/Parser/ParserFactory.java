package astava.java.agent.Parser;

import astava.java.Descriptor;
import astava.java.DomFactory;
import astava.java.agent.*;
import astava.java.parser.*;
import astava.tree.FieldDom;
import astava.tree.MethodDom;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ParserFactory {
    private ClassResolver classResolver;
    private ClassInspector classInspector;

    public ParserFactory(ClassResolver classResolver, ClassInspector classInspector) {
        this.classResolver = classResolver;
        this.classInspector = classInspector;
    }

    /*public ClassNodeExtenderParser newExtender() {
        return new ClassNodeExtenderParser(classResolver, classInspector);
    }

    public ClassNodePredicateParser newPredicate() {
        return new ClassNodePredicateParser(classInspector);
    }

    public MethodNodePredicateParser newMethodPredicate() {
        return new MethodNodePredicateParser();
    }*/

    public ExDeclaringClassNodeExtenderElement modClass(String sourceCode) throws IOException {
        List<ExDeclaringClassNodeExtenderElement> elements = new Parser(sourceCode).parse().stream().map(d -> new ExDeclaringClassNodeExtenderElement() {
            @Override
            public ExDeclaringClassNodeExtenderTransformer declare(ClassNode classNode, MutableClassDeclaration thisClass, ClassResolver classResolver) {
                d.accept(new DefaultDomBuilderVisitor() {
                    @Override
                    public void visitFieldBuilder(FieldDomBuilder fieldBuilder) {
                        thisClass.addField(fieldBuilder.declare(classResolver));
                    }

                    @Override
                    public void visitMethodBuilder(MethodDomBuilder methodBuilder) {
                        thisClass.addMethod(methodBuilder.declare(classResolver));
                    }
                });

                return (classNode1, thisClass1, classResolver1, classInspector1) -> new DefaultDomBuilderVisitor.Return<ExDeclaringClassNodeExtenderTransformer>() {
                    @Override
                    public void visitFieldBuilder(FieldDomBuilder fieldBuilder) {
                        FieldDom fieldDom = fieldBuilder.declare(classResolver1).build(thisClass1);
                        setResult(ClassNodeExtenderFactory.addField(fieldDom));
                    }

                    @Override
                    public void visitMethodBuilder(MethodDomBuilder methodBuilder) {
                        MethodDom methodDom = methodBuilder.declare(classResolver1).build(thisClass1, classInspector1);
                        setResult(ClassNodeExtenderFactory.addMethod(methodDom));
                    }

                    @Override
                    public void visitInitializer(StatementDomBuilder statement) {
                        setResult(MethodNodeExtenderFactory.append(DomFactory.block(Arrays.asList(
                            // How to add initialization after method body? Method body seems to return
                            statement.build(classResolver1, thisClass1, classInspector1, new Hashtable<>())
                        ))).when((c, cr, ci, m) -> m.name.equals("<init>")));
                    }

                    @Override
                    public void visitAnnotation(String typeName, Map<String, Object> values) {
                        setResult(ClassNodeExtenderFactory.addAnnotation(Descriptor.get(typeName), values));
                    }
                }.visit(d).transform(classNode1, thisClass1, classResolver1, classInspector1);
            }
        }).collect(Collectors.toList());

        return ExDeclaringClassNodeExtenderUtil.composeElement(elements);
    }

    public ExDeclaringClassNodeExtenderElementPredicate whenClass(String sourceCode) throws IOException {
        List<ClassNodePredicate> predicates = new Parser(sourceCode).parseClassPredicates(classInspector);

        return (classNode, thisClass, classResolver1) -> predicates.stream().allMatch(p -> p.test(classNode));
    }

    public ExDeclaringClassNodeExtenderElement modClass(BiFunctionException<ClassNode, ClassDeclaration, ExDeclaringClassNodeExtenderElement> function) throws Exception {
        return (classNode, thisClass, classResolver1) -> {
            try {
                return function
                    .apply(classNode, thisClass)
                    .declare(classNode, thisClass, classResolver1);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        };
    }
}
