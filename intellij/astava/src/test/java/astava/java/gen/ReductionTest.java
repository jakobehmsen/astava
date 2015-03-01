package astava.java.gen;

import astava.CommonTest;
import astava.core.Tuple;
import astava.java.Descriptor;
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
            }
        );

        List<PrimitiveTest> tests = Arrays.asList(
            new ByteTest(), new ShortTest(), new IntTest(), new LongTest(),
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
    public void testReduction() {
        long lhs = op.getLhs();
        long rhs = op.getRhs();
        long expectedValue = op.getExpectedValue(lhs, rhs);

        String reductionResultType = t1.canReduceWith(t2.getDescriptor());
        if (reductionResultType != null) {
            Tuple lhsAST = t1.createAST(lhs);
            Tuple rhsAST = t2.createAST(rhs);
            Tuple reduction = op.createAST(lhsAST, rhsAST);

            try {
                CommonTest.testExpression(reduction, reductionResultType, (Number actualValue) ->
                        assertEquals(expectedValue, actualValue.longValue()));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
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
            } catch(Throwable e) {
                //e.toString();
            }
        }
    }

    private interface PrimitiveTest {
        String getDescriptor();
        Tuple createAST(Number number);
        String canReduceWith(String descriptor);
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

        @Override
        public String canReduceWith(String descriptor) {
            switch(descriptor) {
                case Descriptor.BYTE:
                    return Descriptor.BYTE;
                case Descriptor.SHORT:
                    return Descriptor.SHORT;
                case Descriptor.INT:
                    return Descriptor.INT;
            }

            return null;
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

        @Override
        public String canReduceWith(String descriptor) {
            switch(descriptor) {
                case Descriptor.BYTE:
                case Descriptor.SHORT:
                    return Descriptor.SHORT;
                case Descriptor.INT:
                    return Descriptor.INT;
            }

            return null;
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

        @Override
        public String canReduceWith(String descriptor) {
            switch(descriptor) {
                case Descriptor.BYTE:
                case Descriptor.SHORT:
                case Descriptor.INT:
                    return Descriptor.INT;
            }

            return null;
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

        @Override
        public String canReduceWith(String descriptor) {
            switch(descriptor) {
                case Descriptor.LONG:
                    return Descriptor.LONG;
            }

            return null;
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

        @Override
        public String canReduceWith(String descriptor) {
            switch(descriptor) {
                case Descriptor.FLOAT:
                    return Descriptor.FLOAT;
            }

            return null;
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

        @Override
        public String canReduceWith(String descriptor) {
            switch(descriptor) {
                case Descriptor.DOUBLE:
                    return Descriptor.DOUBLE;
            }

            return null;
        }
    }

    private interface ReduceOperatorTest {
        default long getLhs() {return 7;}
        default long getRhs() {return 7;}
        long getExpectedValue(long lhs, long rhs);
        Tuple createAST(Tuple lhs, Tuple rhs);
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
    }
}