package astava.samples;

import astava.core.Atom;
import astava.core.Node;

import astava.core.Tuple;
import astava.java.*;
import astava.java.gen.ClassGenerator;
import astava.java.gen.CodeAnalyzer;
import astava.macro.*;
import astava.parse.*;
import astava.parse.CommonMatcher;
import astava.parse.Matcher;
import astava.parse.Parser;
import astava.parse3.*;
import astava.parse3.Cursor;
import astava.parse3.charsequence.CharSequenceCursor;
import astava.parse3.charsequence.LineColumnCursorStateFactory;
import astava.parse3.charsequence.CharParse;
import astava.parse3.tree.NodeParse;
import astava.parse3.tree.OpRouter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static astava.java.Factory.*;

public class Main {
    private static class CharRouter implements Parser {
        private Hashtable<Character, Parser> charToParserMap = new Hashtable<>();

        @Override
        public void parse(Matcher matcher) {
            char ch = (char)matcher.peekByte();

            Parser parser = charToParserMap.get(ch);

            if(parser != null) {
                parser.parse(matcher);
            } else {
                matcher.error("Expected either of " + charToParserMap.keySet() + ".");
                matcher.reject();
            }
        }

        public CharRouter put(char ch, Parser parser) {
            charToParserMap.put(ch, parser);
            return this;
        }
    }

    private static class CharSet implements Parser {
        private HashSet<Character> chars = new HashSet<>();

        @Override
        public void parse(Matcher matcher) {
            char ch = (char)matcher.peekByte();

            if(chars.contains(ch))
                matcher.accept();
            else {
                matcher.error("Expected either of " + chars + ".");
                matcher.reject();
            }
        }

        public CharSet add(char ch) {
            chars.add(ch);
            return this;
        }
    }

    private static class Assert implements Parser {
        private Predicate<Matcher> assertion;
        private String errorMessage;

        private Assert(Predicate<Matcher> assertion, String errorMessage) {
            this.assertion = assertion;
            this.errorMessage = errorMessage;
        }

