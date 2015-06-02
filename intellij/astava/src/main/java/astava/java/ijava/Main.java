package astava.java.ijava;

import astava.java.Descriptor;
import astava.java.gen.ClassGenerator;
import astava.java.parser.*;
import astava.tree.*;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static astava.java.Factory.*;

public class Main {
    private static Object currentRoot = null;

    public static void main(String[] args) throws IOException {

        ClassResolver classResolver = new ClassResolver() {
            private Map<String, String> simpleNameToNameMap = Arrays.asList(
                String.class,
                Modifier.class
            )
                .stream().map(x -> new AbstractMap.SimpleImmutableEntry<>(x.getSimpleName(), x.getName()))
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
            private Set<String> nameSet = simpleNameToNameMap.values().stream().collect(Collectors.toSet());

            @Override
            public boolean canResolveAmbiguous(String className) {
                return nameSet.contains(className);
            }

            @Override
            public String resolveSimpleName(String className) {
                return simpleNameToNameMap.get(className);
            }
        };

        JFrame frame = new JFrame();
        frame.setTitle("IJAVA");

        JTextPane pendingScript = new JTextPane();
        JTextPane historyScript = new JTextPane();

        //MutableClassDomBuilder rootClassBuilder = new MutableClassDomBuilder();

        MutableClassDomBuilder rootClassBuilder = new Parser("public class Root { }").parseClass();

        pendingScript.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String code = pendingScript.getText();

                    StringBuilder output = new StringBuilder(code + "\n=>\n");
                    InputStream inputStream = new ByteArrayInputStream(code.getBytes());

                    try {
                        java.util.List<DomBuilder> script = new Parser(inputStream).parse();

                        script.forEach(x -> x.accept(new DomBuilderVisitor() {
                            @Override
                            public void visitClassBuilder(ClassDomBuilder classBuilder) {

                            }

                            @Override
                            public void visitExpressionBuilder(ExpressionDomBuilder expressionBuilder) {
                                ClassDeclaration rootClassDeclaration = rootClassBuilder.build(classResolver);

                                rootClassDeclaration = rootClassDeclaration.withDefaultConstructor();

                                // Add entry point method
                                ExpressionDom expr = expressionBuilder.build(classResolver, rootClassDeclaration, new HashSet<>());

                                String exprResultType = expressionResultType(rootClassDeclaration, expr);

                                rootClassDeclaration = new ClassDeclaration.Mod(rootClassDeclaration) {
                                    @Override
                                    protected List<MethodDeclaration> newMethods() {
                                        return Arrays.asList(new MethodDeclaration() {
                                            @Override
                                            public int getModifiers() {
                                                return Modifier.PUBLIC;
                                            }

                                            @Override
                                            public String getName() {
                                                return "exec";
                                            }

                                            @Override
                                            public List<ParameterInfo> getParameterTypes() {
                                                return Arrays.asList();
                                            }

                                            @Override
                                            public String getReturnTypeName() {
                                                return exprResultType;
                                            }

                                            @Override
                                            public MethodDom build(ClassDeclaration classDeclaration) {
                                                return methodDeclaration(Modifier.PUBLIC, "exec", Arrays.asList(), exprResultType, block(Arrays.asList(
                                                    ret(expr)
                                                )));
                                            }
                                        });
                                    }
                                };

                                ClassDom classDom = rootClassDeclaration.build();

                                ClassGenerator generator = new ClassGenerator(classDom);

                                try {
                                    Class<?> gc = generator.newClass();
                                    Object oldRoot = currentRoot;
                                    currentRoot = gc.newInstance();

                                    if(oldRoot != null) {
                                        for(Field f: oldRoot.getClass().getDeclaredFields()) {
                                            f.setAccessible(true);
                                            Field nf = gc.getDeclaredField(f.getName());
                                            nf.setAccessible(true);
                                            nf.set(currentRoot, f.get(oldRoot));
                                        }
                                    }

                                    Method execMethod = gc.getMethod("exec");
                                    Object result = execMethod.invoke(currentRoot);

                                    output.append(result);
                                } catch (ClassNotFoundException e1) {
                                    e1.printStackTrace();
                                } catch (InstantiationException e1) {
                                    e1.printStackTrace();
                                } catch (IllegalAccessException e1) {
                                    e1.printStackTrace();
                                } catch (NoSuchFieldException e1) {
                                    e1.printStackTrace();
                                } catch (NoSuchMethodException e1) {
                                    e1.printStackTrace();
                                } catch (InvocationTargetException e1) {
                                    e1.printStackTrace();
                                }
                            }

                            @Override
                            public void visitFieldBuilder(FieldDomBuilder fieldBuilder) {
                                rootClassBuilder.addField(fieldBuilder);
                            }

                            @Override
                            public void visitMethodBuilder(MethodDomBuilder methodBuilder) {

                            }

                            @Override
                            public void visitStatementBuilder(StatementDomBuilder statementBuilder) {
                                ClassDeclaration rootClassDeclaration = rootClassBuilder.build(classResolver);







                                rootClassDeclaration = rootClassDeclaration.withDefaultConstructor();

                                // Add entry point method
                                StatementDom stmt = statementBuilder.build(classResolver, rootClassDeclaration, new HashSet<>());

                                rootClassDeclaration = new ClassDeclaration.Mod(rootClassDeclaration) {
                                    @Override
                                    protected List<MethodDeclaration> newMethods() {
                                        return Arrays.asList(new MethodDeclaration() {
                                            @Override
                                            public int getModifiers() {
                                                return Modifier.PUBLIC;
                                            }

                                            @Override
                                            public String getName() {
                                                return "exec";
                                            }

                                            @Override
                                            public List<ParameterInfo> getParameterTypes() {
                                                return Arrays.asList();
                                            }

                                            @Override
                                            public String getReturnTypeName() {
                                                return Descriptor.VOID;
                                            }

                                            @Override
                                            public MethodDom build(ClassDeclaration classDeclaration) {
                                                return methodDeclaration(Modifier.PUBLIC, "exec", Arrays.asList(), Descriptor.VOID, block(Arrays.asList(
                                                    stmt,
                                                    ret()
                                                )));
                                            }
                                        });
                                    }
                                };

                                ClassDom classDom = rootClassDeclaration.build();

                                ClassGenerator generator = new ClassGenerator(classDom);

                                try {
                                    Class<?> gc = generator.newClass();
                                    Object oldRoot = currentRoot;
                                    currentRoot = gc.newInstance();

                                    if(oldRoot != null) {
                                        for(Field f: oldRoot.getClass().getDeclaredFields()) {
                                            f.setAccessible(true);
                                            Field nf = gc.getDeclaredField(f.getName());
                                            nf.setAccessible(true);
                                            nf.set(currentRoot, f.get(oldRoot));
                                        }
                                    }

                                    Method execMethod = gc.getMethod("exec");
                                    execMethod.invoke(currentRoot);

                                    //output.append("");
                                } catch (ClassNotFoundException e1) {
                                    e1.printStackTrace();
                                } catch (InstantiationException e1) {
                                    e1.printStackTrace();
                                } catch (IllegalAccessException e1) {
                                    e1.printStackTrace();
                                } catch (NoSuchFieldException e1) {
                                    e1.printStackTrace();
                                } catch (NoSuchMethodException e1) {
                                    e1.printStackTrace();
                                } catch (InvocationTargetException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    // Can the code be parsed? Then run it.
                    try {
                        historyScript.getDocument().insertString(0, output + "\n", null);
                    } catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }

                    pendingScript.setText("");
                }
            }
        });

        frame.getContentPane().add(pendingScript, BorderLayout.NORTH);
        frame.getContentPane().add(historyScript, BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1028, 768);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static String expressionResultType(ClassDeclaration self, ExpressionDom expr) {
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

            }

            @Override
            public void visitNewInstance(String type, List<String> parameterTypes, List<ExpressionDom> arguments) {

            }

            @Override
            public void visitThis() {
                setResult(Descriptor.get(self.getName()));
            }
        }.returnFrom(expr);
    }
}
