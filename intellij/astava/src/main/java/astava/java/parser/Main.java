package astava.java.parser;

import astava.java.gen.ClassGenerator;
import astava.tree.ClassDom;
import astava.tree.ClassDomBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Parser p = new Parser("public class MyClass { public static int myMethod() { return 666; } }");
        ClassDom c = p.parseClass();
        ClassGenerator generator = new ClassGenerator(c);
        Class<?> gc = generator.newClass();
        Object actualValue = gc.getMethod("myMethod").invoke(null, null);
        System.out.println(actualValue);

        ClassDomBuilder classBuilder = new ClassDomBuilder();

        classBuilder.setFrom(new Parser("public class MyClass { }").parseClass());
        classBuilder.getMethods().add(new Parser("public static int myMethod() { int i = 777; return i; }").parseMethod());

        generator = new ClassGenerator(classBuilder);
        gc = generator.newClass();
        actualValue = gc.getMethod("myMethod").invoke(null, null);
        System.out.println(actualValue);
    }
}
