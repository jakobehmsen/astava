package astava.java.gen;

import astava.CommonTest;
import astava.core.Tuple;
import astava.java.Descriptor;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static astava.java.Factory.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TernaryTest {
    @Test
    public void testTernaryTrue() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Tuple ast = ternary(literal(true), literal(true), literal(false));

        CommonTest.testExpression(ast, Descriptor.BOOLEAN, actualValue ->
            assertTrue((boolean) actualValue));
    }

    @Test
    public void testTernaryFalse() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Tuple ast = ternary(literal(false), literal(true), literal(false));

        CommonTest.testExpression(ast, Descriptor.BOOLEAN, actualValue ->
            assertFalse((boolean) actualValue));
    }

    @Test
    public void testTernaryTrueThenTrue() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Tuple ast = ternary(literal(true), ternary(literal(true), literal(true), literal(false)), literal(false));

        CommonTest.testExpression(ast, Descriptor.BOOLEAN, actualValue ->
            assertTrue((boolean) actualValue));
    }
    @Test
    public void testTernaryTrueThenFalse() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Tuple ast = ternary(literal(true), ternary(literal(false), literal(true), literal(false)), literal(false));

        CommonTest.testExpression(ast, Descriptor.BOOLEAN, actualValue ->
            assertFalse((boolean) actualValue));
    }

    @Test
    public void testTernaryFalseThenFalse() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Tuple ast = ternary(literal(false), literal(true), ternary(literal(false), literal(true), literal(false)));

        CommonTest.testExpression(ast, Descriptor.BOOLEAN, actualValue ->
            assertFalse((boolean) actualValue));
    }

    @Test
    public void testTernaryFalseThenTrue() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Tuple ast = ternary(literal(false), literal(true), ternary(literal(true), literal(true), literal(false)));

        CommonTest.testExpression(ast, Descriptor.BOOLEAN, actualValue ->
            assertTrue((boolean) actualValue));
    }
}
