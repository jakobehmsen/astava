package astava.java.gen;

import astava.CommonTest;
import astava.tree.Tuple;
import astava.java.Descriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import static astava.java.Factory.literal;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class LiteralTest {
    private LiteralProvider literal;

    public LiteralTest(LiteralProvider literal) {
        this.literal = literal;
    }

    @Parameterized.Parameters
    public static Collection values() {
        return Arrays.asList(
            new Object[]{new BooleanProvider(true)},
            new Object[]{new ByteProvider((byte)5)},
            new Object[]{new ShortProvider((short)5)},
            new Object[]{new IntProvider(5)},
            new Object[]{new LongProvider(5L)},
            new Object[]{new FloatProvider(5.5f)},
            new Object[]{new DoubleProvider(5.5)},
            new Object[]{new CharProvider('x')},
            new Object[]{new StringProvider("string")}
        );
    }

    public static abstract class LiteralProvider {
        private Object value;
        private String type;

        protected LiteralProvider(Object value, String type) {
            this.value = value;
            this.type = type;
        }

        public Object getValue() { return value; }
        public abstract Tuple createAST(Object value);
        public String getType() { return type; }

        public static <T> LiteralProvider create(T value, String type, Function<T, Tuple> astFunc) {
            return new LiteralProvider(value, type) {
                @Override
                public Tuple createAST(Object value) {
                    return astFunc.apply((T)value);
                }
            };
        }
    }

    public static class BooleanProvider extends LiteralProvider {
        protected BooleanProvider(boolean value) {
            super(value, Descriptor.BOOLEAN);
        }

        @Override
        public Tuple createAST(Object value) {
            return literal((boolean)value);
        }
    }

    public static class ByteProvider extends LiteralProvider {
        protected ByteProvider(byte value) {
            super(value, Descriptor.BYTE);
        }

        @Override
        public Tuple createAST(Object value) {
            return literal((byte)value);
        }
    }

    public static class ShortProvider extends LiteralProvider {
        protected ShortProvider(short value) {
            super(value, Descriptor.SHORT);
        }

        @Override
        public Tuple createAST(Object value) {
            return literal((short)value);
        }
    }

    public static class IntProvider extends LiteralProvider {
        protected IntProvider(int value) {
            super(value, Descriptor.INT);
        }

        @Override
        public Tuple createAST(Object value) {
            return literal((int)value);
        }
    }

    public static class LongProvider extends LiteralProvider {
        protected LongProvider(long value) {
            super(value, Descriptor.LONG);
        }

        @Override
        public Tuple createAST(Object value) {
            return literal((long)value);
        }
    }

    public static class FloatProvider extends LiteralProvider {
        protected FloatProvider(float value) {
            super(value, Descriptor.FLOAT);
        }

        @Override
        public Tuple createAST(Object value) {
            return literal((float)value);
        }
    }

    public static class DoubleProvider extends LiteralProvider {
        protected DoubleProvider(double value) {
            super(value, Descriptor.DOUBLE);
        }

        @Override
        public Tuple createAST(Object value) {
            return literal((double)value);
        }
    }

    public static class CharProvider extends LiteralProvider {
        protected CharProvider(char value) {
            super(value, Descriptor.CHAR);
        }

        @Override
        public Tuple createAST(Object value) {
            return literal((char)value);
        }
    }

    public static class StringProvider extends LiteralProvider {
        protected StringProvider(String value) {
            super(value, Descriptor.STRING);
        }

        @Override
        public Tuple createAST(Object value) {
            return literal((String)value);
        }
    }

    @Test
    public void testLiteral()
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object expectedValue = literal.getValue();
        Tuple ast = literal.createAST(expectedValue);
        String type = literal.getType();

        CommonTest.testExpression(ast, type, actualValue ->
            assertEquals(expectedValue, actualValue));
    }
}
