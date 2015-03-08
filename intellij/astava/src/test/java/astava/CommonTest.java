package astava;

import astava.core.Node;
import astava.core.Tuple;
import astava.java.gen.ClassGenerator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import static astava.java.Factory.*;
import static astava.java.Factory.goTo;
import static astava.java.Factory.label;

public class CommonTest {
    public static <T> void testExpression(Tuple expression, String returnType, Consumer<T> assertion)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        testMethodBody(ret(expression), returnType, assertion);
    }

    public static <T> void testMethodBody(Tuple methodBody, String returnType, Consumer<T> assertion)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String methodName = "myMethod";
        Node classDeclaration = classDeclaration(Modifier.PUBLIC, "MyClass", "java/lang/Object", Arrays.asList(
            methodDeclaration(Modifier.PUBLIC | Modifier.STATIC, methodName, Collections.emptyList(), returnType, methodBody)
        ));
        ClassGenerator generator = new ClassGenerator(classDeclaration);
        Object actualValue = generator.newClass().getMethod(methodName).invoke(null, null);
        assertion.accept((T)actualValue);
    }

    public static Tuple whileLoop(Tuple condition, Tuple body) {
        // Embedded loop aren't supported
        return block(Arrays.asList(
            label("continue"),
            ifElse(condition,
                block(Arrays.asList(
                    body,
                    goTo("continue")
                )),
                goTo("break")
            ),
            label("break")
        ));
    }
}
