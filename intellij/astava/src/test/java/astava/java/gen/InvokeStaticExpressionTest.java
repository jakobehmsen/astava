package astava.java.gen;

import astava.java.Descriptor;
import astava.tree.ExpressionDom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static astava.CommonTestDom.testExpression;
import static astava.java.FactoryDom.invokeStaticExpr;
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
        List<ExpressionDom> astArguments = argumentProviders.stream().map(ap -> ap.createASTDom(ap.getValue())).collect(Collectors.toList());

        Object expectedResult = method.invoke(null, arguments);

        ExpressionDom expression = invokeStaticExpr(
            Descriptor.get(type),
            name,
            Descriptor.getMethodDescriptor(parameterTypes, returnType),
            astArguments
        );
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
