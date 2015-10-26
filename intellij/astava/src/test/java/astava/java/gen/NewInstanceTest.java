package astava.java.gen;

import astava.tree.ExpressionDom;
import astava.tree.StatementDom;
import astava.java.Descriptor;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;

import static astava.CommonTest.testExpression;
import static astava.CommonTest.testMethodBody;
import static astava.CommonTest.whileLoop;
import static astava.java.DomFactory.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NewInstanceTest {
    public static class ToInstantiate {
        public static int instantiationCount;

        public ToInstantiate() {
            instantiationCount++;
        }
    }

    @Test
    public void testNewInstanceExpression() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ExpressionDom expression = newInstanceExpr(
            Descriptor.get(StringBuilder.class),
            Collections.emptyList(),
            Collections.emptyList()
        );

        testExpression(expression, Descriptor.get(StringBuilder.class), actualValue -> {
            assertTrue(actualValue instanceof StringBuilder);
        });
    }

    @Test
    public void testNewInstanceStatement() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // Unused return occurrences should implicitly be popped
        // This is asserted inserting an instantiation within a loop in which verification is performed.
        // I.e., if the return occurrences isn't implicitly popped, an exception is thrown.

        int count = 10;
        StatementDom instantiation = newInstance(
            Descriptor.get(ToInstantiate.class),
            Collections.emptyList(),
            Collections.emptyList()
        );

        StatementDom methodBody = block(Arrays.asList(
            declareVar(Descriptor.INT, "i"),
            assignVar("i", literal(0)),
            whileLoop(lt(accessVar("i"), literal(count)), block(Arrays.asList(
                instantiation,
                intIncVar("i", 1)
            ))),
            ret()
        ));

        testMethodBody(methodBody, Descriptor.VOID, actualValue -> {
            assertEquals(count, ToInstantiate.instantiationCount);
        });
    }
}
