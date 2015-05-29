package astava.java.parser;

import astava.java.gen.ClassGenerator;
import astava.tree.ClassDom;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        ClassResolver classResolver = new ClassResolver() {
            private Map<String, String> simpleNameToNameMap = Arrays.asList(
                String.class,
                Modifier.class
            )
                .stream().map(x -> new AbstractMap.SimpleImmutableEntry<>(x.getSimpleName(), x.getName()))
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
            private Set<String> nameSet = simpleNameToNameMap.values().stream().collect(Collectors.toSet());

            @Override
            public boolean canResolveAmbiguous(String className) {
                return nameSet.contains(className);
            }

            @Override
            public String resolveSimpleName(String className) {
                return simpleNameToNameMap.get(className);
            }
        };

        /*Parser p = new Parser("public class MyClass { public static int myMethod() { return 666; } }");
        ClassDom c = p.parseClass();
        ClassGenerator generator = new ClassGenerator(c);
        Class<?> gc = generator.newClass();
        Object actualValue = gc.getMethod("myMethod").invoke(null, null);
        System.out.println(actualValue);*/

        MutableClassDomBuilder classBuilder = new Parser("public class MyClass { }").parseClass();
        classBuilder.addMethod(new Parser("public static int myMethod1() { int i = 777; return i; }").parseMethodBuilder());
        classBuilder.addMethod(new Parser("public static String myMethod2() { String str = \"A string\"; return str; }").parseMethodBuilder());
        //classBuilder.addMethod(new Parser("public static int myMethod3() { return Modifier.ABSTRACT; }").parseMethodBuilder());

        /*ClassDomBuilder_OLD classBuilder = new ClassDomBuilder_OLD();

        classBuilder.setFrom(new Parser("public class MyClass { }").parseClass());
        //classBuilder.getMethods().add(new Parser(cs, "public static int myMethod1() { int i = 777; return i; }").parseMethod());
        //classBuilder.getMethods().add(new Parser(cs, "public static String myMethod2() { String str = \"A string\"; return str; }").parseMethod());
        classBuilder.getMethods().add(new Parser("public static int myMethod3() { return Modifier.ABSTRACT; }").parseMethod());*/

        ClassDom classDom = classBuilder.build(classResolver);

        ClassGenerator generator = new ClassGenerator(classDom);
        Class<?> gc = generator.newClass();
        Object actualValue = gc.getMethod("myMethod1").invoke(null, null);
        System.out.println(actualValue);
        actualValue = gc.getMethod("myMethod2").invoke(null, null);
        System.out.println(actualValue);
    }
}
