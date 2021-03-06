package astava.java.gen;

import astava.java.Descriptor;
import astava.java.DomFactory;
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
public class ByteCodeToTreeTest {
    private MethodNode methodNode;
    private StatementDom expectedStatement;
    private StatementDom expectedUnpreparedStatement;

    public ByteCodeToTreeTest(MethodNode methodNode, StatementDom expectedStatement, StatementDom expectedUnpreparedStatement) {
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
            new Object() {
                private int i;

                public int byteCode() {
                    int j = i == 1 ? 1 : 0;

                    return j;
                }

                public StatementDom expectedTree() {
                    // To improve robustness of tests, add support for logical equivalents of variables, such that
                    // it is not necessary to write the exact same names for variables (such as "{s3, s4}"), but
                    // instead their logical equivalents - corresponding to how labels are compared.
                    // This is especially practical for stack variables that aren't copy propagated.
                    return DomFactory.block(
                        DomFactory.ifElse(DomFactory.ne(DomFactory.accessField(DomFactory.self(), "i", Descriptor.INT), DomFactory.literal(1)), DomFactory.goTo("L0"), DomFactory.block()),
                        DomFactory.assignVar("{s3, s4}", DomFactory.literal(1)),
                        DomFactory.goTo("L1"),
                        DomFactory.mark("L0"),
                        DomFactory.assignVar("{s3, s4}", DomFactory.literal(0)),
                        DomFactory.mark("L1"),
                        DomFactory.assignVar("j", DomFactory.accessVar("{s3, s4}")),
                        DomFactory.ret(DomFactory.accessVar("j"))
                    );
                }
            },
            new Object() {
                private int i;

                public boolean byteCode(boolean a, boolean b, boolean c) {
                    return a || b && c;

                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.ifElse(DomFactory.ne(DomFactory.accessVar("a"), DomFactory.literal(false)), DomFactory.goTo("L0"), DomFactory.block()),
                        DomFactory.ifElse(DomFactory.eq(DomFactory.accessVar("b"), DomFactory.literal(false)), DomFactory.goTo("L1"), DomFactory.block()),
                        DomFactory.ifElse(DomFactory.eq(DomFactory.accessVar("c"), DomFactory.literal(false)), DomFactory.goTo("L1"), DomFactory.block()),
                        DomFactory.mark("L0"),
                        DomFactory.assignVar("{s3, s4}", DomFactory.literal(1)),
                        DomFactory.goTo("L2"),
                        DomFactory.mark("L1"),
                        DomFactory.assignVar("{s3, s4}", DomFactory.literal(0)),
                        DomFactory.mark("L2"),
                        DomFactory.ret(DomFactory.accessVar("{s3, s4}"))
                    );
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
                            DomFactory.self(), Arrays.asList(DomFactory.literal(50), DomFactory.literal(55))),
                        DomFactory.ret()
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
                            DomFactory.self(), Arrays.asList(DomFactory.literal(50), DomFactory.literal(55))),
                        DomFactory.ret()
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
                            DomFactory.self(), Arrays.asList(DomFactory.literal(50), DomFactory.literal(55)))),
                        DomFactory.ret()
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
                            Arrays.asList(DomFactory.literal(4), DomFactory.literal(5)))),
                        DomFactory.ret()
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
                            Arrays.asList(DomFactory.literal(4), DomFactory.literal(5))),
                        DomFactory.ret()
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
                        DomFactory.intIncVar("k", 1),
                        DomFactory.ret()
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
                        DomFactory.intIncVar("k", 11),
                        DomFactory.ret()
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
                        DomFactory.intIncVar("k", -1),
                        DomFactory.ret()
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
                        DomFactory.intIncVar("k", -11),
                        DomFactory.ret()
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
            },
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
            },
            new Object() {
                public int byteCode(Object[] array) {
                    return array.length;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.ret(DomFactory.arrayLength(DomFactory.accessVar("array")))
                    );
                }
            },
            new Object() {
                public void byteCode(Object[] array) {
                    array[1] = "str";
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.arrayStore(DomFactory.accessVar("array"), DomFactory.literal(1), DomFactory.literal("str")),
                        DomFactory.ret()
                    );
                }
            },
            new Object() {
                public byte byteCode(int i) {
                    return (byte)i;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.ret(DomFactory.typeCast(DomFactory.accessVar("i"), Descriptor.get(byte.class)))
                    );
                }
            },
            new Object() {
                public boolean byteCode(Object obj) {
                    return obj instanceof String;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.ret(DomFactory.instanceOf(DomFactory.accessVar("obj"), Descriptor.get(String.class)))
                    );
                }
            },
            new Object() {
                public void byteCode() {
                    MyClass.x = 10;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.assignStaticField(Descriptor.get(MyClass.class), "x", Descriptor.get(int.class), DomFactory.literal(10)),
                        DomFactory.ret()
                    );
                }
            },
            new Object() {
                public int byteCode(int i) {
                    return -i;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.ret(DomFactory.neg(DomFactory.accessVar("i")))
                    );
                }
            },
            new Object() {
                public int byteCode(int i, int j) {
                    return i << j;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.ret(DomFactory.shl(DomFactory.accessVar("i"), DomFactory.accessVar("j")))
                    );
                }
            },
            new Object() {
                public int byteCode(int i, int j) {
                    return i >> j;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.ret(DomFactory.shr(DomFactory.accessVar("i"), DomFactory.accessVar("j")))
                    );
                }
            },
            new Object() {
                public int byteCode(int i, int j) {
                    return i >>> j;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.ret(DomFactory.ushr(DomFactory.accessVar("i"), DomFactory.accessVar("j")))
                    );
                }
            },


            new Object() {
                public int byteCode(int i) {
                    switch(i) {
                        case 0:
                            return 1;
                        case 2:
                            return 4;
                        default:
                            return 7;
                    }
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.select(DomFactory.accessVar("i"), "L2", new int[]{0, 2}, new Object[]{"L0", "L1"}),
                        DomFactory.mark("L0"),
                        DomFactory.ret(DomFactory.literal(1)),
                        DomFactory.mark("L1"),
                        DomFactory.ret(DomFactory.literal(4)),
                        DomFactory.mark("L2"),
                        DomFactory.ret(DomFactory.literal(7))
                    );
                }
            },
            new Object() {
                public void byteCode(int i) {
                    switch(i) {
                        case 0:
                            i++;
                            break;
                        case 2:
                            i--;
                            break;
                        default:
                            i*=2;
                            break;
                    }
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.select(DomFactory.accessVar("i"), "L2", new int[]{0, 2}, new Object[]{"L0", "L1"}),
                        DomFactory.mark("L0"),
                        DomFactory.intIncVar("i", 1),
                        DomFactory.goTo("L3"),
                        DomFactory.mark("L1"),
                        DomFactory.intIncVar("i", -1),
                        DomFactory.goTo("L3"),
                        DomFactory.mark("L2"),
                        DomFactory.assignVar("i", DomFactory.mul(DomFactory.accessVar("i"), DomFactory.literal(2))),
                        DomFactory.mark("L3"),
                        DomFactory.ret()
                    );
                }
            },


            new Object() {
                public int byteCode(int i) {
                    if (i > 0) {
                        switch (i) {
                            case 0:
                                return 1;
                            case 2:
                                return 4;
                            default:
                                return 7;
                        }
                    } else {
                        switch (i) {
                            case 0:
                                return 1;
                            case 2:
                                return 4;
                            default:
                                return 7;
                        }
                    }
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.ifElse(DomFactory.le(DomFactory.accessVar("i"), DomFactory.literal(0)), DomFactory.goTo("L0"), DomFactory.block()),

                        DomFactory.select(DomFactory.accessVar("i"), "L3", new int[]{0, 2}, new Object[]{"L1", "L2"}),
                        DomFactory.mark("L1"),
                        DomFactory.ret(DomFactory.literal(1)),
                        DomFactory.mark("L2"),
                        DomFactory.ret(DomFactory.literal(4)),
                        DomFactory.mark("L3"),
                        DomFactory.ret(DomFactory.literal(7)),

                        DomFactory.mark("L0"),

                        DomFactory.select(DomFactory.accessVar("i"), "L6", new int[]{0, 2}, new Object[]{"L4", "L5"}),
                        DomFactory.mark("L4"),
                        DomFactory.ret(DomFactory.literal(1)),
                        DomFactory.mark("L5"),
                        DomFactory.ret(DomFactory.literal(4)),
                        DomFactory.mark("L6"),
                        DomFactory.ret(DomFactory.literal(7))
                    );
                }
            },
            new Object() {
                public int byteCode(int i) {
                    switch (i) {
                        case 0:
                            if (i > 0)
                                return 1;
                            break;
                        case 2:
                            if (i > 0)
                                return 4;
                            break;
                        default:
                            if (i > 0)
                                return 7;
                            break;
                    }

                    return 9;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.select(DomFactory.accessVar("i"), "L2", new int[]{0, 2}, new Object[]{"L0", "L1"}),

                        DomFactory.mark("L0"),
                        DomFactory.ifElse(DomFactory.le(DomFactory.accessVar("i"), DomFactory.literal(0)), DomFactory.goTo("L3"), DomFactory.block()),
                        DomFactory.ret(DomFactory.literal(1)),

                        DomFactory.mark("L1"),
                        DomFactory.ifElse(DomFactory.le(DomFactory.accessVar("i"), DomFactory.literal(0)), DomFactory.goTo("L3"), DomFactory.block()),
                        DomFactory.ret(DomFactory.literal(4)),

                        DomFactory.mark("L2"),
                        DomFactory.ifElse(DomFactory.le(DomFactory.accessVar("i"), DomFactory.literal(0)), DomFactory.goTo("L3"), DomFactory.block()),
                        DomFactory.ret(DomFactory.literal(7)),

                        DomFactory.mark("L3"),
                        DomFactory.ret(DomFactory.literal(9))
                    );
                }
            },
            new Object() {
                public void byteCode(int i) {
                    if (i > 0) {
                        switch (i) {
                            case 0:
                                i++;
                                break;
                            case 2:
                                i--;
                                break;
                            default:
                                i *= 2;
                                break;
                        }
                    } else {
                        switch (i) {
                            case 0:
                                i++;
                                break;
                            case 2:
                                i--;
                                break;
                            default:
                                i *= 2;
                                break;
                        }
                    }
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.ifElse(DomFactory.le(DomFactory.accessVar("i"), DomFactory.literal(0)), DomFactory.goTo("L0"), DomFactory.block()),

                        DomFactory.select(DomFactory.accessVar("i"), "L3", new int[]{0, 2}, new Object[]{"L1", "L2"}),
                        DomFactory.mark("L1"),
                        DomFactory.intIncVar("i", 1),
                        DomFactory.goTo("L4"),
                        DomFactory.mark("L2"),
                        DomFactory.intIncVar("i", -1),
                        DomFactory.goTo("L4"),
                        DomFactory.mark("L3"),
                        DomFactory.assignVar("i", DomFactory.mul(DomFactory.accessVar("i"), DomFactory.literal(2))),
                        DomFactory.goTo("L4"),

                        DomFactory.mark("L0"),

                        DomFactory.select(DomFactory.accessVar("i"), "L7", new int[]{0, 2}, new Object[]{"L5", "L6"}),
                        DomFactory.mark("L5"),
                        DomFactory.intIncVar("i", 1),
                        DomFactory.goTo("L4"),
                        DomFactory.mark("L6"),
                        DomFactory.intIncVar("i", -1),
                        DomFactory.goTo("L4"),
                        DomFactory.mark("L7"),
                        DomFactory.assignVar("i", DomFactory.mul(DomFactory.accessVar("i"), DomFactory.literal(2))),

                        DomFactory.mark("L4"),
                        DomFactory.ret()
                    );
                }
            },
            new Object() {
                public void byteCode(int i) {
                    switch (i) {
                        case 0:
                            if (i > 0)
                                i++;
                            break;
                        case 2:
                            if (i > 0)
                                i--;
                            break;
                        default:
                            if (i > 0)
                                i *= 2;
                            break;
                    }
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(
                        DomFactory.select(DomFactory.accessVar("i"), "L2", new int[]{0, 2}, new Object[]{"L0", "L1"}),

                        DomFactory.mark("L0"),
                        DomFactory.ifElse(DomFactory.le(DomFactory.accessVar("i"), DomFactory.literal(0)), DomFactory.goTo("L3"), DomFactory.block()),
                        DomFactory.intIncVar("i", 1),
                        DomFactory.goTo("L3"),

                        DomFactory.mark("L1"),
                        DomFactory.ifElse(DomFactory.le(DomFactory.accessVar("i"), DomFactory.literal(0)), DomFactory.goTo("L3"), DomFactory.block()),
                        DomFactory.intIncVar("i", -1),
                        DomFactory.goTo("L3"),

                        DomFactory.mark("L2"),
                        DomFactory.ifElse(DomFactory.le(DomFactory.accessVar("i"), DomFactory.literal(0)), DomFactory.goTo("L3"), DomFactory.block()),
                        DomFactory.assignVar("i", DomFactory.mul(DomFactory.accessVar("i"), DomFactory.literal(2))),

                        DomFactory.mark("L3"),
                        DomFactory.ret()
                    );
                }
            }
        ).stream().map(x -> load(x)).collect(Collectors.toList());
    }

    private static class MyClass {
        public static int x;

        public MyClass(int i, int j) {

        }
    }

    @Test
    public void test() {
        ByteCodeToTree byteCodeToTree = new ByteCodeToTree(methodNode);
        byteCodeToTree.prepareVariables(mv -> methodNode.accept(mv));
        methodNode.accept(byteCodeToTree);
        StatementDom actualStatement = byteCodeToTree.getBlock();

        System.out.println("expectedStatement:");
        System.out.println(expectedStatement);
        System.out.println("actualStatement:");
        System.out.println(actualStatement);

        assertEquals(expectedStatement, actualStatement);

        if (expectedUnpreparedStatement != null) {
            byteCodeToTree = new ByteCodeToTree(methodNode);
            methodNode.accept(byteCodeToTree);
            actualStatement = byteCodeToTree.getBlock();
            assertEquals(expectedUnpreparedStatement, actualStatement);
        }
    }
}