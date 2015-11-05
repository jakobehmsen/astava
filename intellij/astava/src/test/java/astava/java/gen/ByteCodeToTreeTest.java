package astava.java.gen;

import astava.java.ArithmeticOperator;
import astava.java.Descriptor;
import astava.java.DomFactory;
import astava.java.RelationalOperator;
import astava.tree.StatementDom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class ByteCodeToTreeTest {
    public static class CaseClass {
        private int i = 0;

        /*public void test1() {
            this.i = 10;
        }

        public static StatementDom fortest1expect() {
            return DomFactory.block(Arrays.asList(
                DomFactory.assignField(DomFactory.self(), "i", Descriptor.INT, DomFactory.literal(10)),
                DomFactory.ret()
            ));
        }

        public void test2(int a) {
            this.i = a;
        }

        public static StatementDom fortest2expect() {
            return DomFactory.block(Arrays.asList(
                DomFactory.assignField(DomFactory.self(), "i", Descriptor.INT, DomFactory.accessVar("a")),
                DomFactory.ret()
            ));
        }

        public int test3(int i) {
            return i;
        }

        public static StatementDom fortest3expect() {
            return DomFactory.block(Arrays.asList(
                DomFactory.ret(DomFactory.accessVar("i"))
            ));
        }

        public static StatementDom fortest3unpreparedexpect() {
            return DomFactory.block(Arrays.asList(
                DomFactory.ret(DomFactory.accessVar("arg0"))
            ));
        }

        public int test4() {
            return 11;
        }

        public static StatementDom fortest4expect() {
            return DomFactory.block(Arrays.asList(
                DomFactory.ret(DomFactory.literal(11))
            ));
        }

        public int test5() {
            int x = 5;
            return x;
        }

        public static StatementDom fortest5expect() {
            return DomFactory.block(Arrays.asList(
                DomFactory.declareVar(Descriptor.INT, "x"),
                DomFactory.assignVar("x", DomFactory.literal(5)),
                DomFactory.ret(DomFactory.accessVar("x"))
            ));
        }

        public static StatementDom fortest5unpreparedexpect() {
            return DomFactory.block(Arrays.asList(
                DomFactory.declareVar(Descriptor.INT, "var0"),
                DomFactory.assignVar("var0", DomFactory.literal(5)),
                DomFactory.ret(DomFactory.accessVar("var0"))
            ));
        }

        public int test6() {
            int x = 5;
            int y = 8;
            return x + y;
        }

        public static StatementDom fortest6expect() {
            return DomFactory.block(Arrays.asList(
                DomFactory.declareVar(Descriptor.INT, "x"),
                DomFactory.declareVar(Descriptor.INT, "y"),
                DomFactory.assignVar("x", DomFactory.literal(5)),
                DomFactory.assignVar("y", DomFactory.literal(8)),
                DomFactory.ret(DomFactory.add(DomFactory.accessVar("x"), DomFactory.accessVar("y")))
            ));
        }

        public static StatementDom fortest6unpreparedexpect() {
            return DomFactory.block(Arrays.asList(
                DomFactory.declareVar(Descriptor.INT, "var0"),
                DomFactory.assignVar("var0", DomFactory.literal(5)),
                DomFactory.declareVar(Descriptor.INT, "var1"),
                DomFactory.assignVar("var1", DomFactory.literal(8)),
                DomFactory.ret(DomFactory.add(DomFactory.accessVar("var0"), DomFactory.accessVar("var1")))
            ));
        }

        public int test7(int i, int x) {
            return i + x;
        }

        public static StatementDom fortest7expect() {
            return DomFactory.block(Arrays.asList(
                DomFactory.ret(DomFactory.add(DomFactory.accessVar("i"), DomFactory.accessVar("x")))
            ));
        }

        public static StatementDom fortest7unpreparedexpect() {
            return DomFactory.block(Arrays.asList(
                DomFactory.ret(DomFactory.add(DomFactory.accessVar("arg0"), DomFactory.accessVar("arg1")))
            ));
        }*/

        public int test8() {
            int i;

            if(this.i == 1) {
                i = 1;
            } else {
                i = 0;
            }

            return i;
        }

        public static StatementDom fortest8expect() {
            return DomFactory.block(Arrays.asList(
                DomFactory.declareVar(Descriptor.INT, "i"),
                DomFactory.ifElse(
                    DomFactory.compare(DomFactory.accessField(DomFactory.self(), "i", Descriptor.INT), DomFactory.literal(1), RelationalOperator.EQ),
                    DomFactory.block(Arrays.asList(DomFactory.assignVar("i", DomFactory.literal(1)))),
                    DomFactory.block(Arrays.asList(DomFactory.assignVar("i", DomFactory.literal(0))))
                ),
                DomFactory.ret(DomFactory.accessVar("i"))
            ));
        }

        public int test9() {
            if(this.i == 1) {
                return 1;
            } else {
                return 0;
            }
        }

        public static StatementDom fortest9expect() {
            return DomFactory.block(Arrays.asList(
                DomFactory.ifElse(
                    DomFactory.compare(DomFactory.accessField(DomFactory.self(), "i", Descriptor.INT), DomFactory.literal(1), RelationalOperator.EQ),
                    DomFactory.block(Arrays.asList(DomFactory.ret(DomFactory.literal(1)))),
                    DomFactory.block(Arrays.asList(DomFactory.ret(DomFactory.literal(0))))
                )
            ));
        }
    }

    private MethodNode methodNode;
    private StatementDom expectedStatement;
    private StatementDom expectedUnpreparedStatement;

    public ByteCodeToTreeTest(MethodNode methodNode, StatementDom expectedStatement, StatementDom expectedUnpreparedStatement) {
        this.methodNode = methodNode;
        this.expectedStatement = expectedStatement;
        this.expectedUnpreparedStatement = expectedUnpreparedStatement;
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

                    StatementDom unpreparedExpectedStatement = null;
                    try {
                        unpreparedExpectedStatement = (StatementDom)c.getMethod("for" + x.getName() + "unpreparedexpect").invoke(null);
                    } catch (IllegalAccessException e) {

                    } catch (InvocationTargetException e) {

                    } catch (NoSuchMethodException e) {

                    }

                    try {
                        StatementDom expectedStatement = (StatementDom)c.getMethod("for" + x.getName() + "expect").invoke(null);

                        return new Object[]{methodNode, expectedStatement, unpreparedExpectedStatement};
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
        byteCodeToTree.prepareVariables(mv -> methodNode.accept(mv));
        methodNode.accept(byteCodeToTree);
        StatementDom actualStatement = byteCodeToTree.getBlock();
        assertEquals(expectedStatement, actualStatement);

        if(expectedUnpreparedStatement != null) {
            byteCodeToTree = new ByteCodeToTree(methodNode);
            methodNode.accept(byteCodeToTree);
            actualStatement = byteCodeToTree.getBlock();
            assertEquals(expectedUnpreparedStatement, actualStatement);
        }
    }
}