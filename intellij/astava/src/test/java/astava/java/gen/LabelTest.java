package astava.java.gen;

import astava.tree.StatementDom;
import astava.java.Descriptor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static astava.CommonTest.testMethodBody;
import static astava.CommonTest.whileLoop;
import static astava.java.DomFactory.*;
import static org.junit.Assert.assertEquals;

public class LabelTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testSimpleGoTo() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String labelName = "l";
        int expectedValue = 1;

        StatementDom methodBody = block(Arrays.asList(
            goTo(labelName),
            ret(literal(0)), // This shouldn't be reached
            label(labelName),
            ret(literal(expectedValue))
        ));

        testMethodBody(methodBody, Descriptor.INT, actualValue -> assertEquals(expectedValue, actualValue));
    }

    @Test
    public void testGoToUnsetFails() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String labelName = "l";
        int expectedValue = 1;

        StatementDom methodBody = block(Arrays.asList(
            goTo(labelName),
            ret(literal(expectedValue))
        ));

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Unset labels were used");
        testMethodBody(methodBody, Descriptor.INT, actualValue -> assertEquals(expectedValue, actualValue));
    }

    @Test
    public void testMultiSetFails() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String labelName = "l";
        int expectedValue = 1;

        StatementDom methodBody = block(Arrays.asList(
            goTo(labelName),
            ret(literal(0)),
            label(labelName),
            label(labelName),
            ret(literal(expectedValue))
        ));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("is already set");
        testMethodBody(methodBody, Descriptor.INT, actualValue -> assertEquals(expectedValue, actualValue));
    }

    @Test
    public void testLoop() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        int count = 10;
        int expectedValue = count;

        StatementDom methodBody = block(Arrays.asList(
            declareVar(Descriptor.INT, "i"),
            assignVar("i", literal(0)),
            whileLoop(lt(accessVar("i"), literal(count)), intIncVar("i", 1)),
            ret(accessVar("i"))
        ));

        testMethodBody(methodBody, Descriptor.INT, actualValue -> assertEquals(expectedValue, actualValue));
    }
}
