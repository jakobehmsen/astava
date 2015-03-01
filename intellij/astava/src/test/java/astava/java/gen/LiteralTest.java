package astava.java.gen;

import astava.CommonTest;
import astava.core.Tuple;
import astava.java.Descriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import static astava.java.Factory.literal;
import static astava.java.Factory.ret;
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
            new Object[]{LiteralProvider.create(true, Descriptor.BOOLEAN, value -> literal(value))},
            new Object[]{LiteralProvider.create((byte)5, Descriptor.BYTE, value -> literal(value))},
            new Object[]{LiteralProvider.create((short)5, Descriptor.SHORT, value -> literal(value))},
            new Object[]{LiteralProvider.create(5, Descriptor.INT, value -> literal(value))},
            new Object[]{LiteralProvider.create(5L, Descriptor.LONG, value -> literal(value))},
            new Object[]{LiteralProvider.create(5.5f, Descriptor.FLOAT, value -> literal(value))},
            new Object[]{LiteralProvider.create(5.5, Descriptor.DOUBLE, value -> literal(value))},
            new Object[]{LiteralProvider.create("string", Descriptor.STRING, value -> literal(value))}
        );
    }

    private static abstract class LiteralProvider {
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
