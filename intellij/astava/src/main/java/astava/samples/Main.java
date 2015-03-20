package astava.samples;

import astava.core.Atom;
import astava.core.Node;

import astava.core.Tuple;
import astava.java.*;
import astava.java.gen.ClassGenerator;
import astava.java.gen.CodeAnalyzer;
import astava.macro.*;
import astava.parse.*;
import astava.parse.Matcher;
import astava.parse.Parser;
import astava.parse2.*;
import astava.parse3.*;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
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

        class CMatcher implements astava.parse3.Matcher<Character, Character> {
            private astava.parse3.Parser<Character, Character> parser;
            private Input<Character> input;
            private Boolean result;
            private int depth;
            private Position<Character> start;
            private Position<Character> end;

            CMatcher(astava.parse3.Parser<Character, Character> parser, Input<Character> input, int depth) {
                this.parser = parser;
                this.input = input;
                this.depth = depth;
                start = input.position();
                System.out.println(getIndention() + "Begin parse: " + parser + "...");
            }

            private String getIndention() {
                return IntStream.range(0, depth).mapToObj(d -> "    ").collect(Collectors.joining());
            }

            @Override
            public void visitSuccess() {
                System.out.println(getIndention() + "Success");
                end = input.position();
                result = true;
            }

            @Override
            public void visitFailure() {
                System.out.println(getIndention() + "Failure.");
                end = input.position();
                result = false;
            }

            private StringBuilder production = new StringBuilder();

            @Override
            public void put(Character value) {
                production.append(value);
            }

            @Override
            public Input<Character> production() {
                return new CharSequenceInput(production);
            }

            @Override
            public astava.parse3.Matcher<Character, Character> beginVisit(astava.parse3.Parser<Character, Character> parser, Input<Character> input) {
                return new CMatcher(parser, input, depth + 1);
            }

            @Override
            public boolean isMatch() {
                return result;
            }
        }

        astava.parse3.Parser<Character, Character> grammar = new DelegateParser<Character, Character>() {
            private astava.parse3.Parser<Character, Character> element1 =
                Parse.<Character>isChar('a').then(Parse.consume());
            private astava.parse3.Parser<Character, Character> element2 =
                Parse.<Character>isChar('b').then(Parse.consume());
            private astava.parse3.Parser<Character, Character> element3 =
                Parse.<Character>isChar('c').then(Parse.copy()).then(Parse.consume()).then(Parse.isChar('d')).then(Parse.consume());

            @Override
            protected astava.parse3.Parser<Character, Character> createParser() {
                return
                    ref(() -> this.element1)
                    .or(ref(() -> this.element2))
                    .or(ref(() -> this.element3));
            }
        };

        String chars = "cd";
        Input<Character> production = grammar.parseInit(new CharSequenceInput(chars), (p, i) -> new CMatcher(p, i, 0)).production();

        System.out.print("Production: ");
        while(!production.atEnd()) {
            System.out.print(production.peek());
            production.consume();
        }

        if(1 != 2)
            return;

//        Node classDeclaration = classDeclaration(Modifier.PUBLIC, "MyClass", "java/lang/Object", Arrays.asList(
//            methodDeclaration(Modifier.PUBLIC | Modifier.STATIC, "myMethod", Collections.emptyList(), "I",
//                ret(literal(7))
//            )
//        ));

        /*
        Node classDeclaration = classDeclaration(Modifier.PUBLIC, "MyClass", "java/lang/Object", Arrays.asList(
            methodDeclaration(Modifier.PUBLIC | Modifier.STATIC, "myMethod", Collections.emptyList(), "java/lang/String",
                ret(literal("myString"))
            )
        ));
        */

        /*
        Node classDeclaration = classDeclaration(Modifier.PUBLIC, "MyClass", "java/lang/Object", Arrays.asList(
            methodDeclaration(Modifier.PUBLIC | Modifier.STATIC, "myMethod", Collections.emptyList(), "B",
                ret(add(literal(7), literal(11)))
            )
        ));
        */

        //Processor labelScopeProcessor = createLabelScopeProcessor();


        Node in = new Tuple(Arrays.asList(
            new Tuple(new Atom("scopedLabel"), new Atom("outer")),
            new Tuple(
                new Atom("labelScope"),
                new Tuple(
                    new Tuple(new Atom("scopedLabel"), new Atom("start")),
                    new Tuple(
                        new Atom("labelScope"),
                        new Tuple(
                            new Tuple(new Atom("op"), new Tuple(
                                new Tuple(new Atom("scopedLabel"), new Atom("start")),
                                new Tuple(new Atom("scopedGoTo"), new Atom("start"))
                            )),
                            new Tuple(new Atom("scopedLabel"), new Atom("start")),
                            new Tuple(new Atom("scopedGoTo"), new Atom("start"))
                        )
                    ),
                    new Tuple(new Atom("scopedGoTo"), new Atom("start"))
                )
            )
        ));

        /*
        Node in = new Tuple(
            new Tuple(new Atom("op"), new Tuple(
                new Tuple(new Atom("scopedLabel"), new Atom("start")),
                new Tuple(new Atom("scopedGoTo"), new Atom("start"))
            )),
            new Tuple(new Atom("scopedLabel"), new Atom("start")),
            new Tuple(new Atom("scopedGoTo"), new Atom("start"))
        );
        */

