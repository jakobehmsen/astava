package astava.java.gen;

import astava.core.Tuple;
import astava.java.Descriptor;
import junit.framework.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import static astava.CommonTest.testExpression;
import static astava.java.Factory.newInstance;
import static org.junit.Assert.assertTrue;

public class NewInstanceTest {
    @Test
    public void testNewInstance() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Tuple expression = newInstance(
            Descriptor.get(StringBuilder.class),
            Collections.emptyList(),
            Collections.emptyList()
        );

        testExpression(expression, Descriptor.get(StringBuilder.class), actualValue -> {
            assertTrue(actualValue instanceof StringBuilder);
        });
    }
}
