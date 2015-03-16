package astava.parse2;

import java.util.function.Function;

public interface Parser<T, Success, Failure> {
    ParseResult<T, Success, Failure> parse(Source<T> source);

    default <R2> Parser<T, Pair<Success, R2>, Failure> then(Parser<T, R2, Failure> next) {
        Parser<T, Success, Failure> self = this;

        return source -> {
            ParseResult<T, Success, Failure> selfResult = self.parse(source);
            if(selfResult.isSuccess()) {
                source = selfResult.getSource();
                ParseResult<T, R2, Failure> nextResult = next.parse(source);
                if(nextResult.isSuccess()) {
                    Pair<Success, R2> value = new Pair<Success, R2>(selfResult.getValueIfSuccess(), nextResult.getValueIfSuccess());
                    return new ParseSuccess<T, Pair<Success, R2>, Failure>(nextResult.getSource(), value);
                } else {
                    return new ParseFailure<T, Pair<Success, R2>, Failure>(nextResult.getSource(), nextResult.getValueIfFailure());
                }
            } else
                return new ParseFailure<T, Pair<Success, R2>, Failure>(selfResult.getSource(), selfResult.getValueIfFailure());
        };
    }

    default <NewSuccess> Parser<T, NewSuccess, Failure> map(Function<Success, NewSuccess> mapper) {
        Parser<T, Success, Failure> self = this;

        return source -> {
            ParseResult<T, Success, Failure> selfResult = self.parse(source);
            if(selfResult.isSuccess()) {
                NewSuccess newSuccess = mapper.apply(selfResult.getValueIfSuccess());
                return new ParseSuccess<T, NewSuccess, Failure>(selfResult.getSource(), newSuccess);
            } else
                return new ParseFailure<T, NewSuccess, Failure>(selfResult.getSource(), selfResult.getValueIfFailure());
        };
    }
}
