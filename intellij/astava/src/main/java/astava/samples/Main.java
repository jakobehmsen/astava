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

        //String input = "(add 8 (sub 9 4))";
        String input = "((scopedLabel x) (labelScope (scopedLabel x)))";
        //String input = "((scopedLabel x) (scopedLabel x))";
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
        // Configure without RecursiveProcessor
        Processor intLiteralProcessor =
            new OperatorProcessor<Symbol>(new Symbol("int"), n -> n, operatorFunc).or(n ->
                n instanceof Atom && ((Atom)n).getValue() instanceof Integer ? new Tuple(new Atom(new Symbol("int")), n) : null);

        return new RecursiveProcessor(intLiteralProcessor);
    }

    public static Processor createLabelScopeProcessor() {
        return new Processor() {
            int scopeCount;

            Processor processor = createLayer();

            Processor createLayer() {
                int id = scopeCount++;

                Hashtable<String, Processor> rules = new Hashtable<>();

                Processor nameProcessor = new AtomProcessor<Symbol, Symbol>(name -> new Symbol(id + name.str));

                MapProcessor mapProcessor = new MapProcessor();

                Function<Processor, Processor> operandsProcessor = p ->
                    new OperandsProcessor(n -> rules.get("base").process(n)).then(p);

                Processor defaultOperandsProcessor =
                    new OperandsProcessor(n -> rules.get("base").process(n))
                    .or(new TupleProcessor(n -> rules.get("base").process(n)))
                    .or(n -> n);

                rules.put("base", mapProcessor
                    .put(new Symbol("scopedLabel"), operandsProcessor.apply(new IndexProcessor()
                        .set(0, new AtomProcessor<Symbol, Symbol>(operator -> new Symbol("label")))
                        .set(1, nameProcessor)))
                    .put(new Symbol("scopedGoTo"), operandsProcessor.apply(new IndexProcessor()
                        .set(0, new AtomProcessor<Symbol, Symbol>(operator -> new Symbol("goTo")))
                        .set(1, nameProcessor)))
                        // Process the first operand of the labelScope form
                    .put(new Symbol("labelScope"), code -> createLayer().process(((Tuple) code).get(1)))
                    .or(defaultOperandsProcessor));

                return rules.get("base");
            }

            @Override
            public Node process(Node code) {
                return processor.process(code);
            }
        };
    }

    private static Processor createLiteralProcessor(Function<Number, Node> literalFunction) {
        return n -> {
            Number number = (Number) ((Atom) ((Tuple) n).get(1)).getValue();
            return literalFunction.apply(number);
        };
    }

    public static Processor createOperatorToBuiltinProcessor() {
        Hashtable<String, Processor> rules = new Hashtable<>();
        Function<Processor, Processor> operandsProcessor = processor ->
            new OperandsProcessor(n -> rules.get("base").process(n)).then(processor);

        MapProcessor mp = new MapProcessor()
            .put(new Symbol("add"),
                operandsProcessor.apply(n -> add((Tuple) ((Tuple) n).get(1), (Tuple) ((Tuple) n).get(2)))
            )
            .put(new Symbol("sub"),
                operandsProcessor.apply(n -> add((Tuple) ((Tuple) n).get(1), (Tuple) ((Tuple) n).get(2)))
            )
            .put(new Symbol("short"), createLiteralProcessor(number -> literal(number.shortValue())))
            .put(new Symbol("int"), createLiteralProcessor(number -> literal(number.intValue())));

        Processor stringLiteralProcessor = n ->
            n instanceof Atom && ((Atom)n).getValue() instanceof String ? literal((String)((Atom)n).getValue()) : null;

        Processor defaultOperandsProcessor =
            new OperandsProcessor(n -> rules.get("base").process(n))
            .or(new TupleProcessor(n -> rules.get("base").process(n)))
            .or(n -> n);

        rules.put("base",
            mp.or(stringLiteralProcessor).or(defaultOperandsProcessor));

        return rules.get("base");
    }
}
