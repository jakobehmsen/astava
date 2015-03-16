package astava.parse2;

public class RootParseContext<Failure> implements ParseContext<Failure> {
    @Override
    public <T, Success> ParseResult<T, Success, Failure> success(Source<T> source, Success value) {
        return new ParseSuccess<>(null, source, value);
    }

    @Override
    public <T, Success> ParseResult<T, Success, Failure> trackFailure(Source<T> source, Failure value) {
        return new ParseFailure<>(null, source, value, true);
    }

    @Override
    public <T, Success> ParseResult<T, Success, Failure> failure(Source<T> source, Failure value) {
        return new ParseFailure<>(null, source, value, false);
    }
}
