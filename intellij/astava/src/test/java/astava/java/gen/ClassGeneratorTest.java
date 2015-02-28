package astava.java.gen;

import astava.core.Node;
import astava.core.Tuple;
import astava.java.Descriptor;
import jdk.internal.org.objectweb.asm.Type;
import junit.framework.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import static astava.java.Factory.*;
import static astava.java.Factory.literal;
import static org.junit.Assert.*;

public class ClassGeneratorTest {
    private <T> void test(Tuple methodBody, Class<T> returnType, Consumer<T> assertion)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String methodName = "myMethod";
        String returnTypeDescriptor = Type.getDescriptor(returnType);
        Node classDeclaration = classDeclaration(Modifier.PUBLIC, "MyClass", "java/lang/Object", Arrays.asList(
            methodDeclaration(Modifier.PUBLIC | Modifier.STATIC, methodName, Collections.emptyList(), returnTypeDescriptor, methodBody)
        ));
        ClassGenerator generator = new ClassGenerator(classDeclaration);
        Object actualValue = generator.newClass().getMethod(methodName).invoke(null, null);
        assertion.accept((T)actualValue);
    }

    @Test
    public void testAddInt() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        int expectedValue = 7 + 11;
        test(ret(add(literal(7), literal(11))), int.class, actualValue -> assertEquals(expectedValue, actualValue, 0));
    }

    @Test
    public void testSubInt() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        int expectedValue = 7 - 11;
        test(ret(sub(literal(7), literal(11))), int.class, actualValue -> assertEquals(expectedValue, actualValue, 0));
    }

    @Test
    public void testMulInt() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        int expectedValue = 7 * 11;
        test(ret(mul(literal(7), literal(11))), int.class, actualValue -> assertEquals(expectedValue, actualValue, 0));
    }

    @Test
    public void testDivInt() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        int expectedValue = 10 / 2;
        test(ret(div(literal(10), literal(2))), int.class, actualValue -> assertEquals(expectedValue, actualValue, 0));
    }

    @Test
    public void testMulDouble() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        double expectedValue = 7.0 * 11.0;
        test(ret(mul(literal(7.0), literal(11.0))), double.class, actualValue -> assertEquals(expectedValue, actualValue, 0.0));
    }
}