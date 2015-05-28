package astava;

import astava.java.FactoryDom;
import astava.java.gen.ClassGenerator;
import astava.java.gen.ClassGeneratorFromDom;
import astava.tree.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import static astava.java.FactoryDom.*;

public class CommonTest {
    public static <T> void testExpression(ExpressionDom expression, String returnType, Consumer<T> assertion)
        throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        testMethodBody(FactoryDom.ret(expression), returnType, assertion);
    }

    public static <T> void testMethodBody(StatementDom methodBody, String returnType, Consumer<T> assertion)
        throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String methodName = "myMethod";
        ClassDom classDeclaration = classDeclaration(Modifier.PUBLIC, "MyClass", "java/lang/Object", Arrays.asList(), Arrays.asList(
            methodDeclaration(Modifier.PUBLIC | Modifier.STATIC, methodName, Collections.emptyList(), returnType, methodBody)
        ));
        ClassGeneratorFromDom generator = new ClassGeneratorFromDom(classDeclaration);
        Object actualValue = generator.newClass().getMethod(methodName).invoke(null, null);
        assertion.accept((T)actualValue);
    }

    public static StatementDom whileLoop(ExpressionDom condition, StatementDom body) {
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
