package astava.java.gen;

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
            /*new Object() {
                private int i;

                public void byteCode() {
                    this.i = 10;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(Arrays.asList(
                        DomFactory.assignField(DomFactory.self(), "i", Descriptor.INT, DomFactory.literal(10)),
                        DomFactory.ret()
                    ));
                }
            },
            new Object() {
                private int i;

                public void byteCode(int a) {
                    this.i = a;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(Arrays.asList(
                        DomFactory.assignField(DomFactory.self(), "i", Descriptor.INT, DomFactory.accessVar("a")),
                        DomFactory.ret()
                    ));
                }
            },
            new Object() {
                private int i;

                public int byteCode(int i) {
                    return i;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(Arrays.asList(
                        DomFactory.ret(DomFactory.accessVar("i"))
                    ));
                }

                public StatementDom unpreparedExpectedTree() {
                    return DomFactory.block(Arrays.asList(
                        DomFactory.ret(DomFactory.accessVar("arg0"))
                    ));
                }
            },
            new Object() {
                public int byteCode() {
                    return 11;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(Arrays.asList(
                        DomFactory.ret(DomFactory.literal(11))
                    ));
                }
            },
            new Object() {
                public int byteCode() {
                    int x = 5;
                    return x;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "x"),
                        DomFactory.assignVar("x", DomFactory.literal(5)),
                        DomFactory.ret(DomFactory.accessVar("x"))
                    ));
                }

                public StatementDom unpreparedExpectedTree() {
                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "var0"),
                        DomFactory.assignVar("var0", DomFactory.literal(5)),
                        DomFactory.ret(DomFactory.accessVar("var0"))
                    ));
                }
            },
            new Object() {
                public int byteCode() {
                    int x = 5;
                    int y = 8;
                    return x + y;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "x"),
                        DomFactory.declareVar(Descriptor.INT, "y"),
                        DomFactory.assignVar("x", DomFactory.literal(5)),
                        DomFactory.assignVar("y", DomFactory.literal(8)),
                        DomFactory.ret(DomFactory.add(DomFactory.accessVar("x"), DomFactory.accessVar("y")))
                    ));
                }

                public StatementDom unpreparedExpectedTree() {
                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "var0"),
                        DomFactory.assignVar("var0", DomFactory.literal(5)),
                        DomFactory.declareVar(Descriptor.INT, "var1"),
                        DomFactory.assignVar("var1", DomFactory.literal(8)),
                        DomFactory.ret(DomFactory.add(DomFactory.accessVar("var0"), DomFactory.accessVar("var1")))
                    ));
                }
            },
            new Object() {
                public int byteCode(int i, int x) {
                    return i + x;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(Arrays.asList(
                        DomFactory.ret(DomFactory.add(DomFactory.accessVar("i"), DomFactory.accessVar("x")))
                    ));
                }

                public StatementDom unpreparedExpectedTree() {
                    return DomFactory.block(Arrays.asList(
                        DomFactory.ret(DomFactory.add(DomFactory.accessVar("arg0"), DomFactory.accessVar("arg1")))
                    ));
                }
            },*/
            // *** If-else-statements with ieq compare condition inside methods that return non-void value ***
            new Object() {
                private int i;

                public int byteCode() {
                    int i;

                    if(this.i == 1) {
                        i = 1;
                    } else {
                        i = 0;
                    }

                    return i;
                }

                public StatementDom expectedTree() {
                    Object ifFalse = new Object();
                    Object end = new Object();

                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "i"),
                        DomFactory.ifElse(
                            DomFactory.compare(DomFactory.accessField(DomFactory.self(), "i", Descriptor.INT), DomFactory.literal(1), RelationalOperator.NE),
                            DomFactory.goTo(ifFalse),
                            DomFactory.block(Arrays.asList())
                        ),
                        DomFactory.assignVar("i", DomFactory.literal(1)),
                        DomFactory.goTo(end),
                        DomFactory.mark(ifFalse),
                        DomFactory.assignVar("i", DomFactory.literal(0)),
                        DomFactory.mark(end),
                        DomFactory.ret(DomFactory.accessVar("i"))
                    ));
                }
            },
            new Object() {
                private int i;

                public int byteCode() {
                    if(this.i == 1) {
                        return 1;
                    } else {
                        return 0;
                    }
                }

                public StatementDom expectedTree() {
                    Object ifFalse = new Object();

                    return DomFactory.block(Arrays.asList(
                        DomFactory.ifElse(
                            DomFactory.compare(DomFactory.accessField(DomFactory.self(), "i", Descriptor.INT), DomFactory.literal(1), RelationalOperator.NE),
                            DomFactory.goTo(ifFalse),
                            DomFactory.block(Arrays.asList())
                        ),
                        DomFactory.ret(DomFactory.literal(1)),
                        DomFactory.mark(ifFalse),
                        DomFactory.ret(DomFactory.literal(0))
                    ));
                }
            },
            new Object() {
                private int i;

                public int byteCode() {
                    if(this.i == 1) {
                        return 1;
                    }

                    return 0;
                }

                public StatementDom expectedTree() {
                    Object ifFalse = new Object();

                    return DomFactory.block(Arrays.asList(
                        DomFactory.ifElse(
                            DomFactory.compare(DomFactory.accessField(DomFactory.self(), "i", Descriptor.INT), DomFactory.literal(1), RelationalOperator.NE),
                            DomFactory.goTo(ifFalse),
                            DomFactory.block(Arrays.asList())
                        ),
                        DomFactory.ret(DomFactory.literal(1)),
                        DomFactory.mark(ifFalse),
                        DomFactory.ret(DomFactory.literal(0))
                    ));
                }
            },
            new Object() {
                private int i;

                public int byteCode() {
                    int i = 1;

                    if(this.i == 1) {
                        return i;
                    } else {
                        i = 0;
                    }

                    return i;
                }

                public StatementDom expectedTree() {
                    Object ifFalse = new Object();

                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "i"),
                        DomFactory.assignVar("i", DomFactory.literal(1)),
                        DomFactory.ifElse(
                            DomFactory.compare(DomFactory.accessField(DomFactory.self(), "i", Descriptor.INT), DomFactory.literal(1), RelationalOperator.NE),
                            DomFactory.goTo(ifFalse),
                            DomFactory.block(Arrays.asList())
                        ),
                        DomFactory.ret(DomFactory.accessVar("i")),
                        DomFactory.mark(ifFalse),
                        DomFactory.assignVar("i", DomFactory.literal(0)),
                        DomFactory.ret(DomFactory.accessVar("i"))
                    ));
                }
            },
            new Object() {
                private int i;

                public int byteCode() {
                    int i = 1;

                    if(this.i == 1) {
                        i = 0;
                    } else {
                        return i;
                    }

                    return i;
                }

                public StatementDom expectedTree() {
                    Object ifFalse = new Object();
                    Object end = new Object();

                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "i"),
                        DomFactory.assignVar("i", DomFactory.literal(1)),
                        DomFactory.ifElse(
                            DomFactory.compare(DomFactory.accessField(DomFactory.self(), "i", Descriptor.INT), DomFactory.literal(1), RelationalOperator.NE),
                            DomFactory.goTo(ifFalse),
                            DomFactory.block(Arrays.asList())
                        ),
                        DomFactory.assignVar("i", DomFactory.literal(0)),
                        DomFactory.goTo(end),
                        DomFactory.mark(ifFalse),
                        DomFactory.ret(DomFactory.accessVar("i")),
                        DomFactory.mark(end),
                        DomFactory.ret(DomFactory.accessVar("i"))
                    ));
                }
            },
            new Object() {
                private int i;

                public int byteCode() {
                    int i = 0;

                    if(this.i == 1) {
                        i = 1;
                    }

                    return i;
                }

                public StatementDom expectedTree() {
                    Object end = new Object();

                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "i"),
                        DomFactory.assignVar("i", DomFactory.literal(0)),
                        DomFactory.ifElse(
                            DomFactory.compare(DomFactory.accessField(DomFactory.self(), "i", Descriptor.INT), DomFactory.literal(1), RelationalOperator.NE),
                            DomFactory.goTo(end),
                            DomFactory.block(Arrays.asList())
                        ),
                        DomFactory.assignVar("i", DomFactory.literal(1)),
                        DomFactory.mark(end),
                        DomFactory.ret(DomFactory.accessVar("i"))
                    ));
                }
            },
            // *** If-else-statements with ieq compare condition inside methods that return void ***
            new Object() {
                private int i;

                public void byteCode() {
                    int i;

                    if(this.i == 1) {
                        i = 1;
                    } else {
                        i = 0;
                    }

                    return;
                }

                public StatementDom expectedTree() {
                    Object ifFalse = new Object();
                    Object end = new Object();

                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "i"),
                        DomFactory.ifElse(
                            DomFactory.compare(DomFactory.accessField(DomFactory.self(), "i", Descriptor.INT), DomFactory.literal(1), RelationalOperator.NE),
                            DomFactory.goTo(ifFalse),
                            DomFactory.block(Arrays.asList())
                        ),
                        DomFactory.assignVar("i", DomFactory.literal(1)),
                        DomFactory.goTo(end),
                        DomFactory.mark(ifFalse),
                        DomFactory.assignVar("i", DomFactory.literal(0)),
                        DomFactory.mark(end),
                        DomFactory.ret()
                    ));
                }
            },
            new Object() {
                private int i;

                public void byteCode() {
                    if(this.i == 1) {
                        return;
                    } else {
                        return;
                    }
                }

                public StatementDom expectedTree() {
                    Object ifFalse = new Object();

                    return DomFactory.block(Arrays.asList(
                        DomFactory.ifElse(
                            DomFactory.compare(DomFactory.accessField(DomFactory.self(), "i", Descriptor.INT), DomFactory.literal(1), RelationalOperator.NE),
                            DomFactory.goTo(ifFalse),
                            DomFactory.block(Arrays.asList())
                        ),
                        DomFactory.ret(),
                        DomFactory.mark(ifFalse),
                        DomFactory.ret()
                    ));
                }
            },
            new Object() {
                private int i;

                public void byteCode() {
                    if(this.i == 1) {
                        return;
                    }

                    return;
                }

                public StatementDom expectedTree() {
                    Object ifFalse = new Object();

                    return DomFactory.block(Arrays.asList(
                        DomFactory.ifElse(
                            DomFactory.compare(DomFactory.accessField(DomFactory.self(), "i", Descriptor.INT), DomFactory.literal(1), RelationalOperator.NE),
                            DomFactory.goTo(ifFalse),
                            DomFactory.block(Arrays.asList())
                        ),
                        DomFactory.ret(),
                        DomFactory.mark(ifFalse),
                        DomFactory.ret()
                    ));
                }
            },
            new Object() {
                private int i;

                public void byteCode() {
                    int i = 1;

                    if(this.i == 1) {
                        return;
                    } else {
                        i = 0;
                    }

                    return;
                }

                public StatementDom expectedTree() {
                    Object ifFalse = new Object();

                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "i"),
                        DomFactory.assignVar("i", DomFactory.literal(1)),
                        DomFactory.ifElse(
                            DomFactory.compare(DomFactory.accessField(DomFactory.self(), "i", Descriptor.INT), DomFactory.literal(1), RelationalOperator.NE),
                            DomFactory.goTo(ifFalse),
                            DomFactory.block(Arrays.asList())
                        ),
                        DomFactory.ret(),
                        DomFactory.mark(ifFalse),
                        DomFactory.assignVar("i", DomFactory.literal(0)),
                        DomFactory.ret()
                    ));
                }
            },
            new Object() {
                private int i;

                public void byteCode() {
                    int i = 1;

                    if(this.i == 1) {
                        i = 0;
                    } else {
                        return;
                    }

                    return;
                }

                public StatementDom expectedTree() {
                    Object ifFalse = new Object();
                    Object end = new Object();

                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "i"),
                        DomFactory.assignVar("i", DomFactory.literal(1)),
                        DomFactory.ifElse(
                            DomFactory.compare(DomFactory.accessField(DomFactory.self(), "i", Descriptor.INT), DomFactory.literal(1), RelationalOperator.NE),
                            DomFactory.goTo(ifFalse),
                            DomFactory.block(Arrays.asList())
                        ),
                        DomFactory.assignVar("i", DomFactory.literal(0)),
                        DomFactory.goTo(end),
                        DomFactory.mark(ifFalse),
                        DomFactory.ret(),
                        DomFactory.mark(end),
                        DomFactory.ret()
                    ));
                }
            },
            new Object() {
                private int i;

                public void byteCode() {
                    int i = 0;

                    if(this.i == 1) {
                        i = 1;
                    }

                    return;
                }

                public StatementDom expectedTree() {
                    Object end = new Object();

                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "i"),
                        DomFactory.assignVar("i", DomFactory.literal(0)),
                        DomFactory.ifElse(
                            DomFactory.compare(DomFactory.accessField(DomFactory.self(), "i", Descriptor.INT), DomFactory.literal(1), RelationalOperator.NE),
                            DomFactory.goTo(end),
                            DomFactory.block(Arrays.asList())
                        ),
                        DomFactory.assignVar("i", DomFactory.literal(1)),
                        DomFactory.mark(end),
                        DomFactory.ret()
                    ));
                }
            }/*,
            // *** If-else-statements (same structure) with non-eq compare condition inside methods that return void ***
            new Object() {
                private int i;

                public void byteCode() {
                    int i;

                    if(this == this) {
                        i = 1;
                    } else {
                        i = 0;
                    }

                    return;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "i"),
                        DomFactory.ifElse(
                            DomFactory.compare(DomFactory.self(), DomFactory.self(), RelationalOperator.EQ),
                            DomFactory.block(Arrays.asList(DomFactory.assignVar("i", DomFactory.literal(1)))),
                            DomFactory.block(Arrays.asList(DomFactory.assignVar("i", DomFactory.literal(0))))
                        ),
                        DomFactory.ret()
                    ));
                }
            },
            new Object() {
                private int i;

                public void byteCode() {
                    int i;

                    if(this != this) {
                        i = 1;
                    } else {
                        i = 0;
                    }

                    return;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "i"),
                        DomFactory.ifElse(
                            DomFactory.compare(DomFactory.self(), DomFactory.self(), RelationalOperator.NE),
                            DomFactory.block(Arrays.asList(DomFactory.assignVar("i", DomFactory.literal(1)))),
                            DomFactory.block(Arrays.asList(DomFactory.assignVar("i", DomFactory.literal(0))))
                        ),
                        DomFactory.ret()
                    ));
                }
            },
            new Object() {
                private int i;

                public void byteCode() {
                    int i;

                    if(this == null) {
                        i = 1;
                    } else {
                        i = 0;
                    }

                    return;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "i"),
                        DomFactory.ifElse(
                            DomFactory.compare(DomFactory.self(), DomFactory.nil(), RelationalOperator.EQ),
                            DomFactory.block(Arrays.asList(DomFactory.assignVar("i", DomFactory.literal(1)))),
                            DomFactory.block(Arrays.asList(DomFactory.assignVar("i", DomFactory.literal(0))))
                        ),
                        DomFactory.ret()
                    ));
                }
            },
            new Object() {
                private int i;

                public void byteCode() {
                    int i;

                    if(this != null) {
                        i = 1;
                    } else {
                        i = 0;
                    }

                    return;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "i"),
                        DomFactory.ifElse(
                            DomFactory.compare(DomFactory.self(), DomFactory.nil(), RelationalOperator.NE),
                            DomFactory.block(Arrays.asList(DomFactory.assignVar("i", DomFactory.literal(1)))),
                            DomFactory.block(Arrays.asList(DomFactory.assignVar("i", DomFactory.literal(0))))
                        ),
                        DomFactory.ret()
                    ));
                }
            },
            new Object() {
                private int i;

                public void byteCode() {
                    int i;

                    if(this.i != 1) {
                        i = 1;
                    } else {
                        i = 0;
                    }

                    return;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "i"),
                        DomFactory.ifElse(
                            DomFactory.compare(DomFactory.accessField(DomFactory.self(), "i", Descriptor.INT), DomFactory.literal(1), RelationalOperator.NE),
                            DomFactory.block(Arrays.asList(DomFactory.assignVar("i", DomFactory.literal(1)))),
                            DomFactory.block(Arrays.asList(DomFactory.assignVar("i", DomFactory.literal(0))))
                        ),
                        DomFactory.ret()
                    ));
                }
            },
            new Object() {
                private int i;

                public void byteCode() {
                    int i;

                    if(this.i < 1) {
                        i = 1;
                    } else {
                        i = 0;
                    }

                    return;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "i"),
                        DomFactory.ifElse(
                            DomFactory.compare(DomFactory.accessField(DomFactory.self(), "i", Descriptor.INT), DomFactory.literal(1), RelationalOperator.LT),
                            DomFactory.block(Arrays.asList(DomFactory.assignVar("i", DomFactory.literal(1)))),
                            DomFactory.block(Arrays.asList(DomFactory.assignVar("i", DomFactory.literal(0))))
                        ),
                        DomFactory.ret()
                    ));
                }
            },
            new Object() {
                private int i;

                public void byteCode() {
                    int i;

                    if(this.i <= 1) {
                        i = 1;
                    } else {
                        i = 0;
                    }

                    return;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "i"),
                        DomFactory.ifElse(
                            DomFactory.compare(DomFactory.accessField(DomFactory.self(), "i", Descriptor.INT), DomFactory.literal(1), RelationalOperator.LE),
                            DomFactory.block(Arrays.asList(DomFactory.assignVar("i", DomFactory.literal(1)))),
                            DomFactory.block(Arrays.asList(DomFactory.assignVar("i", DomFactory.literal(0))))
                        ),
                        DomFactory.ret()
                    ));
                }
            },
            new Object() {
                private int i;

                public void byteCode() {
                    int i;

                    if(this.i > 1) {
                        i = 1;
                    } else {
                        i = 0;
                    }

                    return;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "i"),
                        DomFactory.ifElse(
                            DomFactory.compare(DomFactory.accessField(DomFactory.self(), "i", Descriptor.INT), DomFactory.literal(1), RelationalOperator.GT),
                            DomFactory.block(Arrays.asList(DomFactory.assignVar("i", DomFactory.literal(1)))),
                            DomFactory.block(Arrays.asList(DomFactory.assignVar("i", DomFactory.literal(0))))
                        ),
                        DomFactory.ret()
                    ));
                }
            },
            new Object() {
                private int i;

                public void byteCode() {
                    int i;

                    if(this.i >= 1) {
                        i = 1;
                    } else {
                        i = 0;
                    }

                    return;
                }

                public StatementDom expectedTree() {
                    return DomFactory.block(Arrays.asList(
                        DomFactory.declareVar(Descriptor.INT, "i"),
                        DomFactory.ifElse(
                            DomFactory.compare(DomFactory.accessField(DomFactory.self(), "i", Descriptor.INT), DomFactory.literal(1), RelationalOperator.GE),
                            DomFactory.block(Arrays.asList(DomFactory.assignVar("i", DomFactory.literal(1)))),
                            DomFactory.block(Arrays.asList(DomFactory.assignVar("i", DomFactory.literal(0))))
                        ),
                        DomFactory.ret()
                    ));
                }
            }*/
        ).stream().map(x -> load(x)).collect(Collectors.toList());
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

        if(expectedUnpreparedStatement != null) {
            byteCodeToTree = new ByteCodeToTree(methodNode);
            methodNode.accept(byteCodeToTree);
            actualStatement = byteCodeToTree.getBlock();
            assertEquals(expectedUnpreparedStatement, actualStatement);
        }
    }
}