package astava.java.gen;


import astava.core.Tuple;
import astava.java.Descriptor;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static astava.CommonTest.testMethodBody;
import static astava.java.Factory.block;
import static astava.java.Factory.invokeStatic;
import static astava.java.Factory.ret;
import static org.junit.Assert.assertTrue;

public class InvokeStaticVoidTest {
    private static boolean vToVWasInvoked;

    public static void vToV() {
        vToVWasInvoked = true;
    }

    @Test
    public void testInvokeVoid() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Tuple methodBody = block(Arrays.asList(
            invokeStatic(Descriptor.get(InvokeStaticVoidTest.class), "vToV", Descriptor.getMethodDescriptor(Arrays.asList(), Descriptor.VOID), Arrays.asList()),
            ret()
        ));

        testMethodBody(methodBody, Descriptor.VOID, actualValue -> {
            assertTrue(vToVWasInvoked);
        });
    }
}
