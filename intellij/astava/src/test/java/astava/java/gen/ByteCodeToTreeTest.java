package astava.java.gen;

import astava.java.Descriptor;
import astava.java.DomFactory;
import astava.java.parser.Parser;
import astava.tree.StatementDom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class ByteCodeToTreeTest {
    public static class CaseClass {
        private int i = 0;

        public void test1() {
            this.i = 10;
        }

        public static StatementDom fortest1expect() {
            return DomFactory.block(Arrays.asList(
                DomFactory.assignField(DomFactory.self(), "i", Descriptor.INT, DomFactory.literal(10))
            ));
        }
    }

    private MethodNode methodNode;
    private StatementDom expectedStatement;

    public ByteCodeToTreeTest(MethodNode methodNode, StatementDom expectedStatement) {
        this.methodNode = methodNode;
        this.expectedStatement = expectedStatement;
    }

    private static List<Object[]> load(Class<?> c) {
        InputStream resource = CaseClass.class.getResourceAsStream(c.getName().substring(c.getName().lastIndexOf('.') + 1) + ".class");

        try {
            ClassReader cr = new ClassReader(resource);

            ClassNode classNode = new ClassNode(Opcodes.ASM5);
            cr.accept(classNode, org.objectweb.asm.ClassReader.EXPAND_FRAMES);

            return Arrays.asList(CaseClass.class.getMethods()).stream()
                .filter(x -> x.getName().startsWith("test"))
                .map(x -> {
                    MethodNode methodNode = ((List<MethodNode>)classNode.methods).stream().filter(y -> y.name.equals(x.getName())).findFirst().get();
                    try {
                        StatementDom expectedStatement = (StatementDom)c.getMethod("for" + x.getName() + "expect").invoke(null);
                        return new Object[]{methodNode, expectedStatement};
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }

                    return null;
                })
                .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SecurityException e) {

        }

        return null;
    }

    @Parameterized.Parameters
    public static Collection values() {
        return load(CaseClass.class);
    }

    /*private static Object[] createCase(String sourceCode) {
        Parser parser = new Parser(sourceCode);
    }*/

    @Test
    public void test() {
        ByteCodeToTree byteCodeToTree = new ByteCodeToTree(methodNode);
        methodNode.accept(byteCodeToTree);
        StatementDom actualStatement = byteCodeToTree.getBlock();
        assertEquals(expectedStatement, actualStatement);
    }
}