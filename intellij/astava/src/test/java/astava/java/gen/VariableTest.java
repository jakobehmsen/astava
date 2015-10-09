package astava.java.gen;

import astava.CommonTest;
import astava.tree.ExpressionDom;
import astava.tree.StatementDom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;

import static astava.java.DomFactory.*;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class VariableTest {
    private LiteralTest.LiteralProvider literal;

    public VariableTest(LiteralTest.LiteralProvider literal) {
        this.literal = literal;
    }

    @Parameterized.Parameters
    public static Collection values() {
        return LiteralTest.values();
    }

    @Test
    public void testDeclareAssignAccess()
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object expectedValue = literal.getValue();
        ExpressionDom valueAST = literal.createASTDom(expectedValue);
        String type = literal.getDescriptor();
        String name = "myVar";

        StatementDom ast = block(Arrays.asList(
            declareVar(type, name),
            assignVar(name, valueAST),
            ret(accessVar(name))
        ));

        CommonTest.testMethodBody(ast, type, actualValue ->
            assertEquals(expectedValue, actualValue));
    }
}
