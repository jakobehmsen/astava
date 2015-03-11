package astava;

import astava.core.Atom;
import astava.core.Node;

import astava.core.Tuple;
import astava.macro.AtomProcessor;
import astava.macro.IndexProcessor;
import astava.macro.MapProcessor;
import astava.macro.Processor;
import parse.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

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

        Processor labelScopeProcessor = createLabelScopeProcess();


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
            if(m.peekByte() == -1)
                m.match();
        });

        String input = "(x u (h tail))\n(gfd  gfd) ";
        ArrayList<Node> elements = new ArrayList<>();
        CommonMatcher matcher = new CommonMatcher(new CharSequenceByteSource(input), 0, null, new BufferCollector(elements));
        parser.parse(matcher, rules);

        if(matcher.matched()) {
            System.out.println(input);
            System.out.println("=>");
            System.out.println(elements);
        }
    }

    public static Processor createLabelScopeProcess() {
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

            Processor createLayer() {
                MapProcessor layer = new MapProcessor();
                createLayer(layer);

                return code -> layer.process(((Tuple)code).get(1));
            }
        };
    }
}
