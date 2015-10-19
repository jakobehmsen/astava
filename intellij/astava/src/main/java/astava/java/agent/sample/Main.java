package astava.java.agent.sample;

import astava.java.Descriptor;
import astava.java.agent.ClassLoaderExtender;
import astava.java.agent.ClassNodeExtender;
import astava.java.agent.Parser.ClassNodeExtenderParser;
import astava.java.agent.Parser.ClassNodePredicateParser;
import astava.java.agent.Parser.ParserFactory;
import astava.java.parser.*;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException {
        //MyClass mc = new MyClass("Ignored");

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        ClassResolver classResolver = new DefaultClassResolver(classLoader, Arrays.asList(
            String.class,
            Modifier.class,
            Object.class
        ));

        ClassInspector classInspector = new DefaultClassInspector(classLoader);

        ParserFactory factory = new ParserFactory(classResolver, classInspector);

        ClassNodePredicateParser classNodePredicate = factory.newPredicate()
            //.add("class astava.java.agent.sample.MyClass")
            .add("@astava.java.agent.sample.MyAnnotation(value=333, extra=\"bla\")")
            .add("extends java.lang.Object")
            .add("implements java.io.Serializable");

        //classNodePredicate.add("class astava.java.agent.sample.MyClass extends astava.java.agent.sample.MyOtherClass implements java.io.Serializable");
        //classNodePredicate.add("extends astava.java.agent.sample.MyOtherClass");

        ClassLoaderExtender loader = new ClassLoaderExtender(((ClassNodeExtender) classNode -> {
            factory.newExtender()
                .extend(String.format(
                    "public boolean equals(Object other) {\n" +
                        "   if(other instanceof %1$s) {\n" +
                        "       %1$s otherAsThis = (%1$s)other;\n" +
                        "       return %2$s;\n" +
                        "   }\n" +
                        "   return false;\n" +
                        "}",
                    ASMClassDeclaration.getName(classNode),
                    ASMClassDeclaration.getFields(classNode).stream().map(x -> String.format("this.%1$s.equals(otherAsThis.%1$s)", x.getName())).collect(Collectors.joining(" && "))
                ))
                .extend("public java.lang.String toString() {return \"Was changed\";}")
                .transform(classNode);
        }).when(classNodePredicate));
        //Object mc = Class.forName("astava.java.agent.sample.MyClass").newInstance();
        Object mc1 = Class.forName("astava.java.agent.sample.MyClass", false, loader).newInstance();
        Object mc2 = Class.forName("astava.java.agent.sample.MyClass", false, loader).newInstance();

        try {
            mc1.getClass().getField("someField").set(mc1, "someValue");
            mc2.getClass().getField("someField").set(mc2, "someValue");
            mc1.getClass().getField("someOtherField").set(mc1, "someOtherValue");
            mc2.getClass().getField("someOtherField").set(mc2, "someOtherValue");

            System.out.println(mc1.equals(mc2));

            mc2.getClass().getField("someOtherField").set(mc2, "someOtherValue2");

            System.out.println(mc1.equals(mc2));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
