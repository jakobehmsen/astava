package astava.parse2;

import java.util.function.Function;

public interface Parser<T, Success, Failure> {
    //ParseResult<T, Success, Failure> parse(Source<T> source);

    ParseResult<T, Success, Failure> parse(ParseContext<Failure> ctx, Source<T> source);

    default Parser<T, Success, Failure> or(Parser<T, Success, Failure> other) {
        Parser<T, Success, Failure> self = this;

        return (ctx, source) -> {
            ParseResult<T, Success, Failure> selfResult = self.parse(ctx, source);
            if(selfResult.isFailure()) {
                return other.parse(selfResult, source);
                /*
                ParseResult<T, Success, Failure> otherResult = other.parse(selfResult, source);
                if(otherResult.isSuccess()) {
                    return otherResult;
                } else {
                    //Pair<Failure, Failure> value = new Pair<Failure, Failure>(selfResult.getValueIfFailure(), nextResult.getValueIfFailure());

                    // Somehow, generically, compose first and second failure value.

                    Failure value;
                    if(selfResult.getValueIfFailure() instanceof CharSequence && otherResult.getValueIfFailure() instanceof CharSequence) {
                        StringBuilder sb = new StringBuilder();
                        sb.append((CharSequence)selfResult.getValueIfFailure());
                        sb.append((CharSequence)otherResult.getValueIfFailure());
                        value = (Failure)sb.toString();
                    } else {
                        value = otherResult.getValueIfFailure();
                    }

                    return new ParseFailure<T, Success, Failure>(source, value);
                }
                */
            }

            return selfResult;
        };
    }

    // Should be pair?
    default <SuccessNext> Parser<T, Pair<Success, SuccessNext>, Failure> then(Parser<T, SuccessNext, Failure> next) {
        Parser<T, Success, Failure> self = this;

        return (ctx, source) -> {
            ParseResult<T, Success, Failure> selfResult = self.parse(ctx, source);
            if(selfResult.isSuccess()) {
                source = selfResult.getSource();
                //return next.parse(selfResult, source);
                ParseResult<T, SuccessNext, Failure> nextResult = next.parse(selfResult, source);
                if(nextResult.isSuccess()) {
                    Pair<Success, SuccessNext> value = new Pair<>(selfResult.getValueIfSuccess(), nextResult.getValueIfSuccess());
                    //return new ParseSuccess<T, Pair<Success, SuccessNext>, Failure>(nextResult.getSource(), value);
                    return selfResult.success(nextResult.getSource(), value);
                } else {
                    //return selfResult.failure(nextResult.getSource(), nextResult.getValueIfFailure());
                    return (ParseResult<T, Pair<Success, SuccessNext>, Failure>)nextResult;
                }
            } else
                //return (ParseResult<T, SuccessNext, Failure>)selfResult;
                //return selfResult.failure(selfResult.getSource(), selfResult.getValueIfFailure());
                return (ParseResult<T, Pair<Success, SuccessNext>, Failure>)selfResult;
        };
    }

    default <R2> Parser<T, R2, Failure> ignoreThen(Parser<T, R2, Failure> next) {
        Parser<T, Success, Failure> self = this;

        return (ctx, source) -> {
            ParseResult<T, Success, Failure> selfResult = self.parse(ctx, source);
            if(selfResult.isSuccess()) {
                source = selfResult.getSource();
                return next.parse(selfResult, source);
            } else
                //return new ParseFailure<T, R2, Failure>(selfResult.getSource(), selfResult.getValueIfFailure());
                //return ctx.failure(selfResult.getSource(), selfResult.getValueIfFailure());
                return (ParseResult<T, R2, Failure>)selfResult;
        };
    }

    default <R2> Parser<T, Success, Failure> thenIgnore(Parser<T, R2, Failure> next) {
        Parser<T, Success, Failure> self = this;

        return (ctx, source) -> {
            ParseResult<T, Success, Failure> selfResult = self.parse(ctx, source);
            if(selfResult.isSuccess()) {
                source = selfResult.getSource();
                ParseResult<T, R2, Failure> nextResult = next.parse(selfResult, source);
                if(nextResult.isSuccess()) {
                    Pair<Success, R2> value = new Pair<>(selfResult.getValueIfSuccess(), nextResult.getValueIfSuccess());
                    return nextResult.success(nextResult.getSource(), selfResult.getValueIfSuccess());
                } else {
                    //return nextResult.failure(nextResult.getSource(), nextResult.getValueIfFailure());
                    return (ParseResult<T, Success, Failure>)nextResult;
                }
            } else
                //return new ParseFailure<T, Success, Failure>(selfResult.getSource(), selfResult.getValueIfFailure());
                //return ctx.failure(selfResult.getSource(), selfResult.getValueIfFailure());
                return selfResult;
        };
    }

    default <NewSuccess> Parser<T, NewSuccess, Failure> map(Function<Success, NewSuccess> mapper) {
        Parser<T, Success, Failure> self = this;

        return (ctx, source) -> {
            ParseResult<T, Success, Failure> selfResult = self.parse(ctx, source);
            if(selfResult.isSuccess()) {
                NewSuccess newSuccess = mapper.apply(selfResult.getValueIfSuccess());
                return selfResult.success(selfResult.getSource(), newSuccess);
            } else
                //return ctx.failure(selfResult.getSource(), selfResult.getValueIfFailure());
                return (ParseResult<T, NewSuccess, Failure>)selfResult;
        };
    }

    default Parser<T, Success, Failure> frame(Object description) {
        Parser<T, Success, Failure> self = this;

        return (ctx, source) -> {
            ParseFrameBuilder<T, Success, Failure> frameBuilder =
                (ParseFrameBuilder<T, Success, Failure>)self.parse(new ParseFrameBuilder<>(ctx, null, description), source);
            return frameBuilder.build();
        };
    }
}
