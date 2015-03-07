package astava.java.gen;

import astava.core.Tuple;
import astava.java.Descriptor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static astava.CommonTest.testMethodBody;
import static astava.java.Factory.*;
import static org.junit.Assert.assertEquals;

public class LabelScopeTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testSimpleGoTo() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String labelName = "l";
        int expectedValue = 1;

        Tuple methodBody = block(Arrays.asList(
            labelScope(Arrays.asList(labelName), block(Arrays.asList(
                labelGoTo(labelName),
                ret(literal(0)), // This shouldn't be reached
                labelSet(labelName),
                ret(literal(expectedValue))
            )))
        ));

        testMethodBody(methodBody, Descriptor.INT, actualValue -> assertEquals(expectedValue, actualValue));
    }

    @Test
    public void testGoToUndeclaredFails() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String labelName = "l";
        int expectedValue = 1;

        Tuple methodBody = block(Arrays.asList(
            labelScope(Arrays.asList(), block(Arrays.asList(
                labelGoTo(labelName),
                ret(literal(0)),
                labelSet(labelName),
                ret(literal(expectedValue))
            )))
        ));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("is undeclared");
        testMethodBody(methodBody, Descriptor.INT, actualValue -> assertEquals(expectedValue, actualValue));
    }

    @Test
    public void testGoToUnsetFails() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String labelName = "l";
        int expectedValue = 1;

        Tuple methodBody = block(Arrays.asList(
            labelScope(Arrays.asList(labelName), block(Arrays.asList(
                labelGoTo(labelName),
                ret(literal(expectedValue))
            )))
        ));

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Unset labels were used");
        testMethodBody(methodBody, Descriptor.INT, actualValue -> assertEquals(expectedValue, actualValue));
    }

    @Test
    public void testGoToMultiSetFails() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String labelName = "l";
        int expectedValue = 1;

        Tuple methodBody = block(Arrays.asList(
            labelScope(Arrays.asList(labelName), block(Arrays.asList(
                labelGoTo(labelName),
                ret(literal(0)),
                labelSet(labelName),
                labelSet(labelName),
                ret(literal(expectedValue))
            )))
        ));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("is already set");
        testMethodBody(methodBody, Descriptor.INT, actualValue -> assertEquals(expectedValue, actualValue));
    }

    @Test
    public void testInheritFromParent() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String labelName = "l";
        int expectedValue = 1;

        Tuple methodBody = block(Arrays.asList(
            labelScope(Arrays.asList(labelName), block(Arrays.asList(
                labelScope(Arrays.asList(), block(Arrays.asList(
                        labelGoTo(labelName) // Label should be accessible here via parent label scope
                ))),
                ret(literal(0)),
                labelSet(labelName),
                ret(literal(expectedValue))
            )))
        ));

        testMethodBody(methodBody, Descriptor.INT, actualValue -> assertEquals(expectedValue, actualValue));
    }

    @Test
    public void testInheritFromGrandParent() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String labelName = "l";
        int expectedValue = 1;

        Tuple methodBody = block(Arrays.asList(
            labelScope(Arrays.asList(labelName), block(Arrays.asList(
                labelScope(Arrays.asList(), block(Arrays.asList(
                    labelScope(Arrays.asList(), block(Arrays.asList(
                            labelGoTo(labelName) // Label should be accessible here via grand parent label scope
                    )))
                ))),
                ret(literal(0)),
                labelSet(labelName),
                ret(literal(expectedValue))
            )))
        ));

        testMethodBody(methodBody, Descriptor.INT, actualValue -> assertEquals(expectedValue, actualValue));
    }

        /*thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Bla");
        throw new IllegalArgumentException("Bla");*/

    @Test
    public void testRedefine() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String labelName = "l";
        int expectedValue = 1;

        Tuple methodBody = block(Arrays.asList(
            labelScope(Arrays.asList(labelName), block(Arrays.asList(
                labelScope(Arrays.asList(labelName), block(Arrays.asList(
                    labelGoTo(labelName), // Redefined label should be used
                    labelSet(labelName),
                    ret(literal(expectedValue))
                ))),
                labelSet(labelName), // This shouldn't be reached
                ret(literal(0))
            )))
        ));

        testMethodBody(methodBody, Descriptor.INT, actualValue -> assertEquals(expectedValue, actualValue));
    }

    @Test
    public void testLoop() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        int count = 10;
        int expectedValue = count;

        Tuple methodBody = block(Arrays.asList(
            declareVar(Descriptor.INT, "i"),
            assignVar("i", literal(0)),
            labelScope(Arrays.asList("break", "continue"), block(Arrays.asList(
                labelSet("continue"),
                ifElse(lt(accessVar("i"), literal(count)),
                    block(Arrays.asList(
                        intIncVar("i", 1),
                        labelGoTo("continue")
                    )),
                    labelGoTo("break")
                ),
                labelSet("break")
            ))),
            ret(accessVar("i"))
        ));

        testMethodBody(methodBody, Descriptor.INT, actualValue -> assertEquals(expectedValue, actualValue));
    }
}
