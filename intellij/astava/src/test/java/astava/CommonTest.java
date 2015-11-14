package astava;

import astava.java.DomFactory;
import astava.java.gen.ClassGenerator;
import astava.tree.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import static astava.java.DomFactory.*;

public class CommonTest {
    public static <T> void testExpression(ExpressionDom expression, String returnType, Consumer<T> assertion)
        throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        testMethodBody(DomFactory.ret(expression), returnType, assertion);
    }

    public static <T> void testMethodBody(StatementDom methodBody, String returnType, Consumer<T> assertion)
        throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String methodName = "myMethod";
        ClassDom classDeclaration = classDeclaration(Modifier.PUBLIC, "MyClass", "java/lang/Object", Arrays.asList(), Arrays.asList(), Arrays.asList(
            methodDeclaration(Modifier.PUBLIC | Modifier.STATIC, methodName, Collections.emptyList(), returnType, methodBody)
        ));
        ClassGenerator generator = new ClassGenerator(classDeclaration);
        Object actualValue = generator.newClass().getMethod(methodName).invoke(null, null);
        assertion.accept((T)actualValue);
    }

    public static StatementDom whileLoop(ExpressionDom condition, StatementDom body) {
        // Embedded loop aren't supported
        return block(Arrays.asList(
            labelOLD("continue"),
            ifElse(condition,
                block(Arrays.asList(
                    body,
                    goToOLD("continue")
                )),
                goToOLD("break")
            ),
            labelOLD("break")
        ));
    }
}
