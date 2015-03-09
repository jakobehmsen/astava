package astava;

import astava.core.Atom;
import astava.core.Node;

import astava.core.Tuple;
import astava.macro.AtomProcessor;
import astava.macro.IndexProcessor;
import astava.macro.MapProcessor;
import astava.macro.Processor;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

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
        Node n = labelScopeProcessor.process(in);

        System.out.println(in);
        System.out.println("=>");
        System.out.println(n);

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
