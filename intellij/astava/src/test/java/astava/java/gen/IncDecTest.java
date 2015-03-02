package astava.java.gen;

import astava.CommonTest;
import astava.core.Tuple;
import astava.java.Descriptor;
import astava.java.IncDec;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static astava.java.Factory.*;
import static org.junit.Assert.assertEquals;

public class IncDecTest {
    @Test
    public void testIncPre() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        int value = 10;
        int myVar = value;

        String type = Descriptor.INT;
        String name = "myVar";
        Tuple valueAST = literal(value);

        int expectedValue = ++myVar;

        Tuple ast = block(Arrays.asList(
            declareVar(type, name),
            assignVar(name, valueAST),
            ret(intIncVar(name, IncDec.TIMING_PRE, 1))
        ));

        CommonTest.testMethodBody(ast, type, actualValue ->
                assertEquals(expectedValue, actualValue));
    }
}
