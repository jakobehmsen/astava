package astava.parse;

public class NullMatcher<TIn, TOut> extends AbstractMatcher<TIn, TOut> {
    public static final NullMatcher INSTANCE = new NullMatcher();
}
