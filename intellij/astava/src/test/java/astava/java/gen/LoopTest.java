package astava.java.gen;

import astava.core.Tuple;
import astava.java.Descriptor;
import astava.java.IncDec;
import astava.java.RelationalOperator;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static astava.CommonTest.testMethodBody;
import static astava.java.Factory.*;
import static org.junit.Assert.assertEquals;

public class LoopTest {
    @Test
    public void testLoop() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        int repeat = 10;
        Object expectedValue = repeat;

        Tuple ast = block(Arrays.asList(
            declareVar(Descriptor.INT, "i"),
            assignVar("i", literal(0)),
            loop(compare(accessVar("i"), literal(repeat), RelationalOperator.LT),
                intIncVar("i", IncDec.TIMING_POST, 1)
            ),
            ret(accessVar("i"))
        ));

        testMethodBody(ast, Descriptor.INT, actualValue ->
                assertEquals(expectedValue, actualValue));
    }
}
