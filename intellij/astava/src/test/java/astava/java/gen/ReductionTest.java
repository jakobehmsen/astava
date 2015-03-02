package astava.java.gen;

import astava.CommonTest;
import astava.core.Tuple;
import astava.java.*;
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
            new UshrTest(),
            new BAndTest(),
            new BOrTest(),
            new BXorTest(),
            new LTTest() { // => true
                @Override
                public long getLhs() { return 7L; }
                @Override
                public long getRhs() { return 8L; }
            },
            new LTTest() { // => false
                @Override
                public long getLhs() { return 8L; }
                @Override
                public long getRhs() { return 8L; }
            },
            new LETest() { // => true
                @Override
                public long getLhs() { return 7L; }
                @Override
                public long getRhs() { return 8L; }
            },
            new LETest() { // => true
                @Override
                public long getLhs() { return 8L; }
                @Override
                public long getRhs() { return 8L; }
            },
            new LETest() { // => false
                @Override
                public long getLhs() { return 9L; }
                @Override
                public long getRhs() { return 8L; }
            },
            new GTTest() { // => true
                @Override
                public long getLhs() { return 9L; }
                @Override
                public long getRhs() { return 8L; }
            },
            new GTTest() { // => false
                @Override
                public long getLhs() { return 8L; }
                @Override
                public long getRhs() { return 8L; }
            },
            new GETest() { // => true
                @Override
                public long getLhs() { return 9L; }
                @Override
                public long getRhs() { return 8L; }
            },
            new GETest() { // => true
                @Override
                public long getLhs() { return 8L; }
                @Override
                public long getRhs() { return 8L; }
            },
            new GETest() { // => false
                @Override
                public long getLhs() { return 7L; }
                @Override
                public long getRhs() { return 8L; }
            },
            new EQTest() { // => true
                @Override
                public long getLhs() { return 8L; }
                @Override
                public long getRhs() { return 8L; }
            },
            new EQTest() { // => false
                @Override
                public long getLhs() { return 7L; }
                @Override
                public long getRhs() { return 8L; }
            },
            new NETest() { // => true
                @Override
                public long getLhs() { return 7L; }
                @Override
                public long getRhs() { return 8L; }
            },
            new NETest() { // => false
                @Override
                public long getLhs() { return 8L; }
                @Override
                public long getRhs() { return 8L; }
            }
        );

        List<PrimitiveTest> tests = Arrays.asList(
            new ByteTest(), new ShortTest(),
            new IntTest(), new LongTest(),
            new FloatTest(), new DoubleTest()
        );

        operators.forEach(op -> {
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
        Object expectedValue = op.getExpectedValue(lhs, rhs);

        String reductionResultType = op.resultType(t1.getDescriptor(), t2.getDescriptor());
        boolean expectSuccess = reductionResultType != null;

        try {
            Tuple lhsAST = t1.createAST(lhs);
            Tuple rhsAST = t2.createAST(rhs);
            Tuple reduction = op.createAST(lhsAST, rhsAST);

            CommonTest.testExpression(reduction, reductionResultType, (Object actualValue) ->
                assertEquals(expectedValue, op.normalizeValue(actualValue)));

            if(!expectSuccess)
                fail();
        } catch (Throwable e) {
            if(expectSuccess)
                throw e;

            // Expect error? Which kind of error? Which message?
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
        default long getRhs() {return 9;}
        Object getExpectedValue(long lhs, long rhs);
        Object normalizeValue(Object value);
        Tuple createAST(Tuple lhs, Tuple rhs);
        String resultType(String lhsResultType, String rhsResultType);
    }

    private static abstract class NumberResultTest implements ReduceOperatorTest {
        @Override
        public Object normalizeValue(Object value) {
            return ((Number)value).longValue();
        }
    }

    private static abstract class ArithmeticResultTest extends NumberResultTest {
        @Override
        public String resultType(String lhsResultType, String rhsResultType) {
            return Factory.arithmeticResultType(lhsResultType, rhsResultType);
        }
    }

    private static class AddTest extends ArithmeticResultTest {
        @Override
        public Object getExpectedValue(long lhs, long rhs) {
            return lhs + rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return add(lhs, rhs);
        }
    }

    private static class SubTest extends ArithmeticResultTest {
        @Override
        public Object getExpectedValue(long lhs, long rhs) {
            return lhs - rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return sub(lhs, rhs);
        }
    }

    private static class MulTest extends ArithmeticResultTest {
        @Override
        public Object getExpectedValue(long lhs, long rhs) {
            return lhs * rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return mul(lhs, rhs);
        }
    }

    private static class DivTest extends ArithmeticResultTest {
        @Override
        public Object getExpectedValue(long lhs, long rhs) {
            return lhs / rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return div(lhs, rhs);
        }
    }

    private static class RemTest extends ArithmeticResultTest {
        @Override
        public Object getExpectedValue(long lhs, long rhs) {
            return lhs % rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return rem(lhs, rhs);
        }
    }

    private static abstract class ShiftResultTest extends NumberResultTest {
        @Override
        public String resultType(String lhsResultType, String rhsResultType) {
            return Factory.shiftResultType(lhsResultType, rhsResultType);
        }
    }

    private static class ShlTest extends ShiftResultTest {
        @Override
        public Object getExpectedValue(long lhs, long rhs) {
            return lhs << rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return shl(lhs, rhs);
        }
    }

    private static class ShrTest extends ShiftResultTest {
        @Override
        public Object getExpectedValue(long lhs, long rhs) {
            return lhs >> rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return shr(lhs, rhs);
        }
    }

    private static class UshrTest extends ShiftResultTest {
        @Override
        public Object getExpectedValue(long lhs, long rhs) {
            return lhs >>> rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return ushr(lhs, rhs);
        }
    }

    private static abstract class BitwiseResultTest extends NumberResultTest {
        @Override
        public String resultType(String lhsResultType, String rhsResultType) {
            return Factory.bitwiseResultType(lhsResultType, rhsResultType);
        }
    }

    private static class BAndTest extends BitwiseResultTest {
        @Override
        public Object getExpectedValue(long lhs, long rhs) {
            return lhs & rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return band(lhs, rhs);
        }
    }

    private static class BOrTest extends BitwiseResultTest {
        @Override
        public Object getExpectedValue(long lhs, long rhs) {
            return lhs | rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return bor(lhs, rhs);
        }
    }

    private static class BXorTest extends BitwiseResultTest {
        @Override
        public Object getExpectedValue(long lhs, long rhs) {
            return lhs ^ rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return bxor(lhs, rhs);
        }
    }

    private static abstract class BooleanResultTest implements ReduceOperatorTest {
        @Override
        public Object normalizeValue(Object value) {
            return value;
        }

        @Override
        public String resultType(String lhsResultType, String rhsResultType) {
            return Factory.compareResultType(lhsResultType, rhsResultType);
        }
    }

    private static class LTTest extends BooleanResultTest {
        @Override
        public Object getExpectedValue(long lhs, long rhs) {
            return lhs < rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return lt(lhs, rhs);
        }
    }

    private static class LETest extends BooleanResultTest {
        @Override
        public Object getExpectedValue(long lhs, long rhs) {
            return lhs <= rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return le(lhs, rhs);
        }
    }

    private static class GTTest extends BooleanResultTest {
        @Override
        public Object getExpectedValue(long lhs, long rhs) {
            return lhs > rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return gt(lhs, rhs);
        }
    }

    private static class GETest extends BooleanResultTest {
        @Override
        public Object getExpectedValue(long lhs, long rhs) {
            return lhs >= rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return ge(lhs, rhs);
        }
    }

    private static class EQTest extends BooleanResultTest {
        @Override
        public Object getExpectedValue(long lhs, long rhs) {
            return lhs == rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return eq(lhs, rhs);
        }
    }

    private static class NETest extends BooleanResultTest {
        @Override
        public Object getExpectedValue(long lhs, long rhs) {
            return lhs != rhs;
        }

        @Override
        public Tuple createAST(Tuple lhs, Tuple rhs) {
            return ne(lhs, rhs);
        }
    }
}