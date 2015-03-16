package astava.parse2;

public interface ParseContext<Failure> {
    <T, Success> ParseResult<T, Success, Failure> success(Source<T> source, Success value);
    // Keep this failure for any future failure
    <T, Success> ParseResult<T, Success, Failure> trackFailure(Source<T> source, Failure value);
    // Create new failure; any past failures are part of this failure
    <T, Success> ParseResult<T, Success, Failure> failure(Source<T> source, Failure value);
}
