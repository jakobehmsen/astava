package astava.samples;

import astava.core.Atom;
import astava.core.Node;

import astava.core.Tuple;
import astava.java.*;
import astava.parse3.*;
import astava.parse3.Cursor;
import astava.parse3.charsequence.CharSequenceCursor;
import astava.parse3.charsequence.LineColumnCursorStateFactory;
import astava.parse3.charsequence.CharParse;
import astava.parse3.tree.NodeParse;
import astava.parse3.tree.OpRouter;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static astava.java.Factory.*;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Hashtable<String, Object> variables = new Hashtable<>();
        Impava impava = new Impava(variables);
        impava.parseInit(new CharSequenceCursor("i = 768 jk = 'A string'"), (p, c) -> new astava.parse3.CommonMatcher<Character, Object>());

        System.out.println(variables);

        if(1 != 2)
            return;

        class FailureInfo {
            private astava.parse3.Parser parser;
            private Cursor input;
            private CursorState state;
            private int depth;

            FailureInfo(astava.parse3.Parser parser, Cursor input, CursorState position, int depth) {
                this.parser = parser;
                this.input = input;
                this.state = position;
                this.depth = depth;
            }
        }

        class SkipParser<TIn, TOut> extends MarkerParser<TIn, TOut> {
            public SkipParser(astava.parse3.Parser<TIn, TOut> parser) {
                super(parser);
            }
        }

        class TraceMatcher<TIn, TOut> extends astava.parse3.AbstractMatcher<TIn, TOut> {
            private astava.parse3.Parser<TIn, TOut> parser;
            private Cursor<TIn> input;
            private int depth;

            TraceMatcher(astava.parse3.Parser<TIn, TOut> parser, Cursor<TIn> input, int depth) {
                this.parser = parser;
                this.input = input;
                this.depth = depth;
                System.out.println(getIndention() + "Begin parse: " + parser + " " + input.state() + "...");
            }

            private String getIndention() {
                return IntStream.range(0, depth).mapToObj(d -> "    ").collect(Collectors.joining());
            }

            @Override
            public void visitSuccess() {
                System.out.println(getIndention() + "Success: " + input.state());
            }

            @Override
            public void visitFailure() {
                System.out.println(getIndention() + "Failure: " + input.state());
            }

            @Override
            public <TIn, TOut> astava.parse3.Matcher<TIn, TOut> beginVisit(astava.parse3.Parser<TIn, TOut> parser, Cursor<TIn> input) {
                return new TraceMatcher(parser, input, depth + 1);
            }
        }

        class FailureCollector<TIn, TOut> extends astava.parse3.AbstractMatcher<TIn, TOut> {
            private astava.parse3.Parser<TIn, TOut> parser;
            private Cursor<TIn> input;
            private List<FailureInfo> failures;
            private boolean collectFailures;
            private int depth;

            FailureCollector(astava.parse3.Parser<TIn, TOut> parser, Cursor<TIn> input, int depth, List<FailureInfo> failures, boolean collectFailures) {
                this.parser = parser;
                this.input = input;
                this.depth = depth;
                this.failures = failures;
                this.collectFailures = collectFailures;

                if(parser instanceof SkipParser)
                    this.collectFailures = false;
            }

            @Override
            public void visitFailure() {
                if(parser instanceof LeafParser && collectFailures)
                    failures.add(new FailureInfo(parser, input, input.state(), depth));
            }

            @Override
            public <TIn, TOut> astava.parse3.Matcher<TIn, TOut> beginVisit(astava.parse3.Parser<TIn, TOut> parser, Cursor<TIn> input) {
                return new FailureCollector(parser, input, depth + 1, failures, collectFailures);
            }
        }

        astava.parse3.Parser<Character, Node> grammar = new DelegateParser<Character, Node>() {
            private astava.parse3.Parser<Character, Node> ws =
                new SkipParser<>(CharParse.<Node>isWhitespace().then(Parse.consume()).multi());
            private astava.parse3.Parser<Character, Node> element =
                ref(() -> this.word)
                .or(ref(() -> this.tree))
                .or(ref(() -> this.dyn));
            private astava.parse3.Parser<Character, Node> elements =
                ref(() -> this.ws)
                .then(
                    ref(() -> this.element)
                    .then(ref(() -> this.ws))
                    .multi()
                );
            private astava.parse3.Parser<Character, Node> word =
                (CharParse.<Character>isLetter().or(CharParse.isEither("+-"))).then(Parse.copy()).then(Parse.consume()).onceOrMore()
                .wrap((cursor, matcher) -> {
                    CursorState start = cursor.state();

                    return production -> {
                        CursorState end = cursor.state();
                        System.out.println("Matched atom from " + start + " to " + end + ".");
                        String value = production.stream().map(c -> "" + c).collect(Collectors.joining());
                        matcher.put(new Atom(new Symbol(value)));
                    };
                });
            private astava.parse3.Parser<Character, Node> tree2 =
                CharParse.<Node>isChar('(').then(Parse.consume())
                .then(ref(() -> this.elements))
                .then(CharParse.isChar(')')).then(Parse.consume())
                .pipeOut(Parse.map(nodes -> {
                    List<Node> nodesAsList = nodes.stream().collect(Collectors.toList());
                    return new Tuple(nodesAsList);
                }));

            private astava.parse3.Parser<Character, Node> tree =
                CharParse.<Node>isChar('(').then(Parse.consume())
                .then(ref(() -> this.elements))
                .then(CharParse.isChar(')')).then(Parse.consume())
                .wrap((cursor, matcher) -> {
                    CursorState start = cursor.state();

                    return production -> {
                        CursorState end = cursor.state();
                        System.out.println("Matched tuple from " + start + " to " + end + ".");

                        List<Node> nodesAsList = production.stream().collect(Collectors.toList());
                        matcher.put(new Tuple(nodesAsList));
                    };
                });

            private astava.parse3.Parser<Character, Node> dyn =
                CharParse.<Character>isChar('$').then(Parse.consume())
                .then(
                    CharParse.<Character>isLetter().then(Parse.copy()).then(Parse.consume()).onceOrMore()
                ).then(CharParse.<Character>isChar(':').then(Parse.consume()))
                .wrap((cursor, matcher) -> {
                    return production -> {
                        String parserName = production.stream().map(c -> "" + c).collect(Collectors.joining());
                        String parserClassName = "astava.samples." + parserName + "Parser";
                        try {
                            Class<? extends astava.parse3.Parser<Character, Node>> parserClass =
                                (Class<? extends astava.parse3.Parser<Character, Node>>) Class.forName(parserClassName);
                            astava.parse3.Parser<Character, Node> parser = parserClass.newInstance();
                            parser.parse(cursor, matcher);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    };
                });

            @Override
            public astava.parse3.Parser<Character, Node> createParser() {
                return ref(() -> this.elements);
            }
        };

        ArrayList<FailureInfo> failures = new ArrayList<>();
        String chars =
            //"(byte $Number:56)" + "\n" +
            //"$Number:56" + "\n" +
            //"(add $Number:57 $Number:58)" + "\n" +
            //"(whatever whichever)" + "\n" +
            //"(scopedLabel lbl) (labelScope (scopedLabel lbl) (labelScope (scopedLabel lbl))) (scopedLabel lbl) (labelScope (scopedLabel lbl))" +
            "(+ $Number:56 $Number:56)" + "\n" +
            "";
        //astava.parse3.Matcher<Character, Node> ma = grammar.then(new SkipParser<>(Parse.atEnd()))
        //    .parseInit(new CharSequenceCursor(chars), (p, i) -> new CMatcher(p, i, 0, failures, true));

        CharSequenceCursor cursor = new CharSequenceCursor(chars, new LineColumnCursorStateFactory());
        astava.parse3.Parser<Character, Node> grammar1 = grammar.then(new SkipParser<>(Parse.atEnd()));
        astava.parse3.Matcher<Character, Node> ma = grammar1.parseInit(cursor, (p, i) ->
            new CompositeMatcher<>(Arrays.asList(new astava.parse3.CommonMatcher<>(), new FailureCollector<>(p, i, 0, failures, true))));

        System.out.println("Matching input:");
        System.out.println(chars);
        System.out.println("against parser:");
        System.out.println(grammar1);

        Cursor<Node> frontProduction = ma.production().cursor();

        if(ma.isMatch()) {
            System.out.println("Success:");

            astava.parse3.Parser<Node, Node> macroParser = new DelegateParser<Node, Node>() {
                private astava.parse3.Parser<Node, Node> createLiteralExpander() {
                    return new DelegateParser<Node, Node>() {
                        @Override
                        public astava.parse3.Parser<Node, Node> createParser() {
                            astava.parse3.Parser<Tuple, Node> passOn = Parse.<Tuple, Node>copy().then(Parse.consume());
                            OpRouter primitivePassOn = new OpRouter()
                                .put(new Symbol("boolean"), passOn)
                                .put(new Symbol("byte"), passOn)
                                .put(new Symbol("short"), passOn)
                                .put(new Symbol("int"), passOn)
                                .put(new Symbol("long"), passOn)
                                .put(new Symbol("float"), passOn)
                                .put(new Symbol("double"), passOn);

                            return
                                (
                                    (Parse.<Node, Tuple>cast(Tuple.class).pipe(
                                        primitivePassOn
                                        .or(
                                            NodeParse.descentReduce(ref(() -> this))
                                        )
                                    ))
                                    .or(Parse.<Node, Atom>cast(Atom.class).pipe(
                                            Parse.<Atom, Node>test(a -> a.getValue() instanceof Boolean).then(Parse.map(a ->
                                                new Tuple(new Atom(new Symbol("boolean")), a)))
                                                .or(Parse.<Atom, Node>test(a -> a.getValue() instanceof Byte).then(Parse.map(a ->
                                                    new Tuple(new Atom(new Symbol("byte")), a))))
                                                .or(Parse.<Atom, Node>test(a -> a.getValue() instanceof Short).then(Parse.map(a ->
                                                    new Tuple(new Atom(new Symbol("short")), a))))
                                                .or(Parse.<Atom, Node>test(a -> a.getValue() instanceof Integer).then(Parse.map(a ->
                                                    new Tuple(new Atom(new Symbol("int")), a))))
                                                .or(Parse.<Atom, Node>test(a -> a.getValue() instanceof Long).then(Parse.map(a ->
                                                    new Tuple(new Atom(new Symbol("long")), a))))
                                                .or(Parse.<Atom, Node>test(a -> a.getValue() instanceof Float).then(Parse.map(a ->
                                                    new Tuple(new Atom(new Symbol("float")), a))))
                                                .or(Parse.<Atom, Node>test(a -> a.getValue() instanceof Double).then(Parse.map(a ->
                                                    new Tuple(new Atom(new Symbol("double")), a))))
                                        )
                                    ).or(Parse.<Node, Node>copy())
                                )
                                .then(Parse.consume())
                                .multi();
                        }
                    };
                }

                private astava.parse3.Parser<Node, Node> createLabelScopeProcessor() {
                    return new DelegateParser<Node, Node>() {
                        // Can scopeCount be part of a matcher instead somehow?
                        int scopeCount;
                        //Supplier<astava.parse3.Parser<Node, Node>> labelScopeParserSupplier = () ->
                        //    createParser();

                        @Override
                        public void parse(Cursor<Node> cursor, astava.parse3.Matcher<Node, Node> matcher) {
                            super.parse(cursor, matcher);
                        }

                        @Override
                        public astava.parse3.Parser<Node, Node> createParser() {
                            DelegateParser<Node, Node> labelScopeParser = this;

                            int id = scopeCount++;

                            return new DelegateParser<Node, Node>() {
                                @Override
                                public astava.parse3.Parser<Node, Node> createParser() {
                                    OpRouter labelParser = new OpRouter()
                                        .put(new Symbol("scopedLabel"),
                                            NodeParse.descentReduce(
                                                Parse.<Node, Node>map(a -> new Atom(new Symbol("label")))
                                                    .then(Parse.consume())
                                                    .then(Parse.<Node, Atom>cast(Atom.class).<Node>pipe(Parse.<Atom, Node>map(a ->
                                                        new Atom(id + ((Symbol) a.getValue()).str))))
                                                    .then(Parse.consume())
                                            )
                                        )
                                        .put(new Symbol("scopedGoTo"),
                                            NodeParse.descentReduce(
                                                Parse.<Node, Node>map(a -> new Atom(new Symbol("goTo")))
                                                    .then(Parse.consume())
                                                    .then(Parse.<Node, Atom>cast(Atom.class).<Node>pipe(Parse.<Atom, Node>map(a ->
                                                        new Atom(id + ((Symbol) a.getValue()).str))))
                                                    .then(Parse.consume())
                                            )
                                        )
                                        .put(new Symbol("labelScope"), Parse.<Tuple, Node, Node, Node>descentProduce(
                                            tuple -> new ListInput(tuple),
                                            (nodes, m) -> m.put(nodes),
                                            Parse.<Node, Node>consume().then(ref(() ->
                                                labelScopeParser.createParser()))
                                        ));
                                    return
                                        (
                                            (Parse.<Node, Tuple>cast(Tuple.class).pipe(
                                                labelParser
                                                    .or(
                                                        NodeParse.descentReduce(
                                                            // Pass operator as is, then parse each operand
                                                            Parse.<Node, Node>copy().then(Parse.consume()).then(ref(
                                                                () -> this
                                                            ))
                                                        )
                                                    )
                                            ))
                                            .or(Parse.<Node, Node>copy())
                                        )
                                        .then(Parse.consume())
                                        .multi();
                                }
                            };
                        }
                    };
                }

                private astava.parse3.Parser<Node, Node> createOperatorToBuiltinProcessor() {
                    return new DelegateParser<Node, Node>() {
                        private astava.parse3.Parser<Tuple, Node> arithmeticParser(int arithmeticOperator) {
                            return NodeParse.descentReduce(self(), nodes -> {
                                List<Node> listNodes = nodes.stream().collect(Collectors.toList());
                                return arithmetic((Tuple) listNodes.get(1), (Tuple) listNodes.get(2), arithmeticOperator);
                            });
                        }

                        private astava.parse3.Parser<Tuple, Node> createLiteralParser(Function<Number, Node> literalFunction) {
                            return NodeParse.descentReduce(self(), nodes -> {
                                List<Node> listNodes = nodes.stream().collect(Collectors.toList());
                                Number number = (Number) ((Atom) listNodes.get(1)).getValue();
                                return literalFunction.apply(number);
                            });
                        }

                        @Override
                        public astava.parse3.Parser<Node, Node> createParser() {
                            OpRouter mp = new OpRouter()
                                .put(new Symbol("+"), arithmeticParser(ArithmeticOperator.ADD))
                                .put(new Symbol("-"), arithmeticParser(ArithmeticOperator.SUB))
                                .put(new Symbol("*"), arithmeticParser(ArithmeticOperator.MUL))
                                .put(new Symbol("/"), arithmeticParser(ArithmeticOperator.DIV))
                                .put(new Symbol("%"), arithmeticParser(ArithmeticOperator.REM))

                                .put(new Symbol("byte"), createLiteralParser(number -> literal(number.byteValue())))
                                .put(new Symbol("short"), createLiteralParser(number -> literal(number.shortValue())))
                                .put(new Symbol("int"), createLiteralParser(number -> literal(number.intValue())))
                                .put(new Symbol("long"), createLiteralParser(number -> literal(number.longValue())))
                                .put(new Symbol("float"), createLiteralParser(number -> literal(number.floatValue())))
                                .put(new Symbol("double"), createLiteralParser(number -> literal(number.doubleValue())))
                                ;

                            return (
                                (Parse.<Node, Tuple>cast(Tuple.class).pipe(
                                    mp
                                    .or(
                                        NodeParse.descentReduce(
                                            Parse.<Node, Node>copy().then(Parse.consume()).then(self()),
                                            nodes -> {
                                                List<Node> listNodes = nodes.stream().collect(Collectors.toList());
                                                return block(listNodes);
                                            }
                                        )
                                    )
                                ))
                                .or(Parse.<Node, Node>copy())
                            )
                            .then(Parse.consume())
                            .multi();
                        }
                    }.wrap((cursor, matcher) -> {
                        return nodes -> {
                            List<Node> listNodes = nodes.stream().collect(Collectors.toList());
                            matcher.put(block(listNodes));
                        };
                    });



                    /*return new DelegateProcessor() {
                        private Processor createLiteralParser(Function<Number, Node> literalFunction) {
                            return n -> {
                                Number number = (Number) ((Atom) ((Tuple) n).get(1)).getValue();
                                return literalFunction.apply(number);
                            };
                        }

                        private Processor arithmeticProcessor(int arithmeticOperator) {
                            return forOperands(n ->
                                arithmetic((Tuple) ((Tuple) n).get(1), (Tuple) ((Tuple) n).get(2), arithmeticOperator));
                        }

                        private Processor shiftProcessor(int shiftOperator) {
                            return forOperands(n ->
                                shift((Tuple) ((Tuple) n).get(1), (Tuple) ((Tuple) n).get(2), shiftOperator));
                        }

                        private Processor logicalProcessor(int logicalOperator) {
                            return forOperands(n ->
                                logical((Tuple) ((Tuple) n).get(1), (Tuple) ((Tuple) n).get(2), logicalOperator));
                        }

                        private Processor compareProcessor(int compareOperator) {
                            return forOperands(n ->
                                compare((Tuple) ((Tuple) n).get(1), (Tuple) ((Tuple) n).get(2), compareOperator));
                        }

                        private Node processDeclareVar(Node n) {
                            Tuple t = (Tuple)n;
                            String type = (String)((Atom)t.get(1)).getValue();
                            String name = (String)((Atom)t.get(2)).getValue();
                            return declareVar(type, name);
                        }

                        private Node processAssignVar(Node n) {
                            Tuple t = (Tuple)n;
                            String name = (String)((Atom)t.get(1)).getValue();
                            Tuple value = (Tuple)process(t.get(2));
                            return assignVar(name, value);
                        }

                        private Node processAccessVar(Node n) {
                            Tuple t = (Tuple)n;
                            String name = (String)((Atom)t.get(1)).getValue();
                            return accessVar(name);
                        }

                        @Override
                        protected Processor createProcessor() {
                            MapProcessor mp = new MapProcessor()
                                .put(new Symbol("+"), arithmeticProcessor(ArithmeticOperator.ADD))
                                .put(new Symbol("-"), arithmeticProcessor(ArithmeticOperator.SUB))
                                .put(new Symbol("*"), arithmeticProcessor(ArithmeticOperator.MUL))
                                .put(new Symbol("/"), arithmeticProcessor(ArithmeticOperator.DIV))
                                .put(new Symbol("%"), arithmeticProcessor(ArithmeticOperator.REM))

                                .put(new Symbol("<<"), shiftProcessor(ShiftOperator.SHL))
                                .put(new Symbol(">>"), shiftProcessor(ShiftOperator.SHR))
                                .put(new Symbol(">>>"), shiftProcessor(ShiftOperator.USHR))

                                .put(new Symbol("&&"), logicalProcessor(LogicalOperator.AND))
                                .put(new Symbol("||"), logicalProcessor(LogicalOperator.OR))

                                .put(new Symbol("<"), compareProcessor(RelationalOperator.LT))
                                .put(new Symbol("<="), compareProcessor(RelationalOperator.LE))
                                .put(new Symbol(">"), compareProcessor(RelationalOperator.GT))
                                .put(new Symbol(">="), compareProcessor(RelationalOperator.GE))
                                .put(new Symbol("=="), compareProcessor(RelationalOperator.EQ))
                                .put(new Symbol("!="), compareProcessor(RelationalOperator.NE))

                                .put(new Symbol("byte"), createLiteralParser(number -> literal(number.byteValue())))
                                .put(new Symbol("short"), createLiteralParser(number -> literal(number.shortValue())))
                                .put(new Symbol("int"), createLiteralParser(number -> literal(number.intValue())))
                                .put(new Symbol("long"), createLiteralParser(number -> literal(number.longValue())))
                                .put(new Symbol("float"), createLiteralParser(number -> literal(number.floatValue())))
                                .put(new Symbol("double"), createLiteralParser(number -> literal(number.doubleValue())))

                                .put(new Symbol("declareVar"), n -> processDeclareVar(n))
                                .put(new Symbol("assignVar"), n -> processAssignVar(n))
                                .put(new Symbol("accessVar"), n -> processAccessVar(n))
                                ;

                            Processor stringLiteralProcessor = n ->
                                n instanceof Atom && ((Atom)n).getValue() instanceof String ? literal((String)((Atom)n).getValue()) : null;
                            Processor booleanLiteralProcessor = n ->
                                n instanceof Atom && ((Atom)n).getValue() instanceof Boolean ? literal((boolean)((Atom)n).getValue()) : null;

                            Processor fallbackProcessor =
                                new OperandsProcessor(n ->
                                    this.process(n))
                                    // Generate non-operator tuples as blocks
                                    .or(new TupleProcessor(
                                        n ->
                                            this.process(n),
                                        newElements ->
                                            block(newElements)))
                                    .or(n -> n);

                            return mp.or(stringLiteralProcessor).or(booleanLiteralProcessor).or(fallbackProcessor);
                        }
                    };*/
                }

                @Override
                public astava.parse3.Parser<Node, Node> createParser() {
                    return
                        createLiteralExpander()
                        .pipe(createLabelScopeProcessor())
                        .pipe(createOperatorToBuiltinProcessor());
                }
            };

            astava.parse3.Matcher<Node, Node> macroMatcher = macroParser.parseInit(frontProduction, (p, i) ->
                new astava.parse3.CommonMatcher<>());

            if(macroMatcher.isMatch()) {
                Cursor<Node> middleProduction = macroMatcher.production().cursor();

                while (!middleProduction.atEnd()) {
                    System.out.print(middleProduction.peek());
                    middleProduction.consume();
                }
            }
        } else {
            System.out.println("Failed:");
            failures.stream().collect(Collectors.groupingBy(f -> f.state)).entrySet().stream().sorted((x, y) -> y.getKey().compareTo(x.getKey())).limit(5).forEach(e -> {
                System.out.println("At " + e.getKey() + ", expected:");
                e.getValue().stream().sorted((x, y) -> x.depth - y.depth).forEach(f -> System.out.println(f.parser));
            });
        }
    }
}
