package astava.parse;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Parser<TIn, TOut> {
    void parse(Cursor<TIn> cursor, Matcher<TIn, TOut> matcher);
    default Matcher<TIn, TOut> parseInit(Cursor<TIn> cursor, BiFunction<Parser<TIn, TOut>, Cursor<TIn>, Matcher<TIn, TOut>> matcherFunction) {
        Matcher<TIn, TOut> matcher = matcherFunction.apply(this, cursor);
        parse(cursor, matcher);
        return matcher;
    }

    default <TIn2> void parseFrom(Cursor<TIn> cursor, Matcher<TIn2, TOut> matcher) {
        Matcher<TIn, TOut> m = matcher.beginVisit(this, cursor);
        this.parse(cursor, m);
        m.propagate(matcher);
    }

    default Parser<TIn, TOut> or(Parser<TIn, TOut> other) {
        return Parse.decision(this, other);
    }

    default Parser<TIn, TOut> then(Parser<TIn, TOut> next) {
        return Parse.sequence(this, next);
    }

    default <TOut2> Parser<TIn, Pair<Input<TOut>, Input<TOut2>>> compose(Parser<TIn, TOut2> next) {
        return Parse.compose(this, next);
    }

    default <TOut2> Parser<TIn, TOut2> pipe(Parser<TOut, TOut2> next) {
        return Parse.pipe(this, next);
    }

    default <TOut2> Parser<TIn, TOut2> pipeOut(Parser<Input<TOut>, TOut2> next) {
        return Parse.pipeOut(this, next);
    }

    default Parser<TIn, TOut> multi() {
        return Parse.multi(this);
    }

    default Parser<TIn, TOut> not() {
        return Parse.not(this);
    }

    default Parser<TIn, TOut> maybe() {
        return Parse.maybe(this);
    }

    default Parser<TIn, TOut> onceOrMore() {
        return Parse.onceOrMore(this);
    }

    default <TOut2> Parser<TIn, TOut2> wrap(BiFunction<Cursor<TIn>, Matcher<TIn, TOut2>, Consumer<Input<TOut>>> wrapper) {
        return Parse.wrap(this, wrapper);
    }

    default Parser<TIn, TOut> pipeTo(Consumer<Input<TOut>> consumer) {
        return wrap((c, m) -> consumer);
    }

    default Parser<TIn, TOut> pipe1To(Consumer<TOut> consumer) {
        return pipeTo(p -> p.take1(consumer));
    }

    default Parser<TIn, TOut> pipe2To(BiConsumer<TOut, TOut> consumer) {
        return pipeTo(p -> p.take2(consumer));
    }

    default void pipeParseTo(Cursor<TIn> cursor, Matcher<TIn, TOut> matcher, Consumer<Input<TOut>> consumer) {
        this.<TOut>pipeTo(consumer).parse(cursor, matcher);
    }

    default <TOut2> Parser<TIn, TOut2> reduce(Function<Input<TOut>, TOut2> reducer) {
        return wrap((cursor, matcher) -> production -> {
            TOut2 out = reducer.apply(production);
            matcher.put(out);
        });
    }

    default <TOut2> Parser<TIn, TOut2> reduce1(Function<TOut, TOut2> reducer) {
        return reduce(production -> {
            Cursor<TOut> cursor = production.cursor();
            TOut first = cursor.peek();
            return reducer.apply(first);
        });
    }

    default <TOut2, T1, T2> Parser<TIn, TOut2> reduce2(BiFunction<T1, T2, TOut2> reducer) {
        return reduce(production -> {
            Cursor<TOut> cursor = production.cursor();
            T1 first = (T1)cursor.peek();
            cursor.consume();
            T2 second = (T2)cursor.peek();
            return reducer.apply(first, second);
        });
    }

    default <TOut2> Parser<TIn, TOut2> produce(BiConsumer<Input<TOut>, Matcher<TIn, TOut2>> producer) {
        return wrap((cursor, matcher) -> production -> {
            producer.accept(production, matcher);
        });
    }

    default <TOut2> Parser<TIn, TOut2> consume(Consumer<Input<TOut>> consumer) {
        return wrap((cursor, matcher) -> production -> {
            consumer.accept(production);
        });
    }

    default <TOut2, T1, T2> Parser<TIn, TOut2> consume2(BiConsumer<T1, T2> consumer) {
        return consume(production -> {
            Cursor<TOut> cursor = production.cursor();
            T1 first = (T1)cursor.peek();
            cursor.consume();
            T2 second = (T2)cursor.peek();
            consumer.accept(first, second);
        });
    }
}
