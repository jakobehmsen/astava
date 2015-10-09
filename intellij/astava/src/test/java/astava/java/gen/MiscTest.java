package astava.java.gen;

import astava.CommonTest;
import astava.java.Descriptor;
import astava.tree.ExpressionDom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;

import static astava.java.DomFactory.*;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class MiscTest {
    private Expression expression;

    public MiscTest(Expression expression) {
        this.expression = expression;
    }

    @Test
    public void testExpression() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String resultType = expression.resultType();
        Object expectedResult = expression.evaluate();
        ExpressionDom ast = expression.createAST();
        CommonTest.testExpression(ast, resultType, actualResult ->
            assertEquals(expectedResult, actualResult));
    }

    @Parameterized.Parameters
    public static Collection values() {
        return Arrays.asList(
            new Object[] {
                new Or(new GT(new Int(1), new Int(2)), new Bool(true))
            },
            new Object[] { new Bool(true) }
        );
    }

    private interface Expression {
        String resultType();
        ExpressionDom createAST();
        Object evaluate();
    }

    private static class Or implements Expression {
        private Expression lhs;
        private Expression rhs;

        private Or(Expression lhs, Expression rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public String resultType() {
            return Descriptor.BOOLEAN;
        }

        @Override
        public ExpressionDom createAST() {
            return or(lhs.createAST(), rhs.createAST());
        }

        @Override
        public Object evaluate() {
            return (boolean)lhs.evaluate() || (boolean)rhs.evaluate();
        }
    }

    private static class GT implements Expression {
        private Expression lhs;
        private Expression rhs;

        private GT(Expression lhs, Expression rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public String resultType() {
            return Descriptor.BOOLEAN;
        }

        @Override
        public ExpressionDom createAST() {
            return gt(lhs.createAST(), rhs.createAST());
        }

        @Override
        public Object evaluate() {
            return ((Number)lhs.evaluate()).longValue() < ((Number)rhs.evaluate()).longValue();
        }
    }

    private static class Int implements Expression {
        private int value;

        private Int(int value) {
            this.value = value;
        }

        @Override
        public String resultType() {
            return Descriptor.INT;
        }

        @Override
        public ExpressionDom createAST() {
            return literal(value);
        }

        @Override
        public Object evaluate() {
            return value;
        }
    }

    private static class Bool implements Expression  {
        private boolean value;

        private Bool(boolean value) {
            this.value = value;
        }

        @Override
        public String resultType() {
            return Descriptor.BOOLEAN;
        }

        @Override
        public ExpressionDom createAST() {
            return literal(value);
        }

        @Override
        public Object evaluate() {
            return value;
        }
    }
}
