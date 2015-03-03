package astava.java.gen;

import astava.core.Tuple;
import astava.java.Descriptor;
import astava.java.LogicalOperator;
import astava.java.RelationalOperator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import static astava.CommonTest.testMethodBody;
import static astava.java.Factory.*;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class IfElseTest {
    private ExpressionProvider conditionProvider;

    public IfElseTest(ExpressionProvider conditionProvider) {
        this.conditionProvider = conditionProvider;
    }

    @Parameterized.Parameters
    public static Collection values() {
        ArrayList<ExpressionProvider> conditionProviders = new ArrayList<>();

        ArrayList<ExpressionProvider> conditionProviders1 = new ArrayList<>();

        conditionProviders1.add(new BooleanProvider(true));
        conditionProviders1.add(new BooleanProvider(false));

        conditionProviders1.add(new CompareProvider(new IntProvider(6), new IntProvider(7), RelationalOperator.LT));
        conditionProviders1.add(new CompareProvider(new IntProvider(7), new IntProvider(7), RelationalOperator.LT));

        conditionProviders1.add(new CompareProvider(new IntProvider(6), new IntProvider(7), RelationalOperator.LE));
        conditionProviders1.add(new CompareProvider(new IntProvider(7), new IntProvider(7), RelationalOperator.LE));
        conditionProviders1.add(new CompareProvider(new IntProvider(7), new IntProvider(8), RelationalOperator.LE));

        conditionProviders1.add(new CompareProvider(new IntProvider(8), new IntProvider(7), RelationalOperator.GT));
        conditionProviders1.add(new CompareProvider(new IntProvider(7), new IntProvider(7), RelationalOperator.GT));

        conditionProviders1.add(new CompareProvider(new IntProvider(8), new IntProvider(7), RelationalOperator.GE));
        conditionProviders1.add(new CompareProvider(new IntProvider(7), new IntProvider(7), RelationalOperator.GE));
        conditionProviders1.add(new CompareProvider(new IntProvider(6), new IntProvider(7), RelationalOperator.GE));

        conditionProviders1.add(new CompareProvider(new IntProvider(7), new IntProvider(7), RelationalOperator.EQ));
        conditionProviders1.add(new CompareProvider(new IntProvider(6), new IntProvider(7), RelationalOperator.EQ));

        conditionProviders1.add(new CompareProvider(new IntProvider(6), new IntProvider(7), RelationalOperator.NE));
        conditionProviders1.add(new CompareProvider(new IntProvider(7), new IntProvider(7), RelationalOperator.NE));

        conditionProviders.addAll(conditionProviders1);

        ArrayList<ExpressionProvider> conditionProviders2 = new ArrayList<>();

        conditionProviders1.forEach(lhsProvider ->
            conditionProviders1.forEach(rhsProvider -> {
                conditionProviders2.add(new LogicalProvider(lhsProvider, rhsProvider, LogicalOperator.AND));
                conditionProviders2.add(new LogicalProvider(lhsProvider, rhsProvider, LogicalOperator.OR));
            })
        );

        conditionProviders.addAll(conditionProviders2);

        ArrayList<ExpressionProvider> conditionProviders3 = new ArrayList<>();

        // Complex lhs
        conditionProviders2.forEach(lhsProvider -> {
            conditionProviders3.add(new LogicalProvider(lhsProvider, conditionProviders2.get(0), LogicalOperator.AND));
            conditionProviders3.add(new LogicalProvider(lhsProvider, conditionProviders2.get(0), LogicalOperator.OR));
        });

        // Complex rhs
        conditionProviders2.forEach(rhsProvider -> {
            conditionProviders3.add(new LogicalProvider(conditionProviders2.get(0), rhsProvider, LogicalOperator.AND));
            conditionProviders3.add(new LogicalProvider(conditionProviders2.get(0), rhsProvider, LogicalOperator.OR));
        });

        conditionProviders.addAll(conditionProviders3);

        return conditionProviders.stream().map(x -> new Object[]{x}).collect(Collectors.toList());
    }

    @Test
    public void testIfElse() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Tuple condition = conditionProvider.createExpression();
        boolean conditionValue = conditionProvider.valueBoolean();

        Object expectedValue = conditionValue ? true : false;

        Tuple ast = ifElse(
            condition,
            ret(literal(true)),
            ret(literal(false))
        );

        testMethodBody(ast, Descriptor.BOOLEAN, actualValue ->
            assertEquals(expectedValue, actualValue));
    }

    private interface ExpressionProvider {
        Object value();
        default Number valueNumber() {
            return (Number)value();
        }
        default long valueLong() {
            return valueNumber().longValue();
        }
        default boolean valueBoolean() {
            return (Boolean)value();
        }
        Tuple createExpression();
    }

    private static class BooleanProvider implements ExpressionProvider {
        private final boolean value;

        private BooleanProvider(boolean value) {
            this.value = value;
        }

        @Override
        public Object value() {
            return value;
        }

        @Override
        public Tuple createExpression() {
            return literal(value);
        }
    }

    private static class IntProvider implements ExpressionProvider {
        private final int value;

        private IntProvider(int value) {
            this.value = value;
        }

        @Override
        public Object value() {
            return value;
        }

        @Override
        public Tuple createExpression() {
            return literal(value);
        }
    }

    private static class CompareProvider implements ExpressionProvider {
        private ExpressionProvider lhsProvider;
        private ExpressionProvider rhsProvider;
        private int operator;

        private CompareProvider(ExpressionProvider lhsProvider, ExpressionProvider rhsProvider, int operator) {
            this.lhsProvider = lhsProvider;
            this.rhsProvider = rhsProvider;
            this.operator = operator;
        }

        @Override
        public Object value() {
            long lhs = lhsProvider.valueLong();
            long rhs = rhsProvider.valueLong();

            switch (operator) {
                case RelationalOperator.LT:
                    return lhs < rhs;
                case RelationalOperator.LE:
                    return lhs <= rhs;
                case RelationalOperator.GT:
                    return lhs > rhs;
                case RelationalOperator.GE:
                    return lhs >= rhs;
                case RelationalOperator.EQ:
                    return lhs == rhs;
                case RelationalOperator.NE:
                    return lhs != rhs;
            }

            return null;
        }

        @Override
        public Tuple createExpression() {
            Tuple lhs = lhsProvider.createExpression();
            Tuple rhs = rhsProvider.createExpression();

            return compare(lhs, rhs, operator);
        }
    }

    private static class LogicalProvider implements ExpressionProvider {
        private ExpressionProvider lhsProvider;
        private ExpressionProvider rhsProvider;
        private int operator;

        private LogicalProvider(ExpressionProvider lhsProvider, ExpressionProvider rhsProvider, int operator) {
            this.lhsProvider = lhsProvider;
            this.rhsProvider = rhsProvider;
            this.operator = operator;
        }

        @Override
        public Object value() {
            boolean lhs = lhsProvider.valueBoolean();
            boolean rhs = rhsProvider.valueBoolean();

            switch (operator) {
                case LogicalOperator.AND:
                    return lhs && rhs;
                case LogicalOperator.OR:
                    return lhs || rhs;
            }

            return null;
        }

        @Override
        public Tuple createExpression() {
            Tuple lhs = lhsProvider.createExpression();
            Tuple rhs = rhsProvider.createExpression();

            return logical(lhs, rhs, operator);
        }
    }
}
