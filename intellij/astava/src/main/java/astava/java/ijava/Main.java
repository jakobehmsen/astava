package astava.java.ijava;

import astava.java.Descriptor;
import astava.java.gen.ClassGenerator;
import astava.java.gen.SingleClassLoader;
import astava.java.parser.*;
import astava.java.parser.antlr4.JavaParser;
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
        MutableClassDomBuilder scriptClassBuilder = new Parser("public class Script { }").parseClass();

        ijavaClassLoader = new IJAVAClassLoader(classResolver);

        ijavaClassLoader.putClassBuilder("Root", new ClassDomBuilder() {
            @Override
            public ClassDeclaration build(ClassResolver classResolver) {
                return rootClassBuilder.build(classResolver).withDefaultConstructor();
            }
        });
        try {
            currentRoot = ijavaClassLoader.loadClass("Root").newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        ArrayList<StatementDomBuilder> executions = new ArrayList<>();

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
                                Object result = exec(new StatementDomBuilder() {
                                    @Override
                                    public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, Set<String> locals) {
                                        return ret(expressionBuilder.build(classResolver, classDeclaration, locals));
                                    }
                                }, classResolver, rootClassBuilder, executions);
                                output.append(result);
                            }

                            @Override
                            public void visitFieldBuilder(FieldDomBuilder fieldBuilder) {
                                rootClassBuilder.addField(fieldBuilder);

                                ijavaClassLoader = new IJAVAClassLoader(classResolver);

                                ijavaClassLoader.putClassBuilder("Root", new ClassDomBuilder() {
                                    @Override
                                    public ClassDeclaration build(ClassResolver classResolver) {
                                        return rootClassBuilder.build(classResolver).withDefaultConstructor();
                                    }
                                });

                                try {
                                    currentRoot = ijavaClassLoader.loadClass("Root").newInstance();
                                } catch (ClassNotFoundException e1) {
                                    e1.printStackTrace();
                                } catch (InstantiationException e1) {
                                    e1.printStackTrace();
                                } catch (IllegalAccessException e1) {
                                    e1.printStackTrace();
                                }

                                // Replay all script
                                executions.forEach(x -> exec(x, classResolver, rootClassBuilder));
                            }

                            @Override
                            public void visitMethodBuilder(MethodDomBuilder methodBuilder) {

                            }

                            @Override
                            public void visitStatementBuilder(StatementDomBuilder statementBuilder) {
                                exec(new StatementDomBuilder() {
                                    @Override
                                    public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, Set<String> locals) {
                                        return block(Arrays.asList(statementBuilder.build(classResolver, classDeclaration, locals), ret()));
                                    }
                                }, classResolver, rootClassBuilder, executions);
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

    private static Map<String, ClassDomBuilder> classBuilders = new Hashtable<>();
    private static Object exec(StatementDomBuilder statementDomBuilder, ClassResolver classResolver, MutableClassDomBuilder rootClassBuilder, java.util.List<StatementDomBuilder> script) {
        script.add(statementDomBuilder);

        return exec(statementDomBuilder, classResolver, rootClassBuilder);
    }

    private static IJAVAClassLoader ijavaClassLoader;

    private static Object exec(StatementDomBuilder statementDomBuilder, ClassResolver classResolver, MutableClassDomBuilder rootClassBuilder) {
        MutableClassDomBuilder exeClassBuilder = null;

        try {
            exeClassBuilder = new Parser("public class Exec { }").parseClass();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //ClassDeclaration rootClassDeclaration = rootClassBuilder.build(classResolver);

        //rootClassDeclaration = rootClassDeclaration.withDefaultConstructor();

        // Add entry point method

        ClassDeclaration rootClassDeclaration = ijavaClassLoader.getClassDeclaration("Root");

        StatementDom stmt = statementDomBuilder.build(classResolver, rootClassDeclaration, new HashSet<>());

        String exprResultType = statementReturnType(rootClassDeclaration, stmt);

        exeClassBuilder.addMethod(new MethodDomBuilder() {
            @Override
            public MethodDeclaration declare(ClassResolver classResolver) {
                return new MethodDeclaration() {
                    @Override
                    public int getModifiers() {
                        return Modifier.PUBLIC | Modifier.STATIC;
                    }

                    @Override
                    public String getName() {
                        return "exec";
                    }

                    @Override
                    public List<ParameterInfo> getParameterTypes() {
                        return Arrays.asList(new ParameterInfo(Descriptor.get("Root"), "self"));
                    }

                    @Override
                    public String getReturnTypeName() {
                        return exprResultType;
                    }

                    @Override
                    public MethodDom build(ClassDeclaration classDeclaration) {
                        return methodDeclaration(getModifiers(), getName(), getParameterTypes(), getReturnTypeName(), block(Arrays.asList(
                            stmt
                        )));
                    }
                };
            }
        });

        ClassDeclaration execClassDeclaration = exeClassBuilder.build(classResolver);




        /*rootClassDeclaration = new ClassDeclaration.Mod(rootClassDeclaration) {
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
                            stmt
                        )));
                    }
                });
            }
        };*/

        //ClassDom classDom = rootClassDeclaration.build();
        Map<String, ClassDomBuilder> classBuilders2 = new Hashtable<>();
        classBuilders2.putAll(classBuilders);
        classBuilders2.put("Exec", exeClassBuilder);

        ClassDom classDom = execClassDeclaration.build();

        ClassGenerator generator = new ClassGenerator(classDom);

        ClassLoader classLoader = new SingleClassLoader(ijavaClassLoader, generator);

        try {
            Class<?> execClass = classLoader.loadClass("Exec");
            Class<?> rootClass = classLoader.loadClass("Root");

            /*Class<?> gc = generator.newClass();
            Object oldRoot = currentRoot;
            currentRoot = gc.newInstance();

            if(oldRoot != null) {
                for(Field f: oldRoot.getClass().getDeclaredFields()) {
                    f.setAccessible(true);
                    Field nf = gc.getDeclaredField(f.getName());
                    nf.setAccessible(true);
                    nf.set(currentRoot, f.get(oldRoot));
                }
            }*/

            Method execMethod = execClass.getMethod("exec", new Class<?>[]{rootClass});
            return execMethod.invoke(null, currentRoot);
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
        }

        return null;
    }

    private static String statementReturnType(ClassDeclaration self, StatementDom stmt) {
        String returnType = new StatementDomVisitor.Return<String>() {
            @Override
            public void visitVariableDeclaration(String type, String name) {

            }

            @Override
            public void visitVariableAssignment(String name, ExpressionDom value) {

            }

            @Override
            public void visitFieldAssignment(ExpressionDom target, String name, ExpressionDom value) {

            }

            @Override
            public void visitStaticFieldAssignment(String typeName, String name, ExpressionDom value) {

            }

            @Override
            public void visitIncrement(String name, int amount) {

            }

            @Override
            public void visitReturnValue(ExpressionDom expression) {
                String resultType = expressionResultType(self, expression);
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
