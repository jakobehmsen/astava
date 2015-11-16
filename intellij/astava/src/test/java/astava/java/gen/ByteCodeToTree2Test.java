package astava.java.gen;

import astava.java.Descriptor;
import astava.java.DomFactory;
import astava.java.RelationalOperator;
import astava.tree.Dom;
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
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class ByteCodeToTree2Test {
    private MethodNode methodNode;
    private StatementDom expectedStatement;
    private StatementDom expectedUnpreparedStatement;

    public ByteCodeToTree2Test(MethodNode methodNode, StatementDom expectedStatement, StatementDom expectedUnpreparedStatement) {
        this.methodNode = methodNode;
        this.expectedStatement = expectedStatement;
        this.expectedUnpreparedStatement = expectedUnpreparedStatement;
    }

    private static Object[] load(Object testCase) {
        Class<?> c = testCase.getClass();
        InputStream resource = c.getResourceAsStream(c.getName().substring(c.getName().lastIndexOf('.') + 1) + ".class");

        try {
            ClassReader cr = new ClassReader(resource);

            ClassNode classNode = new ClassNode(Opcodes.ASM5);
            cr.accept(classNode, org.objectweb.asm.ClassReader.EXPAND_FRAMES);

            String byteCodeMethodName = "byteCode";
            String expectedMethodName = "expectedTree";
            String preparedExpectedMethodName = "unpreparedExpectedTree";

            MethodNode methodNode = ((List<MethodNode>)classNode.methods).stream().filter(y -> y.name.equals(byteCodeMethodName)).findFirst().get();

            StatementDom unpreparedExpectedStatement = null;
            try {
                unpreparedExpectedStatement = (StatementDom)c.getMethod(preparedExpectedMethodName).invoke(testCase);
            } catch (IllegalAccessException e) {

            } catch (InvocationTargetException e) {

            } catch (NoSuchMethodException e) {

            }

            try {
                StatementDom expectedStatement = (StatementDom)c.getMethod(expectedMethodName).invoke(testCase);

                return new Object[]{methodNode, expectedStatement, unpreparedExpectedStatement};
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SecurityException e) {

        }

        return null;
    }

    @Parameterized.Parameters
    public static Collection values() {
        return Arrays.asList(
            /*new Object() {
                private int i;

                public int byteCode() {
                    int j = i == 1 ? 1 : 0;

                    return j;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "j"),
                        DomFactory.assignVar("j", DomFactory.ifElseExpr(DomFactory.compare(DomFactory.accessField(DomFactory.self(), "i", Descriptor.INT), DomFactory.literal(1), RelationalOperator.EQ), DomFactory.literal(1), DomFactory.literal(0))),
                        DomFactory.ret(DomFactory.accessVar("j"))
                    ));
                }
            },
            new Object() {
                private int i;

                public boolean byteCode(boolean a, boolean b, boolean c) {
                    return a || b && c;

                }

                public StatementDom expectedTree() {
                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "j"),
                        DomFactory.assignVar("j", DomFactory.ifElseExpr(DomFactory.compare(DomFactory.accessField(DomFactory.self(), "i", Descriptor.INT), DomFactory.literal(1), RelationalOperator.EQ), DomFactory.literal(1), DomFactory.literal(0))),
                        DomFactory.ret(DomFactory.accessVar("j"))
                    ));
                }
            },
            new Object() {
                public boolean byteCode(boolean a, boolean b, boolean c) {
                    if(a || b && c)
                        return true;
                    return false;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.ifElse(DomFactory.ne(DomFactory.accessVar("a"), DomFactory.literal(false)), DomFactory.goTo("L0"), DomFactory.block()),
                        DomFactory.ifElse(DomFactory.eq(DomFactory.accessVar("b"), DomFactory.literal(false)), DomFactory.goTo("L1"), DomFactory.block()),
                        DomFactory.ifElse(DomFactory.eq(DomFactory.accessVar("c"), DomFactory.literal(false)), DomFactory.goTo("L1"), DomFactory.block()),
                        DomFactory.mark("L0"),
                        DomFactory.ret(DomFactory.literal(true)),
                        DomFactory.mark("L1"),
                        DomFactory.ret(DomFactory.literal(false))
                    );
                }
            },
            new Object() {
                public boolean byteCode(boolean a, boolean b, boolean c) {
                    if(a || b && c) {
                        if(b && a)
                            return true;
                        return false;
                    }
                    return false;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.ifElse(DomFactory.ne(DomFactory.accessVar("a"), DomFactory.literal(false)), DomFactory.goTo("L0"), DomFactory.block()),
                        DomFactory.ifElse(DomFactory.eq(DomFactory.accessVar("b"), DomFactory.literal(false)), DomFactory.goTo("L1"), DomFactory.block()),
                        DomFactory.ifElse(DomFactory.eq(DomFactory.accessVar("c"), DomFactory.literal(false)), DomFactory.goTo("L1"), DomFactory.block()),
                        DomFactory.mark("L0"),
                        DomFactory.ifElse(DomFactory.eq(DomFactory.accessVar("b"), DomFactory.literal(false)), DomFactory.goTo("L2"), DomFactory.block()),
                        DomFactory.ifElse(DomFactory.eq(DomFactory.accessVar("a"), DomFactory.literal(false)), DomFactory.goTo("L2"), DomFactory.block()),
                        DomFactory.ret(DomFactory.literal(true)),
                        DomFactory.mark("L2"),
                        DomFactory.ret(DomFactory.literal(false)),
                        DomFactory.mark("L1"),
                        DomFactory.ret(DomFactory.literal(false))
                    );
                }
            },
            new Object() {
                public void myMethod(int i, int j) {

                }

                public void byteCode() {
                    myMethod(50, 55);
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.invokeVirtual(
                            Descriptor.get(getClass().getName()), "myMethod", Descriptor.getMethodDescriptor(Arrays.asList(int.class, int.class), void.class),
                            DomFactory.self(), Arrays.asList(DomFactory.literal(50), DomFactory.literal(55)))
                    );
                }
            },
            new Object() {
                public int myMethod(int i, int j) {
                    return i;
                }

                public void byteCode() {
                    myMethod(50, 55);
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.invokeVirtual(
                            Descriptor.get(getClass().getName()), "myMethod", Descriptor.getMethodDescriptor(Arrays.asList(int.class, int.class), int.class),
                            DomFactory.self(), Arrays.asList(DomFactory.literal(50), DomFactory.literal(55)))
                    );
                }
            },
            new Object() {
                public int myMethod(int i, int j) {
                    return i;
                }

                public void byteCode() {
                    int i = myMethod(50, 55);
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.assignVar("i", DomFactory.invokeVirtualExpr(
                            Descriptor.get(getClass().getName()), "myMethod", Descriptor.getMethodDescriptor(Arrays.asList(int.class, int.class), int.class),
                            DomFactory.self(), Arrays.asList(DomFactory.literal(50), DomFactory.literal(55))))
                    );
                }
            },
            new Object() {
                public void byteCode() {
                    MyClass x = new MyClass(4, 5);
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.assignVar("x", DomFactory.newInstanceExpr(
                            Descriptor.get(MyClass.class.getName()), Arrays.asList(Descriptor.get(int.class), Descriptor.get(int.class)),
                            Arrays.asList(DomFactory.literal(4), DomFactory.literal(5))))
                    );
                }
            },
            new Object() {
                public void byteCode() {
                    new MyClass(4, 5);
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.newInstance(
                            Descriptor.get(MyClass.class.getName()), Arrays.asList(Descriptor.get(int.class), Descriptor.get(int.class)),
                            Arrays.asList(DomFactory.literal(4), DomFactory.literal(5)))
                    );
                }
            },
            new Object() {
                public void byteCode() {
                    int k = 0;
                    k++;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.assignVar("k", DomFactory.literal(0)),
                        DomFactory.intIncVar("k", 1)
                    );
                }
            },
            new Object() {
                public void byteCode() {
                    int k = 0;
                    k += 11;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.assignVar("k", DomFactory.literal(0)),
                        DomFactory.intIncVar("k", 11)
                    );
                }
            },
            new Object() {
                public void byteCode() {
                    int k = 0;
                    k--;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.assignVar("k", DomFactory.literal(0)),
                        DomFactory.intIncVar("k", -1)
                    );
                }
            },
            new Object() {
                public void byteCode() {
                    int k = 0;
                    k -= 11;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.assignVar("k", DomFactory.literal(0)),
                        DomFactory.intIncVar("k", -11)
                    );
                }
            },
            new Object() {
                public int byteCode(int i, int j) {
                    return i & j;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.ret(DomFactory.band(DomFactory.accessVar("i"), DomFactory.accessVar("j")))
                    );
                }
            },
            new Object() {
                public int byteCode(int i, int j) {
                    return i | j;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.ret(DomFactory.bor(DomFactory.accessVar("i"), DomFactory.accessVar("j")))
                    );
                }
            },
            new Object() {
                public int byteCode(int i, int j) {
                    return i ^ j;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.ret(DomFactory.bxor(DomFactory.accessVar("i"), DomFactory.accessVar("j")))
                    );
                }
            },*/
            new Object() {
                public Class<?> byteCode() {
                    return MyClass.class;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.ret(DomFactory.classLiteral(Descriptor.get(MyClass.class)))
                    );
                }
            },
            new Object() {
                public Class<?> byteCode() {
                    return int.class;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.ret(DomFactory.accessStaticField(Descriptor.get(Integer.class), "TYPE", Descriptor.get(Class.class)))
                    );
                }
            }
        ).stream().map(x -> load(x)).collect(Collectors.toList());
    }

    private static class MyClass {
        public MyClass(int i, int j) {

        }
    }

    @Test
    public void test() {
        ByteCodeToTree2 byteCodeToTree = new ByteCodeToTree2(methodNode);
        byteCodeToTree.prepareVariables(mv -> methodNode.accept(mv));
        methodNode.accept(byteCodeToTree);
        StatementDom actualStatement = byteCodeToTree.getBlock();

        System.out.println("expectedStatement:");
        System.out.println(expectedStatement);
        System.out.println("actualStatement:");
        System.out.println(actualStatement);

        assertEquals(expectedStatement, actualStatement);

        if (expectedUnpreparedStatement != null) {
            byteCodeToTree = new ByteCodeToTree2(methodNode);
            methodNode.accept(byteCodeToTree);
            actualStatement = byteCodeToTree.getBlock();
            assertEquals(expectedUnpreparedStatement, actualStatement);
        }
    }
}