        @Override
        public void parse(Matcher matcher) {
            if(assertion.test(matcher))
                matcher.accept();
            else {
                matcher.error(errorMessage);
                matcher.reject();;
            }
        }
    }

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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
                CharParse.<Character>isLetter().then(Parse.copy()).then(Parse.consume()).onceOrMore()
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
            protected astava.parse3.Parser<Character, Node> createParser() {
                return ref(() -> this.elements);
            }
        };

        ArrayList<FailureInfo> failures = new ArrayList<>();
        String chars =
            //"(byte $Number:56)" + "\n" +
            //"$Number:56" + "\n" +
            //"(add $Number:57 $Number:58)" + "\n" +
            //"(whatever whichever)" + "\n" +
            "(scopedLabel lbl) (labelScope (scopedLabel lbl)) (scopedLabel lbl)" +
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
                private astava.parse3.Parser<Node, Node> createLabelScopeProcessor() {
                    return new DelegateParser<Node, Node>() {
                        // Can scopeCount be part of a matcher instead somehow?
                        int scopeCount;
                        Supplier<astava.parse3.Parser<Node, Node>> labelScopeParserSupplier = () ->
                            createParser();

                        @Override
                        public void parse(Cursor<Node> cursor, astava.parse3.Matcher<Node, Node> matcher) {
                            super.parse(cursor, matcher);
                        }

                        @Override
                        protected astava.parse3.Parser<Node, Node> createParser() {
                            int id = scopeCount++;

                            return new DelegateParser<Node, Node>() {
                                @Override
                                protected astava.parse3.Parser<Node, Node> createParser() {
                                    OpRouter labelParser = new OpRouter()
                                        .put(new Symbol("scopedLabel"),
                                            NodeParse.descent(
                                                Parse.<Node, Node>map(a -> new Atom(new Symbol("label")))
                                                    .then(Parse.consume())
                                                    .then(Parse.<Node, Atom>cast(Atom.class).<Node>pipe(Parse.<Atom, Node>map(a ->
                                                        new Atom(id + ((Symbol) a.getValue()).str))))
                                                    .then(Parse.consume())
                                            )
                                        )
                                        .put(new Symbol("scopedGoTo"),
                                            NodeParse.descent(
                                                Parse.<Node, Node>map(a -> new Atom(new Symbol("goTo")))
                                                .then(Parse.consume())
                                                .then(Parse.<Node, Atom>cast(Atom.class).<Node>pipe(Parse.<Atom, Node>map(a ->
                                                    new Atom(id + ((Symbol) a.getValue()).str))))
                                                .then(Parse.consume())
                                            )
                                        )
                                        .put(new Symbol("labelScope"), Parse.<Tuple, Node>success().wrap((cursor, matcher) -> {
                                            return production -> {
                                                Parse.<Tuple, Node, Node, Node>descent(
                                                    tuple -> new ListInput(tuple),
                                                    nodes -> new Tuple(nodes.stream().collect(Collectors.toList())),
                                                    Parse.<Node, Node>consume().then(labelScopeParserSupplier.get())
                                                ).parse(cursor, matcher);
                                            };
                                        }));
                                    return
                                    (
                                        (Parse.<Node, Tuple>cast(Tuple.class).pipe(
                                            labelParser
                                                .or(
                                                    Parse.<Tuple, Node, Node, Node>descent(
                                                        tuple -> new ListInput(tuple),
                                                        nodes -> new Tuple(nodes.stream().collect(Collectors.toList())),
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

                private astava.parse3.Parser<Node, Node> createLiteralExpander() {
                    return new DelegateParser<Node, Node>() {
                        @Override
                        protected astava.parse3.Parser<Node, Node> createParser() {
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
                                            NodeParse.descent(ref(() -> this))
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

                    /*astava.parse3.Parser<Tuple, Node> passOn = Parse.<Tuple, Node>copy().then(Parse.consume());
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
                                    NodeParse.descent(ref(() -> this))
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
                        .multi();*/
                }

                @Override
                protected astava.parse3.Parser<Node, Node> createParser() {
                    return
                        /*createLiteralExpander()
                        .wrap((cursor, matcher) -> {
                            return production -> {
                                System.out.println("@wrap");
                                production.toString();
                            };
                        });*/

                        /*createLiteralExpander()
                        .wrap(new BiFunction<Cursor<Node>, astava.parse3.Matcher<Node, Node>, Consumer<Input<Node>>>() {
                            @Override
                            public Consumer<Input<Node>> apply(Cursor<Node> nodeCursor, astava.parse3.Matcher<Node, Node> nodeObjectMatcher) {
                                return production -> {
                                    System.out.println("wrap@" + this);
                                    production.toString();
                                };
                            }
                        });*/

                        createLiteralExpander()
                        .pipe(createLabelScopeProcessor());

                        //createLiteralExpander();
                        //createLabelScopeProcessor();
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


        if(1 != 2)
            return;

        Hashtable<String, Parser> rules = new Hashtable<>();

        Parser tupleParser = matcher -> {
            if(matcher.peekByte() == '(') {
                matcher.consume();

                matcher.ignoreWS();

                ArrayList<Node> elements = new ArrayList<>();
                Matcher elementsMatcher = matcher.beginMatch(new BufferCollector(elements));
                rules.get("elements").parse(elementsMatcher);
                if(elementsMatcher.accepted()) {
                    if(matcher.peekByte() == ')') {
                        matcher.consume();

                        matcher.put(new Tuple(elements));
                        matcher.accept();
                    }
                }
            }
        };
        Parser atomParser = matcher -> {
            if(Character.isLetter(matcher.peekByte()) || isSpecialSymbolChar((char)matcher.peekByte())) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append((char)matcher.peekByte());
                matcher.consume();

                while(Character.isLetter(matcher.peekByte()) || isSpecialSymbolChar((char)matcher.peekByte())) {
                    stringBuilder.append((char)matcher.peekByte());
                    matcher.consume();
                }

                String str = stringBuilder.toString();

                switch (str) {
                    case "true":
                        matcher.put(new Atom(true));
                        break;
                    case "false":
                        matcher.put(new Atom(false));
                        break;
                    default:
                        matcher.put(new Atom(new Symbol(stringBuilder.toString())));
                }

                matcher.accept();
            } else if(Character.isDigit(matcher.peekByte())) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append((char)matcher.peekByte());
                matcher.consume();

                while(Character.isDigit(matcher.peekByte())) {
                    stringBuilder.append((char)matcher.peekByte());
                    matcher.consume();
                }

                if(matcher.peekByte() == '.') {
                    stringBuilder.append((char)matcher.peekByte());
                    matcher.consume();
                    while(Character.isDigit(matcher.peekByte())) {
                        stringBuilder.append((char)matcher.peekByte());
                        matcher.consume();
                    }

                    if(Character.toUpperCase(matcher.peekByte()) == 'F') {
                        // long
                        matcher.consume();
                        matcher.put(new Atom(Float.parseFloat(stringBuilder.toString())));
                    } else {
                        // int
                        matcher.put(new Atom(Double.parseDouble(stringBuilder.toString())));
                    }
                } else if(Character.toUpperCase(matcher.peekByte()) == 'L') {
                    // long
                    matcher.consume();
                    matcher.put(new Atom(Long.parseLong(stringBuilder.toString())));
                } else {
                    // int
                    matcher.put(new Atom(Integer.parseInt(stringBuilder.toString())));
                }

                matcher.accept();
            } else if(matcher.peekByte() == '\"') {
                StringBuilder stringBuilder = new StringBuilder();
                matcher.consume();

                while(matcher.peekByte() != '\"') {
                    stringBuilder.append((char)matcher.peekByte());
                    matcher.consume();
                }

                if(matcher.peekByte() == '\"') {
                    matcher.consume();
                    matcher.put(new Atom(stringBuilder.toString()));
                    matcher.accept();
                }
            }
        };
        rules.put("element", tupleParser.or(atomParser));
        rules.put("elements", matcher -> {
            matcher.ignoreWS();
            Matcher elementMatcher = matcher.beginMatch();
            rules.get("element").parse(elementMatcher);
            while(elementMatcher.accepted()) {
                matcher.ignoreWS();
                elementMatcher = matcher.beginMatch();
                rules.get("element").parse(elementMatcher);
            }
            matcher.ignoreWS();
            matcher.accept();
        });

        Parser parser = rules.get("elements").then(m -> {
            if (m.peekByte() == -1)
                m.accept();
        });

        Processor processor =
            createLabelScopeProcessor()
            .then(createLiteralExpander())
            .then(createOperatorToBuiltinProcessor());

        String input = "(>>> 512 8)";

        //String input = "(+ 8 (* 7 9))";
        //String input = "((scopedLabel x) (labelScope (scopedLabel x)) (labelScope (scopedLabel x)))";
        //String input = "((scopedLabel x) (scopedLabel x))";

        System.out.println("Input:");
        System.out.println(input);
        System.out.println("Parsing...");

        Collector collector = new Collector() {
            @Override
            public void put(Node matchedElement) {
                System.out.print("Matched: ");
                System.out.println(matchedElement);

                Node processedElement = processor.process(matchedElement);

                System.out.print("Processed to: ");
                System.out.println(processedElement);

                Tuple expression = (Tuple)processedElement;

                CodeAnalyzer analyzer = new CodeAnalyzer(expression);
                String resultType = analyzer.resultType();

                ClassGenerator generator = new ClassGenerator(classDeclaration(Modifier.PUBLIC, "MyClass", Descriptor.get(Object.class), Arrays.asList(
                    methodDeclaration(Modifier.PUBLIC | Modifier.STATIC, "myMethod", Collections.emptyList(), resultType, ret(expression))
                )));

                Class<?> c = null;
                try {
                    c = generator.newClass();
                    Object result = c.getMethod("myMethod").invoke(null, null);
                    System.out.print("Evaluates to (" + resultType + "): ");
                    System.out.println(result);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        };

        ErrorHandler errorHandler = (index, message) -> { };
        List<Node> elements = new ArrayList<>();
        CommonMatcher matcher = new CommonMatcher(new CharSequenceByteSource(input), 0, null, collector, errorHandler);
        parser.parse(matcher);

        if(!matcher.accepted()) {
            System.out.print("Could not astava.parse.");
        }

        System.out.println("Finished parsing.");
    }

    private static boolean isSpecialSymbolChar(char ch) {
        switch (ch) {
            case '+':
            case '-':
            case '/':
            case '*':
            case '%':
            case '&':
            case '|':
            case '<':
            case '>':
            case '=':
                return true;
        }

        return false;
    }

    private static Processor createFallbackProcessor(Processor elementProcessor) {
        return new OperandsProcessor(n -> elementProcessor.process(n))
            .or(new TupleProcessor(
                n -> elementProcessor.process(n),
                newElements -> new Tuple(newElements)))
            .or(n -> n);
    }

    public static Processor createLiteralExpander() {
        return new SelfProcessor(self ->
            new MapProcessor()
                .put(new Symbol("byte"), n -> n)
                .put(new Symbol("short"), n -> n)
                .put(new Symbol("int"), n -> n)
                .put(new Symbol("long"), n -> n)
                .put(new Symbol("float"), n -> n)
                .put(new Symbol("double"), n -> n)
            .or(n ->
                n instanceof Atom && ((Atom) n).getValue() instanceof Integer ? new Tuple(new Atom(new Symbol("int")), n) : null
            )
            .or(n ->
                n instanceof Atom && ((Atom) n).getValue() instanceof Long ? new Tuple(new Atom(new Symbol("long")), n) : null
            )
            .or(n ->
                n instanceof Atom && ((Atom) n).getValue() instanceof Float ? new Tuple(new Atom(new Symbol("float")), n) : null
            )
            .or(n ->
                n instanceof Atom && ((Atom) n).getValue() instanceof Double ? new Tuple(new Atom(new Symbol("double")), n) : null
            )
            .or(createFallbackProcessor(self))
        );
    }

    public static Processor createLabelScopeProcessor() {
        return new DelegateProcessor() {
            int scopeCount;

            @Override
            protected Processor createProcessor() {
                int id = scopeCount++;

                return new SelfProcessor(self -> {
                    Processor nameProcessor = new AtomProcessor<Symbol, Symbol>(name -> new Symbol(id + name.str));

                    MapProcessor mapProcessor = new MapProcessor();

                    return mapProcessor
                        .put(new Symbol("scopedLabel"), forOperands(new IndexProcessor()
                            .set(0, new AtomProcessor<Symbol, Symbol>(operator -> new Symbol("label")))
                            .set(1, nameProcessor)))
                        .put(new Symbol("scopedGoTo"), forOperands(new IndexProcessor()
                            .set(0, new AtomProcessor<Symbol, Symbol>(operator -> new Symbol("goTo")))
                            .set(1, nameProcessor)))
                            // Process the first operand of the labelScope form
                        .put(new Symbol("labelScope"), code -> createProcessor().process(((Tuple) code).get(1)))
                        .or(createFallbackProcessor(n -> self.process(n)));
                });
            }
        };
    }

    public static Processor createOperatorToBuiltinProcessor() {
        return new DelegateProcessor() {
            private Processor createLiteralProcessor(Function<Number, Node> literalFunction) {
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

                    .put(new Symbol("byte"), createLiteralProcessor(number -> literal(number.byteValue())))
                    .put(new Symbol("short"), createLiteralProcessor(number -> literal(number.shortValue())))
                    .put(new Symbol("int"), createLiteralProcessor(number -> literal(number.intValue())))
                    .put(new Symbol("long"), createLiteralProcessor(number -> literal(number.longValue())))
                    .put(new Symbol("float"), createLiteralProcessor(number -> literal(number.floatValue())))
                    .put(new Symbol("double"), createLiteralProcessor(number -> literal(number.doubleValue())))

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
        };
    }
}
