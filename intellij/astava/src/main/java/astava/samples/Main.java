package astava.samples;

import astava.core.Atom;
import astava.core.Node;

import astava.core.Tuple;
import astava.java.ArithmeticOperator;
import astava.java.Descriptor;
import astava.java.LogicalOperator;
import astava.java.RelationalOperator;
import astava.java.gen.ClassGenerator;
import astava.java.gen.CodeAnalyzer;
import astava.macro.*;
import parse.*;
import sun.dc.pr.PRError;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static astava.java.Factory.*;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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

        Hashtable<String, Parser> rules = new Hashtable<>();

        Parser tupleParser = matcher -> {
            if(matcher.peekByte() == '(') {
                matcher.consume();

                matcher.ignoreWS();

                ArrayList<Node> elements = new ArrayList<>();
                Matcher elementsMatcher = matcher.beginMatch(new BufferCollector(elements));
                rules.get("elements").parse(elementsMatcher);
                if(elementsMatcher.matched()) {
                    if(matcher.peekByte() == ')') {
                        matcher.consume();

                        matcher.put(new Tuple(elements));
                        matcher.match();
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

                matcher.match();
            } else if(Character.isDigit(matcher.peekByte())) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append((char)matcher.peekByte());
                matcher.consume();

                while(Character.isDigit(matcher.peekByte())) {
                    stringBuilder.append((char)matcher.peekByte());
                    matcher.consume();
                }

                if(Character.toUpperCase(matcher.peekByte()) == 'L') {
                    // long
                    matcher.consume();
                    matcher.put(new Atom(Long.parseLong(stringBuilder.toString())));
                } else {
                    // int
                    matcher.put(new Atom(Integer.parseInt(stringBuilder.toString())));
                }

                matcher.match();
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
                    matcher.match();
                }
            }
        };
        rules.put("element", tupleParser.or(atomParser));
        rules.put("elements", matcher -> {
            matcher.ignoreWS();
            Matcher elementMatcher = matcher.beginMatch();
            rules.get("element").parse(elementMatcher);
            while(elementMatcher.matched()) {
                matcher.ignoreWS();
                elementMatcher = matcher.beginMatch();
                rules.get("element").parse(elementMatcher);
            }
            matcher.ignoreWS();
            matcher.match();
        });

        Parser parser = rules.get("elements").then(m -> {
            if (m.peekByte() == -1)
                m.match();
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
        String input =
            "(" + "\n" +
            "    (declareVar \"I\" \"x\")" + "\n" +
            "    (assignVar \"x\" 5)" + "\n" +
            "    (+ (accessVar \"x\") (accessVar \"x\"))" + "\n" +
            ")" + "\n" +
            "";

        //String input = "(+ 8 (* 7 9))";
        //String input = "((scopedLabel x) (labelScope (scopedLabel x)) (labelScope (scopedLabel x)))";
        //String input = "((scopedLabel x) (scopedLabel x))";

        System.out.println("Input:");
        System.out.println(input);
        System.out.println("Parsing...");

        Collector collector = matchedElement -> {
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
        };
        List<Node> elements = new ArrayList<>();
        CommonMatcher matcher = new CommonMatcher(new CharSequenceByteSource(input), 0, null, collector);
        parser.parse(matcher);

        if(!matcher.matched()) {
            System.out.print("Could not parse.");
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
            .or(n ->
                n instanceof Atom && ((Atom) n).getValue() instanceof Integer ? new Tuple(new Atom(new Symbol("int")), n) : null
            )
            .or(n ->
                n instanceof Atom && ((Atom) n).getValue() instanceof Long ? new Tuple(new Atom(new Symbol("long")), n) : null
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
