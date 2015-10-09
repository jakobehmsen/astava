package astava.java.gen;

import astava.java.Descriptor;
import astava.tree.StatementDom;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;

import static astava.CommonTest.testMethodBody;
import static astava.CommonTest.whileLoop;
import static astava.java.DomFactory.*;
import static org.junit.Assert.assertEquals;

public class InvokeInstanceStatementTest {
    public static class InvokeInstanceVirtual {
        public int vToVWasInvokeCount;

        public void vToV() {
            vToVWasInvokeCount++;
        }
    }

    public interface InvokeInstanceInterface {
        void vToV();
    }

    public static class InvokeInstanceImpl implements InvokeInstanceInterface {
        public int vToVWasInvokeCount;

        @Override
        public void vToV() {
            vToVWasInvokeCount++;
        }
    }

    @Test
    public void testInvokeVirtualWithinLoop() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // Unused return value should implicitly be popped
        // This is asserted inserting a static invocation within a loop in which verification is performed.
        // I.e., if the return value isn't implicitly popped, an exception is thrown.

        int count = 10;
        StatementDom invocation = invokeVirtual(
            Descriptor.get(InvokeInstanceVirtual.class),
            "vToV",
            Descriptor.getMethodDescriptor(Arrays.asList(), Descriptor.VOID),
            accessVar("target"),
            Arrays.asList());

        StatementDom methodBody = block(Arrays.asList(
            declareVar(Descriptor.get(InvokeInstanceVirtual.class), "target"),
            assignVar("target", newInstanceExpr(Descriptor.get(InvokeInstanceVirtual.class), Collections.emptyList(), Collections.emptyList())),
            declareVar(Descriptor.INT, "i"),
            assignVar("i", literal(0)),
            whileLoop(lt(accessVar("i"), literal(count)), block(Arrays.asList(
                invocation,
                intIncVar("i", 1)
            ))),
            ret(accessVar("target"))
        ));

        testMethodBody(methodBody, Descriptor.get(InvokeInstanceVirtual.class), actualValue -> {
            assertEquals(count, ((InvokeInstanceVirtual)actualValue).vToVWasInvokeCount);
        });
    }

    @Test
    public void testInvokeInterfaceWithinLoop() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // Unused return value should implicitly be popped
        // This is asserted inserting a static invocation within a loop in which verification is performed.
        // I.e., if the return value isn't implicitly popped, an exception is thrown.

        int count = 10;
        StatementDom invocation = invokeInterface(
            Descriptor.get(InvokeInstanceInterface.class),
            "vToV",
            Descriptor.getMethodDescriptor(Arrays.asList(), Descriptor.VOID),
            accessVar("target"),
            Arrays.asList());

        StatementDom methodBody = block(Arrays.asList(
            declareVar(Descriptor.get(InvokeInstanceImpl.class), "target"),
            assignVar("target", newInstanceExpr(Descriptor.get(InvokeInstanceImpl.class), Collections.emptyList(), Collections.emptyList())),
            declareVar(Descriptor.INT, "i"),
            assignVar("i", literal(0)),
            whileLoop(lt(accessVar("i"), literal(count)), block(Arrays.asList(
                    invocation,
                    intIncVar("i", 1)
            ))),
            ret(accessVar("target"))
        ));

        testMethodBody(methodBody, Descriptor.get(InvokeInstanceImpl.class), actualValue -> {
            assertEquals(count, ((InvokeInstanceImpl)actualValue).vToVWasInvokeCount);
        });
    }
}
