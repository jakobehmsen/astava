package astava.java.gen;

import astava.CommonTestDom;
import astava.tree.ExpressionDom;
import astava.tree.Tuple;
import astava.java.Descriptor;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static astava.java.FactoryDom.instanceOf;
import static astava.java.FactoryDom.literal;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InstanceOfTest {
    @Test
    public void testInstanceOfTrue() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ExpressionDom ast = instanceOf(literal("str"), Descriptor.STRING);

        CommonTestDom.testExpression(ast, Descriptor.BOOLEAN, actualValue ->
            assertTrue((boolean)actualValue));
    }

    @Test
    public void testInstanceOfFalse() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ExpressionDom ast = instanceOf(literal("str"), Descriptor.get(Number.class));

        CommonTestDom.testExpression(ast, Descriptor.BOOLEAN, actualValue ->
            assertFalse((boolean)actualValue));
    }
}
