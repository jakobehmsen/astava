package astava.java.gen;

import astava.CommonTest;
import astava.java.Descriptor;
import astava.tree.ExpressionDom;
import astava.tree.StatementDom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static astava.java.FactoryDom.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class ExpressionBlockTest {
    private ExpressionBlockProvider provider;

    public ExpressionBlockTest(ExpressionBlockProvider provider) {
        this.provider = provider;
    }

    @Parameterized.Parameters
    public static Collection values() {
        // * indicates expression in block
        return Arrays.asList(
            // {Declare, assign, *access} as expression should succeed
            new ExpressionBlockProvider(7, Descriptor.INT, false) {
                @Override
                public ExpressionDom createExpression() {
                    return blockExpr(Arrays.asList(
                        declareVar(Descriptor.INT, "myVar"),
                        assignVar("myVar", literal(7)),
                        accessVar("myVar")
                    ));
                }
            },
            // {Declare,assign} as expression should fail
            new ExpressionBlockProvider(null, Descriptor.INT, true) {
                @Override
                public ExpressionDom createExpression() {
                    return blockExpr(Arrays.asList(
                        declareVar(Descriptor.INT, "myVar"),
                        assignVar("myVar", literal(7))
                    ));
                }
            },
            // {Declare, assign, *access, *access} as expression should fail
            new ExpressionBlockProvider(null, Descriptor.INT, true) {
                @Override
                public ExpressionDom createExpression() {
                    return blockExpr(Arrays.asList(
                        declareVar(Descriptor.INT, "myVar"),
                        assignVar("myVar", literal(7)),
                        accessVar("myVar"),
                        accessVar("myVar")
                    ));
                }
            },
            // {Declare, assign, *access, inc} should succeed
            // Corresponds to myVar++
            new ExpressionBlockProvider(7, Descriptor.INT, false) {
                @Override
                public ExpressionDom createExpression() {
                    return blockExpr(Arrays.asList(
                        declareVar(Descriptor.INT, "myVar"),
                        assignVar("myVar", literal(7)),
                        accessVar("myVar"),
                        intIncVar("myVar", 1)
                    ));
                }
            },
            // {Declare, assign, inc, *access} should succeed
            // Corresponds to {++myVar}
            new ExpressionBlockProvider(8, Descriptor.INT, false) {
                @Override
                public ExpressionDom createExpression() {
                    return blockExpr(Arrays.asList(
                        declareVar(Descriptor.INT, "myVar"),
                        assignVar("myVar", literal(7)),
                        intIncVar("myVar", 1),
                        accessVar("myVar")
                    ));
                }
            },
            // {*ifElse(true, {*true}, {*false})} should succeed
            // Corresponds to {true ? true : false}
            new ExpressionBlockProvider(true, Descriptor.BOOLEAN, false) {
                @Override
                public ExpressionDom createExpression() {
                    return blockExpr(Arrays.asList(
                        ifElseExpr(literal(true),
                            blockExpr(Arrays.asList(literal(true))),
                            blockExpr(Arrays.asList(literal(false)))
                        )
                    ));
                }
            },
            // {*ifElse(false, {*true}, {*false})} should succeed
            // Corresponds to {false ? true : false}
            new ExpressionBlockProvider(false, Descriptor.BOOLEAN, false) {
                @Override
                public ExpressionDom createExpression() {
                    return blockExpr(Arrays.asList(
                        ifElseExpr(literal(false),
                            blockExpr(Arrays.asList(literal(true))),
                            blockExpr(Arrays.asList(literal(false)))
                        )
                    ));
                }
            }
        ).stream().map(x -> new Object[]{x}).collect(Collectors.toList());
    }

    @Test
    public void testExpressionBlock()
        throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object expectedValue = provider.expectedValue();
        String type = provider.resultType();

        ExpressionDom expressionBlock = provider.createExpression();

        StatementDom ast = block(Arrays.asList(
            ret(expressionBlock)
        ));

        try {
            CommonTest.testMethodBody(ast, type, actualValue ->
                assertEquals(expectedValue, actualValue));
            if(provider.shouldFail())
                fail();
        } catch (Exception e) {
            if(!provider.shouldFail())
                throw e;
        }
    }

    private static abstract class ExpressionBlockProvider {
        private Object expectedValue;
        private String resultType;
        private boolean shouldFail;

        protected ExpressionBlockProvider(Object expectedValue, String resultType, boolean shouldFail) {
            this.expectedValue = expectedValue;
            this.resultType = resultType;
            this.shouldFail = shouldFail;
        }

        public Object expectedValue() {
            return expectedValue;
        }

        public String resultType() {
            return resultType;
        }

        public boolean shouldFail() {
            return shouldFail;
        }

        public abstract ExpressionDom createExpression();
    }
}
