package astava.java.gen;

import astava.core.Node;
import astava.core.Tuple;
import astava.java.Descriptor;
import astava.java.Invocation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static astava.CommonTest.testExpression;
import static astava.java.Factory.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class InvokeInstanceExpression {
    public static class InvokeInstanceVirtual {
        public int iToI(int x) {
            return x * 7;
        }

        public int vToI() {
            return 7;
        }

        public void vToV() {

        }
    }

    public interface InvokeInstanceInterface {
        int iToI(int x);
        int vToI();
        void vToV();
    }

    public static class InvokeInstanceImpl implements InvokeInstanceInterface {
        @Override
        public int iToI(int x) {
            return x * 7;
        }

        @Override
        public int vToI() {
            return 7;
        }

        @Override
        public void vToV() {

        }
    }

    private Class<?> typeToInstantiate;
    private int invocation;
    private Class<?> type;
    private String name;
    private List<Class<?>> parameterTypes;
    private Class<?> returnType;
    private List<LiteralTest.LiteralProvider> argumentProviders;
    private Class<? extends Exception> expectedExceptionType;

    public InvokeInstanceExpression(Class<?> typeToInstantiate, int invocation, Class<?> type, String name, List<Class<?>> parameterTypes, Class<?> returnType, List<LiteralTest.LiteralProvider> argumentProviders, Class<? extends Exception> expectedExceptionType) {
        this.typeToInstantiate = typeToInstantiate;
        this.invocation = invocation;
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
            new Object[]{
                InvokeInstanceVirtual.class, Invocation.VIRTUAL, InvokeInstanceVirtual.class, "iToI", Arrays.asList(int.class), int.class, Arrays.asList(new LiteralTest.IntProvider(5)), null
            },
            new Object[]{
                InvokeInstanceVirtual.class, Invocation.VIRTUAL, InvokeInstanceVirtual.class, "vToI", Arrays.asList(), int.class, Arrays.asList(), null
            },
            new Object[]{
                InvokeInstanceVirtual.class, Invocation.VIRTUAL, InvokeInstanceVirtual.class, "vToV", Arrays.asList(), void.class, Arrays.asList(), IllegalArgumentException.class
            },
            new Object[]{
                InvokeInstanceImpl.class, Invocation.INTERFACE, InvokeInstanceInterface.class, "iToI", Arrays.asList(int.class), int.class, Arrays.asList(new LiteralTest.IntProvider(5)), null
            },
            new Object[]{
                InvokeInstanceImpl.class, Invocation.INTERFACE, InvokeInstanceInterface.class, "vToI", Arrays.asList(), int.class, Arrays.asList(), null
            },
            new Object[]{
                InvokeInstanceImpl.class, Invocation.INTERFACE, InvokeInstanceInterface.class, "vToV", Arrays.asList(), void.class, Arrays.asList(), IllegalArgumentException.class
            }
        );
    }

    @Test
    public void testInvokeInstance() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = type.getMethod(name, parameterTypes.toArray(new Class<?>[parameterTypes.size()]));
        Object[] arguments = argumentProviders.stream().map(ap -> ap.getValue()).toArray(s -> new Object[s]);
        List<Node> astArguments = argumentProviders.stream().map(ap -> ap.createAST(ap.getValue())).collect(Collectors.toList());

        Object expectedResult = method.invoke(typeToInstantiate.newInstance(), arguments);

        Tuple expression = invoke(
            invocation,
            Descriptor.get(type),
            name,
            Descriptor.getMethodDescriptor(parameterTypes, returnType),
            newInstance(Descriptor.get(typeToInstantiate), Collections.emptyList(), Collections.emptyList()),
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
