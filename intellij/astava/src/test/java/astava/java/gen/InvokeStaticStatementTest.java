package astava.java.gen;

import astava.java.Descriptor;
import astava.tree.StatementDom;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static astava.CommonTest.testMethodBody;
import static astava.CommonTest.whileLoop;
import static astava.java.DomFactory.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InvokeStaticStatementTest {
    private static boolean vToVWasInvoked;

    public static void vToV() {
        vToVWasInvoked = true;
    }

    private static boolean vToIWasInvoked;

    public static int vToI() {
        vToIWasInvoked = true;
        return 0;
    }

    private static int vToIWasInvokeCount2;

    public static int vToI2() {
        vToIWasInvokeCount2++;
        return 0;
    }

    @Test
    public void testInvokeStaticVoid() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        StatementDom methodBody = block(Arrays.asList(
            invokeStatic(Descriptor.get(InvokeStaticStatementTest.class), "vToV", Descriptor.getMethodDescriptor(Arrays.asList(), Descriptor.VOID), Arrays.asList()),
            ret()
        ));

        testMethodBody(methodBody, Descriptor.VOID, actualValue -> {
            assertTrue(vToVWasInvoked);
        });
    }

    @Test
    public void testInvokeStaticInt() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        StatementDom methodBody = block(Arrays.asList(
            invokeStatic(Descriptor.get(InvokeStaticStatementTest.class), "vToI", Descriptor.getMethodDescriptor(Arrays.asList(), Descriptor.INT), Arrays.asList()),
            ret()
        ));

        testMethodBody(methodBody, Descriptor.VOID, actualValue -> {
            assertTrue(vToIWasInvoked);
        });
    }

    @Test
    public void testInvokeStaticIntWithinLoop() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // Unused return occurrences should implicitly be popped
        // This is asserted inserting a static invocation within a loop in which verification is performed.
        // I.e., if the return occurrences isn't implicitly popped, an exception is thrown.

        int count = 10;
        StatementDom invocation = invokeStatic(Descriptor.get(InvokeStaticStatementTest.class), "vToI2", Descriptor.getMethodDescriptor(Arrays.asList(), Descriptor.INT), Arrays.asList());

        StatementDom methodBody = block(Arrays.asList(
            declareVar(Descriptor.INT, "i"),
            assignVar("i", literal(0)),
            whileLoop(lt(accessVar("i"), literal(count)), block(Arrays.asList(
                invocation,
                intIncVar("i", 1)
            ))),
            ret()
        ));

        testMethodBody(methodBody, Descriptor.VOID, actualValue -> {
            assertEquals(count, vToIWasInvokeCount2);
        });
    }
}
