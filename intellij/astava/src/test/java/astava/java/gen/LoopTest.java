package astava.java.gen;

import astava.core.Tuple;
import astava.java.Descriptor;
import astava.java.RelationalOperator;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;

import static astava.CommonTest.testMethodBody;
import static astava.java.Factory.*;
import static org.junit.Assert.assertEquals;

public class LoopTest {
    private Tuple whileLoop(Tuple condition, Tuple body) {
        return loop(
            ifElse(condition, block(Arrays.asList(body, cnt())), brk())
        );
    }

    @Test
    public void testLoop() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        int repeat = 10;
        Object expectedValue = repeat;

        Tuple ast = block(Arrays.asList(
            declareVar(Descriptor.INT, "i"),
            assignVar("i", literal(0)),
            whileLoop(lt(accessVar("i"), literal(repeat)),
                intIncVar("i", 1)
            ),
            ret(accessVar("i"))
        ));

        testMethodBody(ast, Descriptor.INT, actualValue ->
            assertEquals(expectedValue, actualValue));
    }

    @Test
    public void testLoopWithBreak() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        int repeat = 10;
        Object expectedValue = repeat;

        Tuple b = brk();

        Tuple ast = block(Arrays.asList(
            declareVar(Descriptor.INT, "i"),
            assignVar("i", literal(0)),
            whileLoop(literal(true),
                block(Arrays.asList(
                    ifElse(ge(accessVar("i"), literal(repeat)),
                        brk(),
                        block(Collections.emptyList())),
                    intIncVar("i", 1)
                ))
            ),
            ret(accessVar("i"))
        ));

        testMethodBody(ast, Descriptor.INT, actualValue ->
            assertEquals(expectedValue, actualValue));
    }

    @Test
    public void testLoopWithContinue() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        int repeat = 10;
        Object expectedValue = repeat;

        Tuple b = brk();

        Tuple ast = block(Arrays.asList(
            declareVar(Descriptor.INT, "i"),
            assignVar("i", literal(0)),
            whileLoop(literal(true),
                block(Arrays.asList(
                    intIncVar("i", 1),
                    ifElse(lt(accessVar("i"), literal(repeat)),
                        cnt(),
                        block(Collections.emptyList())),
                    brk()
                ))
            ),
            ret(accessVar("i"))
        ));

        testMethodBody(ast, Descriptor.INT, actualValue ->
            assertEquals(expectedValue, actualValue));
    }
}
