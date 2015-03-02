package astava.java.gen;

import astava.CommonTest;
import astava.core.Tuple;
import astava.java.Descriptor;
import astava.java.IncDec;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static astava.java.Factory.*;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class IncDecTest {
    private BlockProvider blockProvider;
    private IncDecProvider incDecProvider;

    public IncDecTest(BlockProvider blockProvider, IncDecProvider provider) {
        this.blockProvider = blockProvider;
        this.incDecProvider = provider;
    }

    @Parameterized.Parameters
    public static Collection values() {
        BlockProvider[] blockProviders = new BlockProvider[] {
            new BlockProvider() { // IncDec as expression
                @Override
                public Tuple createBlock(IncDecProvider idp, int varInitValue) {
                    String varName = "myVar";
                    return block(Arrays.asList(
                        declareVar(Descriptor.INT, varName),
                        assignVar(varName, literal(varInitValue)),
                        ret(idp.createAST(varName))
                    ));
                }

                @Override
                public int expectedValue(int varModExpectedValue, int incDecExpectedValue) {
                    return incDecExpectedValue;
                }
            },
            new BlockProvider() { // IncDec as statement
                @Override
                public Tuple createBlock(IncDecProvider idp, int varInitValue) {
                    String varName = "myVar";
                    return block(Arrays.asList(
                        declareVar(Descriptor.INT, varName),
                        assignVar(varName, literal(varInitValue)),
                        idp.createAST(varName),
                        ret(accessVar(varName))
                    ));
                }

                @Override
                public int expectedValue(int varModExpectedValue, int incDecExpectedValue) {
                    return varModExpectedValue;
                }
            }
        };

        IncDecProvider[] incDecProviders = new IncDecProvider[] {
            new IncDecProvider() { // inc 1, pre
                @Override
                public int modVar(int var) { return var + 1; }

                @Override
                public int result(int var) { return var + 1; }

                @Override
                public Tuple createAST(String varName) { return intIncVar(varName, IncDec.TIMING_PRE, 1); }
            },
            new IncDecProvider() { // inc 1, post
                @Override
                public int modVar(int var) { return var + 1; }

                @Override
                public int result(int var) { return var; }

                @Override
                public Tuple createAST(String varName) { return intIncVar(varName, IncDec.TIMING_POST, 1); }
            },
            new IncDecProvider() { // dec 1, pre
                @Override
                public int modVar(int var) { return var - 1; }

                @Override
                public int result(int var) { return var - 1; }

                @Override
                public Tuple createAST(String varName) { return intIncVar(varName, IncDec.TIMING_PRE, -1); }
            },
            new IncDecProvider() { // dec 1, post
                @Override
                public int modVar(int var) { return var - 1; }

                @Override
                public int result(int var) { return var; }

                @Override
                public Tuple createAST(String varName) { return intIncVar(varName, IncDec.TIMING_POST, -1); }
            }
        };

        ArrayList<Object[]> values = new ArrayList<>();

        for(BlockProvider bp: blockProviders) {
            for(IncDecProvider idp: incDecProviders)
                values.add(new Object[]{bp, idp});
        }

        return values;
    }

    @Test
    public void testIncPre() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        int varInit = 10;
        int varMod = incDecProvider.modVar(varInit);
        int incDecExpectedValue = incDecProvider.result(varInit);

        String type = Descriptor.INT;
        Tuple ast = blockProvider.createBlock(incDecProvider, varInit);

        CommonTest.testMethodBody(ast, type, actualValue ->
                assertEquals(blockProvider.expectedValue(varMod, incDecExpectedValue), actualValue));
    }

    private interface BlockProvider {
        Tuple createBlock(IncDecProvider idp, int preValue);
        int expectedValue(int preValue, int incDecResult);
    }

    private interface IncDecProvider {
        int modVar(int var);
        int result(int var);
        Tuple createAST(String varName);
    }
}
