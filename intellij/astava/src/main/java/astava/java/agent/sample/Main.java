package astava.java.agent.sample;

import astava.java.agent.*;
import astava.java.agent.Parser.ParserFactory;
import astava.java.agent.Parser.SourceCode;
import astava.java.parser.*;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Main {
    private static Boolean o(String str) {
        System.out.println(str);
        return true;
    }

    public static void main(String[] args) throws Exception {
        boolean b = new Object() == new Object();

        //MyClass mc = new MyClass("Ignored");

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        ClassResolver classResolver = new DefaultClassResolver(classLoader, Arrays.asList(
            String.class,
            Modifier.class,
            Object.class
        ));

        ClassInspector classInspector = new DefaultClassInspector(classLoader);

        ParserFactory factory = new ParserFactory(classResolver, classInspector);

        /*factory.modClass(classNode -> {
            return factory.modClass(String.format(
                "public boolean equals(Object other) {\n" +
                    "   if(other instanceof %1$s) {\n" +
                    "       %1$s otherAsThis = (%1$s)other;\n" +
                    "       return %2$s;\n" +
                    "   }\n" +
                    "   return false;\n" +
                    "}",
                ASMClassDeclaration.getName(classNode),
                ASMClassDeclaration.getFields(classNode).stream().map(x -> String.format("this.%1$s.equals(otherAsThis.%1$s)", x.getName())).collect(Collectors.joining(" && "))
            ));
        }).when(
            factory.whenClass("implements java.io.Serializable")
            .and(factory.whenClass("extends java.lang.Object"))
            .and(factory.whenClass("implements java.io.Serializable"))
            .and(factory.whenClass("boolean (...);"))
            .and(factory.whenClass("public;"))
        );*/

        ClassLoaderExtender loader = new ClassLoaderExtender(
            factory.whenClass("implements java.io.Serializable")
            .and(factory.whenClass("extends java.lang.Object"))
            .and(factory.whenClass("boolean (...)"))
            .and(factory.whenClass("public;"))
            .then(
                factory.modClass((classNode, thisClass) -> String.format(
                    "public boolean equals(Object other) {\n" +
                        /*"   if(other instanceof %1$s) {\n" +
                        "       %1$s otherAsThis = (%1$s)other;\n" +
                        "       return %2$s;\n" +
                        "   }\n" +*/
                        "   return false;\n" +
                        "}",
                    thisClass.getName(),
                    thisClass.getFields().stream().map(x -> String.format("this.%1$s.equals(otherAsThis.%1$s)", x.getName())).collect(Collectors.joining(" && "))
                )).andThen(
                    factory
                        .whenMethod("public boolean")
                            //.whenMethod("public void")
                        .and(factory.whenMethod("@" + MyNotNullAnnotation.class.getName()))
                        .then(
                            /*factory.modMethod((classNode, thisClass, methodNode) ->
                                String.format("\njava.lang.System.out.println(\"Starting call to %1$s\");", methodNode.name)).prepend()
                            .andThen(factory.modMethod((classNode, thisClass, methodNode) ->
                                String.format("\njava.lang.System.out.println(\"Ending call to %1$s\");", methodNode.name)).append())
                            .andThen(factory.modMethod((classNode, thisClass, methodNode) ->
                                String.format("\njava.lang.System.out.println(\"Testing arguments...\");") + "\n" +
                                ASMClassDeclaration.getMethod(methodNode).getParameterTypes().stream()
                                .map(p ->
                                    String.format("if(%1$s == null) throw new java.lang.NullPointerException(\"%1$s\");", p.getName())
                                ).collect(Collectors.joining())).prepend())*/

                            /*factory.modMethod((classNode, thisClass, methodNode) ->
                                String.format("\njava.lang.System.out.println(\"Ending call to %1$s\");", methodNode.name)).append()*/

                            /*factory.modMethod((classNode, thisClass, methodNode) ->
                                String.format("\njava.lang.System.out.println(\"Starting call to %1$s\");", methodNode.name) + "\n" +
                                "..." + "\n" + // A ... construct should occur at most once for a particular path
                                String.format("\njava.lang.System.out.println(\"Ending call to %1$s\");", methodNode.name))*/

                            /*factory.modMethod((classNode, thisClass, methodNode) ->
                                    "if(true) {\n" +
                                    "    java.lang.System.out.println(\"Attempting call to method...\");\n" +
                                    "    ...\n" +
                                    "    java.lang.System.out.println(\"Call went fine :)\");\n" +
                                    "} else {\n" +
                                    "    java.lang.System.out.println(\"Rejecting call to method\");\n" +
                                    "    return false;\n" +
                                    "}"
                            )*/

                            /*factory.modMethod((classNode, thisClass, methodNode) ->
                                "try {\n" +
                                "    java.lang.System.out.println(\"Attempting call to method...\");\n" +
                                "    ...\n" +
                                "    java.lang.System.out.println(\"Call went fine :)\");\n" +
                                "} catch (java.lang.NullPointerException e) {\n" +
                                "    java.lang.System.out.println(\"Caught NullPointerException error during call to method\");\n" +
                                "    return false;\n" +
                                //"    throw new java.lang.IllegalArgumentException();" +
                                "} catch (java.lang.IllegalArgumentException e) {\n" +
                                "    java.lang.System.out.println(\"Caught RuntimeException error during call to method\");\n" +
                                "    return false;\n" +
                                "} finally {\n" +
                                "    java.lang.System.out.println(\"Within finally block\");\n" +
                                //"    return false;\n" +
                                "}"
                            )*/

                            /*factory.modMethod((classNode, thisClass, methodNode) ->
                                    "try {\n" +
                                    "    java.lang.System.out.println(\"Attempting call to method...\");\n" +
                                    //"    return true;\n" +
                                    "    ...\n" +
                                    "    java.lang.System.out.println(\"Call went fine :)\");\n" +
                                    "} catch (java.lang.RuntimeException e) {\n" +
                                    "    java.lang.System.out.println(\"Caught error during call to method\");\n" +
                                    //"    return false;\n" +
                                    "}"
                            )*/

                            /*factory.modMethod((classNode, thisClass, methodNode) ->
                                    "    java.lang.System.out.println(\"Attempting call to method...\");\n" +
                                    "    ...\n" +
                                    "    java.lang.System.out.println(\"Call went fine :)\");\n" +
                                    "    return;\n"
                            )*/

                            factory.modMethod((classNode, thisClass, methodNode) ->
                                    "    java.lang.System.out.println(\"Attempting call to method...\");\n" +
                                    "    ...\n" +
                                    "    boolean result = ...;\n" +
                                    "    ...\n" +
                                    "    ...\n" +
                                    "    java.lang.System.out.println(\"Call went fine :)\");\n" +
                                    "    return result;\n"
                            )

                            /*factory.modMethod("@astava.java.agent.sample.MyAnnotation(occurrences=333, extra=\"A boolean return type!!!\")")
                            .andThen(factory.modMethod((classNode, thisClass, methodNode) ->
                                String.format("\njava.lang.System.out.println(\"Starting call to %1$s\");", methodNode.name)
                                + ASMClassDeclaration.getMethod(methodNode).getParameterTypes().stream()
                                    .map(p -> String.format("if(%1$s == null) throw new java.lang.NullPointerException(\"%1$s\");", p.getName()))
                                    .collect(Collectors.joining())
                                    + "\n..."
                                    + String.format("\njava.lang.System.out.println(\"Repeating call to %1$s\");", methodNode.name)
                                    + "\n..."
                                    + String.format("\njava.lang.System.out.println(\"Ending call to %1$s\");", methodNode.name)
                            ))*/
                            /*.andThen(
                                factory.whenParameter("@" + MyNotNullAnnotation.class.getName())
                                .then(factory.modMethodFromParameter((classNode, thisClass, methodNode, p) ->
                                        String.format("if(%1$s == null) throw new NullReferenceException(\"%1$s\")", p.getName())
                                            + "..." // Means insert before
                                        )
                                    )
                            )*/
                        )
                    .andThen(factory
                        //.whenMethod("")
                        .whenMethod("public boolean someOtherMethod3")
                        .then(factory
                            .whenBody("this.?name = ?value;")
                            .then(factory.modBody(captures -> new SourceCode(
                                "java.lang.System.out.println(\"Assigning field " + captures.get("name") + "\");\n" +
                                "this." + captures.get("name") + " = ?value;\n" +
                                "java.lang.System.out.println(\"Assigned field " + captures.get("name") + "\");\n"
                            , captures)))
                        )
                    )
                )
            )
            ,
            classResolver, classInspector);

        /*
        factory.whenParameter("@" + MyNotNullAnnotation.class.getName())")
        .then()
        */

        /*MethodNodePredicateParser methodNodePredicate = factory.newMethodPredicate();

        methodNodePredicate.extend("private;");

        ClassNodePredicateParser classNodePredicate = factory.newPredicate()
            .add("@astava.java.agent.sample.MyAnnotation(occurrences=333, extra=\"bla\")")
            .add("extends java.lang.Object")
            .add("implements java.io.Serializable")
            //.add("public java.lang.String someField;")
            //.add("public java.lang.String toString();")
            .add("boolean (...);")
            .add("public;");

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
                //.extend("public java.lang.String toString() {return \"Was changed\";}")
                //.extend("public java.lang.String toString() {return someField;}")
                .extend("public java.lang.String toString() {return \"Public fields are baaaad!!!...\";}")
                .transform(classNode);
        }).when(classNodePredicate), classResolver, classInspector);*/

        Object mc1 = Class.forName(MyClass.class.getName(), false, loader).newInstance();
        Object mc2 = Class.forName(MyClass.class.getName(), false, loader).newInstance();

        try {
            mc1.getClass().getField("someField").set(mc1, "someValue");
            mc2.getClass().getField("someField").set(mc2, "someValue");
            mc1.getClass().getField("someOtherField").set(mc1, "someOtherValue");
            mc2.getClass().getField("someOtherField").set(mc2, "someOtherValue");

            System.out.println(mc1.equals(mc2));

            mc2.getClass().getField("someOtherField").set(mc2, "someOtherValue2");

            System.out.println(mc1.equals(mc2));

            System.out.println(mc2.getClass().getMethod("someOtherMethod3", String.class, String.class).invoke(mc2, "First", "Second"));
            //System.out.println(mc2.getClass().getMethod("someOtherMethod4", String.class, String.class).invoke(mc2, "First", "Second"));
            //mc2.getClass().getMethod("someOtherMethod3", String.class, String.class).invoke(mc2, "First", null);
            //mc2.getClass().getMethod("someOtherMethod4", String.class, String.class).invoke(mc2, "First", "Second");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