/*        Node n = labelScopeProcessor.process(in);

        System.out.println(in);
        System.out.println("=>");
        System.out.println(n);
        */



        /*Node classDeclaration = classDeclaration(Modifier.PUBLIC, "MyClass", "java/lang/Object", Arrays.asList(
            methodDeclaration(Modifier.PUBLIC | Modifier.STATIC, "myMethod", Collections.emptyList(), "D",
                ret(mul(literal(7.0), literal(11.0)))
            )
        ));


        ClassGenerator generator = new ClassGenerator(classDeclaration);
        Class<?> c = generator.newClass();

        Method m = c.getMethod("myMethod");

        Object result = m.invoke(null, null);

        System.out.println(result);*/

        astava.parse2.Parser<Character, List<Node>, String> p3 = new astava.parse2.Parser<Character, List<Node>, String>() {
            astava.parse2.Parser<Character, List<Character>, String> whitespace =
                // Failure within this multi is always non-informative for errors
                new Multi<>(CharPredicate.isWhitespace()).frame("Skip");
            astava.parse2.Parser<Character, Node, String> symbol =
                (CharPredicate.isLetter()
                .then(new Multi<>(CharPredicate.isLetter())).map(charThenChars -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(charThenChars.getFirst());
                    charThenChars.getSecond().forEach(ch -> sb.append(ch));
                    return new Atom(new Symbol(sb.toString()));
                }));
            astava.parse2.Parser<Character, Node, String> tuple =
                CharPredicate.is('(')
                .ignoreThen(Parsers.ref(() -> this.elements))
                .thenIgnore(CharPredicate.is(')')).map(elements -> new Tuple(elements));
            astava.parse2.Parser<Character, List<Node>, String> elements =
                    whitespace
                    // Failure within this multi is always informative for errors
                    .ignoreThen(new Multi<>(
                        Parsers.ref(() -> this.element)
                    .thenIgnore(whitespace)
                ));
            astava.parse2.Parser<Character, Node, String> element = symbol.or(tuple);

            @Override
            public ParseResult<Character, List<Node>, String> parse(ParseContext<String> ctx, Source<Character> source) {
                return elements.parse(ctx, source);
            }
        };

        String charsSource = "df (";

        Consumer<ParseResult> errorPrinter = new Consumer<ParseResult>() {
            Object frameDescription;

            @Override
            public void accept(ParseResult ctx) {
                accept("Root", ctx);
            }

            private void accept(Object frameDescription, ParseResult ctx) {
                if(ctx instanceof ParseFrame) {
                    ParseFrame f = (ParseFrame)ctx;
                    accept(f.getDescription(), f.getResult());
                }
                else {
                    ParseResult<?, ?, String> pr = (ParseResult<?, ?, String>)ctx;

                    if(pr.isFailure() && !frameDescription.equals("Skip")) {
                        System.out.println(pr.getSource() + ": " + pr.getValueIfFailure());
                    }
                }

                if (ctx.getParent() != null && ctx.getParent() instanceof ParseResult)
                    accept(frameDescription, (ParseResult)ctx.getParent());
            }
        };

        astava.parse2.ParseResult pr2 = p3.thenIgnore(Parsers.<Character, Object>atEnd().frame("Skip"))
            .parse(new RootParseContext<>(), new astava.parse2.CharSequenceSource(charsSource));

        System.out.println("Input: \"" + charsSource + "\"");
        if(pr2.isSuccess()) {
            System.out.println("YAY, captured: " + pr2.getValueIfSuccess());
        } else {
            System.out.println("Bah... ");
            errorPrinter.accept(pr2);

            // How to traverse and report errors?
            // Convert into Source and then process?

            //pr2.stream().filter(ctx -> ctx)

            /*Source<ParseResult> s = null;
            Parser p4 = new FrameDescription(self ->
                new InstanceOf(ParseFrame)
                .or(IsFailure().ignoreThen(self.descriptionIs("Skip")))
            );*/
        }

        if(1 != 2)
            return ;

        Parser p =
            (new CharSet().add('a').add('e').add('i'))
            .then(matcher -> { matcher.consume(); matcher.accept();})
            .then(
                (new CharRouter()
                    .put('a', matcher -> { matcher.consume(); matcher.accept(); } )
                    .put('b', matcher -> { matcher.consume(); matcher.accept(); } )
                ).or(matcher -> {
                    if(Character.isDigit(matcher.peekByte())) {
                        matcher.consume();
                        matcher.accept();
                    } else {
                        matcher.error("Expected digit.");
                        matcher.reject();
                    }
                }).or(matcher -> {
                    if(matcher.peekByte() == '/') {
                        matcher.consume();
                        matcher.accept();
                    } else {
                        matcher.error("Expected '/'.");
                        matcher.reject();
                    }
                })
            )
            .then(new Assert(matcher -> matcher.peekByte() == -1, "Expected end."));

        Matcher matcher2 = new CommonMatcher(new CharSequenceByteSource("a6"),
            matchedElement -> { },
            (index, message) -> {
                System.err.println("Error at " + index + ": " + message);
            });

        p.parse(matcher2);

        if(matcher2.accepted()) {

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

        /*String input =
            "(== 5L 4L)" + "\n" +
            "(+ 1 4)" + "\n" +
            "(|| (< 1 2) false)" + "\n" +
            "(|| (< 1 2) true)" + "\n"
            "";*/

        /*
        String input =
            "(" + "\n" +
            "    (declareVar \"I\" \"x\")" + "\n" +
            "    (assignVar \"x\" 5)" + "\n" +
            "    (accessVar \"x\")" + "\n" +
            ")" + "\n" +
            "";
        */
        /*String input =
            "(" + "\n" +
            "    (declareVar \"I\" \"x\")" + "\n" +
            "    (assignVar \"x\" 5)" + "\n" +
            "    (+ (accessVar \"x\") (accessVar \"x\"))" + "\n" +
            ")" + "\n" +
            "";
        */

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

        /*Collector collector = matchedElement -> {
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
        };*/
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
