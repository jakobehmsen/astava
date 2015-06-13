package astava.java.ijava.server;

import astava.debug.Debug;
import astava.java.Descriptor;
import astava.java.gen.ClassGenerator;
import astava.java.gen.SingleClassLoader;
import astava.java.parser.*;
import astava.tree.*;
import javafx.stage.Screen;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static astava.java.Factory.fieldDeclaration;
import static astava.java.Factory.methodDeclaration;

public class Main {
    private static class Variable {
        public String typeName;
        public Object value;

        public Variable(String typeName, Object value) {
            this.typeName = typeName;
            this.value = value;
        }
    }

    private static JFrame frame;
    private static JTextPane console;

    public static void main(String[] args) {
        console = new JTextPane();
        DefaultCaret caret = (DefaultCaret)console.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);


        frame = new JFrame();
        frame.setSize(480, 800);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new JScrollPane(console), BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        //frame.setLocation((int)Screen.getPrimary().getBounds().getWidth() - frame.getWidth(), frame.getY());
        frame.setVisible(true);

        //Scanner inputScanner = new Scanner(System.in);
        //PrintStream output = System.out;
        DataInputStream input = new DataInputStream(System.in);
        Field outField = null;
        try {
            outField = FilterOutputStream.class.getDeclaredField("out");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        outField.setAccessible(true);

        OutputStream outputStream = null;
        try {
            outputStream = (OutputStream)outField.get(System.out);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        DataOutputStream output = new DataOutputStream(outputStream);

        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        }));

        /*JTextPane console = new JTextPane();
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        }));

        JFrame frame = new JFrame();
        frame.setSize(480, 800);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(console, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);*/

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        Hashtable<String, Variable> variables = new Hashtable<>();

        ClassResolver classResolver = new ClassResolver() {
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

        boolean run = true;
        while(run) {
            log("Waiting...");
            int operator = 0;
            try {
                operator = input.readInt();
            } catch (IOException e) {
                log("Error: " + e.getMessage());
            }

            switch (operator) {
                case RequestCode.DECLARE:
                    try {
                        ObjectInputStream oiStream = null;
                        try {
                            oiStream = new ObjectInputStream(input);
                        } catch (IOException e) {
                            log("Error: " + e.getMessage());
                        }
                        FieldDomBuilder fieldDomBuilder = null;
                        try {
                            fieldDomBuilder = (FieldDomBuilder)oiStream.readObject();
                        } catch (ClassNotFoundException e) {
                            log("Error: " + e.getMessage());
                        } catch (IOException e) {
                            log("Error: " + e.getMessage());
                        }

                        FieldDeclaration field = fieldDomBuilder.declare(classResolver);

                        String name = field.getName();
                        String typeName = field.getTypeName();
                        String descriptor = Descriptor.get(typeName);

                        log("Declare " + typeName + " " + name);

                        Object defaultValue = Descriptor.getDefaultValue(descriptor);

                        variables.put(name, new Variable(typeName, defaultValue));
                    } catch(Exception e) {
                        log("Error: " + e.getMessage());
                    }

                    break;
                case RequestCode.EXEC:
                    log("Exec");

                    /*MutableClassDomBuilder exeClassBuilder = null;

                    try {
                        exeClassBuilder = new Parser("public class Exec { }").parseClass();

                        for (Map.Entry<String, Variable> e : variables.entrySet()) {
                            exeClassBuilder.addField(new FieldDomBuilder() {
                                @Override
                                public FieldDeclaration declare(ClassResolver classResolver) {
                                    return new FieldDeclaration() {
                                        @Override
                                        public int getModifier() {
                                            return Modifier.PUBLIC;
                                        }

                                        @Override
                                        public String getTypeName() {
                                            return e.getValue().typeName;
                                        }

                                        @Override
                                        public String getName() {
                                            return e.getKey();
                                        }

                                        @Override
                                        public FieldDom build(ClassDeclaration classDeclaration) {
                                            String descriptor = Descriptor.get(e.getValue().typeName);
                                            return fieldDeclaration(Modifier.PUBLIC, getName(), descriptor);
                                        }
                                    };
                                }

                                @Override
                                public String getName() {
                                    return e.getKey();
                                }
                            });
                        }

                        ClassDeclaration exeClassBuilderDeclaration = exeClassBuilder.build(classResolver);
                        Hashtable<String, String> locals = new Hashtable<>();
                        StatementDom stmt = stmtBuilder.build(classResolver, exeClassBuilderDeclaration, classInspector, new Hashtable<>());
                        String exprResultType = Parser.statementReturnType(null, null, stmt, locals);

                        exeClassBuilder.addMethod(new MethodDomBuilder() {
                            @Override
                            public MethodDeclaration declare(ClassResolver classResolver) {
                                return new MethodDeclaration() {
                                    @Override
                                    public int getModifier() {
                                        return Modifier.PUBLIC;
                                    }

                                    @Override
                                    public String getName() {
                                        return "exec";
                                    }

                                    @Override
                                    public List<ParameterInfo> getParameterTypes() {
                                        return Collections.emptyList();
                                    }

                                    @Override
                                    public String getReturnTypeName() {
                                        return Descriptor.getName(exprResultType);
                                    }

                                    @Override
                                    public MethodDom build(ClassDeclaration classDeclaration, ClassInspector classInspector) {
                                        return methodDeclaration(Modifier.PUBLIC, getName(), getParameterTypes(), exprResultType, stmt);
                                    }
                                };
                            }

                            @Override
                            public String getName() {
                                return "exec";
                            }
                        });
                    } catch (IOException e) {
                        log("Error: " + e.getMessage());
                    }*/

                    String resultToString;

                    try {
                        ObjectInputStream oiStream = new ObjectInputStream(input);
                        StatementDomBuilder stmtBuilder = (StatementDomBuilder)oiStream.readObject();

                        log("stmt=" + stmtBuilder);

                        MutableClassDomBuilder exeClassBuilder = new Parser("public class Exec { }").parseClass();

                        for (Map.Entry<String, Variable> e : variables.entrySet()) {
                            exeClassBuilder.addField(new FieldDomBuilder() {
                                @Override
                                public FieldDeclaration declare(ClassResolver classResolver) {
                                    return new FieldDeclaration() {
                                        @Override
                                        public int getModifier() {
                                            return Modifier.PUBLIC;
                                        }

                                        @Override
                                        public String getTypeName() {
                                            return e.getValue().typeName;
                                        }

                                        @Override
                                        public String getName() {
                                            return e.getKey();
                                        }

                                        @Override
                                        public FieldDom build(ClassDeclaration classDeclaration) {
                                            String descriptor = Descriptor.get(e.getValue().typeName);
                                            return fieldDeclaration(Modifier.PUBLIC, getName(), descriptor);
                                        }
                                    };
                                }

                                @Override
                                public String getName() {
                                    return e.getKey();
                                }
                            });
                        }

                        ClassDeclaration exeClassBuilderDeclaration = exeClassBuilder.build(classResolver);

                        ClassInspector classInspector = new ClassInspector() {
                            @Override
                            public ClassDeclaration getClassDeclaration(String name) {
                                if(name.equals(exeClassBuilder.getName()))
                                    return exeClassBuilderDeclaration;

                                // Inspect virtual classes in class builders and physical classes in class loader
                                return null;
                            }
                        };

                        Hashtable<String, String> locals = new Hashtable<>();
                        StatementDom stmt = stmtBuilder.build(classResolver, exeClassBuilderDeclaration, classInspector, new Hashtable<>());
                        String exprResultType = Parser.statementReturnType(null, exeClassBuilderDeclaration, stmt, locals);

                        exeClassBuilder.addMethod(new MethodDomBuilder() {
                            @Override
                            public MethodDeclaration declare(ClassResolver classResolver) {
                                return new MethodDeclaration() {
                                    @Override
                                    public int getModifier() {
                                        return Modifier.PUBLIC;
                                    }

                                    @Override
                                    public String getName() {
                                        return "exec";
                                    }

                                    @Override
                                    public List<ParameterInfo> getParameterTypes() {
                                        return Collections.emptyList();
                                    }

                                    @Override
                                    public String getReturnTypeName() {
                                        return Descriptor.getName(exprResultType);
                                    }

                                    @Override
                                    public MethodDom build(ClassDeclaration classDeclaration, ClassInspector classInspector) {
                                        return methodDeclaration(Modifier.PUBLIC, getName(), getParameterTypes(), exprResultType, stmt);
                                    }
                                };
                            }

                            @Override
                            public String getName() {
                                return "exec";
                            }
                        });



                        ClassDeclaration execClassDeclaration = exeClassBuilder.build(classResolver).withDefaultConstructor();

                        ClassDom classDom = execClassDeclaration.build(classInspector);

                        ByteArrayOutputStream classGenerationOutputStream = new ByteArrayOutputStream();
                        PrintStream classGenerationPrintStream = new PrintStream(classGenerationOutputStream);
                        Debug.setPrintStream(classGenerationPrintStream);

                        ClassGenerator generator = new ClassGenerator(classDom);

                        ClassLoader exeClassLoader = new SingleClassLoader(classLoader, generator);

                        Class<?> execClass = exeClassLoader.loadClass("Exec");

                        String classGenerationOutput = new String(classGenerationOutputStream.toByteArray());

                        if(classGenerationOutput.length() > 0) {
                            log("Class generation:");
                            log(classGenerationOutput);
                        }

                        Object exec = execClass.newInstance();

                        for (Map.Entry<String, Variable> e : variables.entrySet()) {
                            Field f = execClass.getDeclaredField(e.getKey());
                            f.set(exec, e.getValue().value);
                        }


                        Method execMethod = execClass.getMethod("exec", new Class<?>[]{});

                        Object result = execMethod.invoke(exec);

                        for (Map.Entry<String, Variable> e : variables.entrySet()) {
                            Field f = execClass.getDeclaredField(e.getKey());
                            e.getValue().value = f.get(exec);
                        }

                        resultToString = result != null ? result.toString() : "null";

                        log("result = " + resultToString);
                        //outputStream.flush();

                        //output.write(ResponseCode.FINISHED);
                        //output.flush();

                        //System.out.println(resultToString);
                        //System.out.flush();
                    } catch (Exception e1) {
                        log("Error: " + e1.getMessage());

                        ByteArrayOutputStream stackTraceOutputStream = new ByteArrayOutputStream();
                        PrintStream stackTracePrintStream = new PrintStream(stackTraceOutputStream);
                        e1.printStackTrace(stackTracePrintStream);
                        log("Stack trace: " + new String(stackTraceOutputStream.toByteArray()));

                        resultToString = "Error: " + e1.getMessage();
                    }

                    try {
                        output.writeUTF(resultToString);
                        output.flush();
                    } catch (IOException e) {
                        log("Error when sending response: " + e.getMessage());
                    }

                    break;
                case RequestCode.END:
                    log("end");
                    run = false;
                    try {
                        output.writeInt(ResponseCode.FINISHED);
                        output.flush();
                    } catch (IOException e) {
                        log("Error: " + e.getMessage());
                    }
                    break;
            }
        }

        frame.setVisible(false);
        frame.dispose();
    }

    public static void log(String message) {
        try {
            console.getDocument().insertString(console.getDocument().getLength(), message + "\n", null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}
