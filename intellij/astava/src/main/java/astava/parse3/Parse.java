package astava.parse3;

import java.util.Arrays;
import java.util.function.*;
import java.util.stream.Collectors;

public class Parse {
    public static <TIn, TOut> LeafParser<TIn, TOut> success() {
        return new LeafParser<TIn, TOut>() {
            @Override
            public void parse(Cursor<TIn> cursor, Matcher<TIn, TOut> matcher) {
                matcher.visitSuccess();
            }

            @Override
            public String toString() {
                return "T";
            }
        };
    }

    public static <TIn, TOut> LeafParser<TIn, TOut> test(Predicate<TIn> predicate) {
        return new LeafParser<TIn, TOut>() {
            @Override
            public void parse(Cursor<TIn> cursor, Matcher<TIn, TOut> matcher) {
                if(!cursor.atEnd() && predicate.test(cursor.peek()))
                    matcher.visitSuccess();
                else
                    matcher.visitFailure();
            }

            @Override
            public String toString() {
                return "test(" + predicate + ")";
            }
        };
    }

    public static <TIn, TOut> LeafParser<TIn, TOut> cast(Class<TOut> cOut) {
        return new LeafParser<TIn, TOut>() {
            @Override
            public void parse(Cursor<TIn> cursor, Matcher<TIn, TOut> matcher) {
                if(!cursor.atEnd() && cOut.isInstance(cursor.peek())) {
                    matcher.put(cOut.cast(cursor.peek()));
                    matcher.visitSuccess();
                } else {
                    matcher.visitFailure();
                }
            }

            @Override
            public String toString() {
                return "cast(" + cOut + ")";
            }
        };
    }

    public static <TIn, TOut> LeafParser<TIn, TOut> consume() {
        return new LeafParser<TIn, TOut>() {
            @Override
            public void parse(Cursor<TIn> cursor, Matcher<TIn, TOut> matcher) {
                if(!cursor.atEnd()) {
                    cursor.consume();
                    matcher.visitSuccess();
                } else
                    matcher.visitFailure();
            }

            @Override
            public String toString() {
                return "++";
            }
        };
    }

    /*public static <TIn> LeafParser<TIn, TIn> copy() {
        return new LeafParser<TIn, TIn>() {
            @Override
            public void parse(Cursor<TIn> cursor, Matcher<TIn, TIn> matcher) {
                if(!cursor.atEnd()) {
                    TIn value = cursor.peek();
                    matcher.put(value);
                    matcher.visitSuccess();
                } else
                    matcher.visitFailure();
            }

            @Override
            public String toString() {
                return "^";
            }
        };
    }*/

    public static <TIn extends TOut, TOut> LeafParser<TIn, TOut> copy() {
        return new LeafParser<TIn, TOut>() {
            @Override
            public void parse(Cursor<TIn> cursor, Matcher<TIn, TOut> matcher) {
                if(!cursor.atEnd()) {
                    TIn value = cursor.peek();
                    matcher.put(value);
                    matcher.visitSuccess();
                } else
                    matcher.visitFailure();
            }

            @Override
            public String toString() {
                return "^";
            }
        };
    }

    public static <TIn, TOut> LeafParser<TIn, TOut> atEnd() {
        return new LeafParser<TIn, TOut>() {
            @Override
            public void parse(Cursor<TIn> cursor, Matcher<TIn, TOut> matcher) {
                if(cursor.atEnd()) {
                    matcher.visitSuccess();
                } else
                    matcher.visitFailure();
            }

            @Override
            public String toString() {
                return "<at-end>";
            }
        };
    }

    public static <TIn, TInter, TOut> Parser<TIn, TOut> wrap(Parser<TIn, TInter> parser, BiFunction<Cursor<TIn>, Matcher<TIn, TOut>, Consumer<Input<TInter>>> wrapper) {
        return new ParserWrapper<>(parser, wrapper);
    }

    public static class PipeIn<TIn, TInter, TOut> implements Parser<TIn, TOut> {
        private Parser<TIn, Input<TInter>> first;
        private Parser<TInter, TOut> second;

        public PipeIn(Parser<TIn, Input<TInter>> first, Parser<TInter, TOut> second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public void parse(Cursor<TIn> cursor, Matcher<TIn, TOut> matcher) {
            Matcher<TIn, Input<TInter>> firstMatcher = matcher.beginVisit(first, cursor);
            first.parse(cursor, firstMatcher);

            if(firstMatcher.isMatch()) {
                Input<TInter> toPipeIn = firstMatcher.production().cursor().peek();

                Cursor<TInter> toPipeInCursor = toPipeIn.cursor();
                Matcher<TInter, TOut> secondMatcher = matcher.beginVisit(second, toPipeInCursor);
                second.parse(toPipeInCursor, secondMatcher);

                if(secondMatcher.isMatch()) {
                    matcher.visitSuccess();
                    secondMatcher.propagateOutput(matcher);
                    matcher.visitSuccess();
                    return;
                }
            }

            matcher.visitFailure();
        }

        @Override
        public String toString() {
            return first + ":>>" + second;
        }
    }

