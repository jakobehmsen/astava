package astava.java.gen;

import astava.java.Descriptor;
import astava.tree.ClassDom;
import astava.tree.ParameterInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static astava.java.Factory.*;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ArgumentTest {
    private int argumentToTest;
    private List<LiteralTest.LiteralProvider> arguments;

    public ArgumentTest(List<LiteralTest.LiteralProvider> arguments, int argumentToTest) {
        this.argumentToTest = argumentToTest;
        this.arguments = arguments;
    }

    @Parameterized.Parameters
    public static Collection values() {
        return IntStream.range(1, 4)
            .mapToObj(x -> createArguments(x))
            .flatMap(argumentsStream -> argumentsStream)
            .flatMap(arguments -> IntStream.range(0, arguments.size()).mapToObj(argumentToTest -> new Object[]{arguments, argumentToTest}))
            .collect(Collectors.toList());
    }

    private static Stream<List<LiteralTest.LiteralProvider>> createArguments(int count) {
        return LiteralTest.getProviders().stream().map(provider -> Stream.generate(() -> provider).limit(count).collect(Collectors.toList()));
    }

    @Test
    public void testField() throws ClassNotFoundException, NoSuchFieldException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        String className = "MyClass";
        String paramPrefix = "p";
        String methodName = "myMethod";

        List<ParameterInfo> parameters = IntStream.range(0, arguments.size()).mapToObj(x -> new ParameterInfo(arguments.get(x).getDescriptor(), paramPrefix + x)).collect(Collectors.toList());

        String returnTypeDescriptor = arguments.get(argumentToTest).getDescriptor();
        String paramName = parameters.get(argumentToTest).name;

        ClassDom classDeclaration = classDeclaration(Modifier.PUBLIC, className, "java/lang/Object", Arrays.asList(), Arrays.asList(), Arrays.asList(
            methodDeclaration(Modifier.PUBLIC | Modifier.STATIC, methodName, parameters, returnTypeDescriptor, ret(accessVar(paramName)))
        ));

        ClassGenerator generator = new ClassGenerator(classDeclaration);
        Class<?> c = generator.newClass();

        Method m = c.getDeclaredMethod(methodName, arguments.stream().map(x -> x.getType()).toArray(s -> new Class<?>[s]));
        m.setAccessible(true);

        assertEquals(arguments.size(), m.getParameterCount());

        Object[] actualArguments = arguments.stream().map(x -> x.getValue()).toArray();
        Object expectedValue = arguments.get(argumentToTest).getValue();
        Object value = m.invoke(null, actualArguments);

        assertEquals(expectedValue, value);
    }
}
