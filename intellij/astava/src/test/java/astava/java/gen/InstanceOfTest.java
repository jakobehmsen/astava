package astava.java.gen;

import astava.CommonTest;
import astava.tree.ExpressionDom;
import astava.java.Descriptor;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static astava.java.DomFactory.instanceOf;
import static astava.java.DomFactory.literal;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InstanceOfTest {
    @Test
    public void testInstanceOfTrue() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ExpressionDom ast = instanceOf(literal("str"), Descriptor.STRING);

        CommonTest.testExpression(ast, Descriptor.BOOLEAN, actualValue ->
            assertTrue((boolean) actualValue));
    }

    @Test
    public void testInstanceOfFalse() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ExpressionDom ast = instanceOf(literal("str"), Descriptor.get(Number.class));

        CommonTest.testExpression(ast, Descriptor.BOOLEAN, actualValue ->
            assertFalse((boolean) actualValue));
    }
}