    public static class Pipe<TIn, TInter, TOut> implements Parser<TIn, TOut> {
        private Parser<TIn, TInter> first;
        private Parser<TInter, TOut> second;

        public Pipe(Parser<TIn, TInter> first, Parser<TInter, TOut> second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public void parse(Cursor<TIn> cursor, Matcher<TIn, TOut> matcher) {
            Matcher<TIn, TInter> firstMatcher = matcher.beginVisit(first, cursor);
            first.parse(cursor, firstMatcher);

            if(firstMatcher.isMatch()) {
                Cursor<TInter> secondCursor = firstMatcher.production().cursor();
                Matcher<TInter, TOut> secondMatcher = matcher.beginVisit(second, secondCursor);
                second.parse(secondCursor, secondMatcher);

                if(secondMatcher.isMatch()) {
                    secondMatcher.propagateOutput(matcher);
                    matcher.visitSuccess();
                    return;
                }
            }

            matcher.visitFailure();
        }

        @Override
        public String toString() {
            return first + ">>" + second;
        }
    }

    public static class PipeOut<TIn, TInter, TOut> implements Parser<TIn, TOut> {
        private Parser<TIn, TInter> first;
        private Parser<Input<TInter>, TOut> second;

        public PipeOut(Parser<TIn, TInter> first, Parser<Input<TInter>, TOut> second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public void parse(Cursor<TIn> cursor, Matcher<TIn, TOut> matcher) {
            Matcher<TIn, TInter> firstMatcher = matcher.beginVisit(first, cursor);
            first.parse(cursor, firstMatcher);

            if(firstMatcher.isMatch()) {
                Input<TInter> secondInput = firstMatcher.production();
                Cursor<Input<TInter>> secondInputReified = new ListCursor<>(Arrays.asList(secondInput));
                Matcher<Input<TInter>, TOut> secondMatcher = matcher.beginVisit(second, secondInputReified);
                second.parse(secondInputReified, secondMatcher);

                if(secondMatcher.isMatch()) {
                    matcher.visitSuccess();
                    secondMatcher.propagateOutput(matcher);
                    matcher.visitSuccess();
                    return;
                }
            }

            matcher.visitFailure();
        }

        @Override
        public String toString() {
            return first + ">>:" + second;
        }
    }

    public static <TIn, TInter, TOut> Parser<TIn, TOut> pipeIn(Parser<TIn, Input<TInter>> first, Parser<TInter, TOut> second) {
        return new PipeIn(first, second);
    }

    public static <TIn, TInter, TOut> Parser<TIn, TOut> pipe(Parser<TIn, TInter> first, Parser<TInter, TOut> second) {
        return new Pipe(first, second);
    }

    public static <TIn, TInter, TOut> Parser<TIn, TOut> pipeOut(Parser<TIn, TInter> first, Parser<Input<TInter>, TOut> second) {
        return new PipeOut(first, second);
    }

    public static <TIn, TOut> Parser<TIn, TOut> sequence(Parser<TIn, TOut>... parsers) {
        return new Parser<TIn, TOut>() {
            @Override
            public void parse(Cursor<TIn> cursor, Matcher<TIn, TOut> matcher) {
                CursorState start = cursor.state();

                for(Parser<TIn, TOut> parser: parsers) {
                    Matcher<TIn, TOut> elementMatcher = matcher.beginVisit(parser, cursor);
                    parser.parse(cursor, elementMatcher);
                    if(!elementMatcher.isMatch()) {
                        matcher.visitFailure();
                        start.restore();
                        return;
                    }
                    elementMatcher.propagateOutput(matcher);
                }

                matcher.visitSuccess();
            }

            @Override
            public Parser<TIn, TOut> then(Parser<TIn, TOut> next) {
                Parser<TIn, TOut>[] newAlternatives = (Parser<TIn, TOut>[])new Parser[parsers.length + 1];
                System.arraycopy(parsers, 0, newAlternatives, 0, parsers.length);
                newAlternatives[newAlternatives.length - 1] = next;
                return sequence(newAlternatives);
            }

            @Override
            public String toString() {
                return Arrays.asList(parsers).stream().map(a -> a.toString()).collect(Collectors.joining(", "));
            }
        };
    }

