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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static astava.CommonTest.testExpression;
import static astava.CommonTest.testMethodBody;
import static astava.java.Factory.block;
import static astava.java.Factory.invokeStatic;
import static astava.java.Factory.literal;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class InvokeStaticTest<T> {
    public static int vToI() {
        return 7;
    }

    private Class<?> type;
    private String name;
    private List<Class<?>> parameterTypes;
    private Class<T> returnType;
    private List<LiteralTest.LiteralProvider> argumentProviders;

    public InvokeStaticTest(Class<?> type, String name, List<Class<?>> parameterTypes, Class<T> returnType, List<LiteralTest.LiteralProvider> argumentProviders) {
        this.type = type;
        this.name = name;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.argumentProviders = argumentProviders;
    }

    @Parameterized.Parameters
    public static Collection values() {
        return Arrays.asList(
            new Object[] {
                Math.class, "round", Arrays.asList(double.class), long.class, Arrays.asList(new LiteralTest.DoubleProvider(1.5))
            },
            new Object[] {
                Math.class, "abs", Arrays.asList(double.class), double.class, Arrays.asList(new LiteralTest.DoubleProvider(1.5))
            },
            new Object[] {
                InvokeStaticTest.class, "vToI", Arrays.asList(), int.class, Arrays.asList()
            }
        );
    }

    @Test
    public void testInvoke() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = type.getMethod(name, parameterTypes.toArray(new Class<?>[parameterTypes.size()]));
        Object[] arguments = argumentProviders.stream().map(ap -> ap.getValue()).toArray(s -> new Object[s]);

        Object expectedResult = method.invoke(null, arguments);

        Tuple expression = invokeStatic(
                Descriptor.get(type),
                name,
                Descriptor.getMethodDescriptor(parameterTypes, returnType),
                Arrays.asList(literal(1.5)));

        testExpression(expression, Descriptor.get(returnType), actualValue -> {
            assertEquals(expectedResult, actualValue);
        });
    }
}
