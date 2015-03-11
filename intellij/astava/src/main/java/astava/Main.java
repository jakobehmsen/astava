package astava;

import astava.core.Atom;
import astava.core.Node;

import astava.core.Tuple;
import astava.java.ASTType;
import astava.java.Descriptor;
import astava.java.gen.ClassGenerator;
import astava.java.gen.CodeAnalyzer;
import astava.macro.AtomProcessor;
import astava.macro.IndexProcessor;
import astava.macro.MapProcessor;
import astava.macro.Processor;
import com.sun.org.apache.xpath.internal.operations.Mod;
import parse.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
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

        Parser tupleParser = (matcher, r) -> {
            if(matcher.peekByte() == '(') {
                matcher.consume();

                matcher.ignoreWS();

                ArrayList<Node> elements = new ArrayList<>();
                Matcher elementsMatcher = matcher.beginMatch(new BufferCollector(elements));
                rules.get("elements").parse(elementsMatcher, rules);
                if(elementsMatcher.matched()) {
                    if(matcher.peekByte() == ')') {
                        matcher.consume();

                        matcher.put(new Tuple(elements));
                        matcher.match();
                    }
                }
            }
        };
        Parser atomParser = (matcher, r) -> {
            if(Character.isLetter(matcher.peekByte())) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append((char)matcher.peekByte());
                matcher.consume();

                while(Character.isLetter(matcher.peekByte())) {
                    stringBuilder.append((char)matcher.peekByte());
                    matcher.consume();
                }

                matcher.put(new Atom(stringBuilder.toString()));
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
            }
        };
        rules.put("element", tupleParser.or(atomParser));
        rules.put("elements", (matcher, r) -> {
            matcher.ignoreWS();
            Matcher elementMatcher = matcher.beginMatch();
            rules.get("element").parse(elementMatcher, rules);
            while(elementMatcher.matched()) {
                matcher.ignoreWS();
                elementMatcher = matcher.beginMatch();
                rules.get("element").parse(elementMatcher, rules);
            }
            matcher.ignoreWS();
            matcher.match();
        });

        Parser parser = rules.get("elements").then((m, p) -> {
            if (m.peekByte() == -1)
                m.match();
        });

        String input = "(sub (short 7) (short 9))";
        List<Node> elements = new ArrayList<>();
        CommonMatcher matcher = new CommonMatcher(new CharSequenceByteSource(input), 0, null, new BufferCollector(elements));
        parser.parse(matcher, rules);

        if(matcher.matched()) {
            System.out.println(input);
            System.out.println("=>");
            System.out.println(elements);

            Processor labelScopeProcessor = createLabelScopeProcessor();
            elements = elements.stream().map(n -> labelScopeProcessor.process(n)).collect(Collectors.toList());
            System.out.println("=>");
            System.out.println(elements);
            Processor operatorProcessor = createOperatorToBuiltinProcessor();
            elements = elements.stream().map(n ->
                operatorProcessor.process(n)).collect(Collectors.toList());
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

    public static Processor createLabelScopeProcessor() {
        return new MapProcessor() {
            int scopeCount;

            {
                createLayer(this);
            }

            void createLayer(MapProcessor layer) {
                int id = scopeCount++;

                Processor nameProcessor = new AtomProcessor<String, String>(name -> id + name);

                layer.put("scopedLabel", new IndexProcessor()
                    .set(0, new AtomProcessor<String, String>(operator -> "label"))
                    .set(1, nameProcessor));
                layer.put("scopedGoTo", new IndexProcessor()
                    .set(0, new AtomProcessor<String, String>(operator -> "goTo"))
                    .set(1, nameProcessor));
                layer.put("labelScope", code -> createLayer().process(code));
            }

            @Override
            protected Node processCode(Node code, Processor processor) {
                // Don't process operands
                return processor.process(code);
            }

            Processor createLayer() {
                MapProcessor layer = new MapProcessor() {
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

    public static Processor createOperatorToBuiltinProcessor() {
        MapProcessor processor = new MapProcessor();

        processor.put("add", n -> add((Tuple)((Tuple)n).get(1), (Tuple)((Tuple)n).get(2)));
        processor.put("sub", n -> sub((Tuple) ((Tuple) n).get(1), (Tuple) ((Tuple) n).get(2)));
        processor.put("short", n -> literal(
            ((Number) ((Atom) ((Tuple) n).get(1)).getValue()).shortValue()
        ));
        processor.put("int", n -> literal(
            ((Number) ((Atom) ((Tuple) n).get(1)).getValue()).intValue()
        ));

        return processor;
    }
}
