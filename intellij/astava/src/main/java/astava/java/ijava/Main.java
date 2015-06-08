package astava.java.ijava;

import astava.java.Descriptor;
import astava.java.gen.ClassGenerator;
import astava.java.gen.SingleClassLoader;
import astava.java.parser.*;
import astava.tree.*;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static astava.java.Factory.*;

public class Main {
    private static MutableClassDomBuilder rootClassBuilder;
    private static IJAVAClassLoader ijavaClassLoader;
    private static Object currentRoot = null;
    private static int startIndex = 0;

    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void main(String[] args) throws IOException {
        ClassResolver baseClassResolver = new ClassResolver() {
            private Map<String, String> simpleNameToNameMap = Arrays.asList(
                String.class,
                Modifier.class,
                Object.class
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

        Color bgColor = Color.BLACK;
        Color fgColor = Color.WHITE;

        String shellPrefix = "> ";

        pendingScript.setDocument(new DefaultStyledDocument() {
            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                if (offs >= startIndex)
                    super.insertString(offs, str, a);
            }

            @Override
            public void remove(int offs, int len) throws BadLocationException {
                if (offs >= startIndex)
                    super.remove(offs, len);
            }
        });

        try {
            pendingScript.getDocument().insertString(0, shellPrefix, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        startIndex = pendingScript.getDocument().getLength();
        pendingScript.setCaretPosition(startIndex);

        executor.execute(() -> {
            pendingScript.setBackground(bgColor);
            pendingScript.setForeground(fgColor);
            pendingScript.setCaretColor(fgColor);
            pendingScript.setSelectionColor(fgColor);

            pendingScript.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));

            try {
                rootClassBuilder = new Parser("public class Root { }").parseClass();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ijavaClassLoader = new IJAVAClassLoader(baseClassResolver);

            ijavaClassLoader.putClassBuilder("Root", new ClassDomBuilder() {
                @Override
                public String getName() {
                    return "Root";
                }

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
        });
        ArrayList<StatementDomBuilder> executions = new ArrayList<>();

        pendingScript.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    executor.execute(() -> {
                        String code = null;
                        try {
                            code = pendingScript.getDocument().getText(startIndex, pendingScript.getDocument().getLength() - startIndex);
                        } catch (BadLocationException e1) {
                            e1.printStackTrace();
                        }

                        StringBuilder output = new StringBuilder();
                        InputStream inputStream = new ByteArrayInputStream(code.getBytes());

                        try {
                            java.util.List<DomBuilder> script = new Parser(inputStream).parse();

                            script.forEach(x -> x.accept(new DomBuilderVisitor() {
                                @Override
                                public void visitClassBuilder(ClassDomBuilder classBuilder) {
                                    ijavaClassLoader.putClassBuilder(classBuilder.getName(), new ClassDomBuilder() {
                                        @Override
                                        public ClassDeclaration build(ClassResolver classResolver) {
                                            return classBuilder.build(classResolver).withDefaultConstructor();
                                        }

                                        @Override
                                        public String getName() {
                                            return classBuilder.getName();
                                        }
                                    });

                                    resetClassLoader(baseClassResolver, rootClassBuilder, executions);
                                }

                                @Override
                                public void visitExpressionBuilder(ExpressionDomBuilder expressionBuilder) {
                                    Object result = exec(new StatementDomBuilder() {
                                        @Override
                                        public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals) {
                                            return ret(expressionBuilder.build(classResolver, classDeclaration, classInspector, locals));
                                        }
                                    }, ijavaClassLoader, rootClassBuilder, executions);
                                    output.append("\n" + result);
                                }

                                @Override
                                public void visitFieldBuilder(FieldDomBuilder fieldBuilder) {
                                    rootClassBuilder.addField(fieldBuilder);

                                    resetClassLoader(baseClassResolver, rootClassBuilder, executions);
                                }

                                @Override
                                public void visitMethodBuilder(MethodDomBuilder methodBuilder) {

                                }

                                @Override
                                public void visitStatementBuilder(StatementDomBuilder statementBuilder) {
                                    exec(new StatementDomBuilder() {
                                        @Override
                                        public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals) {
                                            return block(Arrays.asList(statementBuilder.build(classResolver, classDeclaration, classInspector, locals), ret()));
                                        }
                                    }, ijavaClassLoader, rootClassBuilder, executions);
                                }
                            }));
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                        // Can the code be parsed? Then run it.
                        try {
                            pendingScript.getDocument().insertString(pendingScript.getDocument().getLength(), output + "\n" + shellPrefix, null);
                        } catch (BadLocationException e1) {
                            e1.printStackTrace();
                        }

                        startIndex = pendingScript.getDocument().getLength();
                        pendingScript.setCaretPosition(startIndex);
                    });
                }
            }
        });

        frame.getContentPane().add(new JScrollPane(pendingScript), BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1028, 768);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void resetClassLoader(ClassResolver baseClassResolver, final MutableClassDomBuilder rootClassBuilder, java.util.List<StatementDomBuilder> executions) {
        Map<String, ClassDomBuilder> classBuilders = ijavaClassLoader.getClassBuilders();

        ijavaClassLoader = new IJAVAClassLoader(baseClassResolver);

        classBuilders.entrySet().stream().forEach(x -> {
            if(x.getKey().equals("Root")) {
                ijavaClassLoader.putClassBuilder("Root", new ClassDomBuilder() {
                    @Override
                    public ClassDeclaration build(ClassResolver classResolver) {
                        return rootClassBuilder.build(classResolver).withDefaultConstructor();
                    }

                    @Override
                    public String getName() {
                        return "Root";
                    }
                });
            } else
                ijavaClassLoader.putClassBuilder(x.getKey(), x.getValue());
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
        executions.forEach(x -> exec(x, ijavaClassLoader, rootClassBuilder));
    }

    private static Map<String, ClassDomBuilder> classBuilders = new Hashtable<>();
    private static Object exec(StatementDomBuilder statementDomBuilder, ClassResolver classResolver, MutableClassDomBuilder rootClassBuilder, java.util.List<StatementDomBuilder> script) {
        script.add(statementDomBuilder);

        return exec(statementDomBuilder, classResolver, rootClassBuilder);
    }

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

        Hashtable<String, String> locals = new Hashtable<>();
        locals.put("self", rootClassDeclaration.getName());
        StatementDom stmt = statementDomBuilder.build(classResolver, rootClassDeclaration, ijavaClassLoader, locals);

        String exprResultType = Parser.statementReturnType(ijavaClassLoader, rootClassDeclaration, stmt, locals);

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
                    public MethodDom build(ClassDeclaration classDeclaration, ClassInspector classInspector) {
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

        ClassDom classDom = execClassDeclaration.build(ijavaClassLoader);

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

}
