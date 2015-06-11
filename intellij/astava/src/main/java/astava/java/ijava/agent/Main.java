package astava.java.ijava.agent;

import astava.java.Descriptor;
import astava.java.parser.ClassDomBuilder;
import astava.java.parser.Factory;
import astava.java.parser.MutableClassDomBuilder;
import astava.tree.ParameterInfo;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static void premain(String agentArgument, Instrumentation instrumentation) {

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

        /*System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        }));*/

        try {
            log("Initializing agent...");

            ObjectInputStream objectInputStream = new ObjectInputStream(input);
            Map<String, ClassDomBuilder> classBuilders = (Map<String, ClassDomBuilder>)objectInputStream.readObject();

            instrumentation.addTransformer(new ClassFileTransformer() {
                @Override
                public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                    ClassDomBuilder classBuilder = classBuilders.get(className);

                    if(classBuilder != null) {
                        ClassReader cr = new ClassReader(classfileBuffer);
                        ClassNode classNode = new ClassNode(Opcodes.ASM5);
                        cr.accept(classNode, ClassReader.EXPAND_FRAMES);

                        MutableClassDomBuilder combinedBuilder = new MutableClassDomBuilder();

                        // Populate from class node
                        combinedBuilder.setName(className);
                        combinedBuilder.setSuperName(classNode.superName);
                        combinedBuilder.setModifier(classNode.access);

                        for(Object field: classNode.fields) {
                            combinedBuilder.addField(Factory.field(
                                ((FieldNode) field).access,
                                ((FieldNode) field).name,
                                Descriptor.getName(((FieldNode) field).desc)
                            ));
                        }

                        for(Object method: classNode.methods) {
                            MethodNode methodNode = (MethodNode)method;
                            Type returnType = Type.getReturnType(methodNode.desc);
                            Type[] parameterTypes = Type.getArgumentTypes(methodNode.desc);
                            List<ParameterInfo> parameters = IntStream.range(0, parameterTypes.length).mapToObj(i -> {
                                ParameterNode pm = (ParameterNode) methodNode.parameters.get(i);
                                return new ParameterInfo(parameterTypes[i].getDescriptor(), pm.name);
                            }).collect(Collectors.toList());
                            Factory.method(
                                ((MethodNode) method).access,
                                ((MethodNode) method).name,
                                parameters,
                                returnType.getDescriptor(),
                                v -> v.visitASM(methodNode));
                        }

                        // Populate from class builder
                        classBuilder.getFields().forEach(x -> combinedBuilder.addField(x));
                        classBuilder.getMethods().forEach(x -> combinedBuilder.addMethod(x));
                    }

                    return classfileBuffer;
                }
            });

            log("Initialized agent.");
        } catch (IOException e) {
            log("Error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            log("Error: " + e.getMessage());
        }

        /*try {
            String message = input.readUTF();
            output.writeUTF("Received: " + message);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public static void log(String message) {
        //System.console().printf(message + "\n");
    }
}
