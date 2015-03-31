package astava.parse;

public class SkipParser<TIn, TOut> extends MarkerParser<TIn, TOut> {
    public SkipParser(astava.parse.Parser<TIn, TOut> parser) {
        super(parser);
    }
}
