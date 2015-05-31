package astava.java.gen;

import astava.CommonTest;
import astava.tree.ExpressionDom;
import astava.java.Descriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static astava.java.Factory.*;

@RunWith(Parameterized.class)
public class LiteralTest {
    private LiteralProvider literal;

    public LiteralTest(LiteralProvider literal) {
        this.literal = literal;
    }

    @Parameterized.Parameters
    public static Collection values() {
        return getProviders().stream().map(x -> new Object[]{x}).collect(Collectors.toList());
    }

    public static List<LiteralProvider> getProviders() {
        return Arrays.asList(
            new BooleanProvider(true),
            new ByteProvider((byte)5),
            new ShortProvider((short)5),
            new IntProvider(5),
            new LongProvider(5L),
            new FloatProvider(5.5f),
            new DoubleProvider(5.5),
            new CharProvider('x'),
            new StringProvider("string")
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
        public abstract ExpressionDom createASTDom(Object value);
        public String getDescriptor() { return type; }

        public abstract Class<?> getType();
    }

    public static class BooleanProvider extends LiteralProvider {
        protected BooleanProvider(boolean value) {
            super(value, Descriptor.BOOLEAN);
        }

        @Override
        public ExpressionDom createASTDom(Object value) {
            return literal((boolean)value);
        }

        @Override
        public Class<?> getType() {
            return boolean.class;
        }
    }

    public static class ByteProvider extends LiteralProvider {
        protected ByteProvider(byte value) {
            super(value, Descriptor.BYTE);
        }

        @Override
        public ExpressionDom createASTDom(Object value) {
            return literal((byte) value);
        }

        @Override
        public Class<?> getType() {
            return byte.class;
        }
    }

    public static class ShortProvider extends LiteralProvider {
        protected ShortProvider(short value) {
            super(value, Descriptor.SHORT);
        }

        @Override
        public ExpressionDom createASTDom(Object value) {
            return literal((short) value);
        }

        @Override
        public Class<?> getType() {
            return short.class;
        }
    }

    public static class IntProvider extends LiteralProvider {
        protected IntProvider(int value) {
            super(value, Descriptor.INT);
        }

        @Override
        public ExpressionDom createASTDom(Object value) {
            return literal((int) value);
        }

        @Override
        public Class<?> getType() {
            return int.class;
        }
    }

    public static class LongProvider extends LiteralProvider {
        protected LongProvider(long value) {
            super(value, Descriptor.LONG);
        }

        @Override
        public ExpressionDom createASTDom(Object value) {
            return literal((long) value);
        }

        @Override
        public Class<?> getType() {
            return long.class;
        }
    }

    public static class FloatProvider extends LiteralProvider {
        protected FloatProvider(float value) {
            super(value, Descriptor.FLOAT);
        }

        @Override
        public ExpressionDom createASTDom(Object value) {
            return literal((float) value);
        }

        @Override
        public Class<?> getType() {
            return float.class;
        }
    }

    public static class DoubleProvider extends LiteralProvider {
        protected DoubleProvider(double value) {
            super(value, Descriptor.DOUBLE);
        }

        @Override
        public ExpressionDom createASTDom(Object value) {
            return literal((double) value);
        }

        @Override
        public Class<?> getType() {
            return double.class;
        }
    }

    public static class CharProvider extends LiteralProvider {
        protected CharProvider(char value) {
            super(value, Descriptor.CHAR);
        }

        @Override
        public ExpressionDom createASTDom(Object value) {
            return literal((char) value);
        }

        @Override
        public Class<?> getType() {
            return char.class;
        }
    }

    public static class StringProvider extends LiteralProvider {
        protected StringProvider(String value) {
            super(value, Descriptor.STRING);
        }

        @Override
        public ExpressionDom createASTDom(Object value) {
            return literal((String) value);
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }
    }

    @Test
    public void testLiteral()
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object expectedValue = literal.getValue();
        //Tuple ast = literal.createAST(expectedValue);
        ExpressionDom ast = literal.createASTDom(expectedValue);
        String type = literal.getDescriptor();

        CommonTest.testExpression(ast, type, actualValue ->
            assertEquals(expectedValue, actualValue));
    }
}
