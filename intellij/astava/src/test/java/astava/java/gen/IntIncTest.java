package astava.java.gen;

import astava.CommonTest;
import astava.core.Tuple;
import astava.java.Descriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static astava.java.Factory.*;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class IntIncTest {
    private int amount;

    public IntIncTest(int amount) {
        this.amount = amount;
    }

    @Parameterized.Parameters
    public static Collection values() {
        return Arrays.asList(
            new Object[]{1}, // Increment by one
            new Object[]{-1} // Decrement by one
        );
    }

    @Test
    public void testIntInc() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        int varInit = 10;
        int varMod = varInit + amount;
        int incDecExpectedValue = varMod;

        String type = Descriptor.INT;

        String varName = "myVar";
        Tuple ast = block(Arrays.asList(
            declareVar(Descriptor.INT, varName),
            assignVar(varName, literal(varInit)),
            intIncVar(varName, amount),
            ret(accessVar(varName))
        ));

        CommonTest.testMethodBody(ast, type, actualValue ->
            assertEquals(varMod, actualValue));
    }
}
