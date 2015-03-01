package astava;

import astava.core.Node;
import astava.core.Tuple;
import astava.java.gen.ClassGenerator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import static astava.java.Factory.classDeclaration;
import static astava.java.Factory.methodDeclaration;

public class CommonTest {
    public static <T> void testExpression(Tuple expression, String returnType, Consumer<T> assertion)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String methodName = "myMethod";
        Node classDeclaration = classDeclaration(Modifier.PUBLIC, "MyClass", "java/lang/Object", Arrays.asList(
                methodDeclaration(Modifier.PUBLIC | Modifier.STATIC, methodName, Collections.emptyList(), returnType, expression)
        ));
        ClassGenerator generator = new ClassGenerator(classDeclaration);
        Object actualValue = generator.newClass().getMethod(methodName).invoke(null, null);
        assertion.accept((T)actualValue);
    }
}
