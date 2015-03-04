package astava.java.gen;

import astava.CommonTest;
import astava.core.Tuple;
import astava.java.Descriptor;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static astava.java.Factory.instanceOf;
import static astava.java.Factory.literal;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InstanceOfTest {
    @Test
    public void testInstanceOfTrue() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Tuple ast = instanceOf(literal("str"), Descriptor.STRING);

        CommonTest.testExpression(ast, Descriptor.BOOLEAN, actualValue ->
            assertTrue((boolean)actualValue));
    }

    @Test
    public void testInstanceOfFalse() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Tuple ast = instanceOf(literal("str"), Descriptor.get(Number.class));

        CommonTest.testExpression(ast, Descriptor.BOOLEAN, actualValue ->
            assertFalse((boolean)actualValue));
    }
}
