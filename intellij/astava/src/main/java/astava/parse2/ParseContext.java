package astava.parse2;

public interface ParseContext<Failure> {
    <T, Success> ParseResult<T, Success, Failure> success(Source<T> source, Success value);
    <T, Success> ParseResult<T, Success, Failure> failure(Source<T> source, Failure value);
}
