package astava.java.gen;

import astava.CommonTest;
import astava.core.Tuple;
import astava.java.Descriptor;
import astava.java.Factory;
import astava.java.ReduceOperator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static astava.java.Factory.*;
import static astava.java.Factory.literal;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class ReductionTest {
    private ReduceOperatorTest op;
    private PrimitiveTest t1;
    private PrimitiveTest t2;

    public ReductionTest(ReduceOperatorTest op, PrimitiveTest t1, PrimitiveTest t2) {
        this.op = op;
        this.t1 = t1;
        this.t2 = t2;
    }

    @Parameterized.Parameters
    public static Collection values() {
        ArrayList<Object[]> values = new ArrayList<>();

        List<ReduceOperatorTest> operators = Arrays.asList(
            new AddTest(),
            new SubTest(),
            new MulTest(),
            new DivTest() {
                @Override
                public long getLhs() {
                    return 20L;
                }

                @Override
                public long getRhs() {
                    return 5L;
                }
            },
            new RemTest() {
                @Override
                public long getLhs() {
                    return 10L;
                }

                @Override
                public long getRhs() {
                    return 9L;
                }
            },
            new ShlTest(),
            new ShrTest(),
            new UshrTest()
        );

        List<PrimitiveTest> tests = Arrays.asList(
            new ByteTest(), new ShortTest(),
            new IntTest(), new LongTest(),
            new FloatTest(), new DoubleTest()
        );

        operators.forEach(op -> {
            long lhs = op.getLhs();
            long rhs = op.getRhs();
            long expectedValue = op.getExpectedValue(lhs, rhs);

            tests.forEach(t1 -> {
                tests.forEach(t2 -> {
                    values.add(new Object[]{op, t1, t2});
                });
            });
        });

        return values;
    }

    @Test
    public void testReduction() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        long lhs = op.getLhs();
        long rhs = op.getRhs();
        long expectedValue = op.getExpectedValue(lhs, rhs);

        String reductionResultType = op.resultType(t1.getDescriptor(), t2.getDescriptor());

        if(reductionResultType != null) {
            Tuple lhsAST = t1.createAST(lhs);
            Tuple rhsAST = t2.createAST(rhs);
            Tuple reduction = op.createAST(lhsAST, rhsAST);

            CommonTest.testExpression(reduction, reductionResultType, (Number actualValue) ->
                    assertEquals(expectedValue, actualValue.longValue()));
        } else {
            // Expect error? Which kind of error? Which message?
            reductionResultType = t1.getDescriptor();

            try {
                Tuple lhsAST = t1.createAST(lhs);
                Tuple rhsAST = t2.createAST(rhs);
                Tuple reduction = op.createAST(lhsAST, rhsAST);

                CommonTest.testExpression(reduction, reductionResultType, (Number actualValue) ->
                        assertEquals(expectedValue, actualValue.longValue()));
                fail();
            } catch (Throwable e) {
                //e.toString();
            }
        }
    }

    private interface PrimitiveTest {
        String getDescriptor();
        Tuple createAST(Number number);
    }

    private static class ByteTest implements PrimitiveTest {
        @Override
        public String getDescriptor() {
            return Descriptor.BYTE;
        }

        @Override
        public Tuple createAST(Number number) {
            return literal(number.byteValue());
        }
    }

    private static class ShortTest implements PrimitiveTest {
        @Override
        public String getDescriptor() {
            return Descriptor.SHORT;
        }

        @Override
        public Tuple createAST(Number number) {
            return literal(number.shortValue());
        }
    }

    private static class IntTest implements PrimitiveTest {
        @Override
        public String getDescriptor() {
            return Descriptor.INT;
        }

        @Override
        public Tuple createAST(Number number) {
            return literal(number.intValue());
        }
    }

    private static class LongTest implements PrimitiveTest {
        @Override
        public String getDescriptor() {
            return Descriptor.LONG;
        }

        @Override
        public Tuple createAST(Number number) {
            return literal(number.longValue());
        }
    }

    private static class FloatTest implements PrimitiveTest {
        @Override
        public String getDescriptor() {
            return Descriptor.FLOAT;
        }

        @Override
        public Tuple createAST(Number number) {
            return literal(number.floatValue());
        }
    }

    private static class DoubleTest implements PrimitiveTest {
        @Override
        public String getDescriptor() {
            return Descriptor.DOUBLE;
        }

        @Override
        public Tuple createAST(Number number) {
            return literal(number.doubleValue());
        }
    }

    private interface ReduceOperatorTest {
        default long getLhs() {return 7;}
        default long getRhs() {return 7;}
        long getExpectedValue(long lhs, long rhs);
        Tuple createAST(Tuple lhs, Tuple rhs);
        default String resultType(String lhsResultType, String rhsResultType) {
            return Factory.resultType(getOperator(), lhsResultType, rhsResultType);
        }
        int getOperator();
    }

    private static class AddTest implements ReduceOperatorTest {
        @Override
        public long getExpectedValue(long lhs, long rhs) {
            return lhs + rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return add(lhs, rhs);
        }

        @Override
        public int getOperator() {
            return ReduceOperator.ADD;
        }
    }

    private static class SubTest implements ReduceOperatorTest {
        @Override
        public long getExpectedValue(long lhs, long rhs) {
            return lhs - rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return sub(lhs, rhs);
        }

        @Override
        public int getOperator() {
            return ReduceOperator.SUB;
        }
    }

    private static class MulTest implements ReduceOperatorTest {
        @Override
        public long getExpectedValue(long lhs, long rhs) {
            return lhs * rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return mul(lhs, rhs);
        }

        @Override
        public int getOperator() {
            return ReduceOperator.MUL;
        }
    }

    private static class DivTest implements ReduceOperatorTest {
        @Override
        public long getExpectedValue(long lhs, long rhs) {
            return lhs / rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return div(lhs, rhs);
        }

        @Override
        public int getOperator() {
            return ReduceOperator.DIV;
        }
    }

    private static class RemTest implements ReduceOperatorTest {
        @Override
        public long getExpectedValue(long lhs, long rhs) {
            return lhs % rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return rem(lhs, rhs);
        }

        @Override
        public int getOperator() {
            return ReduceOperator.REM;
        }
    }

    private static class ShlTest implements ReduceOperatorTest {
        @Override
        public long getExpectedValue(long lhs, long rhs) {
            return lhs << rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return shl(lhs, rhs);
        }

        @Override
        public int getOperator() {
            return ReduceOperator.SHL;
        }
    }

    private static class ShrTest implements ReduceOperatorTest {
        @Override
        public long getExpectedValue(long lhs, long rhs) {
            return lhs >> rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return shr(lhs, rhs);
        }

        @Override
        public int getOperator() {
            return ReduceOperator.SHR;
        }
    }

    private static class UshrTest implements ReduceOperatorTest {
        @Override
        public long getExpectedValue(long lhs, long rhs) {
            return lhs >>> rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return ushr(lhs, rhs);
        }

        @Override
        public int getOperator() {
            return ReduceOperator.USHR;
        }
    }
}