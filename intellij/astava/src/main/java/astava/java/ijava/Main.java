package astava.java.ijava;

import astava.java.Descriptor;
import astava.java.gen.ClassGenerator;
import astava.java.gen.SingleClassLoader;
import astava.java.ijava.server.RequestCode;
import astava.java.parser.*;
import astava.tree.*;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
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
    private static IJAVAClassLoader ijavaClassLoader;
    private static Object currentRoot = null;
    private static int startIndex = 0;

    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Process serverProcess;

    private static DataInputStream inputStream;
    private static DataOutputStream outputStream;

    public static void main(String[] args) throws IOException {
        String serverFilePath = new java.io.File("classes/artifacts/ijava_server_jar/astava.jar").getAbsolutePath();
        String javaAgentFilePath = new java.io.File("classes/artifacts/ijava_agent_jar/astava.jar").getAbsolutePath();
        serverProcess = new ProcessBuilder(
            "java",
            //"-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005",
            "-javaagent:" + javaAgentFilePath,

            "-cp",
            serverFilePath, "astava.java.ijava.server.Main"
            ).start();

        inputStream = new DataInputStream(serverProcess.getInputStream());
        //Scanner input = new Scanner(serverProcess.getInputStream());
        outputStream = new DataOutputStream(serverProcess.getOutputStream());

        // Interact with agent premain; send map of class builder to be used for instrumentation
        ObjectOutputStream agentObjectOutputStream = new ObjectOutputStream(serverProcess.getOutputStream());
        agentObjectOutputStream.writeObject(new Hashtable<String, ClassDomBuilder>());
        outputStream.flush();

        /*
        outputStream.writeInt(RequestCode.EXEC);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(serverProcess.getOutputStream());
        objectOutputStream.writeObject(astava.java.parser.Factory.ret(astava.java.parser.Factory.literal(5)));
        outputStream.flush();
        String resultString = inputStream.readUTF();
        */

        /*
        outputStream.writeInt(RequestCode.END);
        outputStream.flush();
        int responseCode = inputStream.read();
        */


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
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    outputStream.writeInt(RequestCode.END);
                    outputStream.flush();
                    int responseCode = inputStream.read();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

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

            MutableClassDomBuilder initialRootClass = null;

            try {
                initialRootClass = new Parser("public class Root { }").parseClass();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ijavaClassLoader = new IJAVAClassLoader(baseClassResolver);

            ijavaClassLoader.putClassBuilder("Root", initialRootClass);
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
                                    ijavaClassLoader.putClassBuilder(classBuilder.getName(), classBuilder);
                                    resetClassLoader(executions);
                                }

                                @Override
                                public void visitExpressionBuilder(ExpressionDomBuilder expressionBuilder) {
                                    Object result = exec(astava.java.parser.Factory.ret(expressionBuilder), ijavaClassLoader, executions);
                                    output.append("\n" + result);
                                }

                                @Override
                                public void visitFieldBuilder(FieldDomBuilder fieldBuilder) {
                                    try {
                                        outputStream.writeInt(RequestCode.DECLARE);
                                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(serverProcess.getOutputStream());
                                        objectOutputStream.writeObject(fieldBuilder);
                                        outputStream.flush();
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                }

                                @Override
                                public void visitMethodBuilder(MethodDomBuilder methodBuilder) {

                                }

                                @Override
                                public void visitStatementBuilder(StatementDomBuilder statementBuilder) {
                                    exec(astava.java.parser.Factory.block(Arrays.asList(
                                        statementBuilder, astava.java.parser.Factory.ret()
                                    )), ijavaClassLoader, executions);
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

    private static void resetClassLoader(List<StatementDomBuilder> executions) {
        ijavaClassLoader = ijavaClassLoader.reset();

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
        executions.forEach(x -> exec(x, ijavaClassLoader));
    }

    private static Object exec(StatementDomBuilder statementDomBuilder, ClassResolver classResolver, List<StatementDomBuilder> script) {
        script.add(statementDomBuilder);

        return exec(statementDomBuilder, classResolver);
    }

    private static Object exec(StatementDomBuilder statementDomBuilder, ClassResolver classResolver) {
        try {
            outputStream.writeInt(RequestCode.EXEC);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(serverProcess.getOutputStream());
            objectOutputStream.writeObject(statementDomBuilder);
            outputStream.flush();
            String resultString = inputStream.readUTF();

            if(1 != 2)
                return resultString;
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(1 != 2)
            return null;








        try {
            serverProcess.getOutputStream().write(RequestCode.EXEC);
            ObjectOutputStream ooStream = new ObjectOutputStream(serverProcess.getOutputStream());
            ooStream.writeObject(statementDomBuilder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        MutableClassDomBuilder exeClassBuilder = null;

        try {
            exeClassBuilder = new Parser("public class Exec { }").parseClass();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Add entry point method

        ClassDeclaration rootClassDeclaration = ijavaClassLoader.getClassDeclaration("Root");

        Hashtable<String, String> locals = new Hashtable<>();
        locals.put("self", rootClassDeclaration.getName());
        StatementDom stmt = statementDomBuilder.build(classResolver, rootClassDeclaration, ijavaClassLoader, locals);

        String exprResultType = Parser.statementReturnType(ijavaClassLoader, rootClassDeclaration, stmt, locals);

        exeClassBuilder.addMethod(new MethodDomBuilder() {
            @Override
            public String getName() {
                return "exec";
            }

            @Override
            public MethodDeclaration declare(ClassResolver classResolver) {
                return new MethodDeclaration() {
                    @Override
                    public int getModifier() {
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
                        return methodDeclaration(getModifier(), getName(), getParameterTypes(), getReturnTypeName(), block(Arrays.asList(
                            stmt
                        )));
                    }
                };
            }
        });

        ClassDeclaration execClassDeclaration = exeClassBuilder.build(classResolver);

        ClassDom classDom = execClassDeclaration.build(ijavaClassLoader);

        ClassGenerator generator = new ClassGenerator(classDom);

        ClassLoader classLoader = new SingleClassLoader(ijavaClassLoader, generator);

        try {
            Class<?> execClass = classLoader.loadClass("Exec");
            Class<?> rootClass = classLoader.loadClass("Root");

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
