package astava.java.gen;

import astava.CommonTestDom;
import astava.tree.StatementDom;
import astava.tree.Tuple;
import astava.java.Descriptor;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static astava.java.FactoryDom.*;
import static org.junit.Assert.assertEquals;

public class SwitchTest {
    @Test
    public void testSwitchFirstCase() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        StatementDom ast = block(Arrays.asList(
            declareVar(Descriptor.INT, "result"),
            select(literal(0),
                Arrays.asList(
                    option(0, block(Arrays.asList(assignVar("result", literal(0)), breakOption()))),
                    option(1, block(Arrays.asList(assignVar("result", literal(1)), breakOption()))),
                    option(2, block(Arrays.asList(assignVar("result", literal(2)), breakOption())))
                ),
                assignVar("result", literal(-1))
            ),
            ret(accessVar("result"))
        ));

        CommonTestDom.testMethodBody(ast, Descriptor.INT, actualValue ->
            assertEquals(0, actualValue));
    }

    @Test
    public void testSwitchSecondCase() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        StatementDom ast = block(Arrays.asList(
            declareVar(Descriptor.INT, "result"),
            select(literal(1),
                Arrays.asList(
                    option(0, block(Arrays.asList(assignVar("result", literal(0)), breakOption()))),
                    option(1, block(Arrays.asList(assignVar("result", literal(1)), breakOption()))),
                    option(2, block(Arrays.asList(assignVar("result", literal(2)), breakOption())))
                ),
                assignVar("result", literal(-1))
            ),
            ret(accessVar("result"))
        ));

        CommonTestDom.testMethodBody(ast, Descriptor.INT, actualValue ->
            assertEquals(1, actualValue));
    }

    @Test
    public void testSwitchThirdCase() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        StatementDom ast = block(Arrays.asList(
            declareVar(Descriptor.INT, "result"),
            select(literal(2),
                Arrays.asList(
                    option(0, block(Arrays.asList(assignVar("result", literal(0)), breakOption()))),
                    option(1, block(Arrays.asList(assignVar("result", literal(1)), breakOption()))),
                    option(2, block(Arrays.asList(assignVar("result", literal(2)), breakOption())))
                ),
                assignVar("result", literal(-1))
            ),
            ret(accessVar("result"))
        ));

        CommonTestDom.testMethodBody(ast, Descriptor.INT, actualValue ->
            assertEquals(2, actualValue));
    }

    @Test
    public void testSwitchFirstToThirdCase() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        StatementDom ast = block(Arrays.asList(
            declareVar(Descriptor.INT, "result"),
            select(literal(0),
                Arrays.asList(
                    option(0, block(Arrays.asList(assignVar("result", literal(0))))), // Fallthrough
                    option(1, block(Arrays.asList(assignVar("result", literal(1))))), // Fallthrough
                    option(2, block(Arrays.asList(assignVar("result", literal(2)), breakOption())))
                ),
                assignVar("result", literal(-1))
            ),
            ret(accessVar("result"))
        ));

        CommonTestDom.testMethodBody(ast, Descriptor.INT, actualValue ->
            assertEquals(2, actualValue));
    }

    @Test
    public void testSwitchDefaultCaseByGT() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        StatementDom ast = block(Arrays.asList(
            declareVar(Descriptor.INT, "result"),
            select(literal(3),
                Arrays.asList(
                    option(0, block(Arrays.asList(assignVar("result", literal(0)), breakOption()))),
                    option(1, block(Arrays.asList(assignVar("result", literal(1)), breakOption()))),
                    option(2, block(Arrays.asList(assignVar("result", literal(2)), breakOption())))
                ),
                assignVar("result", literal(-1))
            ),
            ret(accessVar("result"))
        ));

        CommonTestDom.testMethodBody(ast, Descriptor.INT, actualValue ->
            assertEquals(-1, actualValue));
    }

    @Test
    public void testSwitchDefaultCaseByLT() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        StatementDom ast = block(Arrays.asList(
            declareVar(Descriptor.INT, "result"),
            select(literal(-1),
                Arrays.asList(
                    option(0, block(Arrays.asList(assignVar("result", literal(0)), breakOption()))),
                    option(1, block(Arrays.asList(assignVar("result", literal(1)), breakOption()))),
                    option(2, block(Arrays.asList(assignVar("result", literal(2)), breakOption())))
                ),
                assignVar("result", literal(-1))
            ),
            ret(accessVar("result"))
        ));

        CommonTestDom.testMethodBody(ast, Descriptor.INT, actualValue ->
            assertEquals(-1, actualValue));
    }
}
