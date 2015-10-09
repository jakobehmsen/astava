package astava.java.ijava.server;

import astava.debug.Debug;
import astava.java.Descriptor;
import astava.java.gen.ClassGenerator;
import astava.java.gen.SingleClassLoader;
import astava.java.ijava.DebugClassLoader;
import astava.java.parser.*;
import astava.tree.*;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static astava.java.DomFactory.fieldDeclaration;
import static astava.java.DomFactory.methodDeclaration;

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

        Debug.setPrintStream(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        }) {
            @Override
            public void println() {
                logln("");
            }

            @Override
            public void println(boolean x) {
                logln(x);
            }

            @Override
            public void println(char x) {
                logln(x);
            }

            @Override
            public void println(int x) {
                logln(x);
            }

            @Override
            public void println(long x) {
                logln(x);
            }

            @Override
            public void println(float x) {
                logln(x);
            }

            @Override
            public void println(double x) {
                logln(x);
            }

            @Override
            public void println(char[] x) {
                logln(new String(x));
            }

            @Override
            public void println(String x) {
                logln(x);
            }

            @Override
            public void println(Object x) {
                logln(x);
            }

            @Override
            public void print(boolean b) {
                log(b);
            }

            @Override
            public void print(char c) {
                log(c);
            }

            @Override
            public void print(double d) {
                log(d);
            }

            @Override
            public void print(float f) {
                log(f);
            }

            @Override
            public void print(int i) {
                log(i);
            }

            @Override
            public void print(long l) {
                log(l);
            }

            @Override
            public void print(Object obj) {
                log(obj);
            }

            @Override
            public void print(char[] s) {
                log(new String(s));
            }

            @Override
            public void print(String s) {
                log(s);
            }

            @Override
            public PrintStream printf(String format, Object... args) {
                log(String.format(format, args));
                return this;
            }

            @Override
            public PrintStream printf(Locale l, String format, Object... args) {
                log(String.format(l, format, args));
                return this;
            }

            @Override
            public PrintStream append(char c) {
                log(c);
                return this;
            }

            @Override
            public PrintStream append(CharSequence csq) {
                log(csq);
                return this;
            }

            @Override
            public PrintStream append(CharSequence csq, int start, int end) {
                log(csq.subSequence(start, end));
                return this;
            }

            @Override
            public PrintStream format(String format, Object... args) {
                log(String.format(format, args));
                return this;
            }

            @Override
            public PrintStream format(Locale l, String format, Object... args) {
                log(String.format(l, format, args));
                return this;
            }

            @Override
            public void write(byte[] b) throws IOException {
                log(new String(b));
            }

            @Override
            public void write(int b) {
                log((char)b);
            }

            @Override
            public void write(byte[] buf, int off, int len) {
                log(new String(buf, off, len));
            }
        });

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        Hashtable<String, Variable> variables = new Hashtable<>();

        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(input);
        } catch (IOException e) {
            e.printStackTrace(Debug.getPrintStream(Debug.LEVEL_HIGH));
        }
        Map<String, ClassDomBuilder> classBuildersTmp = null;
        //Rectangle2D mainFrameBounds = null;
        try {
            classBuildersTmp = (Map<String, ClassDomBuilder>)objectInputStream.readObject();
            //output.writeInt(1);
            //output.flush();
            //mainFrameBounds = (Rectangle2D)objectInputStream.readObject();
            logln("Received class builders: " + classBuildersTmp);
        } catch (IOException e) {
            e.printStackTrace(Debug.getPrintStream(Debug.LEVEL_HIGH));
        } catch (ClassNotFoundException e) {
            e.printStackTrace(Debug.getPrintStream(Debug.LEVEL_HIGH));
        }
        Map<String, ClassDomBuilder> classBuilders = classBuildersTmp;


        //frame.setLocation((int)(mainFrameBounds.getX() + mainFrameBounds.getWidth()), 0);
        frame.setLocationRelativeTo(null);
        //frame.setLocation((int)Screen.getPrimary().getBounds().getWidth() - frame.getWidth(), frame.getY());
        frame.setVisible(true);

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
            logln("Waiting...");
            int operator = 0;
            try {
                operator = input.readInt();
            } catch (IOException e) {
                logln("Error: " + e.getMessage());
            }

            switch (operator) {
                case RequestCode.DECLARE:
                    try {
                        ObjectInputStream oiStream = null;
                        try {
                            oiStream = new ObjectInputStream(input);
                        } catch (IOException e) {
                            logln("Error: " + e.getMessage());
                        }
                        FieldDomBuilder fieldDomBuilder = null;
                        try {
                            fieldDomBuilder = (FieldDomBuilder)oiStream.readObject();
                        } catch (ClassNotFoundException e) {
                            logln("Error: " + e.getMessage());
                        } catch (IOException e) {
                            logln("Error: " + e.getMessage());
                        }

                        FieldDeclaration field = fieldDomBuilder.declare(classResolver);

                        String name = field.getName();
                        String typeName = field.getTypeName();
                        String descriptor = Descriptor.get(typeName);

                        logln("Declare " + typeName + " " + name);

                        Object defaultValue = Descriptor.getDefaultValue(descriptor);

                        variables.put(name, new Variable(typeName, defaultValue));
                    } catch(Exception e) {
                        logln("Error: " + e.getMessage());
                    }

                    break;
                case RequestCode.EXEC:
                    logln("Exec");

                    String resultToString;

                    try {
                        ObjectInputStream oiStream = new ObjectInputStream(input);
                        StatementDomBuilder stmtBuilder = (StatementDomBuilder)oiStream.readObject();

                        logln("stmt=" + stmtBuilder);

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

                                boolean isVirtualExtension = classBuilders.containsKey(name);
                                if(isVirtualExtension)
                                    logln("Class " + name + " is virtual extension.");

                                // Inspect virtual classes in class builders and physical classes in class loader
                                try {
                                    Class<?> physicalClass = classLoader.loadClass(name);

                                    logln("Loaded physical class " + name);

                                    return new ClassDeclaration() {
                                        @Override
                                        public List<FieldDeclaration> getFields() {
                                            return Arrays.asList(physicalClass.getDeclaredFields()).stream().map(x -> new FieldDeclaration() {
                                                @Override
                                                public int getModifier() {
                                                    return x.getModifiers();
                                                }

                                                @Override
                                                public String getTypeName() {
                                                    return x.getType().getName();
                                                }

                                                @Override
                                                public String getName() {
                                                    return x.getName();
                                                }

                                                @Override
                                                public FieldDom build(ClassDeclaration classDeclaration) {
                                                    return fieldDeclaration(getModifier(), getName(), getTypeName());
                                                }
                                            }).collect(Collectors.toList());
                                        }

                                        @Override
                                        public List<MethodDeclaration> getMethods() {
                                            List<MethodDeclaration> methods = Arrays.asList(physicalClass.getDeclaredMethods()).stream().map(x -> new MethodDeclaration() {
                                                @Override
                                                public int getModifier() {
                                                    return x.getModifiers();
                                                }

                                                @Override
                                                public String getName() {
                                                    return x.getName();
                                                }

                                                @Override
                                                public List<ParameterInfo> getParameterTypes() {
                                                    return Arrays.asList(x.getParameterTypes()).stream()
                                                        .map(x -> new ParameterInfo(Descriptor.get(x), "<NA>"))
                                                        .collect(Collectors.toList());
                                                }

                                                @Override
                                                public String getReturnTypeName() {
                                                    return x.getReturnType().getName();
                                                }

                                                @Override
                                                public MethodDom build(ClassDeclaration classDeclaration, ClassInspector classInspector) {
                                                    return null;
                                                }
                                            }).collect(Collectors.toList());

                                            List<MethodDeclaration> constructors = Arrays.asList(physicalClass.getConstructors()).stream().map(x -> new MethodDeclaration() {
                                                @Override
                                                public int getModifier() {
                                                    return x.getModifiers();
                                                }

                                                @Override
                                                public String getName() {
                                                    return "<init>";
                                                }

                                                @Override
                                                public List<ParameterInfo> getParameterTypes() {
                                                    return Arrays.asList(x.getParameterTypes()).stream()
                                                        .map(x -> new ParameterInfo(Descriptor.get(x), "<NA>"))
                                                        .collect(Collectors.toList());
                                                }

                                                @Override
                                                public String getReturnTypeName() {
                                                    return "void";
                                                }

                                                @Override
                                                public MethodDom build(ClassDeclaration classDeclaration, ClassInspector classInspector) {
                                                    return null;
                                                }
                                            }).collect(Collectors.toList());

                                            return Stream.concat(methods.stream(), constructors.stream()).collect(Collectors.toList());
                                        }

                                        @Override
                                        public int getModifiers() {
                                            return physicalClass.getModifiers();
                                        }

                                        @Override
                                        public String getName() {
                                            return physicalClass.getName();
                                        }

                                        @Override
                                        public String getSuperName() {
                                            return physicalClass.getSuperclass().getName();
                                        }

                                        @Override
                                        public List<String> getInterfaces() {
                                            return Arrays.asList(physicalClass.getInterfaces()).stream().map(x -> x.getName()).collect(Collectors.toList());
                                        }

                                        @Override
                                        public boolean isInterface() {
                                            return physicalClass.isInterface();
                                        }
                                    };
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                    return null;
                                }
                            }
                        };

                        Hashtable<String, String> locals = new Hashtable<>();
                        StatementDom stmt = stmtBuilder.build(classResolver, exeClassBuilderDeclaration, classInspector, new Hashtable<>());
                        String exprResultType = Parser.statementReturnType(null, exeClassBuilderDeclaration, stmt, locals);
                        log("exprResultType=" + exprResultType);

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

                        /*
                        ByteArrayOutputStream classGenerationOutputStream = new ByteArrayOutputStream();
                        PrintStream classGenerationPrintStream = new PrintStream(classGenerationOutputStream);
                        Debug.setPrintStream(classGenerationPrintStream);
                        */

                        ClassGenerator generator = new ClassGenerator(classDom);

                        ClassLoader exeClassLoader = new SingleClassLoader(classLoader, generator);
                        exeClassLoader = new DebugClassLoader(exeClassLoader);

                        Class<?> execClass = exeClassLoader.loadClass("Exec");

                        /*String classGenerationOutput = new String(classGenerationOutputStream.toByteArray());

                        if(classGenerationOutput.length() > 0) {
                            logln("Class generation:");
                            logln(classGenerationOutput);
                        }*/

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

                        logln("result = " + resultToString);
                        //outputStream.flush();

                        //output.write(ResponseCode.FINISHED);
                        //output.flush();

                        //System.out.println(resultToString);
                        //System.out.flush();
                    } catch (Exception e1) {
                        logln("Error: " + e1.getMessage());

                        /*
                        ByteArrayOutputStream stackTraceOutputStream = new ByteArrayOutputStream();
                        PrintStream stackTracePrintStream = new PrintStream(stackTraceOutputStream);
                        e1.printStackTrace(stackTracePrintStream);
                        logln("Stack trace: " + new String(stackTraceOutputStream.toByteArray()));
                        */

                        e1.printStackTrace(Debug.getPrintStream(Debug.LEVEL_HIGH));

                        resultToString = "Error: " + e1.getMessage();
                    }

                    try {
                        output.writeUTF(resultToString);
                        output.flush();
                    } catch (IOException e) {
                        logln("Error when sending response: " + e.getMessage());
                    }

                    break;
                case RequestCode.END:
                    logln("end");
                    run = false;
                    try {
                        output.writeInt(ResponseCode.FINISHED);
                        output.flush();
                    } catch (IOException e) {
                        logln("Error: " + e.getMessage());
                    }
                    break;
            }
        }

        frame.setVisible(false);
        frame.dispose();
    }

    public static void logln(Object message) {
        logln(message.toString());
    }

    public static void log(Object message) {
        log(message.toString());
    }

    public static void logln(String message) {
        log(message + "\n");
    }

    public static void log(String message) {
        if(console == null)
            return;

        try {
            console.getDocument().insertString(console.getDocument().getLength(), message, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}
