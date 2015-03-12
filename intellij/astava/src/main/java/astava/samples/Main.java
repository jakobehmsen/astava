package astava.samples;

import astava.core.Atom;
import astava.core.Node;

import astava.core.Tuple;
import astava.java.Descriptor;
import astava.java.gen.ClassGenerator;
import astava.java.gen.CodeAnalyzer;
import astava.macro.*;
import parse.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiFunction;
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
            if(Character.isLetter(matcher.peekByte())) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append((char)matcher.peekByte());
                matcher.consume();

                while(Character.isLetter(matcher.peekByte())) {
                    stringBuilder.append((char)matcher.peekByte());
                    matcher.consume();
                }

                matcher.put(new Atom(new Symbol(stringBuilder.toString())));
                matcher.match();
            } else if(Character.isDigit(matcher.peekByte())) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append((char)matcher.peekByte());
                matcher.consume();

                while(Character.isDigit(matcher.peekByte())) {
                    stringBuilder.append((char)matcher.peekByte());
                    matcher.consume();
                }

                matcher.put(new Atom(Integer.parseInt(stringBuilder.toString())));
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

        String input = "(add 8 (sub 9 4))";
        List<Node> elements = new ArrayList<>();
        CommonMatcher matcher = new CommonMatcher(new CharSequenceByteSource(input), 0, null, new BufferCollector(elements));
        parser.parse(matcher);

        if(matcher.matched()) {
            System.out.println(input);
            System.out.println("=>");
            System.out.println(elements);

            Processor processor =
                createLabelScopeProcessor()
                .then(createLiteralExpander())
                .then(createOperatorToBuiltinProcessor());

            elements = elements.stream().map(n ->
                processor.process(n)
            ).collect(Collectors.toList());

            System.out.println("=>");
            System.out.println(elements);

            Tuple expression = (Tuple)elements.get(0);

            CodeAnalyzer analyzer = new CodeAnalyzer(expression);
            String resultType = analyzer.resultType();

            ClassGenerator generator = new ClassGenerator(classDeclaration(Modifier.PUBLIC, "MyClass", Descriptor.get(Object.class), Arrays.asList(
                methodDeclaration(Modifier.PUBLIC | Modifier.STATIC, "myMethod", Collections.emptyList(), resultType, ret(expression))
            )));

            Class<?> c = generator.newClass();
            Object result = c.getMethod("myMethod").invoke(null, null);
            System.out.println("=> " + resultType);
            System.out.println(result);
        }
    }

    private static Function<Node, Symbol> operatorFunc = code ->
    {
        if(code instanceof Tuple) {
            Tuple codeTuple = (Tuple)code;

            if(codeTuple.size() > 0 && codeTuple.get(0) instanceof Atom) {
                Atom firstElement = (Atom)codeTuple.get(0);
                if(firstElement.getValue() instanceof Symbol)
                    return (Symbol)firstElement.getValue();
            }
        }

        return null;
    };

    public static Processor createLiteralExpander() {
        Processor intLiteralProcessor =
            new OperatorProcessor<Symbol>(new Symbol("int"), n -> n, operatorFunc).or(n ->
                n instanceof Atom && ((Atom)n).getValue() instanceof Integer ? new Tuple(new Atom(new Symbol("int")), n) : null);

        return new RecursiveProcessor(intLiteralProcessor);
    }

    public static Processor createLabelScopeProcessor() {
        return new MapProcessor<Symbol>(operatorFunc) {
            int scopeCount;

            {
                createLayer(this);
            }

            void createLayer(MapProcessor<Symbol> layer) {
                int id = scopeCount++;

                Processor nameProcessor = new AtomProcessor<String, String>(name -> id + name);

                layer.put(new Symbol("scopedLabel"), new IndexProcessor()
                    .set(0, new AtomProcessor<String, String>(operator -> "label"))
                    .set(1, nameProcessor));
                layer.put(new Symbol("scopedGoTo"), new IndexProcessor()
                    .set(0, new AtomProcessor<String, String>(operator -> "goTo"))
                    .set(1, nameProcessor));
                layer.put(new Symbol("labelScope"), code -> createLayer().process(code));
            }

            @Override
            protected Node processCode(Node code, Processor processor) {
                // Don't process operands
                return processor.process(code);
            }

            Processor createLayer() {
                MapProcessor layer = new MapProcessor(operatorFunc) {
                    @Override
                    protected Node processCode(Node code, Processor processor) {
                        // Don't process operands
                        return processor.process(code);
                    }
                };
                createLayer(layer);

                return code -> layer.process(((Tuple)code).get(1));
            }
        };
    }

    private static Processor createOperatorOperandsProcessor(Symbol operator, Supplier<Processor> operandProcessorSupplier, Processor processor) {
        return new OperatorProcessor<>(operator, createOperandsProcessor(operandProcessorSupplier).then(processor), operatorFunc);
    }

    private static Processor createOperandsProcessor(Supplier<Processor> operandProcessorSupplier) {
        return n -> {
            if(n instanceof Tuple) {
                Processor operandProcessor = operandProcessorSupplier.get();
                List<Node> newElements = ((Tuple) n).stream().skip(1).map(o ->
                    operandProcessor.process(o)
                ).collect(Collectors.toList());
                newElements.add(0, ((Tuple) n).get(0));
                return new Tuple(newElements);
            }

            return null;
        };
    }

    private static Processor createLiteralProcessor(Symbol operator, Function<Number, Node> literalFunction) {
        return new OperatorProcessor<>(
            operator,
            n -> {
                Number number = (Number) ((Atom) ((Tuple) n).get(1)).getValue();
                return literalFunction.apply(number);
            },
            operatorFunc
        );
    }

    public static Processor createOperatorToBuiltinProcessor() {
        Hashtable<String, Processor> rules = new Hashtable<>();
        Supplier<Processor> operandProcessorSupplier = () -> rules.get("base");
        BiFunction<Symbol, Processor, Processor> operatorOperandsFactory = (operator, processor) ->
            createOperatorOperandsProcessor(operator, operandProcessorSupplier, processor);

        Processor addProcessor = operatorOperandsFactory.apply(
            new Symbol("add"),
            n -> add((Tuple) ((Tuple) n).get(1), (Tuple) ((Tuple) n).get(2)));
        Processor subProcessor = operatorOperandsFactory.apply(
            new Symbol("sub"),
            n -> sub((Tuple) ((Tuple) n).get(1), (Tuple) ((Tuple) n).get(2)));

        Processor shortProcessor = createLiteralProcessor(new Symbol("short"), number -> literal(number.shortValue()));
        Processor intProcessor = createLiteralProcessor(new Symbol("int"), number -> literal(number.intValue()));

        Processor stringLiteralProcessor = n ->
            n instanceof Atom && ((Atom)n).getValue() instanceof String ? literal((String)((Atom)n).getValue()) : null;

        Processor defaultOperandsProcessor = createOperandsProcessor(operandProcessorSupplier);

        rules.put("base",
            addProcessor.or(subProcessor).or(shortProcessor).or(intProcessor).or(stringLiteralProcessor).or(defaultOperandsProcessor));

        return rules.get("base");
    }
}