    public static <TIn, TOut> Parser<TIn, TOut> decision(Parser<TIn, TOut>... alternatives) {
        return new Parser<TIn, TOut>() {
            @Override
            public void parse(Cursor<TIn> cursor, Matcher<TIn, TOut> matcher) {
                for(Parser<TIn, TOut> alternative: alternatives) {
                    CursorState start = cursor.state();
                    Matcher<TIn, TOut> alternativeMatcher = matcher.beginVisit(alternative, cursor);
                    alternative.parse(cursor, alternativeMatcher);
                    if(alternativeMatcher.isMatch()) {
                        alternativeMatcher.propagateOutput(matcher);
                        matcher.visitSuccess();
                        return;
                    } else
                        start.restore();
                }

                matcher.visitFailure();
            }

            @Override
            public Parser<TIn, TOut> or(Parser<TIn, TOut> other) {
                Parser<TIn, TOut>[] newAlternatives = (Parser<TIn, TOut>[])new Parser[alternatives.length + 1];
                System.arraycopy(alternatives, 0, newAlternatives, 0, alternatives.length);
                newAlternatives[newAlternatives.length - 1] = other;
                return decision(newAlternatives);
            }

            @Override
            public String toString() {
                return Arrays.asList(alternatives).stream().map(a -> a.toString()).collect(Collectors.joining(" | "));
            }
        };
    }

    public static <TIn, TOut> Parser<TIn, TOut> multi(Parser<TIn, TOut> parser) {
        return new Parser<TIn, TOut>() {
            @Override
            public void parse(Cursor<TIn> cursor, Matcher<TIn, TOut> matcher) {
                while(true) {
                    Matcher<TIn, TOut> iterationMatcher = matcher.beginVisit(parser, cursor);
                    parser.parse(cursor, iterationMatcher);

                    if(iterationMatcher.isMatch()) {
                        iterationMatcher.propagateOutput(matcher);
                    } else {
                        break;
                    }
                }

                matcher.visitSuccess();
            }

            @Override
            public String toString() {
                return "(" + parser + ")*";
            }
        };
    }

    public static <TIn, TOut> Parser<TIn, TOut> onceOrMore(Parser<TIn, TOut> parser) {
        return parser.then(parser.multi());
    }

    public static <TIn, TOut> LeafParser<TIn, TOut> map(Function<TIn, TOut> mapper) {
        return new LeafParser<TIn, TOut>() {
            @Override
            public void parse(Cursor<TIn> cursor, Matcher<TIn, TOut> matcher) {
                if(!cursor.atEnd()) {
                    TOut value = mapper.apply(cursor.peek());
                    matcher.put(value);
                    matcher.visitSuccess();
                } else
                    matcher.visitFailure();
            }

            @Override
            public String toString() {
                return mapper.toString();
            }
        };
    }

    public static <TIn, TElementIn, TElementOut, TOut> LeafParser<TIn, TOut> descentReduce(Function<TIn, Input<TElementIn>> expander, Function<Input<TElementOut>, TOut> reducer, Parser<TElementIn, TElementOut> elementParser) {
        /*return new LeafParser<TIn, TOut>() {
            @Override
            public void parse(Cursor<TIn> cursor, Matcher<TIn, TOut> matcher) {
                if(!cursor.atEnd()) {
                    Input<TElementIn> elementsIn = expander.apply(cursor.peek());
                    Cursor<TElementIn> elementsInCursor = elementsIn.cursor();
                    Matcher<TElementIn, TElementOut> elementsMatcher = matcher.beginVisit(elementParser, elementsInCursor);
                    elementParser.parse(elementsInCursor, elementsMatcher);

                    if(elementsMatcher.isMatch()) {
                        TOut reduction = reducer.apply(elementsMatcher.production());
                        matcher.put(reduction);
                        matcher.visitSuccess();
                    } else
                        matcher.visitFailure();
                } else
                    matcher.visitFailure();
            }

            @Override
            public String toString() {
                return expander + " :>> " + elementParser + " >>: " + reducer;
            }
        };*/

        return descentProduce(expander, (production, matcher) -> {
            TOut reduction = reducer.apply(production);
            matcher.put(reduction);
        }, elementParser);
    }

    public static <TIn, TElementIn, TElementOut, TOut> LeafParser<TIn, TOut> descentProduce(Function<TIn, Input<TElementIn>> expander, BiConsumer<Input<TElementOut>, Matcher<TIn, TOut>> producer, Parser<TElementIn, TElementOut> elementParser) {
        return new LeafParser<TIn, TOut>() {
            @Override
            public void parse(Cursor<TIn> cursor, Matcher<TIn, TOut> matcher) {
                if(!cursor.atEnd()) {
                    Input<TElementIn> elementsIn = expander.apply(cursor.peek());
                    Cursor<TElementIn> elementsInCursor = elementsIn.cursor();
                    Matcher<TElementIn, TElementOut> elementsMatcher = matcher.beginVisit(elementParser, elementsInCursor);
                    elementParser.parse(elementsInCursor, elementsMatcher);

                    if(elementsMatcher.isMatch()) {
                        producer.accept(elementsMatcher.production(), matcher);

                        matcher.visitSuccess();
                    } else
                        matcher.visitFailure();
                } else
                    matcher.visitFailure();
            }

            @Override
            public String toString() {
                return expander + " :>> " + elementParser + " >>: " + producer;
            }
        };
    }
}
