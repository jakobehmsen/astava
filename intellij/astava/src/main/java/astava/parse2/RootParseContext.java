package astava.parse2;

public class RootParseContext<Failure> implements ParseContext<Failure> {
    @Override
    public <T, Success> ParseResult<T, Success, Failure> success(Source<T> source, Success value) {
        return new ParseSuccess<T, Success, Failure>(this, source, value);
    }

    @Override
    public <T, Success> ParseResult<T, Success, Failure> failure(Source<T> source, Failure value) {
        return new ParseFailure<T, Success, Failure>(this, source, value);
    }
}
