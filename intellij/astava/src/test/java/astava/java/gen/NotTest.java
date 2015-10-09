package astava.java.gen;

import astava.java.Descriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;

import static astava.java.DomFactory.literal;
import static astava.java.DomFactory.not;
import static astava.CommonTest.testExpression;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class NotTest {
    private boolean value;

    public NotTest(boolean value) {
        this.value = value;
    }

    @Parameterized.Parameters
    public static Collection values() {
        return Arrays.asList(new Object[]{true}, new Object[]{false});
    }

    @Test
    public void testNot() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        boolean expectedValue = !value;

        testExpression(not(literal(value)), Descriptor.BOOLEAN, (Boolean actualValue) ->
            assertEquals(expectedValue, actualValue));
    }
}
