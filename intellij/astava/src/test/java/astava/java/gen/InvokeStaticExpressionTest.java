package astava.java.gen;

import astava.core.Tuple;
import astava.java.Descriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static astava.CommonTest.testExpression;
import static astava.java.Factory.invokeStatic;
import static astava.java.Factory.literal;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class InvokeStaticExpressionTest {
    public static int vToI() {
        return 7;
    }

    public static void vToV() {

    }

    private Class<?> type;
    private String name;
    private List<Class<?>> parameterTypes;
    private Class<?> returnType;
    private List<LiteralTest.LiteralProvider> argumentProviders;
    private Class<? extends Exception> expectedExceptionType;

    public InvokeStaticExpressionTest(Class<?> type, String name, List<Class<?>> parameterTypes, Class<?> returnType, List<LiteralTest.LiteralProvider> argumentProviders, Class<? extends Exception> expectedExceptionType) {
        this.type = type;
        this.name = name;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.argumentProviders = argumentProviders;
        this.expectedExceptionType = expectedExceptionType;
    }

    @Parameterized.Parameters
    public static Collection values() {
        return Arrays.asList(
            new Object[] {
                Math.class, "round", Arrays.asList(double.class), long.class, Arrays.asList(new LiteralTest.DoubleProvider(1.5)), null
            },
            new Object[] {
                Math.class, "abs", Arrays.asList(double.class), double.class, Arrays.asList(new LiteralTest.DoubleProvider(1.5)), null
            },
            new Object[] {
                InvokeStaticExpressionTest.class, "vToI", Arrays.asList(), int.class, Arrays.asList(), null
            },
            new Object[] {
                InvokeStaticExpressionTest.class, "vToV", Arrays.asList(), void.class, Arrays.asList(), IllegalArgumentException.class
            }
        );
    }

    @Test
    public void testInvokeStatic() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = type.getMethod(name, parameterTypes.toArray(new Class<?>[parameterTypes.size()]));
        Object[] arguments = argumentProviders.stream().map(ap -> ap.getValue()).toArray(s -> new Object[s]);

        Object expectedResult = method.invoke(null, arguments);

        Tuple expression = invokeStatic(
            Descriptor.get(type),
            name,
            Descriptor.getMethodDescriptor(parameterTypes, returnType),
            Arrays.asList(literal(1.5)));

        try {
            testExpression(expression, Descriptor.get(returnType), actualValue -> {
                assertEquals(expectedResult, actualValue);
            });

            if(expectedExceptionType != null)
                fail();
        } catch (Exception e) {
            if(!expectedExceptionType.isInstance(e))
                throw e;
        }
    }
}
